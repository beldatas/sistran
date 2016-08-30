package net.sistransito.mobile.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AnyDiaglog {
	
	public static void DialogShow(String mgs, Context context, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context,
				AlertDialog.THEME_HOLO_LIGHT);
		builder.setTitle(title);
		builder.setMessage(mgs);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

}
