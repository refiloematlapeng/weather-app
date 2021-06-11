package com.dvt.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/* this program will help to save user location and places */
/* in this project we're going to use SharedPreferences to save information instead of sqlite */
public class DataRepository {
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public static Context ctx;
    public final static String SavedDetails="DVTWeatherInformation";
    public final String LastWeatherUpdate="LAST_WEATHER_UPDATE";
    public final String LastFCWeatherUpdate="LAST_FCWEATHER_UPDATE";

    @SuppressLint("CommitPrefEdits")
    DataRepository(Context c){ ctx=c;
        sharedPreferences=ctx.getSharedPreferences(SavedDetails,ctx.MODE_PRIVATE);
        editor=sharedPreferences.edit();
    }

    /* this function will help get the last updated location (if the function return null it mean no location information saved before) */
    public String getLastWeatherUpdate(){ return sharedPreferences.getString(LastWeatherUpdate,null); }

    /* this function will help to get the forecastweather */
    public String getLastForecastUpdate(){ return sharedPreferences.getString(LastFCWeatherUpdate,null); }

    /* this function will help to update the last weather information received */
    public void updateLastWeatherInfo(String data){
        editor.putString(LastWeatherUpdate,data);
        editor.commit(); /* informations saved successfully */
    }

    /* this function will help to update the last forecast weather */
    public void updateLastFCWeather(String data){
        editor.putString(LastFCWeatherUpdate,data);
        editor.commit(); /* informations saved successfully */
    }



    /* in case user wanna see the weather of some location/ place  */
    public static final String FavoritePlace="USER_FAVORITE";

    /* this function will return all the favorite place saved in our fav list */
    public JSONArray getFavorites(){
        try{
            String data=sharedPreferences.getString(FavoritePlace,null);
            if(data!=null){ return new JSONArray(data); }
        }
        catch (Exception e){
            Log.e("[EXCEPTION] [58]",e.getMessage()); e.printStackTrace(); }
        return null; /* if the above json throw an excception we will return null value */
    }


    /* this methode will help us to update the user favorite list */
    public void updateUserFavoriteList(JSONArray data) throws JSONException{
        /* before saving the data we have to convert JSONArray into a string */
        String[] s=new String[data.length()];
        for(int i=0;i<data.length();i++){ s[i]=data.getString(i); }

        editor.putString(FavoritePlace, Arrays.toString(s));
        editor.commit(); /* informations saved successfully */
    }


    /*this methode will help us to unsave the city weather from our favorite list  */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean removeItemToFavList(String countryName, String cityName){
        boolean isRemoved=false;
        try {
            JSONArray listOfFavorite=getFavorites();
            if(listOfFavorite!=null){
                for (int i=0;i<listOfFavorite.length();i++){
                    JSONObject rowData=new JSONObject(listOfFavorite.getString(i));
                    if(rowData.has("name")&&rowData.has("sys")){
                        if(countryName.equals(rowData.getJSONObject("sys").getString("country"))&&rowData.getString("name").equals(cityName)){
                            listOfFavorite.remove(i); /* we remove this to our favorite list after we update*/
                            updateUserFavoriteList(listOfFavorite);
                            isRemoved=true;
                        }
                    }
                }
            }
        }
        catch (Exception e){ Log.e("[EXCEPTION] [76]",e.getMessage()); }
        return isRemoved;
    }


    /* this function will help us to add a weather information to our favorite list */
    public boolean addToFavoriteList(String weatherInfo){
        boolean isSaved=false;
        try{
            JSONArray listOfFavorite=getFavorites();
            if(listOfFavorite==null){
                /* nothing was saved before now we have to save them so we will create a JSONArray and save information */
                updateUserFavoriteList(new JSONArray().put(weatherInfo)); isSaved=true;/* cool data is saved */
            }
            else{ updateUserFavoriteList(listOfFavorite.put(weatherInfo)); isSaved=true;  }
        }
        catch (Exception e){ Log.e("[EXCEPTION] [111]",e.getMessage()); }

        return isSaved;
    }

}
