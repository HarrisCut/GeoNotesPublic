package cs371m.harris.geonotes;

import android.location.Location;

import java.util.Date;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class GeoNote {
    private String name;
    private String message;
    private double triggerRadius = 200.0;
    private String uuid;
    //time
//    private String share;
    private GeoPoint location;

    public GeoNote(String name, String message, GeoPoint location, String uuid) {
        this.name = name;
        this.message = message;
        this.location = location;
        this.uuid = uuid;
    }

    public LatLng getLocationAsLatLng() {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public Location getLocationAsLocation() {
        Location l = new Location("temporary");
        l.setLatitude(location.getLatitude());
        l.setLongitude(location.getLongitude());
        return l;
    }

    public String getName() {
        return name;
    }

    public double getTriggerRadius() {
        return triggerRadius;
    }

    public String getMessage() {
        return message;
    }

    public String getUuid(){return uuid;}

    public void setName(String name){this.name = name;}

    public void setMessage(String message){this.message = message;}

}
