package com.example.tabbed;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link page1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class page1 extends Fragment implements OnMapReadyCallback {
    private Marker destinationMarker;
    private LatLng mark = new LatLng(13.731602236427433, 100.77802717608478);
    TextView t_des, t_city, t_tempmax, t_tempmin, t_temp;
    EditText searchcity;
    Button btn;
    GifImageView gif;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public page1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment page1.
     */
    // TODO: Rename and change types and number of parameters
    public static page1 newInstance(String param1, String param2) {
        page1 fragment = new page1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView2);
        if (mapFragment != null) {
            mapFragment.getMapAsync((OnMapReadyCallback) this);
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_page1, container, false);
        t_tempmax = (TextView) view.findViewById((R.id.textViewTempMax));
        t_des = (TextView) view.findViewById((R.id.textViewDescription));
        t_city = (TextView) view.findViewById((R.id.textViewCity));
        t_temp = (TextView) view.findViewById(R.id.textViewTemp);
        t_tempmin = (TextView)view.findViewById((R.id.textViewTempMin));
        searchcity = (EditText)view.findViewById(R.id.search);
        gif = (GifImageView) view.findViewById(R.id.gif);
        btn = (Button)view.findViewById(R.id.mybutton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = searchcity.getText().toString();
                try {
                    get_latlng(location);
                    mapcall(mark);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        mapcall(mark);
        return view;
    }
    public void find_weather(LatLng latLng) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.longitude + "&appid=a8cfa5cb694a9ed21d8c581df262cab7";
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String description = object.getString("description");
                    String tempMin = String.valueOf(main_object.getDouble("temp_min") - 273).substring(0, 4);
                    String tempMax = String.valueOf(main_object.getDouble("temp_max") - 273).substring(0, 4);
                    String temp = String.valueOf(main_object.getDouble("temp") - 273).substring(0, 4);
                    String city = response.getString("name");
                    t_des.setText( description);
                    t_tempmax.setText((tempMax + "°C"));
                    t_city.setText(city);
                    t_tempmin.setText((tempMin+"°C"));
                    t_temp.setText((temp+"°C"));
                    if(description.contains("clouds")){
                        gif.setBackgroundResource(R.drawable.clouddy);
                    }
                    else if((description.contains("rain"))||description.contains("storm")){
                        gif.setBackgroundResource(R.drawable.rain);
                    }
                    else if(description.contains("sky")){
                        gif.setBackgroundResource(R.drawable.clear);
                    }
                    else if(description.contains("snow")){
                        gif.setBackgroundResource(R.drawable.snowwy);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf("ErrorRes", error);
            }
        });
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(jor);
    }
    public void get_latlng(String city){
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=a8cfa5cb694a9ed21d8c581df262cab7";
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject coor = response.getJSONObject("coord");
                    mark = new LatLng(coor.getDouble("lat"), coor.getDouble("lon"));
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String description = object.getString("description");
                    String tempMin = String.valueOf(main_object.getDouble("temp_min") - 273).substring(0, 4);
                    String tempMax = String.valueOf(main_object.getDouble("temp_max") - 273).substring(0, 4);
                    String temp = String.valueOf(main_object.getDouble("temp") - 273).substring(0, 4);
                    String city = response.getString("name");
                    t_des.setText(description);
                    t_tempmax.setText((tempMax + "C"));
                    t_city.setText(city);
                    t_tempmin.setText((tempMin+"°C"));
                    t_temp.setText((temp+"°C"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.wtf("ErrorRes", error);
            }
        });
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(jor);
    }
    public void mapcall(LatLng latLng){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView2);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.mapView2, mapFragment).commit();
        }
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(@NonNull LatLng latLng) {
                            if (destinationMarker != null) {
                                googleMap.clear();
                            }
                            mark = new LatLng(latLng.latitude, latLng.longitude);
                            destinationMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                            find_weather(latLng);

                        }
                    });
                }
            });
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}