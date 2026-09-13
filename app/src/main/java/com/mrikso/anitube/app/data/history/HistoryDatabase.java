package com.mrikso.anitube.app.data.history;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mrikso.anitube.app.data.history.dao.HistoryDao;
import com.mrikso.anitube.app.data.history.dao.LastWatchedEpisodeDao;
import com.mrikso.anitube.app.data.history.enity.HistoryEnity;
import com.mrikso.anitube.app.data.history.enity.LastWatchedEpisodeEnity;

@Database(
        entities = {HistoryEnity.class, LastWatchedEpisodeEnity.class},
        version = 2,
        exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "watch_history.db";
    private static HistoryDatabase sInstance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Remove duplicates for watch_history keeping the latest one
            database.execSQL("DELETE FROM watch_history WHERE id NOT IN " +
                    "(SELECT id FROM (SELECT id, MAX(watch_date) FROM watch_history GROUP BY anime_id))");
            // Add unique index for watch_history
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_watch_history_anime_id` ON `watch_history` (`anime_id`)");

            // Remove duplicates for last_watched_episode
            database.execSQL("DELETE FROM last_watched_episode WHERE id NOT IN " +
                    "(SELECT id FROM (SELECT id, MAX(id) FROM last_watched_episode GROUP BY anime_id, episode_id))");
            // Add unique index for last_watched_episode
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_last_watched_episode_anime_id_episode_id` ON `last_watched_episode` (`anime_id`, `episode_id`)");
        }
    };

    public static HistoryDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(
                                context.getApplicationContext(), HistoryDatabase.class, HistoryDatabase.DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract HistoryDao historyDao();

    public abstract LastWatchedEpisodeDao lastWatchedEpisodeDao();
}
