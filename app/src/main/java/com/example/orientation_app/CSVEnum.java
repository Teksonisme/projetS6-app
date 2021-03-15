package com.example.orientation_app;

public enum CSVEnum {
    COULOISY;


    public static int findIdOfCSV(CSVEnum csvFileName){
        switch(csvFileName){
            case COULOISY:
                return R.raw.couloisy_test;

            default:
                return -1;
        }
    }
}
