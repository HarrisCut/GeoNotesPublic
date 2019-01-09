package cs371m.harris.geonotes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    private static int LOGIN_REQUEST = 1;
    private static int MAPS_REQUEST = 2;
    static String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, LOGIN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST) {
            if(resultCode == RESULT_OK) {
                user = data.getStringExtra("user");
                Log.i("user", "The current user is " + user);
                Intent mapIntent = new Intent(this, MapsActivity.class);
                mapIntent.putExtra("user", user);
                startActivityForResult(mapIntent, MAPS_REQUEST);
            }
            else {
                Toast.makeText(this, "login result failed", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == MAPS_REQUEST) {
            if(resultCode == RESULT_OK) {
                Log.i("Back in main", "Back in main after maps finished");
                Intent signOutIntent = new Intent(this, LoginActivity.class);
                signOutIntent.putExtra("signOutFlag", 1);
                startActivityForResult(signOutIntent, LOGIN_REQUEST);
            }
        }
    }
}
