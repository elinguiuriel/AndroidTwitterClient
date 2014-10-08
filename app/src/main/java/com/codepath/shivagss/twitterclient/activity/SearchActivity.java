package com.codepath.shivagss.twitterclient.activity;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.TwitterClientApp;
import com.codepath.shivagss.twitterclient.adapter.TimeLineTweetsAdapter;
import com.codepath.shivagss.twitterclient.fragment.CreateTweetFragment;
import com.codepath.shivagss.twitterclient.fragment.TweetListFragment;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class SearchActivity extends FragmentActivity implements TweetListFragment.OnFragmentInteractionListener, ActionBar.TabListener {

    private static final String TAG = SearchActivity.class.getName().toString();
    private TwitterRestClient mClient;
    private ViewPager mViewPager;
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private SearchView searchView;
    private static String mSearchString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        Intent intent = getIntent();
        mSearchString = intent.getStringExtra("search_string");
        mClient = TwitterClientApp.getRestClient();

        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 1:
                    Fragment fragment = TweetListFragment.newInstance("search_all");
                    fragment.getArguments().putString("search_string", mSearchString);
                    return fragment;

                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment fragment1 = TweetListFragment.newInstance("search_top");
                    fragment1.getArguments().putString("search_string", mSearchString);
                    return fragment1;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            ((TweetListFragment)object).setSearchString(mSearchString);
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "All Tweets";

                default:
                    return "Top Tweets";
            }
        }


    }

//    private void startTweetDetailsActivity(String tweetID) {
//        Intent intent = new Intent(this, TweetDetailActivity.class);
//        intent.putExtra("tweet_id", tweetID);
//        startActivity(intent);
//    }
//
//    private void populateTimeLine(final String maxID, boolean loadFromNetwork) {
//        long maxIDl = -1;
//        if (TextUtils.isEmpty(maxID)) {
//            mTweetsAdapter.clear();
//        } else {
//            maxIDl = Long.parseLong(maxID);
//        }
//        List<Tweet> list = Tweet.getTweetsList(maxIDl);
//
//        if (list.isEmpty() || loadFromNetwork) {
//            loadTweetsFromNetwork(maxID, true);
//        } else {
//            mTweetsAdapter.addAll(list);
//        }
//    }
//
//    private void loadTweetsFromNetwork(String maxID) {
//        loadTweetsFromNetwork(maxID, false);
//    }
//
//    private void loadTweetsFromNetwork(final String maxID, final boolean await) {
//
//        mClient.getHomeTimeLine(maxID, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(JSONArray jsonArray) {
//                Tweet.fromJson(jsonArray);
//                mNetworkRequestDoneSignal.countDown();
//                populateTimeLine(maxID, false);
//            }
//
//            @Override
//            public void onFailure(Throwable throwable, JSONObject jsonObject) {
//                Toast.makeText(TimeLineActivity.this, "Failed to load new tweets from network", Toast.LENGTH_LONG).show();
//                Log.e(TAG, throwable.toString());
//                mNetworkRequestDoneSignal.countDown();
//                populateTimeLine(maxID, false);
//            }
//        });
//
//        try {
//            if (await) {
//                mNetworkRequestDoneSignal.await();
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchString = query;
                mAppSectionsPagerAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        if (!TextUtils.isEmpty(mSearchString)) {
            searchItem.expandActionView();
            searchView.setQuery(mSearchString, false);
            searchView.requestFocus();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
