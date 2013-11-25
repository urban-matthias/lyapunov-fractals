package com.urban.app.fractal.ljapunow;

import java.util.Arrays;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.urban.app.fractal.ljapunow.util.DisplayMetrics;
import com.urban.app.fractal.ljapunow.util.Logger;
import com.urban.app.fractal.ljapunow.util.NumberUtil;
import com.urban.app.fractal.ljapunow.util.Storage;
import com.urban.app.fractal.ljapunow.view.ImageView;

public class FractalGenerator extends AsyncTask<Void, Integer, Integer>
{
	public class State
	{
		public int					x;
		public int					y;
		public int					progress;
		public boolean[][]			gridPoints;
		public int[]				pixels;
		public float[]				exponents;
		public int					gridWidth;
		public boolean				done;
		public int					height;
		public int					width;
		public float				measuredMaxExp;
		public float				measuredMinExp;
		public FractalColoration	coloration;
	}

	public static final int		EQUATION_LOGISTIC	= 0;
	public static final int		EQUATION_NEWTON_3RD	= 1;
	public static final int		EQUATION_NEWTON		= 2;

	public static final String	STORAGE_EQUATION	= "equation";
	public static final String	STORAGE_SEQUENCE	= "sequence";
	public static final String	STORAGE_ITERATIONS	= "iterations";
	public static final String	STORAGE_A_INTERVAL	= "aInterval";
	public static final String	STORAGE_B_INTERVAL	= "bInterval";
	public static final String	STORAGE_X0			= "x0";
	public static final String	STORAGE_WARMUP		= "warmup";
	public static final String	STORAGE_ROTATE_LEFT	= "rotateLeft";
	private static final int	GRID_SIZE			= 16;

	private static final String	lock				= new String("");
	private boolean				running				= true;

	private State				state;
	private ProgressBar			progressBar;
	private ImageView			view;

	public int					equation;
	private char[]				sequence;
	private int					maxIters;
	private double[]			aRange, bRange;
	private double				x0;
	private int					warmup;
	private boolean				swap;
	private boolean				recolor;
	private double				stepa, stepb;
	private double				a0, b0;

	// Native implementation of the logistic equation
	private static native double logistic(double a, double b, double x0, int iterations, int warmup, char[] sequenceArray, int sequence_length);

	// Native implementation of the newton equation
	private static native double newton(double a, double b, double x0, int iterations, int warmup, char[] sequenceArray, int sequence_length);

	// Native implementation of the newton 3rd grade equations
	private static native double newton3rdGrade(double a, double b, double x0, int iterations, int warmup, char[] sequenceArray, int sequence_length);

	public FractalGenerator(ImageView view, ProgressBar progressBar, State savedState, boolean keepPixels)
	{
		this.progressBar = progressBar;
		this.view = view;
		this.recolor = true;

		sequence = Storage.get(STORAGE_SEQUENCE).toUpperCase().toCharArray();
		maxIters = NumberUtil.toInt(Storage.get(STORAGE_ITERATIONS));
		String[] aInterval = Storage.get(STORAGE_A_INTERVAL).split(",");
		aRange = new double[] { NumberUtil.toDouble(aInterval[0]), NumberUtil.toDouble(aInterval[1]) };
		String[] bInterval = Storage.get(STORAGE_B_INTERVAL).split(",");
		bRange = new double[] { NumberUtil.toDouble(bInterval[0]), NumberUtil.toDouble(bInterval[1]) };
		x0 = NumberUtil.toDouble(Storage.get(STORAGE_X0));
		warmup = NumberUtil.toInt(Storage.get(STORAGE_WARMUP));
		swap = NumberUtil.toBoolean(Storage.get(STORAGE_ROTATE_LEFT));
		equation = NumberUtil.toInt(Storage.get(STORAGE_EQUATION));

		initState(savedState, keepPixels);
	}

	public static void loadNativeCodeLib()
	{
		System.loadLibrary("equations");
	}

	private void initState(State savedState, boolean keepPixels)
	{
		synchronized (lock)
		{
			state = savedState;

			if (state == null || state.height != DisplayMetrics.getPortraitHeight() || state.width != DisplayMetrics.getPortraitWidth())
			{
				// no saved state or display metrics changed
				state = new State();
				state.height = DisplayMetrics.getPortraitHeight();
				state.width = DisplayMetrics.getPortraitWidth();
				state.x = 0;
				state.y = 0;
				state.measuredMaxExp = 0;
				state.measuredMinExp = 0;
				state.done = false;
				state.gridWidth = GRID_SIZE;
				state.progress = 0;
				state.gridPoints = new boolean[!swap ? state.height : state.width][!swap ? state.width : state.height];
				state.pixels = new int[state.width * state.height];
				state.exponents = new float[state.width * state.height];
				state.coloration = new FractalColoration();
			}
			else if (state.done)
			{
				// reuse previous state
				state.x = 0;
				state.y = 0;
				state.measuredMaxExp = 0;
				state.measuredMinExp = 0;
				state.done = false;
				state.gridWidth = GRID_SIZE;
				state.progress = 0;
				state.coloration.init();
				if (state.gridPoints.length != (!swap ? state.height : state.width))
				{
					state.gridPoints = new boolean[!swap ? state.height : state.width][!swap ? state.width : state.height];
				}
				else
				{
					int len = state.gridPoints.length;
					for (int i = 0; i < len; i++)
					{
						Arrays.fill(state.gridPoints[i], false);
					}
				}
				if (keepPixels == false)
				{
					Arrays.fill(state.pixels, 0);
					Arrays.fill(state.exponents, 0);
				}
			}
		}
	}

	public void stop(boolean recolor)
	{
		this.recolor = recolor;
		state.done = true;
	}

	public State getState()
	{
		return state;
	}

	public boolean isRunning()
	{
		return running;
	}

	@Override
	protected void onPreExecute()
	{
		progressBar.setProgress(state.progress);
		progressBar.setMax(state.height * state.width);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.bringToFront();

		view.setPixels(state.pixels, state.exponents, state.measuredMinExp, state.measuredMaxExp, state.width, state.height, false);
	}

	@Override
	protected void onPostExecute(Integer result)
	{
		try
		{
			progressBar.setVisibility(View.INVISIBLE);
			if (recolor)
			{
				boolean recalcColors = state.coloration.histogramEqualizationEnabled() || state.coloration.automaticExponentIntervalEnabled();
				view.setPixels(state.pixels, state.exponents, state.measuredMinExp, state.measuredMaxExp, state.width, state.height, recalcColors);
			}
			System.gc();
		}
		finally
		{
			running = false;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		progressBar.setVisibility(View.VISIBLE);
		progressBar.incrementProgressBy(values[0]);

		view.setPixels(state.pixels, state.exponents, state.measuredMinExp, state.measuredMaxExp, state.width, state.height, false);
	}

	@Override
	protected Integer doInBackground(Void... arg0)
	{
		synchronized (lock)
		{
			try
			{
				int w = !swap ? state.width : state.height;
				int h = !swap ? state.height : state.width;

				double ad = aRange[1] - aRange[0];
				double bd = bRange[1] - bRange[0];

				double acenter = aRange[0] + (ad / 2);
				double bcenter = bRange[0] + (bd / 2);

				ad *= (double) w / (double) state.width;
				bd *= (double) h / (double) state.height;

				a0 = acenter - (ad / 2);
				b0 = bcenter - (bd / 2);

				stepa = ad / w;
				stepb = bd / h;

				int progress = 0, progressUpdateInterval = 100;
				float exponent = 0;
				double a, b;
				long lastTime = 0, currTime;

				for (; state.gridWidth > 0; state.gridWidth /= 2)
				{
					for (; state.y < h && !state.done; state.y += state.gridWidth)
					{
						for (; state.x < w && !state.done; state.x += state.gridWidth)
						{
							if (state.gridPoints[state.y][state.x] == false)
							{
								state.gridPoints[state.y][state.x] = true;

								a = a0 + (stepa * state.x);
								b = b0 + (stepb * state.y);

								switch (equation)
								{
								case EQUATION_LOGISTIC:
									exponent = NumberUtil.toFloat(logistic(a, b, x0, maxIters, warmup, sequence, sequence.length));
									break;
								case EQUATION_NEWTON_3RD:
									exponent = NumberUtil.toFloat(newton3rdGrade(a, b, x0, maxIters, warmup, sequence, sequence.length));
									break;
								case EQUATION_NEWTON:
									exponent = NumberUtil.toFloat(newton(a, b, x0, maxIters, sequence.length, sequence, sequence.length));
									break;
								}

								if (exponent > state.measuredMaxExp)
								{
									state.measuredMaxExp = exponent;
								}
								if (exponent < state.measuredMinExp)
								{
									state.measuredMinExp = exponent;
								}

								setPixels(!swap ? state.x : state.y, !swap ? state.y : (state.height - state.x - 1), exponent, state.gridWidth);

								progress++;
								state.progress++;
							}
						}
						state.x = 0;

						currTime = System.currentTimeMillis();
						if ((currTime - lastTime) / progressUpdateInterval > 1)
						{
							lastTime = currTime;
							this.publishProgress(progress);
							progress = 0;
						}
					}
					state.y = 0;
					progressUpdateInterval += 100;
				}
			}
			catch (Throwable e)
			{
				Logger.error(view, e);
			}
			finally
			{
				state.done = true;
			}
		}
		return 0;
	}

	private void setPixels(int x0, int y0, float exponent, int gridWidth)
	{
		int color = state.coloration.calculateColor(exponent);
		int h = y0 + gridWidth >= state.height ? state.height : y0 + gridWidth;
		int w = x0 + gridWidth >= state.width ? state.width : x0 + gridWidth;
		int offset;
		for (int y = y0; y < h; y++)
		{
			offset = y * state.width;
			for (int x = x0; x < w; x++)
			{
				state.pixels[offset + x] = color;
				state.exponents[offset + x] = exponent;
			}
		}
	}
}
