package com.android.classifiedapp.models;

import java.util.ArrayList;
import java.util.List;

public class GroupedItem {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_AD = 1;

    private int type;
    private Category category;
    private ArrayList<Ad> ads;

    public GroupedItem(int type, Category category, ArrayList<Ad> ads) {
        this.type = type;
        this.category = category;
        this.ads = ads;
    }

    public int getType() {
        return type;
    }

    public Category getCategory() {
        return category;
    }

    public ArrayList<Ad> getAd() {
        return ads;
    }
}
