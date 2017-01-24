package com.kimmy.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;
import android.database.Cursor;
import android.util.Log;

import com.kimmy.todolist.data.Constants;
import com.kimmy.todolist.data.ToDoAdapter;
import com.kimmy.todolist.data.TodoDbHelper;
import com.kimmy.todolist.data.TodoRecord;
import com.kimmy.todolist.gui.TodoListActivity;
import com.kimmy.todolist.rest.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by KimmyYang on 2017/1/16.
 */
public class TODOList {
    private Activity mActivity = null;
    private ToDoAdapter mAdapter = null;
    private RestClient mRestClient = null;
    private TodoDbHelper mDBHelper = null;
    private ArrayList<TodoRecord> mDbList = new ArrayList<TodoRecord>();
    private ArrayList<TodoRecord> mServerList = new ArrayList<TodoRecord>();
    private HandlerThread mThread = null;
    private Handler mHandler = null;
    private Handler mClient = null;
    private boolean isDbUpdateNeedSync = false;
    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case Constants.EVENT_INIT_LIST:
                    syncList();
                    updateAdapter();
                    mClient.sendEmptyMessage(TodoListActivity.EVENT_DRAW_LIST_VIEW);
                    break;
                case Constants.EVENT_REFRESH_LIST:
                    syncList();
                    updateAdapter();
                    break;
                case Constants.EVENT_ADD_LIST:
                    addListImpl(((TodoRecord) msg.obj));
                    break;
            }
            return false;
        }
    };

    public TODOList( Activity activity, Handler client){
        mActivity = activity;
        mClient = client;
        mAdapter = new ToDoAdapter(activity);
        //DB
        mDBHelper = TodoDbHelper.getInstance();
        //Log.d(Constants.TAG, "TODOList: activity.getApplicationContext().getDatabasePath()=" + activity.getApplicationContext().getDatabasePath(TodoDbHelper.DATABASE_NAME));
        mDBHelper.createDB(activity.getApplicationContext().getDatabasePath(TodoDbHelper.DATABASE_NAME).getPath());

        mDBHelper.createTable(TodoDbHelper.TODO_LIST_TABLE);
        //JSON
        mRestClient = new RestClient(Constants.URL);
        mRestClient.addHeader("content-type", "application/json");
        //init thread
        mThread = new HandlerThread("TODOListThread");
        mThread.start();
        mHandler = new Handler(mThread.getLooper(),mHandlerCallback);
        //init
        mHandler.sendEmptyMessage(Constants.EVENT_INIT_LIST);
    }

    public void addList(TodoRecord record){
        Message msg = mHandler.obtainMessage();
        msg.obj = (Object)record;
        msg.what = Constants.EVENT_ADD_LIST;
        mHandler.sendMessage(msg);
    }

    private void addListImpl(TodoRecord record){
        addListToDB(record);
        updateAdapter();
        addListToServer(record);
    }

    private void addListToServer(TodoRecord record){
        int result = mRestClient.executePost(transferToJsonData(record));
        if(!mRestClient.isSuccess(Constants.COMMAND_POST,result)){
            //send respCode to client
            isDbUpdateNeedSync = true;
        }else {
            //add first, the UI will sync quickly
            mServerList.add(record);
        }
    }

    private void addListToDB(TodoRecord record){
        ContentValues content = new ContentValues();
        content.put(Constants.ID,record.id);
        content.put(Constants.DATE_TIME,record.datetime);
        content.put(Constants.TASK, record.task);
        content.put(Constants.ISFINISH, record.isFinish);
        mDBHelper.insert(TodoDbHelper.TODO_LIST_TABLE, content);
        mDbList.add(record);
    }

    public void refreshList(){
        mHandler.sendEmptyMessage(Constants.EVENT_REFRESH_LIST);
    }

    private boolean syncList(){
        /*
        Get local DB first.
        In case of server is not connected, UI can show DB data
         */
        getListFromDB();//get and reset list
        if(!getListFromServer()){//get and reset list
            isDbUpdateNeedSync = true;
            return false;//connect server fail, no need to sync from DB
        }
        boolean isMatch = false;
        log("syncList: mServerList.size() = " + mServerList.size() + ", mRecordList.size() = " + mDbList.size());
        log("syncList: isDbUpdateNeedSync = "+isDbUpdateNeedSync);
        if(isDbUpdateNeedSync){
            isDbUpdateNeedSync = false;
            for(TodoRecord db_record: mDbList){
                int id = db_record.id;
                for(TodoRecord server_record: mServerList){
                    if(id == server_record.id){
                        isMatch = true;
                        break;
                    }
                }
                if(!isMatch){
                    log("syncList: not match id = " + id);
                    //mServerList.add(db_record);
                    addListToServer(db_record);
                }
                isMatch = false;
            }

        }else{
            for(TodoRecord server_record: mServerList){
                int id = server_record.id;
                for(TodoRecord db_record: mDbList){
                    if(id == db_record.id){
                        isMatch = true;
                        break;
                    }
                }
                if(!isMatch){
                    addListToDB(server_record);
                }
                isMatch = false;
            }
        }
        return true;
    }

    public boolean getListFromServer(){
        mServerList.clear();
        int result = mRestClient.executeGet(mServerList);
        log("getListFromServer: mServerList.size() = "+mServerList.size());
        if(!mRestClient.isSuccess(Constants.COMMAND_GET,result)){
            //send respCode to client
            return false;
        }
        return true;
    }

    public void getListFromDB(){
        mDbList.clear();
        TodoRecord record;
        Cursor cursor = mDBHelper.getAllData(TodoDbHelper.TODO_LIST_TABLE);
        if(cursor.moveToFirst()) {
            do {
                record = new TodoRecord(cursor.getInt(cursor.getColumnIndexOrThrow(Constants.ID)));
                record.datetime = cursor.getString(cursor.getColumnIndexOrThrow(Constants.DATE_TIME));
                record.task = cursor.getString(cursor.getColumnIndexOrThrow(Constants.TASK));
                record.isFinish = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.ISFINISH)) == 1;
                mDbList.add(record);
            } while (cursor.moveToNext());
        }
        Log.d(Constants.TAG, "getListFromDB: mRecordListSize = " + mDbList.size());
    }

    public void updateAdapter(){
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String,String> map;
        for(TodoRecord record: mDbList){
            map = new HashMap<String,String>();
            map.put(Constants.DATE_TIME,record.datetime);
            map.put(Constants.TASK,record.task);
            map.put(Constants.ISFINISH,Boolean.toString(record.isFinish));
            list.add(map);
        }
        mAdapter.updateList(list);
        mClient.sendEmptyMessage(TodoListActivity.EVENT_NOTIFY_DATA_CHANGE);
    }

    public ToDoAdapter getAdapter(){
        return mAdapter;
    }

    public int getListID(){
        if(mServerList.size() > mDbList.size()){
            return mServerList.size()+1;
        }else{
            return mDbList.size()+1;
        }
    }

    private JSONObject transferToJsonData(TodoRecord record)  {
        JSONObject data  = new JSONObject();
        try {
            data.put(Constants.ID,record.id);
            data.put(Constants.DATE_TIME, record.datetime);
            data.put(Constants.TASK,record.task);
            data.put(Constants.ISFINISH,record.isFinish);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data;
    }

    private JSONArray transferToJsonArray(ArrayList<TodoRecord> list)  {
        try{
           return new JSONArray(list);
        }catch (Exception ex){
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        return new JSONArray();
    }

    public void log(String text){
        Log.d(Constants.TAG, text);
    }
}
