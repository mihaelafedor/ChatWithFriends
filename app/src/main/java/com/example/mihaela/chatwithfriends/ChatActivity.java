package com.example.mihaela.chatwithfriends;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mihaela.chatwithfriends.client.GcmUtil;
import com.example.mihaela.chatwithfriends.client.ServerUtilities;

import java.io.IOException;


public class ChatActivity extends Activity implements MessagesFragment.OnFragmentInteractionListener {

    private EditText editText;
	private Button button;
	private String profileId;
	private String profileName;
	private String profileEmail;
	
	private GcmUtil gcmUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		profileId = getIntent().getStringExtra(App.PROFILE_ID);
		editText = (EditText) findViewById(R.id.msg_edit);
		button = (Button) findViewById(R.id.send_btn);
		
		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Cursor c = getContentResolver().query(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
		if (c.moveToFirst()) {
			profileName = c.getString(c.getColumnIndex(DataProvider.name));
			profileEmail = c.getString(c.getColumnIndex(DataProvider.email));
			actionBar.setTitle(profileName);
		}
		//actionBar.setSubtitle("connecting ...");
		
		registerReceiver(registrationStatusReceiver, new IntentFilter(App.ACTION_REGISTER));
		gcmUtil = new GcmUtil(getApplicationContext());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_chat, menu);
		return true;
	}	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;			
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.send_btn:
			send(editText.getText().toString());
			editText.setText(null);
			break;
		}
	}

	@Override
	public String getProfileEmail() {
		return profileEmail;
	}	
	
	private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    ServerUtilities.send(txt, profileEmail);
                    
        			ContentValues values = new ContentValues(2);
        			values.put(DataProvider.msg, txt);
        			values.put(DataProvider.to, profileEmail);
        			getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
        			
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	if (!TextUtils.isEmpty(msg)) {
            		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            	}
            }
        }.execute(null, null, null);		
	}	

	@Override
	protected void onPause() {
		ContentValues values = new ContentValues(1);
		values.put(DataProvider.count, 0);
		getContentResolver().update(Uri.withAppendedPath(DataProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(registrationStatusReceiver);
		gcmUtil.cleanup();
		super.onDestroy();
	}

	
	private BroadcastReceiver registrationStatusReceiver = new  BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && App.ACTION_REGISTER.equals(intent.getAction())) {
				switch (intent.getIntExtra(App.EXTRA_STATUS, 100)) {
				case App.STATUS_SUCCESS:
					getActionBar().setSubtitle("online");
					button.setEnabled(true);
					break;
					
				case App.STATUS_FAILED:
					getActionBar().setSubtitle("offline");					
					break;					
				}
			}
		}
	};	

}
