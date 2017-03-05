package smarteq.weatheralert.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SD on 25.02.2017
 * :)
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "wet.db";//not key sensitive
    public static final String TABLE_NAME = "wet";
    public static final String COL1 = "NAME";
    public static final String COL2 = "SAAT";
    public static final String COL3 = "DK";
    public static final String COL4 = "FLAG";//kaldırılcak

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (NAME TEXT,SAAT TEXT,DK TEXT,FLAG TEXT)");//NAME TEXT PRIMARY KEY
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name, String saat, String dk, String ilk) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL1, name);
        cv.put(COL2, saat);
        cv.put(COL3, dk);
        cv.put(COL4, ilk);
        long result = db.insert(TABLE_NAME, null, cv);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public boolean updateData(String name, String saat, String dk, String ilk) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL1, name);
        cv.put(COL2, saat);
        cv.put(COL3, dk);
        cv.put(COL4, ilk);
        if (db.update(TABLE_NAME, cv, "NAME == ?", new String[]{name}) != 0)
            return true;
        else
            return false;
    }

    public Integer deleteData(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "NAME = ?", new String[]{name});
    }
}
