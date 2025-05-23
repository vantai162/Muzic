package com.example.muzic.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsSharedPrefManager {
    SharedPreferences sharedPreferences;
    public SettingsSharedPrefManager(Context context){
        sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public void setDownloadOverCellular(boolean value){
        sharedPreferences.edit().putBoolean("download_over_cellular", value).apply();
    }
    public boolean getDownloadOverCellular(){
        return sharedPreferences.getBoolean("download_over_cellular", true);
    }

    public void setHighQualityTrack(boolean value){
        sharedPreferences.edit().putBoolean("high_quality_track", value).apply();
    }
    public boolean getHighQualityTrack(){
        return sharedPreferences.getBoolean("high_quality_track", true);
    }

    public void setStoreInCache(boolean value){
        sharedPreferences.edit().putBoolean("store_in_cache", value).apply();
    }
    public boolean getStoreInCache(){
        return sharedPreferences.getBoolean("store_in_cache", true);
    }

    public void setBlurPlayerBackground(boolean value){
        sharedPreferences.edit().putBoolean("blur_player_background", value).apply();
    }
    public boolean getBlurPlayerBackground(){
        return sharedPreferences.getBoolean("blur_player_background", true);
    }

    public void setExplicit(boolean value){
        sharedPreferences.edit().putBoolean("explicit", value).apply();
    }
    public boolean getExplicit() {
        return sharedPreferences.getBoolean("explicit", true);
    }

    public void setDarkMode(String value) {
        sharedPreferences.edit().putString("dark_mode", value).apply();
    }
    public String getDarkMode(){
        return sharedPreferences.getString("dark_mode", "default");
    }

    public void setTheme(String theme){
        sharedPreferences.edit().putString("theme", theme).apply();
    }
    public String getTheme() {
        return sharedPreferences.getString("theme", "original");
    }
}
