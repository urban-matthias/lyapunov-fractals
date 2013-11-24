package com.urban.app.fractal.ljapunow.view;

import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask.Status;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.urban.app.fractal.ljapunow.FractalColoration;
import com.urban.app.fractal.ljapunow.FractalGenerator;
import com.urban.app.fractal.ljapunow.R;
import com.urban.app.fractal.ljapunow.util.DisplayMetrics;
import com.urban.app.fractal.ljapunow.util.Logger;
import com.urban.app.fractal.ljapunow.util.NumberUtil;
import com.urban.app.fractal.ljapunow.util.Storage;

public class ImageView extends View
{
	public static final int			CONTROL_NONE	= 0;
	public static final int			CONTROL_DRAG	= 1;
	public static final int			CONTROL_PINCH	= 2;

	private int						control			= CONTROL_NONE;
	private float					controlX		= 0;
	private float					controlY		= 0;
	private Bitmap					dragControl		= null;
	private Bitmap					pinchControl	= null;

	private static final String		lock			= new String("");
	private static Bitmap			image			= null;
	private FractalGenerator		generator		= null;
	private FractalGenerator.State	reusedState		= null;
	private FractalColoration		coloration		= null;
	private int						x0				= 0;
	private int						y0				= 0;
	private float					scale			= 1;
	private Paint					paint			= null;
	private Paint					whiteTextPaint	= null;
	private Paint					blackTextPaint	= null;
	private boolean					debug			= false;
	private ProgressBar				progressBar		= null;
	private float[]					exponents		= null;
	private int[]					colors			= null;
	private float					measuredMinExp	= 0;
	private float					measuredMaxExp	= 0;

	public ImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setFocusable(true);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);

		blackTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		blackTextPaint.setStyle(Paint.Style.FILL);
		blackTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		blackTextPaint.setTextSize(12);
		blackTextPaint.setColor(Color.BLACK);

		whiteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		whiteTextPaint.setStyle(Paint.Style.FILL);
		whiteTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		whiteTextPaint.setTextSize(12);
		whiteTextPaint.setColor(Color.WHITE);

		dragControl = BitmapFactory.decodeResource(getResources(), R.drawable.drag);
		pinchControl = BitmapFactory.decodeResource(getResources(), R.drawable.pinch);
	}

	public void setControl(int control, float x, float y)
	{
		this.control = control;
		this.controlX = x;
		this.controlY = y;
	}

	public FractalGenerator.State getGeneratorState()
	{
		return generator != null ? generator.getState() : reusedState != null ? reusedState : null;
	}

	public void setProgressBar(ProgressBar progressBar)
	{
		this.progressBar = progressBar;
	}

	public void setOrigin(int x0, int y0)
	{
		this.x0 = x0;
		this.y0 = y0;
	}

	public void setScale(float scale)
	{
		this.scale = scale;
	}

	public void setDebug(boolean debug)
	{
		this.debug = debug;
	}

	public boolean getDebug()
	{
		return debug;
	}

	public void setPixels(int[] pixels, float[] exponents, float measuredMinExp, float measuredMaxExp, int width, int height)
	{
		try
		{
			synchronized (lock)
			{
				this.exponents = exponents;
				this.colors = pixels;
				this.measuredMaxExp = NumberUtil.align(measuredMaxExp);
				this.measuredMinExp = NumberUtil.align(measuredMinExp);

				if (image == null || image.getWidth() != width || image.getHeight() != height)
				{
					if (image != null)
					{
						image.recycle();
					}
					image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				}
				image.setPixels(colors, 0, width, 0, 0, width, height);
			}
			invalidate();
		}
		catch (Throwable e)
		{
			Logger.error(this, e);
		}
	}

	public void recalcColors(boolean force)
	{
		try
		{
			synchronized (lock)
			{
				if (exponents != null && image != null && (force || !isGeneratingFractal()))
				{
					if (coloration == null)
					{
						coloration = new FractalColoration();
					}
					else
					{
						coloration.init();
					}
					if (coloration.histogramEqualizationEnabled())
					{
						coloration.histogramEqualization(exponents, measuredMinExp, measuredMaxExp, colors);
					}
					else if (coloration.automaticExponentIntervalEnabled())
					{
						float minExp = measuredMinExp < -10 ? -10 : measuredMinExp;
						float maxExp = measuredMaxExp > 10 ? 10 : measuredMaxExp;
						for (int i = 0; i < exponents.length; i++)
						{
							colors[i] = coloration.calculateColor(exponents[i], minExp, maxExp);
						}
					}
					else
					{
						for (int i = 0; i < exponents.length; i++)
						{
							colors[i] = coloration.calculateColor(exponents[i]);
						}
					}
					image.setPixels(colors, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
					invalidate();
				}
			}
		}
		catch (Throwable e)
		{
			Logger.error(this, e);
		}
	}

	public void generateFractal()
	{
		generateFractal(null);
	}

	public void generateFractal(int xOffset, int yOffset)
	{
		generateFractal(null, xOffset, yOffset);
	}

	public void generateFractal(FractalGenerator.State savedState)
	{
		generateFractal(savedState, 0, 0);
	}

	public void generateFractal(FractalGenerator.State savedState, int xOffset, int yOffset)
	{
		try
		{
			synchronized (lock)
			{
				setOrigin(0, 0);
				setScale(1);
				stopGenerator(false);
				startGenerator(savedState, xOffset, yOffset);
			}
		}
		catch (Throwable e)
		{
			Logger.error(this, e);
		}
	}

	private void startGenerator(FractalGenerator.State savedState, int xOffset, int yOffset)
	{
		synchronized (lock)
		{
			generator = new FractalGenerator(this, progressBar, savedState != null ? savedState : reusedState);
			reusedState = generator.getState();
			generator.execute();
		}
	}

	public void stopGenerator(boolean recolor)
	{
		try
		{
			synchronized (lock)
			{
				if (generator != null)
				{
					generator.stop(recolor);
				}
			}
		}
		catch (Throwable e)
		{
			Logger.error(this, e);
		}
	}

	public boolean isGeneratingFractal()
	{
		return generator != null && !generator.getStatus().equals(Status.FINISHED) && !generator.isCancelled();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		try
		{
			synchronized (lock)
			{
				if (image != null)
				{
					canvas.save();

					canvas.translate(DisplayMetrics.width / 2, DisplayMetrics.height / 2);

					int rotationAngle = getRotationAngle();
					if (rotationAngle != 0)
					{
						canvas.rotate(rotationAngle);
					}

					if (scale != 1)
					{
						canvas.scale(1 / scale, 1 / scale);
					}

					int xoffset = image.getWidth() / 2;
					int yoffset = image.getHeight() / 2;
					canvas.drawBitmap(image, x0 - xoffset, y0 - yoffset, paint);

					canvas.restore();

					if (debug)
					{
						drawDebugInfo(canvas);
					}

					switch (control)
					{
					case CONTROL_DRAG:
						canvas.drawBitmap(dragControl, controlX - dragControl.getWidth() / 2, controlY - dragControl.getHeight() / 2, paint);
						break;
					case CONTROL_PINCH:
						canvas.drawBitmap(pinchControl, controlX - pinchControl.getWidth() / 2, controlY - pinchControl.getHeight() / 2, paint);
						break;
					}
				}
			}
		}
		catch (Throwable e)
		{
			Logger.error(this, e);
		}
	}

	private int getRotationAngle()
	{
		if (DisplayMetrics.rotation == DisplayMetrics.ROTATION_90)
		{
			return -90;
		}
		else if (DisplayMetrics.rotation == DisplayMetrics.ROTATION_180)
		{
			return -180;
		}
		else if (DisplayMetrics.rotation == DisplayMetrics.ROTATION_270)
		{
			return -270;
		}
		return 0;
	}

	private void drawDebugInfo(Canvas canvas)
	{
		int line = 0;
		drawDebugLine(canvas, FractalGenerator.STORAGE_SEQUENCE, Storage.get(FractalGenerator.STORAGE_SEQUENCE), ++line);
		drawDebugLine(canvas, FractalGenerator.STORAGE_A_INTERVAL, Storage.get(FractalGenerator.STORAGE_A_INTERVAL), ++line);
		drawDebugLine(canvas, FractalGenerator.STORAGE_B_INTERVAL, Storage.get(FractalGenerator.STORAGE_B_INTERVAL), ++line);
		drawDebugLine(canvas, FractalGenerator.STORAGE_ITERATIONS, Storage.get(FractalGenerator.STORAGE_ITERATIONS), ++line);
		drawDebugLine(canvas, FractalGenerator.STORAGE_WARMUP, Storage.get(FractalGenerator.STORAGE_WARMUP), ++line);
		drawDebugLine(canvas, FractalGenerator.STORAGE_X0, Storage.get(FractalGenerator.STORAGE_X0), ++line);
		drawDebugLine(canvas, FractalColoration.STORAGE_STABILITY_GRADIENT, Storage.get(FractalColoration.STORAGE_STABILITY_GRADIENT), ++line);
		drawDebugLine(canvas, FractalColoration.STORAGE_CHAOS_GRADIENT, Storage.get(FractalColoration.STORAGE_CHAOS_GRADIENT), ++line);
		drawDebugLine(canvas, FractalColoration.STORAGE_CYCLIC_COLORATION, Storage.get(FractalColoration.STORAGE_CYCLIC_COLORATION), ++line);
		drawDebugLine(canvas, FractalColoration.STORAGE_ONE_GRADIENT, Storage.get(FractalColoration.STORAGE_ONE_GRADIENT), ++line);
		drawDebugLine(canvas, FractalGenerator.STORAGE_ROTATE_LEFT, Storage.get(FractalGenerator.STORAGE_ROTATE_LEFT), ++line);
		drawDebugLine(canvas, FractalColoration.STORAGE_EXPONENT_INTERVAL, Storage.get(FractalColoration.STORAGE_EXPONENT_INTERVAL), ++line);
		drawDebugLine(canvas, "measuredMinExponent", "" + measuredMinExp, ++line);
		drawDebugLine(canvas, "measuredMaxExponent", "" + measuredMaxExp, ++line);
		drawDebugLine(canvas, "displayWidth", "" + DisplayMetrics.width, ++line);
		drawDebugLine(canvas, "displayHeigth", "" + DisplayMetrics.height, ++line);
		drawDebugLine(canvas, "displayRotation", "" + DisplayMetrics.rotation, ++line);
		Runtime r = Runtime.getRuntime();
		drawDebugLine(canvas, "freeMemory", ((r.maxMemory() - (r.totalMemory() - r.freeMemory())) / 1024) + "kb", ++line);
	}

	private void drawDebugLine(Canvas canvas, String info, String value, int line)
	{
		int y = 15 * line;
		canvas.drawText(info + "(" + value + ")", 4, y, blackTextPaint);
		canvas.drawText(info + "(" + value + ")", 6, y, blackTextPaint);
		canvas.drawText(info + "(" + value + ")", 5, y - 1, blackTextPaint);
		canvas.drawText(info + "(" + value + ")", 5, y + 1, blackTextPaint);
		canvas.drawText(info + "(" + value + ")", 5, y, whiteTextPaint);
	}

	public void saveFractalAsImage(String filePath)
	{
		try
		{
			synchronized (lock)
			{
				if (image != null)
				{
					// Save image in the orientation currently shown
					int rotationAngle = getRotationAngle();
					if (rotationAngle != 0)
					{
						Matrix matrix = new Matrix();
						matrix.postRotate(rotationAngle);
						Bitmap orientedImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
						orientedImage.compress(CompressFormat.PNG, 100, new FileOutputStream(filePath));
						orientedImage.recycle();
					}
					else
					{
						image.compress(CompressFormat.PNG, 100, new FileOutputStream(filePath));
					}
				}
			}
		}
		catch (Throwable e)
		{
			Logger.error(this, e);
		}
	}
}
