package com.example.twichtopgames;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.twichtopgames.adapters.GameAdapter;
import com.example.twichtopgames.database.DataBaseManager;
import com.example.twichtopgames.database.GamesModelDB;
import com.example.twichtopgames.models.Datum;
import com.example.twichtopgames.models.Games;
import com.example.twichtopgames.network.Network;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static com.example.twichtopgames.common.CommonConstants.AUTHORIZATION_BEARER;
import static com.example.twichtopgames.common.CommonConstants.AUTHORIZATION_PREFIX;
import static com.example.twichtopgames.common.CommonConstants.CLIENT_ID;
import static com.example.twichtopgames.common.CommonConstants.ITEM_LIMIT_TO_END;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bRateApp;

    private Games mGames;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<GameAdapter.GameViewHolder> mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private int sizeList = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setupSettingsRecyclerView();
        getGames();
    }

    private void initView() {
        recyclerView = findViewById(R.id.recyclerViewTopGames);
        bRateApp = findViewById(R.id.AM_b_rate_app);
        bRateApp.setOnClickListener(this);
        layoutManager = new LinearLayoutManager(this);
    }

    private void setupSettingsRecyclerView() {
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void getGames() {
        getData().subscribe(new DisposableObserver<Games>() {
            @Override
            public void onNext(Games games) {
                clearAllDb();//очищаем БД при успешном запросе
                mGames = games;
                mAdapter = new GameAdapter<Games>(games);
                recyclerView.setAdapter(mAdapter);
                insertDataDb(games);
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this, "Не удалось подключиться к серверу", Toast.LENGTH_SHORT).show();
                getDataDB().subscribe(new SingleObserver<List<GamesModelDB>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(List<GamesModelDB> gamesModelDBS) {
                        mAdapter = new GameAdapter<List<GamesModelDB>>(gamesModelDBS);
                        recyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this, "Данные в базе данных не обнаружены", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onComplete() {
                addScrollListenerRecyclerView(); //добавляем слушателя при успешном запросе, для фоновой подгрузки item'ов
            }
        });
    }

    private void addScrollListenerRecyclerView() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (sendTheNextRequest()) {
                    sizeList = recyclerView.getAdapter().getItemCount();
                    getData(mGames.getPagination().getCursor()).subscribe(new DisposableObserver<Games>() {
                        @Override
                        public void onNext(Games games) {
                            mGames.setPagination(games.getPagination());
                            mGames.getData().addAll(games.getData());
                            insertDataDb(games);
                        }

                        @Override
                        public void onError(Throwable e) {
                            sizeList -= 20;
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                }
            }
        });
    }

    private boolean sendTheNextRequest() { //Если до конца списка осталось 10 item'ов и количество item'ов не превышает число item'ов при последней отправке запроса(Позволяет избежать дублирование запросов)
        return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition() >= Math.abs(ITEM_LIMIT_TO_END - recyclerView.getAdapter().getItemCount()) && sizeList < recyclerView.getAdapter().getItemCount();
    }

    private Observable<Games> getData() { //первый запрос в сеть при запуске приложения
        return Network.getInstance().getApi().getTopGames(CLIENT_ID, AUTHORIZATION_PREFIX + AUTHORIZATION_BEARER)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());
    }

    private Observable<Games> getData(String afterPagination) { // для последующих запросов подгрузки данных в recycler
        return Network.getInstance().getApi().getTopGames(CLIENT_ID, AUTHORIZATION_PREFIX + AUTHORIZATION_BEARER, afterPagination)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());
    }

    private Single<List<GamesModelDB>> getDataDB() { //получение данных из БД
        return Single.create((SingleOnSubscribe<List<GamesModelDB>>) emitter -> {
            List<GamesModelDB> gamesModelDBList = DataBaseManager.getInstance().getGamesDataBase().gamesDao().getAllGames();

            if (gamesModelDBList.isEmpty()) {
                emitter.onError(new Throwable("Error"));
            } else {
                emitter.onSuccess(gamesModelDBList);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());
    }

    private void insertDataDb(Games games) { //фоновое добавление данных в БД
        Single.create((SingleOnSubscribe<?>) emitter -> {
            for (Datum datum : games.getData()) {
                GamesModelDB gamesModelDB = new GamesModelDB();
                gamesModelDB.coverURL = datum.getBoxArtUrl();
                gamesModelDB.nameGame = datum.getName();
                DataBaseManager.getInstance().getGamesDataBase().gamesDao().insertGame(gamesModelDB);
            }
        })
                .subscribeOn(Schedulers.newThread()).subscribe();
    }

    private void clearAllDb() { //Очистка БД
        Single.create((SingleOnSubscribe<?>) emitter -> {
            DataBaseManager.getInstance().getGamesDataBase().gamesDao().clearAll();
        })
                .subscribeOn(Schedulers.newThread()).subscribe();
    }

    @Override
    public void onClick(View v) {
        showDialogRateApp();
    }

    private void showDialogRateApp() {
        AlertDialog dialog = dialogRateAppCreate();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                EditText etFeedback = ((AlertDialog) dialog).findViewById(R.id.ADRA_et_comment);
                RatingBar ratingBarRateApp = ((AlertDialog) dialog).findViewById(R.id.ADRA_rb_rating_app);
                Button bSendRateApp = ((AlertDialog) dialog).findViewById(R.id.ADRA_b_send);

                bSendRateApp.setOnClickListener(v -> {
                    ((AlertDialog) dialog).dismiss();
                });
            }
        });
        dialog.show();
    }

    private AlertDialog dialogRateAppCreate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setView(getLayoutInflater().inflate(R.layout.alert_dialog_rate_app, null));

        return builder.create();
    }
}