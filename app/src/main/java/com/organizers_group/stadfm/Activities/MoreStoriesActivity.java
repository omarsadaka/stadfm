package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.organizers_group.stadfm.Adapters.MoreStoriesAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.EndlessRecyclerViewScrollListener;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class MoreStoriesActivity extends AppCompatActivity {

    RecyclerView storiesRecyclerView;
    List<NewsFeed> feedList;
    int checkSize;
    MoreStoriesAdapter moreStoriesAdapter;
    ImageView back;
    ImageView search;
    RelativeLayout headerLayout;
    TextView headerTitle;
    ShimmerLayout shimmerLayout;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_more_stories);

        feedList = new ArrayList<>(  );
        progressBar = findViewById ( R.id.pBLoading );

        shimmerLayout = findViewById ( R.id.shimmer_view_container );

        back = findViewById ( R.id.newsfeedNavBack );
        search = findViewById ( R.id.newsfeedSearch );
        back.setOnClickListener (v -> onBackPressed ());
        search.setOnClickListener (v -> startActivity ( new Intent( MoreStoriesActivity.this , SearchActivity.class ) ));

        headerLayout = findViewById(R.id.headerLayout);
        headerTitle = findViewById(R.id.headerTitle);

        storiesRecyclerView = findViewById(R.id.moreStoriesRecyclerview);

        storiesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager storiesLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);
        storiesRecyclerView.setLayoutManager(storiesLayoutManager);

        // get mixed posts
        final int storiesPage = 1;
        moreStoriesAdapter = new MoreStoriesAdapter(this, getMixedPosts(storiesPage));
        storiesRecyclerView.setAdapter(moreStoriesAdapter);
        moreStoriesAdapter.notifyDataSetChanged();

        // Retain an instance so that you can call `resetState()` for fresh searches
        EndlessRecyclerViewScrollListener mixedScrollListener = new EndlessRecyclerViewScrollListener(storiesLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                getMixedPosts(page);
                showProgressView();
            }
        };
        // Adds the scroll listener to RecyclerView
        storiesRecyclerView.addOnScrollListener(mixedScrollListener);
        // End Of Mixed Stories
}

    public List<NewsFeed> getMixedPosts(int storiesPage){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
        String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);
        if (userID != null) {
            JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.USER_ARTICLES_MIXED + userID + "/?p=" + String.valueOf(storiesPage),
                    response1 -> {
                        try {
                            for (int i = 0; i < response1.length(); i++) {
                                JSONObject postsObjs = response1.getJSONObject(i);

                                if (postsObjs.getString("url").equals("")){
                                    hideProgressView();
                                    Toast.makeText(this, R.string.no_more_stories, Toast.LENGTH_SHORT).show();
                                }else {

                                    NewsFeed newsFeed = new NewsFeed();
                                    newsFeed.setPostID(postsObjs.getInt("id"));
                                    newsFeed.setTitle(postsObjs.getString("title"));
                                    newsFeed.setDescription(postsObjs.getString("content"));
                                    newsFeed.setCategory(postsObjs.getString("topic"));
                                    newsFeed.setPostImgUrl(postsObjs.getString("poster"));
                                    newsFeed.setPostURl(postsObjs.getString("url"));
                                    newsFeed.setPostedSince(postsObjs.getString("post_date"));

                                    int readingTime = postsObjs.getJSONObject("timef").getInt("min");
                                    if ( readingTime == 0){
                                        newsFeed.setReadingTime(getString(R.string.thirteen_seconds));
                                    }else if (readingTime == 1){
                                        newsFeed.setReadingTime(String.valueOf(readingTime) + getString(R.string.minute_reading));
                                    } else {
                                        newsFeed.setReadingTime(String.valueOf(readingTime) + getString(R.string.minutes_reading));
                                    }
                                    // add newsFeed to the ArrayList
                                    feedList.add(newsFeed);
                                }

                            }
                            shimmerLayout.stopShimmerAnimation ();
                            shimmerLayout.setVisibility ( View.GONE );
                            hideProgressView();
                            moreStoriesAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        if(error instanceof NoConnectionError){
                            ConnectivityManager cm = (ConnectivityManager)MoreStoriesActivity.this.getSystemService( Context.CONNECTIVITY_SERVICE);
                            NetworkInfo activeNetwork = null;
                            if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                                Toast.makeText(MoreStoriesActivity.this, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                            else Toast.makeText(MoreStoriesActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                            Toast.makeText(MoreStoriesActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                        } else if (error.getCause() instanceof MalformedURLException){
                            Toast.makeText(MoreStoriesActivity.this, R.string.bad_request, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                                || error.getCause() instanceof JSONException
                                || error.getCause() instanceof XmlPullParserException){

                            Toast.makeText(MoreStoriesActivity.this, R.string.parse_error, Toast.LENGTH_SHORT).show();
                        } else if (error.getCause() instanceof OutOfMemoryError){
                            Toast.makeText(MoreStoriesActivity.this, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                        }else if (error instanceof AuthFailureError){
                            Toast.makeText(MoreStoriesActivity.this, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                            Toast.makeText(MoreStoriesActivity.this, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                        }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                || error.getCause() instanceof ConnectTimeoutException
                                || error.getCause() instanceof SocketException) {

                            Toast.makeText(MoreStoriesActivity.this, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MoreStoriesActivity.this, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                        }
                    });
            arrayRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(arrayRequest);
        }
        checkSize = feedList.size();
        return feedList;
    }

    @Override
    protected void onResume() {
        super.onResume ( );
        shimmerLayout.startShimmerAnimation ();
    }

    @Override
    protected void onPause() {
        shimmerLayout.stopShimmerAnimation ();
        super.onPause ( );
    }

    void showProgressView() {
        progressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressView() {
        progressBar.setVisibility(View.INVISIBLE);
    }
}