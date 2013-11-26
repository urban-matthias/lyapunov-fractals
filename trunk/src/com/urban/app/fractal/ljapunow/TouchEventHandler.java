package com.urban.app.fractal.ljapunow;

import android.util.FloatMath;
import android.view.MotionEvent;

import com.urban.app.fractal.ljapunow.util.DisplayMetrics;
import com.urban.app.fractal.ljapunow.util.NumberUtil;
import com.urban.app.fractal.ljapunow.util.Storage;
import com.urban.app.fractal.ljapunow.view.ImageView;

public class TouchEventHandler
{
	private enum Mode
	{
		INITIAL, TOUCH, DRAG, SCALE, MULTI_TOUCH_SCALE, DOUBLE_TAP
	};

	private static final long	MIN_TOUCH_TIME				= 250;
	private static final long	MIN_MULTI_TOUCH_DISTANCE	= 10;
	private static final long	MIN_DRAG_DISTANCE			= 25;

	private ImageView			fractalView;
	private MainActivity		mainActivity;

	private long				lastEventTime				= 0;
	private float[]				startXY						= { 0, 0 };
	private float				startDistance				= 0;
	private float				eventX						= 0;
	private float				eventY						= 0;
	private Mode				mode						= Mode.INITIAL;

	double[]					aRange						= new double[2];
	double[]					bRange						= new double[2];
	double						ad, bd, acenter, bcenter, aOffset, bOffset;
	double						xdist, ydist, xOffset, yOffset;
	float						x1, x2, y1, y2, xd, yd, dist;
	int							x0, y0;

	public TouchEventHandler(MainActivity activity, ImageView fraktalView)
	{
		this.fractalView = fraktalView;
		this.mainActivity = activity;
	}

	private void gatherEventXY(MotionEvent event)
	{
		eventX = DisplayMetrics.getPortraitX(event.getX(), event.getY());
		eventY = DisplayMetrics.getPortraitY(event.getX(), event.getY());
		eventX = eventX == 0.0 ? 1 : eventX;
		eventY = eventY == 0.0 ? 1 : eventY;
	}

	public void touchDown(MotionEvent event)
	{
		gatherEventXY(event);
		if (mode == Mode.INITIAL)
		{
			mode = Mode.TOUCH;
			startXY[0] = eventX;
			startXY[1] = eventY;
			startDistance = 0;
		}
		else if (mode == Mode.DOUBLE_TAP && (event.getEventTime() - lastEventTime < MIN_TOUCH_TIME))
		{
			mode = Mode.SCALE;
			startDistance = DisplayMetrics.isPortrait() ? eventY : eventX;
			fractalView.setControl(ImageView.CONTROL_PINCH, event.getX(), event.getY());
			fractalView.invalidate();
		}
		saveLastEventTime(event);
	}

	public void multiTouchDown(MotionEvent event)
	{
		float distance = getDistance(event);
		if (distance > MIN_MULTI_TOUCH_DISTANCE)
		{
			mode = Mode.MULTI_TOUCH_SCALE;
			startDistance = distance;
			saveLastEventTime(event);
			fractalView.setOrigin(0, 0);
			fractalView.setControl(ImageView.CONTROL_PINCH, event.getX(), event.getY());
			fractalView.invalidate();
		}
	}

	public void touchMoved(MotionEvent event)
	{
		gatherEventXY(event);
		// if (mode == Mode.DRAG || (mode == Mode.TOUCH && (event.getEventTime() - lastEventTime) > MIN_TOUCH_TIME))
		if (mode == Mode.DRAG || (mode == Mode.TOUCH && getDragDistance() > MIN_DRAG_DISTANCE))
		{
			mode = Mode.DRAG;
			x0 = (int) (eventX - startXY[0]);
			y0 = (int) (eventY - startXY[1]);
			fractalView.setOrigin(x0, y0);
			fractalView.setControl(ImageView.CONTROL_DRAG, event.getX(), event.getY());
			fractalView.invalidate();
			saveLastEventTime(event);
		}
		else if (mode == Mode.SCALE)
		{
			fractalView.setScale(NumberUtil.alignNoZero(DisplayMetrics.isPortrait() ? eventY / startDistance : startDistance / eventX));
			fractalView.invalidate();
			saveLastEventTime(event);
		}
		else if (mode == Mode.MULTI_TOUCH_SCALE)
		{
			float distance = getDistance(event);
			if (distance > MIN_MULTI_TOUCH_DISTANCE)
			{
				fractalView.setScale(NumberUtil.alignNoZero(startDistance / distance));
				fractalView.invalidate();
				saveLastEventTime(event);
			}
		}
	}

	public void touchReleased(MotionEvent event)
	{
		gatherEventXY(event);
		if (mode == Mode.DRAG)
		{
			mode = Mode.INITIAL;
			translateABInterval();
			fractalView.setControl(ImageView.CONTROL_NONE, 0, 0);
			fractalView.generateFractalDragged((int) xOffset, (int) yOffset);
		}
		else if (mode == Mode.SCALE)
		{
			mode = Mode.INITIAL;
			scaleABInterval(DisplayMetrics.isPortrait() ? eventY / startDistance : startDistance / eventX);
			fractalView.setControl(ImageView.CONTROL_NONE, 0, 0);
			fractalView.generateFractalScaled();
		}
		else if (mode == Mode.TOUCH)
		{
			if (event.getEventTime() - lastEventTime < MIN_TOUCH_TIME)
			{
				mode = Mode.DOUBLE_TAP;
			}
			else
			{
				mode = Mode.INITIAL;
				mainActivity.repeatLastAction();
			}
		}
		else if (mode == Mode.DOUBLE_TAP)
		{
			mode = Mode.INITIAL;
		}
	}

	public void multiTouchReleased(MotionEvent event)
	{
		if (mode == Mode.MULTI_TOUCH_SCALE)
		{
			mode = Mode.INITIAL;
			float distance = getDistance(event);
			if (distance > MIN_MULTI_TOUCH_DISTANCE)
			{
				scaleABInterval(startDistance / distance);
				fractalView.setControl(ImageView.CONTROL_NONE, 0, 0);
				fractalView.generateFractalScaled();
			}
			else
			{
				fractalView.setControl(ImageView.CONTROL_NONE, 0, 0);
				fractalView.invalidate();
			}
		}
	}

	private void scaleABInterval(double scale)
	{
		String[] aInterval = Storage.get(FractalGenerator.STORAGE_A_INTERVAL).split(",");
		String[] bInterval = Storage.get(FractalGenerator.STORAGE_B_INTERVAL).split(",");

		aRange[0] = NumberUtil.toDouble(aInterval[0]);
		aRange[1] = NumberUtil.toDouble(aInterval[1]);
		bRange[0] = NumberUtil.toDouble(bInterval[0]);
		bRange[1] = NumberUtil.toDouble(bInterval[1]);

		ad = aRange[1] - aRange[0];
		bd = bRange[1] - bRange[0];

		acenter = aRange[0] + (ad / 2);
		bcenter = bRange[0] + (bd / 2);

		ad *= scale;
		bd *= scale;

		if (ad == 0)
			ad = (1d / 1000000d);
		if (bd == 0)
			bd = (1d / 1000000d);

		aRange[0] = NumberUtil.align(acenter - ad / 2);
		aRange[1] = NumberUtil.align(acenter + ad / 2);
		bRange[0] = NumberUtil.align(bcenter - bd / 2);
		bRange[1] = NumberUtil.align(bcenter + bd / 2);

		if (aRange[0] == 0d && aRange[1] == 0d)
		{
			aRange[0] = (scale >= 0 && scale < 1) ? -(1d / 1000000d) : -1000000d;
			aRange[1] = (scale >= 0 && scale < 1) ? (1d / 1000000d) : 1000000d;
		}
		if (bRange[0] == 0d && bRange[1] == 0d)
		{
			bRange[0] = (scale >= 0 && scale < 1) ? -(1d / 1000000d) : -1000000d;
			bRange[1] = (scale >= 0 && scale < 1) ? (1d / 1000000d) : 1000000d;
		}

		Storage.set(FractalGenerator.STORAGE_A_INTERVAL, aRange[0] + "," + aRange[1]);
		Storage.set(FractalGenerator.STORAGE_B_INTERVAL, bRange[0] + "," + bRange[1]);
	}

	private void translateABInterval()
	{
		String[] xInterval = Storage.get(FractalGenerator.STORAGE_A_INTERVAL).split(",");
		String[] yInterval = Storage.get(FractalGenerator.STORAGE_B_INTERVAL).split(",");

		aRange[0] = NumberUtil.toDouble(xInterval[0]);
		aRange[1] = NumberUtil.toDouble(xInterval[1]);
		bRange[0] = NumberUtil.toDouble(yInterval[0]);
		bRange[1] = NumberUtil.toDouble(yInterval[1]);

		boolean rotateLeft = NumberUtil.toBoolean(Storage.get(FractalGenerator.STORAGE_ROTATE_LEFT));

		xdist = eventX - startXY[0];
		ydist = eventY - startXY[1];

		xOffset = rotateLeft ? ydist : -xdist;
		yOffset = rotateLeft ? -xdist : -ydist;

		ad = aRange[1] - aRange[0];
		bd = bRange[1] - bRange[0];

		aOffset = (ad / DisplayMetrics.getPortraitWidth()) * xOffset;
		bOffset = (bd / DisplayMetrics.getPortraitHeight()) * yOffset;

		aRange[0] = NumberUtil.align(aRange[0] + aOffset);
		aRange[1] = NumberUtil.align(aRange[1] + aOffset);
		bRange[0] = NumberUtil.align(bRange[0] + bOffset);
		bRange[1] = NumberUtil.align(bRange[1] + bOffset);

		Storage.set(FractalGenerator.STORAGE_A_INTERVAL, aRange[0] + "," + aRange[1]);
		Storage.set(FractalGenerator.STORAGE_B_INTERVAL, bRange[0] + "," + bRange[1]);
	}

	public void touchCancelled(MotionEvent event)
	{
		if (mode == Mode.DRAG || mode == Mode.SCALE || mode == Mode.MULTI_TOUCH_SCALE)
		{
			fractalView.setOrigin(0, 0);
			fractalView.setScale(1);
			fractalView.setControl(ImageView.CONTROL_NONE, 0, 0);
			fractalView.invalidate();
		}
		mode = Mode.INITIAL;
	}

	private float getDistance(MotionEvent event)
	{
		// Euclidean distance
		x1 = event.getX(0);
		x2 = event.getX(1);
		y1 = event.getY(0);
		y2 = event.getY(1);
		xd = x1 - x2;
		yd = y1 - y2;
		dist = FloatMath.sqrt(xd * xd + yd * yd);
		return NumberUtil.alignNoZero(dist);
	}

	private float getDragDistance()
	{
		// Euclidean distance
		x1 = startXY[0];
		x2 = eventX;
		y1 = startXY[1];
		y2 = eventY;
		xd = x1 - x2;
		yd = y1 - y2;
		dist = FloatMath.sqrt(xd * xd + yd * yd);
		return NumberUtil.alignNoZero(dist);
	}

	private void saveLastEventTime(MotionEvent event)
	{
		lastEventTime = event.getEventTime();
	}
}
