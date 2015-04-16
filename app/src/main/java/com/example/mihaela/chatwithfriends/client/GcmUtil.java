package com.example.mihaela.chatwithfriends.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.mihaela.chatwithfriends.App;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Random;


public class GcmUtil {
	
	private static final String TAG = "GcmUtil";
	
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";
	
    /**
     * Default lifespan (7 days) of a reservation until it is considered expired.
     */
    public static final long REGISTRATION_EXPIRY_TIME_MS = 1000 * 3600 * 24 * 7;
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    private GoogleCloudMessaging gcm;
    private SharedPreferences preferences;
    private Context context;

	public GcmUtil(Context applicationContext) {
		super();
		context = applicationContext;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		String regid = getRegistrationId();
		if (regid.length() == 0) {
            registerBackground();
        } else {
        	broadcastStatus(true);
        }
		gcm = GoogleCloudMessaging.getInstance(context);
	}
	
	/**
	 * Gets the current registration id for application on GCM service.
	 *
	 * If result is empty, the registration has failed.
	 *
	 * @return registration id, or empty string if the registration is not
	 *         complete.
	 */
	private String getRegistrationId() {
	    String registrationId = preferences.getString(PROPERTY_REG_ID, "");
	    if (registrationId.length() == 0) {
	      return "";
	    }
	    // check if app was updated; if so, it must clear registration id to
	    // avoid a race condition if GCM sends a message
	    int registeredVersion = preferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion();
	    if (registeredVersion != currentVersion || isRegistrationExpired()) {

	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's {@code SharedPreferences}.
	 *
	 * @param regId registration id
	 */
	private void setRegistrationId(String regId) {
	    int appVersion = getAppVersion();
	    SharedPreferences.Editor editor = preferences.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;

	    editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
	    editor.commit();
	}	
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private int getAppVersion() {
	    try {
	        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * Checks if the registration has expired.
	 *
	 * To avoid the scenario where the device sends the registration to the
	 * server but the server loses it, the app developer may choose to re-register
	 * after REGISTRATION_EXPIRY_TIME_MS.
	 *
	 * @return true if the registration has expired.
	 */
	private boolean isRegistrationExpired() {
	    // checks if the information is not stale
	    long expirationTime = preferences.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
	    return System.currentTimeMillis() > expirationTime;
	}	
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 *
	 * Stores the registration id, app versionCode, and expiration time in the 
	 * application's shared preferences.
	 */
	private void registerBackground() {
	    new AsyncTask<Void, Void, Boolean>() {
	        @Override
	        protected Boolean doInBackground(Void... params) {
	            long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
	            for (int i = 1; i <= MAX_ATTEMPTS; i++) {
	            	Log.d(TAG, "Attempt #" + i + " to register");
		            try {
		                if (gcm == null) {
		                    gcm = GoogleCloudMessaging.getInstance(context);
		                }
		                String regid = gcm.register(App.getSenderId());
	
		                // You should send the registration ID to your server over HTTP,
		                // so it can use GCM to send messages to your app.
		                ServerUtilities.register(App.getPreferredEmail(), regid);
	
		                // Save the regid - no need to register again.
		                setRegistrationId(regid);
		                return Boolean.TRUE;
		                
		            } catch (IOException ex) {
		                Log.e(TAG, "Failed to register on attempt " + i + ":" + ex);
		                if (i == MAX_ATTEMPTS) {
		                    break;
		                }
		                try {
		                    Thread.sleep(backoff);
		                } catch (InterruptedException e1) {

		                    Thread.currentThread().interrupt();
		                }
		                // increase backoff exponentially
		                backoff *= 2;		                
		            }
	            }
	            return Boolean.FALSE;
	        }

	        @Override
	        protected void onPostExecute(Boolean status) {
	        	broadcastStatus(status);
	        }
	    }.execute(null, null, null);
	}
	
	private void broadcastStatus(boolean status) {
    	Intent intent = new Intent(App.ACTION_REGISTER);
        intent.putExtra(App.EXTRA_STATUS, status ? App.STATUS_SUCCESS : App.STATUS_FAILED);
        context.sendBroadcast(intent);
	}
	
	public void cleanup() {
		gcm.close();
		context = null;
		preferences = null;
		gcm = null;
	}	
	
}
