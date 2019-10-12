package it.astaffolani.musicalgps.utils;

import android.content.Context;
import android.widget.Toast;

public class Alert {

    public static void info(Context context, String text) {
        int duration = Toast.LENGTH_SHORT;
        Alert.info(context, text, duration);
    }

    public static void info(Context context, String text, int duration) {
        CharSequence textChar = text;
        Toast.makeText(context, textChar, duration).show();
    }
}
