package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.LoggedInAccount;

import app.drool.respite.R;

/**
 * Created by drool on 6/15/16.
 */

public class UserInfoActivity extends AppCompatActivity {

    static final String TAG = "UserInfoActivity.java";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        new AsyncTask<Void, Void, LoggedInAccount>() {
            @Override
            protected LoggedInAccount doInBackground(Void... params) {
                RedditClient mRedditClient = AuthenticationManager.get().getRedditClient();
                return mRedditClient.me();
            }

            @Override
            protected void onPostExecute(LoggedInAccount loggedInAccount) {
                ((TextView) findViewById(R.id.user_name)).setText("Name: " + loggedInAccount.getFullName());
                ((TextView) findViewById(R.id.user_created)).setText("Created: " + loggedInAccount.getCreated().toString());
                ((TextView) findViewById(R.id.user_link_karma)).setText("Link karma: " + loggedInAccount.getLinkKarma());
                ((TextView) findViewById(R.id.user_comment_karma)).setText("Comment karma: " + loggedInAccount.getCommentKarma());
                ((TextView) findViewById(R.id.user_has_mail)).setText("Has mail? " + (loggedInAccount.getInboxCount() > 0));
                ((TextView) findViewById(R.id.user_inbox_count)).setText("Inbox count: " + loggedInAccount.getInboxCount());
                ((TextView) findViewById(R.id.user_is_mod)).setText("Is mod? " + loggedInAccount.isMod());
            }
        }.execute();
    }
}
