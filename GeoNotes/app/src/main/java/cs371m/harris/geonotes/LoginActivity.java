package cs371m.harris.geonotes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends AppCompatActivity{

    private static final int RC_SIGN_IN = 1;
    FirestoreHolder fcHolder = new FirestoreHolder();
    private int signOutFlag = 0;

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private FirebaseAuth mAuth;

    final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent data = getIntent();
        Log.i("In login activity", "Just reached login activity");
        if(data != null) {
            signOutFlag = data.getIntExtra("signOutFlag", 0);
            Log.i("Signoutflag", "Sign out flag is: " + signOutFlag);
        }

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(signOutFlag == 1) {
            mAuth.signOut();
            currentUser = null;
        }
        if(currentUser == null) {
            attemptLogin();
        }
        else {
//            mAuth.signOut();
//            attemptLogin();
//            currentUser = mAuth.getCurrentUser();
//            Log.i("checking user", "The user: " + currentUser.toString());
            Intent returnToMain = new Intent();
            returnToMain.putExtra("user", currentUser.getEmail());
            setResult(RESULT_OK, returnToMain);
            finish();
        }
    }

    private void attemptLogin() {
        if(mAuth == null) {
            return;
        }

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                fcHolder.addUser(currentUser.getEmail());
                Intent returnToMain = new Intent();
                returnToMain.putExtra("user", currentUser.getEmail());
                setResult(RESULT_OK, returnToMain);
                finish();
            }
        }
    }
}
