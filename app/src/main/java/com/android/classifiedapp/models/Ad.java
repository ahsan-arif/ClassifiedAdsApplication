package com.android.classifiedapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class Ad implements Parcelable {

    String id;
    String categoryId;
    String subcategoryId;
    List<String> Urls;

    String price;
    boolean isShippingAvailable;
    String shippingPayer;
    String currency;
    String description;
    String title;

    String postedBy;
    String postedOn;
    List<String> likedByUsers;

    public Ad() {
    }

    protected Ad(Parcel in) {
        id = in.readString();
        categoryId = in.readString();
        subcategoryId = in.readString();
        Urls = in.createStringArrayList();
        price = in.readString();
        isShippingAvailable = in.readByte() != 0;
        shippingPayer = in.readString();
        currency = in.readString();
        description = in.readString();
        title = in.readString();
        postedBy = in.readString();
        postedOn = in.readString();
        likedByUsers = in.createStringArrayList();
    }

    public static final Creator<Ad> CREATOR = new Creator<Ad>() {
        @Override
        public Ad createFromParcel(Parcel in) {
            return new Ad(in);
        }

        @Override
        public Ad[] newArray(int size) {
            return new Ad[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(String subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public List<String> getUrls() {
        return Urls;
    }

    public void setUrls(List<String> urls) {
        Urls = urls;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public boolean isShippingAvailable() {
        return isShippingAvailable;
    }

    public void setShippingAvailable(boolean shippingAvailable) {
        isShippingAvailable = shippingAvailable;
    }

    public String getShippingPayer() {
        return shippingPayer;
    }

    public void setShippingPayer(String shippingPayer) {
        this.shippingPayer = shippingPayer;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(String postedOn) {
        this.postedOn = postedOn;
    }

    public List<String> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<String> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(categoryId);
        dest.writeString(subcategoryId);
        dest.writeStringList(Urls);
        dest.writeString(price);
        dest.writeByte((byte) (isShippingAvailable ? 1 : 0));
        dest.writeString(shippingPayer);
        dest.writeString(currency);
        dest.writeString(description);
        dest.writeString(title);
        dest.writeString(postedBy);
        dest.writeString(postedOn);
        dest.writeStringList(likedByUsers);
    }
}
