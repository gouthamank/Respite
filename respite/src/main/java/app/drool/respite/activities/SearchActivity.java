package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.TimePeriod;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.SubmissionListAdapter;

/**
 * Created by drool on 7/3/16.
 */

public class SearchActivity extends AppCompatActivity implements SubmissionListAdapter.EndlessScrollListener {
    private static final String TAG = "SearchActivity.java";

    private SubmissionSearchPaginator.SearchSort currentSort = null;
    private TimePeriod currentTimePeriod = null;
    private SubmissionListAdapter mAdapter = null;
    private SubmissionSearchPaginator mPaginator = null;
    private RecyclerView mList = null;
    private ProgressBar progressBar = null;

    private String query;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RedditClient mRedditClient = ((Respite) getApplication()).getRedditClient();
        currentSort = SubmissionSearchPaginator.SearchSort.RELEVANCE;
        currentTimePeriod = TimePeriod.ALL;
        query = getIntent().getStringExtra("query");
        String subreddit = getIntent().getStringExtra("scope");
        mAdapter = new SubmissionListAdapter(this);
        mPaginator = new SubmissionSearchPaginator(mRedditClient, query);
        if (subreddit != null && subreddit.length() > 0)
            mPaginator.setSubreddit(subreddit);

        mList = (RecyclerView) findViewById(R.id.activity_search_resultlist);
        progressBar = (ProgressBar) findViewById(R.id.activity_search_progressbar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(layoutManager);
        mList.setAdapter(mAdapter);

        setUpMenuBar();
        refreshPage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search_sort_relevance:
                updateSort(SubmissionSearchPaginator.SearchSort.RELEVANCE);
                break;
            case R.id.menu_search_sort_top:
                updateSort(SubmissionSearchPaginator.SearchSort.TOP);
                break;
            case R.id.menu_search_sort_new:
                updateSort(SubmissionSearchPaginator.SearchSort.NEW);
                break;
            case R.id.menu_search_sort_comments:
                updateSort(SubmissionSearchPaginator.SearchSort.COMMENTS);
                break;

            case R.id.menu_search_filter_time_all:
                updateTimePeriod(TimePeriod.ALL);
                break;
            case R.id.menu_search_filter_time_hour:
                updateTimePeriod(TimePeriod.HOUR);
                break;

            case R.id.menu_search_filter_time_day:
                updateTimePeriod(TimePeriod.DAY);
                break;
            case R.id.menu_search_filter_time_week:
                updateTimePeriod(TimePeriod.WEEK);
                break;
            case R.id.menu_search_filter_time_month:
                updateTimePeriod(TimePeriod.MONTH);
                break;
            case R.id.menu_search_filter_time_year:
                updateTimePeriod(TimePeriod.YEAR);
                break;

            case R.id.menu_search_refresh:
                refreshPage();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);
    }

    private void loadNextPage() {
        new AsyncTask<Void, Void, Listing<Submission>>() {
            @Override
            protected Listing<Submission> doInBackground(Void... params) {
                try {
                    return mPaginator.next();
                } catch (NetworkException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Listing<Submission> results) {
                progressBar.setVisibility(View.GONE);
                if(results == null)
                    Toast.makeText(getApplicationContext(), R.string.searchactivity_networkerror, Toast.LENGTH_LONG).show();
                else {
                    mList.setVisibility(View.VISIBLE);
                    mAdapter.addSubmissions(results);
                }
            }
        }.execute();
    }

    private void setUpMenuBar() {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.searchactivity_menubar_title, query));
        getSupportActionBar().setSubtitle(currentSort.name() + ": " + currentTimePeriod.name());
    }

    private void updateSort(SubmissionSearchPaginator.SearchSort newSort) {
        if (currentSort != newSort) {
            currentSort = newSort;
            refreshPage();
        }
    }

    private void updateTimePeriod(TimePeriod newTimePeriod) {
        if (currentTimePeriod != newTimePeriod) {
            currentTimePeriod = newTimePeriod;
            refreshPage();
        }
    }

    private void refreshPage() {
        if (mPaginator == null) return;

        setUpMenuBar();
        progressBar.setVisibility(View.VISIBLE);
        mList.setVisibility(View.GONE);
        mAdapter.clearSubmissions();
        mPaginator.reset();
        mPaginator.setSearchSorting(currentSort);
        mPaginator.setTimePeriod(currentTimePeriod);

        loadNextPage();
    }

    @Override
    public void onLoadMore(int position) {
        loadNextPage();
    }
}
