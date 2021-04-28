package com.example.cameragps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    // Camera preview
    PreviewView previewView;
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // Text views for displaying data
    TextView gpsCoordinates;
    TextView orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Boolean granted = arePermissionsGranted();
        Toast.makeText(this, "Permissions granted: "
                + granted, Toast.LENGTH_SHORT).show();

        previewView = (PreviewView) findViewById(R.id.previewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindImage(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        gpsCoordinates = findViewById(R.id.gps_coordinates);
        orientation = findViewById(R.id.orientation);

        gpsCoordinates.setTextColor(Color.GREEN);
        orientation.setTextColor(Color.GREEN);
        gpsCoordinates.setTextSize(25);
        orientation.setTextSize(25);

        SensorListener sensorListener = new SensorListener(this, orientation);
        GPSLocationListener locationListener = new GPSLocationListener(this, gpsCoordinates);
    }

    // Requests the permissions needed for Camera and GPS
    private boolean arePermissionsGranted(){
        if ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
                & (checkSelfPermission(Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)){
            Log.v(TAG," All permissionGranted is granted");
            return true;
        } else {
            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},10);
            return false;
        }
    }

    // Sets the live preview for the camera
    private void bindImage(ProcessCameraProvider cameraProvider) {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), ImageProxy::close);

        OrientationEventListener orientationEventListener = new OrientationEventListener(this){
            @Override
            public void onOrientationChanged(int orientation){
            }
        };

        orientationEventListener.enable();
        Preview preview = new Preview.Builder().build();

        // Sets the front or rear facing camera
        int definedCamera = CameraSelector.LENS_FACING_BACK;

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(definedCamera).build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageAnalysis, preview);
    }
}