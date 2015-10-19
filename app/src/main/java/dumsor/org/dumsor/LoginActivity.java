package dumsor.org.dumsor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseRole;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "ZDBXQCMOBFN6sw4kC5zUxbWPr";
    private static final String TWITTER_SECRET = "i5xSWYArLXTLQjJH6GEstGMcHcmqJVPJcVW3VXHzsWSqxp1Qv3";

    private CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_login);

        final SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        if (sharedPreferences.contains("login") && sharedPreferences.contains("uid"))
        {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //Facebook Login

        LoginButton facebookButton = (LoginButton) findViewById(R.id.facebook_button);
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>()
        {
            @Override
            public void onSuccess(LoginResult loginResult)
            {
                Toast.makeText(LoginActivity.this, "Facebook Login Success.", Toast.LENGTH_SHORT).show();

                sharedPreferences
                        .edit()
                        .putString("login", "Facebook")
                        .putString("uid", loginResult.getAccessToken().getUserId())
                        .apply();

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onCancel()
            {
                Toast.makeText(getApplicationContext(), "Facebook Login Cancelled.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception)
            {
                Toast.makeText(getApplicationContext(), "Facebook Login Failed.",
                        Toast.LENGTH_SHORT).show();
            }
        });


        //Twitter Login
        TwitterLoginButton twitterLogin = (TwitterLoginButton) findViewById(R.id.twitter_button);
        twitterLogin.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                sharedPreferences
                        .edit()
                        .putString("login", "Twitter")
                        .putString("uid", Long.toString(result.data.getUserId()))
                        .apply();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(LoginActivity.this, "Twitter Login Failed.", Toast.LENGTH_SHORT).show();
            }
        });

        //Guest Login
        Button guestLogin = (Button) findViewById(R.id.guest_button);
        guestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sharedPreferences
                        .edit()
                        .putString("login", "Guest")
                        .putString("uid", "0")
                        .apply();

                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TwitterLoginButton twitterLogin = (TwitterLoginButton) findViewById(R.id.twitter_button);

        // Pass the activity result to the login button.
        twitterLogin.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
