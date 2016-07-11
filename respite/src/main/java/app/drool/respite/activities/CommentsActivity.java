package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Submission;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.CommentListAdapter;
import app.drool.respite.impl.SubmissionParcelable;

/**
 * Created by drool on 6/18/16.
 */

public class CommentsActivity extends AppCompatActivity implements CommentListAdapter.LoadAllCommentsListener {

    private static final String TAG = "CommentsActivity.java";
    private CommentListAdapter mAdapter = null;

    private RedditClient mRedditClient = null;
    private RecyclerView commentList = null;
    private ACTIVITY_MODES currentMode = null;
    private String submissionID = null;
    private String commentID = null;
    private CommentSort currentSort = CommentSort.HOT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        commentList = (RecyclerView) findViewById(R.id.activity_comments_commentlist);
        mAdapter = new CommentListAdapter(CommentsActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(CommentsActivity.this);
        commentList.setLayoutManager(layoutManager);
        commentList.setAdapter(mAdapter);

        SubmissionParcelable submissionParcelable = getIntent().getParcelableExtra("top");
        submissionID = getIntent().getStringExtra("submissionID");
        commentID = getIntent().getStringExtra("commentID");

        mRedditClient = ((Respite) getApplication()).getRedditClient();

        if (submissionParcelable != null)
            currentMode = ACTIVITY_MODES.ALL_COMMENTS_WITH_BUNDLE;
        else if (commentID == null)
            currentMode = ACTIVITY_MODES.ALL_COMMENTS_NO_BUNDLE;
        else
            currentMode = ACTIVITY_MODES.SINGLE_COMMENT;

        if (currentMode != ACTIVITY_MODES.ALL_COMMENTS_WITH_BUNDLE) {
            setUpMenuBar("");
            if (currentMode == ACTIVITY_MODES.SINGLE_COMMENT)
                loadComments(submissionID, commentID);
            else
                loadComments(submissionID);
        } else {
            setUpMenuBar(submissionParcelable.getTitle());
            mAdapter.addSubmission(submissionParcelable);
            submissionID = submissionParcelable.getSubmissionID();
            loadComments(submissionID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_comments_sort_hot:
                updatePaginator(CommentSort.HOT);
                break;

            case R.id.menu_comments_sort_new:
                updatePaginator(CommentSort.NEW);
                break;

            case R.id.menu_comments_sort_confidence:
                updatePaginator(CommentSort.CONFIDENCE);
                break;

            case R.id.menu_comments_sort_old:
                updatePaginator(CommentSort.OLD);
                break;

            case R.id.menu_comments_sort_controversial:
                updatePaginator(CommentSort.CONTROVERSIAL);
                break;

            case R.id.menu_comments_sort_top:
                updatePaginator(CommentSort.TOP);
                break;

            case R.id.menu_comments_refresh:
                refreshPage();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void setUpMenuBar(String title) {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(currentSort.name());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_comments, menu);
        return true;
    }

    private void loadComments(final String submissionID) {
        loadComments(submissionID, null);
    }

    private void loadComments(final String submissionID, final String commentID) {
        new AsyncTask<Void, Void, Submission>() {
            @Override
            protected Submission doInBackground(Void... params) {
                SubmissionRequest.Builder request = new SubmissionRequest.Builder(submissionID);
                request.sort(currentSort);
                try {
                    return mRedditClient.getSubmission(request.build());
                } catch (NetworkException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Submission submission) {
                if (submission == null) {
                    Toast.makeText(getApplicationContext(), R.string.commentsactivity_networkerror, Toast.LENGTH_LONG).show();
                } else {
                    mAdapter.addSubmission(submission);
                    setUpMenuBar(submission.getTitle());
                    CommentNode rootComments = submission.getComments();
                    if (commentID != null && rootComments.findChild("t1_" + commentID).isPresent()) {
//                         addSingleCommentThreadAlert();
                        mAdapter.addComments(rootComments.findChild("t1_" + commentID).get());
                    } else {
                        mAdapter.addComments(rootComments);
                    }
                }
            }
        }.execute();
    }

    private void updatePaginator(CommentSort sort) {
        if (currentSort == sort) return;
        currentSort = sort;
        getSupportActionBar().setSubtitle(currentSort.name());
        refreshPage();
    }

    private void refreshPage() {
        refreshPage(false);
    }

    private void refreshPage(boolean shouldIgnoreCurrentMode) {
        mAdapter.clearComments();
        // show PB

        if (!shouldIgnoreCurrentMode && currentMode == ACTIVITY_MODES.SINGLE_COMMENT) {
            loadComments(submissionID, commentID);
        } else if (shouldIgnoreCurrentMode) {
            currentMode = ACTIVITY_MODES.ALL_COMMENTS_NO_BUNDLE;
            loadComments(submissionID);
        } else
            loadComments(submissionID);
    }

    private void addSingleCommentThreadAlert() {
        ViewGroup alert = (ViewGroup) getLayoutInflater().inflate(R.layout.list_item_single_thread_alert, commentList, false);
        LinearLayout alertLayout = (LinearLayout) alert.findViewById(R.id.comments_single_thread_alert);
        alertLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPage(true);
            }
        });

        commentList.addView(alert);
    }

    @Override
    public void onLoadAllComments() { refreshPage(true); }

    private enum ACTIVITY_MODES {
        SINGLE_COMMENT,
        ALL_COMMENTS_WITH_BUNDLE,
        ALL_COMMENTS_NO_BUNDLE
    }
}
