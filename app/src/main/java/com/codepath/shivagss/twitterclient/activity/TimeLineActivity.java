package com.codepath.shivagss.twitterclient.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.adapter.TimeLineTweetsAdapter;
import com.codepath.shivagss.twitterclient.fragment.CreateTweetFragment;
import com.codepath.shivagss.twitterclient.fragment.TweetListFragment;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.TwitterClientApp;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;
import com.codepath.shivagss.twitterclient.utils.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TimeLineActivity extends FragmentActivity implements CreateTweetFragment.onTweetListener, TweetListFragment.OnFragmentInteractionListener, ActionBar.TabListener {

    private static final String TAG = TimeLineActivity.class.getName().toString();
    private final CountDownLatch mNetworkRequestDoneSignal = new CountDownLatch(1);
    ArrayList<Tweet> mTweetsList;
    TimeLineTweetsAdapter mTweetsAdapter;
    ListView lvTweets;
    private TwitterRestClient mClient;
    private SwipeRefreshLayout swipeContainer;
    private ViewPager mViewPager;
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private SearchView searchView;
    private String mSearchString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        mClient = TwitterClientApp.getRestClient();
        TwitterClientApp.getRestClient()
                .getUserCredentials(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        try {
                            jsonObject.put("current_user", true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        User.fromJson(jsonObject);
                    }

                    @Override
                    public void onFailure(Throwable throwable, JSONObject jsonObject) {
                        Log.e(TAG, throwable.toString());
                    }
                });
//
//        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
//        // Setup refresh listener which triggers new data loading
//        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                populateTimeLine(null, true);
//                swipeContainer.setRefreshing(false);
//            }
//        });
//        // Configure the refreshing colors
//        swipeContainer.setColorScheme(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
//
//        lvTweets = (ListView) findViewById(R.id.lvTweets);
//        mTweetsList = new ArrayList<Tweet>();
//        mTweetsAdapter = new TimeLineTweetsAdapter(this, mTweetsList);
//        lvTweets.setAdapter(mTweetsAdapter);
//        lvTweets.setEmptyView(findViewById(android.R.id.empty));
//        lvTweets.setOnScrollListener(new EndlessScrollListener() {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount) {
//                if (mTweetsList.size() > 0) {
//                    String maxID = mTweetsList.get(mTweetsList.size() - 1).getTweetId();
//                    populateTimeLine("" + (maxID), false);
//                }
//            }
//        });
//        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Tweet tweet = mTweetsAdapter.getItem(position);
//                if (tweet != null) {
//                    String tweetID = tweet.getTweetId();
//                    startTweetDetailsActivity(tweetID);
//                }
//            }
//        });
//
//        loadTweetsFromNetwork(null);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
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
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
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
                    return TweetListFragment.newInstance("mentions");
                case 2:
                    return TweetListFragment.newInstance("user");

                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment fragment = TweetListFragment.newInstance("home");
                    return fragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "Mentions";
                case 2:
                    return "Tweets";

                default:
                    return "Home";
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
    public void onBackPressed() {
        //hijack the event
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.time_line, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchString = query;
                Intent intent = new Intent(TimeLineActivity.this, SearchActivity.class);
                intent.putExtra("search_string", mSearchString);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.signout) {
            onTwitterSignout();
            return true;
        }
        if (id == R.id.compose) {
            startCreateTweetFragment();
        }
        if(id == R.id.profile){
            startProfileActivity(User.getCurrentUser().getUserId());
        }
        return super.onOptionsItemSelected(item);
    }

    private void startProfileActivity(String userID) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id", userID);
        startActivity(intent);

    }

    private void startCreateTweetFragment() {
        DialogFragment fragment = CreateTweetFragment.getInstance(null);
        fragment.show(getFragmentManager(), "dialog");
    }

    private void onTwitterSignout() {
        User.getCurrentUser().setCurrentUser(false);
        User.getCurrentUser().save();
        mClient.clearAccessToken();
        startActivity(new Intent(this, TwitterLoginActivity.class).
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onTweetSubmit() {
        //populateTimeLine(null, true);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
