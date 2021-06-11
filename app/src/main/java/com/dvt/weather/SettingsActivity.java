package com.dvt.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_settings_activity);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN );
        LinearLayout dialDevNumber=findViewById(R.id.dialDevNumber);
        dialDevNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dial=new Intent();
                dial.setAction(Intent.ACTION_DIAL);
                dial.setData(Uri.parse("tel:+27735707103"));
                startActivity(dial);
            }
        });
    }
}