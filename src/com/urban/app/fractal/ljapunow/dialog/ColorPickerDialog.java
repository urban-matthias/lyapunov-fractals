package com.urban.app.fractal.ljapunow.dialog;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.urban.app.fractal.ljapunow.R;
import com.urban.app.fractal.ljapunow.util.ColorUtil;
import com.urban.app.fractal.ljapunow.util.NumberUtil;
import com.urban.app.fractal.ljapunow.util.Storage;

public class ColorPickerDialog extends Dialog
{
	public interface OnColorChangedListener
	{
		void colorChanged(int color);
	}

	private int		mInitialColor;
	private String	mStorageID;
	private Context	mContext;

	private static class ColorPickerView extends View
	{
		private Paint					mColorPaint;
		private Paint					mSaturationPaint;
		private Paint					mBrightnessPaint;
		private Paint					mCenterPaint;
		private Paint					mTextPaint;
		private Paint					mCenterTextPaint;
		private final int[]				mColors;
		private final int[]				mSaturation;
		private OnColorChangedListener	mListener;
		private boolean					mTrackingCenter;
		private boolean					mTrackingSaturation;
		private boolean					mTrackingBrightness;
		private boolean					mTrackingColor;
		private boolean					mHighlightCenter;

		private static final int		CENTER_X			= 150;
		private static final int		CENTER_Y			= 150;
		private static final int		OFFSET_Y			= 60;
		private static final int		STROKE_WIDTH		= 36;
		private static final int		CENTER_RADIUS		= 36;
		private static final int		CENTER_OUTER_RADIUS	= CENTER_RADIUS + 5;
		private static final int		SAT_BR_RADIUS		= 77;
		private static final int		SAT_BR_OUTER_RADIUS	= SAT_BR_RADIUS + STROKE_WIDTH / 2;
		private static final int		SAT_BR_INNER_RADIUS	= SAT_BR_RADIUS - STROKE_WIDTH / 2;
		private static final int		COLORS_RADIUS		= CENTER_X - STROKE_WIDTH / 2;
		private static final int		COLORS_OUTER_RADIUS	= COLORS_RADIUS + STROKE_WIDTH / 2;
		private static final int		COLORS_INNER_RADIUS	= COLORS_RADIUS - STROKE_WIDTH / 2;
		private static final float		PI					= 3.1415926f;

		private class ColorInputDialog extends Builder implements DialogInterface.OnClickListener
		{
			public static final int	RED		= 0;
			public static final int	GREEN	= 1;
			public static final int	BLUE	= 2;

			private EditText		mColorValue;
			private ColorPickerView	mView;
			private int				mPortion;

			public ColorInputDialog(ColorPickerView view, int value, int portion)
			{
				super(view.getContext());
				mView = view;
				mPortion = portion;
				mColorValue = new EditText(view.getContext());
				mColorValue.setText("" + value);
				mColorValue.setInputType(InputType.TYPE_CLASS_NUMBER);
				mColorValue.setSingleLine();
				setView(mColorValue);
				if (portion == RED)
					setTitle(R.string.dialog_enter_red);
				else if (portion == GREEN)
					setTitle(R.string.dialog_enter_green);
				else if (portion == BLUE)
					setTitle(R.string.dialog_enter_blue);
				setPositiveButton(R.string.ok, this);
			}

			public void onClick(DialogInterface dialog, int button)
			{
				int value = NumberUtil.toInt(mColorValue.getText().toString());
				if (value < 0)
					value = 0;
				else if (value > 255)
					value = 255;
				int color = mView.mCenterPaint.getColor();
				if (mPortion == RED)
					color = ColorUtil.setRed(color, value);
				else if (mPortion == GREEN)
					color = ColorUtil.setGreen(color, value);
				else if (mPortion == BLUE)
					color = ColorUtil.setBlue(color, value);
				onOkClicked(color);
				dialog.dismiss();
			}

			public void onOkClicked(int color)
			{
			}
		}

		public ColorPickerView(Context c, OnColorChangedListener l, int color)
		{
			super(c);
			mListener = l;
			mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000 };
			mSaturation = new int[] { 0xFF000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000 };

			Shader rainbow = new SweepGradient(0, 0, mColors, null);
			Shader saturated = new SweepGradient(0, 0, mSaturation, null);
			Shader brightness = new SweepGradient(0, 0, new int[] { 0xFFFFFFFF, color, color, 0xFFFFFFFF }, null);

			mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mColorPaint.setShader(rainbow);
			mColorPaint.setStyle(Paint.Style.STROKE);
			mColorPaint.setStrokeWidth(STROKE_WIDTH);

			mSaturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mSaturationPaint.setShader(saturated);
			mSaturationPaint.setStyle(Paint.Style.STROKE);
			mSaturationPaint.setStrokeWidth(STROKE_WIDTH);

			mBrightnessPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mBrightnessPaint.setShader(brightness);
			mBrightnessPaint.setStyle(Paint.Style.STROKE);
			mBrightnessPaint.setStrokeWidth(STROKE_WIDTH);

			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(color);
			mCenterPaint.setStrokeWidth(5);

			mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mTextPaint.setStyle(Paint.Style.FILL);
			mTextPaint.setTextSize(24);
			mTextPaint.setColor(Color.WHITE);

			mCenterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterTextPaint.setStyle(Paint.Style.STROKE);
			mCenterTextPaint.setTextSize(36);
			mCenterTextPaint.setColor(Color.BLACK);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			int[] colors = null;
			int color = mCenterPaint.getColor();

			// Color RGB values
			canvas.drawText("R: " + Color.red(color), CENTER_X - 120, 2 * CENTER_Y + OFFSET_Y - 10, mTextPaint);
			canvas.drawText("G: " + Color.green(color), CENTER_X - 35, 2 * CENTER_Y + OFFSET_Y - 10, mTextPaint);
			canvas.drawText("B: " + Color.blue(color), CENTER_X + 55, 2 * CENTER_Y + OFFSET_Y - 10, mTextPaint);

			canvas.translate(CENTER_X, CENTER_Y);

			// colors
			canvas.drawOval(new RectF(-COLORS_RADIUS, -COLORS_RADIUS, COLORS_RADIUS, COLORS_RADIUS), mColorPaint);

			// saturation semicircle
			int saturated = ColorUtil.saturate(color, 1);
			int desaturated = ColorUtil.saturate(color, 0);
			colors = new int[] { saturated, desaturated, desaturated, saturated };
			mSaturationPaint.setShader(new SweepGradient(0, 0, colors, null));
			canvas.drawArc(new RectF(-SAT_BR_RADIUS, -SAT_BR_RADIUS, SAT_BR_RADIUS, SAT_BR_RADIUS), 180, 180, false, mSaturationPaint);

			// brightness semicircle
			int brightened = ColorUtil.brighten(color, 1);
			int debrightened = ColorUtil.brighten(color, 0);
			colors = new int[] { brightened, debrightened, debrightened, brightened };
			mBrightnessPaint.setShader(new SweepGradient(0, 0, colors, null));
			canvas.drawArc(new RectF(-SAT_BR_RADIUS, -SAT_BR_RADIUS, SAT_BR_RADIUS, SAT_BR_RADIUS), 0, 180, false, mBrightnessPaint);

			// inner circle with current color and OK label
			canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
			mCenterTextPaint.setStyle(Paint.Style.FILL);
			mCenterTextPaint.setColor(Color.argb(128, 0, 0, 0));
			canvas.drawText("OK", -26, 14, mCenterTextPaint);
			mCenterTextPaint.setStyle(Paint.Style.FILL);
			mCenterTextPaint.setColor(Color.argb(128, 255, 255, 255));
			canvas.drawText("OK", -25, 15, mCenterTextPaint);

			if (mTrackingCenter)
			{
				int c = mCenterPaint.getColor();
				mCenterPaint.setStyle(Paint.Style.STROKE);

				if (mHighlightCenter)
				{
					mCenterPaint.setAlpha(0xFF);
				}
				else
				{
					mCenterPaint.setAlpha(0x80);
				}
				canvas.drawCircle(0, 0, CENTER_RADIUS + 5, mCenterPaint);

				mCenterPaint.setStyle(Paint.Style.FILL);
				mCenterPaint.setColor(c);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2 + OFFSET_Y);
		}

		private int average(int s, int d, float p)
		{
			return s + java.lang.Math.round(p * (d - s));
		}

		private int interpolateColor(float unit)
		{
			// full circle
			if (unit <= 0)
			{
				return mColors[0];
			}
			if (unit >= 1)
			{
				return mColors[mColors.length - 1];
			}

			float p = unit * (mColors.length - 1);
			int i = (int) p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
			int c0 = mColors[i];
			int c1 = mColors[i + 1];
			int a = average(Color.alpha(c0), Color.alpha(c1), p);
			int r = average(Color.red(c0), Color.red(c1), p);
			int g = average(Color.green(c0), Color.green(c1), p);
			int b = average(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);
		}

		private int interpolateSaturation(float unit, float x)
		{
			// upper half circle [0.5..1]
			if (unit < 0.5f && x < 0)
				unit = 0.5f;
			else if (unit < 0.5f && x > 0)
				unit = 1f;
			else if (unit > 1f)
				unit = 1f;
			return ColorUtil.saturate(mCenterPaint.getColor(), (unit * 2) - 1);
		}

		private int interpolateBrightness(float unit, float x)
		{
			// lower half circle [0..0.5]
			if (unit < 0f)
				unit = 0f;
			else if (unit > 0.5f && x < 0)
				unit = 0.5f;
			else if (unit > 0.5f && x > 0)
				unit = 0f;
			return ColorUtil.brighten(mCenterPaint.getColor(), 1 - (unit * 2));
		}

		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			int color = mCenterPaint.getColor();
			float x = event.getX() - CENTER_X;
			float y = event.getY() - CENTER_Y;
			double r = java.lang.Math.sqrt(x * x + y * y);
			boolean inCenter = r <= CENTER_OUTER_RADIUS;
			boolean inSaturation = r >= SAT_BR_INNER_RADIUS && r <= SAT_BR_OUTER_RADIUS && y < 0;
			boolean inBrightness = r >= SAT_BR_INNER_RADIUS && r <= SAT_BR_OUTER_RADIUS && y > 0;
			boolean inColors = r >= COLORS_INNER_RADIUS && r <= COLORS_OUTER_RADIUS;
			boolean onRed = x < -45 && y > COLORS_OUTER_RADIUS;
			boolean onGreen = x >= -45 && x <= 45 && y > COLORS_OUTER_RADIUS;
			boolean onBlue = x > 45 && y > COLORS_OUTER_RADIUS;

			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				mTrackingCenter = inCenter;
				mTrackingSaturation = inSaturation;
				mTrackingBrightness = inBrightness;
				mTrackingColor = inColors;
				if (onRed)
				{
					enterColor(Color.red(color), ColorInputDialog.RED);
					break;
				}
				else if (onGreen)
				{
					enterColor(Color.green(color), ColorInputDialog.GREEN);
					break;
				}
				else if (onBlue)
				{
					enterColor(Color.blue(color), ColorInputDialog.BLUE);
					break;
				}
				else if (inCenter)
				{
					mHighlightCenter = true;
					invalidate();
					break;
				}
				else if (!inColors && !inSaturation && !inBrightness)
				{
					break;
				}
			case MotionEvent.ACTION_MOVE:
				if (mTrackingCenter)
				{
					if (mHighlightCenter != inCenter)
					{
						mHighlightCenter = inCenter;
						invalidate();
					}
				}
				else if (mTrackingBrightness || mTrackingSaturation || mTrackingColor)
				{
					float angle = (float) java.lang.Math.atan2(y, x);
					// need to turn angle [-PI ... PI] into unit [0....1]
					float unit = angle / (2 * PI);
					if (unit < 0)
					{
						unit += 1;
					}
					if (mTrackingBrightness)
					{
						color = interpolateBrightness(unit, x);
					}
					else if (mTrackingSaturation)
					{
						color = interpolateSaturation(unit, x);
					}
					else if (mTrackingColor)
					{
						color = interpolateColor(unit);
					}
					mCenterPaint.setColor(color);
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mTrackingCenter)
				{
					if (inCenter)
					{
						mListener.colorChanged(color);
					}
					mTrackingCenter = false; // so we draw w/o halo
					invalidate();
				}
				mTrackingSaturation = false;
				mTrackingBrightness = false;
				mTrackingColor = false;
				break;
			}
			return true;
		}

		private void enterColor(int value, int portion)
		{
			final ColorPickerView view = this;
			ColorInputDialog dialog = new ColorInputDialog(this, value, portion)
			{
				@Override
				public void onOkClicked(int color)
				{
					view.mCenterPaint.setColor(color);
					view.invalidate();
				}
			};
			dialog.show();
		}
	}

	public ColorPickerDialog(Context context, String storageID)
	{
		super(context);
		mContext = context;
		mStorageID = storageID;
		mInitialColor = ColorUtil.toARGBColor(Storage.get(storageID));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		OnColorChangedListener listener = new OnColorChangedListener()
		{
			public void colorChanged(int color)
			{
				Storage.set(mStorageID, ColorUtil.toRGBString(color));
				if (mContext instanceof Storage.OnChangeListener)
				{
					((Storage.OnChangeListener) mContext).onStorageChange(mStorageID);
				}
				dismiss();
			}
		};

		setContentView(new ColorPickerView(getContext(), listener, mInitialColor));
		setTitle(R.string.dialog_pick_color);
	}
}
