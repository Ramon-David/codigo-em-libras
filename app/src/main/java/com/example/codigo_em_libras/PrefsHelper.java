package com.example.codigo_em_libras;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class PrefsHelper {

    private static final String PREF_NAME = "AppPrefs";
    private SharedPreferences prefs;




    public PrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public String getString(String key) {
        return prefs.getString(key, "");
    }

    public boolean getBoolean(String key, boolean def) {
        return prefs.getBoolean(key, def);
    }

    public int getInt(String key, int def) {
        return prefs.getInt(key, def);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }


}
