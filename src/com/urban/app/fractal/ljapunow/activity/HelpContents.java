package com.urban.app.fractal.ljapunow.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.urban.app.fractal.ljapunow.R;

public class HelpContents extends ListActivity
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setTitle(R.string.help_title);

		String[] helpContents = getResources().getStringArray(R.array.help_contents);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, helpContents);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		Intent intent = new Intent(getBaseContext(), DocumentViewer.class);
		switch (position)
		{
		case 0: // Introduction
			intent.putExtra(DocumentViewer.DOCUMENT_ID, R.raw.introduction);
			break;
		case 1: // Gestures
			intent.putExtra(DocumentViewer.DOCUMENT_ID, R.raw.gestures);
			break;
		case 2: // Generate/Stop
			intent.putExtra(DocumentViewer.DOCUMENT_ID, R.raw.generate);
			break;
		case 3: // Parameters
			intent.putExtra(DocumentViewer.DOCUMENT_ID, R.raw.parameters);
			break;
		case 4: // Colors
			intent.putExtra(DocumentViewer.DOCUMENT_ID, R.raw.colors);
			break;
		case 5: // Display
			intent.putExtra(DocumentViewer.DOCUMENT_ID, R.raw.display);
			break;
		case 6: // Presets
			intent.putExtra(DocumentViewer.DOCUMENT_ID, R.raw.presets);
			break;
		}
		startActivity(intent);
	}
}
