package app.drool.respite;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.paginators.SubredditPaginator;

/**
 * Created by drool on 6/15/16.
 */

public class Respite extends Application {
    public static final Credentials CREDENTIALS = Credentials.installedApp("hWSUKXYvIc1shQ", "https://github.com/gouthamank/respite");
    private static final UserAgent USERAGENT = UserAgent.of("android", "app.drool.respite", "v0.1", "_drool");
    private static final String sharedPreferencesName = "app.drool.respite.sharedprefs";
    private static final String TAG = "Respite.java";
    private RedditClient mRedditClient = null;
    private SubredditPaginator subredditPaginator = null;
    @Override
    public void onCreate() {
        super.onCreate();
        mRedditClient = new RedditClient(USERAGENT);
        mRedditClient.setLoggingMode(LoggingMode.ALWAYS);

        final SharedPreferences sharedPreferences = getApplicationContext()
                                                .getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);

        TokenStore tokenStore = new TokenStore() {
            @Override
            public boolean isStored(String key) {
                return sharedPreferences.contains(key);
            }

            @Override
            public String readToken(String key) throws NoSuchTokenException {
                if(isStored(key))
                    return sharedPreferences.getString(key, null);
                else
                    throw new NoSuchTokenException("Token " + key + " not found in store");
            }

            @Override
            public void writeToken(String key, String token) {
                sharedPreferences.edit().putString(key, token).apply();
            }
        };

        RefreshTokenHandler tokenHandler = new RefreshTokenHandler(tokenStore, mRedditClient);
        AuthenticationManager.get().init(mRedditClient, tokenHandler);

        subredditPaginator = new SubredditPaginator(mRedditClient);
    }


    public void refreshCredentials(Context mcContext){
        AuthenticationState state = AuthenticationManager.get().checkAuthState();

        switch(state) {
            case READY:
                break;
            case NONE:
                Toast.makeText(mcContext, "Need to log in first", Toast.LENGTH_SHORT).show();
                break;
            case NEED_REFRESH:
                new AsyncTask<Credentials, Void, Void>() {
                    @Override
                    protected Void doInBackground(Credentials... params) {
                        try {
                            AuthenticationManager.get().refreshAccessToken(CREDENTIALS);
                        } catch (NoSuchTokenException | OAuthException e) {
                            Log.e(TAG, "doInBackground: Could not refresh access token", e);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        Log.d(TAG, "onPostExecute: Reauthenticated");
                    }
                }.execute();
                break;
        }
    }

    public SubredditPaginator getPaginator() { return subredditPaginator; }

    public RedditClient getRedditClient() { return mRedditClient; }
}
