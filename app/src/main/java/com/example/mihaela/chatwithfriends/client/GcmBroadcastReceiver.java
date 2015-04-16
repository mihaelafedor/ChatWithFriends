package com.example.mihaela.chatwithfriends.client;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.example.mihaela.chatwithfriends.App;
import com.example.mihaela.chatwithfriends.DataProvider;
import com.example.mihaela.chatwithfriends.MainActivity;
import com.example.mihaela.chatwithfriends.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();
	
	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		wakeLock.acquire();
		
		try {
			GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
			
			String messageType = googleCloudMessaging.getMessageType(intent);
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error", false);
				
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server", false);
				
			} else {
				String message = intent.getStringExtra(DataProvider.msg);
				String email = intent.getStringExtra(DataProvider.from);
				
				ContentValues values = new ContentValues(2);
				values.put(DataProvider.msg, message);
				values.put(DataProvider.from, email);
				context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);
				
				if (App.isNotify()) {
					sendNotification("New message", true);
				}
			}
			setResultCode(Activity.RESULT_OK);
			
		} finally {
			wakeLock.release();
		}
	}
	
	private void sendNotification(String text, boolean launchApp) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification.Builder builder = new Notification.Builder(context)
			.setAutoCancel(true)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle(context.getString(R.string.app_name))
			.setContentText(text);

		
		if (launchApp) {
			Intent intent = new Intent(context, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(pendingIntent);
		}
		
		notificationManager.notify(1, builder.getNotification());
	}
}
