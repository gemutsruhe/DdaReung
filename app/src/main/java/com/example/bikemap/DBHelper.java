package com.example.bikemap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "CrossRoadCoord.db";
    static String DB_PATH;
    private SQLiteDatabase mDataBase;
    private Context mContext;
    // DBHelper 생성자
    public DBHelper(Context context, int version) {
        super(context, DB_NAME, null, version);

        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;
        databaseCheck();
    }
    // Person Table 생성
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        //Toast.makeText(mContext,"onOpen()",Toast.LENGTH_SHORT).show();
    }
    // Person Table Upgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void databaseCheck() {
        File dbFile = new File(DB_PATH + DB_NAME);
        if (!dbFile.exists()) {
            dbCopy();
        }
    }

    private void dbCopy() {
        try {
            File folder = new File(DB_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            InputStream inputStream = mContext.getAssets().open(DB_NAME);
            String out_filename = DB_PATH + DB_NAME;
            OutputStream outputStream = new FileOutputStream(out_filename);
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = inputStream.read(mBuffer)) > 0) {
                outputStream.write(mBuffer, 0, mLength);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
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