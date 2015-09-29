package dumsor.org.dumsor;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by bfjel on 9/29/2015.
 */
public class Dumsor extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, "6jR7eMMTMhIcOb03rZiBlICHicstB6zhvqG00KSa", "sychAhNg4my8kPsoG5L78vHxAWzk8IWMKj4bqB2V");
    }

}