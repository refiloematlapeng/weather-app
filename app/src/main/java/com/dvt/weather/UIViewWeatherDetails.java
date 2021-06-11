package com.dvt.weather;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/* this class will show the weather in full details */
public class UIViewWeatherDetails extends AppCompatActivity {
    private JSONObject weatherDetails; /* this variable will contain all the information about the weather */
    private DataRepository dataManagement;
    private RelativeLayout UIScreenView;
    public UIViewObject.CToastView myToast;
    public String weatherType="Clear"; /* this is the default weatherType [Clear,Clouds,Rain] */
    private collectedInformation cInfo;

    /* the ui view about the weather information */
    public TextView locationName,UIWeatherType,currentWeaterUnit;
    public TextView feelsLike,windInfo,humidity; /* this will contain feels like information */
    public TextView savedMes;

    private ImageView WeatherBackgroundPng,savedToFavIco;


    /* this method will update the ui following with the weatherType */
    public void updateUIWWType(String t){
        if(weatherType!=t){
            weatherType=t; /* we will update unless if the weather type provided id different with the preview */
            if(weatherType.equals("Clear")){
                WeatherBackgroundPng.setImageResource(R.drawable.forest_sunny);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.sunny));
            }
            else if(weatherType.equals("Clouds")){
                WeatherBackgroundPng.setImageResource(R.drawable.forest_cloudy);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.cloudy));
            }
            else if(weatherType.equals("Rain")){
                WeatherBackgroundPng.setImageResource(R.drawable.forest_rainy);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.rainy));
            }
            else{
                /* the default on will be sunny */
                WeatherBackgroundPng.setImageResource(R.drawable.forest_sunny);
                UIScreenView.setBackgroundColor(getResources().getColor(R.color.sunny));
            }
        }
    }


    /* this variable will be used to set some of information */
    public static class collectedInformation{
        public  float currentTempure,feels_like,temp_min,temp_max,presure;
        /* this methode will help us to convert information in kelvin to celsius */
        public static int cvrtKelvinToCelsius(float KMesure){ return (int) Math.round(KMesure - 273.15); }

        /* this variable will contain information about the city and country name of the place */
        public String countryName,cityName;
    }


    /* this methode will help us to find out if user already save this place before */
    private boolean isThisPlaceIsSaved(String countryName,String placeName){
        boolean isSaved=false;  /* default value shoulb be false */
        try {
            JSONArray listOfFavorite=dataManagement.getFavorites();
            if(listOfFavorite!=null) {
                for(int i=0;i<listOfFavorite.length();i++){
                    JSONObject favId=new JSONObject(listOfFavorite.getString(i));
                    /* checking if index key exist */
                    if(favId.has("name")&&favId.has("sys")){
                        if(favId.getJSONObject("sys").getString("country").equals(countryName)&&favId.getString("name").equals(placeName)){
                            /* if this condition return true that mean the place is saved already */
                            isSaved=true;
                            break; /* once we found what we're looking for we break the loop */
                        }
                    }
                }
            }
        }
        catch (Exception e){ Log.e("[EXCEPTION] [102]",e.getMessage());  myToast.setTextAndShow("Error"); }

        return isSaved;
    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle dataReceived=getIntent().getExtras();    /* here we get data if data is null unable to view the weather details */

        /* now everything are ok we can display the ui */
        setContentView(R.layout.ui_view_weather_details);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN );

        dataManagement=new DataRepository(getApplicationContext());
        myToast=new UIViewObject.CToastView(getApplicationContext(),UIScreenView);
        cInfo= new collectedInformation();


        /*-----------------------getting all the ui view object ------------------*/
        UIScreenView=findViewById(R.id.UIScreenView);
        WeatherBackgroundPng=findViewById(R.id.WeatherBackgroundPng);

        locationName=findViewById(R.id.locationName);
        UIWeatherType=findViewById(R.id.UIWeatherType);
        currentWeaterUnit=findViewById(R.id.currentWeaterUnit);

        TextView minTemp = findViewById(R.id.minTemp);
        TextView currentTemp = findViewById(R.id.currentTemp);
        TextView maxTemp = findViewById(R.id.maxTemp);

        /* object view that shows temperature details */
        feelsLike=findViewById(R.id.feelsLike);
        windInfo=findViewById(R.id.windInfo);
        humidity=findViewById(R.id.humidity);

        /* gettting object view about adding the place to the favorite */
        /* this linear view will contain the button that will help user to add the place to their favorite*/
        LinearLayout saveToFavorite = findViewById(R.id.saveToFavorite);
        savedToFavIco=findViewById(R.id.savedToFavIco);

        savedMes=findViewById(R.id.savedMes);
        /*------------------------------- [end]---------------------------------*/

        /* before to display the ui we have to get data from the activity */
        if(dataReceived!=null){
            Object data=null; /* this variable will conatain all the information about the weather */

            if(dataReceived.getString("VIEW_DATA_TYPE").equals("CURRENT_WEATHER")) {
                try {
                    data = new JSONObject(dataManagement.getLastWeatherUpdate()); /* here we get the last weather update */
                }
                catch (Exception e){ e.printStackTrace(); Log.e("[EXCEPTION] [48]",e.getMessage()); }
            }
            /* VIEW_XFORECAST_WEATHER this will help to identifier if the user wanna see the   */
            else if(dataReceived.getString("VIEW_DATA_TYPE").equals("VIEW_XFORECAST_WEATHER")){
                /* the date should be provided | we have to check first it's set*/
                String dateForecast=dataReceived.getString("FORECAST_DATE"); /* this shoulb be like something like this 2021-01-01 */
                if(dateForecast!=null) {
                    try {
                        JSONObject read=new JSONObject(dataManagement.getLastForecastUpdate());
                        JSONArray weatherInfo=new JSONArray(); /* this variable will save the weather information */
                        if(read.has("cod")&&read.getInt("cod")==200){
                            JSONArray list=read.getJSONArray("list");
                            for(int i=0;i<list.length();i++){
                                JSONObject lineWI=list.getJSONObject(i); /* here we get the json object */

                                if(lineWI.has("dt_txt")){
                                    /* if the date is the same with the one provided we add the object */
                                    if(lineWI.getString("dt_txt").split(" ")[0].equals(dateForecast)){
                                        weatherInfo.put(lineWI); /* here we add the line of that information */
                                    }
                                }
                            }
                        }

                        /* after we have to check if the weather info has data */
                        if(weatherInfo.length()>0){ data=weatherInfo; }

                    } catch (Exception e) {
                        Log.e("[EXCEPTION] [50]", e.getMessage());
                    }
                }
            }


            /* now we have to display information,we will check firt if the object data is not null||and after we get the type */
            try {
               if(data!=null){
                   /* here weather with multiple informations */
                   if(data instanceof JSONArray){
                       JSONArray arrayInfo=(JSONArray) data;
                       Log.e("[INFO]",arrayInfo.toString());
                   }
                   else if(data instanceof JSONObject){
                       /* here the current weather is provided */
                       JSONObject info=(JSONObject)data;
                       /* here we update the name on the screen */
                       if(info.has("name")){
                           cInfo.cityName=info.getString("name"); /* here we get the name of the city */
                           locationName.setText(cInfo.cityName);

                           /* getting the country name */
                           if(info.has("sys")){ cInfo.countryName=info.getJSONObject("sys").getString("country"); }
                       }

                       /* we will try to update the ui */
                       JSONObject weather=info.getJSONArray("weather").getJSONObject(0); /* here we get the default object */
                       if(weather.has("main")&&weather.has("description")){
                           updateUIWWType(weather.getString("main"));
                           UIWeatherType.setText(weather.getString("description").toUpperCase());
                       }


                       if(info.has("main")){
                           JSONObject main=info.getJSONObject("main");
                           cInfo.currentTempure=main.getLong("temp");
                           cInfo.temp_max=main.getLong("temp_max");
                           cInfo.temp_min=main.getLong("temp_min");
                           cInfo.feels_like=main.getLong("feels_like");
                           cInfo.presure=main.getLong("pressure");


                           /* here we set the main unit of the temperature */
                           currentWeaterUnit.setText(cInfo.cvrtKelvinToCelsius(cInfo.currentTempure)+"⁰");
                           minTemp.setText(cInfo.cvrtKelvinToCelsius(cInfo.temp_min)+"⁰");
                           currentTemp.setText(cInfo.cvrtKelvinToCelsius(cInfo.currentTempure)+"⁰");
                           maxTemp.setText(cInfo.cvrtKelvinToCelsius(cInfo.temp_max)+"⁰");

                           /* feels like */
                           feelsLike.setText(cInfo.cvrtKelvinToCelsius(cInfo.feels_like)+"⁰");

                           /* humidity */
                           if(main.has("humidity")) { humidity.setText(main.getInt("humidity")+"%"); }

                       }


                       /* here we're going to calculate information about wind */
                       if(info.has("wind")) {
                           JSONObject wind = info.getJSONObject("wind");
                           windInfo.setText(wind.getString("speed")+"m/s");
                       }

                       /* here we update our saved to favorite bnt  */
                       if(isThisPlaceIsSaved(cInfo.countryName,cInfo.cityName)){
                           savedToFavIco.setImageResource(R.drawable.ic_baseline_fullheart_24);
                           savedMes.setText(R.string.savedAlready);
                       }


                       /* here we're going to help user to add the place to his favorite list */
                       saveToFavorite.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                                /*  now we have to check if this has been added to our fav list if yes we unsave*/
                               if(isThisPlaceIsSaved(cInfo.countryName,cInfo.cityName)){
                                   /* we have to remove it from our favorite list */
                                   if(dataManagement.removeItemToFavList(cInfo.countryName,cInfo.cityName)){
                                       savedToFavIco.setImageResource(R.drawable.ic_baseline_heart_border_24);
                                       savedMes.setText(R.string.savedThis);
                                   }
                                   else{
                                       myToast.setTextAndShow("Error we we're unable to remove this from your favorite list");
                                       savedToFavIco.setImageResource(R.drawable.ic_baseline_fullheart_24);
                                   }
                               }
                               else{
                                   /* if it's not yet saved we add it */
                                   if(dataManagement.addToFavoriteList(info.toString())){
                                       savedToFavIco.setImageResource(R.drawable.ic_baseline_fullheart_24);
                                       savedMes.setText(R.string.savedAlready);
                                   }
                                   else{
                                       myToast.setTextAndShow("Error we we're unable to save this place");
                                       savedToFavIco.setImageResource(R.drawable.ic_baseline_fullheart_24);
                                   }
                               }
                           }
                       });
                   }
               }
               else{ Log.e("[ERROR] [282]","The object data is null"); }
            }
            catch (Exception e) { e.printStackTrace();Log.e("[EXCEPTION] [287]",e.getMessage()); }
        }
        else{
            /* error we we're unable to display the weather details */
            myToast.setTextAndShow("Error we were unable to display the weather details");
        }

    }
}