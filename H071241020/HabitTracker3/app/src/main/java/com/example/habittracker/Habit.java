package com.example.habittracker;

public class Habit {
    private int id;
    private String title;
    private String daysOfWeek;
    private String time;
    private String priority;
    private String difficulty;
    private int isActive;
    private String lastCheckInDate;
    private int streakCount;

    public Habit() {
    }

    public Habit(int id, String title, String daysOfWeek, String time, String priority, String difficulty, int isActive, String lastCheckInDate, int streakCount) {
        this.id = id;
        this.title = title;
        this.daysOfWeek = daysOfWeek;
        this.time = time;
        this.priority = priority;
        this.difficulty = difficulty;
        this.isActive = isActive;
        this.lastCheckInDate = lastCheckInDate;
        this.streakCount = streakCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getLastCheckInDate() {
        return lastCheckInDate;
    }

    public void setLastCheckInDate(String lastCheckInDate) {
        this.lastCheckInDate = lastCheckInDate;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }
}
