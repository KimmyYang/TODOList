package com.kimmy.todolist.data;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by KimmyYang on 2017/1/17.
 */
public class TodoDbHelper {
    private static TodoDbHelper mInstance = null;
    public static final String DATABASE_NAME = "TODOListDB.db";
    public static final String TODO_LIST_TABLE= "TODOListTable";
    private SQLiteDatabase mDB = null;
    private static final String CREATE_TODO_LIST_TABLE = "CREATE TABLE IF NOT EXISTS "+TODO_LIST_TABLE + " ("
            + Constants.ID+" integer primary key autoincrement,"
            + Constants.DATE_TIME+" TEXT NOT NULL, "
            + Constants.TASK+" TEXT NOT NULL,"
            + Constants.ISFINISH+" BOOLEAN NOT NULL"
            +");";

    private TodoDbHelper(){
    }

    public static TodoDbHelper getInstance(){
        if(mInstance == null){
            mInstance = new TodoDbHelper();
        }
        return mInstance;
    }

    public void createDB(String db){
        try {
            Log.d(Constants.TAG,"TodoDbHelper: db = "+db);
            mDB = SQLiteDatabase.openOrCreateDatabase(db, null);
        }catch (SQLiteCantOpenDatabaseException ex){
            //String databasePath =  getFilesDir().getPath() +  "/" + dbFilename;
            Log.e(Constants.TAG,"TodoDbHelper: open DB failed. ex = "+ex.toString());
        }
    }

    public void createTable(String table){
        String script = null;
       if(table.equals(TODO_LIST_TABLE)) {
           script = CREATE_TODO_LIST_TABLE;
       }
        if(script!=null){
            mDB.execSQL(script);
        }
    }

    public void insert(String table, ContentValues values){
        mDB.insert(table, null, values);
    }

    public void delete(String table, int id){
        mDB.delete(table, Constants.ID + "=" + id, null);
    }

    public void update(String table, int id, ContentValues values){
        mDB.update(table, values, Constants.ID + "=" + id, null);
    }

    public Cursor getAllData(String table){
        try{
            return mDB.query(table, null, null, null, null,null,Constants.ID);//order by ID
        }catch (SQLException ex){
            Log.e(Constants.TAG,"ex = "+ex.toString());
            return null;
        }
    }
}
