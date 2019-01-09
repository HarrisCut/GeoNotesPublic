package cs371m.harris.geonotes;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapHolder implements OnMapReadyCallback {
    private float defaultZoom = 15.0f;
    private GoogleMap gMap;
    private Context context;
    ArrayList<Marker> markerList = new ArrayList<>();

    MapsActivity mapsActivity;


    private static class NameToLatLngTask extends AsyncTask<String, Object, LatLng> {
        public interface OnLatLngCallback {
            public void onLatLng(LatLng a);
        }

        OnLatLngCallback cb;
        Context context;

        URL geocodeURLBuilder(String address) {
            URL result = null;
            try {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("maps.googleapis.com")
                        .appendPath("maps")
                        .appendPath("geocode")
                        .appendPath("json")
                        .appendQueryParameter("key", context.getResources().getString(R.string.google_maps_key))
                        .appendQueryParameter("address", URLEncoder.encode(address, "UTF-8"));
                result = new URL(builder.build().toString());
            } catch (UnsupportedEncodingException e) {

            } catch (MalformedURLException e) {

            }
            return result;
        }

        public NameToLatLngTask(Context ctx, String addr, OnLatLngCallback _cb) {
            context = ctx;
            execute(addr);
            cb = _cb;
        }

        protected LatLng latLngFromJsonString(String json) throws JSONException {
            JSONObject obj = new JSONObject(json);
            LatLng result = null;
            if(!obj.getString("status").equals("OK")) {

            } else {
                JSONObject loc = obj.getJSONArray("results").getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");
                double lat = loc.getDouble("lat");
                double lng = loc.getDouble("lng");
                result = new LatLng(lat, lng);
            }

            return result;
        }

        @Override
        protected LatLng doInBackground(String... params) {
            String name = params[0];
            URL url;
            LatLng pos = null;

            Geocoder geo = new Geocoder(context);

            List<Address> addr = null;
            try {
                addr = geo.getFromLocationName(name, 1);
                pos = new LatLng(addr.get(0).getLatitude(), addr.get(0).getLongitude());
                return pos;
            } catch(Exception e) {
                e.printStackTrace();
            }
            //Toast.makeText(context, "geocode failed", Toast.LENGTH_LONG).show();
            return pos;
        }

        protected String readStreamToString(InputStream in) throws IOException {
            int numRead;
            final int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            ByteArrayOutputStream outString = new ByteArrayOutputStream();

            while((numRead = in.read(buffer)) != -1) {
                outString.write(buffer, 0, numRead);
                if(isCancelled()) {
                    return null;
                }
            }
            return new String(outString.toByteArray(), "UTF-8");
        }

        @Override
        protected void onPostExecute(LatLng result) {
            cb.onLatLng(result);
        }

        @Override
        protected void onCancelled(LatLng result) {

        }

    }

    public MapHolder(Context context, String currentUserEmail, MapsActivity mapsActivity) {
        this.context = context;
        this.mapsActivity = mapsActivity;
    }

    public boolean warnIfNotReady() {
        if(gMap == null) {
            Toast.makeText(context, "map not ready yet", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.setIndoorEnabled(false);
        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//                Toast.makeText(context, "edit a geonote", Toast.LENGTH_LONG).show();
                mapsActivity.editFragment((GeoNote)marker.getTag());
            }
        });
        gMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                mapsActivity.deleteNoteOnMap(((GeoNote)marker.getTag()).getUuid());
            }
        });
    }

    public void showSearchAddress(String address){
        if(warnIfNotReady()) {
            Log.i("warning", "showSearchAddress map is not ready");
            return;
        }

        NameToLatLngTask task = new NameToLatLngTask(context, address, new NameToLatLngTask.OnLatLngCallback() {
            @Override
            public void onLatLng(LatLng latlng) {
                if(latlng != null) {
                    Marker marker = gMap.addMarker(new MarkerOptions().position(latlng));
                    gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, defaultZoom));
                }
                else {
                    Toast.makeText(context, "Invalid location inputted", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean showAddress(String address, String user, String name, String message, FirestoreHolder fH) {
        if(warnIfNotReady()) {
            Log.i("warning", "showAddress map is not ready");
            return false;
        }

        final String userEmail = user;
        final String geoName = name;
        final String geoMessage = message;
        final FirestoreHolder firestoreHolder = fH;

        NameToLatLngTask task = new NameToLatLngTask(context, address, new NameToLatLngTask.OnLatLngCallback() {
            @Override
            public void onLatLng(LatLng latlng) {
                if(latlng != null) {
                    Marker marker = addMarker(latlng);
                    moveCamera(latlng);
                    String id = Integer.toString((geoName + System.currentTimeMillis()).hashCode());
                    GeoNote note = new GeoNote(geoName, geoMessage, new GeoPoint(latlng.latitude, latlng.longitude), id);

                    marker = setMarkerStuff(marker, note);
                    markerList.add(marker);
                    firestoreHolder.addGeonote(userEmail, geoName, geoMessage, latlng, id);
                    GeoNoteHolder.getInstance().addGeoNote(note);
                }
                else {
                    Toast.makeText(context, "Invalid location inputted", Toast.LENGTH_LONG).show();
                }
            }
        });
        return true;
    }

    public Marker addMarker(LatLng latlng) {
        Log.i("marker check", "We are adding a marker at " + latlng.toString());
        return gMap.addMarker(new MarkerOptions().position(latlng));
    }

    public Marker setMarkerStuff(Marker marker, GeoNote note) {
        marker.setTag(note);
        marker.setTitle(note.getName());
        marker.setSnippet(note.getMessage());
        return marker;
    }

    public void addMarkerToList(Marker m) {
        markerList.add(m);
    }

    public void moveCamera(LatLng latlng) {
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, defaultZoom));
    }

    public void clearMap() {
        gMap.clear();
        markerList.clear();
    }

    public void updateMarkerList(String name, String message, String uuid) {
        for(Marker m : markerList) {
            GeoNote g = (GeoNote)m.getTag();
            if(g.getUuid().equals(uuid)) {
                g.setName(name);
                g.setMessage(message);
                m.setTitle(name);
                m.setSnippet(message);
                m.setTag(g);
            }
        }
    }

    public void deleteMarkerFromList(String uuid) {
        ArrayList<Marker> tempList = new ArrayList<>();
        for(Marker m : markerList) {
            if(((GeoNote)(m.getTag())).getUuid().equals(uuid)) {
                m.remove();
            }
            else {
                tempList.add(m);
            }
        }
        markerList = (ArrayList<Marker>)tempList.clone();
    }
}
