package com.example.orientation_app;

public enum CSVEnum {
    COULOISY;


    public int findIdOfCSV(CSVEnum csvFileName){
        switch(csvFileName){
            case COULOISY:
                return R.raw.couloisy_test;

            default:
                return 0;
        }
    }
}
