package com.android.classifiedapp.utilities;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCMSender {

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String KEY_STRING = "YOUR_ADMIN_SDK_KEY"; // Update with your actual key

    private final Context mContext;
    private final RequestQueue mRequestQueue;

    public FCMSender(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public void send(String message, final Callback callback) {
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_URL, requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + KEY_STRING);
                return headers;
            }
        };

        mRequestQueue.add(request);
    }

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }
}
