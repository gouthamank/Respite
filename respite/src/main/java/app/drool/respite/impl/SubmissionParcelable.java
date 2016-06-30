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

@SuppressWarnings("WeakerAccess")
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
    private String commentID;
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
        this.isNSFW = in.readInt();
        this.isStickied = in.readInt();
        this.isSelfPost = in.readInt();
    }

    public static SubmissionParcelable newDummyInstance() {
        return new SubmissionParcelable() {{
            setTitle(" ");
            setComments(" ");
            setSelfText(null);
            setSubmissionID(" ");
            setSubreddit(" ");
            setScore(" ");
            setAuthor(" ");
            setTimeCreated(" ");
            setDomain(" ");
            setLinkFlair(" ");
            setIsSelfPost(1);
        }};
    }

    public int getIsSelfPost() {
        return isSelfPost;
    }

    public void setIsSelfPost(int isSelfPost) {
        this.isSelfPost = isSelfPost;
    }

    public int getIsNSFW() {
        return isNSFW;
    }

    public void setIsNSFW(int isNSFW) {
        this.isNSFW = isNSFW;
    }

    public int getIsStickied() {
        return isStickied;
    }

    public void setIsStickied(int isStickied) {
        this.isStickied = isStickied;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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

        dest.writeInt(isNSFW);
        dest.writeInt(isStickied);
        dest.writeInt(isSelfPost);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getSelfText() {
        return selfText;
    }

    public void setSelfText(String selfText) {
        this.selfText = selfText;
    }

    public String getSubmissionID() {
        return submissionID;
    }

    public void setSubmissionID(String submissionID) {
        this.submissionID = submissionID;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLinkFlair() {
        return linkFlair;
    }

    public void setLinkFlair(String linkFlair) {
        this.linkFlair = linkFlair;
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


