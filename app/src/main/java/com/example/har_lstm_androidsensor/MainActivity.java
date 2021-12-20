package com.example.har_lstm_androidsensor;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private final int N_Samples = 125;
    TextView xCoor; // deklarasikan X axis object
    TextView yCoor; // deklarasikan Y axis object
    TextView zCoor; // deklarasikan Z axis object

    private static List<Float> ax, ay, az;
    private float[][] results;
    private HARClassifier classifier;

//    private TextView walkingTextView;
//    private TextView upstairsTextView;
//    private TextView downstairsTextView;
//    private TextView sittingTextView;
//    private TextView standingTextView;
//    private TextView runningTextView;

    private TextView restingTextView;
    private TextView notrestingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xCoor = findViewById(R.id.xcoor); // buat X axis object
        yCoor = findViewById(R.id.ycoor); // buat Y axis object
        zCoor = findViewById(R.id.zcoor); // buat Z axis object

//        walkingTextView = findViewById(R.id.walkingTextView);
//        upstairsTextView = findViewById(R.id.upstairsTextView);
//        downstairsTextView = findViewById(R.id.downstairsTextView);
//        sittingTextView = findViewById(R.id.sittingTextView);
//        standingTextView = findViewById(R.id.standingTextView);
//        runningTextView = findViewById(R.id.runningTextView);

        restingTextView = findViewById(R.id.restingTextView);
        notrestingTextView = findViewById(R.id.notrestingTextView);

        ax = new ArrayList<>();
        ay = new ArrayList<>();
        az = new ArrayList<>();

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        // menambahkan listener. Listener untuk class ini adalah accelerometer_3axis
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL); // fungsi api yang dipakai untuk perubahan screen orientation

        try{
            classifier = new HARClassifier(getApplicationContext()); //try/catch?
        } catch (IOException e){
            Log.e("tfliteSupport", "Error reading model", e);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // cek jenis sensor
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            // tetapkan directions
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            ax.add(sensorEvent.values[0]);
            ay.add(sensorEvent.values[1]);
            az.add(sensorEvent.values[2]);

            xCoor.setText("X : " + x);
            yCoor.setText("Y : " + y);
            zCoor.setText("Z : " + z);
        }
        HARPrediction();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void HARPrediction(){
        if(ax.size() == N_Samples && ay.size() == N_Samples && az.size() == N_Samples){
            //Segment and Reshape Data into fixed window sizes
            float[][][] input_3d = new float[1][125][3];
            for (int n = 0; n < 125; n++){
                input_3d[0][n][0] = toFloatArray(ax)[n];
                input_3d[0][n][1] = toFloatArray(ay)[n];
                input_3d[0][n][2] = toFloatArray(az)[n];
            }
            //Make predictions on input data window in HAR Classifier
            results = classifier.predictions(input_3d);

            //Output predictions to app UI
//            walkingTextView.setText("Walking: \t" + round(results[0][4], 2));
//            upstairsTextView.setText("Upstairs: \t" + round(results[0][2], 2));
//            downstairsTextView.setText("Downstairs: \t" + round(results[0][3], 2));
//            sittingTextView.setText("Sitting: \t" + round(results[0][0], 2));
//            standingTextView.setText("Standing: \t" + round(results[0][1], 2));
//            runningTextView.setText("Running: \t" + round(results[0][5], 2));

            restingTextView.setText("Resting: \t" + round((results[0][4] + results[0][0] + results[0][1]), 3));
            notrestingTextView.setText("Not-Resting: \t" + round((results[0][2] + results[0][3] + results[0][5]), 3));

            ax.clear();
            ay.clear();
            az.clear();
        }
    }

    private float[] toFloatArray(List<Float> list){
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list){
            array[i++] = (f != null ? f :Float.NaN);
        }
        return array;
    }

    //Rounds the output predictions to two decimal places
    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}