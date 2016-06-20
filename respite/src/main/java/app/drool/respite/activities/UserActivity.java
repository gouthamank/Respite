package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Account;

import app.drool.respite.R;
import app.drool.respite.Respite;

/**
 * Created by drool on 6/20/16.
 */

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity.java";

    private RedditClient mRedditClient = null;
    private final String endpointComments = "comments";
    private final String endpointSubmitted = "submitted";
    private final String endpointOverview = "overview";
    private String username = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mRedditClient == null) {
            mRedditClient = ((Respite) getApplication()).getRedditClient();
            username = getIntent().getExtras().getString("username");
        }

        setUpMenuBar();
        loadUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);
    }

    private void setUpMenuBar() {
        getSupportActionBar().setTitle(getString(R.string.actionbar_title_user, username));
    }

    private void loadUser() {
        new AsyncTask<Void, Void, Account>(){
            @Override
            protected Account doInBackground(Void... params) {
                try {
                    return mRedditClient.getUser(username);
                } catch (NetworkException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Account account) {
                if(account != null)
                    Log.d(TAG, "onPostExecute: " + account.toString());
                else
                    Toast.makeText(UserActivity.this, R.string.useractivity_networkerror, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }
}
