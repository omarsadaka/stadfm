package com.organizers_group.stadfm.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.organizers_group.stadfm.Activities.SearchActivity;
import com.organizers_group.stadfm.Adapters.SearchAdapter;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;

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

import static com.facebook.FacebookSdk.getApplicationContext;

public class SearchableJson {
    private String secondTerm;
    private Context context;
    private List<NewsFeed> searchList;
    private String categoryType;
    private String postImg;
    private SearchAdapter searchAdapter;


    public SearchableJson(String secondTerm, Context context) {
        this.secondTerm = secondTerm;
        this.context = context;
    }
    // get json array
    public List<NewsFeed> getPosts(RecyclerView recyclerView, ShimmerLayout mShimmerViewContainer){

        searchList = new ArrayList<>();

        @SuppressLint("SetTextI18n") JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, Constants.SEARCH_FIRST_TERM + secondTerm,
                response -> {

                    TextView txtView = ((SearchActivity)context).findViewById(R.id.resultsTextView);

                    try {
                        // check if the status is ok and we have values
                        if (response.getInt ( "count_total" )== 0){
                            txtView.setText ( context.getResources().getText(R.string.no_result) + " : " + secondTerm );
                        }else {
                            txtView.setText ( context.getString(R.string.result_for) + " : " + secondTerm );
                            JSONArray postsArray = response.getJSONArray("posts");

                            for (int i = 0; i < postsArray.length(); i++) {
                                JSONObject postsObjs = postsArray.getJSONObject(i);

                                NewsFeed newsFeed = new NewsFeed();
                                newsFeed.setPostID(postsObjs.getInt("id"));
                                newsFeed.setPostURl(postsObjs.getString("url"));
                                newsFeed.setTitle(postsObjs.getString("title"));
                                newsFeed.setDescription(postsObjs.getString("content"));
                                newsFeed.setPostedSince(postsObjs.getString("date"));

                                try {
                                    int readinTime = postsObjs.getJSONObject("timef").getInt("min");
                                    if ( readinTime == 0){
                                        newsFeed.setReadingTime("30 Seconds Reading");
                                    }else if (readinTime == 1){
                                        newsFeed.setReadingTime(String.valueOf(readinTime) + " Minute Reading");
                                    } else {
                                        newsFeed.setReadingTime(String.valueOf(readinTime) + " Minutes Reading");
                                    }
                                }catch (Exception e){newsFeed.setReadingTime("Unknown");}

                                categoryType = getCat(postsObjs);
                                newsFeed.setCategory(categoryType);

                                postImg = getImg(postsObjs);
                                newsFeed.setPostImgUrl(postImg);
                                // add newsFeed to the ArrayList
                                searchList.add(newsFeed);
                            }
                        }
                        // notify Adapter for changes
                        searchAdapter = new SearchAdapter( context, searchList );
                        recyclerView.setAdapter ( searchAdapter );
                        mShimmerViewContainer.stopShimmerAnimation();
                        mShimmerViewContainer.setVisibility( View.GONE);

                        InputMethodManager imm = (InputMethodManager)context.getSystemService( Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(mShimmerViewContainer.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                        }
                        searchAdapter.notifyDataSetChanged ( );

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
        objectRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);
        return searchList;
    }
    // get Json Array
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
    // get the post image
    private String getImg(JSONObject postsObj) {
        String imgUrl ="";

        try {

            if (postsObj.has("attachments")) {

                JSONArray attachments = postsObj.getJSONArray("attachments");

                if (attachments.length() >= 1) {
                    JSONObject thumbnail = attachments.getJSONObject(0);
                    if (thumbnail.has("url")) {
                        imgUrl = thumbnail.getString("url");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return imgUrl;
    }
}