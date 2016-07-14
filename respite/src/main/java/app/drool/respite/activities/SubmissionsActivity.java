package app.drool.respite.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.SubmissionListAdapter;
import app.drool.respite.impl.EndlessRecyclerViewScrollListener;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/15/16.
 */

public class SubmissionsActivity extends AppCompatActivity {
    private static final String TAG = "SubmissionsActivity";

    private SubmissionListAdapter mAdapter = null;
    private SwipeRefreshLayout listContainer = null;
    private ProgressBar progressBar = null;
    private SubredditPaginator paginator = null;
    private RedditClient mRedditClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submissions);

        final RecyclerView submissionList = (RecyclerView) findViewById(R.id.activity_submissions_list);
        listContainer = (SwipeRefreshLayout) findViewById(R.id.activity_submissions_list_container);
        progressBar = (ProgressBar) findViewById(R.id.activity_submissions_progressbar);
        mRedditClient = ((Respite) getApplication()).getRedditClient();

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

        if (getIntent().getExtras() == null)
            setUpPaginator(null);
        else {
            mAdapter.disableSubredditClickable();
            setUpPaginator(getIntent().getExtras().getString("subreddit"));
        }

        setUpMenuBar();
        progressBar.setVisibility(ProgressBar.VISIBLE);
        loadNextPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_submissions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (paginator.getSubreddit() != null && !paginator.getSubreddit().contentEquals("all"))
            menu.findItem(R.id.menu_submissions_submit).setEnabled(true);
        return super.onPrepareOptionsMenu(menu);
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
                Intent submitLinkIntent = new Intent(SubmissionsActivity.this, SubmitActivity.class);
                submitLinkIntent.putExtra("subreddit", paginator.getSubreddit());
                submitLinkIntent.putExtra("mode", "link");
                startActivity(submitLinkIntent);
                return true;

            case R.id.menu_submissions_submit_self:
                Intent submitSelfIntent = new Intent(SubmissionsActivity.this, SubmitActivity.class);
                submitSelfIntent.putExtra("subreddit", paginator.getSubreddit());
                submitSelfIntent.putExtra("mode", "self");
                startActivity(submitSelfIntent);
                return true;

            default:
                finish();
                return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utilities.isNetworkAvailable(SubmissionsActivity.this))
            ((Respite) getApplication()).refreshCredentials(this);
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

    @SuppressWarnings("ConstantConditions")
    private void setUpMenuBar() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    private void loadNextPage() {
        if (Utilities.isNetworkAvailable(SubmissionsActivity.this)) {
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
                    Intent searchIntent = new Intent(SubmissionsActivity.this, SearchActivity.class);
                    searchIntent.putExtra("query", query.getText().toString());
                    searchIntent.putExtra("scope", scope.getText().toString().length() < 1 ? null : scope.getText().toString());
                    startActivity(searchIntent);
                }
            }
        });
        builder.create().show();
    }
}
