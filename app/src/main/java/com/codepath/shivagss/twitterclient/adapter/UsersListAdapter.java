package com.codepath.shivagss.twitterclient.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.codepath.shivagss.twitterclient.activity.ProfileActivity;
import com.codepath.shivagss.twitterclient.model.Tweet;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by Sandeep on 9/28/2014.
 */
public class UsersListAdapter extends ArrayAdapter<User> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private View sProgress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyle);

    public boolean isNoMoreData() {
        return noMoreData;
    }

    public void setNoMoreData(boolean noMoreData) {
        this.noMoreData = noMoreData;
    }

    private boolean noMoreData;

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    private boolean error;

    private void startProfileActivity(String userID) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra("user_id", userID);
        getContext().startActivity(intent);

    }

    private class ViewHolder {
        ImageView ivProfilePic;
        TextView tvUserName;
        TextView tvUserNameHandle;
        TextView tvBodyText;
        ImageView btnFollowing;

    }

    protected ArrayList<User> dataList;

    public UsersListAdapter(Context context, ArrayList<User> objects) {
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
    public User getItem(int position) {
        return (getItemViewType(position) == VIEW_TYPE_ITEM) ? dataList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return (getItemViewType(position) == VIEW_TYPE_ITEM) ? position : -1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(getItemViewType(position) == VIEW_TYPE_LOADING){
            getFooterView();
        }

        final User user = getItem(position);

        if(user == null) return getFooterView();

        return getTweetItemView(convertView, parent, user);
    }

    public View getTweetItemView(View convertView, ViewGroup parent, final User user) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.item_user, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.ivProfilePic = (ImageView) convertView.findViewById(R.id.ivProfilePic);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.tvUserNameHandle = (TextView) convertView.findViewById(R.id.tvUserNameHandle);
            viewHolder.tvBodyText = (TextView) convertView.findViewById(R.id.tvBodyText);
            viewHolder.btnFollowing = (ImageView) convertView.findViewById(R.id.ivFollowing);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(user.isFollowing()){
            viewHolder.btnFollowing.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.btnFollowing.setVisibility(View.VISIBLE);
        }

        viewHolder.tvUserName.setText(user.getName());
        viewHolder.tvUserNameHandle.setText(user.getScreenName());
        viewHolder.tvBodyText.setText(Html.fromHtml(user.getDescription()));

        viewHolder.ivProfilePic.setImageResource(0);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(user.getUrl(), viewHolder.ivProfilePic);

        viewHolder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProfileActivity(user.getUserId());
            }
        });

        return convertView;
    }

    private View getFooterView() {
        if (noMoreData || error) {
            // the ListView has reached the last row
            ImageView ivLastRow = new ImageView(getContext());
            int icon = R.drawable.ic_action_error;
            if(noMoreData){
                icon = R.drawable.ic_stat_twitter;
            }
            ivLastRow.setImageResource(icon);
            ivLastRow.setAdjustViewBounds(true);
            return ivLastRow;
        }
        return sProgress;
    }
}
