package com.codepath.shivagss.twitterclient.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by Sandeep on 9/28/2014.
 */
public class TimeLineTweetsAdapter extends ArrayAdapter<Tweet> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private String mLoggedInUserID = "0";
    private View sProgress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyle);

    private class ViewHolder {
        ImageView ivRetweetIcon;
        ImageView ivProfilePic;
        TextView tvUserRetweetName;
        TextView tvUserName;
        TextView tvUserNameHandle;
        TextView tvBodyText;
        TextView tvCreatedAtTime;
        Button btnReply;
        Button btnRetweet;
        Button btnFavorite;
        Button btnFollowing;
        ImageView ivMedia;

    }

    protected ArrayList<Tweet> dataList;

    public TimeLineTweetsAdapter(Context context, ArrayList<Tweet> objects) {
        super(context, R.layout.item_tweet, objects);
        dataList = objects;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEW_TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return dataList.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position >= dataList.size()) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public Tweet getItem(int position) {
        return (getItemViewType(position) == VIEW_TYPE_ITEM) ? dataList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return (getItemViewType(position) == VIEW_TYPE_ITEM) ? position : -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(getItemViewType(position) == VIEW_TYPE_LOADING){
            getFooterView();
        }

        Tweet tweet = getItem(position);

        if(tweet == null) return getFooterView();

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.item_tweet, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ivRetweetIcon = (ImageView) convertView.findViewById(R.id.ivRetweetUserIcon);
            viewHolder.ivProfilePic = (ImageView) convertView.findViewById(R.id.ivProfilePic);
            viewHolder.tvUserRetweetName = (TextView) convertView.findViewById(R.id.tvUserRetweetName);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.tvUserNameHandle = (TextView) convertView.findViewById(R.id.tvUserNameHandle);
            viewHolder.tvBodyText = (TextView) convertView.findViewById(R.id.tvBodyText);
            viewHolder.tvCreatedAtTime = (TextView) convertView.findViewById(R.id.tvCreatedAtTime);
            viewHolder.btnReply = (Button) convertView.findViewById(R.id.btnReply);
            viewHolder.btnRetweet = (Button) convertView.findViewById(R.id.btnRetweet);
            viewHolder.btnFavorite = (Button) convertView.findViewById(R.id.btnFavorite);
            viewHolder.btnFollowing = (Button) convertView.findViewById(R.id.btnFollow);
            viewHolder.ivMedia = (ImageView) convertView.findViewById(R.id.ivMedia);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (tweet.hasRetweetedUsername()) {
            viewHolder.ivRetweetIcon.setVisibility(View.VISIBLE);
            viewHolder.tvUserRetweetName.setVisibility(View.VISIBLE);
            viewHolder.tvUserRetweetName.setText(getContext().getString(R.string.retweeted_user_name
                    , tweet.getRetweetedUserName()));
        } else {
            viewHolder.ivRetweetIcon.setVisibility(View.GONE);
            viewHolder.tvUserRetweetName.setVisibility(View.GONE);
        }

        if(tweet.getUser().isFollowing() || tweet.getUser().getUserId().equals(mLoggedInUserID)){
            viewHolder.btnFollowing.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.btnFollowing.setVisibility(View.VISIBLE);
        }

        if(tweet.isFavorited()){
            viewHolder.btnFavorite.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_tweet_action_inline_favorite_on), null, null, null);
        }else{
            viewHolder.btnFavorite.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_tweet_action_inline_favorite_off), null, null, null);
        }

        if(tweet.isRetweeted()){
            viewHolder.btnRetweet.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_tweet_action_inline_retweet_on), null, null, null);
        }else{
            viewHolder.btnRetweet.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(R.drawable.ic_tweet_action_inline_retweet_off), null, null, null);

            if(tweet.getUser().getUserId() == mLoggedInUserID){
                viewHolder.btnRetweet.setAlpha(50);
                viewHolder.btnRetweet.setEnabled(false);
            }else{
                viewHolder.btnRetweet.setAlpha(255);
                viewHolder.btnRetweet.setEnabled(true);
            }
        }

        viewHolder.tvUserName.setText(tweet.getUser().getName());
        viewHolder.tvUserNameHandle.setText(tweet.getUser().getScreenName());
        viewHolder.tvCreatedAtTime.setText(tweet.getCreatedAt());
        viewHolder.tvBodyText.setText(Html.fromHtml(tweet.getBody()));

        viewHolder.btnRetweet.setText(tweet.getRetweetCount());
        viewHolder.btnFavorite.setText(tweet.getFavoriteCount());

        viewHolder.ivProfilePic.setImageResource(0);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(tweet.getUser().getUrl(), viewHolder.ivProfilePic);

        if(TextUtils.isEmpty(tweet.getMediaURL()) || !Utils.isNetworkAvailable(getContext())){
            viewHolder.ivMedia.setVisibility(View.GONE);
        }else{
            viewHolder.ivMedia.setImageResource(R.drawable.ic_launcher);
            viewHolder.ivMedia.setVisibility(View.VISIBLE);
            imageLoader.displayImage(tweet.getMediaURL(), viewHolder.ivMedia);
        }

        return convertView;
    }

    private View getFooterView() {
        return sProgress;
    }

    public String getmLoggedInUserID() {
        return mLoggedInUserID;
    }

    public void setmLoggedInUserID(String mLoggedInUserID) {
        this.mLoggedInUserID = mLoggedInUserID;
    }
}
