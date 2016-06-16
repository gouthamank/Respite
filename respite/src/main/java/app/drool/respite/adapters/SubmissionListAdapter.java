package app.drool.respite.adapters;

import android.content.Context;
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
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/15/16.
 */


public class SubmissionListAdapter extends RecyclerView.Adapter<SubmissionListAdapter.SubmissionHolder> {

    private LinkedList<Submission> submissions = null;
    private Context mContext = null;

    static class SubmissionHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView title;
        TextView comments;
        TextView score;
        ImageView preview;

        RelativeLayout view;

        SubmissionHolder(RelativeLayout v){
            super(v);
            this.description = (TextView) v.findViewById(R.id.list_item_submission_description);
            this.title = (TextView) v.findViewById(R.id.list_item_submission_title);
            this.comments = (TextView) v.findViewById(R.id.list_item_submission_comments);
            this.score = (TextView) v.findViewById(R.id.list_item_submission_score);
            this.preview = (ImageView) v.findViewById(R.id.list_item_submission_preview);

            this.view = v;
        }
    }

    public SubmissionListAdapter(Context mContext){
        this.mContext = mContext;
        this.submissions = new LinkedList<>();
    }

    public SubmissionListAdapter(Context mContext, LinkedList<Submission> submissions) {
        this.mContext = mContext;
        this.submissions = submissions;
    }

    public SubmissionListAdapter(Context mContext, Listing<Submission> submissions) {
        this.mContext = mContext;
        for(Submission s : submissions)
            this.submissions.add(s);
    }

    public void addSubmissions(Submission s) {
        this.submissions.add(s);
        notifyDataSetChanged();
    }

    public void addSubmissions(Listing<Submission> submissions) {
        for(Submission s : submissions)
            this.submissions.add(s);

        notifyDataSetChanged();
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
        String description = submission.getAuthor() +
                " • " +
                submission.getSubredditName() +
                " • " +
                Utilities.getReadableCreationTime(submission.getCreated());

        holder.description.setText(description);
        holder.title.setText(submission.getTitle());
        holder.score.setText(String.valueOf(submission.getScore()));
        String commentCount = mContext.getResources().getQuantityString(R.plurals.submission_comments, submission.getCommentCount(), submission.getCommentCount());
        holder.comments.setText(commentCount);

        Thumbnails thumbnails = submission.getThumbnails();
        String thumbnailURL = null;
        if(thumbnails != null) {
            Image[] variations = thumbnails.getVariations();
            if(variations.length > 1)
                thumbnailURL = variations[0].getUrl();
        }

        if(thumbnailURL == null)
            holder.preview.setVisibility(ImageView.GONE);
        else
            holder.preview.setVisibility(ImageView.VISIBLE);

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
            }
        });

    }

    @Override
    public int getItemCount() {
        return submissions.size();
    }


}


