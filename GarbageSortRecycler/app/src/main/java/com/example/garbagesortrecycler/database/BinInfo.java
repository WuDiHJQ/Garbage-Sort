package com.example.garbagesortrecycler.database;

public class BinInfo {

    private int bin_id;
    private String bin_loc;
    private int takeaway_weight;
    private int express_weight;

    public int getBin_id() {
        return bin_id;
    }

    public void setBin_id(int bin_id) {
        this.bin_id = bin_id;
    }

    public String getBin_loc() {
        return bin_loc;
    }

    public void setBin_loc(String bin_loc) {
        this.bin_loc = bin_loc;
    }

    public int getTakeaway_weight() {
        return takeaway_weight;
    }

    public void setTakeaway_weight(int takeaway_weight) {
        this.takeaway_weight = takeaway_weight;
    }

    public int getExpress_weight() {
        return express_weight;
    }

    public void setExpress_weight(int express_weight) {
        this.express_weight = express_weight;
    }
}
