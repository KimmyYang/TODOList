package com.kimmy.todolist.gui;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kimmy.todolist.R;
import com.kimmy.todolist.data.ToDoAdapter;


/**
 * A placeholder fragment containing a simple view.
 */
public class TodoListActivityFragment extends ListFragment {

    private ToDoAdapter mAdapter = null;
    private ListView mListView = null;

    public TodoListActivityFragment(ToDoAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo_list, container, false);
        initView(view);
        return view;
    }

    private void initView(View view){
        setListAdapter(mAdapter);
    }
}
