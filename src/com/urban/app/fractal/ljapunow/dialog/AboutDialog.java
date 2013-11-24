package com.urban.app.fractal.ljapunow.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.widget.TextView;

import com.urban.app.fractal.ljapunow.R;
import com.urban.app.fractal.ljapunow.util.FileUtil;

public class AboutDialog extends Dialog
{
	private static Context	mContext	= null;

	public AboutDialog(Context context)
	{
		super(context);
		mContext = context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.about);

		setTitle(R.string.dialog_about);

		TextView tv = (TextView) findViewById(R.id.legal_text);
		tv.setText(FileUtil.readRawTextFile(mContext, R.raw.legal));

		tv = (TextView) findViewById(R.id.info_text);
		tv.setText(Html.fromHtml(FileUtil.readRawTextFile(mContext, R.raw.info)));

		tv.setLinkTextColor(Color.WHITE);
		Linkify.addLinks(tv, Linkify.ALL);
	}
}
