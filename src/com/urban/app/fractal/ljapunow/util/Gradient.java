package com.urban.app.fractal.ljapunow.util;

import java.util.Iterator;
import java.util.List;

import android.graphics.Color;

public class Gradient
{
	public int[]	colors	= null;

	public Gradient()
	{
	}

	public Gradient(List<Integer> input, int gradations)
	{
		create(input, gradations);
	}

	public Gradient(int[] input, int gradations)
	{
		create(input, gradations);
	}

	public Gradient(String input, int gradations)
	{
		create(input, gradations);
	}

	public void create(List<Integer> input, int gradations)
	{
		int[] intArray = new int[input.size()];
		Iterator<Integer> it = input.iterator();
		for (int i = 0; it.hasNext(); i++)
		{
			intArray[i] = it.next();
		}
		create(intArray, gradations);
	}

	public void create(String input, int gradations)
	{
		String[] gradient = input.split(",");
		int[] colors = null;
		if (gradient.length == 0)
		{
			colors = new int[] { Color.BLACK };
		}
		else
		{
			colors = new int[gradient.length];
			for (int i = 0; i < gradient.length; i++)
			{
				colors[i] = ColorUtil.toARGBColor(gradient[i]);
			}
		}
		create(colors, gradations);
	}

	public void create(int[] input, int gradations)
	{
		if (colors == null || colors.length != gradations)
		{
			colors = new int[gradations];
		}

		int fromColor, toColor, idx, len = input.length;
		float reddiv, greendiv, bluediv, n = 0;
		float step = gradations / (float) ((len == 1 ? 2 : len) - 1);

		for (float i = 0; Math.round(i) <= gradations; i += step)
		{
			idx = Math.round(n / step);
			if (idx >= len)
			{
				idx = len - 1;
			}
			fromColor = input[idx];
			if (idx + 1 < len)
			{
				toColor = input[idx + 1];
			}
			else
			{
				toColor = fromColor;
			}

			reddiv = (Color.red(toColor) - Color.red(fromColor)) / step;
			greendiv = (Color.green(toColor) - Color.green(fromColor)) / step;
			bluediv = (Color.blue(toColor) - Color.blue(fromColor)) / step;
			for (int j = (int) n; j < (int) i; j++)
			{
				if (j < gradations)
				{
					colors[j] = Color.rgb((int) (Color.red(fromColor) + (j - (int) n) * reddiv), (int) (Color.green(fromColor) + (j - (int) n) * greendiv), (int) (Color.blue(fromColor) + (j - (int) n) * bluediv));
				}
			}
			n = i;
		}

		for (int i = (int) n; i < gradations; i++)
		{
			colors[i] = colors[i - 1];
		}
	}

	public int getColor(int i)
	{
		return colors == null ? 0 : colors[i < 0 ? 0 : i >= colors.length ? colors.length - 1 : i];
	}

	public int getCount()
	{
		return colors == null ? 0 : colors.length;
	}
}