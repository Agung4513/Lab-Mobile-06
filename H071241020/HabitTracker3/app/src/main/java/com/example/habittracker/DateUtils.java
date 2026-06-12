package com.example.habittracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static String getIndonesianDayName() {
        String englishDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date());
        switch (englishDay) {
            case "Monday": return "Senin";
            case "Tuesday": return "Selasa";
            case "Wednesday": return "Rabu";
            case "Thursday": return "Kamis";
            case "Friday": return "Jumat";
            case "Saturday": return "Sabtu";
            case "Sunday": return "Minggu";
            default: return "Senin";
        }
    }

    public static String getFormattedFullDate() {
        return new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID")).format(new Date());
    }
}
