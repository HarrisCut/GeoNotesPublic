package cs371m.harris.geonotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHolder {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addUser(String email) {
        Map<String, String> user = new HashMap<>();

        db.collection("Users")
                .document(email)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("add user success", "Successfully added user to firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("add user failure", "Failed to add user to firestore");
                    }
                });
    }

    public void addGeonote(String user, String geoName, String message, LatLng location, String id) {
        Map<String, Object> geonote = new HashMap<>();
        geonote.put("Message", message);
        geonote.put("Location", new GeoPoint(location.latitude, location.longitude));
        geonote.put("Name", geoName);
        db.collection("Users")
                .document(user)
                .collection("Geonotes")
                .document(id)
                .set(geonote)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("add geonote success", "Successfully added geonote to firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("add geonote failure", "Failed to add geonote to firestore");
                    }
                });
    }

    public void editGeoNote(String user, String geoName, String message, String uuid) {
        db.collection("Users")
                .document(user)
                .collection("Geonotes")
                .document(uuid)
                .update("Name", geoName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("update name success", "Successfully updated geonote name in firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("update name failure", "Failed to updated geonote name in firestore");
                    }
                });
        db.collection("Users")
                .document(user)
                .collection("Geonotes")
                .document(uuid)
                .update("Message", message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("update message success", "Successfully updated geonote message in firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("update message failure", "Failed to updated geonote message in firestore");
                    }
                });
    }

    public void deleteGeoNote(String user, String uuid) {
        db.collection("Users")
                .document(user)
                .collection("Geonotes")
                .document(uuid)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("delete geonote success", "Successfully deleted geonote in firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("delete geonote failure", "Failed to delete geonote in firestore");
                    }
                });
    }
}
