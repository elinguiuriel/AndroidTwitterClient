package com.codepath.shivagss.twitterclient.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.shivagss.twitterclient.R;
import com.codepath.shivagss.twitterclient.model.User;
import com.codepath.shivagss.twitterclient.TwitterClientApp;
import com.codepath.shivagss.twitterclient.restclient.TwitterRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

/**
 * Created by Sandeep on 9/29/2014.
 */
public class CreateTweetFragment extends DialogFragment {

    private static final String TAG = CreateTweetFragment.class.getName().toString();
    private onTweetListener mCallback;
    private ImageButton mBtnBack;
    private ImageView mProfilePic;
    private TextView mProfileHandle;
    private Button mBtnSubmit;
    private TextView mTweetCount;
    private EditText mEtStatus;
    private TwitterRestClient mClient;

    private static String sStatus;

    public CreateTweetFragment(){}

    public static CreateTweetFragment getInstance(String status){
        sStatus = status;
        return new CreateTweetFragment();
    }

    public interface onTweetListener {
        public void onTweetSubmit();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (onTweetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onTweetListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_tweet, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);


        mClient = TwitterClientApp.getRestClient();
        mClient.getUserCredentials(new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject jsonObject) {
                User user = User.fromJson(jsonObject);
                mProfileHandle.setText(user.getScreenName());
                ImageLoader imageLoader = ImageLoader.getInstance();
                mProfilePic.setImageResource(0);
                imageLoader.displayImage(user.getUrl(), mProfilePic);
            }

            @Override
            public void onFailure(Throwable throwable, JSONObject jsonObject) {
                Toast.makeText(getDialog().getContext(), "Failed to receive username", Toast.LENGTH_LONG).show();
                Log.e(TAG, throwable.toString());
            }
        });

        setupViews(view);
        return view;
    }

    private void setupViews(View view) {
        mBtnBack = (ImageButton) view.findViewById(R.id.btnBack);
        mProfilePic = (ImageView) view.findViewById(R.id.ivProfilePic);
        mProfileHandle = (TextView) view.findViewById(R.id.tvUserNameHandle);
        mBtnSubmit = (Button) view.findViewById(R.id.btnTweet);
        mTweetCount = (TextView) view.findViewById(R.id.tvTweetCount);
        mEtStatus = (EditText) view.findViewById(R.id.etTweet);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        mBtnSubmit.setEnabled(false);
        mBtnSubmit.getBackground().setAlpha(50);
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTweet();
            }
        });

        mEtStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mEtStatus.getText().toString();
                mTweetCount.setText(""+(140 - text.length()));
                if(text.length() > 0 && text.length() <= 140){
                    mBtnSubmit.setEnabled(true);
                    mBtnSubmit.getBackground().setAlpha(255);
                }else{
                    mBtnSubmit.setEnabled(false);
                    mBtnSubmit.getBackground().setAlpha(50);
                }
                if(text.length() > 140){
                    mTweetCount.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }else{
                    mTweetCount.setTextColor(getResources().getColor(R.color.secondary_text_color));
                }
            }
        });
        if(!TextUtils.isEmpty(sStatus)){
            mEtStatus.setText("");
            mEtStatus.append(sStatus + " ");
        }
    }

    private void submitTweet() {
        mClient.postTweetUpdate(mEtStatus.getText().toString(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject jsonObject) {
                mCallback.onTweetSubmit();
                getDialog().dismiss();
            }

            @Override
            public void onFailure(Throwable throwable, JSONObject jsonObject) {
                Toast.makeText(getDialog().getContext(), "Failed to submit the tweet. Please try again later.", Toast.LENGTH_LONG).show();
                Log.e(TAG, throwable.toString());
            }
        });
    }
}
