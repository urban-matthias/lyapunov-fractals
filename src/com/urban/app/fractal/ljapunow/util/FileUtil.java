package com.urban.app.fractal.ljapunow.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

public class FileUtil
{
	public static String readRawTextFile(Context context, int id)
	{
		InputStream inputStream = context.getResources().openRawResource(id);
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader buf = new BufferedReader(in, 1024);
		String line;
		StringBuilder text = new StringBuilder();
		try
		{
			while ((line = buf.readLine()) != null)
				text.append(line);
		}
		catch (IOException e)
		{
		}
		return text.toString();
	}
}
