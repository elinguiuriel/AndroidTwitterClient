package com.codepath.shivagss.twitterclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.TwitterClientApp;
import com.codepath.shivagss.twitterclient.adapter.TimeLineTweetsAdapter;
import com.codepath.shivagss.twitterclient.fragment.TweetListFragment;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;
import com.codepath.shivagss.twitterclient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;

import java.util.ArrayList;

public class ProfileActivity extends FragmentActivity implements TweetListFragment.OnFragmentInteractionListener {

    private ImageView ivProfilePic;
    private TextView tvUserName;
    private TextView tvScreenName;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvDisplayURL;
    private User mUser;
    private Button mBtnTweetsCount;
    private Button mBtnFollowingCount;
    private Button mBtnFollowersCount;
    private Button mBtnEditProfile;
    private ImageButton mIBtnSettings;
    private ImageButton mIBtnNotifications;
    private Button mBtnFollowing;
    private ImageView ivBackgroundPic;
    private Button mBtnViewAllTweets;
    private Button mBtnTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("user_id");

        mUser = User.getUser(userID);

        setupViews();

        TwitterClientApp.getRestClient().getUserTimeLine(userID, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray jsonArray) {
                super.onSuccess(jsonArray);
                ArrayList<Tweet> list = Tweet.fromJson(jsonArray, TweetListFragment.USER_TWEETS_TYPE);
                ViewGroup parent = (ViewGroup) findViewById(R.id.lvTweets);
                LinearLayout myGallery = (LinearLayout) findViewById(R.id.mygallery);
                parent.removeAllViews();
                TimeLineTweetsAdapter adapter = new TimeLineTweetsAdapter(ProfileActivity.this, list);
                for(int i=0;i<list.size();i++){
                    View v = adapter.getTweetItemView(null, null, list.get(i));
                    parent.addView(v,i);
                    if(i==2) break;
                }
                myGallery.removeAllViews();
                ImageLoader loader = ImageLoader.getInstance();
                int count = 0;
                for(int i=0;i<list.size();i++){
                    if(!TextUtils.isEmpty(list.get(i).getMediaURL())){
                        LinearLayout layout = new LinearLayout(getApplicationContext());
                        layout.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
                        layout.setGravity(Gravity.CENTER);

                        ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        loader.displayImage(list.get(i).getMediaURL(), imageView);
                        layout.addView(imageView);
                        myGallery.addView(layout, count++);
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable, JSONArray jsonArray) {
                super.onFailure(throwable, jsonArray);
                View C = findViewById(R.id.lvTweets);
                ViewGroup parent = (ViewGroup) C.getParent();
                parent.removeAllViews();
                findViewById(R.id.btnViewTweets).setVisibility(View.GONE);

                LinearLayout myGallery = (LinearLayout) findViewById(R.id.mygallery);
                myGallery.setVisibility(View.GONE);
                findViewById(R.id.btnViewPhotos).setVisibility(View.GONE);
            }
        });
    }

    private void setupViews() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        imageLoader.displayImage(mUser.getUrl(), ivProfilePic);

        ivBackgroundPic = (ImageView) findViewById(R.id.ivBackgroundPic);
        if(TextUtils.isEmpty(mUser.getBgURL())) {
            ivBackgroundPic.setVisibility(View.INVISIBLE);
        }else{
            ivBackgroundPic.setVisibility(View.VISIBLE);
            imageLoader.displayImage(mUser.getBgURL(), ivBackgroundPic);
        }
        ivBackgroundPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ImageActivity.class);
                intent.putExtra("media_url", mUser.getBgURL());
                startActivity(intent);
            }
        });

        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserName.setText(mUser.getName());
        if(mUser.isVerified()){
            tvUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_profile_verified, 0);
        }else{
            tvUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        tvScreenName.setText(mUser.getScreenName());

        tvDescription = (TextView) findViewById(R.id.tvUserDescription);
        if (TextUtils.isEmpty(mUser.getDescription())) {
            tvDescription.setVisibility(View.GONE);
        } else {
            tvDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(mUser.getDescription());
        }

        tvLocation = (TextView) findViewById(R.id.tvLocation);
        if (TextUtils.isEmpty(mUser.getLocation())) {
            tvLocation.setVisibility(View.GONE);
        } else {
            tvLocation.setVisibility(View.VISIBLE);
            tvLocation.setText(mUser.getLocation());
        }
        tvDisplayURL = (TextView) findViewById(R.id.tvDisplayURL);
        if (TextUtils.isEmpty(mUser.getDisplayURL())) {
            tvDisplayURL.setVisibility(View.GONE);
        } else {
            tvDisplayURL.setVisibility(View.VISIBLE);
            tvDisplayURL.setText(mUser.getDisplayURL());
        }

        mBtnTweetsCount = (Button) findViewById(R.id.btnTweets);
        mBtnTweetsCount.setText(Html.fromHtml("<b>" + mUser.getTweetsCount() + "</b><br>TWEETS"));

        mBtnFollowingCount = (Button) findViewById(R.id.btnFollowing);
        mBtnFollowingCount.setText(Html.fromHtml("<b>" + mUser.getFollowingCount() + "</b><br>FOLLOWING"));

        mBtnFollowersCount = (Button) findViewById(R.id.btnFollowers);
        mBtnFollowersCount.setText(Html.fromHtml("<b>" + mUser.getFollowersCount() + "</b><br>FOLLOWERS"));

        mBtnEditProfile = (Button) findViewById(R.id.btnEditProfile);
        mIBtnSettings = (ImageButton) findViewById(R.id.btnSettings);
        mIBtnNotifications = (ImageButton) findViewById(R.id.btnNotificationsIcon);
        mBtnFollowing = (Button) findViewById(R.id.btnFollowingIcon);

        if (mUser.isCurrentUser()) {
            mBtnEditProfile.setVisibility(View.VISIBLE);
            mIBtnSettings.setVisibility(View.GONE);
            mIBtnNotifications.setVisibility(View.GONE);
            mBtnFollowing.setVisibility(View.GONE);
        } else {
            mBtnEditProfile.setVisibility(View.GONE);
            mIBtnSettings.setVisibility(View.VISIBLE);
            mIBtnNotifications.setVisibility(View.VISIBLE);
            if (mUser.isNotifications()) {
                mIBtnNotifications.setImageResource(R.drawable.ic_favorite_action_checked);
            } else {
                mIBtnNotifications.setImageResource(R.drawable.ic_favorite_action_default);
            }

            mBtnFollowing.setVisibility(View.VISIBLE);
            if (mUser.isFollowing()) {
                mBtnFollowing.setText("Following");
                mBtnFollowing.setBackgroundResource((R.drawable.rounded_tweet_button));
                mBtnFollowing.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_follow_action_checked, 0, 0, 0);
                mBtnFollowing.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                mBtnFollowing.setText("Follow");
                mBtnFollowing.setBackgroundResource((R.drawable.rounded_blue_edge_button));
                mBtnFollowing.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_follow_action_default, 0, 0, 0);
                mBtnFollowing.setTextColor(getResources().getColor(R.color.primary_color));
            }
        }
    }

    public void onViewUserTweets(View v){
        startDisplayActivity("user", mUser.getUserId());
    }

    public void onViewUserFavorites(View v){
        startDisplayActivity("favorites", mUser.getUserId());
    }

    public void onViewUserFriends(View v){
        startDisplayActivity("friends", mUser.getUserId());
    }

    public void onViewUserFollowers(View v){
        startDisplayActivity("followers", mUser.getUserId());
    }

    private void startDisplayActivity(String type, String userID) {
        Intent intent = new Intent(this, DisplayTweetsListActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("user_id", userID);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
