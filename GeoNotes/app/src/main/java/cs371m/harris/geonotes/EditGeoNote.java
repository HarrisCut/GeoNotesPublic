package cs371m.harris.geonotes;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditGeoNote.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditGeoNote#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditGeoNote extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NAME = "name";
    private static final String ARG_LOCATION = "location";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_USER = "user";
    private static final String ARG_UUID = "uuid";

    Context context;

    private EditText locationEdit;
    private EditText nameEdit;
    private EditText messageEdit;
    private Button saveNoteButton;
    private Button cancelButton;
    private Button deleteButton;

    private String currentUserEmail;

    private String name;
    private String location;
    private String message;
    private String uuid;

    private OnFragmentInteractionListener mListener;

    public EditGeoNote() {
        // Required empty public constructor
    }

    public static EditGeoNote newInstance(String paramName, String paramLocation, String paramMessage) {
        EditGeoNote fragment = new EditGeoNote();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, paramName);
        args.putString(ARG_LOCATION, paramLocation);
        args.putString(ARG_MESSAGE, paramMessage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
            location = getArguments().getString(ARG_LOCATION);
            message = getArguments().getString(ARG_MESSAGE);
            currentUserEmail = getArguments().getString(ARG_USER);
            uuid = getArguments().getString(ARG_UUID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_edit_geo_note, container, false);
        nameEdit = view.findViewById(R.id.name_edit);
        messageEdit = view.findViewById(R.id.geonote_body);
        saveNoteButton = view.findViewById(R.id.save_geonote);
        cancelButton = view.findViewById(R.id.cancel_geonote);
        deleteButton = view.findViewById(R.id.delete_geonote);
        nameEdit.setText(name);
        messageEdit.setText(message);

        saveNoteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                message = messageEdit.getText().toString();
                name = nameEdit.getText().toString();

                //Intent returnMapIntent;
                mListener.onReturnToMap();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                mListener.editNoteOnMap(location, name, message, uuid);
                fragmentManager.popBackStack();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameEdit.setText("");
                messageEdit.setText("");

                mListener.onReturnToMap();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onReturnToMap();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                mListener.deleteNoteOnMap(uuid);
                fragmentManager.popBackStack();
            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);

        void onReturnToMap();
        void editNoteOnMap(String address, String name, String message, String uuid);
        void deleteNoteOnMap(String uuid);
    }

    public void setOnFragmentInteractionListener(Activity activity) {
        mListener = (EditGeoNote.OnFragmentInteractionListener)activity;
    }
}
