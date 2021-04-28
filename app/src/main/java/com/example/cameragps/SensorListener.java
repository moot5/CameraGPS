package com.example.cameragps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class SensorListener implements SensorEventListener {

    String TAG = "SensorListener";

    // Sensor manager
    static SensorManager sensorManager;

    // Sensors
    Sensor accelerometer;
    Sensor magnetometer;

    // Array for sensor data
    float[] accelerometerReading = new float[3];
    float[] magnetometerReading = new float[3];
    private final float[] rotation = new float[9];
    private final float[] inclination = new float[9];
    private float azimuth = 0;
    private float pitch = 0;

    // TextView String
    String sensorData;

    Context context;
    TextView sensorTextView;

    public SensorListener(Context context, TextView aTextView){
        this.context = context;
        this.sensorTextView = aTextView;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magnetometer,SensorManager.SENSOR_DELAY_UI);

        setSensorTextViewData();
    }

    // Read device sensors
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == accelerometer) {
            accelerometerReading = sensorEvent.values;
        }
        else if (sensorEvent.sensor == magnetometer) {
            magnetometerReading = sensorEvent.values;
        }
        boolean success = SensorManager.getRotationMatrix(rotation, inclination, accelerometerReading,
                magnetometerReading);
        if (success) {
            float[] orientation = new float[3];
            float[] gravity = new float[9];
            float[] magnetic = new float[9];

            SensorManager.getOrientation(rotation, orientation);
            azimuth = (float) Math.toDegrees(orientation[0]);
            azimuth = (azimuth + 360) % 360;

            SensorManager.getRotationMatrix(gravity, magnetic, accelerometerReading, magnetometerReading);
            float[] outGravity = new float[9];

            SensorManager.remapCoordinateSystem(gravity, SensorManager.AXIS_X,SensorManager.AXIS_Z, outGravity);
            SensorManager.getOrientation(outGravity, orientation);
            pitch =orientation[1] * 57.2957795f;

            // If needed:
            //float roll = orientation[2] * 57.2957795f;

            sensorData = (int)azimuth + "° , " + (int)pitch + "°";
            Log.i(TAG, "Azimuth =  " + azimuth + " .Device pitch = " + pitch);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    // Set the text view
    private void setSensorTextViewData(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sensorTextView.setText(sensorData);
                handler.postDelayed(this,1000);
            }
        }, 150 );
    }


    public float getAzimuth() {
        return azimuth;
    }

    public float getPitch() {
        return pitch;
    }
}

