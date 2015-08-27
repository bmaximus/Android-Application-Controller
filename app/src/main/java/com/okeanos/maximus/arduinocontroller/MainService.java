package com.okeanos.maximus.arduinocontroller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public class MainService extends Activity {

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "82258115354";
    // String REGISTRATION_ID  = "APA91bEvMmoQndNVtbPPKFzgojA7A8d89G0KfcYwCEl1XzuBwnfuC2SkgMmpX43rADkPbgmrD_TpkAKj9oju2sTkVKPSmQ5iph7U8O7Kh6MFDv-4br8yaiUZEFdgRyvKN50gBYcvmFpC0lT1e7ki2esMedR4Etwe9iY-b5j5ja3O_tXmgcncF0A";
    String REGISTRATION_ID  = "APA91bE2MARvhS7piffKXFrEh6n7vqYAzLgI7P3eswEQLfF-X-aDHhfMp2x0pAV_xonnzst6kwzzpJKphBpbSOcpaI6JUTGsaS25ywyVjQECdekCQtsz-R1z0OTdSDeYtOCJYTfwZENiGMlr0GnHtO6VSOJkurictQ";
    static final String TAG = "GCMDemo";
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_service);

        context = getApplicationContext();

        if (context == null ){Toast.makeText(getApplicationContext(), "Context is NULL", Toast.LENGTH_SHORT).show();}
        else {Toast.makeText(getApplicationContext(), "Context OK", Toast.LENGTH_SHORT).show();}

        boolean serviceStatus = checkPlayServices();

        if (!serviceStatus)   { Toast.makeText(getApplicationContext(), "Service is NULL", Toast.LENGTH_SHORT).show(); }
        else {Toast.makeText(getApplicationContext(), "Serivice OK", Toast.LENGTH_SHORT).show();}

        gcm = GoogleCloudMessaging.getInstance(this);

        if (gcm == null)   { Toast.makeText(getApplicationContext(), "Gcm is NULL", Toast.LENGTH_SHORT).show(); }
        else {Toast.makeText(getApplicationContext(), "Gcm OK", Toast.LENGTH_SHORT).show();}


        regid = REGISTRATION_ID; //getRegistrationId(context);

         if (regid.isEmpty())   { Toast.makeText(getApplicationContext(), "Registration Id is NULL", Toast.LENGTH_SHORT).show();
            registerInBackground(); }
        else {Toast.makeText(getApplicationContext(), ("Registration Id OK "+ regid), Toast.LENGTH_SHORT).show();}
        //registerInBackground();


    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onMovementSwitchChanged(View view) {

        boolean on = ((Switch) view).isChecked();
        if (on)
        {
            Toast.makeText(getApplicationContext(), "Movement On", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Movement Off", Toast.LENGTH_SHORT).show();
        }
    }

    public void onMagnetSwitchChanged(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();
        if (on)
        {
            Toast.makeText(getApplicationContext(), "Magnet On", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Magnet Off", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLightSwitchChanged(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();
        if (on)
        {
            Toast.makeText(getApplicationContext(), "Light On", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Light Off", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainService.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void sendRegistrationIdToBackend(String regId) throws IOException {

        if (regId!= null && regId != "")
        {

          HttpClient client = new DefaultHttpClient();
          HttpPost post = new HttpPost(("83.212.84.224:8080/ArduinoRestService/rest/regid/" + regId));
          HttpResponse response;
          response = client.execute(post);
            Log.v("Asynck", "registered to Server");
         // Toast.makeText(getApplicationContext(), ("REGG IDDDDD" + regid), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Log.v("Asynck", "FAILED TO register to Server");
           // Toast.makeText(getApplicationContext(), ("REGG id is NULL " + regid), Toast.LENGTH_SHORT).show();
        }


    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private void registerInBackground() {
        Toast.makeText(getApplicationContext(), ("STARTED"), Toast.LENGTH_SHORT).show();
        new AsyncTask<Void,Void,String>() {

            protected String doInBackground(Void... params) {
                String msg = "dfghjkihgfd";
               // Toast.makeText(getApplicationContext(), ("step 1"), Toast.LENGTH_SHORT).show();
                Log.v("Asynck","step 1");
                try {
                  //  Toast.makeText(getApplicationContext(), ("step 2"), Toast.LENGTH_SHORT).show();
                    Log.v("Asynck", "step 2");
                    if (gcm == null) {
                      // Toast.makeText(getApplicationContext(), ("step 3"), Toast.LENGTH_SHORT).show();
                        Log.v("Asynck", "step 3");
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                        Log.v("Asynck", gcm.toString());
                        //Toast.makeText(getApplicationContext(), (gcm.toString()), Toast.LENGTH_SHORT).show();

                    }else{
                        //Toast.makeText(getApplicationContext(), ("step 4"), Toast.LENGTH_SHORT).show();
                        Log.v("Asynck", "step 4");
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                        //Toast.makeText(getApplicationContext(), (gcm.toString()), Toast.LENGTH_SHORT).show();
                        Log.v("Asynck", gcm.toString());
                    }
                   // Toast.makeText(getApplicationContext(), ("step 5"), Toast.LENGTH_SHORT).show();
                    Log.v("Asynck", "step 5");
                    Log.v("Asynck", SENDER_ID);
                    regid = gcm.register(SENDER_ID);

                    //Toast.makeText(getApplicationContext(), ("step 6"), Toast.LENGTH_SHORT).show();
                    Log.v("Asynck", "step 6");
                    if (regid.isEmpty() )
                    {
                        //Toast.makeText(getApplicationContext(), "Registration is NULL", Toast.LENGTH_SHORT).show();
                        Log.v("Asynck", "Registration is NULL");
                    }
                    else if( regid =="")
                    {
                       // Toast.makeText(getApplicationContext(), "Registration is EMPTY", Toast.LENGTH_SHORT).show();
                        Log.v("Asynck", "Registration is EMTY");
                    }
                    else
                    {
                       // Toast.makeText(getApplicationContext(), ("Registration OK" + regid), Toast.LENGTH_SHORT).show();
                        Log.v("Asynck", ("Registration is OK " + regid));
                    }
                  //  Toast.makeText(getApplicationContext(), ("step 7"), Toast.LENGTH_SHORT).show();
                    Log.v("Asynck", "Step - 7");
                   // msg = "Device registered, registration ID=" + regid;
                    sendRegistrationIdToBackend(regid);
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                 // "assss"; //  msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return regid;
            }



        }.execute();

    }

}
