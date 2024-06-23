package com.android.classifiedapp.models;

public class PlatformPrefs {
    String platformFee;
    String featuredAdFee;
    int freeAdsCount;
    int freeMessagesCount;

    int maximumOrdersAllowed;

    double maximumListingPrice;

    public String getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(String platformFee) {
        this.platformFee = platformFee;
    }

    public String getFeaturedAdFee() {
        return featuredAdFee;
    }

    public void setFeaturedAdFee(String featuredAdFee) {
        this.featuredAdFee = featuredAdFee;
    }

    public int getFreeAdsCount() {
        return freeAdsCount;
    }

    public void setFreeAdsCount(int freeAdsCount) {
        this.freeAdsCount = freeAdsCount;
    }

    public int getFreeMessagesCount() {
        return freeMessagesCount;
    }

    public void setFreeMessagesCount(int freeMessagesCount) {
        this.freeMessagesCount = freeMessagesCount;
    }

    public int getMaximumOrdersAllowed() {
        return maximumOrdersAllowed;
    }

    public void setMaximumOrdersAllowed(int maximumOrdersAllowed) {
        this.maximumOrdersAllowed = maximumOrdersAllowed;
    }

    public double getMaximumListingPrice() {
        return maximumListingPrice;
    }

    public void setMaximumListingPrice(double maximumListingPrice) {
        this.maximumListingPrice = maximumListingPrice;
    }
}
