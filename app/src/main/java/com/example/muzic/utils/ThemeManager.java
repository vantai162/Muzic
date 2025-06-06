package com.example.muzic.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private final Context context;
    public static final int MODE_ON = 1;
    public static final int MODE_OFF = 2;
    public static final int MODE_SYSTEM = 3;

    public ThemeManager(Context context) {
        this.context = context;
    }

    public void setThemeMode(int mode) {
        SettingsSharedPrefManager settings = new SettingsSharedPrefManager(context);
        settings.setDarkMode(mode == MODE_ON ? "on" : mode == MODE_OFF ? "off" : "default");
        applyTheme();
    }

    public int getThemeMode() {
        SettingsSharedPrefManager settings = new SettingsSharedPrefManager(context);
        String mode = settings.getDarkMode();
        return mode.equals("on") ? MODE_ON : mode.equals("off") ? MODE_OFF : MODE_SYSTEM;
    }

    public void applyTheme() {
        int mode = getThemeMode();
        switch (mode) {
            case MODE_ON:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case MODE_OFF:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public int getPrimaryColor() {
        SettingsSharedPrefManager settings = new SettingsSharedPrefManager(context);
        String theme = settings.getTheme();
        
        switch (theme.toLowerCase()) {
            case "nebula":
                return Color.parseColor("#7B68EE"); // Indigo
            case "aqua":
                return Color.parseColor("#57FFFF"); // Mint green
            case "tangerine":
                return Color.parseColor("#FFA500"); // Orange
            case "crimson love":
                return Color.parseColor("#DC143C"); // Crimson
            case "blue depths":
                return Color.parseColor("#0066CC"); // Deep blue
            default:
                return Color.parseColor("#1DB954"); // Spotify green (Original)
        }
    }

    public ColorStateList getColorStateList() {
        return ColorStateList.valueOf(getPrimaryColor());
    }

    public int getSecondaryColor() {
        return adjustAlpha(getPrimaryColor(), 0.6f);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
} 