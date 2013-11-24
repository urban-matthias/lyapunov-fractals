package com.urban.app.fractal.ljapunow.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.urban.app.fractal.ljapunow.R;
import com.urban.app.fractal.ljapunow.activity.GradientEditor;
import com.urban.app.fractal.ljapunow.dialog.SelectionDialog;
import com.urban.app.fractal.ljapunow.util.ColorUtil;
import com.urban.app.fractal.ljapunow.util.Gradient;
import com.urban.app.fractal.ljapunow.util.Storage;

public class GradientView extends View
{
	private final int[]	MAIN_COLOR_GRADIENT	= new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };

	private static enum GradientOp
	{
		ROTATE, SATURATE, BRIGHTEN
	};

	public static final int		MAIN_COLORS			= 0;
	public static final int		BRIGHTNESS			= 1;
	public static final int		SATURATION			= 2;
	public static final int		PREVIEW_GRADIENT	= 3;
	public static final int		CURRENT_GRADIENT	= 4;

	private GradientEditor		gradientEditor		= null;
	private int					kind				= -1;
	private int					color				= Color.WHITE;
	private String				colorlist			= null;
	private Gradient			gradient			= new Gradient();
	private boolean				finishing			= false;
	private Paint				paint				= new Paint();
	private GestureDetector		gestures			= new GestureDetector(new SimpleGestures());

	private static GradientOp	gradientOp			= GradientOp.ROTATE;

	public GradientView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public GradientView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public GradientView(Context context)
	{
		super(context);
	}

	public GradientView(GradientEditor gradientEditor, int kind)
	{
		super(gradientEditor);
		this.gradientEditor = gradientEditor;
		this.kind = kind;
	}

	public void setGradientEditor(GradientEditor gradientEditor)
	{
		this.gradientEditor = gradientEditor;
	}

	public void setKind(int kind)
	{
		this.kind = kind;
	}

	public void setColor(int color)
	{
		this.color = color;
		invalidate();
	}

	public void setColorList(String colorlist)
	{
		this.colorlist = colorlist;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		int width = getWidth(), height = getHeight();

		switch (kind)
		{
		case MAIN_COLORS:
			if (gradient.getCount() == 0 || gradient.getCount() != width)
			{
				gradient.create(MAIN_COLOR_GRADIENT, width);
			}
			break;
		case SATURATION:
			int saturated = ColorUtil.saturate(color, 1);
			int desaturated = ColorUtil.saturate(color, 0);
			gradient.create(new int[] { saturated, desaturated }, width);
			break;
		case BRIGHTNESS:
			int brightened = ColorUtil.brighten(color, 1);
			int debrightened = ColorUtil.brighten(color, 0);
			gradient.create(new int[] { debrightened, brightened }, width);
			break;
		case PREVIEW_GRADIENT:
			gradient.create(colorlist, width);
			break;
		case CURRENT_GRADIENT:
			gradient.create(gradientEditor.getCurrentGradient(), width);
			break;
		}

		paint.setAntiAlias(false);
		paint.setDither(true);
		paint.setStrokeWidth(0);
		paint.setStyle(Style.FILL);

		// draw the gradient
		for (int x = 0; x < width; x++)
		{
			paint.setColor(gradient.getColor(x));
			canvas.drawLine(x, 0, x, height - 1, paint);
		}

		if (kind == CURRENT_GRADIENT)
		{
			paint.setTextSize(36);
			paint.setAntiAlias(true);

			paint.setColor(!finishing ? Color.argb(128, 0, 0, 0) : Color.argb(255, 0, 0, 0));
			canvas.drawText("OK", width / 2 - 25, height / 2 + 12, paint);

			paint.setColor(!finishing ? Color.argb(128, 255, 255, 255) : Color.argb(255, 255, 255, 255));
			canvas.drawText("OK", width / 2 - 24, height / 2 + 13, paint);
		}
	}

	private class SimpleGestures extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onSingleTapUp(MotionEvent e)
		{
			finishing = true;
			invalidate();
			String storageID = gradientEditor.getIntent().getStringExtra(GradientEditor.GRADIENT);
			Storage.set(storageID, gradientEditor.serializeCurrentGradient());
			gradientEditor.getIntent().putExtra(GradientEditor.GRADIENT, storageID);
			gradientEditor.setResult(GradientEditor.RESULT_OK, gradientEditor.getIntent());
			gradientEditor.finish();
			return true;
		}

		@Override
		public boolean onDown(MotionEvent event)
		{
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e)
		{
			final String[] gradient_ops = getContext().getResources().getStringArray(R.array.gradient_ops);
			SelectionDialog dialog = new SelectionDialog(getContext(), R.string.dialog_gradient_ops, gradient_ops, false)
			{
				@Override
				public boolean onSelection(int which)
				{
					switch (which)
					{
					case 0:
						gradientOp = GradientOp.ROTATE;
						break;
					case 1:
						gradientOp = GradientOp.SATURATE;
						break;
					case 2:
						gradientOp = GradientOp.BRIGHTEN;
						break;
					}
					return true;
				}
			};
			dialog.show();
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			switch (gradientOp)
			{
			case ROTATE:
				gradientEditor.rotateGradient((int) ((90f / getWidth()) * distanceX));
				break;
			case SATURATE:
				gradientEditor.saturateGradient((0.33f / getWidth()) * -distanceX);
				break;
			case BRIGHTEN:
				gradientEditor.brightenGradient((0.33f / getWidth()) * -distanceX);
				break;
			}
			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX();
		switch (kind)
		{
		case MAIN_COLORS:
			int color = gradient.getColor((int) x);
			gradientEditor.onColorChanged(Color.red(color), Color.green(color), Color.blue(color), -1, -1);
			return true;
		case SATURATION:
			gradientEditor.onColorChanged(-1, -1, -1, 1.0f - (1.0f / getWidth()) * x, -1);
			return true;
		case BRIGHTNESS:
			gradientEditor.onColorChanged(-1, -1, -1, -1, (1.0f / getWidth()) * x);
			return true;
		case CURRENT_GRADIENT:
			gestures.setIsLongpressEnabled(true);
			return gestures.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}
}
