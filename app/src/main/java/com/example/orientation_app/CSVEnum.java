package com.example.orientation_app;

public enum CSVEnum {
    COULOISY,AMIENS_GARE, MONUMENTS_MONDIAUX;


    public static int findIdOfCSV(CSVEnum csvFileName){
        switch(csvFileName){
            case COULOISY:
                return R.raw.couloisy_test;
            case AMIENS_GARE:
                return R.raw.amiens_gare;
            case MONUMENTS_MONDIAUX:
                return R.raw.monuments_mondiaux;

            default:
                return -1;
        }
    }
}
