package com.example.mihaela.chatwithfriends;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class AddContactDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Context context = getActivity();
		
		final EditText editText = new EditText(context);
		editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		
		final AlertDialog alert = new AlertDialog.Builder(context)
			.setTitle("Add Contact")
			.setView(editText)
			.setPositiveButton(android.R.string.ok, null)
			.setNegativeButton(android.R.string.cancel, null)
			.create();

		alert.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				Button okBtn = alert.getButton(AlertDialog.BUTTON_POSITIVE);
				okBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						String email = editText.getText().toString();
						if (!isEmailValid(email)) {
							editText.setError("Invalid email!");
							return;
						}
						
						try {
							ContentValues values = new ContentValues(2);
							values.put(DataProvider.name, email.substring(0, email.indexOf('@')));
							values.put(DataProvider.email, email);
							context.getContentResolver().insert(DataProvider.CONTENT_URI_PROFILE, values);
						} catch (SQLException sqle) {}
						
						alert.dismiss();
					}
				});
			}
		});
		
		return alert;
	}
	
	private boolean isEmailValid(CharSequence email) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}	

}
