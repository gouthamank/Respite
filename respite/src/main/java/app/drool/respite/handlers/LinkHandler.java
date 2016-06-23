package app.drool.respite.handlers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import app.drool.respite.activities.CommentsActivity;
import app.drool.respite.activities.SubmissionsActivity;
import app.drool.respite.activities.UserActivity;
import app.drool.respite.activities.WebViewActivity;

/**
 * Created by drool on 6/19/16.
 */

public final class LinkHandler {
    private static final String TAG = "LinkHandler.java";

    public static void analyse(Context mContext, String url) {
        if (url == null)
            return;

        Log.d(TAG, "analyse: " + url);

        if (isSubredditShort(url)) {
            Toast.makeText(mContext, "SUBREDDIT: " + url.substring(3), Toast.LENGTH_SHORT).show();
        } else if (isUserShort(url)) {
            Toast.makeText(mContext, "USER: " + url.substring(3), Toast.LENGTH_SHORT).show();
        } else if (isRedditLink(url)) {
            if (isSubreddit(url) != null) {

                Toast.makeText(mContext, "SUBREDDIT: " + isSubreddit(url), Toast.LENGTH_SHORT).show();
                Intent subredditIntent = new Intent(mContext, SubmissionsActivity.class);
                subredditIntent.putExtra("subreddit", isSubreddit(url));
                mContext.startActivity(subredditIntent);

            } else if (isUser(url) != null) {

                Toast.makeText(mContext, "USER: " + isUser(url), Toast.LENGTH_SHORT).show();
                Intent userIntent = new Intent(mContext, UserActivity.class);
                userIntent.putExtra("username", isUser(url));
                mContext.startActivity(userIntent);

            } else if (isComment(url) != null) {

                Pair<String, String> comment = isComment(url);
                assert comment != null;
                Toast.makeText(mContext, "COMMENT: " + comment.first + ", " + comment.second, Toast.LENGTH_SHORT).show();
                Intent commentIntent = new Intent(mContext, CommentsActivity.class);
                commentIntent.putExtra("submissionID", comment.first);
                commentIntent.putExtra("commentID", comment.second);
                mContext.startActivity(commentIntent);

            } else if (isSubmission(url) != null) {

                Toast.makeText(mContext, "SUBMISSION: " + isSubmission(url), Toast.LENGTH_SHORT).show();
                Intent submissionIntent = new Intent(mContext, CommentsActivity.class);
                submissionIntent.putExtra("submissionID", isSubmission(url));
                mContext.startActivity(submissionIntent);

            } else {
                Toast.makeText(mContext, "REDDIT: " + url, Toast.LENGTH_SHORT).show();
            }
        } /* else if (isPicture(url)){
            Toast.makeText(mContext, "PICTURE: " + url, Toast.LENGTH_SHORT).show();
        } else if (isGIF(url)){
            Toast.makeText(mContext, "GIF: " + url, Toast.LENGTH_SHORT).show();
        } */ else {

            Intent webViewIntent = new Intent(mContext, WebViewActivity.class);
            webViewIntent.putExtra("url", url);
            mContext.startActivity(webViewIntent);

        }

    }

    private static boolean isSubredditShort(String url) {
        return url.startsWith("/r/");
    }

    private static boolean isUserShort(String url) {
        return url.startsWith("/u/");
    }

    private static boolean isRedditLink(String url) {
        try {
            URL uri = new URL(url);
            return uri.getHost().startsWith("reddit.com") ||
                    uri.getHost().startsWith("redd.it") ||
                    uri.getHost().startsWith("www.reddit.com") ||
                    uri.getHost().startsWith("www.redd.it") ||
                    uri.getHost().startsWith("np.reddit.com") ||
                    uri.getHost().startsWith("pay.reddit.com");
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String isSubreddit(String url) {
        try {
            URL uri = new URL(url);
            String[] pieces = uri.getPath().split("/");
            LinkedList<String> piecesList = new LinkedList<>();
            for (String piece : pieces) {
                if (piece.length() > 0) {
                    piecesList.add(piece);
                }
            }
            if ((piecesList.size() == 2) && piecesList.getFirst().contentEquals("r"))
                return piecesList.get(1);
        } catch (MalformedURLException e) {
            Log.e(TAG, "isSubreddit: " + e.getMessage(), e);
        }

        return null;
    }

    public static String isUser(String url) {
        try {
            URL uri = new URL(url);
            String[] pieces = uri.getPath().split("/");
            LinkedList<String> piecesList = new LinkedList<>();
            for (String piece : pieces) {
                if (piece.length() > 0) {
                    piecesList.add(piece);
                }
            }
            if ((piecesList.size() == 2) && piecesList.getFirst().contentEquals("user"))
                return piecesList.get(1);
        } catch (MalformedURLException e) {
            Log.e(TAG, "isUser: " + e.getMessage(), e);
        }

        return null;
    }

    public static Pair<String, String> isComment(String url) {
        try {
            URL uri = new URL(url);
            String[] pieces = uri.getPath().split("/");
            LinkedList<String> piecesList = new LinkedList<>();
            for (String piece : pieces) {
                if (piece.length() > 0) {
                    piecesList.add(piece);
                }
            }
            if ((piecesList.size() == 6) && piecesList.getFirst().contentEquals("r"))
                return new Pair<>(piecesList.get(3), piecesList.get(5));
        } catch (MalformedURLException e) {
            Log.e(TAG, "isComment: " + e.getMessage(), e);
        }

        return null;
    }

    public static String isSubmission(String url) {
        try {
            URL uri = new URL(url);
            String[] pieces = uri.getPath().split("/");
            LinkedList<String> piecesList = new LinkedList<>();
            for (String piece : pieces) {
                if (piece.length() > 0) {
                    piecesList.add(piece);
                }
            }
            if ((piecesList.size() == 5) && piecesList.getFirst().contentEquals("r"))
                return piecesList.get(3);
        } catch (MalformedURLException e) {
            Log.e(TAG, "isSubmission: " + e.getMessage(), e);
        }

        return null;
    }

    private static boolean isPicture(String url) {
        try {
            URL uri = new URL(url);
            String[] fileTypes = {".jpeg", ".jpg", ".png", ".bmp"};
            for (String fileType : fileTypes) {
                if (uri.getPath().endsWith(fileType))
                    return true;
            }
            return false;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static boolean isGIF(String url) {
        try {
            URL uri = new URL(url);
            String[] fileTypes = {".gif"};
            for (String fileType : fileTypes) {
                if (uri.getPath().endsWith(fileType))
                    return true;
            }
            return false;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
