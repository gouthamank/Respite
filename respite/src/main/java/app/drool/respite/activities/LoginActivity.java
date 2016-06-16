package app.drool.respite.activities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

        String[] scopes = {"identity", "read"};

        final URL authorizationURL = helper.getAuthorizationUrl(Respite.CREDENTIALS, true, true, scopes);
        final WebView webView = (WebView) findViewById(R.id.webview);

        assert webView != null;
        webView.loadUrl(authorizationURL.toExternalForm());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(url.contains("code=")){
                    onUserChallenge(url, Respite.CREDENTIALS);
                } else if (url.contains("error=")){
                    Toast.makeText(LoginActivity.this, "You must allow to continue", Toast.LENGTH_LONG).show();
                    webView.loadUrl(authorizationURL.toExternalForm());
                }
            }
        });
    }

    private void onUserChallenge(final String url, final Credentials credentials){
        new AsyncTask<String, Void, String>(){
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
                super.onPostExecute(s);
                LoginActivity.this.finish();
            }
        }.execute();
    }
}
