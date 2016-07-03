package app.drool.respite.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.DistinguishedStatus;
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
    private TextView headerTitle, headerComments, headerScore;
    private ActiveTextView headerSelfText;
    private TextView headerAuthor, headerSubreddit, headerTimeCreated, headerLinkFlair, headerDomain, headerLink;
    private View headerDescriptionBlock, headerLinkLayout;

    private RedditClient mRedditClient = null;
    private ProgressBar progressBar;
    private LinearLayout commentList = null;
    private ACTIVITY_MODES currentMode = null;
    private String submissionID = null;
    private String commentID = null;
    private String threadOP = null;
    private CommentSort currentSort = CommentSort.HOT;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comments);
        commentList = (LinearLayout) findViewById(R.id.comments_list);
        addEmptyHeader();

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
            updateHeader(submissionParcelable);

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
                progressBar.setVisibility(ProgressBar.GONE);

                if(submission == null) {
                    Toast.makeText(getApplicationContext(), R.string.commentsactivity_networkerror, Toast.LENGTH_LONG).show();
                } else {
                    updateHeader(submission);
                    setUpMenuBar(submission.getTitle());
                    CommentNode rootComments = submission.getComments();
                    if (commentID != null && rootComments.findChild("t1_" + commentID).isPresent()) {
                        addSingleCommentThreadAlert();
                        addComments(rootComments.findChild("t1_" + commentID).get());
                    } else
                        addComments(rootComments);
                }
            }
        }.execute();
    }

    private void updatePaginator(CommentSort sort) {
        if(currentSort == sort) return;
        currentSort = sort;
        refreshPage();
    }

    private void refreshPage() {
        refreshPage(false);
    }

    private void refreshPage(boolean shouldIgnoreCurrentMode) {
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

        headerAuthor = (TextView) headerView.findViewById(R.id.comments_header_author);
        headerSubreddit = (TextView) headerView.findViewById(R.id.comments_header_subreddit);
        headerTimeCreated = (TextView) headerView.findViewById(R.id.comments_header_timecreated);
        headerLinkFlair = (TextView) headerView.findViewById(R.id.comments_header_link_flair);
        headerDomain = (TextView) headerView.findViewById(R.id.comments_header_domain);
        headerLink = (TextView) headerView.findViewById(R.id.comments_header_link);
        headerDescriptionBlock = headerView.findViewById(R.id.comments_header_description);
        headerLinkLayout = headerView.findViewById(R.id.comments_header_link_layout);

        headerTitle = (TextView) headerView.findViewById(R.id.comments_header_title);
        headerComments = (TextView) headerView.findViewById(R.id.comments_header_comments);
        headerScore = (TextView) headerView.findViewById(R.id.comments_header_score);
        headerSelfText = (ActiveTextView) headerView.findViewById(R.id.comments_header_selftext);
        progressBar = (ProgressBar) findViewById(R.id.activity_comments_progressbar);

        SubmissionParcelable submission = SubmissionParcelable.newDummyInstance();
        headerDescriptionBlock.setVisibility(View.INVISIBLE);
        headerComments.setText(submission.getComments());
        headerScore.setText(submission.getScore());
        headerTitle.setText(submission.getTitle());
        headerSelfText.setVisibility(View.GONE);
        headerLinkLayout.setVisibility(View.GONE);

        commentList.addView(headerView);
    }

    private void updateHeader(final SubmissionParcelable submission) {

        headerAuthor.setText(submission.getAuthor());
        threadOP = submission.getAuthor();

        if (submission.getAuthor() != null) {
            final String authorURL = "/u/" + submission.getAuthor();
            headerAuthor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!authorURL.contentEquals("/u/[deleted]"))
                        LinkHandler.analyse(CommentsActivity.this, authorURL);
                }
            });
        }
        headerSubreddit.setText(submission.getSubreddit());
        if (submission.getSubreddit() != null) {
            final String subredditURL = "/r/" + submission.getSubreddit();
            headerSubreddit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkHandler.analyse(CommentsActivity.this, subredditURL);
                }
            });
        }
        headerTimeCreated.setText(submission.getTimeCreated());
        headerDomain.setText(submission.getDomain());
        headerLinkFlair.setText(submission.getLinkFlair());
        headerDescriptionBlock.setVisibility(View.VISIBLE);

        headerComments.setText(submission.getComments());
        headerScore.setText(submission.getScore());
        headerTitle.setText(submission.getTitle());
        if (submission.getLinkFlair() == null)
            headerLinkFlair.setVisibility(View.GONE);

        if (submission.isNSFW())
            headerTitle.setTextColor(ContextCompat.getColor(this, R.color.textTitleNSFW_Dark));
        if (submission.isStickied())
            headerTitle.setTextColor(ContextCompat.getColor(this, R.color.textTitleStickied));

        if (submission.getSelfText() != null) {
            headerLinkLayout.setVisibility(View.GONE);
            headerSelfText.setVisibility(View.VISIBLE);
            headerSelfText.setText(Utilities.getHTMLFromMarkdown(submission.getSelfText()));
            headerSelfText.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    LinkHandler.analyse(CommentsActivity.this, url);
                }
            });
        } else if (!submission.isSelfPost()) {
            headerLinkLayout.setVisibility(View.VISIBLE);
            headerSelfText.setVisibility(View.GONE);
            headerSelfText.setText(submission.getLink());
            headerLinkLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkHandler.analyse(CommentsActivity.this, submission.getLink());
                }
            });
            headerLink.setText(submission.getLink());
        } else {
            headerSelfText.setVisibility(View.GONE);
            headerLinkLayout.setVisibility(View.GONE);
        }
    }

    private void updateHeader(Submission submission) {
        updateHeader(new SubmissionParcelable(this, submission));
    }

    private void addComments(CommentNode comments) {
        for (CommentNode node : comments.walkTree()) {
            ViewGroup comment = (ViewGroup) getLayoutInflater().inflate(R.layout.list_item_comment, commentList, false);
            View commentMarker = comment.findViewById(R.id.list_item_comment_marker);
            TextView author, flair, score, timeCreated, gildedCount, timeEdited;
            ActiveTextView body;

            author = (TextView) comment.findViewById(R.id.list_item_comment_author);
            flair = (TextView) comment.findViewById(R.id.list_item_comment_flair);
            score = (TextView) comment.findViewById(R.id.list_item_comment_score);
            timeCreated = (TextView) comment.findViewById(R.id.list_item_comment_timecreated);
            gildedCount = (TextView) comment.findViewById(R.id.list_item_comment_gilded_count);
            timeEdited = (TextView) comment.findViewById(R.id.list_item_comment_timeedited);
            body = (ActiveTextView) comment.findViewById(R.id.list_item_comment_body);

            body.setText(Utilities.getHTMLFromMarkdown(node.getComment().data("body_html")));
            body.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    LinkHandler.analyse(CommentsActivity.this, url);
                }
            });

            if(node.getComment().getAuthor().contentEquals(threadOP))
                author.setTextColor(ContextCompat.getColor(this, R.color.distinguishedOP));

            if(node.getComment().getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
                author.setTextColor(ContextCompat.getColor(this, R.color.distinguishedMod));

            if(node.getComment().getDistinguishedStatus() == DistinguishedStatus.ADMIN)
                author.setTextColor(ContextCompat.getColor(this, R.color.distinguishedAdmin));

            author.setText(node.getComment().getAuthor());
            final String authorURL = "/u/" + node.getComment().getAuthor();
            author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!authorURL.contentEquals("/u/[deleted]"))
                        LinkHandler.analyse(CommentsActivity.this, authorURL);
                }
            });
            timeCreated.setText(Utilities.getReadableCreationTime(node.getComment().getCreated()));

            if (node.getComment().getAuthorFlair() != null
                    && node.getComment().getAuthorFlair().getText() != null
                    && node.getComment().getAuthorFlair().getText().length() > 0)
                flair.setText(node.getComment().getAuthorFlair().getText());
            else
                flair.setVisibility(View.GONE);

            if (node.getComment().isScoreHidden())
                score.setText("??");
            else
                score.setText(String.valueOf(node.getComment().getScore()));

            if (node.getComment().getTimesGilded() > 0)
                gildedCount.setText(getString(R.string.comment_gilded_count, node.getComment().getTimesGilded()));
            else
                gildedCount.setVisibility(View.GONE);

            if (node.getComment().hasBeenEdited())
                timeEdited.setText(getString(R.string.comment_edited, Utilities.getReadableCreationTime(node.getComment().getEditDate())));
            else
                timeEdited.setVisibility(View.GONE);

            int depth = node.getDepth() - 1;
            View indent = comment.findViewById(R.id.list_item_comment_indent);
            ViewGroup.LayoutParams params = indent.getLayoutParams();
            params.width = Utilities.getPixelsFromDPs(this, depth * 5);
            comment.findViewById(R.id.list_item_comment_indent)
                    .setLayoutParams(params);
            if (depth == 0)
                commentMarker.setVisibility(View.GONE);

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
