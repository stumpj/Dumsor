package dumsor.org.dumsor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));


        setContentView(R.layout.activity_login);
        //setContentView(R.layout.test_layout);

        final SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
