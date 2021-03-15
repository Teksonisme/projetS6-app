package com.example.orientation_app;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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

    private boolean updateInProgress = false, isCheckThreadEnRoute = false;
    private boolean isTtsMuted = false;
    private Thread resetLocation, checkInterestFront;

    private final NumberFormat latiLongFormat = new DecimalFormat("#0.00000");

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
        if ((value = CSVEnum.findIdOfCSV(area)) != -1) {
            InputStream is = context.getResources().openRawResource(CSVEnum.findIdOfCSV(area));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            );
            String line = "";
            try {
                reader.readLine();
                // If buffer is not empty
                while ((line = reader.readLine()) != null) {
                    Log.d("MyActivity", "Line: " + line);
                    // use comma as separator columns of CSV
                    String[] tokens = line.split(",");
                    // Read the data 0 : name, 1 : latitude, 2 : longitude, 3 : type
                    Interest interest = new Interest(tokens[0], Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), tokens[3]);
                    listOfInterests.add(interest);

                    Log.d("My Activity", "Just created: " + interest);
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

    // WIP : Retourne un tab de String en deux dimensions avec la valeur inférieur
    // étant dans la 3ème colonne ( 0 : name, 1 : distance, 2 : Moyenne du bearing d'avant et présent )
    private String[][] createOrientationTable(List<Interest> listOfInterests) {

        if (listOfInterests != null || !listOfInterests.isEmpty()) {

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
                    medium /= 2 - 180;
                    arrayInfBearings[i][0] = listOfInterests.get(i).getName();
                    arrayInfBearings[i][1] = "" + listOfInterests.get(i).getDistanceFromUser();
                    arrayInfBearings[i][2] = "" + medium;
                    Log.d("Medium[" + i + "] " + arrayInfBearings[i][0], "" + arrayInfBearings[i][2]);
                } else if (i == 0) {
                    medium = 13;
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

    private void arrangeListOfInterestByAngle(List<Interest> listOfInterests) {
        double angleOfResearch = Config.DEFAULT_RESEARCH_ANGLE;
        for (int i = 0; i < listOfInterests.size(); i++) {
            if (i == 0) {
                //Remove if there is less than the angle of Research available between the bearings of two interests encapsulating the first interest
                if (((360 - listOfInterests.get(listOfInterests.size() - 1).getBearingFromUser() + listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch) &&
                        ((listOfInterests.get(i + 1).getBearingFromUser() - listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch)) {
                    Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                    listOfInterests.remove(i);
                }
            } else {
                if (i == listOfInterests.size() - 1) {
                    if (((listOfInterests.get(i).getBearingFromUser() - listOfInterests.get(i - 1).getBearingFromUser()) <= angleOfResearch) &&
                            ((360 - listOfInterests.get(i).getBearingFromUser() + listOfInterests.get(0).getBearingFromUser()) <= angleOfResearch)) {

                        Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                        listOfInterests.remove(i);
                    }
                } else {
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
                        String distance =  value + " kilomètres ";
                        distanceFromHereText.setText(distance);
                    });
                    nomInterestText.post(() -> {
                        String nomInterest = arrayInfBearings[finalI][0];
                        nomInterestText.setText(nomInterest);
                        if(!lastTtsSaid.equals(nomInterest) && !isTtsMuted){
                            tts.speak(nomInterest,TextToSpeech.QUEUE_FLUSH,null);
                        }
                        lastTtsSaid = nomInterest;
                    });
                    break;
                } else if (Double.parseDouble(arrayInfBearings[i][2]) <= angle && Double.parseDouble(arrayInfBearings[i + 1][2]) > angle) {
                    int finalI = i;
                    distanceFromHereText.post(() -> {
                        float value = (float) Math.round(Double.parseDouble(arrayInfBearings[finalI][1]) * 1000) / 1000;
                        String distance =  value + " kilomètres ";
                        distanceFromHereText.setText(distance);
                    });
                    nomInterestText.post(() -> {
                        String nomInterest = arrayInfBearings[finalI][0];
                        nomInterestText.setText(nomInterest);

                        if(!lastTtsSaid.equals(nomInterest) && !isTtsMuted){
                            tts.speak(nomInterest,TextToSpeech.QUEUE_FLUSH,null);
                        }
                        lastTtsSaid = nomInterest;
                    });
                    break;
                }
            }
        }
    }

    private class ResetLocation implements Runnable {
        @Override
        public void run() {
            handlerOfToasts.post(() -> tracker.getLocation());

            if (tracker.location != null) {
                double CurrentLatitude = tracker.getCurrentLatitude(), CurrentLongitude = tracker.getCurrentLongitude();
                for (Interest interest : listOfInterests) {
                    interest.findBearingFromUser(CurrentLatitude, CurrentLongitude);
                    interest.findDistanceFromUser(CurrentLatitude, CurrentLongitude);
                }
                mediumBearingInterests = createOrientationTable(listOfInterests);

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
                Log.d("Reset Location", "Location is null");
                handlerOfToasts.post(() -> Toast.makeText(context, "L'accès à la localisation a écouchée", Toast.LENGTH_LONG));
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

    public void muteTTS(){
        isTtsMuted = true;
    }
    public void unmuteTTS(){
        isTtsMuted = false;
        tts = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.FRANCE);
            }
        });
    }

    public void notifyResetLocation() {
        if (resetLocation != null) {
            //resetLocation.notify();
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
        if(tts != null){
            tts.shutdown();
        }
    }

}
