package com.android.classifiedapp.models;

public class Rating {
    String ratedBy;
    String productId;
    float sellerRating,productRating;
    String ratedOn;
    String orderId,sellerId;

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

    public float getSellerRating() {
        return sellerRating;
    }

    public void setSellerRating(float sellerRating) {
        this.sellerRating = sellerRating;
    }

    public float getProductRating() {
        return productRating;
    }

    public void setProductRating(float productRating) {
        this.productRating = productRating;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRatedOn() {
        return ratedOn;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public void setRatedOn(String ratedOn) {
        this.ratedOn = ratedOn;
    }
}
