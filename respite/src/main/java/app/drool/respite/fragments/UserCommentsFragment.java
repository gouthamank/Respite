package app.drool.respite.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Listing;
import net.dean.jraw.paginators.UserContributionPaginator;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.CommentContributionListAdapter;

/**
 * Created by drool on 6/30/16.
 */

public class UserCommentsFragment extends Fragment implements CommentContributionListAdapter.EndlessScrollListener {
    private static final String TAG = "UserComments.java";
    private String username = null;
    private RedditClient mRedditClient = null;
    private UserContributionPaginator mPaginator = null;
    private CommentContributionListAdapter mAdapter = null;

    private ProgressBar progressBar = null;
    private RecyclerView commentList = null;

    public static UserCommentsFragment newInstance(String username) {
        Bundle args = new Bundle();
        UserCommentsFragment fragment = new UserCommentsFragment();
        args.putString("username", username);
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
            mPaginator = new UserContributionPaginator(mRedditClient, "comments", username);
        if (mAdapter == null)
            mAdapter = new CommentContributionListAdapter(getContext());

        startDownloadTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_submitted, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.fragment_user_submitted_progressbar);
        commentList = (RecyclerView) view.findViewById(R.id.fragment_user_submitted_list);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        commentList.setLayoutManager(layoutManager);
        progressBar.setVisibility(View.VISIBLE);
        commentList.setVisibility(View.GONE);

        commentList.setAdapter(mAdapter);

        return view;
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
                    progressBar.setVisibility(View.GONE);
                    commentList.setVisibility(View.VISIBLE);
                } else
                    Toast.makeText(getContext(), R.string.useractivity_networkerror, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void addToAdapter(Listing<Contribution> contributions) {
        for(Contribution c : contributions) {
            mAdapter.addComments(new Comment(c.getDataNode()));
        }
    }

    @Override
    public void onLoadMore(int position) {
        startDownloadTask();
    }

}
