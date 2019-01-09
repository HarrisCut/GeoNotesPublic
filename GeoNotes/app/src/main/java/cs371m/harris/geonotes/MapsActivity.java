package cs371m.harris.geonotes;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements AddGeoNoteFragment.OnFragmentInteractionListener, EditGeoNote.OnFragmentInteractionListener{
    private static int RC_MAP_SIGN_OUT = 2;
    private GoogleMap gmap;

    private SearchView searchGeoNotes;
    private Button addGeoNote;
    private Button centerLocationOnSelfButton;
    private DrawerLayout drawerLayout;
    private Button logoutButton;

    FragmentManager fragmentManager;
    FragmentTransaction transaction;

    private MapHolder mapHolder;
    private Locations myLocations;

    private String currentUser;
    private GeoNoteHolder geoNoteHolder;
    private FirestoreHolder firestoreHolder = new FirestoreHolder();


    public MapsActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = getIntent();
        currentUser = data.getStringExtra("user");

        mapHolder = new MapHolder(getApplicationContext(), currentUser, this);

        myLocations = new Locations(this, currentUser, mapHolder);
        myLocations.initLocation();

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);



        mapFragment.getMapAsync(mapHolder);

        fragmentManager = getSupportFragmentManager();

        searchGeoNotes = findViewById(R.id.note_search);
        addGeoNote = findViewById(R.id.add_geonote);
        centerLocationOnSelfButton = findViewById(R.id.center_on_location_button);
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        logoutButton = findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapHolder.clearMap();
                GeoNoteHolder.getInstance().clearGeoList();

                Intent signOutIntent = new Intent(getApplicationContext(), MainActivity.class);
                setResult(RESULT_OK, signOutIntent);
                Log.i("finishing maps", "About to finish maps from logout button");
                finish();
            }
        });

        addGeoNote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//              Toast.makeText(getApplicationContext(), "add a new geonote", Toast.LENGTH_LONG).show();
                //searchGeoNotes.setVisibility(View.GONE);
                //searchGeoNotes.setClickable(false);
                //addGeoNote.setVisibility(View.GONE);
                //addGeoNote.setClickable(false);
                Fragment noteFragment = new AddGeoNoteFragment();
                Bundle userBundle = new Bundle();
                userBundle.putString("user", currentUser);
                noteFragment.setArguments(userBundle);
                transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.map, noteFragment);
                transaction.addToBackStack("map");
                transaction.commit();

            }
        });

        searchGeoNotes.setVisibility(View.INVISIBLE);
        searchGeoNotes.setClickable(false);

        searchGeoNotes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mapHolder.showSearchAddress(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        centerLocationOnSelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              Toast toast = Toast.makeText(getApplicationContext(), "Center on self just clicked", Toast.LENGTH_SHORT);
//              toast.show();
                Location currentLocation = myLocations.getCurrentLocation();
                if(currentLocation == null)
                    Log.i("currentlocation check", "The current location is null");
                mapHolder.moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        });

        getAllGeoNotesFromFirestore(currentUser);

        this.setSupportActionBar(toolbar);
        ActionBar actionbar = this.getSupportActionBar();
        Log.i("action bar", actionbar == null ? "null action bar" : "non null actionbar");
        if(actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

            NavigationView navigationView = findViewById(R.id.nav_id);
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    menuItem.setChecked(true);
                    drawerLayout.closeDrawers();

                    currentUser = null;
                    finish();
                    return true;
                }
            });
        }

       // myLocations.centerCameraOnSelf();
    }

    @Override
    public void onResume() {
        Log.i("in onResume of MapsActivity", "Just entered onResume() in MapsActivity");
        super.onResume();
        Log.i("finished onResume super call", "Just finished super call of onResume() in MapsActivity");
//        searchGeoNotes.setVisibility(View.VISIBLE);
//        searchGeoNotes.setClickable(true);
        addGeoNote.setVisibility(View.VISIBLE);
        addGeoNote.setClickable(true);
        onReturnToMap();
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

    public void onFragmentInteraction(Uri uri) {
//        Toast.makeText(this, "switched fragments", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReturnToMap() {
        // need to do the same thing when the back button is pressed
        Log.i("in onReturnToMap", "Just entered onReturnToMa()p in MapsActivity");
//        searchGeoNotes.setVisibility(View.VISIBLE);
//        searchGeoNotes.setClickable(true);
        addGeoNote.setVisibility(View.VISIBLE);
        addGeoNote.setClickable(true);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if(fragment instanceof AddGeoNoteFragment) {
            AddGeoNoteFragment addGeoNoteFragment = (AddGeoNoteFragment) fragment;
            addGeoNoteFragment.setOnFragmentInteractionListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return true;
    }

    public void getAllGeoNotesFromFirestore(String user) {
        final QuerySnapshot d;
        firestoreHolder.db.collection("Users")
                .document(user)
                .collection("Geonotes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> geonote = document.getData();
                                GeoNote note = new GeoNote((String)geonote.get("Name"), ((String)geonote.get("Message")), (GeoPoint)geonote.get("Location"), document.getId());

                                GeoNoteHolder.getInstance().addGeoNote(note);

                                Marker marker = mapHolder.addMarker(note.getLocationAsLatLng());
                                marker = mapHolder.setMarkerStuff(marker, note);
                                mapHolder.addMarkerToList(marker);
                            }
                        }
                        else {
                            Log.i("geonote retrieval failure", "Failed to retrieve GeoNotes from firestore");
                        }
                    }
                });

    }

    public void editFragment(GeoNote note) {
        Fragment editFragment = new EditGeoNote();
        Bundle userBundle = new Bundle();
        userBundle.putString("user", currentUser);
        userBundle.putString("name", note.getName());
        userBundle.putString("message", note.getMessage());
        userBundle.putString("location", note.getLocationAsLatLng().toString());
        userBundle.putString("uuid", note.getUuid());
        editFragment.setArguments(userBundle);

        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.map, editFragment);
        transaction.addToBackStack("map");
        transaction.commit();
    }

    @Override
    public void addNoteToMap(String address, String name, String message) {
        if(mapHolder.showAddress(address, currentUser, name, message, firestoreHolder)) {
            // success, failure handled in showAddress
        }
    }

    public void deleteNoteOnMap(String uuid) {
        firestoreHolder.deleteGeoNote(currentUser, uuid);
        GeoNoteHolder.getInstance().deleteFromList(uuid);
        mapHolder.deleteMarkerFromList(uuid);
    }

    public void editNoteOnMap(String address, String name, String message, String uuid) {
        firestoreHolder.editGeoNote(currentUser, name, message, uuid);
        GeoNoteHolder.getInstance().updateList(name, message, uuid);
        mapHolder.updateMarkerList(name, message, uuid);
    }

}
