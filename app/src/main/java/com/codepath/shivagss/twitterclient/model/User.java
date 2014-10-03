package com.codepath.shivagss.twitterclient.model;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sandeep on 9/27/2014.
 */
@Table(name = "Users")
public class User extends Model {

    private static final String TAG = User.class.getName().toString();
    @Column(name = "user_name")
    public String name;
    @Column(name = "user_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String userID;
    @Column(name = "screen_name")
    public String screenName;
    @Column(name = "url")
    public String url;
    @Column(name = "following")
    public boolean following;
    @Column(name = "current_user")
    public boolean currentUser;

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
            user.userID = jsonObject.getString("id_str");
            user.screenName = "@"+jsonObject.getString("screen_name");
            user.url = jsonObject.getString("profile_image_url");
            if(!jsonObject.isNull("following"))
                user.following = jsonObject.getBoolean("following");
            if(!jsonObject.isNull("verified"))
                user.verified = jsonObject.getBoolean("verified");
            if(!jsonObject.isNull("current_user")){
                user.currentUser = true;
            }else{
                user.currentUser = false;
            }
        } catch (JSONException ex) {
            Log.e(TAG, ex.toString());
            return null;
        }

        User dbUser = User.getUser(user.getUserId());
        if(dbUser != null){
            return dbUser;
//            new Update(User.class)
//                    .set("user_name = '"+user.name+"'")
//                    .where("user_id = '"+user.userID+"'");
        }
        user.save();
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userID;
    }

    public void setUserId(String id) {
        this.userID = id;
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


    public boolean isCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        this.currentUser = currentUser;
    }

    public static User getUser(String userID){
        return new Select()
                .from(User.class)
                .where("user_id = ?", userID)
                .executeSingle();
    }

    public static User getCurrentUser(){
        return new Select()
                .from(User.class)
                .where("current_user = 1")
                .executeSingle();
    }
}
