package com.android.classifiedapp.models;

public class Rating {
    String ratedBy;
    String productId;
    float rating;
    String ratedOn;

    public String getRatedBy() {
        return ratedBy;
    }

    public void setRatedBy(String ratedBy) {
        this.ratedBy = ratedBy;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getRatedOn() {
        return ratedOn;
    }

    public void setRatedOn(String ratedOn) {
        this.ratedOn = ratedOn;
    }
}
