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

    Double latitude,longitude;
    String address;

    String updatedOn;

    List<Report> reports;

    String status;
    List<String> blockedBy;

    int quantity;

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
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
        updatedOn = in.readString();
        reports = in.createTypedArrayList(Report.CREATOR);
        status = in.readString();
        blockedBy = in.createStringArrayList();
        quantity = in.readInt();
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(String updatedOn) {
        this.updatedOn = updatedOn;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(List<String> blockedBy) {
        this.blockedBy = blockedBy;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        dest.writeString(updatedOn);
        dest.writeTypedList(reports);
        dest.writeString(status);
        dest.writeStringList(blockedBy);
        dest.writeInt(quantity);
    }
}
