package com.example.cameragps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;


public class GPSLocationListener {
    String TAG = "LocationListener";

    Context context;

    // Location
    FusedLocationProviderClient locationClient;
    SettingsClient settingsClient;
    LocationRequest locationRequest;
    LocationSettingsRequest locationSettingsRequest;
    LocationCallback locationCallback;
    Location userLocation;

    // GPS intervals
    // Change these to increase/decrease the time between location requests
    final static int gpsInterval = 500;
    final static int gpsMinInterval = 250;

    TextView gpsTextView;

    public GPSLocationListener(Context context, TextView aTextView){
        this.context = context;
        this.gpsTextView = aTextView;

        locationClient = LocationServices.getFusedLocationProviderClient(context);
        settingsClient = LocationServices.getSettingsClient(context);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettings();
        startLocationUpdates();

        userLocation = new Location(LocationManager.GPS_PROVIDER);
        userLocation.setLatitude(0);
        userLocation.setLongitude(0);

        setGPSTextView();
    }

    private void createLocationRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(gpsInterval);
        locationRequest.setFastestInterval(gpsMinInterval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback(){
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult){
                super.onLocationResult(locationResult);
                userLocation = locationResult.getLastLocation();
                Log.i(TAG, "User location updated. Lat= " + userLocation.getLatitude()
                        + " , lon= " + userLocation.getLongitude());
            }
        };
    }

    private void buildLocationSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                });
    }

    // Set the text view
    private void setGPSTextView(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String latitude = String.valueOf(userLocation.getLatitude());
                String longitude = String.valueOf(userLocation.getLongitude());
                String latlongCombined = latitude + ", " + longitude;
                gpsTextView.setText(latlongCombined);
                handler.postDelayed(this,1500);
            }
        }, 3000 );
    }

    public Location getUserLocation() {
        return userLocation;
    }
}
