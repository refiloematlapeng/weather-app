package com.dvt.weather;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


/* this class will run in the background to provide the weather to the user in real time */
public class WeatherUpdate extends Service implements LocationListener {
    public static double latitude,longitude;
    public CurrentWheather currentWheather;
    public ForecastWeather forecastWeather;
    public CLocationWeather cLocationWeather;
    private final String AppKey="09800f2e4760e9fb28e82719170988c5"; /* this is the key the will help us to have access to the weather api */
    private DataRepository dataManagement;
    private boolean isTheKeyExpired=false; /* this variable will help us to detect if the api key is expired or suspended the response code will 401*/


    /* this class will hep to fetch information about the current wheather  */
    public class CLocationWeather extends AsyncTask<String,String,String> {
        public int requestCode;
        @Override
        protected String doInBackground(String... strings) {
            String dataReceived = "";
            try{
                /* here we're checking if logitude and latitude is set (if the lenght of strings is set witch mean all information are ok) */
                if(strings.length==2) {
                    String AppUrlInfo="lat="+strings[0]+"&lon="+strings[1]+"&appid="+AppKey;

                    String apiCurrentWeatherUrl = "https://api.openweathermap.org/data/2.5/forecast?";
                    URL url = new URL(apiCurrentWeatherUrl+AppUrlInfo);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.addRequestProperty("x-api-key",AppKey);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.connect(); /* here we send our HTTP request*/
                    requestCode = urlConnection.getResponseCode();  /* getting the response code code from the server */

                    /* checking if the response code == 200 [HTTP_OK] */
                    if (requestCode == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder st = new StringBuilder();
                        String r = "";
                        while ((r = br.readLine()) != null) { st.append(r); }

                        dataReceived = st.toString();
                    }
                    else if(requestCode==401){
                        isTheKeyExpired=true; /* the api key is expired or suspended */
                    }
                    else { dataReceived = "[76] Error: " + requestCode; }
                }
                else{
                    /* latitude and longitude param does not exist */
                    dataReceived ="Error latitude and longitude param does not exist";
                }

            }
            catch (Exception e) { dataReceived ="{\"cod\":-1,\"e_mes\":\""+e.getMessage()+" [88]\"}"; }

            return dataReceived;
        }

        /* this methode will be called when the request is done */
        @Override
        protected void onPostExecute(String data) {
            try{
                if(requestCode==200){
                    JSONObject json=new JSONObject(data);
                    dataManagement.updateLastFCWeather(json.toString());
                    sendBroadcast(new Intent("DVT_CUSTOMER_WEATHER_PROVIDER").putExtra("data",data));
                }
                else if(requestCode==401){
                    /* an error accured may be the app key as expired */
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage()+" [94]",Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(data);
        }
    }



    /* this class will help us to detect when user want to refresh manually */
    private class USERMRefreshWeather extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("data");
            try{
                /* at first we have to check if the api key is expired or suspended */
                /* if the key is expired and the user is trying to refresh we alert him */
                if(isTheKeyExpired){
                    sendBroadcast(new Intent("DEVICE_ERROR").putExtra("data","{\"ApiKeyError\":true}"));
                    return;
                }

                JSONObject json=new JSONObject(data);
                if(json.has("ACTION_REFRESH_CURRENT_WEATHER")){
                    currentWheather=new CurrentWheather(); /* me must init the weather class first because it can throw an exception */
                    currentWheather.execute(String.valueOf(latitude),String.valueOf(longitude));
                }
                if(json.has("ACTION_REFRESH_FORECAST_WEATHER")){
                    forecastWeather=new ForecastWeather();
                    forecastWeather.execute(String.valueOf(latitude),String.valueOf(longitude));
                }
                if(json.has("ACTION_GET_CLOCATION_WEATHER")&&json.has("latitude")&&json.has("longitude")){
                    cLocationWeather=new CLocationWeather();
                    cLocationWeather.execute(String.valueOf(json.getDouble("latitude")),String.valueOf(json.getDouble("longitude")));
                }
            }
            catch (Exception e){
                Log.e("[EXCEPTION] ",e.getMessage());
            }
        }
    }



    /* this class will hep to fetch information about the current wheather  */
    public class ForecastWeather extends AsyncTask<String,String,String> {
        public int requestCode;
        @Override
        protected String doInBackground(String... strings) {
            String dataReceived = "";
            try{
                /* here we're checking if logitude and latitude is set (if the lenght of strings is set witch mean all information are ok) */
                if(strings.length==2) {
                    String AppUrlInfo="lat="+strings[0]+"&lon="+strings[1]+"&appid="+AppKey;

                    String apiCurrentWeatherUrl = "https://api.openweathermap.org/data/2.5/forecast?";
                    URL url = new URL(apiCurrentWeatherUrl+AppUrlInfo);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.addRequestProperty("x-api-key",AppKey);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.connect(); /* we connect the app to the network */

                    /* here we get the response code from the server */
                    requestCode = urlConnection.getResponseCode();

                    /* dans ce cas la requette se bien effectuer */
                    if (requestCode == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder st = new StringBuilder();
                        String r = "";
                        while ((r = br.readLine()) != null) { st.append(r); }

                        dataReceived = st.toString();
                    }
                    else { dataReceived = "[76] Error: " + requestCode; }
                }
                else{
                    /* latitude and longitude where not set */
                    dataReceived ="Error longitude and latitude where not set";
                }

            }
            catch (Exception e) { dataReceived ="{\"cod\":-1,\"e_mes\":\""+e.getMessage()+" [88]\"}"; }

            return dataReceived;
        }



        /* this methode will be called when the request is done */
        @Override
        protected void onPostExecute(String data) {
            try{
                if(requestCode==200){
                    JSONObject json=new JSONObject(data);
                    dataManagement.updateLastFCWeather(json.toString());
                    sendBroadcast(new Intent("DVT_FORECADT_WEATHER_UPDATED").putExtra("data",data));
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage()+" [94]",Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(data);
        }
    }



    /* this class will hep to fetch information about the current wheather  */
    public class CurrentWheather extends AsyncTask<String,String,String> {
        public int requestCode;
        @Override
        protected String doInBackground(String... strings) {
            String dataReceived = ""; /* this variable will receive temp informations */
            try{

                /* here we're checking if logitude and latitude is set (if the lenght of strings is set witch mean all information are ok) */
                if(strings.length==2) {
                    String AppUrlInfo="lat="+strings[0]+"&lon="+strings[1]+"&appid="+AppKey;

                    String apiCurrentWeatherUrl = "https://api.openweathermap.org/data/2.5/weather?";
                    URL url = new URL(apiCurrentWeatherUrl+AppUrlInfo);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.addRequestProperty("x-api-key",AppKey);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setUseCaches(false);
                    urlConnection.connect(); /* sending our HTTP request to the server */
                    requestCode = urlConnection.getResponseCode();

                    /* checking if the response code == 200 [HTTP_OK] */
                    if (requestCode == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder st = new StringBuilder(); String r = "";
                        while ((r = br.readLine()) != null) { st.append(r); }
                        dataReceived = st.toString();
                    }
                    else if(requestCode==401) {
                        isTheKeyExpired=true; dataReceived = "Error: " + requestCode;
                    }
                }
                else{
                    /* latitude and longitude where not set */
                    dataReceived ="Error longitude and latitude param does not exist";
                }

            }
            catch (MalformedURLException urlException){
                dataReceived =urlException.getMessage();
                Log.e("[EXCEPTION] [248] ", urlException.getMessage());
            } catch (IOException e) {
                Log.e("[EXCEPTION] [250]", e.getMessage());
                dataReceived =e.getMessage();
            }

            return dataReceived;
        }



        /* this methode will be called when the request is done */
        @Override
        protected void onPostExecute(String data) {
            try{
                if(requestCode==200){
                    JSONObject json=new JSONObject(data);
                    /* here we have to check the response code so we can update infromation
                    * if request code equals 200 we save last update weather information*/
                    dataManagement.updateLastWeatherInfo(json.toString());  /* updated successfully */
                    sendBroadcast(new Intent("DVT_WEATHER_UPDATED").putExtra("data",data));
                }
                else{
                    /* we have to check if its an error code [401 (Invalid AppiK) 404 [Not found] and more] */
                }
            }
            catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage()+" [185]",Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(data);
        }
    }


    /* when the service is created we have to get the location information */
    @Override
    public void onCreate() {
        dataManagement=new DataRepository(getApplicationContext());
        registerReceiver(new USERMRefreshWeather(),new IntentFilter("CREFRESH_WEATHER"));

        /* here we get the systeme location service, after getting the system location service we have to check if the user
        * already allow the app to have access to the location device*/
        /* this variable (LocationManager) will provide the location cord[lat,long] */
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if(latitude!=0&&longitude!=0) {
                            /* here we have to load the current weather with the location cord[lat,long] (we must convert it into string)*/
                            currentWheather = new CurrentWheather();
                            currentWheather.execute(String.valueOf(latitude), String.valueOf(longitude));

                            /* here we load the forecast weather  */
                            forecastWeather = new ForecastWeather();
                            forecastWeather.execute(String.valueOf(latitude), String.valueOf(longitude));
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            },8000,8000);

        }
        else{
            /* the user must allow the app to use the location feature */
            Toast.makeText(getApplicationContext(),"the user should to allow the app to use the location feature",Toast.LENGTH_LONG).show();
        }
    }




    /* this methode is called when the user location have changed [we stock location information]*/
    @Override
    public void onLocationChanged(@NonNull Location location) { latitude=location.getLatitude();longitude=location.getLongitude(); }


    /* this methode is called when the status of the location change */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }


    /* this function is called when user disable the location feature */
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        /* this means the location device is desabled */
        sendBroadcast(new Intent("DEVICE_ERROR").putExtra("data","{\"LCDevice\":0}"));
    }

    /* this methode is called when the location is enabled */
    @Override
    public void onProviderEnabled(@NonNull String provider) { }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
}