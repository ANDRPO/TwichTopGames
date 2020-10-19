package com.example.twichtopgames.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GamesDao {

    @Query("SELECT * FROM GamesModelDB")
    List<GamesModelDB> getAllGames();

    @Query("SELECT*FROM GamesModelDB WHERE idGame = :idGame")
    GamesModelDB getGameById(long idGame);

    @Query("DELETE FROM GamesModelDB")
    void clearAll();

    @Insert
    void insertGame(GamesModelDB gamesModelDB);

    @Delete
    void delete(GamesModelDB gamesModelDB);
}
