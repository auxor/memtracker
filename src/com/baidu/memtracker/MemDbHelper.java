
package com.baidu.memtracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MemDbHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;
    public static final String DB_NAME = "mem.db";
    public static final String TABLE_NAME = "meminfo";
    public static final String TABLE_CLOUMN_NAME = "name";
    public static final String TABLE_CLOUMN_MEM = "mem";
    public static final String TABLE_CLOUMN_TIME = "snaptime";
    public static final String[] ALL_CLOUMN = {
            TABLE_CLOUMN_NAME, TABLE_CLOUMN_MEM, TABLE_CLOUMN_TIME
    };
    public static final String TABLE_ROW_SYSTEM = "total";

    public MemDbHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MemDbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table meminfo(name varchar(100),mem integer,snaptime timestamp)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

}
