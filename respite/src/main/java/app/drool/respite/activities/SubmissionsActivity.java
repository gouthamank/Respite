package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.SubmissionListAdapter;

/**
 * Created by drool on 6/15/16.
 */

public class SubmissionsActivity extends AppCompatActivity implements SubmissionListAdapter.EndlessScrollListener{
    private SubmissionListAdapter mAdapter = null;
    private ProgressBar progressBar = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_front_page);
        final RecyclerView submissionList = (RecyclerView) findViewById(R.id.submissions_list);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        final RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        assert submissionList != null;
        submissionList.setLayoutManager(mLayoutManager);
        mAdapter = new SubmissionListAdapter(this);
        submissionList.setAdapter(mAdapter);
        mAdapter.setEndlessScrollListener(this);

        setUpMenuBar();

        progressBar.setVisibility(ProgressBar.VISIBLE);
        loadNextPage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);
    }

    @SuppressWarnings("ConstantConditions")
    private void setUpMenuBar() {
        String subredditTitle = ((Respite) getApplication()).getPaginator().getSubreddit();
        if(subredditTitle == null)
            getSupportActionBar().setTitle(getResources().getString(R.string.action_bar_title_front_page));
        else
            getSupportActionBar().setTitle(getResources().getString(R.string.action_bar_title_subreddit, subredditTitle));
    }

    private void loadNextPage() {
        new AsyncTask<Void, Void, Listing<Submission>>(){
            @Override
            protected Listing<Submission> doInBackground(Void... params) {
                try {
                    return ((Respite) getApplication()).getPaginator().next();
                } catch (NetworkException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Listing<Submission> submissions) {
                super.onPostExecute(submissions);
                // Toast.makeText(SubmissionsActivity.this, String.valueOf(mAdapter.getItemCount()), Toast.LENGTH_SHORT).show();

                if(submissions != null) {
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
