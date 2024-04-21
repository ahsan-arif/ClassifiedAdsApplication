package com.android.classifiedapp.models;

import java.util.List;

public class SubCategory {
    String name;
    String id;

    String imageUrl;
    List<InternalCategory> internalCategories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<InternalCategory> getInternalCategories() {
        return internalCategories;
    }

    public void setInternalCategories(List<InternalCategory> internalCategories) {
        this.internalCategories = internalCategories;
    }
}
