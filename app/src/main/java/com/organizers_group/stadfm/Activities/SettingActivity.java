package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.FacebookSdk;
import com.organizers_group.stadfm.Data.Constants;
import com.organizers_group.stadfm.R;
import com.organizers_group.stadfm.SigninOrUp.SignInOrOut;
import com.organizers_group.stadfm.Utils.RequestQueueSingleton;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.xmlpull.v1.XmlPullParserException;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "FireBaseSA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setting);

        //initialize FB SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        // change the font family
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/avenir_book.otf");

        TextView fbRegisterText = findViewById(R.id.fbRegisterText);
        RelativeLayout logout = findViewById(R.id.logoutLayout);
        TextView logoutText = findViewById(R.id.logoutText);
        ImageView searchIcon = findViewById(R.id.imageSettingSearch);
        ImageView navBack = findViewById(R.id.settingNavBack);
        Switch switchNotify = findViewById(R.id.switchNotify);

        checkForSwitch (switchNotify);

        switchNotify.setOnClickListener(view -> {
            if (switchNotify.isChecked ()){

                SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
                String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);

                if (userID != null){
                    String kay_id = "?t=on";
                    checkNotify ( userID , kay_id );
                }

            }else {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
                String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);

                if (userID != null){
                    String kayId = "?t=off";
                    checkNotify ( userID , kayId );
                }
            }
        });

        fbRegisterText.setTypeface(typeface);
        logoutText.setTypeface(typeface);

        fbRegisterText.setOnClickListener(this);
        logout.setOnClickListener(this);
        searchIcon.setOnClickListener(this);
        navBack.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setting_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.search:
                startActivity(new Intent(getApplicationContext(),SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fbRegisterText:
                SignInOrOut.logoutUser(SettingActivity.this);
                break;
            case R.id.logoutLayout:
                SignInOrOut.logoutUser(SettingActivity.this);
                break;
            case R.id.imageSettingSearch:
                startActivity(new Intent(getApplicationContext(),SearchActivity.class));
                break;
            case R.id.settingNavBack:
                SettingActivity.super.onBackPressed();
                break;
        }
    }


    public void checkNotify(String id , String kay){


        StringRequest objectRequest = new StringRequest ( Request.Method.GET, Constants.CHECK_SUBSCRIBTION + id + kay,
                response ->{}
                ,
                error -> {
                    if(error instanceof NoConnectionError){
                        ConnectivityManager cm = (ConnectivityManager)SettingActivity.this.getSystemService( Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = null;
                        if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                            Toast.makeText(SettingActivity.this, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                        else Toast.makeText(SettingActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();

                    } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                        Toast.makeText(SettingActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof MalformedURLException){
                        Toast.makeText(SettingActivity.this, R.string.bad_request, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                            || error.getCause() instanceof JSONException
                            || error.getCause() instanceof XmlPullParserException){

                        Toast.makeText(SettingActivity.this, R.string.parse_error, Toast.LENGTH_SHORT).show();
                    } else if (error.getCause() instanceof OutOfMemoryError){
                        Toast.makeText(SettingActivity.this, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof AuthFailureError){
                        Toast.makeText(SettingActivity.this, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                    } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                        Toast.makeText(SettingActivity.this, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                    }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                            || error.getCause() instanceof ConnectTimeoutException
                            || error.getCause() instanceof SocketException) {

                        Toast.makeText(SettingActivity.this, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingActivity.this, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                    }
                });
        objectRequest.setRetryPolicy(new RetryPolicy() {
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
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);

    }

    public void checkForSwitch(Switch switchNotify){

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_NAME , Context.MODE_PRIVATE);
        String userID = prefs.getString(Constants.SHARED_PREFERENCE_USER_ID, null);

        if (userID != null) {
            JsonObjectRequest objectRequest = new JsonObjectRequest ( Request.Method.GET, Constants.CHECK_SUBSCRIBTION + userID ,
                    response -> {
                        try {
                            String value = response.getString ( "on_off" );
                            if (value.equals ( "on" )){
                                switchNotify.setChecked ( true );
                            }else if (value.equals ( "off" )){
                                switchNotify.setChecked ( false );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace ( );
                        }
                    },
                    error -> {
                        if(error instanceof NoConnectionError){
                            ConnectivityManager cm = (ConnectivityManager)SettingActivity.this.getSystemService( Context.CONNECTIVITY_SERVICE);
                            NetworkInfo activeNetwork = null;
                            if (cm != null) activeNetwork = cm.getActiveNetworkInfo();
                            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting())
                                Toast.makeText(SettingActivity.this, R.string.not_connected_to_internet , Toast.LENGTH_SHORT).show();
                            else Toast.makeText(SettingActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError || error.getCause() instanceof ConnectException){
                            Toast.makeText(SettingActivity.this, R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
                        } else if (error.getCause() instanceof MalformedURLException){
                            Toast.makeText(SettingActivity.this, R.string.bad_request, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError || error.getCause() instanceof IllegalStateException
                                || error.getCause() instanceof JSONException
                                || error.getCause() instanceof XmlPullParserException){

                            Toast.makeText(SettingActivity.this, R.string.parse_error, Toast.LENGTH_SHORT).show();
                        } else if (error.getCause() instanceof OutOfMemoryError){
                            Toast.makeText(SettingActivity.this, R.string.out_of_memory, Toast.LENGTH_SHORT).show();
                        }else if (error instanceof AuthFailureError){
                            Toast.makeText(SettingActivity.this, R.string.server_not_find_auth, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError || error.getCause() instanceof ServerError) {
                            Toast.makeText(SettingActivity.this, R.string.server_not_responding, Toast.LENGTH_SHORT).show();
                        }else if (error instanceof TimeoutError || error.getCause() instanceof SocketTimeoutException
                                || error.getCause() instanceof ConnectTimeoutException
                                || error.getCause() instanceof SocketException) {

                            Toast.makeText(SettingActivity.this, R.string.connection_time_oet, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingActivity.this, R.string.unkown_error, Toast.LENGTH_SHORT).show();
                        }
                    });
            objectRequest.setRetryPolicy(new RetryPolicy () {
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
            RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(objectRequest);

        }


    }
}