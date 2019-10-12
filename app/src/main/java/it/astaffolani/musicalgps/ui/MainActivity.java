package it.astaffolani.musicalgps.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import it.astaffolani.musicalgps.R;
import it.astaffolani.musicalgps.database.Song;
import it.astaffolani.musicalgps.plugins.SongSelector;

/*TODO For a complete mediaPlayer: https://github.com/googlesamples/android-SimpleMediaPlayer*/

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 42;
    private static final long INTERVAL = 1000 * 10; // 10 seconds
    private static final long FASTEST_INTERVAL = 1000 * 5; // 5 seconds
    private static final float MIN_DISTANCE_BETWEEN_CURRENT_AND_SONG_LOCATION = 1000 * 20; // 20km

    private CardView activeSongCard;
    private LinearLayout mySongsContainer;
    private FloatingActionButton addSongButton;
    private Button startButton;
    private Button pauseButton;
    private Button stopButton;

    private SongViewModel songViewModel;

    private LocationCallback locationCallback;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;

    private SongSelector songSelector;
    private Song activeSong;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("My Locations");

        bindViewObjects();

        // Init songDbViewModel
        songViewModel = ViewModelProviders.of(this).get(SongViewModel.class);

        // Init Focused Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Attach the observer for database data
        observerSongSetup();

        // Check location settings
        addLocationUpdateRequest();

        // Create new SongSelector
        songSelector = new SongSelector(MIN_DISTANCE_BETWEEN_CURRENT_AND_SONG_LOCATION);

        addButtonHandler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void bindViewObjects() {
        activeSongCard = findViewById(R.id.activeSongCard);
        mySongsContainer = findViewById(R.id.mySongsContainer);
        addSongButton = findViewById(R.id.addSong);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);
    }

    private void addButtonHandler() {
        addSongButton.setOnClickListener(e -> handleAddSongClicked());
        startButton.setOnClickListener(e -> startMediaPlayer());
        pauseButton.setOnClickListener(e -> pauseMediaPlayer());
        stopButton.setOnClickListener(e -> stopMediaPlayer());
    }

    protected void handleAddSongClicked() {
        Intent intent = new Intent(this, MapsLocationActivity.class);
        startActivity(intent);
    }

    protected void observerSongSetup() {
        songViewModel.getAllSongs().observe(this,
                songs -> displayMySongs(songs));

        songViewModel.getSearchResults().observe(this,
                songs -> {
                    if (songs.size() > 0) {
                        Log.d(TAG, "Songs search resault: " + songs.toString());
                    } else {
                        Log.d(TAG, "Songs search resault empty");
                    }
                });
    }

    protected void displayMySongs(List<Song> mySongs) {
        Log.d(TAG, mySongs.size() + "");
        if (mySongs.size() == 0) {
            TextView infoText = new TextView(this);
            infoText.setText(getString(R.string.no_songs));
            infoText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            mySongsContainer.addView(infoText);
        } else {
            songSelector.setAllSongs(mySongs);

            for (Song song : mySongs) {
                TextView infoText = new TextView(this);
                StringBuilder sb = new StringBuilder(song.name);
                sb.append("\n\tLocation: ");
                sb.append(song.locationName);
                sb.append(" lat: ");
                sb.append(song.locationLat);
                sb.append(" lng:");
                sb.append(song.locationLng);
                infoText.setText(sb.toString());
                infoText.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                mySongsContainer.addView(infoText);
            }
        }
    }

    protected void displayActiveSong() {
        if (activeSong != null) {
            TextView activeSongName = findViewById(R.id.activeSongName);
            activeSongName.setText(activeSong.name);
            TextView locationName = findViewById(R.id.locationName);
            locationName.setText(getString(R.string.location) + ": " + activeSong.locationName);
            TextView latText = findViewById(R.id.activeSongLat);
            latText.setText( getString(R.string.latitude) + " " + activeSong.locationLat);
            TextView lngText = findViewById(R.id.activeSongLng);
            lngText.setText(getString(R.string.longitude) + " " + activeSong.locationLng);
        }
    }

    protected void addLocationUpdateRequest() {
        // Create a location request to be notified any 10 seconds about new device location
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(INTERVAL); // 10 seconds
        locationRequest.setFastestInterval(FASTEST_INTERVAL); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        // Check the current Location settings
        checkLocationSettings(builder);

        // Add the location request
        builder.addLocationRequest(locationRequest);

    }

    protected void checkLocationSettings(LocationSettingsRequest.Builder builder) {
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                Log.d(TAG, "Location setting ok");
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    protected void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                List<Location> allResultLocations = locationResult.getLocations();
                for (Location location : allResultLocations) {
                    // Update UI with location data
                    // ...
                    Log.d(TAG, "Device location: lat: " + location.getLatitude() + ", lng: " + location.getLongitude());
                }
                songSelector.setCurrentLocation(allResultLocations.get(0));
                setActiveSong();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        createLocationCallback();

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        mediaPlayerRelease();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    protected void mediaPlayerInitializer() {
        if (mediaPlayer == null) {
            Uri myUri = Uri.parse(activeSong.filePath); // initialize Uri here
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(getApplicationContext(), myUri);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(e -> startMediaPlayer());
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    protected void startMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            mediaPlayerInitializer();
        }
    }

    protected void pauseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    protected void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayerRelease();
        }
    }

    protected void setActiveSong() {
        if (activeSong == null) {
            activeSong = songSelector.getActiveSong();
            mediaPlayerRelease();
            displayActiveSong();
        } else {
            Song newActiveSong = songSelector.getActiveSong();
            if (!activeSong.equals(newActiveSong)) {
                activeSong = newActiveSong;
                mediaPlayerRelease();
                displayActiveSong();
            }
        }
    }

    protected void mediaPlayerRelease() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
