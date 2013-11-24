package com.urban.app.fractal.ljapunow.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.urban.app.fractal.ljapunow.R;
import com.urban.app.fractal.ljapunow.util.FileUtil;

public class DocumentViewer extends Activity
{
	/** Intent ID to deliver the document ID to view. */
	public static final String	DOCUMENT_ID	= "DOCUMENT_ID";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		WebView view = new WebView(this);
		setContentView(view);

		setTitle(R.string.help_title);

		// Load document into view
		int documentId = getIntent().getIntExtra(DOCUMENT_ID, -1);
		view.loadDataWithBaseURL(null, FileUtil.readRawTextFile(this, documentId), "text/html", "UTF-8", "about:blank");
	}
}
