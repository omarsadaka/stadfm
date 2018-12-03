package com.organizers_group.stadfm.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

public class NavMenuAdapter extends ArrayAdapter<NewsFeed> {
private final Context context;
private final int layoutResourceId;
private ArrayList<NewsFeed> data;

    public NavMenuAdapter(Context context, ArrayList<NewsFeed> data) {
        super(context, R.layout.menu_item_model, data);
        this.context = context;
        this.layoutResourceId = R.layout.menu_item_model;
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        @SuppressLint("ViewHolder") View v = ((Activity) context).getLayoutInflater().inflate(layoutResourceId, parent, false);

        TextView name = v.findViewById(R.id.itemTitle);
        TextView dismissBtn = v.findViewById(R.id.dismissBtn);

        final NewsFeed newsFeed = data.get(position);

        name.setText(newsFeed.getTitle());

        dismissBtn.setOnClickListener(v1 -> {

            String accessToken = AccessToken.getCurrentAccessToken().getToken();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                    response -> {
                        try {
                            // fetch user id
                            int userID = response.getInt("wp_user_id");
                            // use userID for getting the topic id
                            CustomJSONHelper.deleteTopic(context, String.valueOf(userID), newsFeed);

                            remove(data.get(position));
                            notifyDataSetChanged();

                        } catch (JSONException | IndexOutOfBoundsException e) {
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
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    try {
                        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                        if (cacheEntry == null) {
                            cacheEntry = new Cache.Entry();
                        }
                        final long cacheHitButRefreshed = 1000; // in 1 second cache will be hit, but also refreshed on background
                        final long cacheExpired = 60 * 1000; // in i minute this cache entry expires completely
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
        });

        (new NewsFeedRecyclerViewAdapter()).notifyDataSetChanged();

        return v;
    }
}