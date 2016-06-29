package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.laurencedawson.activetextview.ActiveTextView;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.handlers.LinkHandler;
import app.drool.respite.impl.SubmissionParcelable;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/18/16.
 */

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity.java";
    private TextView headerDescription, headerTitle, headerComments, headerScore, headerSelfText;

    ;
    private ProgressBar progressBar;
    private LinearLayout commentList = null;
    private SubmissionParcelable submissionParcelable = null;
    private ACTIVITY_MODES currentMode = null;
    private String submissionID = null;
    private String commentID = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        commentList = (LinearLayout) findViewById(R.id.comments_list);
        addEmptyHeader();

        submissionParcelable = getIntent().getParcelableExtra("top");
        submissionID = getIntent().getStringExtra("submissionID");
        commentID = getIntent().getStringExtra("commentID");

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
            setUpMenuBar(submissionParcelable.getSubreddit());
            updateHeader(submissionParcelable);

            submissionID = submissionParcelable.getSubmissionID();
            loadComments(submissionID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_comments_refresh:
                refreshPage();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpMenuBar(String subreddit) {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.actionbar_title_subreddit, subreddit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_comments, menu);
        return true;
    }

    private void loadComments(final String submissionID) {
        new AsyncTask<Void, Void, Submission>() {
            @Override
            protected Submission doInBackground(Void... params) {
                return ((Respite) getApplication()).getRedditClient().getSubmission(submissionID);
            }

            @Override
            protected void onPostExecute(Submission submission) {
                progressBar.setVisibility(ProgressBar.GONE);

                updateHeader(submission);
                setUpMenuBar(submission.getSubredditName());
                addComments(submission.getComments());
            }
        }.execute();
    }

    private void loadComments(final String submissionID, final String commentID) {
        new AsyncTask<Void, Void, Submission>() {
            @Override
            protected Submission doInBackground(Void... params) {
                return ((Respite) getApplication()).getRedditClient().getSubmission(submissionID);
            }

            @Override
            protected void onPostExecute(Submission submission) {
                progressBar.setVisibility(ProgressBar.GONE);


                updateHeader(submission);
                setUpMenuBar(submission.getSubredditName());
                CommentNode rootComments = submission.getComments();
                if (rootComments.findChild("t1_" + commentID).isPresent()) {
                    addSingleCommentThreadAlert();
                    addComments(rootComments.findChild("t1_" + commentID).get());
                } else
                    addComments(rootComments);
            }
        }.execute();
    }

    private void refreshPage() {
        refreshPage(false);
    }

    private void refreshPage(boolean shouldIgnoreCurrentMode) {
        Toast.makeText(getApplicationContext(), "Should refresh here.", Toast.LENGTH_SHORT).show();
        commentList.removeAllViews();
        progressBar.setVisibility(View.VISIBLE);

        addEmptyHeader();

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

    private void addEmptyHeader() {
        ViewGroup headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_comments_header, commentList, false);

        headerDescription = (TextView) headerView.findViewById(R.id.comments_header_description);
        headerTitle = (TextView) headerView.findViewById(R.id.comments_header_title);
        headerComments = (TextView) headerView.findViewById(R.id.comments_header_comments);
        headerScore = (TextView) headerView.findViewById(R.id.comments_header_score);
        headerSelfText = (TextView) headerView.findViewById(R.id.comments_header_selftext);
        progressBar = (ProgressBar) findViewById(R.id.activity_comments_progressbar);

        SubmissionParcelable submission = SubmissionParcelable.newDummyInstance();
        headerDescription.setText(submission.getDescription());
        headerComments.setText(submission.getComments());
        headerScore.setText(submission.getScore());
        headerTitle.setText(submission.getTitle());
        if (submission.getSelfText() != null)
            headerSelfText.setText(Html.fromHtml(submission.getSelfText()));
        else
            headerSelfText.setVisibility(View.GONE);

        commentList.addView(headerView);
    }

    private void updateHeader(SubmissionParcelable submission) {
        headerDescription.setText(submission.getDescription());
        headerComments.setText(submission.getComments());
        headerScore.setText(submission.getScore());
        headerTitle.setText(submission.getTitle());
        if (submission.getSelfText() != null) {
            headerSelfText.setVisibility(View.VISIBLE);
            headerSelfText.setText(Html.fromHtml(submission.getSelfText()));
        } else
            headerSelfText.setVisibility(View.GONE);
    }

    private void updateHeader(Submission submission) {
        updateHeader(new SubmissionParcelable(this, submission));
    }

    private void addComments(CommentNode comments) {
        for (CommentNode node : comments.walkTree()) {
            Log.d(TAG, "addComments: " + node.toString());

            ViewGroup comment = (ViewGroup) getLayoutInflater().inflate(R.layout.list_item_comment, commentList, false);

            ((ActiveTextView) comment.findViewById(R.id.list_item_comment_body))
                    .setText(Utilities.getHTMLFromMarkdown(node.getComment().data("body_html")));
            ((ActiveTextView) comment.findViewById(R.id.list_item_comment_body)).setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    LinkHandler.analyse(CommentsActivity.this, url);
                }
            });

            String description = node.getComment().getAuthor() +
                    " • " +
                    String.valueOf(node.getComment().getScore()) +
                    " • " +
                    Utilities.getReadableCreationTime(node.getComment().getCreated());
            ((TextView) comment.findViewById(R.id.list_item_comment_description)).setText(description);

            int depth = node.getDepth() - 1;
            View indent = comment.findViewById(R.id.list_item_comment_indent);
            ViewGroup.LayoutParams params = indent.getLayoutParams();
            params.width = Utilities.getPixelsFromDPs(this, depth * 5);
            comment.findViewById(R.id.list_item_comment_indent)
                    .setLayoutParams(params);

            if (commentList != null)
                commentList.addView(comment);
        }
    }

    private enum ACTIVITY_MODES {
        SINGLE_COMMENT,
        ALL_COMMENTS_WITH_BUNDLE,
        ALL_COMMENTS_NO_BUNDLE
    }
}
