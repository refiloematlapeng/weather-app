package com.dvt.weather;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/* this class will help to customise some of ui object as toast,dialog and more */
public class UIViewObject {
    UIViewObject(Context c){}

    /* here we're going to create a class that will customize the toast view */
    public static class CToastView extends Toast {
        public TextView tView; /* this is the text view that will contian the image */
        /* we will init the class with the context and the screen object */
        CToastView(Context c, RelativeLayout screen){
            super(c);
            /* here we get the main  */
            View toastView= LayoutInflater.from(c).inflate(R.layout.launcher,(ViewGroup) screen,false);
            tView=toastView.findViewById(R.id.ToastTView);
            setView(toastView); setDuration(Toast.LENGTH_LONG);
            setGravity(Gravity.CENTER_VERTICAL,0,0);
        }

        /* this methode will help us to set the text and to show the toast  */
        public void setTextAndShow(String s){
            tView.setText(s);
            show();
        }
    }
}
