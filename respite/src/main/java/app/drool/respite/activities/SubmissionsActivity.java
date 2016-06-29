package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.SubmissionListAdapter;

/**
 * Created by drool on 6/15/16.
 */

public class SubmissionsActivity extends AppCompatActivity implements SubmissionListAdapter.EndlessScrollListener {
    private SubmissionListAdapter mAdapter = null;
    private ProgressBar progressBar = null;
    private SubredditPaginator paginator = null;
    private Sorting currentSort = null;
    private TimePeriod currentSortTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submissions);
        
        final RecyclerView submissionList = (RecyclerView) findViewById(R.id.submissions_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        assert submissionList != null;
        submissionList.setLayoutManager(mLayoutManager);
        mAdapter = new SubmissionListAdapter(this);
        submissionList.setAdapter(mAdapter);
        mAdapter.setEndlessScrollListener(this);

        if (getIntent().getExtras() == null)
            setUpPaginator(null);
        else
            setUpPaginator(getIntent().getExtras().getString("subreddit"));

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

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);
    }

    private void refreshPage() {
        mAdapter.clearSubmissions();
        progressBar.setVisibility(ProgressBar.VISIBLE);
        setUpMenuBar();

        paginator.reset();
        loadNextPage();
    }

    private void setUpPaginator(String subreddit) {
        if (paginator == null) {
            paginator = new SubredditPaginator(((Respite) getApplication()).getRedditClient());
            if (subreddit != null)
                paginator.setSubreddit(subreddit);
        }
    }

    private void updatePaginator(Sorting sort) {
        updatePaginator(sort, null);
    }

    private void updatePaginator(Sorting sort, TimePeriod time){
        if (paginator != null) {
            paginator.reset();
            paginator.setSorting(sort);
            if(time != null)
                paginator.setTimePeriod(time);
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
        if(paginator.getSorting().requiresTimePeriod())
            getSupportActionBar().setSubtitle(paginator.getSorting().name() + ": " + paginator.getTimePeriod().name());
        else
            getSupportActionBar().setSubtitle(paginator.getSorting().name());
    }

    private void loadNextPage() {
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
                }
            }
        }.execute();
    }

    @Override
    public void onLoadMore(int position) {
        loadNextPage();
    }
}
