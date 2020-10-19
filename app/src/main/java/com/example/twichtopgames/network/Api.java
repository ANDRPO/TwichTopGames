package com.example.twichtopgames.network;

import com.example.twichtopgames.models.Games;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface Api {
    @GET("helix/games/top")
    Observable<Games> getTopGames(
            @Header("Client-ID") String clientId,
            @Header("Authorization") String bearerToken);

    @GET("helix/games/top")
    Observable<Games> getTopGames(
            @Header("Client-ID") String clientId,
            @Header("Authorization") String bearerToken,
            @Query("after") String afterPagination);
}
