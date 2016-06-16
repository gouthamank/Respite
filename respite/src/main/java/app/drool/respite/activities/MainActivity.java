package app.drool.respite.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;

import app.drool.respite.R;
import app.drool.respite.Respite;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button loginButton;
    private Button viewButton;
    private Button frontPageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button) findViewById(R.id.button1);
        viewButton = (Button) findViewById(R.id.button2);
        frontPageButton = (Button) findViewById(R.id.button3);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserInfoActivity.class));
            }
        });

        frontPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubmissionsActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);

        checkIsLoggedIn();
    }

    private void checkIsLoggedIn(){
        if(AuthenticationManager.get().checkAuthState() == AuthenticationState.NONE){
            loginButton.setEnabled(true);
            viewButton.setEnabled(false);
            frontPageButton.setEnabled(false);
        } else {
            loginButton.setEnabled(false);
            viewButton.setEnabled(true);
            frontPageButton.setEnabled(true);
        }
    }

}
