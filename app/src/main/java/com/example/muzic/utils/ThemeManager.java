package com.example.muzic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "AppSettings";
    private static final String THEME_MODE_PREF = "theme_mode_pref";
    private final SharedPreferences sharedPreferences;
    private final Context context;

    public static final int MODE_ON = 0;  // Dark mode on
    public static final int MODE_OFF = 1;  // Dark mode off
    public static final int MODE_SYSTEM = 2;  // Follow system

    public ThemeManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void applyTheme() {
        int themeMode = getThemeMode();
        applyThemeMode(themeMode);
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(THEME_MODE_PREF, MODE_SYSTEM);
    }

    public void setThemeMode(int mode) {
        if (mode < MODE_ON || mode > MODE_SYSTEM) {
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THEME_MODE_PREF, mode);
        editor.apply();
        
        applyThemeMode(mode);
    }

    private void applyThemeMode(int mode) {
        switch (mode) {
            case MODE_ON:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_OFF:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public boolean isDarkModeActive() {
        int currentMode = getThemeMode();
        if (currentMode == MODE_ON) {
            return true;
        } else if (currentMode == MODE_OFF) {
            return false;
        } else {
            // Check system dark mode setting
            int nightModeFlags = context.getResources().getConfiguration().uiMode & 
                               Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }
    }
} 