package app.drool.respite.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.laurencedawson.activetextview.ActiveTextView;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Listing;

import java.util.LinkedList;

import app.drool.respite.R;
import app.drool.respite.handlers.LinkHandler;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 7/1/16.
 */

public class CommentContributionListAdapter extends RecyclerView.Adapter<CommentContributionListAdapter.CommentHolder> {

    private LinkedList<Comment> comments = null;
    private Context mContext = null;

    public CommentContributionListAdapter(Context mContext) {
        this.mContext = mContext;
        this.comments = new LinkedList<>();
    }

    public CommentContributionListAdapter(Context mContext, LinkedList<Comment> comments) {
        this.mContext = mContext;
        this.comments = comments;
    }

    public CommentContributionListAdapter(Context mContext, Listing<Comment> comments) {
        this.mContext = mContext;
        for (Comment c : comments)
            this.comments.add(c);
    }

    public void clearComments() {
        this.comments.clear();
        notifyDataSetChanged();
    }

    public void addComments(Comment c) {
        this.comments.add(c);
        notifyItemInserted(this.comments.size() - 1);
    }

    public void addComments(Listing<Comment> comments) {
        int latestIndex = this.comments.size();

        for (Comment c : comments) {
            this.comments.add(c);
            notifyItemInserted(latestIndex);
            latestIndex++;
        }
    }

    @Override
    public CommentContributionListAdapter.CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment_contribution, parent, false);
        LinearLayout view = (LinearLayout) v;
        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentContributionListAdapter.CommentHolder holder, int position) {
        final Comment comment = comments.get(position);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkHandler.analyse(mContext, "https://www.reddit.com/r/subreddit/comments/"
                                                + comment.getSubmissionId().substring(3)
                                                + "/title/"
                                                + comment.getId());
            }
        });

        holder.body.setText(Utilities.getHTMLFromMarkdown(comment.data("body_html")));
        holder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkHandler.analyse(mContext, "https://www.reddit.com/r/subreddit/comments/"
                        + comment.getSubmissionId().substring(3)
                        + "/title/"
                        + comment.getId());
            }
        });
        holder.author.setText(comment.getAuthor());

        holder.title.setText(comment.getSubmissionTitle().replace("&amp;", "&").replace("&gt;", ">").replace("&lt;", "<")
                        + " (/r/" + comment.getSubredditName() +")");
        holder.timeCreated.setText(Utilities.getReadableCreationTime(comment.getCreated()));

        if (comment.getAuthorFlair() != null
                && comment.getAuthorFlair().getText() != null
                && comment.getAuthorFlair().getText().length() > 0) {
            holder.flair.setText(comment.getAuthorFlair().getText());
            holder.flair.setVisibility(View.VISIBLE);
        }
        else
            holder.flair.setVisibility(View.GONE);

        if (comment.isScoreHidden())
            holder.score.setText("??");
        else
            holder.score.setText(String.valueOf(comment.getScore()));

        if (comment.getTimesGilded() > 0) {
            holder.gildedCount.setText(mContext.getString(R.string.comment_gilded_count, comment.getTimesGilded()));
            holder.gildedCount.setVisibility(View.VISIBLE);
        } else
            holder.gildedCount.setVisibility(View.GONE);

        if (comment.hasBeenEdited()) {
            holder.timeEdited.setText(mContext.getString(R.string.comment_edited, Utilities.getReadableCreationTime(comment.getEditDate())));
            holder.timeEdited.setVisibility(View.VISIBLE);
        } else
            holder.timeEdited.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentHolder extends RecyclerView.ViewHolder {
        TextView author, timeCreated, timeEdited, flair, gildedCount, score;
        ActiveTextView body;
        TextView title;

        LinearLayout view;

        CommentHolder(LinearLayout v) {
            super(v);
            this.author = (TextView) v.findViewById(R.id.list_item_comment_contribution_author);
            this.timeCreated = (TextView) v.findViewById(R.id.list_item_comment_contribution_timecreated);
            this.timeEdited = (TextView) v.findViewById(R.id.list_item_comment_contribution_timeedited);
            this.flair = (TextView) v.findViewById(R.id.list_item_comment_contribution_flair);
            this.gildedCount = (TextView) v.findViewById(R.id.list_item_comment_contribution_gilded_count);
            this.title = (TextView) v.findViewById(R.id.list_item_comment_contribution_title);
            this.score = (TextView) v.findViewById(R.id.list_item_comment_contribution_score);
            this.body = (ActiveTextView) v.findViewById(R.id.list_item_comment_contribution_body);

            this.view = v;
        }
    }
}
