package com.example.finalproject;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class StoryCreator {

    private static void makeHTTPRequest(String context, String keywords, Context c, View view) throws JSONException {
        String url = "https://api.textcortex.com/v1/texts/social-media-posts";
        String API_KEY = "INSERT_KEY_HERE";

        String[] keywordArray;
        keywordArray = keywords.split(",");


        JSONObject data = new JSONObject();
        data.put("context", context);
        data.put("max_tokens", 200);
        data.put("mode", "twitter");
        data.put("model", "claude-3-haiku");
        data.put("keywords", new JSONArray(keywordArray));

        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, url, data, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String createdStory;
                        try {
                            createdStory =
                                    (response
                                            .getJSONObject("data")
                                            .getJSONArray("outputs")
                                            .getJSONObject(0)
                                            .getString("text"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Log.v("Story", createdStory);
                        createdStory = createdStory.substring(createdStory.indexOf('\n'));
                        ((EditText)view).setText(createdStory);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        try {
                            Log.e("Error in HTTP Request", new String(error.networkResponse.data));
                        } catch(NullPointerException npe) {
                            Log.e("Error in HTTP Request", "Null Pointer Exception");
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + API_KEY);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(c);
        requestQueue.add(request);

    }

    public static void getStory(String context, String keywords, Context c, View view) {
        try {
             makeHTTPRequest(context, keywords, c, view);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
