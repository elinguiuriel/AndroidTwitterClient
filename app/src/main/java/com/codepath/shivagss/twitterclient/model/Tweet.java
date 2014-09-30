package com.codepath.shivagss.twitterclient.model;

import android.text.TextUtils;
import android.util.Log;

import com.codepath.shivagss.twitterclient.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Sandeep on 9/27/2014.
 */
public class Tweet {

    private static final String TAG = Tweet.class.getName().toString();
    private long id;
    private User user;
    private String createdAt;
    private String body;
    private String retweetedUserName;
    private String retweetCount;
    private String favoriteCount;
    private boolean favorited;
    private boolean retweeted;

    public static Tweet fromJson(JSONObject tweet) {

        Tweet _tweet = new Tweet();
        try {

            _tweet.body = tweet.getString("text").replace("\n","<br>");
            _tweet.id = tweet.getLong("id");
            _tweet.createdAt = tweet.getString("created_at");
            _tweet.favorited = tweet.getBoolean("favorited");
            _tweet.retweeted = tweet.getBoolean("retweeted");

            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            _tweet.favoriteCount = formatter.format(tweet.getLong("favorite_count"));
            _tweet.retweetCount = formatter.format(tweet.getLong("retweet_count"));

            _tweet.user = User.fromJson(tweet.getJSONObject("user"));

            if (!tweet.isNull("retweeted_status")) {
                _tweet.retweetedUserName = _tweet.user.getName();
                JSONObject retweetStatus = tweet.getJSONObject("retweeted_status");
                _tweet.user = User.fromJson(retweetStatus.getJSONObject("user"));
                _tweet.favoriteCount = formatter.format(retweetStatus.getLong("favorite_count"));
            }

        } catch (JSONException ex) {
            Log.e("ERR", ex.toString());
            return null;
        }
        return _tweet;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return Utils.getRelativeTimeAgo(createdAt);
    }

    public String getTimeStamp() {
        return Utils.getTimeStamp(createdAt);
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public String getRetweetedUserName() {
        return retweetedUserName;
    }

    public void setRetweetedUserName(String retweetedUserName) {
        this.retweetedUserName = retweetedUserName;
    }

    public String getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(String retweetCount) {
        this.retweetCount = retweetCount;
    }

    public String getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(String favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public boolean hasRetweetedUsername() {
        return !TextUtils.isEmpty(this.getRetweetedUserName());

    }

    public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
        ArrayList<Tweet> _list = new ArrayList<Tweet>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject tweet = jsonArray.getJSONObject(i);
                _list.add(fromJson(tweet));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }
        return _list;
    }
}
