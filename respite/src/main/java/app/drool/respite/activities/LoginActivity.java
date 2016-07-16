package app.drool.respite.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
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

    public static void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
            cookieSyncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncManager.stopSync();
            cookieSyncManager.sync();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(R.layout.dialog_login);
        builder.setPositiveButton(R.string.dialog_loginactivity_login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doLogin();
            }
        });

        builder.setNeutralButton(R.string.dialog_loginactivity_signup, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doSignup();
            }
        });

        builder.setNegativeButton(R.string.dialog_loginactivity_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setCancelable(false);
        builder.create().show();

    }

    private void doLogin() {
        final OAuthHelper helper = AuthenticationManager.get().getRedditClient().getOAuthHelper();

        final URL authorizationURL = helper.getAuthorizationUrl(Respite.CREDENTIALS, true, true, Respite.scopes);
        final WebView webView = (WebView) findViewById(R.id.webview);

        webView.clearCache(true);
        assert webView != null;
        clearCookies(LoginActivity.this);
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

    private void doSignup() {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setData(Uri.parse("https://www.reddit.com/register"));
        finish();
        startActivity(viewIntent);
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
                getSharedPreferences("Respite.users", Context.MODE_PRIVATE).edit().putBoolean("loggedIn", true)
                        .putString("username", s).apply();
                super.onPostExecute(s);
                finish();
                startActivity(new Intent(LoginActivity.this, FrontPageActivity.class));
            }
        }.execute();
    }
}
