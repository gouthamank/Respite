package app.drool.respite.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import net.dean.jraw.RedditClient;

import app.drool.respite.R;
import app.drool.respite.Respite;
import app.drool.respite.adapters.UserContributionAdapter;

/**
 * Created by drool on 6/20/16.
 */

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity.java";

    private RedditClient mRedditClient = null;
    private String username = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        if (mRedditClient == null) {
            mRedditClient = ((Respite) getApplication()).getRedditClient();
            username = getIntent().getExtras().getString("username");
        }

        setUpMenuBar();

        UserContributionAdapter mAdapter = new UserContributionAdapter(getSupportFragmentManager(), username);
        ViewPager mPager = (ViewPager) findViewById(R.id.activity_user_viewpager);
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(3);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.activity_user_tabs);
        tabLayout.setupWithViewPager(mPager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Respite) getApplication()).refreshCredentials(this);
    }

    private void setUpMenuBar() {
        getSupportActionBar().setTitle(getString(R.string.actionbar_title_user, username));
    }

}
