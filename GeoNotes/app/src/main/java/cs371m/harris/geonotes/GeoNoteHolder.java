package cs371m.harris.geonotes;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeoNoteHolder {
    private ArrayList<GeoNote> geoNoteList = new ArrayList<>();
    private static GeoNoteHolder INSTANCE = null;

    private GeoNoteHolder(){}

    public static GeoNoteHolder getInstance()
    {
        if(INSTANCE == null){
            INSTANCE = new GeoNoteHolder();
        }
        return INSTANCE;
    }

    protected ArrayList<GeoNote> getGeoNoteList() {
        return geoNoteList;
    }

    protected void addGeoNote(GeoNote newNote) {
        Log.i("adding geonote to local copy", "new geonote added to holder at " + newNote.toString());
        geoNoteList.add(newNote);
    }

    protected void clearGeoList() {
        geoNoteList.clear();
    }

    protected void updateList(String name, String message, String uuid) {
        for(GeoNote g : geoNoteList){
            if(g.getUuid().equals(uuid)) {
                g.setName(name);
                g.setMessage(message);
            }
        }
    }

    protected void deleteFromList(String uuid) {
        ArrayList<GeoNote> tempList = new ArrayList<>();
        for(GeoNote g : geoNoteList){
            if(!g.getUuid().equals(uuid)) {
                tempList.add(g);
            }
        }
        geoNoteList = (ArrayList<GeoNote>)tempList.clone();
    }

}
