package com.example.twichtopgames.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Network {
    private static Network mInstance;
    private Retrofit retrofit;
    private final String TARGET_URL = "https://api.twitch.tv/";

    public Network(){
        this.retrofit = new Retrofit.Builder()
                .baseUrl(TARGET_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static Network getInstance() {
        if(mInstance == null){
            mInstance = new Network();
        }
        return mInstance;
    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }

}
