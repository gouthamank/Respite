package app.drool.respite.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;

import app.drool.respite.R;
import app.drool.respite.Respite;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView frontPageButton, subredditButton, userButton, allButton;

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);

        if(AuthenticationManager.get().checkAuthState() == AuthenticationState.NONE) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frontPageButton = (TextView) findViewById(R.id.activity_main_frontpage);
        subredditButton = (TextView) findViewById(R.id.activity_main_customsubreddit);
        allButton = (TextView) findViewById(R.id.activity_main_all);
        userButton = (TextView) findViewById(R.id.activity_main_customuser);

        setUpClickListeners();
    }

    private void setUpClickListeners() {
        frontPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubmissionsActivity.class));
            }
        });
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allIntent = new Intent(MainActivity.this, SubmissionsActivity.class);
                allIntent.putExtra("subreddit", "all");
                startActivity(allIntent);
            }
        });
        subredditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View inflatedView = getLayoutInflater().inflate(R.layout.dialog_customsubreddit, null);
                final TextInputEditText input = (TextInputEditText) inflatedView.findViewById(R.id.dialog_customsubreddit_input);
                final TextInputLayout layout = (TextInputLayout) inflatedView.findViewById(R.id.dialog_customsubreddit_input_layout);
                builder.setView(inflatedView);

                builder.setNegativeButton(R.string.dialog_customsubreddit_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(R.string.dialog_customsubreddit_positive, null);
                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (input.getText().toString().length() < 1 || input.getText().toString().contains(" "))
                            layout.setError(getString(R.string.dialog_customsubreddit_retry));
                        else {
                            Intent subredditIntent = new Intent(MainActivity.this, SubmissionsActivity.class);
                            subredditIntent.putExtra("subreddit", input.getText().toString());
                            startActivity(subredditIntent);
                        }
                    }
                });
            }
        });

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View inflatedView = getLayoutInflater().inflate(R.layout.dialog_customuser, null);
                final TextInputEditText input = (TextInputEditText) inflatedView.findViewById(R.id.dialog_customuser_input);
                final TextInputLayout layout = (TextInputLayout) inflatedView.findViewById(R.id.dialog_customuser_input_layout);
                builder.setView(inflatedView);

                builder.setNegativeButton(R.string.dialog_customuser_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton(R.string.dialog_customuser_positive, null);

                AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (input.getText().toString().length() < 1 || input.getText().toString().contains(" "))
                            layout.setError(getString(R.string.dialog_customuser_retry));
                        else {
                            Intent userIntent = new Intent(MainActivity.this, UserActivity.class);
                            userIntent.putExtra("username", input.getText().toString());
                            startActivity(userIntent);
                        }
                    }
                });
            }
        });
    }
}
