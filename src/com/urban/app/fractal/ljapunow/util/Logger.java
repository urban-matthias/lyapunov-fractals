package com.urban.app.fractal.ljapunow.util;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Toast;

import com.urban.app.fractal.ljapunow.R;

public class Logger
{
	public static void error(View view, Throwable e)
	{
		error(view.getContext(), e);
	}

	public static void error(Context context, Throwable e)
	{
		try
		{
			Toast.makeText(context, getMessage(context.getResources(), e), Toast.LENGTH_LONG).show();
		}
		catch (Throwable ignore)
		{
		}
//		try
//		{
//			final File file = new File(Environment.getExternalStorageDirectory(), "ljapunow_fractals_error.log");
//			final FileOutputStream stream = new FileOutputStream(file, true);
//			final PrintStream out = new PrintStream(stream, true);
//			out.println("#\n# " + new Date(System.currentTimeMillis()) + "\n#\nError: " + e.getMessage());
//			e.printStackTrace(out);
//			out.println();
//			out.close();
//			stream.close();
//		}
//		catch (Throwable t)
//		{
//		}
	}

	private static String getMessage(Resources res, Throwable e)
	{
		String msg = res.getString(R.string.sorry) + ", " + e.getClass().getSimpleName();
		if (e.getMessage() != null)
		{
			msg += ", " + e.getMessage();
		}
		return msg;
	}
}
