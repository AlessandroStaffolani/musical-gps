package it.astaffolani.musicalgps.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.List;

import it.astaffolani.musicalgps.database.Song;
import it.astaffolani.musicalgps.database.SongRepository;

public class SongViewModel extends AndroidViewModel {

    private SongRepository repository;
    private LiveData<List<Song>> allSongs;
    private MutableLiveData<List<Song>> searchResults;

    public SongViewModel(@NonNull Application application) {
        super(application);
        repository = new SongRepository(application);
        allSongs = repository.getAllSongs();
        searchResults = repository.getSearchResults();
    }

    MutableLiveData<List<Song>> getSearchResults() {
        return searchResults;
    }

    LiveData<List<Song>> getAllSongs() {
        return allSongs;
    }

    public void insertSong(Song song) {
        repository.insert(song);
    }

    public void findSong(String name) {
        repository.findByName(name);
    }

    public void findDefaultSong() {
        repository.findDefalut();
    }

    public void deleteSong(String name) {
        repository.delete(name);
    }
}
