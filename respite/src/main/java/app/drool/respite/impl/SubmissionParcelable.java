package app.drool.respite.impl;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import net.dean.jraw.models.Submission;

import app.drool.respite.R;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/18/16.
 */

public class SubmissionParcelable implements Parcelable {
    private String description;
    private String title;
    private String score;
    private String comments;
    private String selfText;
    private String submissionID;
    private String subreddit;

    public SubmissionParcelable(Context mContext, Submission s) {
        this.description = s.getAuthor() +
                " • " +
                s.getSubredditName() +
                " • " +
                Utilities.getReadableCreationTime(s.getCreated());
        this.title = s.getTitle().replace("&amp;", "&").replace("&gt;", ">").replace("&lt;", "<");
        this.score = String.valueOf(s.getScore());
        this.comments = mContext.getResources()
                                .getQuantityString(R.plurals.submission_comments, s.getCommentCount(), s.getCommentCount());
        this.selfText = s.data("selftext_html");
        this.submissionID = s.getId();
        this.subreddit = s.getSubredditName();
    }

    private SubmissionParcelable(Parcel in) {
        this.description = in.readString();
        this.title = in.readString();
        this.score = in.readString();
        this.comments = in.readString();
        this.selfText = in.readString();
        this.submissionID = in.readString();
        this.subreddit = in.readString();
    }

    public static final Creator<SubmissionParcelable> CREATOR = new Creator<SubmissionParcelable>() {
        @Override
        public SubmissionParcelable createFromParcel(Parcel in) {
            return new SubmissionParcelable(in);
        }

        @Override
        public SubmissionParcelable[] newArray(int size) {
            return new SubmissionParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(title);
        dest.writeString(score);
        dest.writeString(comments);
        dest.writeString(selfText);
        dest.writeString(submissionID);
        dest.writeString(subreddit);
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getScore() {
        return score;
    }

    public String getComments() {
        return comments;
    }

    public String getSelfText() {
        return selfText;
    }

    public String getSubmissionID() {
         return submissionID;
     }

    public String getSubreddit() {
        return subreddit;
    }
}
