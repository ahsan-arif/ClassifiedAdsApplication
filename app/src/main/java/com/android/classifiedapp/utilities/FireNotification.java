package com.android.classifiedapp.utilities;


import static com.android.classifiedapp.utilities.Constants.NOTIFICATION_URL;
import static com.android.classifiedapp.utilities.Constants.SCOPES;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.classifiedapp.R;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.blankj.utilcode.util.LogUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FireNotification {
    public static void prepNotification(String token, Context context, String title, String body) {
        LogUtils.e("prepNotification");
        JSONObject message = new JSONObject();
        JSONObject to = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("title", title);
            data.put("body", body);

            to.put("token", token);
            to.put("data", data);

            message.put("message", to);
            if (token != null) {
                sentNotification(message, context);
            LogUtils.e("PostDetail_makeOffer", "sentNotification:-------------------------------------------------------- "+token );


            }
        } catch (JSONException e) {
            LogUtils.e(e.getMessage()) ;
        }



    }

    private static void sentNotification(JSONObject to, Context context) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NOTIFICATION_URL,to, response -> {
            LogUtils.e("PostDetail_makeOffer", "sentNotification:--------------------------------------------------------response "+response );

        },error -> {
            LogUtils.e("PostDetail_makeOffer", "sentNotification:--------------------------------------------------------error network response  "+error.networkResponse );
            LogUtils.e("PostDetail_makeOffer", "sentNotification:--------------------------------------------------------error response "+error );
        }){
            @Override
            public Map<String, String> getHeaders() {

                Map<String,String> map = new HashMap<>();
                try {
                    String tkn =  getAccessToken(context);
                    map.put("Authorization", "Bearer " + tkn);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                map.put("Content-Type", "application/json");

                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);



    }

    public static String getAccessToken(Context context) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.service_account);

        GoogleCredentials googleCredential = GoogleCredentials
                .fromStream(inputStream)
                .createScoped(Arrays.asList(SCOPES));
        googleCredential.refresh();

        LogUtils.e("PostDetail_makeOffer", "FireNotification java class getAccessToken: " + googleCredential.toString());
        return googleCredential.getAccessToken().getTokenValue();
    }



}
