package com.example.twichtopgames.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.twichtopgames.R;
import com.example.twichtopgames.database.GamesModelDB;
import com.example.twichtopgames.models.Games;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GameAdapter<T> extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    public static class GameViewHolder extends RecyclerView.ViewHolder {

        public ImageView coverGame;
        public TextView nameGame;
        public TextView numbersChannels;
        public TextView numbersSpectators;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            coverGame = itemView.findViewById(R.id.IGI_iv_game);
            nameGame = itemView.findViewById(R.id.IGI_tv_name_game);
            numbersChannels = itemView.findViewById(R.id.IGI_tv_numbers_channels);
            numbersSpectators = itemView.findViewById(R.id.IGI_tv_numbers_viewers);
        }
    }

    private T games;

    public GameAdapter(T games) {
        this.games = (T) games;
    }

    @NonNull
    @Override
    public GameAdapter.GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_info, parent, false);
        GameViewHolder gameViewHolder = new GameViewHolder(v);
        return gameViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        if (games instanceof Games) {
            holder.nameGame.setText(((Games) games).getData().get(position).getName());
            Picasso.with(holder.itemView.getContext()).load(convertUrl(((Games) games).getData().get(position).getBoxArtUrl())).into(holder.coverGame);
        } else {
            holder.nameGame.setText(((ArrayList<GamesModelDB>) games).get(position).nameGame);
            Picasso.with(holder.itemView.getContext()).load(convertUrl(convertUrl(((ArrayList<GamesModelDB>) games).get(position).coverURL))).networkPolicy(NetworkPolicy.OFFLINE).into(holder.coverGame);
        }
    }

    private String convertUrl(String url) {
        return url.replace("{width}", "720").replace("{height}", "1080");
    }

    @Override
    public int getItemCount() {
        if (games instanceof Games)
            return ((Games) games).getData().size();
        else
            return ((ArrayList<GamesModelDB>) games).size();
    }
}
