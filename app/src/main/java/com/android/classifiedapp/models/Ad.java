package com.android.classifiedapp.models;

import java.util.List;

public class Ad {

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
}
