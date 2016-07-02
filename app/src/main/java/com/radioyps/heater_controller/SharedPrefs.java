package com.radioyps.heater_controller;

import android.content.Context;
        import android.content.SharedPreferences;

        import java.util.ArrayList;

public class SharedPrefs {
    private final static String RADIOYPS_PREFS = "ubimonitor_prefs";


    public ArrayList<String> getSharedPrefsStrArray(String prefName, Context ctx){
        ArrayList<String> retAL = new ArrayList<String>();
        String groupedArrayList = "";
        SharedPreferences prefs = ctx.getSharedPreferences(RADIOYPS_PREFS, Context.MODE_PRIVATE);
        groupedArrayList = prefs.getString(prefName, "");
        if(groupedArrayList.length()==0){

        } else {
            String[] splitgroupedArrayList = groupedArrayList.split(";");
            for (int i = 0; i < splitgroupedArrayList.length; i++)
                retAL.add(splitgroupedArrayList[i]) ;
        }
        return retAL;
    }

    public void setSharedPrefsStrArray(String prefName, ArrayList<String> prefValue, Context ctx){
        String groupArrayList = "";
        SharedPreferences.Editor editor = ctx.getSharedPreferences(RADIOYPS_PREFS,  Context.MODE_PRIVATE).edit();
        for(int i=0;i<prefValue.size(); i++)
            groupArrayList += prefValue.get(i)+";";
        editor.putString(prefName, groupArrayList);
        editor.commit();
    }

    public String getSharedPrefsString(String prefName, Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences(RADIOYPS_PREFS, Context.MODE_PRIVATE);
        String prefValue = prefs.getString(prefName, "0");
        return prefValue;
    }

    public void setSharedPrefsString(String prefName, String prefValue, Context ctx){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(RADIOYPS_PREFS,  Context.MODE_PRIVATE).edit();
        editor.putString(prefName, prefValue);
        editor.commit();
    }

    public int getSharedPrefsInt(String prefName , Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences(RADIOYPS_PREFS, Context.MODE_PRIVATE);
        int prefValue = prefs.getInt(prefName, 0);
        return prefValue;
    }

    public void setSharedPrefsInt(String prefName, int prefValue, Context ctx){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(RADIOYPS_PREFS,  Context.MODE_PRIVATE).edit();
        editor.putInt(prefName, prefValue);
        editor.commit();
    }
    //Haendel
    public void setSharedPrefsDouble(String prefName, double prefValue, Context ctx){
        SharedPreferences.Editor editor = ctx.getSharedPreferences(RADIOYPS_PREFS,  Context.MODE_PRIVATE).edit();
        editor.putLong(prefName, Double.doubleToRawLongBits(prefValue));
        editor.commit();
    }
    public double getSharedPrefsDouble(String prefName, Context ctx){
        SharedPreferences prefs = ctx.getSharedPreferences(RADIOYPS_PREFS, Context.MODE_PRIVATE);
        double prefValue =  Double.longBitsToDouble(prefs.getLong(prefName, 0));
        return prefValue;
    }
    public static String getRadioypsPrefs(){
        return RADIOYPS_PREFS;
    }
}