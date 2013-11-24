package com.urban.app.fractal.ljapunow;

import com.urban.app.fractal.ljapunow.util.Gradient;
import com.urban.app.fractal.ljapunow.util.NumberUtil;
import com.urban.app.fractal.ljapunow.util.Storage;

public class FractalColoration
{
	public static final String	STORAGE_EXPONENT_INTERVAL	= "exponentInterval";
	public static final String	STORAGE_STABILITY_GRADIENT	= "stabilityGradient";
	public static final String	STORAGE_CHAOS_GRADIENT		= "chaosGradient";
	public static final String	STORAGE_GRADATIONS			= "gradations";
	public static final String	STORAGE_ONE_GRADIENT		= "oneGradient";
	public static final String	STORAGE_CYCLIC_COLORATION	= "cyclicColoration";
	public static final String	STORAGE_HISTOGRAM			= "histogramEqualization";
	public static final String	STORAGE_AUTO_EXP_INTERVAL	= "autoExponentInterval";

	private float				maxExponent, minExponent;
	private float				cyclicColoration;
	private boolean				oneGradient;
	private boolean				histogramEqual;
	private boolean				autoExponentInterval;
	private int					gradations;
	private Gradient			stabilityGradient			= new Gradient();
	private Gradient			chaosGradient				= new Gradient();

	public FractalColoration()
	{
		init();
	}

	public void init()
	{
		String[] expInterval = Storage.get(STORAGE_EXPONENT_INTERVAL).split(",");
		minExponent = NumberUtil.toFloat(expInterval[0]);
		maxExponent = NumberUtil.toFloat(expInterval[1]);
		cyclicColoration = NumberUtil.toFloat(Storage.get(STORAGE_CYCLIC_COLORATION));
		oneGradient = NumberUtil.toBoolean(Storage.get(STORAGE_ONE_GRADIENT));
		histogramEqual = NumberUtil.toBoolean(Storage.get(STORAGE_HISTOGRAM));
		gradations = NumberUtil.toInt(Storage.get(STORAGE_GRADATIONS));
		stabilityGradient.create(Storage.get(STORAGE_STABILITY_GRADIENT), gradations);
		chaosGradient.create(Storage.get(STORAGE_CHAOS_GRADIENT), gradations);
		autoExponentInterval = NumberUtil.toBoolean(Storage.get(STORAGE_AUTO_EXP_INTERVAL));
	}

	public boolean histogramEqualizationEnabled()
	{
		return histogramEqual;
	}

	public boolean automaticExponentIntervalEnabled()
	{
		return autoExponentInterval;
	}

	public int calculateColor(double exponent, float minExponent, float maxExponent)
	{
		if (autoExponentInterval)
		{
			this.minExponent = minExponent;
			this.maxExponent = maxExponent;
		}
		return calculateColor(exponent);
	}

	public int calculateColor(double exponent)
	{
		Gradient gradient = null;
		double range = 0;

		if (oneGradient)
		{
			// One gradient for whole exponent interval
			range = maxExponent - minExponent;
			gradient = stabilityGradient;
		}
		else
		{
			// Separate gradients for -exp and exp
			if (exponent < 0)
			{
				range = minExponent;
				gradient = stabilityGradient;
			}
			else
			{
				range = maxExponent;
				gradient = chaosGradient;
			}
		}

		int index;
		if (cyclicColoration != 0d)
		{
			index = (int) (((exponent == 0.0 ? 1.0 : exponent) * cyclicColoration) % gradient.getCount());
		}
		else
		{
			exponent = (exponent > maxExponent) ? maxExponent : (exponent < minExponent) ? minExponent : exponent;
			index = (int) ((exponent * gradient.getCount()) / range);
		}

		return gradient.getColor(index < 0 ? -index : index);
	}

	public int[] histogramEqualization(float[] exponents, double minExp, double maxExp, int[] pixels)
	{
		int expCount = exponents.length, negExpCount = 1, posExpCount = 1, index;
		float minNegExp = 0, maxNegExp = 0, minPosExp = 0, maxPosExp = 0, exp;
		for (int i = 0; i < expCount; i++)
		{
			exp = exponents[i];
			if (exp < 0)
			{
				negExpCount++;
				if (exp < minNegExp)
				{
					minNegExp = exp;
				}
				else if (exp > maxNegExp)
				{
					maxNegExp = exp;
				}
			}
			else if (exp >= 0)
			{
				posExpCount++;
				if (exp < minPosExp)
				{
					minPosExp = exp;
				}
				else if (exp > maxPosExp)
				{
					maxPosExp = exp;
				}
			}
		}
		if (minNegExp < minExponent)
		{
			minNegExp = minExponent;
		}
		if (maxPosExp < maxExponent)
		{
			maxPosExp = maxExponent;
		}

		float posExpRange = maxPosExp - minPosExp;
		float negExpRange = maxNegExp - minNegExp;
		if (posExpRange == 0)
		{
			posExpRange = 1;
		}
		if (negExpRange == 0)
		{
			negExpRange = 1;
		}

		int posHistoSize = 1 + (int) (Math.log(posExpCount) / Math.log(2));
		int negHistoSize = 1 + (int) (Math.log(negExpCount) / Math.log(2));
		float posHistoScale = posHistoSize / posExpRange;
		float negHistoScale = negHistoSize / negExpRange;
		int[] posHistoClasses = new int[posHistoSize];
		int[] negHistoClasses = new int[negHistoSize];

		for (int i = 0; i < expCount; i++)
		{
			exp = exponents[i];
			if (exp < 0)
			{
				if (exp < minExponent)
				{
					exp = minExponent;
				}
				index = (int) ((exp - minNegExp) * negHistoScale);
				index = index < 0 ? 0 : index >= negHistoSize ? negHistoSize - 1 : index;
				pixels[i] = index;
				negHistoClasses[index]++;
			}
			else
			{
				if (exp > maxExponent)
				{
					exp = maxExponent;
				}
				index = (int) ((exp - minPosExp) * posHistoScale);
				index = index < 0 ? 0 : index >= posHistoSize ? posHistoSize - 1 : index;
				pixels[i] = index;
				posHistoClasses[index]++;
			}
		}

		class TransParam
		{
			public double	factor, offset;
		}

		TransParam[] posTransParams = new TransParam[posHistoSize];
		TransParam[] negTransParams = new TransParam[negHistoSize];
		TransParam tp;

		Gradient negGradient = stabilityGradient, posGradient = oneGradient ? stabilityGradient : chaosGradient;
		int negGradientLen = negGradient.getCount(), posGradientLen = posGradient.getCount();
		float mint, minv, maxt, maxv;

		for (int i = 0, step = 0; i < posHistoSize; i++)
		{
			minv = (posExpRange * (float) i / posHistoSize) + minPosExp;
			maxv = (posExpRange * (float) (i + 1) / posHistoSize) + minPosExp;
			mint = step;
			step += (posGradientLen * (float) (posHistoClasses[i]) / (float) (posExpCount));
			maxt = step;
			tp = new TransParam();
			tp.factor = (maxt - mint) / (maxv - minv);
			tp.offset = -minv * tp.factor + mint;
			posTransParams[i] = tp;
		}
		for (int i = 0, step = 0; i < negHistoSize; i++)
		{
			minv = (negExpRange * (float) i / negHistoSize) + minNegExp;
			maxv = (negExpRange * (float) (i + 1) / negHistoSize) + minNegExp;
			mint = step;
			step += (negGradientLen * (float) (negHistoClasses[i]) / (float) (negExpCount));
			maxt = step;
			tp = new TransParam();
			tp.factor = (maxt - mint) / (maxv - minv);
			tp.offset = -minv * tp.factor + mint;
			negTransParams[i] = tp;
		}

		for (int i = 0; i < expCount; i++)
		{
			exp = exponents[i];
			if (exp < 0)
			{
				if (exp < minExponent)
				{
					exp = minExponent;
				}
				index = pixels[i];
				index = index < 0 ? 0 : index >= negTransParams.length ? negTransParams.length - 1 : index;
				index = negGradientLen - 1 - (int) (exp * negTransParams[index].factor + negTransParams[index].offset);
				index = index < 0 ? 0 : index >= negGradientLen ? negGradientLen - 1 : index;
				pixels[i] = negGradient.colors[index];
			}
			else
			{
				if (exp > maxExponent)
				{
					exp = maxExponent;
				}
				index = pixels[i];
				index = index < 0 ? 0 : index >= posTransParams.length ? posTransParams.length - 1 : index;
				index = (int) (exp * posTransParams[index].factor + posTransParams[index].offset);
				index = index < 0 ? 0 : index >= posGradientLen ? posGradientLen - 1 : index;
				pixels[i] = posGradient.colors[index];
			}
		}

		return pixels;
	}
}
