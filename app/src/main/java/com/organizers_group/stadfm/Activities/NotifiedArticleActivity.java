package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.organizers_group.stadfm.Adapters.StoriesNewsFeedAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
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

public class NotifiedArticleActivity extends AppCompatActivity {

    private List<NewsFeed> postsList;
    private StoriesNewsFeedAdapter storiesNewsFeedAdapter;
    private String notifiedTopic;
    private ShimmerLayout shimmerNotified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_article);

        //Shimmer
        shimmerNotified = findViewById(R.id.shimmer_notified);

        // navigate to search activity
        ImageView searchIcon = findViewById(R.id.articleSearch);
        searchIcon.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SearchActivity.class)));
        // set on Click Listener for navigation back button
        ImageView navBack = findViewById(R.id.articleNavBack);
        navBack.setOnClickListener(v -> NotifiedArticleActivity.super.onBackPressed());

        notifiedTopic = (String) getIntent().getSerializableExtra("topic");

        postsList = new ArrayList<>();
        postsList = getPosts();

        RecyclerView recyclerView = findViewById(R.id.articleRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        storiesNewsFeedAdapter = new StoriesNewsFeedAdapter( this , postsList);
        recyclerView.setAdapter(storiesNewsFeedAdapter);
        storiesNewsFeedAdapter.notifyDataSetChanged();

    }

    // retrieving data from JSON of Specific Tag
    public List<NewsFeed> getPosts(){
        postsList.clear();

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Constants.TAG_POSTS + notifiedTopic,
                response -> {
                    try {
                        if (!response.has("posts")){

                            Toast.makeText(NotifiedArticleActivity.this, R.string.something_worng , Toast.LENGTH_SHORT).show();
                            // in case of err
                            startActivity(new Intent(NotifiedArticleActivity.this , MainActivity.class));
                            this.finish();

                        }else {

                            JSONArray postsArray = response.getJSONArray("posts");

                            for (int i = 0; i < postsArray.length(); i++) {
                                JSONObject postsObj = postsArray.getJSONObject(i);

                                NewsFeed newsFeed = new NewsFeed();
                                newsFeed.setPostID(postsObj.getInt("id"));
                                newsFeed.setTitle(postsObj.getString("title"));
                                newsFeed.setDescription(postsObj.getString("content"));
                                newsFeed.setPostURl(postsObj.getString("url"));
                                newsFeed.setPostedSince(postsObj.getString("date"));

                                newsFeed.setCategory(getCat(postsObj));
                                newsFeed.setPostImgUrl(postsObj.getJSONArray("attachments").getJSONObject(0).getString("url"));
                                newsFeed.setReadingTime("UNKNOWN");

                                postsList.add(newsFeed);
                            }
                        }
                        // notify the adapter for changes! very important...
                        storiesNewsFeedAdapter.notifyDataSetChanged();
                        shimmerNotified.stopShimmerAnimation();
                        shimmerNotified.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)NotifiedArticleActivity.this.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(NotifiedArticleActivity.this, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(NotifiedArticleActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();

                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(NotifiedArticleActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(NotifiedArticleActivity.this, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){

                        Toast.makeText(NotifiedArticleActivity.this, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(NotifiedArticleActivity.this, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(NotifiedArticleActivity.this, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(NotifiedArticleActivity.this, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {

                        Toast.makeText(NotifiedArticleActivity.this, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NotifiedArticleActivity.this, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                });
        objectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);
        return postsList;
    }
    // get the post category
    private String getCat(JSONObject postsObj) {
        String catType ="";
        try {
            JSONArray cat = postsObj.getJSONArray("categories");
            for (int i = 0 ; i< cat.length() ; i++ ){

                JSONObject category = cat.getJSONObject(i);

                catType = category.getString("title");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return catType;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NotifiedArticleActivity.this , NewsFeedHome.class));
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        shimmerNotified.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        shimmerNotified.stopShimmerAnimation();
        super.onPause();
    }
}