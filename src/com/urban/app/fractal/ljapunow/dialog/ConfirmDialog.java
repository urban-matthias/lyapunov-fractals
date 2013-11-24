package com.urban.app.fractal.ljapunow.dialog;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.urban.app.fractal.ljapunow.R;

public class ConfirmDialog extends Builder implements OnClickListener
{
	public ConfirmDialog(Context context, int messageId)
	{
		super(context);
		setMessage(messageId);
		setPositiveButton(R.string.ok, this);
		setNegativeButton(R.string.cancel, this);
	}

	public ConfirmDialog(Context context, String message)
	{
		super(context);
		setMessage(message);
		setPositiveButton(R.string.ok, this);
		setNegativeButton(R.string.cancel, this);
	}

	public ConfirmDialog(Context context, int titleId, int messageId)
	{
		super(context);
		setTitle(titleId);
		setMessage(messageId);
		setPositiveButton(R.string.ok, this);
		setNegativeButton(R.string.cancel, this);
	}

	public ConfirmDialog(Context context, String title, int messageId)
	{
		super(context);
		setTitle(title);
		setMessage(messageId);
		setPositiveButton(R.string.ok, this);
		setNegativeButton(R.string.cancel, this);
	}

	public ConfirmDialog(Context context, int titleId, String message)
	{
		super(context);
		setTitle(titleId);
		setMessage(message);
		setPositiveButton(R.string.ok, this);
		setNegativeButton(R.string.cancel, this);
	}

	public ConfirmDialog(Context context, String title, String message)
	{
		super(context);
		setTitle(title);
		setMessage(message);
		setPositiveButton(R.string.ok, this);
		setNegativeButton(R.string.cancel, this);
	}

	public void onClick(DialogInterface dialog, int which)
	{
		if (which == DialogInterface.BUTTON_POSITIVE)
		{
			if (onOkClicked())
			{
				dialog.dismiss();
			}
		}
		else if (onCancelClicked())
		{
			dialog.dismiss();
		}
	}

	public boolean onCancelClicked()
	{
		return true;
	}

	public boolean onOkClicked()
	{
		return true;
	}
}
