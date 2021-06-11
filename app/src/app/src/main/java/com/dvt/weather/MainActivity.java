package com.dvt.weather;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.time.DayOfWeek;
import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity{
    public RelativeLayout UIScreenView; /* this is the main ui (RelativeLayout) that will contain the view */
    public ImageView WeatherBackgroundPng;  /* the backgroung img that will help to identifier the weather [Cloud,Rain,Clear]  */
    public DataRepository dataManagement; /* this varibale will help to manage data */


    /* the user should allow to app to use the location device so this variable will be use to handle this feature */
    public int PermLocationCode = 1; /* this is the location code */
    public String[] PermLocation = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    /* this is a button that will help the user to review saved location */
    public ImageButton settingButton,savedLocation; /* this is the main setting button on the screen */

    /* this variable will display information about the user location */
    public TextView locationName,UIWeatherType,currentWeaterUnit;
    public TextView minTemp,currentTemp,maxTemp; /* TextView that will display min,current and max temp  */

    /* this is a RecycleView that will display the 5 day forecast weather,  we will be using Recycle View to update the information on our list */
    public RecyclerView forecastListView;



    /* this class will help to display and to update information on our listview about 5 day forecast*/
    public static class ForeCastAdapter extends RecyclerView.Adapter<ForeCastAdapter.ViewHolder>{
        private final LayoutInflater inflater;
        private Context ctx;

        /* were going to init this class with the context, and the json array that contains the forecast of 5 days */
        ForeCastAdapter(Context context,JSONArray data){
            ctx=context; inflater=LayoutInflater.from(context);
            ListOfUniqueDate=new JSONArray();
            this.getUnitDateKey(data);
        }

        /* this methode is called when the view holder is about to be created */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
            View view=inflater.inflate(R.layout.forecast_list_adapter,parent,false);
            return new ViewHolder(view);
        }


        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView nextDayId,weatherTemp; /* here we get all the ui view adapter object */
            public ImageView weatherIcon; /* this is for the icon of the weather */

            ViewHolder(View rowView){
                super(rowView);
                nextDayId=rowView.findViewById(R.id.nextDayId);
                weatherIcon=rowView.findViewById(R.id.weatherIcon);
                weatherTemp=rowView.findViewById(R.id.weatherTemp);
            }
        }


        public JSONArray ListOfUniqueDate; /* this varibale will contains a unique forecast date  [weather date]*/

        /* this methode will fetch a unique date and max temp of the weather */
        private void getUnitDateKey(JSONArray data){
            String lastData=null;
            try {
                for(int i=0;i<data.length();i++){
                    JSONObject DayW=data.getJSONObject(i);
                    if(DayW.has("dt_txt")) {

                        if(lastData==null){
                            lastData=DayW.getString("dt_txt"); /* we have to give him a new information */
                            JSONObject json=new JSONObject();
                            json.put("dt_txt",lastData);
                            json.put("main",DayW.getJSONObject("main"));
                            json.put("weatherType",DayW.getJSONArray("weather").getJSONObject(0).getString("main"));
                            ListOfUniqueDate.put(json); /* we update our JSONArray */
                        }
                        /* the information was set before so now we have to check if it's not the same*/
                        else if(!lastData.split(" ")[0].equals(DayW.getString("dt_txt").split(" ")[0])){
                            lastData=DayW.getString("dt_txt");/* we have a new date available we save it  */
                            JSONObject json=new JSONObject();
                            json.put("dt_txt",lastData);
                            json.put("main",DayW.getJSONObject("main"));
                            json.put("weatherType",DayW.getJSONArray("weather").getJSONObject(0).getString("main"));
                            ListOfUniqueDate.put(json); /* we update our list view */
                        }
                        else{
                            /* we stile have the same date so we must get the height max_temp */
                            JSONObject lastObject=ListOfUniqueDate.getJSONObject(ListOfUniqueDate.length()-1);
                            if(DayW.getJSONObject("main").getLong("temp")>lastObject.getJSONObject("main").getLong("temp")){
                                /* this is the max tempt we have to update it */
                                lastObject.put("main",DayW.getJSONObject("main"));
                            }
                        }
                    }
                }
            }
            catch (Exception e){
                Toast.makeText(ctx,e.getMessage()+" [88]",Toast.LENGTH_LONG).show();
            }
        }


        /* here we have to work with the main view */
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject DayW=ListOfUniqueDate.getJSONObject(position+1);
                /* for the first we have to check if the date of the day that is  selected */
                if(DayW.has("dt_txt")) {

                    /* after we have to get the object main that contains temp|min_temp|max_temp and more informations */
                    /* we must check if the json key exist before we fetch it otherwise it will throw Exepction */
                    if (DayW.has("main")) {
                        JSONObject main=DayW.getJSONObject("main");
                        /* the date and time is separete by a space */
                        String[] truncDate=DayW.getString("dt_txt").split(" ");
                        /* here we split the string the date will be at possition 0 and time 1 but we should check if the lenght == 2 to confirm otherwise it will throw an error */
                        if(truncDate.length==2){
                            /* we get the date so the string will look like 2021-04-24 whe have to get the year,month and day */
                            String[] dataInfoDatails=truncDate[0].split("-"); /* we split it like this cause the date is separated by - */
                            if(dataInfoDatails.length==3){
                                int year=Integer.parseInt(dataInfoDatails[0]);
                                int month=Integer.parseInt(dataInfoDatails[1]);
                                int day=Integer.parseInt(dataInfoDatails[2]);

                                /* all information are ok now so we have to get the day in string  */
                                LocalDate localDate=LocalDate.of(year,month,day);  /* here we get the local date */
                                DayOfWeek dayOfWeek=localDate.getDayOfWeek(); /* here we get the day of week */

                                String dayInText=dayOfWeek.toString().toLowerCase(); /* we have to get now the day of the week in text */

                                holder.nextDayId.setText(dayInText.substring(0,1).toUpperCase()+dayInText.substring(1));
                                int temp=collectedInformation.cvrtKelvinToCelsius(main.getInt("temp_max"));
                                holder.weatherTemp.setText(temp+"⁰");

                                /* we're updating the icon */
                                if(DayW.has("weatherType")){
                                    String wt=DayW.getString("weatherType");
                                    if(wt.equals("Clear")){ holder.weatherIcon.setImageResource(R.drawable.clear2x); }
                                    else if(wt.equals("Rain")){ holder.weatherIcon.setImageResource(R.drawable.rain2x); }
                                    else if(wt.equals("Clouds")){
                                        holder.weatherIcon.setImageResource(R.drawable.partlysunny2x);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
                Log.d("[ERROR] ",e.getMessage()+" VIEW POSITION="+position);
            }
        }


        @Override
        public int getItemCount(){ return 5; } /* here we return 5 cause 5 items will be displayed */

    }




    /* this method will update the ui following with the weatherType */
    public void updateUIWWType(String t) {
        if (collectedInformation.weatherType != t) {
            collectedInformation.weatherType = t; /* we will update unless if the weather type provided id different with the preview */
            if (collectedInformation.weatherType.equals("Clear")) {
                WeatherBackgroundPng.setImageResource(R.drawable.forest_sunny);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.sunny));
            } else if (collectedInformation.weatherType.equals("Clouds")) {
                WeatherBackgroundPng.setImageResource(R.drawable.forest_cloudy);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.cloudy));
            } else if (collectedInformation.weatherType.equals("Rain")) {
                WeatherBackgroundPng.setImageResource(R.drawable.forest_rainy);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.rainy));
            } else {
                /* the default on will be sunny */
                WeatherBackgroundPng.setImageResource(R.drawable.forest_sunny);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.sunny));
            }
        }
    }


    /* this variable will be used to set some of information */
    public static class collectedInformation{
        public  float currentTempure,temp_min,temp_max;
        /* this methode will help us to convert information in kelvin to celsius */
        public static int cvrtKelvinToCelsius(float KMesure){ return (int) Math.round(KMesure - 273.15); }
        public String weatherDescription,locationName;
        public int weatherCode;
        public static String weatherType;
    }



    /* this class will receive information from the service when there's update of weather */
    public class onWeatherUpdate extends BroadcastReceiver{
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            /* we have to get the data that from the service by doing */
            String data=intent.getStringExtra("data");
            try {
                /* if the variable data==null or "" we trow an error */
                if(data==null||data==""){ throw new Exception("0 byte received");}

                JSONObject json=new JSONObject(data);
                if (json.has("cod")) {
                    int requestCode = json.getInt("cod");
                    /* if the request code == 200 (weather information received) */
                    if (requestCode == 200) {
                        if(json.has("main")){
                            JSONObject mainInfo=json.getJSONObject("main");
                            collectedInformation cInfo= new collectedInformation();
                            cInfo.currentTempure=mainInfo.getLong("temp");
                            cInfo.temp_max=mainInfo.getLong("temp_max");
                            cInfo.temp_min=mainInfo.getLong("temp_min");

                            /* here we set the main unit of the temperature */
                            currentWeaterUnit.setText(cInfo.cvrtKelvinToCelsius(cInfo.currentTempure)+"⁰");
                            minTemp.setText(cInfo.cvrtKelvinToCelsius(cInfo.temp_min)+"⁰");
                            currentTemp.setText(cInfo.cvrtKelvinToCelsius(cInfo.currentTempure)+"⁰");
                            maxTemp.setText(cInfo.cvrtKelvinToCelsius(cInfo.temp_max)+"⁰");

                            if(json.has("weather")&&json.has("name")){
                                JSONArray w=json.getJSONArray("weather");
                                JSONObject weather=w.getJSONObject(0);

                                /* here we update the weather type */
                                updateUIWWType(weather.getString("main"));

                                cInfo.weatherDescription=weather.getString("description");
                                cInfo.weatherCode=weather.getInt("id"); /* we be based on this to change the weather infromation */

                                /* here we set de description about the weather */
                                UIWeatherType.setText(cInfo.weatherDescription.toUpperCase()); /* we set the weather type */

                                cInfo.locationName=json.getString("name");
                                locationName.setText(cInfo.locationName);
                            }

                        }
                    } else {
                        /* error we were unable to fetch weather information */
                        myToast.setTextAndShow("Error we were unable to fetch the weather information");
                        Log.e("[ERROR] [285]","Error we were unable to fetch the weather information");
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
                Log.e("[EXCEPTION] [257]",e.getMessage());
            }
        }
    }




    /* this class will update the forecast ui once we receive the weather is updated*/
    public class onForecastWeatherUpdated extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("data");
            try{
                JSONObject json=new JSONObject(data);
                if(json.has("cod")&&json.getInt("cod")==200){
                    /* now we have to get the list of weather (it will be an JSONArray)*/
                    if(json.has("list")){
                        /* when the list is fetch we have to load information in kist view */
                        try {
                            forecastListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            ForeCastAdapter foreCastAdapter = new ForeCastAdapter(getApplicationContext(), json.getJSONArray("list"));
                            forecastListView.setAdapter(foreCastAdapter);
                        }
                        catch (Exception e){
                            myToast.setTextAndShow(e.getMessage());
                            Log.e("[EXCEPTION] [317]",e.getMessage());
                        }
                    }
                }

            }
            catch (Exception e){
                myToast.setTextAndShow(e.getMessage());
                Log.e("[EXCEPTION] [325]",e.getMessage());
            }
        }
    }






    /* this class will detect all the event ex:[when user disable location,enbale and more] */
    public class onDeviceChange extends BroadcastReceiver{
        public String LCDevice="LCDevice"; /* if the object appear with value  it means the location device is desabled */
        @Override
        public void onReceive(Context context, Intent intent) {
            String data=intent.getStringExtra("data");
            try{
                JSONObject json=new JSONObject(data);
                /* this condition will check if the user disable the location feature */
                if(json.has(LCDevice)&&json.getInt(LCDevice)==0){
                    /* we have to ask the user to enable to gps for better experience */
                    myToast.setTextAndShow("GPS Device disabled");
                }

                /* checking if the api key is expired or suspended */
                else if(json.has("ApiKeyError")&& json.getBoolean("ApiKeyError")){
                    myToast.setTextAndShow("ApiKey Error");
                }
            }
            catch (JSONException e){ e.printStackTrace(); Log.e("[EXCEPTION] [316]",e.getMessage()); }
        }
    }


    /* here we declare the variable that will hadle to Toast */
    public UIViewObject.CToastView myToast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_activity_main);
        myToast=new UIViewObject.CToastView(getApplicationContext(),UIScreenView);

        /* here we're setting the ui screen [full screen mode] */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN );

        /* here we have to check first if the user allow the app to use the location feature */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PermLocation, PermLocationCode);
        } else {
            Intent weatherService=new Intent(getApplicationContext(), WeatherUpdate.class);
            weatherService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(weatherService);

            /* here we register our listener that will detect the change of the weather */
            getApplicationContext().registerReceiver(new onWeatherUpdate(), new IntentFilter("DVT_WEATHER_UPDATED"));
            getApplicationContext().registerReceiver(new onForecastWeatherUpdated(),new IntentFilter("DVT_FORECADT_WEATHER_UPDATED"));
            getApplicationContext().registerReceiver(new onDeviceChange(),new IntentFilter("DEVICE_ERROR"));  /* this will help to handle location device error */

            /* on startup of the app we have to load saved informations about the weather */
            dataManagement=new DataRepository(getApplicationContext());
            /* here we update the user weather  */
            if(dataManagement.getLastWeatherUpdate() != null){
                sendBroadcast(new Intent("DVT_WEATHER_UPDATED").putExtra("data",dataManagement.getLastWeatherUpdate())); /* here we request the ui update */
            }
            else{
                /* when the app is on start up we're going to request a kuick weather information  */
                JSONObject json=new JSONObject();
                try {
                    myToast.setTextAndShow("Please Wait...");
                    json.put("ACTION_REFRESH_CURRENT_WEATHER",true);
                    json.put("ACTION_REFRESH_FORECAST_WEATHER",true);
                    sendBroadcast(new Intent("CREFRESH_WEATHER").putExtra("data",json.toString()));
                } catch (JSONException e) { Log.e("[EXCEPTION] [399]",e.getMessage()); }
            }

            /* here we update the forecast weather */
            if(dataManagement.getLastForecastUpdate()!=null){ sendBroadcast(new Intent("DVT_FORECADT_WEATHER_UPDATED").putExtra("data",dataManagement.getLastForecastUpdate())); }


            /* getting all the ui object that will display informations based on the weather */
            UIScreenView = findViewById(R.id.UIScreenView);
            WeatherBackgroundPng = findViewById(R.id.WeatherBackgroundPng);
            settingButton = findViewById(R.id.settingBnt);
            savedLocation = findViewById(R.id.savedLocation);
            locationName = findViewById(R.id.locationName);
            UIWeatherType = findViewById(R.id.UIWeatherType);
            currentWeaterUnit = findViewById(R.id.currentWeaterUnit);

            /* this is the ui elements that show the min,current and max tempure */
            minTemp=findViewById(R.id.minTemp);
            currentTemp=findViewById(R.id.currentTemp);
            maxTemp=findViewById(R.id.maxTemp);

            forecastListView=findViewById(R.id.forecastListView);


            /* this event is called when the user click on the location button  */
            savedLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent locationActivity = new Intent(getApplicationContext(), SavedLocation.class);
                    locationActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(locationActivity);
                }
            });


            /* when the user click on the settings button */
            settingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent settingActivity = new Intent(getApplicationContext(), SettingsActivity.class);
                    settingActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settingActivity);
                }
            });


            /* when user click on the current weather box we have to start the activity that will help to see the weather detaisl*/
            findViewById(R.id.viewCurrentWeather).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent viewDetails=new Intent(getApplicationContext(),UIViewWeatherDetails.class);
                    viewDetails.putExtra("VIEW_DATA_TYPE","CURRENT_WEATHER"); /* here when we wanna see the current weather */
                    viewDetails.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(viewDetails);
                }
            });
        }

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermLocationCode) {
            if (grantResults.length == 2&&grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                /* if the user allow the app were going to restart the activity */
                Intent weatherService=new Intent(getApplicationContext(), WeatherUpdate.class);
                weatherService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(weatherService);

                Intent intent=getIntent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish(); startActivity(intent); /* here we refresh the activity */

            }
            /* else the user click on decline so we're going to show the message and we end the activity*/
            else {
                myToast.setTextAndShow("Permission denied");
                finish(); /* if the user denied the permission we close the app */
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}