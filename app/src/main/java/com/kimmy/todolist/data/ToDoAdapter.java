package com.kimmy.todolist.data;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.kimmy.todolist.R;


/**
 * Created by KimmyYang on 2017/1/17.
 */
public class ToDoAdapter extends BaseAdapter{

    public ArrayList<HashMap<String, String>> mList = null;

    private Activity mActivity = null;
    private TextView mDateTime = null;
    private TextView mTask = null;
    private TextView mIsFinish = null;

    public ToDoAdapter(Activity activity){
        mActivity = activity;
    }

    private void addTitle(){
        if(mList==null)mList = new ArrayList<HashMap<String, String>>();
        else mList.clear();
        //init title
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.DATE_TIME,Constants.DATE_TIME);
        map.put(Constants.TASK,Constants.TASK);
        map.put(Constants.ISFINISH,Constants.ISFINISH);
        addItem(map);
    }

    public void updateList(ArrayList<HashMap<String, String>> list){
        addTitle();
        for(HashMap<String, String> map: list){
            mList.add(map);
        }
        Log.d(Constants.TAG,"updateList: Adapter mListSize = "+mList.size());
    }

    public void addItem(HashMap<String, String> map){
        mList.add(map);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater=mActivity.getLayoutInflater();
        if(view == null){
            view = inflater.inflate(R.layout.todo_text, null);
            mDateTime = (TextView)view.findViewById(R.id.datetime);
            mTask = (TextView)view.findViewById(R.id.task);
            mIsFinish = (TextView)view.findViewById(R.id.isfinish);
        }
        HashMap<String, String> map=mList.get(position);
        mDateTime.setText(map.get(Constants.DATE_TIME));
        mTask.setText(map.get(Constants.TASK));
        mIsFinish.setText(map.get(Constants.ISFINISH));
        return view;
    }
}
