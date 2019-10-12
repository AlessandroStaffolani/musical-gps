package it.astaffolani.musicalgps.plugins;

import android.location.Location;
import android.util.Log;

import java.util.List;

import it.astaffolani.musicalgps.database.Song;

public class SongSelector {

    private static final String TAG = SongSelector.class.getSimpleName();

    private float locationRange;
    private List<Song> allSongs;
    private Song defaultSong;
    private Song activeSong;
    private Location currentLocation;

    public SongSelector(float locationRange) {
        this.locationRange = locationRange;
    }

    private void updateActiveSong() {
        if (allSongs != null && allSongs.size() > 0 && currentLocation != null) {
            float minDistance = distanceFromCurrentLocation(allSongs.get(0));
            int minDistanceIndex = 0;
            if (allSongs.get(0).isDefault) {
                defaultSong = allSongs.get(0);
            }
            if (allSongs.size() > 1) {
                for (int i = 1; i < allSongs.size(); i++) {
                    Song song = allSongs.get(i);
                    if (song.isDefault) {
                        defaultSong = song;
                    }

                    float distance = distanceFromCurrentLocation(song);
                    if (distance < minDistance) {
                        minDistance = distance;
                        minDistanceIndex = i;
                    }
                }
            }

            if (minDistance <= locationRange) {
                activeSong = allSongs.get(minDistanceIndex);
            } else {
                activeSong = defaultSong;
            }
        }
    }

    private float distanceFromCurrentLocation(Song song) {
        Location location = new Location(song.locationName);
        location.setLatitude(song.locationLat);
        location.setLongitude(song.locationLng);

        return currentLocation.distanceTo(location);
    }

    public Song getDefaultSong() {
        return defaultSong;
    }

    public Song getActiveSong() {
        updateActiveSong();
        return activeSong;
    }

    public void setAllSongs(List<Song> allSongs) {
        this.allSongs = allSongs;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}
