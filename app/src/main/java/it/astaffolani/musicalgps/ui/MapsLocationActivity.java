package it.astaffolani.musicalgps.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

import it.astaffolani.musicalgps.R;
import it.astaffolani.musicalgps.utils.Alert;

public class MapsLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsLocationActivity.class.getSimpleName();
    private static final String KEY_LOCATION = "location";
    public static final String KEY_LOCATION_PROVIDER = "key_intent_location_provider";
    public static final String KEY_LOCATION_LAT = "key_intent_location_lat";
    public static final String KEY_LOCATION_LNG = "key_intent_location_lng";

    private final LatLng mDefaultLocation = new LatLng(41.9054354,12.491431);
    private static final int DEFAULT_ZOOM = 13;

    private GoogleMap mMap;

    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            Log.d(TAG, "Gettinng saved location: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
        }

        setContentView(R.layout.activity_maps_location);

        getSupportActionBar().setTitle(R.string.title_activity_chose_location);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initializeAutocomplete();

        Button next = findViewById(R.id.mapLocationNextButton);
        next.setOnClickListener(e -> handleNextClick());
    }

    protected void handleNextClick() {
        if (lastLocation == null) {
            Alert.info(getApplicationContext(), getString(R.string.select_location));
        } else {
            Intent intent = new Intent(this, CreateSongItemActivity.class);
            intent.putExtra(KEY_LOCATION_PROVIDER, lastLocation.getProvider());
            intent.putExtra(KEY_LOCATION_LAT, lastLocation.getLatitude());
            intent.putExtra(KEY_LOCATION_LNG, lastLocation.getLongitude());
            startActivity(intent);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (lastLocation != null) {
            setPlaceOnMap();
        } else {
            // Add a marker in Rome and move the camera
            mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Marker in Rome"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        }
    }

    protected void initializeAutocomplete() {
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setTypeFilter(TypeFilter.GEOCODE);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + " (lat: " + place.getLatLng().latitude + ", lng: " + place.getLatLng().longitude + ")" );
                setLastLocation(place);
                setPlaceOnMap();
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    protected void setLastLocation(Place place) {
        lastLocation = new Location(place.getName());
        if (place.getLatLng() != null) {
            lastLocation.setLatitude(place.getLatLng().latitude);
            lastLocation.setLongitude(place.getLatLng().longitude);
        } else {
            lastLocation = null;
        }
    }

    protected void setPlaceOnMap() {
        mMap.clear();
        if (lastLocation != null) {
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(lastLocation.getProvider()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "Saving pre state " + lastLocation );
        if (lastLocation != null) {
            savedInstanceState.putParcelable(KEY_LOCATION, lastLocation);
            Log.d(TAG, "Saving lastLocation");
        }
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            lastLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            Log.d(TAG, "Gettinng saved location: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
        }
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        if (lastLocation != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(KEY_LOCATION_PROVIDER, lastLocation.getProvider());
            editor.putString(KEY_LOCATION_LAT, Double.valueOf(lastLocation.getLatitude()).toString());
            editor.putString(KEY_LOCATION_LNG, Double.valueOf(lastLocation.getLongitude()).toString());
            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (!sharedPref.getString(KEY_LOCATION_PROVIDER, "").equals("")
                && !sharedPref.getString(KEY_LOCATION_LAT, "").equals("")
                && !sharedPref.getString(KEY_LOCATION_LNG, "").equals("")) {
            String provider = sharedPref.getString(KEY_LOCATION_PROVIDER, "");
            double lat = Double.parseDouble(sharedPref.getString(KEY_LOCATION_LAT, ""));
            double lng = Double.parseDouble(sharedPref.getString(KEY_LOCATION_LNG, ""));

            lastLocation = new Location(provider);
            lastLocation.setLatitude(lat);
            lastLocation.setLongitude(lng);
        }
    }*/
}
