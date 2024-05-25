package com.android.classifiedapp.models;

public class User {
    String name,email,profileImage,fcmToken,role;
    boolean isPremiumUser;
    int freeMessagesAvailable, freeAdsAvailable;

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
}
