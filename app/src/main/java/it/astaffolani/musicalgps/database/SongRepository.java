package it.astaffolani.musicalgps.database;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;

public class SongRepository {

    private MutableLiveData<List<Song>> searchResults;
    private LiveData<List<Song>> allSongs;
    private SongDao songDao;

    public SongRepository(Application application) {
        searchResults = new MutableLiveData<>();

        SongRoomDatabase db;
        db = SongRoomDatabase.getInMemoryDatabase(application);
        songDao = db.songDao();
        allSongs = songDao.getAll();
    }

    private void asyncFinished(List<Song> results) {
        searchResults.setValue(results);
    }

    public void insert(Song newSong) {
        InsertAsyncTask task = new InsertAsyncTask(songDao);
        task.execute(newSong);
    }

    public void delete(String name) {
        DeleteAsyncTask task = new DeleteAsyncTask(songDao);
        task.execute(name);
    }

    public void findByName(String name) {
        FindByNameTask task = new FindByNameTask(songDao);
        task.delegate = this;
        task.execute(name);
    }

    public void findDefalut() {
        FindDefaultTask task = new FindDefaultTask(songDao);
        task.delegate = this;
        task.execute();
    }

    public LiveData<List<Song>> getAllSongs() {
        return allSongs;
    }

    public MutableLiveData<List<Song>> getSearchResults() {
        return searchResults;
    }

    private static class FindByNameTask extends
            AsyncTask<String, Void, List<Song>> {

        private SongDao asyncTaskDao;
        private SongRepository delegate = null;

        FindByNameTask(SongDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected List<Song> doInBackground(final String... params) {
            return asyncTaskDao.findByName(params[0]);
        }

        @Override
        protected void onPostExecute(List<Song> result) {
            delegate.asyncFinished(result);
        }
    }

    private static class FindDefaultTask extends
            AsyncTask<String, Void, List<Song>> {

        private SongDao asyncTaskDao;
        private SongRepository delegate = null;

        FindDefaultTask(SongDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected List<Song> doInBackground(final String... params) {
            return asyncTaskDao.findDefault();
        }

        @Override
        protected void onPostExecute(List<Song> result) {
            delegate.asyncFinished(result);
        }
    }

    private static class InsertAsyncTask extends AsyncTask<Song, Void, Void> {

        private SongDao asyncTaskDao;

        InsertAsyncTask(SongDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Song... params) {
            asyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<String, Void, Void> {

        private SongDao asyncTaskDao;

        DeleteAsyncTask(SongDao dao) {
            asyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            asyncTaskDao.delete(params[0]);
            return null;
        }
    }
}
