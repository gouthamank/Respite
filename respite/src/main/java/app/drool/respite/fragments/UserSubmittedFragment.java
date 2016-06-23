package app.drool.respite.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.LinkedList;

import app.drool.respite.R;
import app.drool.respite.Respite;

/**
 * Created by drool on 6/20/16.
 */

public class UserSubmittedFragment extends Fragment {
    private static final String TAG = "UserSubmitted.java";
    private String username = null;
    private RedditClient mRedditClient = null;
    private UserContributionPaginator mPaginator = null;
    private LinkedList<Contribution> mContributions = null;

    public static UserSubmittedFragment newInstance(String username) {
        Bundle args = new Bundle();
        args.putString("username", username);
        UserSubmittedFragment fragment = new UserSubmittedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (username == null)
            username = getArguments() == null ? null : getArguments().getString("username");
        if (mRedditClient == null)
            mRedditClient = ((Respite) getActivity().getApplication()).getRedditClient();
        if (mPaginator == null)
            mPaginator = new UserContributionPaginator(mRedditClient, "submitted", username);
        if (mContributions == null)
            mContributions = new LinkedList<>();

        startDownloadTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void startDownloadTask() {
        new AsyncTask<Void, Void, Listing<Contribution>>() {
            @Override
            protected Listing<Contribution> doInBackground(Void... params) {
                try {
                    return mPaginator.next();
                } catch (NetworkException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Listing<Contribution> contributions) {
                if (contributions != null) {
                    addToAdapter(contributions);
                } else
                    Toast.makeText(getContext(), R.string.useractivity_networkerror, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void addToAdapter(Listing<Contribution> contributions) {
        for (Contribution c : contributions) {
            Log.d(TAG, "addToAdapter: " + c.toString());
            mContributions.add(c);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }
}
