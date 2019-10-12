package it.astaffolani.musicalgps.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface SongDao {

    @Query("SELECT * FROM song")
    LiveData<List<Song>> getAll();

    @Query("SELECT * FROM song WHERE name = :name")
    List<Song> findByName(String name);

    @Query("SELECT * FROM song WHERE is_default = 1 LIMIT 1")
    List<Song> findDefault();

    @Insert(onConflict = IGNORE)
    void insert(Song users);

    @Query("DELETE FROM song WHERE name = :name")
    void delete(String name);
}
