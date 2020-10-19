package com.example.twichtopgames.database;

import android.app.Application;

import androidx.room.Room;

public class DataBaseManager extends Application {
    private static DataBaseManager mInstance;
    private GamesDataBase gamesDataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        gamesDataBase = Room.databaseBuilder(getApplicationContext(), GamesDataBase.class, "gamesdb").build();
    }

    public static DataBaseManager getInstance(){
        return mInstance;
    }

    public GamesDataBase getGamesDataBase(){
        return gamesDataBase;
    }

}
