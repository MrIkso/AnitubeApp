package com.mrikso.anitube.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.mrikso.anitube.app.data.dao.AnimeDao;
import com.mrikso.anitube.app.data.dao.AnimeRemoteKeysDao;
import com.mrikso.anitube.app.data.model.AnimeRemoteKeys;
import com.mrikso.anitube.app.model.AnimeReleaseModel;

@Database(
        entities = {AnimeReleaseModel.class, AnimeRemoteKeys.class},
        version = 1,
        exportSchema = false)
 @TypeConverters(Converters.class)
public abstract class AnimeDatabase extends RoomDatabase {

    public abstract AnimeDao animeDao();

    public abstract AnimeRemoteKeysDao animeRemoteKeysDao();

    private static volatile AnimeDatabase INSTANCE;

    public static AnimeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AnimeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE =
                            Room.databaseBuilder(
                                            context.getApplicationContext(),
                                            AnimeDatabase.class,
                                            "anime.db")
                                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
