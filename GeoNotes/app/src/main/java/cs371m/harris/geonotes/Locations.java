package cs371m.harris.geonotes;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Locations {
    Context context;
    private static String CHANNEL_ID = "high";
    private static String GROUP_GEONOTES = "0aB8xoyh4N12";
    private static int SUMMARY_ID = 30;
    private String userEmail;
    // private ArrayList<Notification> notificationList = new ArrayList<>();

    private MapHolder mapHolder;

    private LocationManager locationManager;
    private LocationListener locationListener;

    public Locations(Context context, String userEmail, MapHolder mapHolder) {
        this.context = context;
        this.userEmail = userEmail;
        this.mapHolder = mapHolder;
    }

    private void createNotification(String name, String message) {
        Intent intent = new Intent(context, MapHolder.class);
        int requestCode = (name + System.currentTimeMillis()).hashCode();
        intent.putExtra("randomRequestCode", requestCode);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.login_background)
                .setContentTitle(name)
                .setContentText("You have arrived at GeoNote: " + name)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
//                .setGroup(GROUP_GEONOTES)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(requestCode, notification);
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "test";
            String description = "making sure this works";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static boolean checkLocationPermission(Activity activity) {
        if(ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        }
        return true;
    }

    public void initLocation() {
        createNotificationChannel();

        locationManager =(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.i("location", "located at " + latitude + ", " + longitude);
                //
                // call method to check location against saved locations
                checkProximity(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        if(checkLocationPermission((Activity)context)) {
            Log.i("loation", "permission granted");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, locationListener);
        }
    }

    public Location getCurrentLocation() {
        if(checkLocationPermission((Activity)context)) {
//          Toast.makeText(context, "getting location", Toast.LENGTH_LONG).show();
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        }
        Toast.makeText(context, "failed permissions check", Toast.LENGTH_LONG).show();
        return null;
    }

    private void checkProximity(Location currentLocation) {
        for(GeoNote note: GeoNoteHolder.getInstance().getGeoNoteList()) {
            if(currentLocation.distanceTo(note.getLocationAsLocation()) <= note.getTriggerRadius()) {
                createNotification(note.getName(), note.getMessage());
            }
        }
    }
}
