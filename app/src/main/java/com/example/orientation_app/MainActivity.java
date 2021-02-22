package com.example.orientation_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView boussoleView;
    TextView angleNorth;

    // ** Programmer world : https://www.youtube.com/watch?v=Dqg1A4hy-jI
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    private List<Interest> interestPoints = new ArrayList<>();

    // ** Programmer world

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boussoleStart();
        readInterestFile();
    }

    // ** Programmer world
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.param:
                //fonction ici pour changer les paramètres
                goToParam();
                return true;
            case R.id.access:
                //fonction ici pour changer l'accessibilité
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goToParam() {
        startActivity(new Intent(MainActivity.this, MenuActivity.class));
    }

    public void onClickMenu(View v) {

    }
    public void readInterestFile(){
        // Read the raw csv file
        InputStream is = getResources().openRawResource(R.raw.interest);

        // Reads text from character-input stream, buffering characters for efficient reading
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        // Initialization
        String line = "";

        // Initialization
        try {
            // Step over headers
            reader.readLine();

            // If buffer is not empty
            while ((line = reader.readLine()) != null) {
                Log.d("MyActivity","Line: " + line);
                // use comma as separator columns of CSV
                String[] tokens = line.split(",");
                // Read the data
                Interest interest = new Interest();
                interest.setName(tokens[0]);
                interest.setLatitude(Float.parseFloat(tokens[1]));
                interest.setLongitude(Float.parseFloat(tokens[2]));

                interestPoints.add(interest);

                // Log the object
                Log.d("My Activity", "Just created: " + interest);
            }

        } catch (IOException e) {
            // Logs error with priority level
            Log.wtf("MyActivity", "Error reading data file on line" + line, e);

            // Prints throwable details
            e.printStackTrace();
        }
    }
    public void readCSVFile() {
        try {
            String csvfileString = this.getApplicationInfo().dataDir + File.separatorChar + "interests.csv";
            File csvfile = new File(csvfileString);

            CSVReader reader = new CSVReader(new FileReader(csvfile.getAbsolutePath()));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                System.out.println("Name"+nextLine[0] + " long: "+nextLine[1] + " lat:"+nextLine[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "The specified file was not found", Toast.LENGTH_SHORT).show();
        }
    }

    public float getDistanceFromHereToInterest(float latitudeHere, float longitudeHere, Interest interest){
        float distance = 0.f;
        double pi = 3.141592653589793;
        double radius = 6378.13;


        return distance;
    }
    // ** Programmer world : https://www.youtube.com/watch?v=Dqg1A4hy-jI ****
    public void boussoleStart(){
        boussoleView = findViewById(R.id.imageView);
        angleNorth = findViewById(R.id.textView2);

        // ** Programmer world **
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener sensorEventListenerAccelerometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                boussoleView.setRotation((float) (-floatOrientation[0] * 180 / 3.14159));
                angleNorth.setText("" + (float) (-floatOrientation[0] * 180 / 3.14159) + "°");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                boussoleView.setRotation((float) (-floatOrientation[0] * 180 / 3.14159));
                angleNorth.setText("" + (float) (-floatOrientation[0] * 180 / 3.14159) + "°");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelerometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }
    // ** Programmer world
}