package com.example.garbagesortclient.database;

import java.util.List;

public class Message {

    private String msg;
    private List<Record> list = null;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String meg) {
        this.msg = meg;
    }

    public List<Record> getList() {
        return list;
    }

    public void setList(List<Record> list) {
        this.list = list;
    }

}
