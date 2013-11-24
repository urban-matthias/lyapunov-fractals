package com.urban.app.fractal.ljapunow.util;

import android.graphics.Color;

public class ColorUtil
{
	public static String toARGBString(int color)
	{
		// format: #AARRGGBB
		String alpha = Integer.toHexString(Color.alpha(color));
		String red = Integer.toHexString(Color.red(color));
		String green = Integer.toHexString(Color.green(color));
		String blue = Integer.toHexString(Color.blue(color));
		if (alpha.length() == 1)
			alpha = "0" + alpha;
		if (red.length() == 1)
			red = "0" + red;
		if (green.length() == 1)
			green = "0" + green;
		if (blue.length() == 1)
			blue = "0" + blue;
		return "#" + alpha + red + green + blue;
	}

	public static String toRGBString(int color)
	{
		// format: #RRGGBB
		String red = Integer.toHexString(Color.red(color));
		String green = Integer.toHexString(Color.green(color));
		String blue = Integer.toHexString(Color.blue(color));
		if (red.length() == 1)
			red = "0" + red;
		if (green.length() == 1)
			green = "0" + green;
		if (blue.length() == 1)
			blue = "0" + blue;
		return "#" + red + green + blue;
	}

	public static int toARGBColor(String colorString)
	{
		return Color.parseColor(colorString);
	}
	
	public static int saturate(int color, float saturation)
	{
		float[] hsv = new float[3];
		Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
		hsv[1] = saturation;
		return Color.HSVToColor(Color.alpha(color), hsv);
	}

	public static int saturateRelative(int color, float saturationOffset)
	{
		float[] hsv = new float[3];
		Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
		hsv[1] += saturationOffset;
		if (hsv[1] > 1.0f)
		{
			hsv[1] = 1.0f;
		}
		else if (hsv[1] < 0.0f)
		{
			hsv[1] = 0.0f;
		}
		return Color.HSVToColor(Color.alpha(color), hsv);
	}

	public static int brighten(int color, float brightness)
	{
		float[] hsv = new float[3];
		Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
		hsv[2] = brightness;
		return Color.HSVToColor(Color.alpha(color), hsv);
	}

	public static int brightenRelative(int color, float brightnessOffset)
	{
		float[] hsv = new float[3];
		Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
		hsv[2] += brightnessOffset;
		if (hsv[2] > 1.0f)
		{
			hsv[2] = 1.0f;
		}
		else if (hsv[2] < 0.0f)
		{
			hsv[2] = 0.0f;
		}
		return Color.HSVToColor(Color.alpha(color), hsv);
	}

	public static int setAlpha(int color, int value)
	{
		return Color.argb(value, Color.red(color), Color.green(color), Color.blue(color));
	}

	public static int setRed(int color, int value)
	{
		return Color.argb(Color.alpha(color), value, Color.green(color), Color.blue(color));
	}

	public static int setGreen(int color, int value)
	{
		return Color.argb(Color.alpha(color), Color.red(color), value, Color.blue(color));
	}

	public static int setBlue(int color, int value)
	{
		return Color.argb(Color.alpha(color), Color.red(color), Color.green(color), value);
	}
}
