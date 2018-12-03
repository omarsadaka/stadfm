package com.organizers_group.stadfm.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.organizers_group.stadfm.Adapters.LowerNavMenuAdapter;
import com.organizers_group.stadfm.Adapters.NavMenuAdapter;
import com.organizers_group.stadfm.Adapters.NewsFeedRecyclerViewAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import io.supercharge.shimmerlayout.ShimmerLayout;

import static com.facebook.FacebookSdk.getApplicationContext;

/*
 *
 * POST data to specific API
 * GET all data to specific user with (SHARED_PREFERENCE_USER_ID)
 * Delete specific data from specific user with USER_ID and data (POST_ID)
 *
 **/

public class CustomJSONHelper {
    @SuppressLint("StaticFieldLeak")
    private static NavMenuAdapter navMenuAdapter;
    @SuppressLint("StaticFieldLeak")
    private static LowerNavMenuAdapter lowerNavMenuAdapter;
    private Context context;

    public CustomJSONHelper(Context context) {
        this.context = context;
    }

    public CustomJSONHelper() {
//        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
//        String restoredID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);
//        if (restoredID != null) {
//            this.userID = restoredID;
//        }
    }

    // get Topics MainActivity
    public void getTopics(final RecyclerView recyclerView, ShimmerLayout shimmerMain) {
        final ArrayList<NewsFeed> postsList = new ArrayList<>();
        postsList.clear();
        final NewsFeedRecyclerViewAdapter[] newsFeedRecyclerViewAdapter = {new NewsFeedRecyclerViewAdapter()};

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Constants.TAG_INDEX,
                response -> {
                    try {
                        if (!response.has("tags")) {

                            Toast.makeText(context, R.string.something_worng, Toast.LENGTH_SHORT).show();

                        } else {

                            JSONArray tagsArray = response.getJSONArray("tags");

                            for (int i = 0; i < tagsArray.length(); i++) {
                                JSONObject tagsObjts = tagsArray.getJSONObject(i);

                                NewsFeed newsFeed = new NewsFeed();
                                newsFeed.setPostID(tagsObjts.getInt("id"));
                                newsFeed.setCategory(tagsObjts.getString("title"));
                                newsFeed.setTitle(tagsObjts.getString("title"));
                                newsFeed.setPostImgUrl(tagsObjts.getString("description"));
                                // Topics URL
                                newsFeed.setPostURl("http://stadfm.com/api/get_tag_posts/?slug=" + tagsObjts.getString("title"));

                                postsList.add(newsFeed);

                            }
                        }
                        // notify the adapter for changes
                        newsFeedRecyclerViewAdapter[0] = new NewsFeedRecyclerViewAdapter((Activity) context, postsList);
                        recyclerView.setAdapter(newsFeedRecyclerViewAdapter[0]);
                        shimmerMain.stopShimmerAnimation();
                        shimmerMain.setVisibility(View.GONE);
                        newsFeedRecyclerViewAdapter[0].notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){
                        Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {
                        Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                });

        objectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);

        newsFeedRecyclerViewAdapter[0].notifyDataSetChanged();
    }

    // Posting Topic or Article
    // post user favorite topics to DataBase(API)
    public static void postTopicToFav(Context context, String userID, String topicSlug, CheckBox favBtn, ProgressBar savingPB) {

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userID);
            json.put("topic", topicSlug);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.POST_URL, json,
                response -> {
//                    Toast.makeText(context, R.string.topic_added_fav, Toast.LENGTH_SHORT).show();

                    favBtn.setBackground(context.getResources().getDrawable(R.drawable.checked));
                    savingPB.setVisibility(View.INVISIBLE);

                    FirebaseMessaging.getInstance().subscribeToTopic(topicSlug);
                    CustomJSONHelper.registerNotification(context, FirebaseInstanceId.getInstance().getToken() , userID);
                },
                error -> savingPB.setVisibility(View.INVISIBLE))
        {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                    final long cacheExpired = 5 * 1000; // in 5 seconds this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    //post user favorite topics to DataBase(API)
    public static void postArticleToFav(Context context, String userID, String articleID, CheckBox saveArticleChkBx, ProgressBar savingPB) {

        JSONObject json = new JSONObject();
        try {
            json.put("art_id", articleID);
            json.put("user_id", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // post API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.USER_ARTICLES, json,
                response -> {
                    saveArticleChkBx.setBackground(context.getResources().getDrawable(R.drawable.favorite_article));
                    savingPB.setVisibility(View.INVISIBLE);
                },
                error -> {
                    savingPB.setVisibility(View.INVISIBLE);
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){
                        Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {
                        Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                    final long cacheExpired = 5 * 1000; // in 5 seconds this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    // Removing Topic or Article
    // Delete Topic from favorite from NewsFeedRecyclerViewAdapter
    public static void RemoveUserTopic(Context context, String userID, NewsFeed newsFeed, CheckBox favBtn, ProgressBar savingPB) {

        JsonArrayRequest jsonTopicRequest = new JsonArrayRequest(Request.Method.GET, Constants.DISMISS_TOPIC + userID,
                responseTopicId -> {
                    try {

                        for (int i = 0; i < responseTopicId.length(); i++) {
                            JSONObject topicObject = responseTopicId.getJSONObject(i);

                            if (newsFeed.getCategory().equals(topicObject.getString("slug"))) {
                                // fetch user id
                                int topicID = topicObject.getInt("u_topic_id");

                                StringRequest stringRequest = new StringRequest(Request.Method.DELETE, Constants.DISMISS_TOPIC + topicID,
                                        responseDelete -> {
//                                            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.topic_removed), Toast.LENGTH_LONG).show();

                                            savingPB.setVisibility(View.INVISIBLE);
                                            favBtn.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.unchecked));
                                            FirebaseMessaging.getInstance().unsubscribeFromTopic(newsFeed.getCategory());
                                        },
                                        error -> {
                                            if(error instanceof NoConnectionError){
                                                ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                                                NetworkInfo activeNetwork = null;
                                                if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                                                if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                                                    Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                                                else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                                            } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                                                Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                                            } else if (error.getCause() instanceof MalformedURLException){
                                                Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                                            } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                                                    || error.getCause() instanceof JSONException
                                                    || error.getCause() instanceof XmlPullParserException){
                                                Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                                            } else if (error.getCause() instanceof OutOfMemoryError){
                                                Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                                            }else if (error instanceof AuthFailureError){
                                                Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                                            } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                                                Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                                            }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                                    || error.getCause() instanceof ConnectTimeoutException
                                                    || error.getCause() instanceof SocketException) {
                                                Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                stringRequest.setRetryPolicy(new RetryPolicy() {
                                    @Override
                                    public int getCurrentTimeout() {
                                        return 50000;
                                    }

                                    @Override
                                    public int getCurrentRetryCount() {
                                        return 50000;
                                    }

                                    @Override
                                    public void retry(VolleyError error) {

                                    }
                                });

                                RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            //ERROR
        }) {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 0.5 second cache will be hit, but also refreshed on background
                    final long cacheExpired = 5 * 1000; // in 10 second this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonTopicRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonTopicRequest);

    }
    // Delete Article
    public static void RemoveUserSavedArticle(Context context, String userID, NewsFeed newsFeed, CheckBox checkBox, ProgressBar savingPB){
        JsonArrayRequest jsonTopicRequest = new JsonArrayRequest(Request.Method.GET, Constants.USER_ARTICLES + userID,
                responseTopicId -> {
                    try {
                        for (int i = 0 ; i < responseTopicId.length() ; i++){
                            JSONObject topicObject = responseTopicId.getJSONObject(i);
                            // /* for lowerNavMenuAdapter onClick listener*//*lower nav menu adapter
                            if (newsFeed.getPostID() == topicObject.getInt("id") || newsFeed.getArticleID() == topicObject.getInt("id")){
                                int articleRID = topicObject.getInt("u_artical_id");
                                StringRequest stringRequest =new StringRequest(Request.Method.DELETE,Constants.DISMISS_USER_ARTICLES + articleRID,
                                        responseDelete -> {
                                            Toast.makeText ( getApplicationContext(), getApplicationContext().getResources().getString(R.string.article_removed), Toast.LENGTH_LONG ).show ( );
                                            savingPB.setVisibility(View.INVISIBLE);
                                            checkBox.setChecked(false);
                                            checkBox.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.unfavorite_article));
                                        },
                                        error -> savingPB.setVisibility(View.INVISIBLE));
                                        stringRequest.setRetryPolicy(new RetryPolicy() {
                                            @Override
                                            public int getCurrentTimeout() {
                                                return 50000;
                                            }
                                            @Override
                                            public int getCurrentRetryCount() {
                                                return 50000;
                                            }
                                            @Override
                                            public void retry(VolleyError error) { }});
                                RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                                checkBox.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.unfavorite_article));
                            } } } catch (JSONException e) { e.printStackTrace(); }
                },
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){
                        Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {
                        Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 0.5 second cache will be hit, but also refreshed on background
                    final long cacheExpired =  10 * 1000; // in 10 second this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };
        jsonTopicRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }
            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }
            @Override
            public void retry(VolleyError error) { }});
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonTopicRequest);
    }

    // get menu items
    // GET favorite topic
    public static void  getTopicFromFav(Context context , ListView listView, String getFavoriteURL){
        final ArrayList<NewsFeed> favoriteList = new ArrayList<>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getFavoriteURL,
                response -> {
                    try {
                        favoriteList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject topicObjts = response.getJSONObject(i);

                            if (topicObjts.has("checked") && topicObjts.getString("checked").equals("checked")){

                                NewsFeed newsFeed = new NewsFeed();

                                newsFeed.setPostID(topicObjts.getInt("id"));
                                newsFeed.setCategory(topicObjts.getString("slug"));
                                newsFeed.setTitle(topicObjts.getString("name"));
                                // Topics URL
                                newsFeed.setPostURl("http://stadfm.com/api/get_tag_posts/?slug=" + topicObjts.getString("slug") );

                                favoriteList.add(newsFeed);
                            }
                        }
                        // notify the adapter for changes
                        navMenuAdapter = new NavMenuAdapter(context, favoriteList);
                        listView.setAdapter(navMenuAdapter);
                        navMenuAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){
                        Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {
                        Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 5 seconds cache will be hit, but also refreshed on background
                    final long cacheExpired = 5 * 1000; // in 10 seconds this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
        try{
            navMenuAdapter.notifyDataSetChanged();
        }catch (Exception ignored){}
    }
    // GET favorite Article
    public static void  getArticleFromFav(Context context , ListView listView, String getFavoriteURL){
        final ArrayList<NewsFeed> favoriteList = new ArrayList<>();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, getFavoriteURL,
                response -> {
                    try {
                        favoriteList.clear();

                        for (int i = 0; i < response.length(); i++) {
                            if (i < 3){
                                JSONObject topicObjts = response.getJSONObject(i);

                                NewsFeed newsFeed = new NewsFeed();

                                newsFeed.setArticleID(topicObjts.getInt("id"));
                                newsFeed.setTitle(topicObjts.getString("title"));
                                newsFeed.setDescription(topicObjts.getString("content"));
                                newsFeed.setPostImgUrl(topicObjts.getString("poster"));
                                newsFeed.setPostURl(topicObjts.getString("url"));
                                newsFeed.setCategory(topicObjts.getString("topic"));
                                newsFeed.setPostID(Integer.parseInt(topicObjts.getString("u_artical_id")));

                                try{
                                    newsFeed.setPostedSince(topicObjts.getString("post_date"));
                                    int readinTime = topicObjts.getJSONObject("timef").getInt("min");
                                    if ( readinTime == 0){
                                        newsFeed.setReadingTime("30 Seconds Reading");
                                    }else if (readinTime == 1){
                                        newsFeed.setReadingTime(String.valueOf(readinTime) + " Minute Reading");
                                    } else {
                                        newsFeed.setReadingTime(String.valueOf(readinTime) + " Minutes Reading");
                                    }
                                }catch (Exception ignored){}

                                favoriteList.add(newsFeed);

                            }
                        }
                        // notify the adapter for changes
                        lowerNavMenuAdapter = new LowerNavMenuAdapter(context, favoriteList);
                        listView.setAdapter(lowerNavMenuAdapter);
                        lowerNavMenuAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){
                        Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {
                        Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                    final long cacheExpired = 5 * 1000; // in 5 seconds this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
        try{
            lowerNavMenuAdapter.notifyDataSetChanged();
        }catch (Exception ignored){}
    }

    // Delete menu items
    // Delete Topic from favorite from Menu NavMenuAdapter
    public static void deleteTopic(Context context, String userID, NewsFeed newsFeed){
        JsonArrayRequest jsonTopicRequest = new JsonArrayRequest(Request.Method.GET,Constants.DISMISS_TOPIC + userID ,
                responseTopicId -> {
                    try {

                        for (int i = 0 ; i < responseTopicId.length() ; i++){
                            JSONObject topicObject = responseTopicId.getJSONObject(i);

                            if (newsFeed.getCategory().equals(topicObject.getString("slug"))){
                                // fetch user id
                                int topicID = topicObject.getInt("u_topic_id");

                                StringRequest stringRequest =new StringRequest(Request.Method.DELETE, Constants.DISMISS_TOPIC + topicID,
                                        responseDelete -> {},//Toast.makeText ( getApplicationContext(), getApplicationContext().getResources().getString(R.string.topic_removed), Toast.LENGTH_LONG ).show ( ),
                                        error -> {});

                                stringRequest.setRetryPolicy(new RetryPolicy() {
                                    @Override
                                    public int getCurrentTimeout() {
                                        return 50000;
                                    }

                                    @Override
                                    public int getCurrentRetryCount() {
                                        return 50000;
                                    }

                                    @Override
                                    public void retry(VolleyError error) {

                                    }
                                });

                                RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },

                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){
                        Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {
                        Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 0.5 second cache will be hit, but also refreshed on background
                    final long cacheExpired =  10 * 1000; // in 10 second this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonTopicRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonTopicRequest);

    }
    // Delete Article from favorite from Menu NavMenuAdapter
    public static void deleteArticle(Context context, LowerNavMenuAdapter lowerNavMenuAdapter, String userID, NewsFeed newsFeed, int position){

        JsonArrayRequest jsonTopicRequest = new JsonArrayRequest(Request.Method.GET,Constants.DISMISS_USER_ARTICLES + userID ,
            responseTopicId -> {
                try {

//                        for (int i = 0 ; i < responseTopicId.length() ; i++){
                        JSONObject topicObject = responseTopicId.getJSONObject(position);

                        if (newsFeed.getArticleID() == topicObject.getInt("id")){
                            // fetch user id
                            int articleID = topicObject.getInt("u_artical_id");
                            StringRequest stringRequest =new StringRequest(Request.Method.DELETE, Constants.DISMISS_USER_ARTICLES + articleID,
                                    responseDelete -> {
                                        Toast.makeText ( getApplicationContext(), getApplicationContext().getResources().getString(R.string.article_removed), Toast.LENGTH_LONG ).show ( );
                                        lowerNavMenuAdapter.notifyDataSetChanged();

                                    },
                                    error -> {
                                        if(error instanceof NoConnectionError){
                                            ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                                            NetworkInfo activeNetwork = null;
                                            if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                                            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                                                Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                                            else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                                        } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                                            Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                                        } else if (error.getCause() instanceof MalformedURLException){
                                            Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                                        } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                                                || error.getCause() instanceof JSONException
                                                || error.getCause() instanceof XmlPullParserException){
                                            Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                                        } else if (error.getCause() instanceof OutOfMemoryError){
                                            Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                                        }else if (error instanceof AuthFailureError){
                                            Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                                        } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                                            Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                                        }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                                || error.getCause() instanceof ConnectTimeoutException
                                                || error.getCause() instanceof SocketException) {
                                            Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            stringRequest.setRetryPolicy(new RetryPolicy() {
                                @Override
                                public int getCurrentTimeout() {
                                    return 50000;
                                }

                                @Override
                                public int getCurrentRetryCount() {
                                    return 50000;
                                }

                                @Override
                                public void retry(VolleyError error) {

                                }
                            });

                            RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
        //ERROR
        }){
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 0.5 second cache will be hit, but also refreshed on background
                    final long cacheExpired =  10 * 1000; // in 10 second this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonTopicRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonTopicRequest);

    }

    // setup push notification
    /*
     * Parameters required
     *
     * device_token
     * user_id
     * device_type
     * */
    // registerNotification
    public static void registerNotification(Context context, String device_token , String user_id){

        JSONObject json = new JSONObject();
        try {
            json.put("device_token", device_token);
            json.put("user_id", user_id);
            json.put("device_type", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // notification API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.NOTIFY_URL, json,
                response ->{},
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)context.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(context, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(context, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(context, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){
                        Toast.makeText(context, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(context, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(context, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(context, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {
                        Toast.makeText(context, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                    if (cacheEntry == null) {
                        cacheEntry = new Cache.Entry();
                    }
                    final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                    final long cacheExpired = 5 * 1000; // in 5 seconds this cache entry expires completely
                    long now = System.currentTimeMillis();
                    final long softExpire = now + cacheHitButRefreshed;
                    final long ttl = now + cacheExpired;
                    cacheEntry.data = response.data;
                    cacheEntry.softTtl = softExpire;
                    cacheEntry.ttl = ttl;
                    String headerValue;
                    headerValue = response.headers.get("Date");
                    if (headerValue != null) {
                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    headerValue = response.headers.get("Last-Modified");
                    if (headerValue != null) {
                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
                    }
                    cacheEntry.responseHeaders = response.headers;
                    final String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString), cacheEntry);
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

}
