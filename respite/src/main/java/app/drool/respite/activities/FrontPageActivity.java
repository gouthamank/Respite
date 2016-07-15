package app.drool.respite.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.List;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.SubmissionListAdapter;
import app.drool.respite.handlers.LinkHandler;
import app.drool.respite.impl.EndlessRecyclerViewScrollListener;
import app.drool.respite.utils.Utilities;

public class FrontPageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FrontPageActivity";

    private SubmissionListAdapter mAdapter = null;
    private SwipeRefreshLayout listContainer = null;
    private ProgressBar progressBar = null;
    private SubredditPaginator paginator = null;
    private RedditClient mRedditClient;
    private NavigationView navigationView;
    private AlertDialog connectDialog = null;
    private SharedPreferences preferences = null;
    @Override
    protected void onResume() {
        super.onResume();
        if (Utilities.isNetworkAvailable(FrontPageActivity.this))
            ((Respite) getApplication()).refreshCredentials(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mRedditClient = ((Respite) getApplication()).getRedditClient();

        preferences = getSharedPreferences("Respite.users", Context.MODE_PRIVATE);
        final RecyclerView submissionList = (RecyclerView) findViewById(R.id.activity_frontpage_list);
        listContainer = (SwipeRefreshLayout) findViewById(R.id.activity_frontpage_list_container);
        progressBar = (ProgressBar) findViewById(R.id.activity_frontpage_progressbar);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        assert submissionList != null;
        submissionList.setLayoutManager(mLayoutManager);
        mAdapter = new SubmissionListAdapter(this, ((Respite) getApplication()).getRedditClient());
        submissionList.setAdapter(mAdapter);
        submissionList.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                loadNextPage();
            }
        });
        listContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPage(false);
            }
        });
        listContainer.setColorSchemeColors(ContextCompat.getColor(FrontPageActivity.this, R.color.colorAccent),
                ContextCompat.getColor(FrontPageActivity.this, R.color.colorPrimary));
        setUpPaginator(null);

        setUpMenuBar();
        showConnectingDialog();
        if (!preferences.getBoolean("loggedIn", false)) {
            finish();
            startActivity(new Intent(FrontPageActivity.this, LoginActivity.class));
        } else
            connectToReddit();

        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_username))
                .setText(getString(R.string.frontpageactivity_loggedin, preferences.getString("username", null)));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_submissions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_submissions_sort_hot:
                updatePaginator(Sorting.HOT);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_rising:
                updatePaginator(Sorting.RISING);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_new:
                updatePaginator(Sorting.NEW);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_controversial_time_all:
                updatePaginator(Sorting.CONTROVERSIAL, TimePeriod.ALL);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_controversial_time_day:
                updatePaginator(Sorting.CONTROVERSIAL, TimePeriod.DAY);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_controversial_time_hour:
                updatePaginator(Sorting.CONTROVERSIAL, TimePeriod.HOUR);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_controversial_time_month:
                updatePaginator(Sorting.CONTROVERSIAL, TimePeriod.MONTH);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_controversial_time_week:
                updatePaginator(Sorting.CONTROVERSIAL, TimePeriod.WEEK);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_controversial_time_year:
                updatePaginator(Sorting.CONTROVERSIAL, TimePeriod.YEAR);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_top_time_all:
                updatePaginator(Sorting.TOP, TimePeriod.ALL);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_top_time_day:
                updatePaginator(Sorting.TOP, TimePeriod.DAY);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_top_time_hour:
                updatePaginator(Sorting.TOP, TimePeriod.HOUR);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_top_time_month:
                updatePaginator(Sorting.TOP, TimePeriod.MONTH);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_top_time_week:
                updatePaginator(Sorting.TOP, TimePeriod.WEEK);
                refreshPage();
                return true;

            case R.id.menu_submissions_sort_top_time_year:
                updatePaginator(Sorting.TOP, TimePeriod.YEAR);
                refreshPage();
                return true;

            case R.id.menu_submissions_refresh:
                refreshPage();
                return true;

            case R.id.menu_submissions_search:
                openSearchDialog();
                return true;

            case R.id.menu_submissions_submit_link:
                Intent submitLinkIntent = new Intent(FrontPageActivity.this, SubmitActivity.class);
                submitLinkIntent.putExtra("subreddit", paginator.getSubreddit());
                submitLinkIntent.putExtra("mode", "link");
                startActivity(submitLinkIntent);
                return true;

            case R.id.menu_submissions_submit_self:
                Intent submitSelfIntent = new Intent(FrontPageActivity.this, SubmitActivity.class);
                submitSelfIntent.putExtra("subreddit", paginator.getSubreddit());
                submitSelfIntent.putExtra("mode", "self");
                startActivity(submitSelfIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int itemID = item.getItemId();
        if (itemID == R.id.nav_all) {

            Intent allIntent = new Intent(FrontPageActivity.this, SubmissionsActivity.class);
            allIntent.putExtra("subreddit", "all");
            startActivity(allIntent);

        } else if (itemID == R.id.nav_subreddit) {

            AlertDialog.Builder builder = new AlertDialog.Builder(FrontPageActivity.this);
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
                        Intent subredditIntent = new Intent(FrontPageActivity.this, SubmissionsActivity.class);
                        subredditIntent.putExtra("subreddit", input.getText().toString());
                        dialog.dismiss();
                        startActivity(subredditIntent);
                    }
                }
            });

        } else if (itemID == R.id.nav_user) {

            AlertDialog.Builder builder = new AlertDialog.Builder(FrontPageActivity.this);
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
                        Intent userIntent = new Intent(FrontPageActivity.this, UserActivity.class);
                        userIntent.putExtra("username", input.getText().toString());
                        dialog.dismiss();
                        startActivity(userIntent);
                    }
                }
            });

        } else {
            LinkHandler.analyse(FrontPageActivity.this, "/r/" + item.getTitle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showConnectingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FrontPageActivity.this);
        builder.setView(R.layout.dialog_connecting);
        builder.setCancelable(false);
        connectDialog = builder.create();
        connectDialog.show();
    }

    private void connectToReddit() {
        if (Utilities.isNetworkAvailable(FrontPageActivity.this)) {
            ((Respite) getApplication()).refreshCredentials(this);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    while (AuthenticationManager.get().checkAuthState() != AuthenticationState.READY) {
                        try {
                            wait(1000);
                        } catch (Exception e) {
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);

                    connectDialog.hide();
                    enableButtons();
                    loadSubscriptions();
                    loadNextPage();
                }
            }.execute();
        } else
            Toast.makeText(FrontPageActivity.this, R.string.no_network, Toast.LENGTH_SHORT).show();
    }

    private void enableButtons() {
        navigationView.getMenu().findItem(R.id.nav_all).setEnabled(true);
        navigationView.getMenu().findItem(R.id.nav_user).setEnabled(true);
        navigationView.getMenu().findItem(R.id.nav_subreddit).setEnabled(true);
        navigationView.getHeaderView(0).findViewById(R.id.nav_header_logout_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preferences.edit().clear().apply();
                        finish();
                        startActivity(new Intent(FrontPageActivity.this, LoginActivity.class));
                    }
                });

    }

    private void addSubscriptionsToNavMenu(List<Subreddit> subreddits) {
        Menu subscriptionMenu = navigationView.getMenu().addSubMenu(getString(R.string.frontpageactivity_subscriptions));

        if (subreddits.size() == 0) {
            MenuItem defaultItem = subscriptionMenu.add("No subscriptions");
            defaultItem.setEnabled(false);
        }

        for (Subreddit s : subreddits) {
            subscriptionMenu.add(s.getDisplayName());
        }
    }

    private void loadSubscriptions() {
        if (Utilities.isNetworkAvailable(FrontPageActivity.this)) {
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
                        addSubscriptionsToNavMenu(subreddits);
                    }
                }
            }.execute();
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpMenuBar() {
        String subredditTitle = paginator.getSubreddit();
        if (subredditTitle == null)
            getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_title_frontpage));
        else
            getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_title_subreddit, subredditTitle));
        if (paginator.getSorting().requiresTimePeriod())
            getSupportActionBar().setSubtitle(paginator.getSorting().name() + ": " + paginator.getTimePeriod().name());
        else
            getSupportActionBar().setSubtitle(paginator.getSorting().name());
    }

    private void refreshPage() {
        refreshPage(true);
    }

    private void refreshPage(boolean shouldShowProgressBar) {
        mAdapter.clearSubmissions();
        if (shouldShowProgressBar)
            progressBar.setVisibility(ProgressBar.VISIBLE);
        setUpMenuBar();

        paginator.reset();
        loadNextPage();
    }

    private void setUpPaginator(String subreddit) {
        if (paginator == null) {
            paginator = new SubredditPaginator(mRedditClient);
            if (subreddit != null)
                paginator.setSubreddit(subreddit);
        }
    }

    private void updatePaginator(Sorting sort) {
        updatePaginator(sort, null);
    }

    private void updatePaginator(Sorting sort, TimePeriod time) {
        if (paginator != null) {
            paginator.setSorting(sort);
            if (time != null)
                paginator.setTimePeriod(time);

            paginator.reset();
        }
    }

    private void loadNextPage() {
        if (Utilities.isNetworkAvailable(FrontPageActivity.this)) {
            new AsyncTask<Void, Void, Listing<Submission>>() {
                @Override
                protected Listing<Submission> doInBackground(Void... params) {
                    try {
                        return paginator.next();
                    } catch (NetworkException e) {
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(Listing<Submission> submissions) {
                    super.onPostExecute(submissions);
                    if (submissions != null) {
                        if (progressBar.getVisibility() == ProgressBar.VISIBLE)
                            progressBar.setVisibility(ProgressBar.GONE);

                        mAdapter.addSubmissions(submissions);
                        listContainer.setRefreshing(false);
                        invalidateOptionsMenu();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.submissionsactivity_networkerror, Toast.LENGTH_LONG).show();
                    }
                }
            }.execute();
        } else
            Toast.makeText(getApplicationContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
    }

    private void openSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflatedView = getLayoutInflater().inflate(R.layout.dialog_search, null);
        final EditText query = (EditText) inflatedView.findViewById(R.id.dialog_search_query);
        final EditText scope = (EditText) inflatedView.findViewById(R.id.dialog_search_scope);
        builder.setTitle(R.string.dialog_search_title);
        builder.setView(inflatedView);

        builder.setNegativeButton(R.string.dialog_search_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setPositiveButton(R.string.dialog_search_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (query.getText().toString().length() < 1)
                    Toast.makeText(getApplicationContext(), R.string.dialog_search_retry, Toast.LENGTH_SHORT).show();
                else {
                    Intent searchIntent = new Intent(FrontPageActivity.this, SearchActivity.class);
                    searchIntent.putExtra("query", query.getText().toString());
                    searchIntent.putExtra("scope", scope.getText().toString().length() < 1 ? null : scope.getText().toString());
                    startActivity(searchIntent);
                }
            }
        });
        builder.create().show();
    }
}
