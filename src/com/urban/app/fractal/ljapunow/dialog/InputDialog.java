package com.urban.app.fractal.ljapunow.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.urban.app.fractal.ljapunow.R;
import com.urban.app.fractal.ljapunow.util.Storage;

public class InputDialog extends Builder implements OnClickListener, DialogInterface.OnDismissListener
{
	public static final int	UNSIGNED_NUMBER		= InputType.TYPE_CLASS_NUMBER;
	public static final int	SIGNED_NUMBER		= InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
	public static final int	SIGNED_FLOAT_NUMBER	= InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED;
	public static final int	TEXT				= InputType.TYPE_CLASS_TEXT;
	public static final int	CAPITALS			= InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;

	protected EditText		firstValue			= null;
	protected EditText		secondValue			= null;
	protected String		storageID			= null;
	protected Context		context				= null;
	protected AlertDialog   dialog              = null;
	protected boolean	    neutralClicked	    = false;

	public InputDialog(Context context, int title, boolean singleValue, String storageID, int inputType)
	{
		super(context);
		init(context, title, -1, singleValue, storageID, inputType);
	}

	public InputDialog(Context context, int title, int neutralButtonText, boolean singleValue, String storageID, int inputType)
	{
		super(context);
		init(context, title, neutralButtonText, singleValue, storageID, inputType);
	}

	private void init(Context context, int title, int neutralButtonText, boolean singleValue, String storageID, int inputType)
	{
		this.context = context;
		this.storageID = storageID;

		setTitle(title);

		String initialValue = (Storage.has(storageID) ? Storage.get(storageID) : storageID);

		if (singleValue)
		{
			firstValue = new EditText(context);
			firstValue.setText(initialValue);
			firstValue.setInputType(inputType);
			firstValue.setSingleLine();
			setView(firstValue);
		}
		else
		{
			String[] values = initialValue.split(",");
			LayoutInflater factory = LayoutInflater.from(context);
			View twoValueView = factory.inflate(R.layout.two_value_input, null);
			firstValue = (EditText) twoValueView.findViewById(R.id.edit_value1);
			secondValue = (EditText) twoValueView.findViewById(R.id.edit_value2);
			firstValue.setInputType(inputType);
			secondValue.setInputType(inputType);
			firstValue.setText(values[0]);
			secondValue.setText(values[1]);
			setView(twoValueView);
		}

		setPositiveButton(R.string.ok, this);
		setNegativeButton(R.string.cancel, this);
		if (neutralButtonText != -1)
		{
			setNeutralButton(neutralButtonText, this);
		}
	}

	public void onClick(DialogInterface di, int which)
	{
		if (which == DialogInterface.BUTTON_POSITIVE)
		{
			if (secondValue == null)
			{
				if (onOkClicked(firstValue.getText().toString()))
				{
					if (context instanceof Storage.OnChangeListener)
					{
						((Storage.OnChangeListener) context).onStorageChange(storageID);
					}
					di.dismiss();
				}
			}
			else if (onOkClicked(firstValue.getText().toString(), secondValue.getText().toString()))
			{
				if (context instanceof Storage.OnChangeListener)
				{
					((Storage.OnChangeListener) context).onStorageChange(storageID);
				}
				di.dismiss();
			}
		}
		else if (which == DialogInterface.BUTTON_NEGATIVE)
		{
			if (onCancelClicked())
			{
				di.dismiss();
			}
		}
		else if (which == DialogInterface.BUTTON_NEUTRAL)
		{
			neutralClicked = true;
			if (onNeutralClicked())
			{
				di.dismiss();
			}
		}
	}

	public boolean onCancelClicked()
	{
		return true;
	}

	public boolean onNeutralClicked()
	{
		return true;
	}

	public boolean onOkClicked(String input)
	{
		Storage.set(storageID, input);
		return true;
	}

	public boolean onOkClicked(String input1, String input2)
	{
		Storage.set(storageID, input1 + "," + input2);
		return true;
	}

	@Override
	public AlertDialog create()
	{
		dialog = super.create();
		dialog.setOnDismissListener(this);
		return dialog;
	}

	public void onDismiss(DialogInterface di)
	{
		if (neutralClicked)
		{
			neutralClicked = false;
			dialog.show();
		}
	}
}
