package app.drool.respite.impl;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.models.Submission;

import app.drool.respite.R;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/18/16.
 */

public class SubmissionParcelable implements Parcelable {
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
    private String title;
    private String score;
    private String comments;
    private String selfText;
    private String submissionID;
    private String subreddit;
    private String link;

    private String author, timeCreated, domain, linkFlair;
    private int isNSFW, isStickied, isSelfPost;

    public SubmissionParcelable(Context mContext, JsonNode node) {
        this(mContext, new Submission(node));
    }

    public SubmissionParcelable(Context mContext, Submission s) {
        this.title = s.getTitle().replace("&amp;", "&").replace("&gt;", ">").replace("&lt;", "<");
        this.score = String.valueOf(s.getScore());
        this.comments = mContext.getResources()
                .getQuantityString(R.plurals.submission_comments, s.getCommentCount(), s.getCommentCount());
        this.selfText = s.data("selftext_html");
        this.submissionID = s.getId();
        this.author = s.getAuthor();
        this.subreddit = s.getSubredditName();
        this.link = s.getUrl();

        this.timeCreated = Utilities.getReadableCreationTime(s.getCreated());
        this.domain = s.getDomain();
        this.linkFlair = s.getSubmissionFlair().getText();
        this.isNSFW = s.isNsfw() ? 1 : 0;
        this.isStickied = s.isStickied() ? 1 : 0;
        this.isSelfPost = s.isSelfPost() ? 1 : 0;
    }

    private SubmissionParcelable() {
        // nothing. used for newDummyInstance()
    }

    private SubmissionParcelable(Parcel in) {
        this.title = in.readString();
        this.score = in.readString();
        this.comments = in.readString();
        this.selfText = in.readString();
        this.submissionID = in.readString();
        this.subreddit = in.readString();
        this.timeCreated = in.readString();
        this.domain = in.readString();
        this.linkFlair = in.readString();
        this.link = in.readString();
        this.author = in.readString();
        this.isNSFW = in.readInt();
        this.isStickied = in.readInt();
        this.isSelfPost = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(score);
        dest.writeString(comments);
        dest.writeString(selfText);
        dest.writeString(submissionID);
        dest.writeString(subreddit);
        dest.writeString(timeCreated);
        dest.writeString(domain);
        dest.writeString(linkFlair);
        dest.writeString(link);
        dest.writeString(author);

        dest.writeInt(isNSFW);
        dest.writeInt(isStickied);
        dest.writeInt(isSelfPost);
    }

    public String getLink() {
        return link;
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

    public String getAuthor() {
        return author;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public String getDomain() {
        return domain;
    }

    public String getLinkFlair() {
        return linkFlair;
    }

    public boolean isNSFW() {
        return isNSFW == 1;
    }

    public boolean isStickied() {
        return isStickied == 1;
    }

    public boolean isSelfPost() {
        return isSelfPost == 1;
    }
}


