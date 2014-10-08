package com.codepath.shivagss.twitterclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.fragment.TweetListFragment;

public class DisplayTweetsListActivity extends FragmentActivity implements TweetListFragment.OnFragmentInteractionListener {

    private String userID;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_place_holder);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        userID = intent.getStringExtra("user_id");

        // Check what fragment is currently shown, replace if needed.
        TweetListFragment details = (TweetListFragment)
                getSupportFragmentManager().findFragmentById(R.id.lyFragment);
        if (details == null) {
            // Make new fragment to show this selection.
            details = TweetListFragment.newInstance(type);
            Bundle bundle = details.getArguments();
            bundle.putString("user_id", userID);

            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.lyFragment, details);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fragment_place_holder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
