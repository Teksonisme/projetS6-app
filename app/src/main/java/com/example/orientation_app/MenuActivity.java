package com.example.orientation_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    CheckBox checkAngle5, checkAngle8, checkAngle12, checkAngle15, checkCarte1, checkCarte2, checkCarte3, checkCarte4;
    ImageView background, logo;
    Switch switchSyntheseVocale, switchDarkTheme;

    CheckBox lastCheckAngle = null, lastCheckCarte = null;
    List<CheckBox> checkBoxAngleList = new ArrayList<>();
    List<CheckBox> checkBoxCarteList = new ArrayList<>();
    List<TextView> listNomCarte = new ArrayList<>();

    private View.OnClickListener oclCarte = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox vCheck = (CheckBox) v;
            Log.d("Clicked!", "Clicked " + vCheck.getContentDescription());
            if (vCheck.getId() != lastCheckCarte.getId()) onCheckboxClicked(v);
            else {
                Log.d("Already Checked!", "" + vCheck.getContentDescription());
                vCheck.setChecked(true);
            }
            Log.d("Config changed", "Angle = " + Config.DEFAULT_RESEARCH_ANGLE + " Map" + Config.CURRENT_MAP_ID.name());
            lastCheckCarte = vCheck;

        }
    };
    private View.OnClickListener oclAngle = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckBox vCheck = (CheckBox) v;
            Log.d("Clicked!", "Clicked " + vCheck.getText());
            if (vCheck.getId() != lastCheckAngle.getId()) onCheckboxClicked(v);
            else {
                Log.d("Already Checked!", "" + vCheck.getText());
                vCheck.setChecked(true);
            }
            Log.d("Config changed", "Angle = " + Config.DEFAULT_RESEARCH_ANGLE + " Map" + Config.CURRENT_MAP_ID.name());
            lastCheckAngle = vCheck;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        setUpViews();

        switchSyntheseVocale.setOnClickListener(v -> Config.isSyntheseActivated = !Config.isSyntheseActivated);
        switchDarkTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Config.isIsDarkTheme = !Config.isIsDarkTheme;
                changeTheme();
            }
        });
        changeTheme();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                setResult(RESULT_OK, new Intent().putExtra("MAP_ID", Config.CURRENT_MAP_ID).putExtra("ANGLE", Config.DEFAULT_RESEARCH_ANGLE));

                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // On récupère les vues que l'on souhaite modifiées et utilisées
    private void setUpViews() {
        checkBoxAngleList.add(checkAngle5 = findViewById(R.id.checkBox5));
        checkBoxAngleList.add(checkAngle8 = findViewById(R.id.checkBox8));
        checkBoxAngleList.add(checkAngle12 = findViewById(R.id.checkBox12));
        checkBoxAngleList.add(checkAngle15 = findViewById(R.id.checkBox15));

        switchSyntheseVocale = findViewById(R.id.switchSyntheseVocale);
        switchDarkTheme = findViewById(R.id.switchDarkTheme);

        checkBoxCarteList.add(checkCarte1 = findViewById(R.id.checkCarte1));
        checkBoxCarteList.add(checkCarte2 = findViewById(R.id.checkCarte2));
        checkBoxCarteList.add(checkCarte3 = findViewById(R.id.checkCarte3));
        checkBoxCarteList.add(checkCarte4 = findViewById(R.id.checkCarte4));

        listNomCarte.add(findViewById(R.id.textChangeAngle));
        listNomCarte.add(findViewById(R.id.textNomCarte1));
        listNomCarte.add(findViewById(R.id.textNomCarte2));
        listNomCarte.add(findViewById(R.id.textNomCarte3));
        listNomCarte.add(findViewById(R.id.textNomCarte4));


        background = findViewById(R.id.backgroundMenu);
        logo = findViewById(R.id.logoMenu);

        for (CheckBox checkBox : checkBoxAngleList) {
            checkBox.setOnClickListener(oclAngle);
            if (Double.parseDouble(String.valueOf(checkBox.getContentDescription())) == Config.DEFAULT_RESEARCH_ANGLE) {
                checkBox.setChecked(true);
                lastCheckAngle = checkBox;
            }
        }
        for (CheckBox checkBox : checkBoxCarteList) {
            checkBox.setOnClickListener(oclCarte);
            if (String.valueOf(checkBox.getContentDescription()).equals(Config.CURRENT_MAP_ID.name())) {
                checkBox.setChecked(true);
                lastCheckCarte = checkBox;
            }
        }

        checkCurrentConfigState();
    }

    private void automaticUncheckAngle() {
        for (CheckBox checkBox : checkBoxAngleList) {
            checkBox.setChecked(false);
        }
    }

    private void automaticUncheckCarte() {
        for (CheckBox checkBox : checkBoxCarteList) {
            checkBox.setChecked(false);
        }
    }

    private void onCheckboxClicked(View view) {
        boolean isItChecked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.checkBox5:
                if (isItChecked) {
                    automaticUncheckAngle();
                    checkAngle5.setChecked(true);
                    Config.DEFAULT_RESEARCH_ANGLE = 5.;
                }
                break;
            case R.id.checkBox8:
                if (isItChecked) {
                    automaticUncheckAngle();
                    checkAngle8.setChecked(true);
                    Config.DEFAULT_RESEARCH_ANGLE = 8.;
                }
                break;
            case R.id.checkBox12:
                if (isItChecked) {
                    automaticUncheckAngle();
                    checkAngle12.setChecked(true);
                    Config.DEFAULT_RESEARCH_ANGLE = 12.;
                }
                break;
            case R.id.checkBox15:
                if (isItChecked) {
                    automaticUncheckAngle();
                    checkAngle15.setChecked(true);
                    Config.DEFAULT_RESEARCH_ANGLE = 15.;
                }
                break;
            //Monuments
            case R.id.checkCarte1:
                if (isItChecked) {
                    automaticUncheckCarte();
                    checkCarte1.setChecked(true);
                    Config.CURRENT_MAP_ID = CSVEnum.MONUMENTS_MONDIAUX;
                }
                break;
            //Amiens
            case R.id.checkCarte2:
                if (isItChecked) {
                    automaticUncheckCarte();
                    checkCarte2.setChecked(true);
                    Config.CURRENT_MAP_ID = CSVEnum.AMIENS_GARE;
                }
                break;
            //Couloisy
            case R.id.checkCarte3:
                if (isItChecked) {
                    automaticUncheckCarte();
                    checkCarte3.setChecked(true);
                    Config.CURRENT_MAP_ID = CSVEnum.COULOISY;
                }
                break;
            case R.id.checkCarte4:
                if (isItChecked) {
                    automaticUncheckCarte();
                    checkCarte4.setChecked(true);
                    Config.CURRENT_MAP_ID = CSVEnum.MONTAGNES_PARCS_FRANCE;
                }
        }
    }

    private void checkCurrentConfigState() {
        if (Config.isSyntheseActivated) {
            switchSyntheseVocale.setChecked(true);
        }
        if (Config.isIsDarkTheme) {
            switchDarkTheme.setChecked(true);
        }
    }

    private void changeTheme() {
        int i = 0;


        if (Config.isIsDarkTheme) {
            int color = getResources().getColor(R.color.white);
            //Dark Theme
            while (i < checkBoxAngleList.size()) {

                checkBoxAngleList.get(i).setTextColor(color);
                i++;
            }
            i = 0;
            while (i < listNomCarte.size()) {
                listNomCarte.get(i).setTextColor(color);
                i++;
            }
            background.setImageResource(R.drawable.fondnoir);
            switchDarkTheme.setTextColor(color);
            switchSyntheseVocale.setTextColor(color);
            logo.setImageResource(R.drawable.logo_application_toon_darktheme);
        } else {
            int color = getResources().getColor(R.color.lightGray);
            //White Theme
            while (i < checkBoxAngleList.size()) {

                checkBoxAngleList.get(i).setTextColor(color);
                i++;
            }
            i = 0;
            while (i < listNomCarte.size()) {
                listNomCarte.get(i).setTextColor(color);
                i++;
            }
            background.setImageResource(R.drawable.fondblanc);
            switchDarkTheme.setTextColor(color);
            switchSyntheseVocale.setTextColor(color);
            logo.setImageResource(R.drawable.logo_application_toon);
        }

    }
}