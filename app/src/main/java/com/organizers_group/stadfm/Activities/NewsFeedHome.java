package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.organizers_group.stadfm.Adapters.StoriesNewsFeedAdapter;
import com.organizers_group.stadfm.Adapters.TrendingNewsFeedAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomNavigationHandler;
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

public class NewsFeedHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<NewsFeed> postsList;
    private List<NewsFeed> mixedList;
    private StoriesNewsFeedAdapter storiesNewsFeedAdapter;
    private TrendingNewsFeedAdapter trendingNewsFeedAdapter;
    private ShimmerLayout horizontalShimmer;
    private ShimmerLayout ShimmerVertical;
    private ProgressBar trendingProgressBar;
    private ProgressBar mixedProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_news_feed_home);

        horizontalShimmer = findViewById ( R.id.shimmer_horizontal );
        ShimmerVertical =  findViewById ( R.id.shimmer_vertical );
        trendingProgressBar = findViewById ( R.id.pBLoadingTrending );
        mixedProgressBar = findViewById ( R.id.pBLoadingMixed );
        mixedList = new ArrayList<>();


        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.gradient) );
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);


        toggle.setHomeAsUpIndicator(R.drawable.dawer_icon);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(view -> {
            if (drawer.isDrawerVisible(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ImageView seeMoreStories = findViewById(R.id.moreStoriesImg);
        seeMoreStories.setOnClickListener (v -> startActivity ( new Intent ( NewsFeedHome.this, MoreStoriesActivity.class ) ));

        postsList = new ArrayList<>();

        // Handle event listener for the Navigation view
        new CustomNavigationHandler(NewsFeedHome.this , drawer);

        RecyclerView trendingRecyclerView = findViewById(R.id.trendingStories);
        trendingRecyclerView.setHasFixedSize(true);
        LinearLayoutManager trendingLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);
        trendingRecyclerView.setLayoutManager(trendingLayoutManager);

        // get trending post
        final int trendingPage = 1;
        trendingNewsFeedAdapter = new TrendingNewsFeedAdapter( this , getTrendingPosts(trendingPage));
        trendingRecyclerView.setAdapter(trendingNewsFeedAdapter);
        trendingNewsFeedAdapter.notifyDataSetChanged();

        // Retain an instance so that you can call `resetState()` for fresh searches
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(trendingLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                getTrendingPosts(page);
                showProgressView(trendingProgressBar);
            }
        };
        // Adds the scroll listener to RecyclerView
        trendingRecyclerView.addOnScrollListener(scrollListener);
        // End Of Trending

        // Stories RecyclerView
        RecyclerView storiesRecyclerView = findViewById(R.id.yourStories);
        storiesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager storiesLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.VERTICAL , false);
        storiesRecyclerView.setLayoutManager(storiesLayoutManager);

        // get mixed posts
        final int storiesPage = 1;
        storiesNewsFeedAdapter = new StoriesNewsFeedAdapter( this , getStoriesPosts(storiesPage));
        storiesRecyclerView.setAdapter(storiesNewsFeedAdapter);
        storiesNewsFeedAdapter.notifyDataSetChanged();

        // Retain an instance so that you can call `resetState()` for fresh searches
        EndlessRecyclerViewScrollListener mixedScrollListener = new EndlessRecyclerViewScrollListener(storiesLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
//                getStoriesPosts(page );
//                showProgressView(mixedProgressBar);
            }
        };
        // Adds the scroll listener to RecyclerView
        storiesRecyclerView.addOnScrollListener(mixedScrollListener);
        // End Of Mixed Stories
    }

    // get json array
    public List<NewsFeed> getTrendingPosts(int trendingPage){
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.TRENDING  + "/?p=" + trendingPage,
                response -> {
                    try {
                        // check if the status is ok and we have values
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postsObjs = response.getJSONObject(i);

                            if (postsObjs.getString("url").equals("") || !postsObjs.has("url")){
                                hideProgressView(trendingProgressBar);
                                Toast.makeText(this, "There is no more!", Toast.LENGTH_SHORT).show();
                            }else {

                                NewsFeed newsFeed = new NewsFeed();
                                newsFeed.setPostID(postsObjs.getInt("id"));
                                newsFeed.setTitle(postsObjs.getString("title"));
                                newsFeed.setDescription(postsObjs.getString("content"));
                                newsFeed.setCategory(postsObjs.getString("topic"));
                                newsFeed.setPostImgUrl(postsObjs.getString("poster"));
                                newsFeed.setPostedSince(postsObjs.getString("post_date"));
                                newsFeed.setPostURl(postsObjs.getString("url"));

                                int readinTime = postsObjs.getJSONObject("timef").getInt("min");
                                if ( readinTime == 0){
                                    newsFeed.setReadingTime("30 Seconds Reading");
                                }else if (readinTime == 1){
                                    newsFeed.setReadingTime(String.valueOf(readinTime) + " Minute Reading");
                                } else {
                                    newsFeed.setReadingTime(String.valueOf(readinTime) + " Minutes Reading");
                                }

                                // add newsFeed to the ArrayList
                                postsList.add(newsFeed);
                            }
                        }
                        horizontalShimmer.stopShimmerAnimation();
                        horizontalShimmer.setVisibility( View.GONE);
                        hideProgressView(trendingProgressBar);
                        trendingNewsFeedAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)NewsFeedHome.this.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(NewsFeedHome.this, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(NewsFeedHome.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();

                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(NewsFeedHome.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(NewsFeedHome.this, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){

                        Toast.makeText(NewsFeedHome.this, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(NewsFeedHome.this, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(NewsFeedHome.this, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(NewsFeedHome.this, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {

                        Toast.makeText(NewsFeedHome.this, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NewsFeedHome.this, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                });
        arrayRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(arrayRequest);
        return postsList;
    }

    // get json array
    public List<NewsFeed> getStoriesPosts(int storiesPage){
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
        String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);
        if (userID != null) {
            JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, Constants.USER_ARTICLES_MIXED + userID + "/?p=" + String.valueOf(storiesPage),
                    response1 -> {
                        try {
                            for (int i = 0; i < response1.length(); i++) {
                                JSONObject postsObjs = response1.getJSONObject(i);

                                if (postsObjs.getString("url").equals("")){
                                    hideProgressView(mixedProgressBar);
                                    Toast.makeText ( this, " No More Article", Toast.LENGTH_LONG ).show ( );
                                }else {

                                    NewsFeed newsFeed = new NewsFeed();
                                    newsFeed.setPostID(postsObjs.getInt("id"));
                                    newsFeed.setTitle(postsObjs.getString("title"));
                                    newsFeed.setDescription(postsObjs.getString("content"));
                                    newsFeed.setCategory(postsObjs.getString("topic"));
                                    newsFeed.setPostImgUrl(postsObjs.getString("poster"));
                                    newsFeed.setPostURl(postsObjs.getString("url"));
                                    newsFeed.setPostedSince(postsObjs.getString("post_date"));

                                    int readinTime = postsObjs.getJSONObject("timef").getInt("min");
                                    if ( readinTime == 0){
                                        newsFeed.setReadingTime("30 Seconds Reading");
                                    }else if (readinTime == 1){
                                        newsFeed.setReadingTime(String.valueOf(readinTime) + " Minute Reading");
                                    } else {
                                        newsFeed.setReadingTime(String.valueOf(readinTime) + " Minutes Reading");
                                    }

                                    // add newsFeed to the ArrayList
                                    mixedList.add(newsFeed);
                                }
                            }
                            ShimmerVertical.stopShimmerAnimation();
                            ShimmerVertical.setVisibility( View.GONE);
                            hideProgressView(mixedProgressBar);
                            storiesNewsFeedAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        if(error instanceof NoConnectionError){
                            ConnectivityManager cm = (ConnectivityManager)NewsFeedHome.this.getSystemService( Context.CONNECTIVITY_SERVICE);
                            NetworkInfo activeNetwork = null;
                            if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                                Toast.makeText(NewsFeedHome.this, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                            else Toast.makeText(NewsFeedHome.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                            Toast.makeText(NewsFeedHome.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                        } else if (error.getCause() instanceof MalformedURLException){
                            Toast.makeText(NewsFeedHome.this, R.string.bad_request, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                                || error.getCause() instanceof JSONException
                                || error.getCause() instanceof XmlPullParserException){

                            Toast.makeText(NewsFeedHome.this, R.string.parse_error, Toast.LENGTH_SHORT).show();
                        } else if (error.getCause() instanceof OutOfMemoryError){
                            Toast.makeText(NewsFeedHome.this, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                        }else if (error instanceof AuthFailureError){
                            Toast.makeText(NewsFeedHome.this, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                            Toast.makeText(NewsFeedHome.this, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                        }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                || error.getCause() instanceof ConnectTimeoutException
                                || error.getCause() instanceof SocketException) {
                            Toast.makeText(NewsFeedHome.this, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NewsFeedHome.this, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                        }
                    });
            arrayRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(arrayRequest);
        }
        return mixedList;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_feed_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
            startActivity(new Intent(getApplicationContext(),SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        horizontalShimmer.startShimmerAnimation();
        ShimmerVertical.startShimmerAnimation ();
    }

    @Override
    protected void onPause() {
        horizontalShimmer.stopShimmerAnimation ();
        ShimmerVertical.startShimmerAnimation ();
        super.onPause();
    }

    void showProgressView(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
    }

    void hideProgressView(ProgressBar progressBar) {
        progressBar.setVisibility(View.INVISIBLE);
    }
}