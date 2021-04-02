package com.example.orientation_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView boussoleView, logo;
    private TextView angleNorth, latitudeHere, longitudeHere, distance, nomInterest, localisationText, syntheseVocaleText;
    private Button updateButton, syntheseButton;

    public ImageView background;

    private ManagerApp manager;
    private Boussole boussole;

    private final List<TextView> listTextViews = new ArrayList<>();

    private int requestCode = 1;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setUpViews();
        updateButton.setOnClickListener(v -> manager.notifyResetLocation());
        syntheseButton.setOnClickListener(v -> manager.changeSyntheseVocaleMode(syntheseButton));

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        boussole = new Boussole(sensorManager, boussoleView, angleNorth);

        Tracker gpsTracker = new Tracker(this, localisationText);

        manager = new ManagerApp(this, boussole, gpsTracker, listTextViews);

        getSharedPreferences();
        if (Config.isSyntheseActivated)
            syntheseVocaleText.setText("La synthèse vocale est activée");
        else syntheseVocaleText.setText("La synthèse vocale est désactivée");

        start();

    }

    private void start() {
        changeTheme(listTextViews);
        manager.start();
    }


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
            manager.restartTTS();

        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

    }

    public void onPause() {
        super.onPause();
        try {
            boussole.pause();
            putSharedPreferences();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        manager.cleanup();
        boussole.pause();
        putSharedPreferences();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.param) {
            goToParam();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Passage à l'activité paramètre
    public void goToParam() {
        startActivityForResult(new Intent(MainActivity.this, MenuActivity.class), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ** Récupère les valeurs modifiées dans l'écran menu **
        if (manager.getLastMapUsed() == data.getExtras().get("MAP_ID") && manager.getLastMapUsed() == data.getExtras().get("ANGLE")) {
            Log.d("Intent comeback", "Angle and map unchanged");
        } else {
            Log.d("Intent comeback", "New Angle" + Config.DEFAULT_RESEARCH_ANGLE + " and map " + Config.CURRENT_MAP_ID.name());
            manager.start();
        }
        if (Config.isSyntheseActivated)
            syntheseVocaleText.setText("La synthèse vocale est activée");
        else syntheseVocaleText.setText("La synthèse vocale est désactivée");

        changeTheme(listTextViews);
    }

    // Charge toutes les views devant être modifiés dans des variables
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

        //Accessibility related
        syntheseButton = findViewById(R.id.buttonSyntheseVocale);
        syntheseVocaleText = findViewById(R.id.syntheseVocale);

        //UI related
        background = findViewById(R.id.backgroundMain);
        logo = findViewById(R.id.logo);


        listTextViews.add(findViewById(R.id.textCePointADistance));
        listTextViews.add(findViewById(R.id.textCePointEst));
        listTextViews.add(findViewById(R.id.angleText2));
        listTextViews.add(angleNorth);
        listTextViews.add(findViewById(R.id.syntheseVocale));
    }

    // Permet de changer le thème ( blanc ou nuit ) de l'application.
    public void changeTheme(List<TextView> theViews) {

        int i = 2;
        // 0, 1, 3, 4 !! Don't change
        // To Dark Theme
        if (Config.isIsDarkTheme) {
            while (i < theViews.size()) {
                theViews.get(i).setTextColor(Color.WHITE);
                if (i == 2)
                    i += 3;
                else i++;
            }
            boussole.changeImage(R.drawable.boussole_darktheme);
            background.setImageResource(R.drawable.fondnoir);
            logo.setImageResource(R.drawable.logo_application_toon_darktheme);
        } else {
            // To White Theme
            while (i < theViews.size()) {
                theViews.get(i).setTextColor(getResources().getColor(R.color.lightGray));
                if (i == 2)
                    i += 3;
                else i++;
            }
            boussole.changeImage(R.drawable.boussole);
            background.setImageResource(R.drawable.fondblanc);
            logo.setImageResource(R.drawable.logo_application_toon);
        }
    }

    // Sauvegarde les valeurs de certaines données à la fermeture de l'application.
    public void putSharedPreferences() {
        SharedPreferences sP = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor sPeditor = sP.edit();
        sPeditor.putBoolean("DarkTheme", Config.isIsDarkTheme);
        sPeditor.putBoolean("SynthèseVocale", Config.isSyntheseActivated);
        sPeditor.putInt("IdCarte", CSVEnum.findIdOfCSV(Config.CURRENT_MAP_ID));
        sPeditor.commit();
        Log.d("Shared preference value", "Dark Theme : " + getResources().getString(R.string.isDarkTheme) + " , Synth Vocal : " + getResources().getString(R.string.isSyntheseActivated));
        Log.d("Shared config value", "Dark Theme : " + Config.isIsDarkTheme + " , Synth Vocal : " + Config.isSyntheseActivated);
    }

    // Récupère les données sauvegardés au lancement de l'application ( Ici l'état de la synthèse vocale et du mode nuit )
    public void getSharedPreferences() {
        SharedPreferences sP = getPreferences(Context.MODE_PRIVATE);
        Config.isSyntheseActivated = sP.getBoolean("SynthèseVocale", Config.isSyntheseActivated);
        Config.isIsDarkTheme = sP.getBoolean("DarkTheme", Config.isIsDarkTheme);
        Config.CURRENT_MAP_ID = CSVEnum.findCSVofId(sP.getInt("IdCarte", CSVEnum.findIdOfCSV(Config.CURRENT_MAP_ID)));
        Log.d("Boolean value ", " DT : " + Config.isIsDarkTheme + " Synthese Vocale " + Config.isSyntheseActivated);

    }
}
