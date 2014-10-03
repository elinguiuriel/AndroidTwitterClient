package com.codepath.shivagss.twitterclient.model;

import android.text.TextUtils;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.codepath.shivagss.twitterclient.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by Sandeep on 9/27/2014.
 */
@Table(name = "Tweets")
public class Tweet extends Model {

    private static final String TAG = Tweet.class.getName().toString();
    @Column(name = "tweet_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String tweetID;
    @Column(name = "user", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public User user;
    @Column(name = "created_at")
    public String createdAt;
    @Column(name = "body")
    public String body;
    @Column(name = "retweeted_username")
    public String retweetedUserName;
    @Column(name = "retweet_count")
    public String retweetCount;
    @Column(name = "favorite_count")
    public String favoriteCount;
    @Column(name = "favorited")
    public boolean favorited;
    @Column(name = "retweeted")
    public boolean retweeted;
    @Column(name = "media_url")
    public String mediaURL;

    public String getMediaURL() {
        return mediaURL;
    }

    public void setMediaURL(String mediaURL) {
        this.mediaURL = mediaURL;
    }

    public static Tweet fromJson(JSONObject tweet) {

        Tweet _tweet = new Tweet();
        try {

            _tweet.body = tweet.getString("text").replace("\n","<br>");
            _tweet.tweetID = tweet.getString("id_str");
            Log.d("TEST", "Adding = " +_tweet.tweetID);
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

            if(!tweet.getJSONObject("entities").isNull("media")){
                JSONArray array = tweet.getJSONObject("entities").getJSONArray("media");
                _tweet.mediaURL = array.getJSONObject(0).getString("media_url");
            }

        } catch (JSONException ex) {
            Log.e("ERR", ex.toString());
            return null;
        }
        _tweet.save();
        return _tweet;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTweetId() {
        return tweetID;
    }

    public void setTweetId(String id) {
        this.tweetID = id;
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

    public static Tweet getTweet(long tweetID){
        return new Select()
                .from(Tweet.class)
                .where("tweet_id = ?", tweetID)
                .orderBy("tweet_id DESC")
                .executeSingle();
    }


    public static List<Tweet> getTweetsList(int count, long maxID){
        return getTweetsList(count, 1, maxID);
    }

    public static List<Tweet> getTweetsList(long maxID){
        return getTweetsList(20, 1, maxID);
    }

    public static List<Tweet> getTweetsList(int count){
        return getTweetsList(count, 1, -1);
    }

    public static List<Tweet> getTweetsList(int count, long sinceID, long maxID){

        if(count <= 0) return new ArrayList<Tweet>();

        String whereClause = String.format("tweet_id >= " + sinceID);
        if(maxID > 0){
            whereClause += String.format(" AND tweet_id < "+ maxID);
        }
        From sql = new Select()
                .from(Tweet.class)
                .where(whereClause)
                .orderBy("tweet_id DESC")
                .limit(count);
        Log.d("Tweet", sql.toSql());
        return sql.execute();
    }

    public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
        ArrayList<Tweet> _list = new ArrayList<Tweet>();
        ActiveAndroid.beginTransaction();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    JSONObject tweet = jsonArray.getJSONObject(i);
                    _list.add(fromJson(tweet));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }
            }
            ActiveAndroid.setTransactionSuccessful();
        }finally {
            ActiveAndroid.endTransaction();
        }

        return _list;
    }

    @Override
    public String toString() {
             return "tweetID=" + super.getId();
    }
}
