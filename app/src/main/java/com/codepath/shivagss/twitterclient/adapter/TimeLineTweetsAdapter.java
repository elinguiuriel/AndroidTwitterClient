package com.codepath.shivagss.twitterclient.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by Sandeep on 9/28/2014.
 */
public class TimeLineTweetsAdapter extends ArrayAdapter<Tweet> {

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

    }

    public TimeLineTweetsAdapter(Context context, ArrayList objects) {
        super(context, R.layout.item_tweet, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) return convertView;

        Tweet tweet = getItem(position);
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

        if(tweet.getUser().isFollowing()){
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

        return convertView;
    }
}
