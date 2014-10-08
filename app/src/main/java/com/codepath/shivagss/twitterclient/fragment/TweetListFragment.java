package com.codepath.shivagss.twitterclient.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.TwitterClientApp;
import com.codepath.shivagss.twitterclient.activity.TweetDetailActivity;
import com.codepath.shivagss.twitterclient.adapter.TimeLineTweetsAdapter;
import com.codepath.shivagss.twitterclient.adapter.UsersListAdapter;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;
import com.codepath.shivagss.twitterclient.utils.EndlessScrollListener;
import com.codepath.shivagss.twitterclient.utils.Utils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TweetListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TweetListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TweetListFragment extends Fragment {

    private static final String TAG = TweetListFragment.class.getName();
    private static final String ARG_PARAM1 = "type";
    private static final int HOME_TWEETS_TYPE = 1 << 0;
    private static final int MENTIONS_TWEETS_TYPE = 1 << 1;
    public static final int USER_TWEETS_TYPE = 1 << 2;
    private final CountDownLatch mNetworkRequestDoneSignal = new CountDownLatch(1);
    ArrayList<Tweet> mTweetsList;
    TimeLineTweetsAdapter mTweetsAdapter;
    ArrayList<User> mUsersList;
    UsersListAdapter mUsersAdapter;
    ListView lvTweets;
    private TwitterRestClient mClient;
    private SwipeRefreshLayout swipeContainer;

    private OnFragmentInteractionListener mListener;
    private String mParam1;

    private String maxID;
    public JsonHttpResponseHandler mTweetsJsonResponseHandler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONArray jsonArray) {
            ArrayList<Tweet> list = Tweet.fromJson(jsonArray, mType);
            updateTweetsAdapter(list);
//            populateTimeLine(maxID, false);
        }

        @Override
        public void onFailure(Throwable throwable, JSONObject jsonObject) {
            Toast.makeText(getActivity(), "Failed to load new tweets from network", Toast.LENGTH_LONG).show();
            Log.e(TAG, throwable.toString());
            mUsersAdapter.setError(true);
            mTweetsAdapter.notifyDataSetChanged();
            mNetworkRequestDoneSignal.countDown();
//            populateTimeLine(maxID, false);
        }
    };

    public JsonHttpResponseHandler mSearchJsonResponseHandler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONObject jsonObject) {
            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray("statuses");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayList<Tweet> list = Tweet.fromJson(jsonArray, mType);
            updateTweetsAdapter(list);
//            populateTimeLine(maxID, false);
        }

        @Override
        public void onFailure(Throwable throwable, JSONObject jsonObject) {
            Toast.makeText(getActivity(), "Failed to load new tweets from network", Toast.LENGTH_LONG).show();
            Log.e(TAG, throwable.toString());
            mNetworkRequestDoneSignal.countDown();
            mUsersAdapter.setError(true);
            mTweetsAdapter.notifyDataSetChanged();
//            populateTimeLine(maxID, false);
        }
    };

    public JsonHttpResponseHandler mUsersJsonResponseHandler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONObject jsonObject) {
            JSONArray jsonArray = null;
            try {
                maxID = jsonObject.getString("next_cursor");
                jsonArray = jsonObject.getJSONArray("users");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayList<User> list = User.fromJson(jsonArray);
            if (TextUtils.isEmpty(maxID)) {
                mUsersAdapter.clear();
            }
            if (list.isEmpty()) {
                mUsersAdapter.setNoMoreData(true);
                mUsersAdapter.notifyDataSetChanged();
                mUsersAdapter.setError(false);
            } else {
                mUsersAdapter.setNoMoreData(false);
                mUsersAdapter.addAll(list);
            }
            mNetworkRequestDoneSignal.countDown();
//            populateTimeLine(maxID, false);
        }

        @Override
        public void onFailure(Throwable throwable, JSONObject jsonObject) {
            Toast.makeText(getActivity(), "Failed to load new tweets from network", Toast.LENGTH_LONG).show();
            Log.e(TAG, throwable.toString());
            mNetworkRequestDoneSignal.countDown();
            mUsersAdapter.setError(true);
            mUsersAdapter.notifyDataSetChanged();
//            populateTimeLine(maxID, false);
        }
    };

    private String mUserID;
    private String mSearchString;
    private int mType;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TweetListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TweetListFragment newInstance(String param1) {
        TweetListFragment fragment = new TweetListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public TweetListFragment() {
        // Required empty public constructor
    }

    public String getSearchString() {
        return mSearchString;
    }

    public void setSearchString(String mSearchString) {
        this.mSearchString = mSearchString;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            if(mParam1.equals("home")){
                mType = HOME_TWEETS_TYPE;
            }else if(mParam1.equals("mentions")){
                mType = MENTIONS_TWEETS_TYPE;
            }else if(mParam1.equals("user")){
                mType = USER_TWEETS_TYPE;
            }
            mUserID = getArguments().getString("user_id");
            mSearchString = getArguments().getString("search_string");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tweet_list, container, false);
        mClient = TwitterClientApp.getRestClient();

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                populateTimeLine(null, true);
                loadTweetsFromNetwork(null);
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        lvTweets = (ListView) view.findViewById(R.id.lvTweets);

        if (mParam1.equals("friends") || mParam1.equals("followers")) {

            getActivity().getActionBar().setTitle("F" + mParam1.substring(1));

            mUsersList = new ArrayList<User>();
            mUsersAdapter = new UsersListAdapter(getActivity(), mUsersList);
            lvTweets.setAdapter(mUsersAdapter);
            lvTweets.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    if (mUsersList.size() > 0) {
                        String max = maxID;
//                    populateTimeLine("" + (maxID), false);
                        loadTweetsFromNetwork(max);
                    }
                }
            });
        } else {
            mTweetsList = new ArrayList<Tweet>();
            mTweetsAdapter = new TimeLineTweetsAdapter(getActivity(), mTweetsList);
            lvTweets.setAdapter(mTweetsAdapter);

            lvTweets.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    if (mTweetsList.size() > 0) {
                        String max = mTweetsList.get(mTweetsList.size() - 1).getTweetId();
//                    populateTimeLine("" + (maxID), false);
                        loadTweetsFromNetwork(max);
                    }
                }
            });
            lvTweets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Tweet tweet = mTweetsAdapter.getItem(position);
                    if (tweet != null) {
                        String tweetID = tweet.getTweetId();
                        startTweetDetailsActivity(tweetID);
                    }
                }
            });
        }

        lvTweets.setEmptyView(view.findViewById(android.R.id.empty));

        view.clearFocus();

        loadTweetsFromNetwork(null);
        return view;
    }

    private void startTweetDetailsActivity(String tweetID) {
        Intent intent = new Intent(getActivity(), TweetDetailActivity.class);
        intent.putExtra("tweet_id", tweetID);
        startActivity(intent);
    }

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
//            loadTweetsFromNetwork(true);
//        } else {
//            mTweetsAdapter.addAll(list);
//        }
//    }

    private void loadTweetsFromNetwork(String max) {
        loadTweetsFromNetwork(max, false);
    }

    private void loadTweetsFromNetwork(String max, final boolean await) {

        boolean isActiveNetwork = Utils.isNetworkAvailable(getActivity());

        maxID = max;
        if (mParam1.equals("home")) {
            if (isActiveNetwork) {
                mClient.getHomeTimeLine(max, mTweetsJsonResponseHandler);
            } else {
                List<Tweet> list = Tweet.getTweetsList(maxID, HOME_TWEETS_TYPE);
                updateTweetsAdapter(list);
                Toast.makeText(getActivity(), "No Internet connection. In Offline mode.", Toast.LENGTH_LONG).show();
            }
        } else if (mParam1.equals("mentions")) {
            if (isActiveNetwork) {
                mClient.getMentionsTimeLine(max, mTweetsJsonResponseHandler);
            } else {
                List<Tweet> list = Tweet.getTweetsList(maxID, MENTIONS_TWEETS_TYPE);
                updateTweetsAdapter(list);
                Toast.makeText(getActivity(), "No Internet connection. In Offline mode.", Toast.LENGTH_LONG).show();
            }
        } else if (mParam1.equals("user")) {
                if (isActiveNetwork) {
                    mClient.getUserTimeLine(mUserID, max, mTweetsJsonResponseHandler);
                } else {
                    List<Tweet> list = Tweet.getTweetsList(maxID, USER_TWEETS_TYPE);
                    updateTweetsAdapter(list);
                    Toast.makeText(getActivity(), "No Internet connection. In Offline mode.", Toast.LENGTH_LONG).show();
                }
        } else if (mParam1.equals("favorites")) {
            if (isActiveNetwork) {
                mClient.getFavoritesTimeLine(mUserID, max, mTweetsJsonResponseHandler);
            } else {
                Toast.makeText(getActivity(), "No Internet connection. Please try again later.", Toast.LENGTH_LONG).show();
            }
        } else if (mParam1.equals("friends")) {
            if (isActiveNetwork) {
                mClient.getFriendsTimeLine(mUserID, max, mUsersJsonResponseHandler);
            } else {
                Toast.makeText(getActivity(), "No Internet connection. Please try again later.", Toast.LENGTH_LONG).show();
            }
        } else if (mParam1.equals("followers")) {
            if (isActiveNetwork) {
                mClient.getFollowersTimeLine(mUserID, max, mUsersJsonResponseHandler);
            } else {
                Toast.makeText(getActivity(), "No Internet connection. Please try again later.", Toast.LENGTH_LONG).show();
            }
        } else if (mParam1.equals("search_top")) {
            if (isActiveNetwork) {
                mClient.getSearchTopTweets(mSearchString, max, mSearchJsonResponseHandler);
            } else {
                Toast.makeText(getActivity(), "No Internet connection. Please try again later.", Toast.LENGTH_LONG).show();
            }
        } else if (mParam1.equals("search_all")) {
            if (isActiveNetwork) {
                mClient.getSearchAllTweets(mSearchString, max, mSearchJsonResponseHandler);
            } else {
                Toast.makeText(getActivity(), "No Internet connection. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }

        try {
            if (await) {
                mNetworkRequestDoneSignal.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateTweetsAdapter(List<Tweet> list) {
        if (TextUtils.isEmpty(maxID)) {
            mTweetsAdapter.clear();
        }
        if (list.isEmpty()) {
            mTweetsAdapter.setNoMoreData(true);
            mTweetsAdapter.notifyDataSetChanged();
            mTweetsAdapter.setError(false);
        } else {
            mTweetsAdapter.setNoMoreData(false);
            mTweetsAdapter.addAll(list);
        }
        mNetworkRequestDoneSignal.countDown();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
