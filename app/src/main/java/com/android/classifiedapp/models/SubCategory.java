package com.android.classifiedapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class SubCategory implements Parcelable {
    String name;
    String id;

    String imageUrl;
    List<InternalCategory> internalCategories;

    boolean isSelected;

    public SubCategory() {
    }

    protected SubCategory(Parcel in) {
        name = in.readString();
        id = in.readString();
        imageUrl = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<SubCategory> CREATOR = new Creator<SubCategory>() {
        @Override
        public SubCategory createFromParcel(Parcel in) {
            return new SubCategory(in);
        }

        @Override
        public SubCategory[] newArray(int size) {
            return new SubCategory[size];
        }
    };

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
