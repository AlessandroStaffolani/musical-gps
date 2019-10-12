package it.astaffolani.musicalgps.ui;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import it.astaffolani.musicalgps.R;
import it.astaffolani.musicalgps.database.Song;
import it.astaffolani.musicalgps.utils.Alert;

public class CreateSongItemActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = CreateSongItemActivity.class.getSimpleName();
    private static final int READ_FILE_REQUEST_CODE = 42;

    private final LatLng mDefaultLocation = new LatLng(41.9054354,12.491431);
    private static final int DEFAULT_ZOOM = 13;

    private GoogleMap mMap;

    private static final String FILE_TYPE = "audio/*";

    private Song song;

    private EditText songTitle;
    private EditText songFile;
    private CheckBox defaultSong;
    private Button chooseFile;
    private Button addSong;

    private SongViewModel songViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_song_item);

        // Get info from intent
        Intent intent = getIntent();
        extractInfoFromIntent(intent);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Map view component
        mapViewComponents();

        // Create viewModel object
        songViewModel = ViewModelProviders.of(this).get(SongViewModel.class);

        chooseFile.setOnClickListener(e -> handleChooseFileClick());
        addSong.setOnClickListener(e -> handleAddSongClick());
    }

    private void mapViewComponents() {
        songTitle = findViewById(R.id.songTitle);
        songFile = findViewById(R.id.songFile);
        defaultSong = findViewById(R.id.defaultSong);
        chooseFile = findViewById(R.id.chooseFileButton);
        addSong = findViewById(R.id.addSong);
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    protected void handleChooseFileClick() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType(FILE_TYPE);

        startActivityForResult(intent, READ_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                EditText musicFile = findViewById(R.id.songFile);
                musicFile.setText(uri.toString());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (song != null && song.getLocation() != null) {
            mMap.addMarker(new MarkerOptions().position(song.getLatLng()).title(song.locationName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(song.getLatLng(), DEFAULT_ZOOM));
        } else {
            // Add a marker in Rome and move the camera
            mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Marker in Rome"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        }
    }

    protected void extractInfoFromIntent(Intent intent) {
        song = new Song();
        if (intent.getStringExtra(MapsLocationActivity.KEY_LOCATION_PROVIDER) != null) {
            song.locationName = intent.getStringExtra(MapsLocationActivity.KEY_LOCATION_PROVIDER);
        }
        if (intent.getDoubleExtra(MapsLocationActivity.KEY_LOCATION_LAT, 0) != 0) {
            song.locationLat = intent.getDoubleExtra(MapsLocationActivity.KEY_LOCATION_LAT, 0);
        }
        if (intent.getDoubleExtra(MapsLocationActivity.KEY_LOCATION_LNG, 0) != 0) {
            song.locationLng = intent.getDoubleExtra(MapsLocationActivity.KEY_LOCATION_LNG, 0);
        }
    }

    protected void handleAddSongClick() {
        String name = songTitle.getText().toString();
        String filePath = songFile.getText().toString();
        boolean isDefault = defaultSong.isChecked();
        if (name.equals("") || filePath.equals("") || song.getLocation() == null) {
            Alert.info(getApplicationContext(), getString(R.string.required_fields));
        } else {
            song.name = name;
            song.filePath = filePath;
            song.isDefault = isDefault;

            Log.d(TAG, song.toString());
            // Save the new song
            songViewModel.insertSong(song);

            // Return to main activity
            returnToMainActivity();
        }
    }

    protected void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
