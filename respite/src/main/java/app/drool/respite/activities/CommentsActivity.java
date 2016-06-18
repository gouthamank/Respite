package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.FluentIterable;
import com.laurencedawson.activetextview.ActiveTextView;

import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Submission;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.impl.SubmissionParcelable;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/18/16.
 */

public class CommentsActivity extends AppCompatActivity {

    private TextView headerDescription, headerTitle, headerComments, headerScore, headerSelfText;
    private static final String TAG = "CommentsActivity.java";
    private LinearLayout commentList = null;
    private SubmissionParcelable submissionParcelable = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_comments);
        submissionParcelable = getIntent().getParcelableExtra("top");
        commentList = (LinearLayout) findViewById(R.id.comments_list);

        setUpMenuBar(submissionParcelable.getSubreddit());
        updateHeader(submissionParcelable);
        loadComments(submissionParcelable.getSubmissionID());
    }

    @Override
    protected void onResume() {
        super.onResume ();
    }

    private void setUpMenuBar(String subreddit) {
        getSupportActionBar().setTitle(getResources().getString(R.string.action_bar_title_subreddit, subreddit));
    }

    private void loadComments(final String submissionID) {
        new AsyncTask<Void, Void, Submission>() {
            @Override
            protected Submission doInBackground(Void... params) {
                return ((Respite) getApplication()).getRedditClient().getSubmission(submissionID);
            }

            @Override
            protected void onPostExecute(Submission submission) {
                Log.d(TAG, "onPostExecute: " + submission.getComments().toString());
                updateHeader(submission);
                addComments(submission.getComments());
            }
        }.execute();
    }

    private void updateHeader(SubmissionParcelable submissionOld) {
        ViewGroup headerView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_comments_header, commentList, false);

        headerDescription = (TextView) headerView.findViewById(R.id.comments_header_description);
        headerTitle = (TextView) headerView.findViewById(R.id.comments_header_title);
        headerComments = (TextView) headerView.findViewById(R.id.comments_header_comments);
        headerScore = (TextView) headerView.findViewById(R.id.comments_header_score);
        headerSelfText = (TextView) headerView.findViewById(R.id.comments_header_selftext);

        headerDescription.setText(submissionOld.getDescription());
        headerComments.setText(submissionOld.getComments());
        headerScore.setText(submissionOld.getScore());
        headerTitle.setText(submissionOld.getTitle());
        if(submissionOld.getSelfText() != null)
            headerSelfText.setText(Html.fromHtml(submissionOld.getSelfText()));
        else
            headerSelfText.setVisibility(View.GONE);

        commentList.addView(headerView);
    }
    private void updateHeader(Submission submission) {
        SubmissionParcelable submissionNew = new SubmissionParcelable(this, submission);

        headerDescription.setText(submissionNew.getDescription());
        headerComments.setText(submissionNew.getComments());
        headerScore.setText(submissionNew.getScore());
        headerTitle.setText(submissionNew.getTitle());

        if(submissionNew.getSelfText() != null) {
            headerSelfText.setText(Html.fromHtml(submissionNew.getSelfText()));
            headerSelfText.setVisibility(View.VISIBLE);
        }
        else
            headerSelfText.setVisibility(View.GONE);
    }

    private void addComments(CommentNode comments) {
        FluentIterable<CommentNode> iter = comments.walkTree();
        for(CommentNode node : iter) {
            ViewGroup comment = (ViewGroup) getLayoutInflater().inflate(R.layout.list_item_comment, commentList, false);
            ((ActiveTextView) comment.findViewById(R.id.list_item_comment_body))
                    .setText(Utilities.getHTMLFromMarkdown(node.getComment().data("body_html")));
            ((ActiveTextView) comment.findViewById(R.id.list_item_comment_body)).setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    Toast.makeText(CommentsActivity.this, url, Toast.LENGTH_SHORT).show();
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
            commentList.addView(comment);
        }
    }
}
