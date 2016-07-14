package app.drool.respite.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.text.NumberFormat;
import java.util.List;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.handlers.LinkHandler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView frontPageButton, subredditButton, userButton, allButton;
    private ProgressBar progressBar;
    private LinearLayout mLayout;
    private RedditClient mRedditClient;

    @Override
    protected void onResume() {
        super.onResume();
        if (AuthenticationManager.get().checkAuthState() == AuthenticationState.NONE) {
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        ((Respite) getApplication()).refreshCredentials(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (LinearLayout) findViewById(R.id.activity_main_layout);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progressbar);
        frontPageButton = (TextView) findViewById(R.id.activity_main_frontpage);
        subredditButton = (TextView) findViewById(R.id.activity_main_customsubreddit);
        allButton = (TextView) findViewById(R.id.activity_main_all);
        userButton = (TextView) findViewById(R.id.activity_main_customuser);
        mLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        mRedditClient = ((Respite) getApplication()).getRedditClient();

        if (AuthenticationManager.get().checkAuthState() == AuthenticationState.READY) {
            loadSubscriptions();
        }
        setUpClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_refresh:

                if (AuthenticationManager.get().checkAuthState() != AuthenticationState.NONE) {
                    clearSubscriptions();
                    progressBar.setVisibility(View.VISIBLE);
                    loadSubscriptions();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearSubscriptions() {
        int baseChildren = 9 - 1; // start from 0
        int totalChildren = mLayout.getChildCount() - 1; // *

        for(; totalChildren > baseChildren ; totalChildren--) {
            mLayout.removeViewAt(totalChildren);
        }
    }

    private void loadSubscriptions() {
        new AsyncTask<Void, Void, List<Subreddit>>() {
            @Override
            protected List<Subreddit> doInBackground(Void... params) {
                try {
                    return (new UserSubredditsPaginator(mRedditClient, "subscriber")).accumulateMergedAll();
                } catch (NetworkException e) {
                    Log.d(TAG, "doInBackground: " + e.getMessage());
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Subreddit> subreddits) {
                if (subreddits == null)
                    Toast.makeText(getApplicationContext(), R.string.mainactivity_networkerror, Toast.LENGTH_LONG).show();
                else {
                    progressBar.setVisibility(View.GONE);
                    boolean shouldSkipDivider = true;
                    for (Subreddit s : subreddits) {
                        RelativeLayout v = (RelativeLayout) getLayoutInflater().inflate(R.layout.list_item_subscription, null, false);
                        final TextView title = (TextView) v.findViewById(R.id.list_item_subscription_title);
                        final TextView count = (TextView) v.findViewById(R.id.list_item_subscription_count);
                        LinearLayout innerLayout = (LinearLayout) v.findViewById(R.id.list_item_subscription_layout);
                        title.setText(s.getDisplayName());
                        if(s.isNsfw())
                            title.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.textTitleNSFW));
                        count.setText(getString(R.string.mainactivity_subscribers, NumberFormat.getIntegerInstance().format(s.getSubscriberCount())));
                        innerLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LinkHandler.analyse(MainActivity.this, "/r/" + title.getText().toString().toLowerCase());
                            }
                        });

                        if (shouldSkipDivider)
                            shouldSkipDivider = false;
                        else {
                            RelativeLayout divider = (RelativeLayout) getLayoutInflater().inflate(R.layout.list_item_divider, null, false);
                            mLayout.addView(divider);
                        }

                        mLayout.addView(v);
                    }
                }
            }
        }.execute();
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
                final AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (input.getText().toString().length() < 1 || input.getText().toString().contains(" "))
                            layout.setError(getString(R.string.dialog_customsubreddit_retry));
                        else {
                            Intent subredditIntent = new Intent(MainActivity.this, SubmissionsActivity.class);
                            subredditIntent.putExtra("subreddit", input.getText().toString());
                            dialog.dismiss();
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

                final AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (input.getText().toString().length() < 1 || input.getText().toString().contains(" "))
                            layout.setError(getString(R.string.dialog_customuser_retry));
                        else {
                            Intent userIntent = new Intent(MainActivity.this, UserActivity.class);
                            userIntent.putExtra("username", input.getText().toString());
                            dialog.dismiss();
                            startActivity(userIntent);
                        }
                    }
                });
            }
        });
    }
}
