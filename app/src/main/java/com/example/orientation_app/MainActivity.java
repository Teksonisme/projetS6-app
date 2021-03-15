package com.example.orientation_app;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView boussoleView;
    TextView angleNorth, latitudeHere, longitudeHere, distance, nomInterest, localisationText;
    Button updateButton;

    TextToSpeech t1;

    // ** Programmer world : https://www.youtube.com/watch?v=Dqg1A4hy-jI


    private Tracker gpsTracker;
    private ManagerApp manager;

    private List<Interest> interestPoints = new ArrayList<>();
    private final List<TextView> listTextViews = new ArrayList<>();
    Boussole boussole;

    // ** Programmer world
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpViews();
        updateButton.setOnClickListener(v -> manager.notifyResetLocation());

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        boussole = new Boussole(sensorManager, boussoleView, angleNorth);

        gpsTracker = new Tracker(this, localisationText);

        manager = new ManagerApp(this, boussole, gpsTracker, listTextViews);
        manager.readInterestFile(CSVEnum.COULOISY);
        interestPoints = manager.listOfInterests;
        gpsTracker.interestPoints = interestPoints;

        start();

    }

    private void start() {
        manager.start();
    }

    // ** Programmer world
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            boussole.start();
            manager.unmuteTTS();
        } catch (NullPointerException npe) {

        }

    }

    public void onPause() {
        super.onPause();
        try {
            boussole.pause();
            manager.muteTTS();
        } catch (NullPointerException npe) {

        }
    }

    public void onDestroy() {
        super.onDestroy();
        manager.cleanup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //fonction ici pour changer les paramètres
        if (item.getItemId() == R.id.param) {
            goToParam();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Change the activity to Param Intent
    public void goToParam() {
        startActivity(new Intent(MainActivity.this, MenuActivity.class));
    }

    public void setUpViews() {
        //boussole related
        boussoleView = findViewById(R.id.boussoleView);
        angleNorth = findViewById(R.id.angleText);

        //location related
        listTextViews.add(latitudeHere = findViewById(R.id.latitudeHere));
        listTextViews.add(longitudeHere = findViewById(R.id.longitudeHere));
        listTextViews.add(distance = findViewById(R.id.distance));
        listTextViews.add(nomInterest = findViewById(R.id.nomInterest));
        listTextViews.add(updateButton = findViewById(R.id.button));
        listTextViews.add(localisationText = findViewById((R.id.localisationCheckText)));


        //UI related


    }
}
