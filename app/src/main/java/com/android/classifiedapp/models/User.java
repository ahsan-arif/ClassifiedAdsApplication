package com.android.classifiedapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User implements Parcelable {
    String name,email,profileImage,fcmToken,role;
    boolean isPremiumUser;
    int freeMessagesAvailable, freeAdsAvailable,maximumOrdersAvailable;
    long benefitsExpiry;

    String id;

    public User() {
    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        profileImage = in.readString();
        fcmToken = in.readString();
        role = in.readString();
        isPremiumUser = in.readByte() != 0;
        freeMessagesAvailable = in.readInt();
        freeAdsAvailable = in.readInt();
        maximumOrdersAvailable = in.readInt();
        benefitsExpiry = in.readLong();
        id = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isPremiumUser() {
        return isPremiumUser;
    }

    public void setPremiumUser(boolean premiumUser) {
        isPremiumUser = premiumUser;
    }

    public int getFreeMessagesAvailable() {
        return freeMessagesAvailable;
    }

    public void setFreeMessagesAvailable(int freeMessagesAvailable) {
        this.freeMessagesAvailable = freeMessagesAvailable;
    }

    public int getFreeAdsAvailable() {
        return freeAdsAvailable;
    }

    public void setFreeAdsAvailable(int freeAdsAvailable) {
        this.freeAdsAvailable = freeAdsAvailable;
    }

    public int getMaximumOrdersAvailable() {
        return maximumOrdersAvailable;
    }

    public void setMaximumOrdersAvailable(int maximumOrdersAvailable) {
        this.maximumOrdersAvailable = maximumOrdersAvailable;
    }

    public long getBenefitsExpiry() {
        return benefitsExpiry;
    }

    public void setBenefitsExpiry(long benefitsExpiry) {
        this.benefitsExpiry = benefitsExpiry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(profileImage);
        dest.writeString(fcmToken);
        dest.writeString(role);
        dest.writeByte((byte) (isPremiumUser ? 1 : 0));
        dest.writeInt(freeMessagesAvailable);
        dest.writeInt(freeAdsAvailable);
        dest.writeInt(maximumOrdersAvailable);
        dest.writeLong(benefitsExpiry);
        dest.writeString(id);
    }
}
