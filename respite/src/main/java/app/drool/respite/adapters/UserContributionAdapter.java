package app.drool.respite.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import app.drool.respite.fragments.UserAboutFragment;
import app.drool.respite.fragments.UserCommentsFragment;
import app.drool.respite.fragments.UserSubmittedFragment;

/**
 * Created by drool on 6/20/16.
 */

public class UserContributionAdapter extends FragmentPagerAdapter {
    private static final String[] TITLES = {"ABOUT", "COMMENTS", "SUBMITTED"};
    private UserAboutFragment fragment1 = null;
    private UserCommentsFragment fragment2 = null;
    private UserSubmittedFragment fragment3 = null;

    public UserContributionAdapter(FragmentManager fm, String username) {
        super(fm);
        fragment1 = UserAboutFragment.newInstance(username);
        fragment2 = UserCommentsFragment.newInstance(username);
        fragment3 = UserSubmittedFragment.newInstance(username);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return fragment1;
        else if (position == 1)
            return fragment2;
        else if (position == 2)
            return fragment3;
        else
            return null;
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
}
