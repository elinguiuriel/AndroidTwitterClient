package com.codepath.shivagss.twitterclient.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.TwitterClientApp;
import com.codepath.shivagss.twitterclient.fragment.CreateTweetFragment;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;
import com.codepath.shivagss.twitterclient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

public class TweetDetailActivity extends Activity implements CreateTweetFragment.onTweetListener {

    private static final String TAG = TweetDetailActivity.class.getName().toString();
    private ImageView ivRetweetIcon;
    private ImageView ivProfilePic;
    private TextView tvUserRetweetName;
    private TextView tvUserName;
    private TextView tvUserNameHandle;
    private TextView tvBodyText;
    private TextView tvCreatedAtTime;
    private ImageButton btnReply;
    private ImageButton btnRetweet;
    private ImageButton btnFavorite;
    private ImageButton btnFollowing;
    private TextView tvRetweets;
    private TextView tvFavorites;
    private ImageView ivVerified;
    private ImageButton btnShare;
    private ImageButton btnDelete;
    private User mLoggedUser;
    private ImageView ivMedia;
    private Tweet mTweet;
    private TwitterRestClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        setupViews();

        mClient = TwitterClientApp.getRestClient();

        Intent intent = getIntent();
        String tweetID = intent.getStringExtra("tweet_id");
        if (tweetID == null || tweetID.isEmpty()) {
            Toast.makeText(this, "Error downloading tweet details. Please try again later.", Toast.LENGTH_LONG).show();
            finish();
        }

        mLoggedUser = User.getCurrentUser();

        if (!Utils.isNetworkAvailable(this)) {
            mTweet = Tweet.getTweet(tweetID);
            populateViews(mTweet);
        } else {
            mClient.getTweetDetails(tweetID, mTweetJSONReponseHandler);
        }

        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity(mTweet.getUser().getUserId());
            }
        });
    }

    public JsonHttpResponseHandler mTweetJSONReponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onFailure(Throwable throwable, JSONObject jsonObject) {
                super.onFailure(throwable, jsonObject);
                try {
                    new AlertDialog.Builder(TweetDetailActivity.this).setTitle("Error").setMessage(jsonObject.getString("errors")).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "Error loading tweet details." + throwable.getMessage());
            }

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
                mTweet = Tweet.fromJson(jsonObject, -1);
                populateViews(mTweet);
            }
        };

    private void startProfileActivity(String userID) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id", userID);
        startActivity(intent);

    }

    private void populateViews(Tweet tweet) {
        if (tweet.hasRetweetedUsername()) {
            ivRetweetIcon.setVisibility(View.VISIBLE);
            tvUserRetweetName.setVisibility(View.VISIBLE);
            tvUserRetweetName.setText(getString(R.string.retweeted_user_name
                    , tweet.getRetweetedUserName()));
        } else {
            ivRetweetIcon.setVisibility(View.GONE);
            tvUserRetweetName.setVisibility(View.GONE);
        }

        if (tweet.getUser().isFollowing()) {
            btnFollowing.setVisibility(View.INVISIBLE);
        } else {
            btnFollowing.setVisibility(View.VISIBLE);
        }

        if (tweet.isFavorited()) {
            btnFavorite.setImageResource(R.drawable.ic_action_fave_on_default);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_action_fave_off_pressed);
        }

        if (tweet.isRetweeted()) {
            btnRetweet.setImageResource(R.drawable.ic_action_rt_on_default);
        } else {
            btnRetweet.setImageResource(R.drawable.ic_action_rt_off_pressed);
        }

        tvUserName.setText(tweet.getUser().getName());
        tvUserNameHandle.setText(tweet.getUser().getScreenName());
        tvCreatedAtTime.setText(tweet.getTimeStamp());
        tvBodyText.setText(Html.fromHtml(tweet.getBody()));

        String rtCount = tweet.getRetweetCount();
        tvRetweets.setText(Html.fromHtml(rtCount.equals("1") ? "<b>" + rtCount + "</b> RETWEET" : "<b>" + rtCount + "</b> RETWEETS"));
        String rtFav = tweet.getFavoriteCount();
        tvFavorites.setText(Html.fromHtml(rtFav.equals("1") ? "<b>" + rtFav + "</b> FAVORITE" : "<b>" + rtFav + "</b> FAVORITES"));

        ivProfilePic.setImageResource(0);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(tweet.getUser().getUrl(), ivProfilePic);

        if (tweet.getUser().isVerified()) {
            ivVerified.setVisibility(View.VISIBLE);
        } else {
            ivVerified.setVisibility(View.GONE);
        }

        if (mLoggedUser != null && mLoggedUser.getUserId() == tweet.getUser().getUserId()) {
            btnDelete.setVisibility(View.VISIBLE);
            btnFollowing.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(tweet.getMediaURL())) {
            ivMedia.setVisibility(View.GONE);
        } else {
            ivMedia.setVisibility(View.VISIBLE);
            imageLoader.displayImage(tweet.getMediaURL(), ivMedia);
        }

    }

    private void setupViews() {

        ivRetweetIcon = (ImageView) findViewById(R.id.ivRetweetUserIcon);
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        tvUserRetweetName = (TextView) findViewById(R.id.tvUserRetweetName);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserNameHandle = (TextView) findViewById(R.id.tvUserNameHandle);
        tvBodyText = (TextView) findViewById(R.id.tvBodyText);
        tvCreatedAtTime = (TextView) findViewById(R.id.tvTimeStamp);
        btnReply = (ImageButton) findViewById(R.id.btnReply);
        btnRetweet = (ImageButton) findViewById(R.id.btnRetweet);
        btnRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(TweetDetailActivity.this);
                dialog.setTitle("Retweet").setMessage("Retweet this to you followers?")
                        .setPositiveButton("Retweet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mClient.postReTweet(mTweet.getTweetId(), mTweetJSONReponseHandler);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setNeutralButton("Quote", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String status = "\""+tvUserNameHandle.getText().toString()+": "+tvBodyText.getText().toString()+"\"";
                                DialogFragment fragment = CreateTweetFragment.getInstance(status);
                                fragment.show(getFragmentManager(), "dialog");
                            }
                        }).show();
            }
        });
        btnFavorite = (ImageButton) findViewById(R.id.btnFavorite);
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTweet!=null){
                    if(mTweet.isFavorited()){
                        mClient.postUnFavoriteTweetUpdate(mTweet.getTweetId(), mTweetJSONReponseHandler);
                    }else{
                        mClient.postFavoriteTweetUpdate(mTweet.getTweetId(), mTweetJSONReponseHandler);
                    }
                }
            }
        });
        btnShare = (ImageButton) findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShareItem(mTweet);
            }
        });
        btnDelete = (ImageButton) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTweet!=null){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(TweetDetailActivity.this);
                    dialog.setTitle("Delete").setMessage("Are you sure you want to delete?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mClient.deleteTweet(mTweet.getTweetId(), new JsonHttpResponseHandler(){
                                        @Override
                                        public void onSuccess(JSONObject jsonObject) {
                                            Tweet.delete(Tweet.class,mTweet.getId());
                                            onBackPressed();
                                        }

                                        @Override
                                        public void onFailure(Throwable throwable, JSONObject jsonObject) {
                                            super.onFailure(throwable, jsonObject);
                                            Toast.makeText(TweetDetailActivity.this, "Failed to delete tweet. Please try again later", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", null).show();

                }
            }
        });
        tvRetweets = (TextView) findViewById(R.id.tvRetweets);
        tvFavorites = (TextView) findViewById(R.id.tvFavorites);
        ivVerified = (ImageView) findViewById(R.id.ivVerified);
        btnFollowing = (ImageButton) findViewById(R.id.btnFollow);
        ivMedia = (ImageView) findViewById(R.id.ivMedia);

        ivMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTweet != null) {
                    Intent intent = new Intent(TweetDetailActivity.this, ImageActivity.class);
                    intent.putExtra("media_url", mTweet.getMediaURL());
                    startActivity(intent);
                }
            }
        });

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = tvUserNameHandle.getText().toString();
                DialogFragment fragment = CreateTweetFragment.getInstance(status);
                fragment.show(getFragmentManager(), "dialog");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTweetSubmit() {
        //DO NOTHING
    }

    // Can be triggered by a view event such as a button press
    public void onShareItem(Tweet tweet) {
        if(tweet!=null) {
            String text = "Check out "+tweet.getUser().getScreenName()+"'s Tweet: https://twitter.com/"+tweet.getUser().getScreenName().substring(1)+"/status/"+tweet.getTweetId();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            // Launch sharing dialog for image
            startActivity(Intent.createChooser(sendIntent, "Share Tweet"));
        }
    }
}
