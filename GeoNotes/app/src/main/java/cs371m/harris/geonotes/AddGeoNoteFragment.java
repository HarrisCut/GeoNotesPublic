package cs371m.harris.geonotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class AddGeoNoteFragment extends Fragment {

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);

        void onReturnToMap();
        void addNoteToMap(String address, String name, String message);
    }

    public void setOnFragmentInteractionListener(Activity activity) {
        listener = (OnFragmentInteractionListener)activity;
    }

    private final static String ARG_LOCATION = "location";
    private final static String ARG_SHARE = "share";
    private final static String ARG_MESSAGE = "message";
    private final static String ARG_USER = "user";

    Context context;

    private EditText locationEdit;
    private EditText nameEdit;
    private EditText messageEdit;
    private Button saveNoteButton;
    private Button discardNoteButton;

    private String location;
    private String share;
    private String message;
    private String currentUser;

//    private OnFragmentInteractionListener mListener;

    private OnFragmentInteractionListener listener;


    public AddGeoNoteFragment() {
        // Required empty public constructor
    }

    public static AddGeoNoteFragment newInstance(String paramLocation, String paramShare, String paramMessage) {
        AddGeoNoteFragment fragment = new AddGeoNoteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOCATION, paramLocation);
        args.putString(ARG_SHARE, paramShare);
        args.putString(ARG_MESSAGE, paramMessage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = getArguments().getString("user");
            Log.i("user", "the current user is " + currentUser);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.add_geonote_layout, container, false);
        locationEdit = view.findViewById(R.id.location_edit);
        nameEdit = view.findViewById(R.id.name_edit);
        messageEdit = view.findViewById(R.id.geonote_body);
        saveNoteButton = view.findViewById(R.id.save_geonote);
        discardNoteButton = view.findViewById(R.id.discard_geonote);

        saveNoteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                location = locationEdit.getText().toString();
                share = nameEdit.getText().toString();
                message = messageEdit.getText().toString();

                if(!addGeoNote(location, share, message)){
                    Toast.makeText(getContext(), "Failed to add GeoNote", Toast.LENGTH_LONG).show();
                }

                //Intent returnMapIntent;
                listener.onReturnToMap();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                listener.addNoteToMap(location, share, message);
                fragmentManager.popBackStack();


            }
        });

        discardNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationEdit.setText("");
                nameEdit.setText("");
                messageEdit.setText("");

                listener.onReturnToMap();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        return view;
    }

    public boolean addGeoNote(String location, String share, String message) {

        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.i("detach", "In onDetach() of AddGeoNoteFragment");
        super.onDetach();
        listener.onReturnToMap();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
        listener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Log.i("menu button selected", "a menu button was selected");
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i("back button", "pressed back menu button from add note fragment");
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();

        }
        return true;
    }

}
