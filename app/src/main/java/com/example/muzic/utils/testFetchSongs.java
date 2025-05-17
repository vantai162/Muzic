package com.example.muzic.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.example.muzic.network.utility.RequestNetwork;
import com.example.muzic.network.utility.RequestNetworkController;
import com.example.muzic.records.GlobalSearch;

import java.util.HashMap;

public class testFetchSongs {

    private final String TAG = "testFetchSongs";
    private final Context mContext;

    public testFetchSongs(Context context){
        mContext = context;
    }

    public void searchSongs(String text){
        RequestNetwork requestNetwork = new RequestNetwork((Activity) mContext);
        HashMap<String, Object> data = new HashMap<>();
        data.put("query", text);
        requestNetwork.setParams(data, RequestNetworkController.REQUEST_PARAM);
        requestNetwork.startRequestNetwork(RequestNetworkController.GET, "https://saavn.dev/api/search", "", new RequestNetwork.RequestListener() {
            @Override
            public void onResponse(String tag, String response, HashMap<String, Object> responseHeaders) {

                GlobalSearch globalSearch = new Gson().fromJson(response, GlobalSearch.class);
                try {
                    //GlobalSearch globalSearch = new ObjectMapper().readValue(response, GlobalSearch.class);
                    if(globalSearch.success())
                        Log.i(TAG, "onResponse: " + globalSearch.data().topQuery().results().get(0).title());

                    Log.i(TAG, "onResponse: " + globalSearch.data().topQuery());

                } catch (Exception  e) {
                    Log.e(TAG, "onResponse: ", e);
                }

            }

            @Override
            public void onErrorResponse(String tag, String message) {
                Log.i(TAG, "onErrorResponse: " + message);
            }
        });
    }

}
