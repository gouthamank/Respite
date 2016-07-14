package app.drool.respite.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;

import java.net.MalformedURLException;
import java.net.URL;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.impl.SubmissionParcelable;

/**
 * Created by drool on 7/14/16.
 */

public class SubmitActivity extends AppCompatActivity {
    private static final String TAG = "SubmitActivity";

    private RedditClient mRedditClient;
    private String subreddit, mode;
    private Button submit, discard;
    private TextInputLayout linkLayout, textLayout, titleLayout;
    private TextInputEditText title, link, text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit);
        getSupportActionBar().hide();
        bindViews();

        mRedditClient = ((Respite) getApplication()).getRedditClient();
        subreddit = getIntent().getStringExtra("subreddit");
        mode = getIntent().getStringExtra("mode");

        setUpViews();

        discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SubmitActivity.this);
                builder.setTitle(getString(R.string.submitactivity_confirm));
                builder.setPositiveButton(getString(R.string.submitactivity_discard), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                builder.create().show();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean errorsDetected = false;

                if (title.getText().toString().length() < 1) {
                    errorsDetected = true;
                    titleLayout.setError(getString(R.string.submitactivity_title_error));
                }

                if (mode.contentEquals("link")) {
                    if (link.getText().toString().length() < 1) {
                        errorsDetected = true;
                        linkLayout.setError(getString(R.string.submitactivity_url_error_missing));
                    }

                    try {
                        URL linkURL = new URL(link.getText().toString());
                    } catch (MalformedURLException e) {
                        errorsDetected = true;
                        linkLayout.setError(getString(R.string.submitactivity_url_error_invalid));
                    }
                }

                if (!errorsDetected) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubmitActivity.this);
                    builder.setTitle(getString(R.string.submitactivity_confirm));
                    builder.setPositiveButton(getString(R.string.submitactivity_submit), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), R.string.submitactivity_submitting, Toast.LENGTH_SHORT).show();
                            AccountManager.SubmissionBuilder submissionBuilder = null;
                            if (mode.contentEquals("link")) {
                                try {
                                    String postTitle = title.getText().toString();
                                    URL postURL = new URL(link.getText().toString());
                                    submissionBuilder = new AccountManager.SubmissionBuilder(postURL, subreddit, postTitle);
                                } catch (MalformedURLException e) {
                                    // will never be so
                                }
                            } else {
                                String postTitle = title.getText().toString();
                                String postBody = text.getText().toString();
                                submissionBuilder = new AccountManager.SubmissionBuilder(postBody, subreddit, postTitle);
                            }

                            submitPost(submissionBuilder);
                        }
                    });

                    builder.create().show();
                }
            }
        });

    }

    private void submitPost(final AccountManager.SubmissionBuilder builder) {
        new AsyncTask<Void, Void, Submission>() {
            @Override
            protected Submission doInBackground(Void... params) {
                AccountManager manager = new AccountManager(mRedditClient);
                try {
                    return manager.submit(builder);
                } catch (ApiException e) {
                    Log.d(TAG, "doInBackground: " + e.getMessage());
                    return null;
                } catch (NetworkException e) {
                    Log.d(TAG, "doInBackground: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), R.string.submitactivity_networkerror, Toast.LENGTH_LONG).show();
                    return new Submission(null);
                }
            }

            @Override
            protected void onPostExecute(Submission submission) {
                if (submission == null) {
                    Toast.makeText(getApplicationContext(), R.string.submitactivity_apierror, Toast.LENGTH_LONG).show();
                    return;
                }

                if (submission.getAuthor() == null) {
                    Toast.makeText(getApplicationContext(), R.string.submitactivity_networkerror, Toast.LENGTH_LONG).show();
                    return;
                }

                SubmissionParcelable top = new SubmissionParcelable(SubmitActivity.this, submission);
                Intent commentsActivity = new Intent(SubmitActivity.this, CommentsActivity.class);
                commentsActivity.putExtra("top", top);
                startActivity(commentsActivity);
                finish();
            }
        }.execute();
    }

    private void bindViews() {
        submit = (Button) findViewById(R.id.activity_submit_submit);
        discard = (Button) findViewById(R.id.activity_submit_discard);

        titleLayout = (TextInputLayout) findViewById(R.id.activity_submit_title_layout);
        linkLayout = (TextInputLayout) findViewById(R.id.activity_submit_url_layout);
        textLayout = (TextInputLayout) findViewById(R.id.activity_submit_text_layout);

        title = (TextInputEditText) findViewById(R.id.activity_submit_title);
        link = (TextInputEditText) findViewById(R.id.activity_submit_url);
        text = (TextInputEditText) findViewById(R.id.activity_submit_text);
    }

    private void setUpViews() {
        if (mode.contentEquals("link")) {
            textLayout.setVisibility(View.GONE);
        } else {
            linkLayout.setVisibility(View.GONE);
        }
    }
}
