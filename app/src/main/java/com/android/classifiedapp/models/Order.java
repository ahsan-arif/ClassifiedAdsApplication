package com.android.classifiedapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Order implements Parcelable {

    String id;
    int quantity;
    String title;
    String productId;
    Double amount;
    String sellerId,buyerId;

    String status;

    String currency,placeOn,address;
    String confirmedOn,pickedOn,shippedOn,deliveredOn;

    boolean isRated,isSettled;

    public Order() {
    }

    protected Order(Parcel in) {
        id = in.readString();
        quantity = in.readInt();
        title = in.readString();
        productId = in.readString();
        if (in.readByte() == 0) {
            amount = null;
        } else {
            amount = in.readDouble();
        }
        sellerId = in.readString();
        buyerId = in.readString();
        status = in.readString();
        currency = in.readString();
        placeOn = in.readString();
        address = in.readString();
        confirmedOn = in.readString();
        pickedOn = in.readString();
        shippedOn = in.readString();
        deliveredOn = in.readString();
        isRated = in.readInt() != 0;
        isSettled = in.readInt() != 0;
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPlaceOn() {
        return placeOn;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPlaceOn(String placeOn) {
        this.placeOn = placeOn;
    }

    public String getConfirmedOn() {
        return confirmedOn;
    }

    public void setConfirmedOn(String confirmedOn) {
        this.confirmedOn = confirmedOn;
    }

    public String getPickedOn() {
        return pickedOn;
    }

    public void setPickedOn(String pickedOn) {
        this.pickedOn = pickedOn;
    }

    public String getShippedOn() {
        return shippedOn;
    }

    public void setShippedOn(String shippedOn) {
        this.shippedOn = shippedOn;
    }

    public boolean isRated() {
        return isRated;
    }

    public void setRated(boolean rated) {
        isRated = rated;
    }

    public String getDeliveredOn() {
        return deliveredOn;
    }

    public void setDeliveredOn(String deliveredOn) {
        this.deliveredOn = deliveredOn;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(quantity);
        dest.writeString(title);
        dest.writeString(productId);
        if (amount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(amount);
        }
        dest.writeString(sellerId);
        dest.writeString(buyerId);
        dest.writeString(status);
        dest.writeString(currency);
        dest.writeString(placeOn);
        dest.writeString(address);
       dest.writeString(confirmedOn);
       dest.writeString(pickedOn);
       dest.writeString(shippedOn);
       dest.writeString(deliveredOn);
        dest.writeInt(isRated ? 1 : 0);
        dest.writeInt(isSettled ? 1 : 0);
    }
}
