package com.example.bikemap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "CrossRoadCoord.db";

    // DBHelper 생성자
    public DBHelper(Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    // Person Table 생성
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    // Person Table Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<Position> getCrossRoadList() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Position> crossRoadList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM CrossRoadCoord", null);
        while (cursor.moveToNext()) {
            crossRoadList.add(new Position(cursor.getString(0), cursor.getString(1)));
        }

        db.close();
        return crossRoadList;
    }
}