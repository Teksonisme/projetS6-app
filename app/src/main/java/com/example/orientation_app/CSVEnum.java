package com.example.orientation_app;

public enum CSVEnum {
    COULOISY, AMIENS_GARE, MONUMENTS_MONDIAUX, MONTAGNES_PARCS_FRANCE;


    public static int findIdOfCSV(CSVEnum csvFileName) {
        switch (csvFileName) {
            case COULOISY:
                return R.raw.couloisy_test;
            case AMIENS_GARE:
                return R.raw.amiens_gare;
            case MONUMENTS_MONDIAUX:
                return R.raw.monuments_mondiaux;
            case MONTAGNES_PARCS_FRANCE:
                return R.raw.montagnes_et_parcs_naturels_de_france;
            default:
                return -1;
        }
    }

    public static CSVEnum findCSVofId(int id){
        switch (id) {
            case R.raw.couloisy_test:
                return COULOISY;
            case R.raw.amiens_gare:
                return AMIENS_GARE;
            case R.raw.monuments_mondiaux:
                return MONUMENTS_MONDIAUX;
            case R.raw.montagnes_et_parcs_naturels_de_france:
                return MONTAGNES_PARCS_FRANCE;
            default:
                return Config.CURRENT_MAP_ID;
        }
    }
}
