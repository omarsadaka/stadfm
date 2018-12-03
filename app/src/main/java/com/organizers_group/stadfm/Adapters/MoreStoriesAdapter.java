package com.organizers_group.stadfm.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.organizers_group.stadfm.Activities.ArticleDetailsActivity;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.Model.NewsFeed;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.Utils.CustomJSONHelper;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;
import com.squareup.picasso.Picasso;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MoreStoriesAdapter extends RecyclerView.Adapter<MoreStoriesAdapter.ViewHolder> {

    private Context context;
    private List<NewsFeed> newsFeeds;

    public MoreStoriesAdapter(Context context, List<NewsFeed> newsFeeds) {
        this.context = context;
        this.newsFeeds = newsFeeds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from ( parent.getContext ()).inflate ( R.layout.more_stories_item_row,parent ,false );
        return new ViewHolder ( view );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsFeed feed = newsFeeds.get ( position );
        Picasso.get ().load ( feed.getPostImgUrl () ).into ( holder.newsfeedImage );
        holder.newsfeedTitle.setText ( Html.fromHtml(feed.getTitle(), Html.FROM_HTML_MODE_COMPACT ));
        holder.newsfeedContent.setText ( Html.fromHtml(feed.getDescription (), Html.FROM_HTML_MODE_COMPACT ));

    }

    @Override
    public int getItemCount() {
        return newsFeeds.size ();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView newsfeedImage;
        LinearLayout share;
        TextView newsfeedTitle;
        TextView newsfeedContent;
        CheckBox saveArticleChkBx;
        ProgressBar savingPB;

        ViewHolder(View itemView) {
            super ( itemView );
            newsfeedImage = itemView.findViewById ( R.id.newsfeed_image );
            share = itemView.findViewById ( R.id.share );
            newsfeedTitle = itemView.findViewById ( R.id.newsfeed_title );
            newsfeedContent = itemView.findViewById ( R.id.newsfeed_content );
            saveArticleChkBx = itemView.findViewById(R.id.saveMoreArticleChkBx);
            savingPB = itemView.findViewById(R.id.savingPBMoreStories);

            // save or dismiss article from savedArticles
            saveArticleChkBx.setOnClickListener(v -> {
                savingPB.setVisibility(View.VISIBLE);

                NewsFeed newsFeed = newsFeeds.get(getAdapterPosition());

                String accessToken = AccessToken.getCurrentAccessToken().getToken();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_ACCESS_TOKEN + accessToken,
                        response -> {
                            try {
                                // fetch user id
                                int userID = response.getInt("wp_user_id");
                                if (saveArticleChkBx.isChecked()) {
                                    // add topic to favorite
                                    CustomJSONHelper.postArticleToFav(context , String.valueOf(userID), String.valueOf(newsFeed.getPostID()), saveArticleChkBx, savingPB);
                                } else {
                                    // use userID for getting the topic id
                                    CustomJSONHelper.RemoveUserSavedArticle(context, String.valueOf(userID), newsFeed , saveArticleChkBx , savingPB);
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
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        try {
                            Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                            if (cacheEntry == null) {
                                cacheEntry = new Cache.Entry();
                            }
                            final long cacheHitButRefreshed = 30 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
                            final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
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

            share.setOnClickListener (v -> {
                NewsFeed newsFeed = newsFeeds.get ( getAdapterPosition ( ) );

                    Intent sharingIntent = new Intent ( Intent.ACTION_SEND );
                    sharingIntent.setType ( "text/plain" );
                    sharingIntent.putExtra ( Intent.EXTRA_SUBJECT, "Subject Here" );
                    sharingIntent.putExtra ( Intent.EXTRA_TEXT,  newsFeed.getPostURl( ) );
                    context.startActivity ( Intent.createChooser ( sharingIntent ,"Sharing By" ));

                });

            itemView.setOnClickListener (v -> {
                NewsFeed newsFeed = newsFeeds.get(getAdapterPosition());

                if (saveArticleChkBx.isChecked()) newsFeed.setChosenArticle(true);

                Intent intent = new Intent(context, ArticleDetailsActivity.class);
                intent.putExtra("post", newsFeed);
                intent.putExtra("chosenArticle", true);
                context.startActivity(intent);

            });
        }
    }
}