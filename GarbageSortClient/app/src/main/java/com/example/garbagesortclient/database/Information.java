package com.example.garbagesortclient.database;

public class Information {

    private String Key;
    private String Value;

    public Information(String key,String value){
        this.Key = key;
        this.Value = value;
    }

    public String getKey() {
        return Key;
    }

    public String getValue() {
        return Value;
    }

}
