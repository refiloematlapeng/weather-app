package com.dvt.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class SavedLocation extends AppCompatActivity {
    public RecyclerView recyclerView; /* this is a list ui that will show saved location */
    public DataRepository dataManagement;    /* this class contains all informations about current and forecast */
    public static JSONArray ListOfFavorite;    /* this class will contain list  */
    public UIViewObject.CToastView myToast;


    /* this class will display all the information about favoriite place */
    public class FavLWeatherAdapter extends RecyclerView.Adapter<FavLWeatherAdapter.ViewHolder>{
        private Context ctx;
        private LayoutInflater inflater;
        public FavLWeatherAdapter(Context c,JSONArray data){ inflater=LayoutInflater.from(c); ListOfFavorite=data; }

        public int cvrtKelvinToCelsius(float KMesure){ return (int) Math.round(KMesure - 273.15); }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=inflater.inflate(R.layout.favorite_adapter,parent,false);
            return new ViewHolder(view);
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView cityName,cityNameView,countryName;
            public TextView minAndMaxTemp; /* this text view will show the min and max temp */
            public LinearLayout lineView; /* this will help us to perform something like delete... */

            ViewHolder(View rowView){
                super(rowView);
                cityName=rowView.findViewById(R.id.cityName);
                countryName=rowView.findViewById(R.id.countryName);
                cityNameView=rowView.findViewById(R.id.cityNameView); /* this is the second city textview */
                lineView=rowView.findViewById(R.id.lineView);
                minAndMaxTemp=rowView.findViewById(R.id.minAndMaxTemp);
            }
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try{
                JSONObject rowData=new JSONObject(ListOfFavorite.getString(position));
                if(rowData.has("name")&&rowData.has("sys")&&rowData.has("main")){
                    holder.cityName.setText(rowData.getString("name"));
                    holder.cityNameView.setText(rowData.getString("name"));

                    /* here we're going to show the country name */
                    holder.countryName.setText(" - "+rowData.getJSONObject("sys").getString("country"));

                    JSONObject main=rowData.getJSONObject("main");
                    float tempMin=main.getLong("temp_min");
                    float tempMax=main.getLong("temp_max");

                    holder.minAndMaxTemp.setText(" | Temperature: "+cvrtKelvinToCelsius(tempMin)+"⁰ - "+cvrtKelvinToCelsius(tempMax)+"⁰");

                    holder.lineView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            myToast.setTextAndShow("Do you want to remove this?");
                            myToast.setTextAndShow("Please contact the developer for more info...");
                            return true;
                        }
                    });
                }
                else{
                    myToast.setTextAndShow("Error");
                    Log.e("[ERROR] [85]","json key error");
                }
            }
            catch (Exception e){ Log.e("[EXCEPTIOM] [66]",e.getMessage()); }
        }

        @Override
        public int getItemCount() { return ListOfFavorite.length(); }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_saved_location);

        RelativeLayout UIScreenView=findViewById(R.id.UIScreenView);

        dataManagement=new DataRepository(getApplicationContext());
        myToast=new UIViewObject.CToastView(getApplicationContext(),UIScreenView);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN );
        recyclerView=findViewById(R.id.favList); /* getting the recycle view */


        /* this variable will help to view  */
        LinearLayout myCurrentLocation = findViewById(R.id.myCurrentLocation); /* this when the user click on his current location to see the details */
        myCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        ImageButton closeBnt = findViewById(R.id.closeBnt);   /* this button will help to close the activity when user click on the button */
        closeBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { finish(); }
        });



        /* we're going to display the ui that will help the user to save the favorite location */
        JSONArray data= null;
        try { data = dataManagement.getFavorites(); }
        catch (Exception e) { e.printStackTrace(); }
        if(data!=null&&data.length()>0){
            TextView nothingToShow=findViewById(R.id.nothingToShow);
            nothingToShow.setVisibility(View.GONE);

            /* here data is set so we have to test them and see  */
            FavLWeatherAdapter favLWeatherAdapter=new FavLWeatherAdapter(getApplicationContext(),data);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(favLWeatherAdapter);

        }


    }
}