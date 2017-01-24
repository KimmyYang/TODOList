package com.kimmy.todolist.rest;


import android.util.Log;

import com.kimmy.todolist.data.Constants;
import com.kimmy.todolist.data.TodoRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by KimmyYang on 2017/1/17.
 */
public class RestClient {

    URL mURL;
    String url;
    String headerName;
    String headerValue;

    public RestClient(String url){
        try {
            this.url = url;
            mURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void addHeader(String name, String value){
        headerName = name;
        headerValue = value;
    }

    public String executeDelete(String parameters){
        log("executeDelete");
        HttpURLConnection conn = null;
        String result ="";
        try{
            URL _url = new URL(url+parameters);
            conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            int respCode = conn.getResponseCode();
            if(respCode == 204){
                InputStream in = conn.getInputStream();
                result =  getStringFromInputStream(in);
            }else{
                log("executePost: fail "+respCode);
            }

        }catch (Exception ex){
            log("executePost: ex = "+ex.toString());
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
        return result;
    }

    public int executePost(JSONObject data){  // If you want to use post method to hit server
        log("executePost");
        HttpURLConnection conn = null;
        //String result ="";
        int respCode = -1;
        try{
            conn = (HttpURLConnection) mURL.openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStream out = conn.getOutputStream();
            out.write(data.toString().getBytes("UTF-8"));
            log("executePost: data = " + data.toString());
            out.flush();
            out.close();

            respCode = conn.getResponseCode();
            if(respCode == 200 || respCode==201){
                //InputStream in = conn.getInputStream();
                //result =  getStringFromInputStream(in);
            }else{
                log("executePost: fail "+respCode);
            }

        }catch (Exception ex){
            log("executePost: ex = " + ex.toString());
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
        return respCode;
    }

    public int executeGet(ArrayList<TodoRecord> list){ //If you want to use get method to hit server
        HttpURLConnection conn = null;
        int respCode = -1;
        try {
            conn = (HttpURLConnection) mURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);

            respCode = conn.getResponseCode();
            switch (respCode){
                case 200:
                case 201:
                    InputStream is = conn.getInputStream();
                    parserJsonToRecordList(getStringFromInputStream(is), list);
                    break;
                default:
                    log("executeGet: fail "+respCode);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            if(conn!=null){
                conn.disconnect();
            }
        }
        return respCode;
    }

    private static String getStringFromInputStream(InputStream is)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();
        os.close();
        return state;
    }

    private ArrayList<TodoRecord> parserJsonToRecordList(String data,ArrayList<TodoRecord> list) throws JSONException {
        log("parserJsonToRecordList: data = " + data);
        JSONArray array = new JSONArray(data);
        for(int i=0; i<array.length();++i){
            JSONObject obj = array.getJSONObject(i);
            TodoRecord record = new TodoRecord(obj.getInt(Constants.ID));
            record.datetime = obj.getString(Constants.DATE_TIME);
            record.task = obj.getString(Constants.TASK);
            record.isFinish = obj.getBoolean(Constants.ISFINISH);
            list.add(record);
        }
        return list;
    }

    public boolean isSuccess(int command, int code){
        switch (command){
            case Constants.COMMAND_GET:
            case Constants.COMMAND_POST:
                if(code == 200 || code == 201)return true;
                break;
            case Constants.COMMAND_DELETE:
                if(code == 204)return true;
                break;
            default:
                return false;
        }
        return false;
    }

    public void log(String text){
        Log.d(Constants.TAG, text);
    }
}
