package com.codepath.shivagss.twitterclient.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.codepath.oauth.OAuthLoginActivity;
import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;

public class TwitterLoginActivity extends OAuthLoginActivity<TwitterRestClient> {

    private ProgressDialog mPRogressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

       Intent intent = getIntent();

        if(intent.getData() != null && intent.getData().getScheme().equals("oauth")) {
            mPRogressBar = new ProgressDialog(this);
            mPRogressBar.setMessage("Signing in...");
            mPRogressBar.show();
        }
    }

    // OAuth authenticated successfully, launch primary authenticated activity
    // i.e Display application "homepage"
    @Override
    public void onLoginSuccess() {
        Intent i = new Intent(this, TimeLineActivity.class);
        startActivity(i);
    }

    // OAuth authentication flow failed, handle the error
    // i.e Display an error dialog or toast
    @Override
    public void onLoginFailure(Exception e) {
        e.printStackTrace();
    }

    // Click handler method for the button used to start OAuth flow
    // Uses the client to initiate OAuth authorization
    // This should be tied to a button used to login
    public void login(View view) {
        getClient().connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPRogressBar != null) {
            mPRogressBar.dismiss();
        }
    }
}
