package com.dvt.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppLaunch extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        /* at the startup of android we're going to launch the service that will help to get the weather information
        * even if the app is offline */
        Intent weatherService=new Intent(context, WeatherUpdate.class);
        weatherService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(weatherService);
    }
}