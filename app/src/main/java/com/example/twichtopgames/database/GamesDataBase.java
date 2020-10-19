package com.example.twichtopgames.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {GamesModelDB.class}, version = 1)
public abstract class GamesDataBase extends RoomDatabase {
    public abstract GamesDao gamesDao();
}
