package app.drool.respite.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import app.drool.respite.fragments.ArrayListFragment;
import app.drool.respite.fragments.UserAboutFragment;

/**
 * Created by drool on 6/20/16.
 */

public class UserContributionAdapter extends FragmentPagerAdapter {
    private static final String[] TITLES = {"ABOUT", "COMMENTS", "SUBMITTED"};
    private UserAboutFragment fragment1 = null;
    public UserContributionAdapter(FragmentManager fm, String username) {
        super(fm);
        fragment1 = UserAboutFragment.newInstance(username);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0)
            return fragment1;
        else
            return ArrayListFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
}
