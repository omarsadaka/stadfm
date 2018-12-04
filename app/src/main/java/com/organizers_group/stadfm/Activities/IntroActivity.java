package com.organizers_group.stadfm.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.widget.LoginButton;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class IntroActivity extends AppCompatActivity {

    private LoginButton fbLoginButton;
    private CallbackManager callbackManager;
    private SignInOrOut signInOrOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*enable full screen*/
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);

        // initialize FB SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        signInOrOut = new SignInOrOut();
        // define login buttons
        fbLoginButton = findViewById(R.id.login_button);

        // change the font family
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/avenir_book.otf");

        fbLoginButton.setTypeface(typeface);
        fbLoginButton.setPadding(50 ,20 ,10 ,20);
        fbLoginButton.setTextSize(15.0f);

        final ProgressBar progressBar = findViewById(R.id.intro_progress);
        progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.basicAppColor), android.graphics.PorterDuff.Mode.SRC_IN);
        Objects.requireNonNull(getSupportActionBar()).hide();
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // If you want to modify a view in your Activity
                IntroActivity.this.runOnUiThread(() -> {
                    //here override the onResume Method for NetWork Connection Chick
                    ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    // ARE WE CONNECTED TO THE NET
                    assert conMgr != null;
                    if (conMgr.getActiveNetworkInfo() != null
                            && conMgr.getActiveNetworkInfo().isAvailable()
                            && conMgr.getActiveNetworkInfo().isConnected()) {

                        if (AccessToken.getCurrentAccessToken() != null){
                            startActivity(new Intent(IntroActivity.this , MainActivity.class));
                            IntroActivity.this.finish();
                        }

                        callbackManager= CallbackManager.Factory.create();

                        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                        // shaw animation
                        fbLoginButton.startAnimation(slideUp);
                        fbLoginButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);

                        signInOrOut.LogInWithFaceBook(IntroActivity.this ,fbLoginButton , callbackManager);

                    } else {
                        Toast.makeText(IntroActivity.this, "Please Check your Internet Connectivity \n And try later!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        }, 2000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


}