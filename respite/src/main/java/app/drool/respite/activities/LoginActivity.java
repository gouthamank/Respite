package app.drool.respite.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.net.URL;

import app.drool.respite.R;
import app.drool.respite.Respite;

/**
 * Created by drool on 6/15/16.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final OAuthHelper helper = AuthenticationManager.get().getRedditClient().getOAuthHelper();

        final URL authorizationURL = helper.getAuthorizationUrl(Respite.CREDENTIALS, true, true, Respite.scopes);
        final WebView webView = (WebView) findViewById(R.id.webview);

        assert webView != null;
        webView.loadUrl(authorizationURL.toExternalForm());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    onUserChallenge(url, Respite.CREDENTIALS);
                } else if (url.contains("error=")) {
                    Toast.makeText(LoginActivity.this, R.string.loginactivity_failure_userdeclined, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private void onUserChallenge(final String url, final Credentials credentials) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    OAuthData data = AuthenticationManager.get().getRedditClient().getOAuthHelper().onUserChallenge(url, credentials);
                    AuthenticationManager.get().getRedditClient().authenticate(data);
                    return AuthenticationManager.get().getRedditClient().getAuthenticatedUser();
                } catch (OAuthException | NetworkException e) {
                    Log.e(TAG, "doInBackground: Could not log in", e);
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (s == null) {
                    Toast.makeText(getApplicationContext(), R.string.loginactivity_failure_token, Toast.LENGTH_LONG).show();
                    finish();
                }
                getSharedPreferences("Respite.users", Context.MODE_PRIVATE).edit().putBoolean("loggedIn", true).apply();
                super.onPostExecute(s);
                finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        }.execute();
    }
}
