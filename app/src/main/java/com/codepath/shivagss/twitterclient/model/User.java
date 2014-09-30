package com.codepath.shivagss.twitterclient.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sandeep on 9/27/2014.
 */
public class User {

    private static final String TAG = User.class.getName().toString();
    private String name;
    private long id;
    private String screenName;
    private String url;
    private boolean following;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    private boolean verified;

    public static User fromJson(JSONObject jsonObject) {
        User user = new User();
        try {

            user.name = jsonObject.getString("name");
            user.id = jsonObject.getLong("id");
            user.screenName = "@"+jsonObject.getString("screen_name");
            user.url = jsonObject.getString("profile_image_url");
            if(!jsonObject.isNull("following"))
                user.following = jsonObject.getBoolean("following");
            if(!jsonObject.isNull("verified"))
                user.verified = jsonObject.getBoolean("verified");
        } catch (JSONException ex) {
            Log.e(TAG, ex.toString());
            return null;
        }
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFollowing() {
        return following;
    }

    public void setFollowing(boolean following) {
        this.following = following;
    }
}
