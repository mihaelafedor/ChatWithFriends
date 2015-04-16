package com.example.mihaela.chatwithfriends;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MessagesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateFormat[] dateFormat = new DateFormat[] {
		DateFormat.getDateInstance(), DateFormat.getTimeInstance()};

	private OnFragmentInteractionListener listener;
	private SimpleCursorAdapter simpleCursorAdapter;
	private Date date;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		date = new Date();
		
		simpleCursorAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.list_item_chat,
				null, 
				new String[]{DataProvider.msg, DataProvider.at},
				new int[]{R.id.text1, R.id.text2},
				0);
		
		simpleCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (view.getId()) {
                    case R.id.text1:
                        LinearLayout root = (LinearLayout) view.getParent().getParent();
                        if (cursor.getString(cursor.getColumnIndex(DataProvider.from)) == null) {
                            root.setGravity(Gravity.RIGHT);
                            root.setPadding(50, 10, 10, 10);
                        } else {
                            root.setGravity(Gravity.LEFT);
                            root.setPadding(10, 10, 50, 10);
                        }
                        break;

                    case R.id.text2:
                        TextView textView = (TextView) view;
                        textView.setText(getDisplayTime(cursor.getString(columnIndex)));
                        return true;
                }
                return false;
            }
        });
		
		setListAdapter(simpleCursorAdapter);
	}	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getListView().setDivider(null);
		
		Bundle args = new Bundle();
		args.putString(DataProvider.email, listener.getProfileEmail());
		getLoaderManager().initLoader(0, args, this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	public interface OnFragmentInteractionListener {
		public String getProfileEmail();
	}
	
	private String getDisplayTime(String datetime) {
		try {
			Date dt = simpleDateFormat.parse(datetime);
			if (date.getYear()==dt.getYear() && date.getMonth()==dt.getMonth() && date.getDate()==dt.getDate()) {
				return dateFormat[1].format(dt);
			}
			return dateFormat[0].format(dt);
		} catch (ParseException e) {
			return datetime;
		}
	}
	
	//----------------------------------------------------------------------------

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String profileEmail = args.getString(DataProvider.email);
		CursorLoader loader = new CursorLoader(getActivity(),
				DataProvider.CONTENT_URI_MESSAGES, 
				null, 
				DataProvider.from + " = ? or " + DataProvider.to + " = ?",
				new String[]{profileEmail, profileEmail},
				DataProvider.at + " DESC");
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		simpleCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		simpleCursorAdapter.swapCursor(null);
	}

}
