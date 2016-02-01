package dumsor.org.dumsor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.login.LoginManager;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRole;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.twitter.sdk.android.Twitter;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private static final int MAX_CONNECT_TRIES = 3;
    private static final int WAIT_TIME = 2000;
    private static final int POWER_PAUSE = 60000;
    private static final boolean POWER_VISIBLE = false;
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final String GPS = "GPS";
    private static final String NETWORK = "NETWORK";
    private SharedPreferences sharedPreferences;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private long previousTime;
    private int power;
    private ToggleButton toggleButton;
    private MobileServiceClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Log in to parse anonymously
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {

                if (e != null) {
                    Log.d("MainActivity", "Login failed");
                } else {
                    parseUser.saveInBackground();

                }
            }


        });

        try {
            mClient = new MobileServiceClient(
                    "https://dumortest.azure-mobile.net/",
                    "dAwHNatRmDTzFZUGTgDVXNaWqoEtRS55",
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        sharedPreferences = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        toggleButton = (ToggleButton) findViewById(R.id.powerToggleButton);
        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setImageResource(R.drawable.dumsor_launcher);

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.layout_screen);
        rl.setBackgroundColor(Color.WHITE);

        previousTime = 0;
        power = 0;

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        //listen for location changes
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                sharedPreferences
                        .edit()
                        .putString(LAT, Double.toString(location.getLatitude()))
                        .putString(LNG, Double.toString((location.getLongitude())))
                        .apply();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        //establish connections via GPS or netwrok
        checkConnections();

        toggleButton.setChecked(false);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RelativeLayout rl = (RelativeLayout) findViewById(R.id.layout_screen);
                toggleButton.setEnabled(false);
                if(!POWER_VISIBLE) toggleButton.setVisibility(View.INVISIBLE);

                Timer buttonTimer = new Timer();
                buttonTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                toggleButton.setEnabled(true);
                                if(!POWER_VISIBLE) toggleButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }, POWER_PAUSE);

                if (isChecked) {
                    rl.setBackgroundColor(Color.BLACK);
                    Toast.makeText(getApplicationContext(), getString(R.string
                            .main_power_off_message), Toast.LENGTH_LONG).show();
                    power = 1;
                    recordLocation(1);
                } else {
                    rl.setBackgroundColor(Color.WHITE);
                    Toast.makeText(getApplicationContext(), getString(R.string
                            .main_power_on_message), Toast.LENGTH_LONG).show();
                    power = 0;
                    recordLocation(0);
                }
            }
        });
    }


    /**
     * Check and start location services for the best available connection.
     *
     * @return Whether or not a connection was established.
     */
    private boolean checkConnections() {
        LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isGpsEnabled && !isNetworkEnabled) {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(isGpsEnabled || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(MainActivity.this, "GPS Enabled", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            sharedPreferences
                    .edit()
                    .putString("location", GPS)
                    .apply();
            return true;
        } else if(isNetworkEnabled || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(MainActivity.this, "Network Enabled", Toast.LENGTH_SHORT).show();

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            sharedPreferences
                    .edit()
                    .putString("location", NETWORK)
                    .apply();
            return true;

        } else {
            Toast.makeText(MainActivity.this, getString(R.string.loc_not_found_error), Toast.LENGTH_SHORT).show();
            sharedPreferences
                    .edit()
                    .putString("location", "NONE")
                    .apply();
            return false;
        }

    }

    /**
     * Record the current location of the user to cloud services.
     *
     * @param powerStatus If the power is off (0) or on (1).
     */
    private void recordLocation(int powerStatus) {

        if(powerStatus == 0) previousTime = System.currentTimeMillis();


        String testConnection = sharedPreferences.getString("location", null);
        //If connected via GPS update lat and lng, run a background thread to look for location update
        if(GPS.equals(testConnection) || NETWORK.equals(testConnection)) {

            AsyncNetwork asyncNetwork = new AsyncNetwork();
            asyncNetwork.execute();

        //if not connected, recheck connections
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.loc_not_found_error), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Saves data to Parse cloud service.
     */
    private void parseSave(final int age) {


        long currentTime = System.currentTimeMillis();

        ParseObject powerRecord = new ParseObject("Power");

        powerRecord.put("auth", sharedPreferences.getString("login", null));
        powerRecord.put("source", sharedPreferences.getString("uid", null));

        if (age == 0) {
            powerRecord.put("lat", Double.parseDouble(sharedPreferences.getString("lat", null)));
            powerRecord.put("long", Double.parseDouble(sharedPreferences.getString("lng", null)));
        } else {
            powerRecord.put("lat", Double.parseDouble(sharedPreferences.getString("latold", null)));
            powerRecord.put("long", Double.parseDouble(sharedPreferences.getString("lngold", null)));
        }
        powerRecord.put("timestamp", currentTime);
        if (previousTime == 0) {
            powerRecord.put("previous", currentTime);
        } else {
            powerRecord.put("previous", previousTime);
        }
        powerRecord.put("power", power);
        powerRecord.saveInBackground();
    }

    private void azureSave(final int age) {

        long currentTime = System.currentTimeMillis();

        Point pt = new Point();

        pt.auth = sharedPreferences.getString("login", null);
        pt.source = sharedPreferences.getString("uid", null);



        if (age == 0) {
            pt.lat = Double.parseDouble(sharedPreferences.getString("lat", null));
            pt.lng = Double.parseDouble(sharedPreferences.getString("lng", null));
        } else {
            pt.lat = Double.parseDouble(sharedPreferences.getString("latold", null));
            pt.lng = Double.parseDouble(sharedPreferences.getString("lngold", null));
        }

        pt.timestamp = currentTime;

        if (previousTime == 0) {
            pt.previous = currentTime;
        } else {
            pt.previous = previousTime;
        }

        pt.power = power;

        //powerRecord.saveInBackground();

        mClient.getTable(Point.class).insert(pt, new TableOperationCallback<Point>() {
            public void onCompleted(Point entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    Log.i("MainAcitivity", "Insert worked");
                } else {
                    Log.i("MainAcitivity", "Insert failed");
                }
            }
        });
    }

    /**
     * Log out of app, return to main menu.
     */
    public void logout() {

        String login = sharedPreferences.getString("login", null);

        if("Facebook".equals(login)) {

            LoginManager.getInstance().logOut();

        } else if ("Twitter".equals(login)) {

            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();

        }

        sharedPreferences
                .edit()
                .remove("login")
                .remove("uid")
                .apply();

        Intent i = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!"NONE".equals(sharedPreferences.getString("location", null))) {
            locationManager.removeUpdates(locationListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!"NONE".equals(sharedPreferences.getString("location", null))) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(GPS.equals(sharedPreferences.getString("location", null))) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        if(NETWORK.equals(sharedPreferences.getString("location", null))) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * This AsyncTask runs in the background after the button is pressed to check that status of
     * the network provider update for location.  It will look as many times as the
     * constant MAX_CONNECT_TRIES is set and wait between tries at WAIT_TIME is set for (in MS).
     *
     * If the location is found, it will update the information and if not, it will do nothing.
     */
    private class AsyncNetwork extends AsyncTask<String, String, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            for(int i = 0; i < MAX_CONNECT_TRIES; i++) {
                if(sharedPreferences.getString("lat", null) == null) {
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {

                    return true;

                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if(result) {
                azureSave(0);
                //parseSave(0);
            } else {
                if(sharedPreferences.getString("latold", null) != null) {
                    //parseSave(1);
                    azureSave(1);
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.loc_not_found_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
