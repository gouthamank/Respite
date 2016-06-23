package app.drool.respite.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Account;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.utils.Utilities;

/**
 * Created by drool on 6/20/16.
 */

public class UserAboutFragment extends Fragment {
    private static final String TAG = "UserAboutFragment.java";
    private String username = null;
    private RedditClient mRedditClient = null;
    private Account mAccount = null;

    public static UserAboutFragment newInstance(String username) {
        Bundle args = new Bundle();
        args.putString("username", username);
        UserAboutFragment fragment = new UserAboutFragment();
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

        if (mAccount == null)
            startDownloadTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_about, container, false);
        if (savedInstanceState == null) {
            view.findViewById(R.id.fragment_user_about_progressbar).setVisibility(View.VISIBLE);
            view.findViewById(R.id.fragment_user_about_content).setVisibility(View.INVISIBLE);
        } else {
            TextView name = (TextView) view.findViewById(R.id.fragment_user_about_name);
            TextView age = (TextView) view.findViewById(R.id.fragment_user_about_age);
            TextView linkkarma = (TextView) view.findViewById(R.id.fragment_user_about_linkkarma);
            TextView commentkarma = (TextView) view.findViewById(R.id.fragment_user_about_commentkarma);
            TextView ismod = (TextView) view.findViewById(R.id.fragment_user_about_ismod);
            TextView isfriends = (TextView) view.findViewById(R.id.fragment_user_about_isfriends);

            name.setText(savedInstanceState.getString("name"));
            age.setText(savedInstanceState.getString("age"));
            linkkarma.setText(savedInstanceState.getString("linkkarma"));
            commentkarma.setText(savedInstanceState.getString("commentkarma"));
            isfriends.setText(savedInstanceState.getString("isfriends"));
            ismod.setText(savedInstanceState.getString("ismod"));
            view.findViewById(R.id.fragment_user_about_progressbar).setVisibility(View.GONE);
            view.findViewById(R.id.fragment_user_about_content).setVisibility(View.VISIBLE);
        }
        return view;
    }

    private void startDownloadTask() {
        new AsyncTask<Void, Void, Account>() {
            @Override
            protected Account doInBackground(Void... params) {
                try {
                    return mRedditClient.getUser(username);
                } catch (NetworkException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Account account) {
                if (account != null) {
                    refreshWithAccountDetails(account);
                } else
                    Toast.makeText(getContext(), R.string.useractivity_networkerror, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    private void refreshWithAccountDetails(Account account) {
        View view = getView();
        TextView name = (TextView) view.findViewById(R.id.fragment_user_about_name);
        TextView age = (TextView) view.findViewById(R.id.fragment_user_about_age);
        TextView linkkarma = (TextView) view.findViewById(R.id.fragment_user_about_linkkarma);
        TextView commentkarma = (TextView) view.findViewById(R.id.fragment_user_about_commentkarma);
        TextView ismod = (TextView) view.findViewById(R.id.fragment_user_about_ismod);
        TextView isfriends = (TextView) view.findViewById(R.id.fragment_user_about_isfriends);

        name.setText(account.getFullName());
        age.setText(Utilities.getFormattedCreationTime(account.getCreated()));
        linkkarma.setText(String.valueOf(account.getLinkKarma()));
        commentkarma.setText(String.valueOf(account.getCommentKarma()));
        if (account.isFriend())
            isfriends.setText(R.string.fragment_user_about_true);
        else
            isfriends.setText(R.string.fragment_user_about_false);
        if (account.isMod())
            ismod.setText(R.string.fragment_user_about_true);
        else
            ismod.setText(R.string.fragment_user_about_false);

        view.findViewById(R.id.fragment_user_about_progressbar).setVisibility(View.GONE);
        view.findViewById(R.id.fragment_user_about_content).setVisibility(View.VISIBLE);

        mAccount = account;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mAccount != null) {
            outState.putString("name", mAccount.getFullName());
            outState.putString("age", Utilities.getFormattedCreationTime(mAccount.getCreated()));
            outState.putString("linkkarma", String.valueOf(mAccount.getLinkKarma()));
            outState.putString("commentkarma", String.valueOf(mAccount.getCommentKarma()));
            outState.putString("isfriends",
                    mAccount.isFriend() ? getString(R.string.fragment_user_about_true) : getString(R.string.fragment_user_about_false));
            outState.putString("ismod",
                    mAccount.isMod() ? getString(R.string.fragment_user_about_true) : getString(R.string.fragment_user_about_false));
            super.onSaveInstanceState(outState);
        }
    }
}
