package app.drool.respite.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import net.dean.jraw.ApiException;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.CommentListAdapter;
import app.drool.respite.impl.SubmissionParcelable;

/**
 * Created by drool on 6/18/16.
 */

public class CommentsActivity extends AppCompatActivity implements CommentListAdapter.LoadAllCommentsListener,
                                                                    CommentListAdapter.ReplyToCommentListener{

    private static final String TAG = "CommentsActivity.java";
    private CommentListAdapter mAdapter = null;

    private RedditClient mRedditClient = null;
    private RecyclerView commentList = null;
    private ACTIVITY_MODES currentMode = null;
    private String submissionID = null;
    private String commentID = null;
    private CommentSort currentSort = CommentSort.HOT;
    private Submission submission = null;
    private Comment commentInContext = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        commentList = (RecyclerView) findViewById(R.id.activity_comments_commentlist);
        mAdapter = new CommentListAdapter(CommentsActivity.this, ((Respite) getApplication()).getRedditClient());
        LinearLayoutManager layoutManager = new LinearLayoutManager(CommentsActivity.this);
        commentList.setLayoutManager(layoutManager);
        commentList.setAdapter(mAdapter);
        mAdapter.setLoadAllCommentsListener(this);
        mAdapter.setReplyToCommentListener(this);

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
            if (currentMode == ACTIVITY_MODES.SINGLE_COMMENT) {
                loadComments(submissionID, commentID);
            } else
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
            case R.id.menu_comments_reply:
                startReplyToSubmission();
                break;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == ReplyActivity.REPLY_POST) {
            replyToContribution(data.getStringExtra("reply"), submission);
        } else if (resultCode == ReplyActivity.REPLY_COMMENT) {
            replyToContribution(data.getStringExtra("reply"), commentInContext);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_comments, menu);
        return true;
    }

    private void replyToContribution(String markdown, final Contribution c) {
        new AsyncTask<String, Void, String>(){
            @Override
            protected String doInBackground(String... params) {
                String markdown = params[0];
                try {
                    return (new AccountManager(mRedditClient)).reply(c, markdown);
                } catch (ApiException e) {
                    Log.d(TAG, "doInBackground: " + e.getReason());
                    Toast.makeText(getApplicationContext(), R.string.commentsactivity_reply_apirerror, Toast.LENGTH_LONG).show();
                } catch (NetworkException e) {
                    Toast.makeText(getApplicationContext(), R.string.commentsactivity_reply_networkerror, Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String newID) {
                Toast.makeText(getApplicationContext(), R.string.commentsactivity_reply_success, Toast.LENGTH_LONG).show();
                refreshPage();
            }
        }.execute(markdown);
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
                    CommentsActivity.this.submission = submission;
                    mAdapter.addSubmission(submission);
                    setUpMenuBar(submission.getTitle());
                    CommentNode rootComments = submission.getComments();
                    if(rootComments.getImmediateSize() == 0) {
                        Toast.makeText(getApplicationContext(), R.string.commentsactivity_nocomments, Toast.LENGTH_LONG).show();
                    }
                    if (commentID != null && rootComments.findChild("t1_" + commentID).isPresent()) {
                        mAdapter.setShouldShowSingleCommentNotice(true);
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

        if (!shouldIgnoreCurrentMode && currentMode == ACTIVITY_MODES.SINGLE_COMMENT) {
            loadComments(submissionID, commentID);
        } else if (shouldIgnoreCurrentMode) {
            currentMode = ACTIVITY_MODES.ALL_COMMENTS_NO_BUNDLE;
            commentID = null;
            mAdapter.setShouldShowSingleCommentNotice(false);
            loadComments(submissionID);
        } else
            loadComments(submissionID);
    }

    @Override
    public void onLoadAllComments() {
        refreshPage(true);
    }


    public void startReplyToSubmission() {
        Intent replyIntent = new Intent(CommentsActivity.this, ReplyActivity.class);
        replyIntent.putExtra("postRequest", true);
        startActivityForResult(replyIntent, ReplyActivity.REPLY_REQUEST);
    }

    // Implementation from within list adapter
    @Override
    public void startReplyToComment(Comment c) {
        commentInContext = c;
        Intent replyIntent = new Intent(CommentsActivity.this, ReplyActivity.class);
        replyIntent.putExtra("postRequest", false);
        startActivityForResult(replyIntent, ReplyActivity.REPLY_REQUEST);
    }

    private enum ACTIVITY_MODES {
        SINGLE_COMMENT,
        ALL_COMMENTS_WITH_BUNDLE,
        ALL_COMMENTS_NO_BUNDLE
    }
}
