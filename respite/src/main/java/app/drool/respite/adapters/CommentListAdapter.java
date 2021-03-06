package app.drool.respite.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.laurencedawson.activetextview.ActiveTextView;
import com.wefika.flowlayout.FlowLayout;

import net.dean.jraw.RedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import java.util.ArrayList;

import app.drool.respite.R;
import app.drool.respite.handlers.LinkHandler;
import app.drool.respite.impl.SubmissionParcelable;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 7/10/16.
 */

public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTICE = 1;
    private static final int TYPE_COMMENT = 2;

    private LoadAllCommentsListener loadAllCommentsListener = null;
    private ReplyToCommentListener replyToCommentListener = null;
    private ArrayList<CommentNode> commentNodes = null;
    private ArrayList<VoteDirection> votes = null;
    private SubmissionParcelable submission = null;
    private RedditClient mRedditClient = null;
    private String threadOP = null;
    private Context mContext = null;
    private boolean isSingleCommentNoticeShown = false; // single comment notice

    public CommentListAdapter(Context c, RedditClient mRedditClient) {
        mContext = c;
        commentNodes = new ArrayList<>();
        votes = new ArrayList<>();
        this.mRedditClient = mRedditClient;
    }

    public void addSubmission(SubmissionParcelable s) {
        threadOP = s.getAuthor();
        this.submission = s;
    }

    public void addSubmission(Submission s) {
        threadOP = s.getAuthor();
        this.submission = new SubmissionParcelable(mContext, s);
    }

    public void addComments(CommentNode c) {
        for (CommentNode n : c.walkTree()) {
            votes.add(n.getComment().getVote());
            commentNodes.add(n);
        }

        notifyDataSetChanged();
    }

    public void clear() {
        this.submission = null;
        this.threadOP = null;
        this.commentNodes.clear();
        notifyDataSetChanged();
    }

    public void clearComments() {
        this.commentNodes.clear();
        notifyDataSetChanged();
    }

    public void setShouldShowSingleCommentNotice(boolean shouldShow) {
        isSingleCommentNoticeShown = shouldShow;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.activity_comments_header, parent, false);
            LinearLayout view = (LinearLayout) v;
            return new HeaderHolder(view);

        } else if (viewType == TYPE_NOTICE) {

            View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_single_thread_alert, parent, false);
            LinearLayout view = (LinearLayout) v;
            return new NoticeHolder(view);

        } else if (viewType == TYPE_COMMENT) {

            View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_comment, parent, false);
            LinearLayout view = (LinearLayout) v;
            return new CommentHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            final HeaderHolder headerHolder = (HeaderHolder) holder;
            if (submission == null) {
                headerHolder.score.setText("");
                headerHolder.comments.setText("");
                headerHolder.title.setText("");
                headerHolder.headerLinkLayout.setVisibility(View.GONE);
                headerHolder.selfText.setVisibility(View.GONE);
                headerHolder.headerDescription.setVisibility(View.INVISIBLE);
                headerHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                headerHolder.headerDescription.setVisibility(View.VISIBLE);

                headerHolder.author.setText(submission.getAuthor());
                if (submission.getAuthor() != null) {
                    final String authorURL = "/u/" + submission.getAuthor();
                    headerHolder.author.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!authorURL.contentEquals("/u/[deleted]"))
                                LinkHandler.analyse(mContext, authorURL);
                        }
                    });
                }
                headerHolder.author.setTextColor(ContextCompat.getColor(mContext, R.color.distinguishedOP));
                if (submission.getDistinguishedStatus().contentEquals(DistinguishedStatus.MODERATOR.name()))
                    headerHolder.author.setTextColor(ContextCompat.getColor(mContext, R.color.distinguishedMod));
                else if (submission.getDistinguishedStatus().contentEquals(DistinguishedStatus.ADMIN.name()) ||
                        submission.getDistinguishedStatus().contentEquals(DistinguishedStatus.SPECIAL.name()))
                    headerHolder.author.setTextColor(ContextCompat.getColor(mContext, R.color.distinguishedAdmin));

                headerHolder.subreddit.setText(submission.getSubreddit());
                if (submission.getSubreddit() != null) {
                    headerHolder.subreddit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String subredditURL = "/r/" + submission.getSubreddit();
                            LinkHandler.analyse(mContext, subredditURL);
                        }
                    });
                }

                headerHolder.timeCreated.setText(submission.getTimeCreated());

                headerHolder.domain.setText(submission.getDomain());

                headerHolder.flairTag.setText(submission.getLinkFlair());
                if (submission.getLinkFlair() == null)
                    headerHolder.flairTag.setVisibility(View.GONE);
                else
                    headerHolder.flairTag.setVisibility(View.VISIBLE);
                
                if (submission.isNSFW())
                    headerHolder.nsfwTag.setVisibility(View.VISIBLE);
                else
                    headerHolder.nsfwTag.setVisibility(View.GONE);
                
                if (submission.isStickied())
                    headerHolder.stickyTag.setVisibility(View.VISIBLE);
                else
                    headerHolder.stickyTag.setVisibility(View.GONE);

                headerHolder.title.setText(Utilities.replaceHTMLTags(submission.getTitle()));
                headerHolder.comments.setText(submission.getComments());

                headerHolder.score.setText(submission.getScore());
                if(submission.getVote().contentEquals(VoteDirection.UPVOTE.name()))
                    headerHolder.score.setTextColor(ContextCompat.getColor(mContext, R.color.textUpvoted));
                else if(submission.getVote().contentEquals(VoteDirection.DOWNVOTE.name()))
                    headerHolder.score.setTextColor(ContextCompat.getColor(mContext, R.color.textDownvoted));
                else if(submission.getVote().contentEquals(VoteDirection.NO_VOTE.name()))
                    headerHolder.score.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryText));

                if (submission.getSelfText() != null) {
                    headerHolder.headerLinkLayout.setVisibility(View.GONE);
                    headerHolder.selfText.setVisibility(View.VISIBLE);
                    headerHolder.selfText.setText(Utilities.getHTMLFromMarkdown(submission.getSelfText()));
                    headerHolder.selfText.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                        @Override
                        public void onClick(String url) {
                            LinkHandler.analyse(mContext, url);
                        }
                    });
                } else if (!submission.isSelfPost()) {
                    headerHolder.headerLinkLayout.setVisibility(View.VISIBLE);
                    headerHolder.selfText.setVisibility(View.GONE);
                    headerHolder.link.setText(Utilities.replaceHTMLTags(submission.getLink()));

                    headerHolder.headerLinkLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LinkHandler.analyse(mContext, Utilities.replaceHTMLTags(submission.getLink()));
                        }
                    });
                } else {
                    headerHolder.headerLinkLayout.setVisibility(View.GONE);
                    headerHolder.selfText.setVisibility(View.GONE);
                }

                if (this.commentNodes.size() == 0)
                    headerHolder.progressBar.setVisibility(View.VISIBLE);
                else
                    headerHolder.progressBar.setVisibility(View.GONE);

            }
        } else if (holder instanceof NoticeHolder) {

            NoticeHolder noticeHolder = (NoticeHolder) holder;
            noticeHolder.noticeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (loadAllCommentsListener != null) {
                        loadAllCommentsListener.onLoadAllComments();
                    }
                }
            });

        } else if (holder instanceof CommentHolder) {
            final CommentHolder commentHolder = (CommentHolder) holder;
            final int currentVotePosition = isSingleCommentNoticeShown ? position - 2 : position - 1;
            final CommentNode node = commentNodes.get(currentVotePosition);
            final Comment comment = node.getComment();

            commentHolder.commentLayout.setHapticFeedbackEnabled(false);
            commentHolder.commentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String[] options = mContext.getResources().getStringArray(R.array.comment_context_menu);
                    if (votes.get(currentVotePosition) == VoteDirection.UPVOTE)
                        options[0] = "Remove Vote";
                    else if (votes.get(currentVotePosition) == VoteDirection.DOWNVOTE)
                        options[1] = "Remove Vote";

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, options), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Up, Down, Reply, CopyContent, CopyPerma
                            switch (which) {
                                case 0:

                                    if (votes.get(currentVotePosition) == VoteDirection.UPVOTE) {
                                        votes.remove(currentVotePosition);
                                        voteComment(comment, VoteDirection.NO_VOTE);
                                        votes.add(currentVotePosition, VoteDirection.NO_VOTE);
                                    } else {
                                        votes.remove(currentVotePosition);
                                        voteComment(comment, VoteDirection.UPVOTE);
                                        votes.add(currentVotePosition, VoteDirection.UPVOTE);
                                    }
                                    notifyItemChanged(commentHolder.getAdapterPosition());

                                    break;

                                case 1:

                                    if (votes.get(currentVotePosition) == VoteDirection.DOWNVOTE) {
                                        votes.remove(currentVotePosition);
                                        voteComment(comment, VoteDirection.NO_VOTE);
                                        votes.add(currentVotePosition, VoteDirection.NO_VOTE);
                                    } else {
                                        votes.remove(currentVotePosition);
                                        voteComment(comment, VoteDirection.DOWNVOTE);
                                        votes.add(currentVotePosition, VoteDirection.DOWNVOTE);
                                    }
                                    notifyItemChanged(commentHolder.getAdapterPosition());
                                    break;

                                case 2:
                                    replyToCommentListener.startReplyToComment(comment);
                                    break;

                                case 3:

                                    String commentBody = Utilities.getHTMLFromMarkdown(comment.data("body_html")).toString();
                                    ClipData bodyData = ClipData.newPlainText("Respite.commentBody", commentBody);
                                    ((ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(bodyData);
                                    Toast.makeText(mContext, R.string.clipboard_copy_success, Toast.LENGTH_SHORT).show();
                                    break;

                                case 4:

                                    String permalink = "https://www.reddit.com" + Utilities.replaceHTMLTags(submission.getPermalink() + comment.getId());
                                    ClipData linkData = ClipData.newPlainText("Respite.commentPermalink", permalink);
                                    ((ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(linkData);
                                    Toast.makeText(mContext, R.string.clipboard_copy_success, Toast.LENGTH_SHORT).show();
                                    break;

                            }
                        }
                    });
                    builder.create().show();
                    return false;
                }
            });

            commentHolder.body.setText(Utilities.getHTMLFromMarkdown(comment.data("body_html")));
            commentHolder.body.setLinkClickedListener(new ActiveTextView.OnLinkClickedListener() {
                @Override
                public void onClick(String url) {
                    LinkHandler.analyse(mContext, url);
                }
            });
            commentHolder.body.setLongPressedLinkListener(new ActiveTextView.OnLongPressedLinkListener() {
                @Override
                public void onLongPressed() {
                    // do nothing
                }
            }, true);

            commentHolder.author.setText(comment.getAuthor());
            if (threadOP != null && comment.getAuthor().contentEquals(threadOP))
                commentHolder.author.setTextColor(ContextCompat.getColor(mContext, R.color.distinguishedOP));
            else if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR)
                commentHolder.author.setTextColor(ContextCompat.getColor(mContext, R.color.distinguishedMod));
            else if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN 
                    || comment.getDistinguishedStatus() == DistinguishedStatus.SPECIAL)
                commentHolder.author.setTextColor(ContextCompat.getColor(mContext, R.color.distinguishedAdmin));
            else
                commentHolder.author.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryText));
            final String authorURL = "/u/" + comment.getAuthor();
            commentHolder.author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkHandler.analyse(mContext, authorURL);
                }
            });

            commentHolder.timeCreated.setText(Utilities.getReadableCreationTime(comment.getCreated()));

            if (comment.getAuthorFlair() != null
                    && comment.getAuthorFlair().getText() != null
                    && comment.getAuthorFlair().getText().length() > 0) {
                commentHolder.flair.setText(Utilities.getEscapedHTML(comment.getAuthorFlair().getText()));
                commentHolder.flair.setVisibility(View.VISIBLE);
            } else
                commentHolder.flair.setVisibility(View.GONE);

            if (comment.isScoreHidden())
                commentHolder.score.setText("??");
            else
                commentHolder.score.setText(String.valueOf(comment.getScore()));

            if (votes.get(currentVotePosition) == VoteDirection.UPVOTE)
                commentHolder.score.setTextColor(ContextCompat.getColor(mContext, R.color.textUpvoted));
            else if (votes.get(currentVotePosition) == VoteDirection.DOWNVOTE)
                commentHolder.score.setTextColor(ContextCompat.getColor(mContext, R.color.textDownvoted));
            else
                commentHolder.score.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryText));

            if (comment.getTimesGilded() > 0) {
                commentHolder.gildedCount.setText(mContext.getString(R.string.comment_gilded_count, comment.getTimesGilded()));
                commentHolder.gildedCount.setVisibility(View.VISIBLE);
            } else
                commentHolder.gildedCount.setVisibility(View.GONE);

            if (comment.hasBeenEdited()) {
                commentHolder.timeEdited.setText(mContext.getString(R.string.comment_edited, Utilities.getReadableCreationTime(comment.getEditDate())));
                commentHolder.timeEdited.setVisibility(View.VISIBLE);
            } else
                commentHolder.timeEdited.setVisibility(View.GONE);

            int depth = node.getDepth() - 1;
            ViewGroup.LayoutParams params = commentHolder.commentIndent.getLayoutParams();
            params.width = Utilities.getPixelsFromDPs(mContext, depth * 5);
            commentHolder.commentIndent.setLayoutParams(params);
            if (depth % 2 == 0)
                commentHolder.commentMarker.setVisibility(View.GONE);
            else
                commentHolder.commentMarker.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return commentNodes.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;
        else if (isSingleCommentNoticeShown) {
            if (position == 1) return TYPE_NOTICE;
            return TYPE_COMMENT;
        }
        return TYPE_COMMENT;
    }

    private void voteComment(Comment c, final VoteDirection direction) {
        new AsyncTask<Comment, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Comment... params) {
                Comment c = params[0];
                try {
                    (new AccountManager(mRedditClient)).vote(c, direction);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (!success)
                    Toast.makeText(mContext, R.string.submissionsactivity_voteerror, Toast.LENGTH_LONG).show();
            }
        }.execute(c);
    }

    public void setLoadAllCommentsListener(LoadAllCommentsListener l) {
        this.loadAllCommentsListener = l;
    }

    public void setReplyToCommentListener(ReplyToCommentListener l) {
        this.replyToCommentListener = l;
    }

    public interface LoadAllCommentsListener {
        void onLoadAllComments();
    }

    public interface ReplyToCommentListener {
        void startReplyToComment(Comment c);
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        FlowLayout headerDescription;
        View headerLinkLayout;
        ActiveTextView selfText;
        TextView author, subreddit, timeCreated, flairTag, nsfwTag, stickyTag, domain, link;
        TextView title, comments, score;
        ProgressBar progressBar;

        HeaderHolder(LinearLayout v) {
            super(v);
            this.headerLinkLayout = v.findViewById(R.id.comments_header_link_layout);
            this.selfText = (ActiveTextView) v.findViewById(R.id.comments_header_selftext);
            this.headerDescription = (FlowLayout) v.findViewById(R.id.comments_header_description);
            this.author = (TextView) v.findViewById(R.id.comments_header_author);
            this.subreddit = (TextView) v.findViewById(R.id.comments_header_subreddit);
            this.timeCreated = (TextView) v.findViewById(R.id.comments_header_timecreated);
            this.flairTag = (TextView) v.findViewById(R.id.comments_header_flair_tag);
            this.nsfwTag = (TextView) v.findViewById(R.id.comments_header_nsfw_tag);
            this.stickyTag = (TextView) v.findViewById(R.id.comments_header_sticky_tag);
            this.domain = (TextView) v.findViewById(R.id.comments_header_domain);
            this.link = (TextView) v.findViewById(R.id.comments_header_link);
            this.title = (TextView) v.findViewById(R.id.comments_header_title);
            this.comments = (TextView) v.findViewById(R.id.comments_header_comments);
            this.score = (TextView) v.findViewById(R.id.comments_header_score);
            this.progressBar = (ProgressBar) v.findViewById(R.id.comments_header_progressbar);
        }
    }

    static class NoticeHolder extends RecyclerView.ViewHolder {
        LinearLayout noticeLayout;

        NoticeHolder(LinearLayout v) {
            super(v);
            this.noticeLayout = (LinearLayout) v.findViewById(R.id.comments_single_thread_alert);
        }
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        TextView author, timeCreated, timeEdited, flair, gildedCount, score;
        ActiveTextView body;
        View commentIndent, commentMarker;
        LinearLayout commentLayout;
        LinearLayout view;

        CommentHolder(LinearLayout v) {
            super(v);
            this.author = (TextView) v.findViewById(R.id.list_item_comment_author);
            this.timeCreated = (TextView) v.findViewById(R.id.list_item_comment_timecreated);
            this.timeEdited = (TextView) v.findViewById(R.id.list_item_comment_timeedited);
            this.flair = (TextView) v.findViewById(R.id.list_item_comment_flair);
            this.gildedCount = (TextView) v.findViewById(R.id.list_item_comment_gilded_count);
            this.score = (TextView) v.findViewById(R.id.list_item_comment_score);
            this.body = (ActiveTextView) v.findViewById(R.id.list_item_comment_body);
            this.commentIndent = v.findViewById(R.id.list_item_comment_indent);
            this.commentMarker = v.findViewById(R.id.list_item_comment_marker);
            this.commentLayout = (LinearLayout) v.findViewById(R.id.list_item_comment_layout);
            this.view = v;
        }
    }
}
