package app.drool.respite.handlers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import app.drool.respite.activities.CommentsActivity;
import app.drool.respite.activities.ImageViewActivity;
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

            Intent subredditIntent = new Intent(mContext, SubmissionsActivity.class);
            subredditIntent.putExtra("subreddit", url.substring(3));
            mContext.startActivity(subredditIntent);

        } else if (isUserShort(url)) {

            Intent userIntent = new Intent(mContext, UserActivity.class);
            userIntent.putExtra("username", url.substring(3));
            mContext.startActivity(userIntent);

        } else if (isRedditLink(url)) {
            if (isSubreddit(url) != null) {

                Intent subredditIntent = new Intent(mContext, SubmissionsActivity.class);
                subredditIntent.putExtra("subreddit", isSubreddit(url));
                mContext.startActivity(subredditIntent);

            } else if (isUser(url) != null) {

                Intent userIntent = new Intent(mContext, UserActivity.class);
                userIntent.putExtra("username", isUser(url));
                mContext.startActivity(userIntent);

            } else if (isComment(url) != null) {

                Pair<String, String> comment = isComment(url);
                assert comment != null;
                Intent commentIntent = new Intent(mContext, CommentsActivity.class);
                commentIntent.putExtra("submissionID", comment.first);
                commentIntent.putExtra("commentID", comment.second);
                mContext.startActivity(commentIntent);

            } else if (isSubmission(url) != null) {

                Intent submissionIntent = new Intent(mContext, CommentsActivity.class);
                submissionIntent.putExtra("submissionID", isSubmission(url));
                mContext.startActivity(submissionIntent);

            } else if (isWiki(url)) {

                Intent wikiIntent = new Intent(mContext, WebViewActivity.class);
                wikiIntent.putExtra("url", url);
                mContext.startActivity(wikiIntent);

            }
        } else if (isPicture(url)) {

            Intent pictureIntent = new Intent(mContext, ImageViewActivity.class);
            pictureIntent.putExtra("url", url);
            mContext.startActivity(pictureIntent);

        } /* else if (isGIF(url)){
            Toast.makeText(mContext, "GIF: " + url, Toast.LENGTH_SHORT).show();
        } */ else if (isPotentialYoutube(url)) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {

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
                    uri.getHost().startsWith("np.reddit.com") || // TODO Handle this better
                    uri.getHost().startsWith("www.np.reddit.com") || // TODO Handle this better
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

    public static boolean isWiki(String url) {
        try {
            URL uri = new URL(url);
            String[] pieces = uri.getPath().split("/");
            LinkedList<String> piecesList = new LinkedList<>();
            for (String piece : pieces) {
                if (piece.length() > 0) {
                    piecesList.add(piece);
                }
            }
            if (piecesList.size() >= 3
                    && piecesList.getFirst().contentEquals("r")
                    && piecesList.get(2).contentEquals("wiki"))
                return true;
        } catch (MalformedURLException e) {
            Log.e(TAG, "isSubmission: " + e.getMessage(), e);
        }

        return false;
    }

    private static boolean isPicture(String url) {
        try {
            URL uri = new URL(url);
            String[] fileTypes = {".jpeg", ".jpg", ".png"};
            for (String fileType : fileTypes) {
                if (uri.getPath().endsWith(fileType))
                    return true;
            }
            return uri.getHost().contentEquals("i.reddituploads.com");
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

    private static boolean isPotentialYoutube(String url) { // Ech. Disgusting.
        return url.contains("/youtube.") || url.contains("youtu.be") || url.contains("www.youtube.")
                || url.contains("m.youtube.");
    }
}
