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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    boolean isSyncVocalEnabled = false;
    ImageView boussoleView;
    TextView angleNorth,latitudeHere,longitudeHere,distance,bearing;
    Button updateButton;
    double latitude,longitude;
    // ** Programmer world : https://www.youtube.com/watch?v=Dqg1A4hy-jI
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private Tracker gpsTracker;

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

        readInterestFile();
        updateButton = findViewById(R.id.button);
        updateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                updateAll();
            }
        });
        latitudeHere = findViewById(R.id.latitudeHere);
        longitudeHere = findViewById(R.id.longitudeHere);
        distance = findViewById(R.id.distance);
        getLocation(latitudeHere,longitudeHere);

        Boussole boussole = new Boussole(this,(SensorManager) getSystemService(SENSOR_SERVICE),findViewById(R.id.boussoleView),findViewById(R.id.angleText));
        boussole.boussoleStart();
        updateAll();
    }

    private void updateAll() {
        gpsTracker.updateHere(latitudeHere,longitudeHere,distance);
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
                isSyncVocalEnabled = !isSyncVocalEnabled;
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
        InputStream is = getResources().openRawResource(R.raw.couloisy_test);

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
            reader.close();
            is.close();

        } catch (IOException e) {
            // Logs error with priority level
            Log.wtf("MyActivity", "Error reading data file on line" + line, e);
            // Prints throwable details
            e.printStackTrace();
        }
    }

    public void getLocation(TextView latitudeHere, TextView longitudeHere){
        gpsTracker = new Tracker(MainActivity.this,latitudeHere,longitudeHere,distance,interestPoints,bearing);
    }

    public void searchInterestInFront(){

    }
}
