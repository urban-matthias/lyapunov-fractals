package com.urban.app.fractal.ljapunow.dialog;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;

public class DialogRunner extends AsyncTask<Void, AlertDialog.Builder, Void>
{
	private List<AlertDialog.Builder>	mList	= new LinkedList<AlertDialog.Builder>();
	private Activity					activity;
	private Dialog						dialog;

	public DialogRunner(Activity act)
	{
		activity = act;
	}

	public void add(AlertDialog.Builder dialog)
	{
		mList.add(dialog);
	}

	@Override
	protected Void doInBackground(Void... arg0)
	{
		dialog = null;
		for (final AlertDialog.Builder builder : mList)
		{
			activity.runOnUiThread(new Runnable()
			{
				public void run()
				{
					dialog = builder.show();
				}
			});
			while (dialog == null || dialog.isShowing())
				;
			dialog = null;
		}
		return null;
	}
}
