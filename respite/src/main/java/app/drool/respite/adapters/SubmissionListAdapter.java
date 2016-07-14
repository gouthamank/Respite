package app.drool.respite.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.dean.jraw.RedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thumbnails;
import net.dean.jraw.models.Thumbnails.Image;
import net.dean.jraw.models.VoteDirection;

import java.util.LinkedList;

import app.drool.respite.R;
import app.drool.respite.activities.CommentsActivity;
import app.drool.respite.asyncloaders.PreviewFromCacheTask;
import app.drool.respite.asyncloaders.PreviewFromURLTask;
import app.drool.respite.handlers.LinkHandler;
import app.drool.respite.impl.SubmissionParcelable;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/15/16.
 */


public class SubmissionListAdapter extends RecyclerView.Adapter<SubmissionListAdapter.SubmissionHolder> {

    private LinkedList<Submission> submissions = null;
    private Context mContext = null;
    private RedditClient mRedditClient = null;
    private LinkedList<Integer> votes = null;
    private boolean isAuthorClickable = true;
    private boolean isSubredditClickable = true;

    public SubmissionListAdapter(Context mContext, RedditClient mRedditClient) {
        this.mContext = mContext;
        this.submissions = new LinkedList<>();
        this.votes = new LinkedList<>();
        this.mRedditClient = mRedditClient;
    }

    private static boolean cancelPotentialWorkFromCache(String submissionID, ImageView preview) {
        final PreviewFromCacheTask previewFromCacheTask = Utilities.getPreviewFromCacheTask(preview);

        if (previewFromCacheTask != null) {
            final String id = previewFromCacheTask.submissionID;
            if (id == null || !id.equals(submissionID)) {
                previewFromCacheTask.cancel(true);
            } else {
                return false;
            }
        }

        return true;
    }

    private static boolean cancelPotentialWorkFromURL(String submissionID, ImageView preview) {
        final PreviewFromURLTask previewFromURLTask = Utilities.getPreviewFromURLTask(preview);

        if (previewFromURLTask != null) {
            final String id = previewFromURLTask.submissionID;
            if (id == null || !id.equals(submissionID)) {
                previewFromURLTask.cancel(true);
            } else {
                return false;
            }
        }

        return true;
    }

    public void clearSubmissions() {
        this.submissions.clear();
        notifyDataSetChanged();
    }

    public void addSubmissions(Submission s) {
        this.submissions.add(s);
        this.votes.add(s.getVote().getValue());

        notifyItemInserted(this.submissions.size() - 1);
    }

    public void addSubmissions(Listing<Submission> submissions) {
        int latestIndex = this.submissions.size();

        for (Submission s : submissions) {
            this.submissions.add(s);
            this.votes.add(s.getVote().getValue());
            notifyItemInserted(latestIndex);
            latestIndex++;
        }
    }

    @Override
    public SubmissionListAdapter.SubmissionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_submission, parent, false);
        RelativeLayout view = (RelativeLayout) v;
        return new SubmissionHolder(view);
    }

    @Override
    public void onBindViewHolder(final SubmissionListAdapter.SubmissionHolder holder, final int position) {
        final Submission submission = submissions.get(position);
        final SubmissionParcelable submissionParcelable = new SubmissionParcelable(mContext, submission);

        holder.author.setText(submissionParcelable.getAuthor());
        holder.subreddit.setText(submissionParcelable.getSubreddit());
        holder.timeCreated.setText(submissionParcelable.getTimeCreated());
        holder.domain.setText(submissionParcelable.getDomain());
        if (submissionParcelable.getLinkFlair() != null) {
            holder.flairTag.setVisibility(TextView.VISIBLE);
            holder.flairTag.setText(submissionParcelable.getLinkFlair());
        } else
            holder.flairTag.setVisibility(TextView.GONE);
        holder.author.setText(submissionParcelable.getAuthor());
        holder.title.setText(submissionParcelable.getTitle());

        holder.score.setText(submissionParcelable.getScore());
        if (votes.get(position) == VoteDirection.UPVOTE.getValue())
            holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.textUpvoted));
        if (votes.get(position) == VoteDirection.DOWNVOTE.getValue())
            holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.textDownvoted));
        if (votes.get(position) == VoteDirection.NO_VOTE.getValue())
            holder.score.setTextColor(ContextCompat.getColor(mContext, R.color.secondaryText));

        holder.comments.setText(submissionParcelable.getComments());

        if (submissionParcelable.isNSFW())
            holder.nsfwTag.setVisibility(View.VISIBLE);
        else
            holder.nsfwTag.setVisibility(View.GONE);

        if (submissionParcelable.isStickied())
            holder.stickyTag.setVisibility(View.VISIBLE);
        else
            holder.stickyTag.setVisibility(View.GONE);

        Thumbnails thumbnails = submission.getThumbnails();
        String thumbnailURL = null;
        if (thumbnails != null) {
            Image[] variations = thumbnails.getVariations();
            if (variations.length > 1)
                thumbnailURL = variations[0].getUrl();
        }

        if (thumbnailURL == null)
            holder.preview.setVisibility(ImageView.GONE);
        else {
            holder.preview.setVisibility(ImageView.VISIBLE);
            loadPreview(submission.getId(), holder.preview, Utilities.replaceHTMLTags(thumbnailURL));
        }

        holder.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkHandler.analyse(mContext, Utilities.replaceHTMLTags(submission.getUrl()));
            }
        });

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentsIntent = new Intent(mContext, CommentsActivity.class);
                commentsIntent.putExtra("top", submissionParcelable);
                mContext.startActivity(commentsIntent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentsIntent = new Intent(mContext, CommentsActivity.class);
                commentsIntent.putExtra("top", submissionParcelable);
                mContext.startActivity(commentsIntent);
            }
        });
        if (isAuthorClickable) {
            holder.author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkHandler.analyse(mContext, "/u/" + submissionParcelable.getAuthor());
                }
            });
        }

        if (isSubredditClickable) {
            holder.subreddit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkHandler.analyse(mContext, "/r/" + submissionParcelable.getSubreddit());
                }
            });
        }

        holder.upvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (votes.get(holder.getAdapterPosition()) == VoteDirection.UPVOTE.getValue()) {
                    votes.remove(holder.getAdapterPosition());
                    voteSubmission(submission, VoteDirection.NO_VOTE);
                    votes.add(holder.getAdapterPosition(), 0);
                } else {
                    votes.remove(holder.getAdapterPosition());
                    voteSubmission(submission, VoteDirection.UPVOTE);
                    votes.add(holder.getAdapterPosition(), 1);
                }
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.downvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (votes.get(holder.getAdapterPosition()) == VoteDirection.DOWNVOTE.getValue()) {
                    votes.remove(holder.getAdapterPosition());
                    voteSubmission(submission, VoteDirection.NO_VOTE);
                    votes.add(holder.getAdapterPosition(), 0);
                } else {
                    votes.remove(holder.getAdapterPosition());
                    voteSubmission(submission, VoteDirection.DOWNVOTE);
                    votes.add(holder.getAdapterPosition(), -1);
                }
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    private void loadPreview(final String submissionID, final ImageView preview, final String thumbnailURL) {
        preview.setBackgroundResource(android.R.color.transparent);
        Picasso.with(mContext).load(thumbnailURL).networkPolicy(NetworkPolicy.OFFLINE)  //Trying Picasso
                .into(preview, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso.with(mContext).load(thumbnailURL).into(preview);
                    }
                });

        /*
        if (CacheWrapper.hasPreview(mContext.getCacheDir(), submissionID)) {
            if (cancelPotentialWorkFromCache(submissionID, preview)) {
                final PreviewFromCacheTask task = new PreviewFromCacheTask(mContext.getCacheDir(), submissionID, preview);
                Bitmap placeholder = BitmapFactory.decodeResource(mContext.getResources(), R.color.colorAccent);
                final AsyncDrawableCache asyncDrawableCache = new AsyncDrawableCache(mContext.getResources(), placeholder, task);
                preview.setImageDrawable(asyncDrawableCache);
                task.execute();
            }
        } else {
            if (cancelPotentialWorkFromURL(submissionID, preview)) {
                final PreviewFromURLTask task = new PreviewFromURLTask(mContext.getCacheDir(), submissionID, preview, thumbnailURL);
                Bitmap placeholder = BitmapFactory.decodeResource(mContext.getResources(), R.color.colorAccent);
                final AsyncDrawableURL asyncDrawableURL = new AsyncDrawableURL(mContext.getResources(), placeholder, task);
                preview.setImageDrawable(asyncDrawableURL);
                task.execute();
            }
        } */
    }

    private void voteSubmission(Submission s, final VoteDirection direction) {
        new AsyncTask<Submission, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Submission... params) {
                Submission s = params[0];
                try {
                    (new AccountManager(mRedditClient)).vote(s, direction);
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
        }.execute(s);
    }

    public void disableAuthorClickable() {
        this.isAuthorClickable = false;
    }

    public void disableSubredditClickable() {
        this.isSubredditClickable = false;
    }

    static class SubmissionHolder extends RecyclerView.ViewHolder {
        TextView author, subreddit, timeCreated, domain, flairTag, nsfwTag, stickyTag;
        CardView cardView;
        TextView title;
        TextView comments;
        TextView score;
        ImageView preview;

        ImageView upvote, downvote;

        RelativeLayout view;

        SubmissionHolder(RelativeLayout v) {
            super(v);
            this.cardView = (CardView) v.findViewById(R.id.list_item_submission_cardview);
            this.author = (TextView) v.findViewById(R.id.list_item_submission_author);
            this.subreddit = (TextView) v.findViewById(R.id.list_item_submission_subreddit);
            this.timeCreated = (TextView) v.findViewById(R.id.list_item_submission_timecreated);
            this.domain = (TextView) v.findViewById(R.id.list_item_submission_domain);
            this.flairTag = (TextView) v.findViewById(R.id.list_item_submission_flair_tag);
            this.nsfwTag = (TextView) v.findViewById(R.id.list_item_submission_nsfw_tag);
            this.stickyTag = (TextView) v.findViewById(R.id.list_item_submission_sticky_tag);

            this.title = (TextView) v.findViewById(R.id.list_item_submission_title);
            this.comments = (TextView) v.findViewById(R.id.list_item_submission_comments);
            this.score = (TextView) v.findViewById(R.id.list_item_submission_score);
            this.preview = (ImageView) v.findViewById(R.id.list_item_submission_preview);
            this.upvote = (ImageView) v.findViewById(R.id.list_item_submission_upvote);
            this.downvote = (ImageView) v.findViewById(R.id.list_item_submission_downvote);

            this.view = v;
        }
    }

}


