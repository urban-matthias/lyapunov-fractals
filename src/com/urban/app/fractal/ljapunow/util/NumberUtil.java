package com.urban.app.fractal.ljapunow.util;

public class NumberUtil
{
	public static boolean toBoolean(String string)
	{
		boolean number = false;
		try
		{
			number = Boolean.valueOf(string);
		}
		catch (Throwable e)
		{
		}
		return number;
	}

	public static double toDouble(String string)
	{
		double number = 0;
		try
		{
			number = Double.valueOf(string);
		}
		catch (Throwable e)
		{
		}
		return number;
	}

	public static double toDoubleNoZero(String string)
	{
		double number = 0;
		try
		{
			number = Double.valueOf(string);
		}
		catch (Throwable e)
		{
		}
		if (number == 0.0)
		{
			number = 1;
		}
		return number;
	}

	public static float toFloat(String string)
	{
		float number = 0;
		try
		{
			number = Float.valueOf(string);
		}
		catch (Throwable e)
		{
		}
		return number;
	}

	public static float toFloat(double number)
	{
		if (number > Float.MAX_VALUE)
		{
			return Float.MAX_VALUE;
		}
		else if (number < -Float.MAX_VALUE)
		{
			return -Float.MAX_VALUE;
		}
		else if (number == Double.NaN)
		{
			return 0;
		}
		return (float) number;
	}

	public static int toInt(String string)
	{
		int number = 0;
		try
		{
			number = Integer.valueOf(string);
		}
		catch (Throwable e)
		{
		}
		return number;
	}

	public static long toLong(String string)
	{
		long number = 0;
		try
		{
			number = Long.valueOf(string);
		}
		catch (Throwable e)
		{
		}
		return number;
	}

	public static double align(double number)
	{
		if (number == Double.POSITIVE_INFINITY)
		{
			return Double.MAX_VALUE;
		}
		else if (number == Double.NEGATIVE_INFINITY)
		{
			return -Double.MAX_VALUE;
		}
		else if (number == Double.NaN)
		{
			return 0;
		}
		return number;
	}

	public static float align(float number)
	{
		if (number == Float.POSITIVE_INFINITY)
		{
			return Float.MAX_VALUE;
		}
		else if (number == Float.NEGATIVE_INFINITY)
		{
			return -Float.MAX_VALUE;
		}
		else if (number == Float.NaN)
		{
			return 0;
		}
		return number;
	}

	public static float alignNoZero(float number)
	{
		number = align(number);
		if (number == 0.0)
		{
			number = 1;
		}
		return number;
	}

	public static double alignNoZero(double number)
	{
		number = align(number);
		if (number == 0.0)
		{
			number = 1;
		}
		return number;
	}
}
