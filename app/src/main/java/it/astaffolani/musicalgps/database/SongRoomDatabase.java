package it.astaffolani.musicalgps.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

// Reference https://www.techotopia.com/index.php/An_Android_Room_Database_and_Repository_Tutorial

@Database(entities = {Song.class}, version = 1)
public abstract class SongRoomDatabase extends RoomDatabase {

    private static SongRoomDatabase INSTANCE;

    public abstract SongDao songDao();

    public static SongRoomDatabase getInMemoryDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(
                            context.getApplicationContext(),
                            SongRoomDatabase.class,
                            "song_database")
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
