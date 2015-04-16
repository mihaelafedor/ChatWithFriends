package com.example.mihaela.chatwithfriends;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DataProvider extends ContentProvider {
	
	public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://com.example.mihaela.provider/messages");
	public static final Uri CONTENT_URI_PROFILE = Uri.parse("content://com.example.mihaela.provider/profile");

	public static final String id = "_id";
	
	public static final String messages = "messages";
	public static final String msg = "msg";
	public static final String from = "email";
	public static final String to = "email2";
	public static final String at = "at";
	
	public static final String profile = "profile";
	public static final String name = "name";
	public static final String email = "email";
	public static final String count = "count";
	
	private DbHelper dbHelper;
	
	private static final int MESSAGES_ALLROWS = 1;
	private static final int MESSAGES_SINGLE_ROW = 2;
	private static final int PROFILE_ALLROWS = 3;
	private static final int PROFILE_SINGLE_ROW = 4;
	
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.example.mihaela.provider", "messages", MESSAGES_ALLROWS);
		uriMatcher.addURI("com.example.mihaela.provider", "messages/#", MESSAGES_SINGLE_ROW);
		uriMatcher.addURI("com.example.mihaela.provider", "profile", PROFILE_ALLROWS);
		uriMatcher.addURI("com.example.mihaela.provider", "profile/#", PROFILE_SINGLE_ROW);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			qb.setTables(messages);
			break;			
			
		case MESSAGES_SINGLE_ROW:
			qb.setTables(messages);
			qb.appendWhere("_id = " + uri.getLastPathSegment());
			break;

		case PROFILE_ALLROWS:
			qb.setTables(profile);
			break;			
			
		case PROFILE_SINGLE_ROW:
			qb.setTables(profile);
			qb.appendWhere("_id = " + uri.getLastPathSegment());
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		long id;
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			id = db.insertOrThrow(messages, null, values);
			if (values.get(to) == null) {
				db.execSQL("update profile set count = count+1 where email = ?", new Object[]{values.get(from)});
				getContext().getContentResolver().notifyChange(CONTENT_URI_PROFILE, null);
			}
			break;
			
		case PROFILE_ALLROWS:
			id = db.insertOrThrow(profile, null, values);
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		Uri insertUri = ContentUris.withAppendedId(uri, id);
		getContext().getContentResolver().notifyChange(insertUri, null);
		return insertUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		int count;
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			count = db.update(messages, values, selection, selectionArgs);
			break;			
			
		case MESSAGES_SINGLE_ROW:
			count = db.update(messages, values, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;

		case PROFILE_ALLROWS:
			count = db.update(profile, values, selection, selectionArgs);
			break;			
			
		case PROFILE_SINGLE_ROW:
			count = db.update(profile, values, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		int count;
		switch(uriMatcher.match(uri)) {
		case MESSAGES_ALLROWS:
			count = db.delete(messages, selection, selectionArgs);
			break;			
			
		case MESSAGES_SINGLE_ROW:
			count = db.delete(messages, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;

		case PROFILE_ALLROWS:
			count = db.delete(profile, selection, selectionArgs);
			break;			
			
		case PROFILE_SINGLE_ROW:
			count = db.delete(profile, "_id = ?", new String[]{uri.getLastPathSegment()});
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}	
	
	//--------------------------------------------------------------------------
	
	private static class DbHelper extends SQLiteOpenHelper {
		
		private static final String DATABASE_NAME = "chatwithfriends.db";
		private static final int DATABASE_VERSION = 1;

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table messages (_id integer primary key autoincrement, msg text, email text, email2 text, at datetime default current_timestamp);");
			db.execSQL("create table profile (_id integer primary key autoincrement, name text, email text unique, count integer default 0);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
