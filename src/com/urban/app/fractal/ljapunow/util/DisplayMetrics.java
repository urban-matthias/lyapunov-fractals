package com.urban.app.fractal.ljapunow.util;

import java.lang.reflect.Method;

import android.content.Context;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class DisplayMetrics
{
	public static int	ROTATION_0		= Surface.ROTATION_0;
	public static int	ROTATION_90		= Surface.ROTATION_90;
	public static int	ROTATION_180	= Surface.ROTATION_180;
	public static int	ROTATION_270	= Surface.ROTATION_270;

	public static int	width			= 0;
	public static int	height			= 0;
	public static int	rotation		= 0;

	public static void gather(Context context)
	{
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		// Get display measures
		width = display.getWidth();
		if (width <= 0)
		{
			width = 1;
		}
		height = display.getHeight();
		if (height <= 0)
		{
			height = 1;
		}

		// Get screen rotation
		try
		{
			Method getRotation = display.getClass().getMethod("getRotation");
			rotation = (Integer) getRotation.invoke(display);
		}
		catch (Exception e)
		{
			rotation = display.getOrientation();
		}

		// Setup rotation values according to current base orientation
		if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && !isPortrait()) ||
			((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && isPortrait()))
		{
			// Base orientation is landscape
			ROTATION_0 = Surface.ROTATION_270;
			ROTATION_90 = Surface.ROTATION_0;
			ROTATION_180 = Surface.ROTATION_90;
			ROTATION_270 = Surface.ROTATION_180;
		}
	}

	public static boolean isPortrait()
	{
		return height > width;
	}

	public static int getPortraitHeight()
	{
		return height > width ? height : width;
	}

	public static int getPortraitWidth()
	{
		return width < height ? width : height;
	}

	public static float getPortraitX(float x, float y)
	{
		float px = x;
		if (rotation == ROTATION_90)
		{
			px = height - y - 1;
		}
		else if (rotation == ROTATION_180)
		{
			px = width - x - 1;
		}
		else if (rotation == ROTATION_270)
		{
			px = y;
		}
		return px;
	}

	public static float getPortraitY(float x, float y)
	{
		float py = y;
		if (rotation == ROTATION_90)
		{
			py = x;
		}
		else if (rotation == ROTATION_180)
		{
			py = height - y - 1;
		}
		else if (rotation == ROTATION_270)
		{
			py = width - x - 1;
		}
		return py;
	}
}
