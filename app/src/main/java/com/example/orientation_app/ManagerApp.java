package com.example.orientation_app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ManagerApp {

    private TextView latitudeHereText, longitudeHereText, distanceFromHereText, nomInterestText;

    public TextToSpeech tts;
    private final Context context;
    private final Boussole boussole;
    private final Tracker tracker;
    private final Handler handlerOfToasts;

    private String lastTtsSaid = "";
    public List<Interest> listOfInterests;
    private String[][] mediumBearingInterests;
    private int arrayInfSize = 0;

    private CSVEnum lastMapUsed = Config.CURRENT_MAP_ID;
    private double lastAngleUsed = Config.DEFAULT_RESEARCH_ANGLE;

    private boolean updateInProgress = false, isCheckThreadEnRoute = false, isTtsMuted = true;

    private Thread resetLocation, checkInterestFront;

    private final NumberFormat latiLongFormat = new DecimalFormat("#0.00000");

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ManagerApp(Context context, Boussole boussole, Tracker tracker, List<TextView> textViews) {
        this.context = context;
        this.boussole = boussole;
        this.tracker = tracker;
        this.listOfInterests = new ArrayList<>();
        this.latitudeHereText = textViews.get(0);
        this.longitudeHereText = textViews.get(1);
        this.distanceFromHereText = textViews.get(2);
        this.nomInterestText = textViews.get(3);

        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.FRANCE);
            }
        });

        handlerOfToasts = new Handler(Looper.getMainLooper());

    }

    // Code from San Askaruly : https://stackoverflow.com/questions/43055661/reading-csv-file-in-android-app/50443558
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void readInterestFile(CSVEnum area) {

        int value;
        listOfInterests = new ArrayList<>();
        lastMapUsed = area;

        if ((value = CSVEnum.findIdOfCSV(area)) != -1) {
            InputStream is = context.getResources().openRawResource(value);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );
            String line = "";
            try {
                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    Log.d("MyActivity", "Line: " + line);

                    String[] tokens = line.split(",");

                    // Read the data 0 : name, 1 : latitude, 2 : longitude, 3 : type
                    Interest interest = new Interest(tokens[0], Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), tokens[3]);
                    listOfInterests.add(interest);
                }

                reader.close();
                is.close();

            } catch (IOException e) {
                // Logs error with priority level
                Log.wtf("MyActivity", "Error reading data file on line" + line, e);
                e.printStackTrace();
            }
        }
    }

    // Retourne un tab de String en deux dimensions avec la valeur inférieur
    // étant dans la 3ème colonne ( 0 : name, 1 : distance, 2 : Moyenne du bearing d'avant et présent )
    private String[][] createOrientationTable(List<Interest> listOfInterests) {

        if (listOfInterests != null || !listOfInterests.isEmpty()) {

            // Range la liste des intérêts en fonction de leurs bearings
            Collections.sort(listOfInterests, new InterestBearingComparator());

            for (Interest interest : listOfInterests) {
                Log.d("Bearing of", "" + interest.getName() + " bearing : " + interest.getBearingFromUser());
            }
            arrangeListOfInterestByAngle(listOfInterests);
            String[][] arrayInfBearings = new String[listOfInterests.size()][3];
            int i = 0;
            double medium;
            while (i < listOfInterests.size()) {
                if (i == 0)
                    medium = (listOfInterests.get(listOfInterests.size() - 1).getBearingFromUser()
                            + listOfInterests.get(i).getBearingFromUser());
                else medium = (listOfInterests.get(i - 1).getBearingFromUser()
                        + listOfInterests.get(i).getBearingFromUser());

                if (i == 0 && medium > 360) {
                    medium = medium / 2 - 180;
                    arrayInfBearings[i][0] = listOfInterests.get(i).getName();
                    arrayInfBearings[i][1] = "" + listOfInterests.get(i).getDistanceFromUser();
                    arrayInfBearings[i][2] = "" + medium;
                    Log.d("Medium[" + i + "] " + arrayInfBearings[i][0], "" + arrayInfBearings[i][2]);
                } else if (i == 0) {
                    medium = 1;
                    arrayInfBearings[i][0] = listOfInterests.get(i).getName();
                    arrayInfBearings[i][1] = "" + listOfInterests.get(i).getDistanceFromUser();
                    arrayInfBearings[i][2] = "" + medium;
                    Log.d("Medium[" + i + "] " + arrayInfBearings[i][0], "" + arrayInfBearings[i][2]);

                } else {
                    medium /= 2;
                    arrayInfBearings[i][0] = listOfInterests.get(i).getName();
                    arrayInfBearings[i][1] = "" + listOfInterests.get(i).getDistanceFromUser();
                    arrayInfBearings[i][2] = "" + medium;
                    Log.d("Medium[" + i + "] " + arrayInfBearings[i][0], "" + arrayInfBearings[i][2]);
                }
                i++;
            }
            this.arrayInfSize = listOfInterests.size();
            return arrayInfBearings;
        }
        this.arrayInfSize = 0;
        return null;
    }

    // Supprime les intérêts compris dans un même angle de recherche
    private void arrangeListOfInterestByAngle(List<Interest> listOfInterests) {
        double angleOfResearch = Config.DEFAULT_RESEARCH_ANGLE;
        for (int i = 0; i < listOfInterests.size(); i++) {
            // Check First/Last and First/Second
            if (i == 0) {
                if (((360 - listOfInterests.get(listOfInterests.size() - 1).getBearingFromUser() + listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch) &&
                        ((listOfInterests.get(i + 1).getBearingFromUser() - listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch)) {
                    Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                    listOfInterests.remove(i);
                }
            } else {
                // Check Last/Before Last and Last/First
                if (i == listOfInterests.size() - 1) {
                    if (((listOfInterests.get(i).getBearingFromUser() - listOfInterests.get(i - 1).getBearingFromUser()) <= angleOfResearch) &&
                            ((360 - listOfInterests.get(i).getBearingFromUser() + listOfInterests.get(0).getBearingFromUser()) <= angleOfResearch)) {

                        Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                        listOfInterests.remove(i);
                    }
                } else {
                    // All the other cases
                    if (((listOfInterests.get(i).getBearingFromUser() - listOfInterests.get(i - 1).getBearingFromUser()) <= angleOfResearch) &&
                            ((listOfInterests.get(i + 1).getBearingFromUser() - listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch)) {
                        Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                        listOfInterests.remove(i);
                    }
                }
            }
        }
    }

    private void showCurrentInterestInFront(String[][] arrayInfBearings, double angle) {
        if (arrayInfBearings != null) {
            for (int i = 0; i < arrayInfSize; i++) {
                if (i == arrayInfSize - 1) {
                    int finalI = i;
                    distanceFromHereText.post(() -> {
                        float value = (float) Math.round(Double.parseDouble(arrayInfBearings[finalI][1]) * 1000) / 1000;
                        String distance = value + " kilomètres ";
                        distanceFromHereText.setText(distance);
                    });
                    nomInterestText.post(() -> {
                        String nomInterest = arrayInfBearings[finalI][0];
                        nomInterestText.setText(nomInterest);

                        //TTS speaker
                        if (!lastTtsSaid.equals(nomInterest) && !isTtsMuted && Config.isSyntheseActivated) {
                            tts.speak(nomInterest, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        lastTtsSaid = nomInterest;
                    });
                    break;
                } else if (Double.parseDouble(arrayInfBearings[i][2]) <= angle && Double.parseDouble(arrayInfBearings[i + 1][2]) > angle) {
                    int finalI = i;
                    distanceFromHereText.post(() -> {
                        float value = (float) Math.round(Double.parseDouble(arrayInfBearings[finalI][1]) * 1000) / 1000;
                        String distance = value + " kilomètres ";
                        distanceFromHereText.setText(distance);
                    });
                    nomInterestText.post(() -> {
                        String nomInterest = arrayInfBearings[finalI][0];
                        nomInterestText.setText(nomInterest);

                        //TTS Speaker
                        if (!lastTtsSaid.equals(nomInterest) && !isTtsMuted && Config.isSyntheseActivated) {
                            tts.speak(nomInterest, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        lastTtsSaid = nomInterest;
                    });
                    break;
                }
            }
        }
    }

    private class ResetLocation implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            handlerOfToasts.post(tracker::getLocation);

            if (tracker.getLocation() != null) {

                readInterestFile(Config.CURRENT_MAP_ID);
                double CurrentLatitude = tracker.getCurrentLatitude(), CurrentLongitude = tracker.getCurrentLongitude();
                for (Interest interest : listOfInterests) {
                    interest.findBearingFromUser(CurrentLatitude, CurrentLongitude);
                    interest.findDistanceFromUser(CurrentLatitude, CurrentLongitude);
                }
                mediumBearingInterests = createOrientationTable(listOfInterests);

                // Post permet de transmettre au thread Main un bloc de code à éxécuter
                latitudeHereText.post(() -> {
                    String latitudeHere = latiLongFormat.format(CurrentLatitude);
                    latitudeHereText.setText(latitudeHere);
                });
                longitudeHereText.post(() -> {
                    String longitudeHere = latiLongFormat.format(CurrentLongitude);
                    longitudeHereText.setText(longitudeHere);
                });

                Log.d("Thread findind distance", "Thread has finished to update");
                updateInProgress = false;


            } else {
                tracker.getLocation();
                Log.d("Reset Location", "Location is null");
                handlerOfToasts.post(() -> Toast.makeText(context, "L'accès à la localisation a échouée", Toast.LENGTH_LONG));
            }
        }
    }

    private class CheckInterestInFront implements Runnable {
        @Override
        public void run() {
            while (true) {
                showCurrentInterestInFront(mediumBearingInterests, boussole.getAzimuth());
                try {
                    Thread.sleep(Config.REFRESH_MS_CHECK_THREAD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start() {
        if (!updateInProgress) {
            updateInProgress = true;
            ResetLocation resetLocationRun = new ResetLocation();
            resetLocation = new Thread(resetLocationRun);
            resetLocation.start();

            if (!isCheckThreadEnRoute) {
                isCheckThreadEnRoute = true;
                CheckInterestInFront checkInterestInFrontRun = new CheckInterestInFront();
                checkInterestFront = new Thread(checkInterestInFrontRun);
                checkInterestFront.start();
            }
        }
    }

    public void restartTTS() {

        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.FRANCE);
            }
        });
    }

    public void notifyResetLocation() {
        if (resetLocation != null) {
            start();
        }
    }

    public void changeSyntheseVocaleMode(Button button) {
        isTtsMuted = !isTtsMuted;
        if (!isTtsMuted) {
            button.setText("Désactiver synthèse vocale");
        } else {
            button.setText("Réactiver synthèse vocale");
        }
    }

    public void cleanup() {
        if (resetLocation != null) {
            resetLocation.interrupt();
            Log.d("Thread removal", "Reset Location Cleaned");
        }
        if (checkInterestFront != null) {
            checkInterestFront.interrupt();
            Log.d("Thread removal", "Check Interest In Front Cleaned");
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    public CSVEnum getLastMapUsed() {
        return lastMapUsed;
    }


}
