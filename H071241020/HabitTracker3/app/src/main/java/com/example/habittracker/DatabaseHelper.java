package com.example.habittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.habittracker.Habit;
import com.example.habittracker.SocialPost;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habit_tracker_pro.db";
    private static final int DATABASE_VERSION = 2;

    // Table and Columns
    public static final String TABLE_HABITS = "habits";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DAYS_OF_WEEK = "days_of_week";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_LAST_CHECK_IN = "last_check_in_date";
    public static final String COLUMN_STREAK = "streak_count";

    // User Community Posts Table
    public static final String TABLE_USER_POSTS = "user_posts";
    public static final String COLUMN_POST_USERNAME = "username";
    public static final String COLUMN_POST_TIME = "time";
    public static final String COLUMN_POST_TITLE = "title";
    public static final String COLUMN_POST_BODY = "body";
    public static final String COLUMN_POST_PHOTO = "photo_path";
    public static final String COLUMN_POST_TAG = "tag";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_DAYS_OF_WEEK + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_PRIORITY + " TEXT, " +
                COLUMN_DIFFICULTY + " TEXT, " +
                COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " +
                COLUMN_LAST_CHECK_IN + " TEXT, " +
                COLUMN_STREAK + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);

        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_USER_POSTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_POST_USERNAME + " TEXT, " +
                COLUMN_POST_TIME + " TEXT, " +
                COLUMN_POST_TITLE + " TEXT, " +
                COLUMN_POST_BODY + " TEXT, " +
                COLUMN_POST_PHOTO + " TEXT, " +
                COLUMN_POST_TAG + " TEXT)";
        db.execSQL(CREATE_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String CREATE_POSTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER_POSTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_POST_USERNAME + " TEXT, " +
                    COLUMN_POST_TIME + " TEXT, " +
                    COLUMN_POST_TITLE + " TEXT, " +
                    COLUMN_POST_BODY + " TEXT, " +
                    COLUMN_POST_PHOTO + " TEXT, " +
                    COLUMN_POST_TAG + " TEXT)";
            db.execSQL(CREATE_POSTS_TABLE);
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_POSTS);
            onCreate(db);
        }
    }

    // CREATE
    public long insertHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, habit.getTitle());
        values.put(COLUMN_DAYS_OF_WEEK, habit.getDaysOfWeek());
        values.put(COLUMN_TIME, habit.getTime());
        values.put(COLUMN_PRIORITY, habit.getPriority());
        values.put(COLUMN_DIFFICULTY, habit.getDifficulty());
        values.put(COLUMN_IS_ACTIVE, 1); // always active upon insertion
        values.put(COLUMN_LAST_CHECK_IN, "");
        values.put(COLUMN_STREAK, 0);
        long id = db.insert(TABLE_HABITS, null, values);
        db.close();
        return id;
    }

    // DUPLICATE CHECK
    public boolean isHabitTitleExists(String title, int excludeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_TITLE + " = ? AND " + COLUMN_IS_ACTIVE + " = 1";
        String[] selectionArgs;
        if (excludeId != -1) {
            selection += " AND " + COLUMN_ID + " != ?";
            selectionArgs = new String[]{title, String.valueOf(excludeId)};
        } else {
            selectionArgs = new String[]{title};
        }
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_ID}, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    // UPDATE
    public int updateHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, habit.getTitle());
        values.put(COLUMN_DAYS_OF_WEEK, habit.getDaysOfWeek());
        values.put(COLUMN_TIME, habit.getTime());
        values.put(COLUMN_PRIORITY, habit.getPriority());
        values.put(COLUMN_DIFFICULTY, habit.getDifficulty());
        // keep other fields intact during basic edit
        int rows = db.update(TABLE_HABITS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(habit.getId())});
        db.close();
        return rows;
    }

    // SOFT DELETE: Never DELETE FROM. Always sets is_active = 0.
    public int softDeleteHabit(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_ACTIVE, 0); // Mark inactive
        int rows = db.update(TABLE_HABITS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // CHECK-IN / COMPLETE HABIT TODAY (Anti-Spam protection)
    public boolean checkInHabit(int id, String todayDateString) {
        if (isHabitCompletedToday(id, todayDateString)) {
            return false; // already checked-in today, prevent double entries!
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        // Fetch current streak first
        int currentStreak = 0;
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_STREAK}, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                currentStreak = cursor.getInt(0);
            }
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_CHECK_IN, todayDateString);
        values.put(COLUMN_STREAK, currentStreak + 1); // increment streak
        db.update(TABLE_HABITS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return true;
    }

    // Anti-Spam Check-In helper
    public boolean isHabitCompletedToday(int id, String todayDateString) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_LAST_CHECK_IN},
                COLUMN_ID + " = ? AND " + COLUMN_LAST_CHECK_IN + " = ?",
                new String[]{String.valueOf(id), todayDateString}, null, null, null);
        boolean completed = false;
        if (cursor != null) {
            completed = cursor.getCount() > 0;
            cursor.close();
        }
        return completed;
    }

    // RETRIEVE WITH SMART SORTING
    // - Tugas yang is_active = 1
    // - Filter berdasarkan hari pelakaanaan (days_of_week LIKE '%Senin%')
    // - ORDER BY completion status (Belum selesai ke atas, selesai ke bawah)
    // - ORDER BY priority (Urgent -> Tinggi -> Sedang -> Rendah)
    // - ORDER BY difficulty (Difficult -> Sedang -> Mudah)
    public List<Habit> getHabitsForDay(String dayOfWeek, String todayDateString) {
        List<Habit> habitsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_HABITS + " WHERE " +
                COLUMN_IS_ACTIVE + " = 1 AND " +
                COLUMN_DAYS_OF_WEEK + " LIKE ? ORDER BY " +
                "(CASE WHEN " + COLUMN_LAST_CHECK_IN + " = ? THEN 1 ELSE 0 END) ASC, " +
                "(CASE WHEN " + COLUMN_PRIORITY + " = 'Urgent' THEN 1 " +
                "      WHEN " + COLUMN_PRIORITY + " = 'Tinggi' THEN 2 " +
                "      WHEN " + COLUMN_PRIORITY + " = 'Sedang' THEN 3 " +
                "      ELSE 4 END) ASC, " +
                "(CASE WHEN " + COLUMN_DIFFICULTY + " = 'Sulit' THEN 1 " +
                "      WHEN " + COLUMN_DIFFICULTY + " = 'Sedang' THEN 2 " +
                "      ELSE 3 END) ASC";

        Cursor cursor = db.rawQuery(query, new String[]{"%" + dayOfWeek + "%", todayDateString});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Habit h = new Habit();
                    h.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    h.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    h.setDaysOfWeek(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAYS_OF_WEEK)));
                    h.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)));
                    h.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));
                    h.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)));
                    h.setIsActive(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ACTIVE)));
                    h.setLastCheckInDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_CHECK_IN)));
                    h.setStreakCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK)));
                    habitsList.add(h);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return habitsList;
    }

    // STATS QUERY: Get active habits count
    public int getActiveHabitsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HABITS, new String[]{COLUMN_ID}, COLUMN_IS_ACTIVE + " = 1", null, null, null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        db.close();
        return count;
    }

    // STATS QUERY: Get total streaks (including deleted/inactive habits so streaks are never lost)
    public int getMaxStreak() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COLUMN_STREAK + ") FROM " + TABLE_HABITS, null);
        int max = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                max = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return max;
    }

    // STATS QUERY: Today's completing ratio
    public double getCompletionRate(String dayOfWeek, String todayDateString) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor totalToday = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_HABITS + " WHERE " + COLUMN_IS_ACTIVE + " = 1 AND " + COLUMN_DAYS_OF_WEEK + " LIKE ?", new String[]{"%" + dayOfWeek + "%"});
        int total = 0;
        if (totalToday != null) {
            total = totalToday.getCount();
            totalToday.close();
        }
        
        if (total == 0) return 0.0;

        Cursor completedToday = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + TABLE_HABITS + " WHERE " + COLUMN_IS_ACTIVE + " = 1 AND " + COLUMN_DAYS_OF_WEEK + " LIKE ? AND " + COLUMN_LAST_CHECK_IN + " = ?", new String[]{"%" + dayOfWeek + "%", todayDateString});
        int completed = 0;
        if (completedToday != null) {
            completed = completedToday.getCount();
            completedToday.close();
        }
        db.close();
        return ((double) completed / total) * 100.0;
    }

    // STATS QUERY: Get total sessions logged across all active or deleted habits (cumulative completions)
    public int getTotalSessions() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_STREAK + ") FROM " + TABLE_HABITS, null);
        int total = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return total;
    }

    // STATS QUERY: Get total habit tasks ever created (active + deleted)
    public int getTotalHabitsEverCreated() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HABITS, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        db.close();
        return count;
    }

    // STATS QUERY: Get top habits with highest check-in counts (active + deleted)
    public List<Habit> getTopHabits(int limit) {
        List<Habit> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TITLE + ", " + COLUMN_STREAK + " FROM " + TABLE_HABITS + 
                " WHERE " + COLUMN_STREAK + " > 0 ORDER BY " + COLUMN_STREAK + " DESC LIMIT " + limit, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Habit h = new Habit();
                    h.setTitle(cursor.getString(0));
                    h.setStreakCount(cursor.getInt(1));
                    list.add(h);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    // USER COMMUNITY POSTS INSERT & RETRIEVE
    public long insertUserPost(SocialPost post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_USERNAME, post.getUsername());
        values.put(COLUMN_POST_TIME, post.getTime());
        values.put(COLUMN_POST_TITLE, post.getTitle());
        values.put(COLUMN_POST_BODY, post.getBody());
        values.put(COLUMN_POST_PHOTO, post.getPhotoPath());
        values.put(COLUMN_POST_TAG, post.getTag());
        long id = db.insert(TABLE_USER_POSTS, null, values);
        db.close();
        return id;
    }

    public List<SocialPost> getUserPosts() {
        List<SocialPost> lists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_POSTS, null, null, null, null, null, COLUMN_ID + " DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    SocialPost post = new SocialPost();
                    post.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    post.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_USERNAME)));
                    post.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TIME)));
                    post.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TITLE)));
                    post.setBody(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_BODY)));
                    post.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_PHOTO)));
                    post.setTag(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TAG)));
                    lists.add(post);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();
        return lists;
    }

    public SocialPost getUserPostById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        SocialPost post = null;
        Cursor cursor = db.query(TABLE_USER_POSTS, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                post = new SocialPost();
                post.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                post.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_USERNAME)));
                post.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TIME)));
                post.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TITLE)));
                post.setBody(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_BODY)));
                post.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_PHOTO)));
                post.setTag(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TAG)));
            }
            cursor.close();
        }
        db.close();
        return post;
    }

    public boolean updateUserPost(SocialPost post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_TITLE, post.getTitle());
        values.put(COLUMN_POST_BODY, post.getBody());
        values.put(COLUMN_POST_PHOTO, post.getPhotoPath());
        values.put(COLUMN_POST_TAG, post.getTag());
        
        int rows = db.update(TABLE_USER_POSTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(post.getId())});
        db.close();
        return rows > 0;
    }

    public boolean deleteUserPost(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USER_POSTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }
}
