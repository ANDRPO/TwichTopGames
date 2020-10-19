package com.example.twichtopgames.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GamesModelDB {
    @PrimaryKey(autoGenerate = true)
    public long idGame;
    public String coverURL;
    public String nameGame;
}
