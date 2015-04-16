package com.example.mihaela.chatwithfriends;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Patterns;

import com.example.mihaela.chatwithfriends.client.Constants;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
	
	public static final String PROFILE_ID = "profile_id";
	
	public static final String ACTION_REGISTER = "com.example.mihaela.chatwithfriends.REGISTER";
	public static final String EXTRA_STATUS = "status";
	public static final int STATUS_SUCCESS = 1;
	public static final int STATUS_FAILED = 0;
	
	//parameters recognized by demo server
	public static final String from = "email";
	public static final String regId = "regId";
	public static final String message = "msg";
	public static final String to= "email2";
	
	public static String[] listOfEmails;
	
	private static SharedPreferences prefs;

	@Override
	public void onCreate() {
		super.onCreate();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		List<String> emailList = getEmailList();
		listOfEmails = emailList.toArray(new String[emailList.size()]);
	}
	
	private List<String> getEmailList() {
		List<String> lst = new ArrayList<String>();
		Account[] accounts = AccountManager.get(this).getAccounts();
		for (Account account : accounts) {
		    if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
		        lst.add(account.name);
		    }
		}
		return lst;
	}
	
	public static String getPreferredEmail() {
		return prefs.getString("chat_email_id", listOfEmails.length==0 ? "abc@example.com" : listOfEmails[0]);
	}
	
	public static String getDisplayName() {
		String email = getPreferredEmail();
		return prefs.getString("display_name", email.substring(0, email.indexOf('@')));
	}

	public static String getServerUrl() {
		return prefs.getString("server_url_pref", Constants.SERVER_URL);
	}
	
	public static String getSenderId() {
		return prefs.getString("sender_id_pref", Constants.SENDER_ID);
	}

    public static boolean isNotify() {
        return prefs.getBoolean("notifications_new_message", true);
    }
}
