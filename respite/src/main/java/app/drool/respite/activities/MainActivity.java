package app.drool.respite.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.handlers.LinkHandler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button loginButton, viewButton, frontPageButton, subredditButton, userButton;

    private Button lhTest1, lhTest2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (Button) findViewById(R.id.button1);
        viewButton = (Button) findViewById(R.id.button2);
        frontPageButton = (Button) findViewById(R.id.button3);
        subredditButton = (Button) findViewById(R.id.button4);
        userButton = (Button) findViewById(R.id.button5);

        lhTest1 = (Button) findViewById(R.id.button6);
        lhTest2 = (Button) findViewById(R.id.button7);

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

        subredditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View inflatedView = getLayoutInflater().inflate(R.layout.dialog_customsubreddit, null);
                final EditText input = (EditText) inflatedView.findViewById(R.id.dialog_customsubreddit_input);
                builder.setTitle(R.string.dialog_customsubreddit_title);
                builder.setView(inflatedView);

                builder.setNegativeButton(R.string.dialog_customsubreddit_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setPositiveButton(R.string.dialog_customsubreddit_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().length() < 1 || input.getText().toString().contains(" "))
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.dialog_customsubreddit_retry), Toast.LENGTH_SHORT).show();
                        else {
                            Intent subredditIntent = new Intent(MainActivity.this, SubmissionsActivity.class);
                            subredditIntent.putExtra("subreddit", input.getText().toString());
                            startActivity(subredditIntent);
                        }
                    }
                });
                builder.create().show();
            }
        });

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View inflatedView = getLayoutInflater().inflate(R.layout.dialog_customuser, null);
                final EditText input = (EditText) inflatedView.findViewById(R.id.dialog_customuser_input);
                builder.setTitle(R.string.dialog_customuser_title);
                builder.setView(inflatedView);

                builder.setNegativeButton(R.string.dialog_customuser_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.setPositiveButton(R.string.dialog_customuser_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().length() < 1 || input.getText().toString().contains(" "))
                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.dialog_customuser_retry), Toast.LENGTH_SHORT).show();
                        else {
                            Intent userIntent = new Intent(MainActivity.this, UserActivity.class);
                            userIntent.putExtra("username", input.getText().toString());
                            startActivity(userIntent);
                        }
                    }
                });
                builder.create().show();
            }
        });

        lhTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkHandler.analyse(MainActivity.this, "https://www.reddit.com/r/unixporn/comments/37csyp/osx_heres_my_custom_iterm2_without_borders_and/");
            }
        });

        lhTest2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkHandler.analyse(MainActivity.this, "https://www.reddit.com/r/unixporn/comments/37csyp/osx_heres_my_custom_iterm2_without_borders_and/crm2p4m");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);
        checkIsLoggedIn();
    }

    private void checkIsLoggedIn() {
        if (AuthenticationManager.get().checkAuthState() == AuthenticationState.NONE) {
            loginButton.setEnabled(true);
            loginButton.setText(getResources().getString(R.string.mainactivity_login));
            viewButton.setEnabled(false);
            frontPageButton.setEnabled(false);
            subredditButton.setEnabled(false);
            userButton.setEnabled(false);
            lhTest1.setEnabled(false);
            lhTest2.setEnabled(false);
        } else {
            loginButton.setEnabled(false);
            loginButton.setText(getResources().getString(R.string.mainactivity_loggedin));
            viewButton.setEnabled(true);
            frontPageButton.setEnabled(true);
            subredditButton.setEnabled(true);
            userButton.setEnabled(true);
            lhTest1.setEnabled(true);
            lhTest2.setEnabled(true);
        }
    }
}
