package com.codepath.shivagss.twitterclient.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.adapter.TimeLineTweetsAdapter;
import com.codepath.shivagss.twitterclient.fragment.CreateTweetFragment;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.TwitterClientApp;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;
import com.codepath.shivagss.twitterclient.utils.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TimeLineActivity extends Activity implements CreateTweetFragment.onTweetListener {

    private static final String TAG = TimeLineActivity.class.getName().toString();
    private TwitterRestClient mClient;
    ArrayList<Tweet> mTweetsList;
    ArrayAdapter<Tweet> mTweetsAdapter;
    ListView lvTweets;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        mClient = TwitterClientApp.getRestClient();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeLine(null);
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        lvTweets = (ListView) findViewById(R.id.lvTweets);
        mTweetsList = new ArrayList<Tweet>();
        mTweetsAdapter = new TimeLineTweetsAdapter(this, mTweetsList);
        lvTweets.setAdapter(mTweetsAdapter);
        lvTweets.setEmptyView(findViewById(android.R.id.empty));
        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                long maxID = mTweetsList.get(mTweetsList.size()-1).getId();
                populateTimeLine(""+maxID);
            }
        });
        lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long tweetID = mTweetsAdapter.getItem(position).getId();
                startTweetDetailsActivity(tweetID);
            }
        });

        populateTimeLine(null);
    }

    private void startTweetDetailsActivity(long tweetID) {
        Intent intent = new Intent(this, TweetDetailActivity.class);
        intent.putExtra("tweet_id", tweetID);
        startActivity(intent);
    }

    private void populateTimeLine(final String maxID) {
        mClient.getHomeTimeLine(maxID, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray jsonArray) {
                if (TextUtils.isEmpty(maxID)) {
                    mTweetsAdapter.clear();
                }
                mTweetsAdapter.addAll(Tweet.fromJson(jsonArray));
            }

            @Override
            public void onFailure(Throwable throwable, JSONObject jsonObject) {
                Toast.makeText(TimeLineActivity.this, "Failed to populate your timeline", Toast.LENGTH_LONG).show();
                Log.e(TAG, throwable.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.time_line, menu);
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
        if(id == R.id.compose){
            startCreateTweetFragment();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCreateTweetFragment() {
        DialogFragment fragment = CreateTweetFragment.getInstance(null);
        fragment.show(getFragmentManager(), "dialog");
    }

    private void onTwitterSignout() {
        mClient.clearAccessToken();
        startActivity(new Intent(this, TwitterLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onTweetSubmit() {
        populateTimeLine(null);
    }
}
