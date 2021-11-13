package com.example.garbagesortclient.database;

import org.litepal.crud.LitePalSupport;


public class Record extends LitePalSupport {

    private String time;
    private String bin_loc;
    private int trash_weight;
    private int trash_type;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBin_loc() {
        return bin_loc;
    }

    public void setBin_loc(String bin_loc) {
        this.bin_loc = bin_loc;
    }

    public int getTrash_weight() {
        return trash_weight;
    }

    public void setTrash_weight(int trash_weight) {
        this.trash_weight = trash_weight;
    }

    public int getTrash_type() {
        return trash_type;
    }

    public void setTrash_type(int trash_type) {
        this.trash_type = trash_type;
    }

}
