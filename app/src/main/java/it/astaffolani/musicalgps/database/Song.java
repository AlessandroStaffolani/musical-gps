package it.astaffolani.musicalgps.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

@Entity
public class Song {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    @NonNull
    public String name;

    @ColumnInfo(name = "file_path")
    @NonNull
    public String filePath;

    @ColumnInfo(name = "is_default")
    public boolean isDefault;

    @ColumnInfo(name = "location_name")
    @NonNull
    public String locationName;

    @ColumnInfo(name = "location_latitude")
    @NonNull
    public Double locationLat;

    @ColumnInfo(name = "location_longitude")
    @NonNull
    public Double locationLng;

    public Location getLocation() {
        if (locationName != null && locationLat != null && locationLng != null) {
            Location location = new Location(locationName);
            location.setLatitude(locationLat);
            location.setLongitude(locationLng);
            return location;
        } else {
            return null;
        }
    }

    public LatLng getLatLng() {
        return new LatLng(locationLat, locationLng);
    }

    @Override
    public String toString() {
        return "Song{" +
                "name='" + name + '\'' +
                ", filePath='" + filePath + '\'' +
                ", isDefault=" + isDefault +
                ", locationName='" + locationName + '\'' +
                ", locationLat=" + locationLat +
                ", locationLng=" + locationLng +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song)) return false;
        Song song = (Song) o;
        return id == song.id &&
                isDefault == song.isDefault &&
                name.equals(song.name) &&
                filePath.equals(song.filePath) &&
                locationName.equals(song.locationName) &&
                locationLat.equals(song.locationLat) &&
                locationLng.equals(song.locationLng);
    }
}
