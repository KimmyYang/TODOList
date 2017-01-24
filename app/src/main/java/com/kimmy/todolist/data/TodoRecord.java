package com.kimmy.todolist.data;

/**
 * Created by KimmyYang on 2017/1/17.
 */
public class TodoRecord {
    public int id;
    public String datetime;
    public String task;
    public boolean isFinish;

    public TodoRecord(int id){
        this.id = id;
    }
}
