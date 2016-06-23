package app.drool.respite.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Thumbnails;
import net.dean.jraw.models.Thumbnails.Image;

import java.util.LinkedList;

import app.drool.respite.R;
import app.drool.respite.activities.CommentsActivity;
import app.drool.respite.asyncloaders.AsyncDrawableCache;
import app.drool.respite.asyncloaders.AsyncDrawableURL;
import app.drool.respite.asyncloaders.PreviewFromCacheTask;
import app.drool.respite.asyncloaders.PreviewFromURLTask;
import app.drool.respite.cache.CacheWrapper;
import app.drool.respite.impl.SubmissionParcelable;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/15/16.
 */


public class SubmissionListAdapter extends RecyclerView.Adapter<SubmissionListAdapter.SubmissionHolder> {

    private static final int ENDLESS_SCROLL_THRESHOLD = 10;
    private LinkedList<Submission> submissions = null;
    private Context mContext = null;
    private EndlessScrollListener endlessScrollListener;
    public SubmissionListAdapter(Context mContext) {
        this.mContext = mContext;
        this.submissions = new LinkedList<>();
    }

    public SubmissionListAdapter(Context mContext, LinkedList<Submission> submissions) {
        this.mContext = mContext;
        this.submissions = submissions;
    }

    public SubmissionListAdapter(Context mContext, Listing<Submission> submissions) {
        this.mContext = mContext;
        for (Submission s : submissions)
            this.submissions.add(s);
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

    public void addSubmissions(Submission s) {
        this.submissions.add(s);
        notifyDataSetChanged();
    }

    public void addSubmissions(Listing<Submission> submissions) {
        int latestIndex = this.submissions.size();

        for (Submission s : submissions) {
            this.submissions.add(s);
            notifyItemInserted(latestIndex);
            latestIndex++;
        }
    }

    public void setEndlessScrollListener(EndlessScrollListener listener) {
        this.endlessScrollListener = listener;
    }

    @Override
    public SubmissionListAdapter.SubmissionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_submission, parent, false);
        RelativeLayout view = (RelativeLayout) v;
        return new SubmissionHolder(view);
    }

    @Override
    public void onBindViewHolder(SubmissionListAdapter.SubmissionHolder holder, int position) {
        final Submission submission = submissions.get(position);
        final SubmissionParcelable submissionParcelable = new SubmissionParcelable(mContext, submission);

        holder.description.setText(submissionParcelable.getDescription());
        holder.title.setText(submissionParcelable.getTitle());
        holder.score.setText(submissionParcelable.getScore());
        holder.comments.setText(submissionParcelable.getComments());

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
            loadPreview(submission.getId(), holder.preview, thumbnailURL.replace("&amp;", "&"));
        }

        holder.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, submission.getSubredditId(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, submission.getAuthor(), Toast.LENGTH_SHORT).show();
                Intent commentsInstent = new Intent(mContext, CommentsActivity.class);
                commentsInstent.putExtra("top", submissionParcelable);
                mContext.startActivity(commentsInstent);
            }
        });

        if (position == getItemCount() - ENDLESS_SCROLL_THRESHOLD) {
            if (endlessScrollListener != null)
                endlessScrollListener.onLoadMore(position);
        }

    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }

    private void loadPreview(final String submissionID, ImageView preview, final String thumbnailURL) {
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
        }
    }

    public interface EndlessScrollListener {
        void onLoadMore(int position);
    }

    static class SubmissionHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView title;
        TextView comments;
        TextView score;
        ImageView preview;

        RelativeLayout view;

        SubmissionHolder(RelativeLayout v) {
            super(v);
            this.description = (TextView) v.findViewById(R.id.list_item_submission_description);
            this.title = (TextView) v.findViewById(R.id.list_item_submission_title);
            this.comments = (TextView) v.findViewById(R.id.list_item_submission_comments);
            this.score = (TextView) v.findViewById(R.id.list_item_submission_score);
            this.preview = (ImageView) v.findViewById(R.id.list_item_submission_preview);

            this.view = v;
        }
    }
}


