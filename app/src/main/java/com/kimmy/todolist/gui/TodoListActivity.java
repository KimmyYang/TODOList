package com.kimmy.todolist.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import com.kimmy.todolist.R;
import com.kimmy.todolist.TODOList;
import com.kimmy.todolist.data.Constants;
import com.kimmy.todolist.data.TodoRecord;

public class TodoListActivity extends Activity {

    private static final boolean DBG = true;
    private static final boolean VDBG = false;
    private TODOList mTodo = null;
    private TodoListActivityFragment mTodoFragment = null;

    //dialog view
    EditText mDateTimeEdit = null;
    EditText mTaskEdit = null;
    CheckBox mIsFinishCheckBox = null;

    public static final int EVENT_DRAW_LIST_VIEW = 1;
    public static final int EVENT_NOTIFY_DATA_CHANGE = 2;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            log("handleMessage: msg = "+msg.what);
            switch (msg.what){
                case EVENT_DRAW_LIST_VIEW:
                    mTodoFragment = new TodoListActivityFragment(mTodo.getAdapter());
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, mTodoFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                    break;
                case EVENT_NOTIFY_DATA_CHANGE:
                    mTodo.getAdapter().notifyDataSetChanged();
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        mTodo = new TODOList(this, mHandler);
        initView();
    }

    private void initView(){
        Button addBtn = (Button) findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddListDialog();
            }
        });

        Button deleteBtn = (Button) findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        ImageButton refreshBtn = (ImageButton)findViewById(R.id.refreshBtn);
        refreshBtn.setImageResource(R.drawable.refresh_button);
        refreshBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mTodo.refreshList();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todo_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAddListDialog(){
        if(DBG)log("showAddListDialog");
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        AlertDialog.Builder addListDialog = new AlertDialog.Builder(this);
        View view = layoutInflater.inflate(R.layout.add_list_dialog, null);
        initAddListDialogView(view);
        addListDialog.setView(view);
        addListDialog.setPositiveButton(R.string.addBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (VDBG) log("showAddListDialog: Add List");
                TodoRecord record = new TodoRecord(mTodo.getListID());
                record.datetime = mDateTimeEdit.getText().toString();
                record.task = mTaskEdit.getText().toString();
                record.isFinish = mIsFinishCheckBox.isChecked();
                mTodo.addList(record);
            }
        });
        addListDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (VDBG) log("showAddListDialog: Cancel");
            }
        });
        addListDialog.show();
    }

    private void initAddListDialogView(View view){
        mDateTimeEdit = (EditText)view.findViewById(R.id.dateTimeEdited);
        mTaskEdit = (EditText)view.findViewById(R.id.taskEdited);
        mIsFinishCheckBox = (CheckBox)view.findViewById(R.id.isFinishCheckBox);
    }

    private void log(String text){
        Log.d(Constants.TAG,text);
    }
}
