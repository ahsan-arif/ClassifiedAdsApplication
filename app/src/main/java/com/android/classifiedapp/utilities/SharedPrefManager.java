package com.android.classifiedapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static SharedPrefManager mInstance;
    private static Context context;
    private static final String SHARED_PREF_NAME="mall_maweshi_pref";
    private static final String COOKIES = "cookies";
    private static final String NAME = "name";
    private static final String PHONE = "phone";
    private static final String FIRST_TIME = "first_time";
    private static final String MALAMALL_POINTS = "malamall_points";
    private static final String FCM_TOKEN = "fcm_token";
    private static final String USER_ID = "user_id";
    private static String ACCESS_TOKEN = "access_token";

    private static final String APP_LOCALE="appLocale";

    private SharedPrefManager(Context context) {
        this.context=context;
    }

    public static synchronized SharedPrefManager getInstance(Context context){
        if (mInstance==null){
            mInstance=new SharedPrefManager(context);
        }
        return mInstance;
    }

    public void setAppLocale(String locale){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(APP_LOCALE,locale);
        editor.apply();

    }

    public String getAppLocale(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(APP_LOCALE,"es");
    }

    public void setCookies(String cookies){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(COOKIES,cookies);
        editor.apply();
    }

    public String getCookies(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(COOKIES,"");
    }

    public void setName(String name){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(NAME,name);
        editor.apply();
    }

    public void setPhone(String phone){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(PHONE,phone);
        editor.apply();
    }

    public void setFCMToken(String fcmToken){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(FCM_TOKEN,fcmToken);
        editor.apply();
    }

    public void setUserId(String user_id){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(USER_ID,user_id);
        editor.apply();
    }

    public void setAccessToken(String accessToken){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putString(ACCESS_TOKEN,accessToken);
        editor.apply();
    }

    public String getPhone(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(PHONE,"");
    }
    public String getName(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(NAME,"");
    }

    public String getFcmToken(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(FCM_TOKEN,"");
    }
    public String getUserId(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_ID,"");
    }

    public String getAccessToken(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getString(ACCESS_TOKEN,"");
    }

   public void setFirstTime(boolean b){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putBoolean(FIRST_TIME,b);
        editor.apply();
    }
    public void setMalamallPoints(int points){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        editor.putInt(MALAMALL_POINTS,points);
        editor.apply();
    }

    public boolean isFirstTime(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(FIRST_TIME,true);
    }

    public int getMalaMallPoints(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        return sharedPreferences.getInt(MALAMALL_POINTS,0);
    }

    public void clearPreferences(){
        SharedPreferences sharedPreferences=context.getSharedPreferences(SHARED_PREF_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
