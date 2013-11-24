package com.urban.app.fractal.ljapunow;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.urban.app.fractal.ljapunow.activity.FileSelection;
import com.urban.app.fractal.ljapunow.activity.GradientEditor;
import com.urban.app.fractal.ljapunow.activity.HelpContents;
import com.urban.app.fractal.ljapunow.dialog.AboutDialog;
import com.urban.app.fractal.ljapunow.dialog.InputDialog;
import com.urban.app.fractal.ljapunow.dialog.SelectionDialog;
import com.urban.app.fractal.ljapunow.util.ColorUtil;
import com.urban.app.fractal.ljapunow.util.DisplayMetrics;
import com.urban.app.fractal.ljapunow.util.NumberUtil;
import com.urban.app.fractal.ljapunow.util.Storage;
import com.urban.app.fractal.ljapunow.view.ImageView;

public class MainActivity extends Activity implements OnClickListener, Storage.OnChangeListener
{
	private class State
	{
		public FractalGenerator.State	generatorState;
		// public boolean fullScreen;
		// public String settings;
	}

	static public final int		REQUEST_IMPORT			= 1;
	static public final int		REQUEST_EXPORT			= 2;
	static public final int		REQUEST_SAVE_IMAGE		= 3;
	static public final int		REQUEST_GRADIENT		= 4;

	private Presets				presets					= null;
	private ImageView			imageView				= null;
	private ProgressBar			progressBar				= null;
	private TouchEventHandler	touchEventHandler		= null;
	private View				mainView				= null;
	private boolean				fullScreen				= true;
	private State				savedState				= null;
	private Set<String>			colorParams				= null;
	private Set<String>			generatorParams			= null;
	private Handler				handler					= null;
	private MenuItem			lastOptionsItemSelected	= null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mainView = getLayoutInflater().inflate(R.layout.main, null);
		setContentView(mainView);

		handler = new Handler();

		// Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		// {
		// public void uncaughtException(Thread thread, Throwable e)
		// {
		// Logger.error(MainActivity.this, e);
		// }
		// });

		// savedState = (State) getLastNonConfigurationInstance();
		// if (savedState != null)
		// {
		// fullScreen = savedState.fullScreen;
		// Storage.deserialize(savedState.settings);
		// }
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);

		if (imageView == null)
		{
			findViewById(R.id.loading).setVisibility(View.VISIBLE);
			findViewById(R.id.title).setVisibility(View.VISIBLE);
			findViewById(R.id.subtitle).setVisibility(View.VISIBLE);

			new Thread(new Runnable()
			{
				public void run()
				{
					presets = new Presets(MainActivity.this);

					handler.post(new Runnable()
					{
						public void run()
						{
							initUI();
						}
					});
				}
			}).start();
		}
	}

	private void initUI()
	{
		DisplayMetrics.gather(getBaseContext());

		View title = findViewById(R.id.title);
		title.setVisibility(View.GONE);
		title.setEnabled(false);

		View subtitle = findViewById(R.id.subtitle);
		subtitle.setVisibility(View.GONE);
		subtitle.setEnabled(false);

		View loadingBar = findViewById(R.id.loading);
		loadingBar.setVisibility(View.GONE);
		loadingBar.setEnabled(false);
		loadingBar.clearAnimation();

		progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setOnClickListener(MainActivity.this);

		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setProgressBar(progressBar);

		touchEventHandler = new TouchEventHandler(this, imageView);

		if (fullScreen)
		{
			setFullscreen(fullScreen);
		}

		FractalGenerator.loadNativeCodeLib();
		imageView.generateFractal(savedState == null ? null : savedState.generatorState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		DisplayMetrics.gather(getBaseContext());
		super.onConfigurationChanged(newConfig);
	}

	// @Override
	// public Object onRetainNonConfigurationInstance()
	// {
	// State state = new State();
	// state.fullScreen = fullScreen;
	// state.settings = Storage.serialize();
	// state.generatorState = imageView != null ? imageView.getGeneratorState() : null;
	// return state;
	// }

	@Override
	protected void onDestroy()
	{
		// Save current settings to be loaded on next start
		presets.saveAsLast();
		
		if (imageView != null)
		{
			imageView.stopGenerator(false);
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if (imageView == null)
		{
			return false;
		}

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (imageView == null)
		{
			return false;
		}

		if (imageView.isGeneratingFractal())
		{
			menu.findItem(R.id.generate).setVisible(false);
			menu.findItem(R.id.stop).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.generate).setVisible(true);
			menu.findItem(R.id.stop).setVisible(false);
		}

		boolean rotateLeft = NumberUtil.toBoolean(Storage.get(FractalGenerator.STORAGE_ROTATE_LEFT));
		menu.findItem(R.id.swap).setChecked(rotateLeft);

		boolean cyclicColoration = NumberUtil.toBoolean(Storage.get(FractalColoration.STORAGE_CYCLIC_COLORATION));
		menu.findItem(R.id.cyclic_coloration).setChecked(cyclicColoration);

		boolean oneGradient = NumberUtil.toBoolean(Storage.get(FractalColoration.STORAGE_ONE_GRADIENT));
		menu.findItem(R.id.one_gradient).setChecked(oneGradient);
		menu.findItem(R.id.chaos_gradient).setVisible(!oneGradient);

		boolean histogram = NumberUtil.toBoolean(Storage.get(FractalColoration.STORAGE_HISTOGRAM));
		menu.findItem(R.id.histogram).setChecked(histogram);

		boolean autoExp = NumberUtil.toBoolean(Storage.get(FractalColoration.STORAGE_AUTO_EXP_INTERVAL));
		menu.findItem(R.id.auto_exponent_interval).setChecked(autoExp);

		menu.findItem(R.id.fullscreen).setChecked(fullScreen);

		menu.findItem(R.id.debug).setChecked(imageView.getDebug());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;
		AlertDialog.Builder dialog;

		switch (item.getItemId())
		{
		case R.id.generate:
			imageView.generateFractal();
			break;
		case R.id.stop:
			imageView.stopGenerator(true);
			break;
		case R.id.stability_gradient:
			intent = new Intent(getBaseContext(), GradientEditor.class);
			intent.putExtra(GradientEditor.GRADIENT, FractalColoration.STORAGE_STABILITY_GRADIENT);
			startActivityForResult(intent, REQUEST_GRADIENT);
			break;
		case R.id.chaos_gradient:
			intent = new Intent(getBaseContext(), GradientEditor.class);
			intent.putExtra(GradientEditor.GRADIENT, FractalColoration.STORAGE_CHAOS_GRADIENT);
			startActivityForResult(intent, REQUEST_GRADIENT);
			break;
		case R.id.cyclic_coloration:
			dialog = new InputDialog(this, R.string.dialog_cyclic_coloration, true, FractalColoration.STORAGE_CYCLIC_COLORATION, InputDialog.SIGNED_FLOAT_NUMBER);
			dialog.show();
			break;
		case R.id.auto_exponent_interval:
			boolean autoExp = NumberUtil.toBoolean(Storage.get(FractalColoration.STORAGE_AUTO_EXP_INTERVAL));
			Storage.set(FractalColoration.STORAGE_AUTO_EXP_INTERVAL, autoExp ? "false" : "true");
			onStorageChange(FractalColoration.STORAGE_AUTO_EXP_INTERVAL);
			break;
		case R.id.histogram:
			boolean histogram = NumberUtil.toBoolean(Storage.get(FractalColoration.STORAGE_HISTOGRAM));
			Storage.set(FractalColoration.STORAGE_HISTOGRAM, histogram ? "false" : "true");
			onStorageChange(FractalColoration.STORAGE_HISTOGRAM);
			break;
		case R.id.one_gradient:
			boolean oneGradient = NumberUtil.toBoolean(Storage.get(FractalColoration.STORAGE_ONE_GRADIENT));
			Storage.set(FractalColoration.STORAGE_ONE_GRADIENT, oneGradient ? "false" : "true");
			onStorageChange(FractalColoration.STORAGE_ONE_GRADIENT);
			break;
		case R.id.exponent:
			changeExponentInterval();
			break;
		case R.id.iterations:
			dialog = new InputDialog(this, R.string.dialog_iterations, true, FractalGenerator.STORAGE_ITERATIONS, InputDialog.UNSIGNED_NUMBER);
			dialog.show();
			break;
		case R.id.gradations:
			dialog = new InputDialog(this, R.string.dialog_gradations, true, FractalColoration.STORAGE_GRADATIONS, InputDialog.UNSIGNED_NUMBER);
			dialog.show();
			break;
		case R.id.warmup:
			dialog = new InputDialog(this, R.string.dialog_warmup, true, FractalGenerator.STORAGE_WARMUP, InputDialog.UNSIGNED_NUMBER);
			dialog.show();
			break;
		case R.id.sequence:
			dialog = new InputDialog(this, R.string.dialog_sequence, true, FractalGenerator.STORAGE_SEQUENCE, InputDialog.CAPITALS);
			dialog.show();
			break;
		case R.id.x0:
			dialog = new InputDialog(this, R.string.dialog_x0, true, FractalGenerator.STORAGE_X0, InputDialog.SIGNED_FLOAT_NUMBER);
			dialog.show();
			break;
		case R.id.swap:
			boolean rotateLeft = NumberUtil.toBoolean(Storage.get(FractalGenerator.STORAGE_ROTATE_LEFT));
			Storage.set(FractalGenerator.STORAGE_ROTATE_LEFT, rotateLeft ? "false" : "true");
			onStorageChange(FractalGenerator.STORAGE_ROTATE_LEFT);
			break;
		case R.id.x_interval:
			dialog = new InputDialog(this, R.string.dialog_xint, false, FractalGenerator.STORAGE_A_INTERVAL, InputDialog.SIGNED_FLOAT_NUMBER);
			dialog.show();
			break;
		case R.id.y_interval:
			dialog = new InputDialog(this, R.string.dialog_yint, false, FractalGenerator.STORAGE_B_INTERVAL, InputDialog.SIGNED_FLOAT_NUMBER);
			dialog.show();
			break;
		case R.id.save_preset:
			presets.save();
			break;
		case R.id.load_preset:
			presets.load(imageView);
			break;
		case R.id.remove_preset:
			presets.remove();
			break;
		case R.id.import_preset:
			presets.import_(this);
			break;
		case R.id.export_preset:
			presets.export(this);
			break;
		case R.id.save_image:
			intent = new Intent(getBaseContext(), FileSelection.class);
			intent.putExtra(FileSelection.TITLE, getResources().getString(R.string.menu_save_image) + ": " + Storage.get(Presets.SETTINGS_NAME));
			intent.putExtra(FileSelection.START_PATH, "/sdcard");
			intent.putExtra(FileSelection.FILE_NAME, Storage.get(Presets.SETTINGS_NAME) + ".png");
			startActivityForResult(intent, REQUEST_SAVE_IMAGE);
			break;
		case R.id.fullscreen:
			fullScreen = !fullScreen;
			setFullscreen(fullScreen);
			break;
		case R.id.debug:
			imageView.setDebug(!imageView.getDebug());
			imageView.invalidate();
			break;
		case R.id.about:
			new AboutDialog(this).show();
			break;
		case R.id.help_contents:
			intent = new Intent(getBaseContext(), HelpContents.class);
			startActivity(intent);
			break;
		case R.id.exit:
			this.finish();
			System.exit(0);
			break;
		case R.id.random_params:
			randomizeParameters();
			break;
		case R.id.random_colors:
			randomizeColors();
			break;
		case R.id.equation:
			dialog = new SelectionDialog(this, R.string.dialog_equation, R.array.equations, FractalGenerator.STORAGE_EQUATION);
			dialog.show();
			break;
		default:
			return false;
		}

		lastOptionsItemSelected = item;
		return true;
	}

	public void repeatLastAction()
	{
		if (lastOptionsItemSelected != null)
		{
			String[] selections = new String[] { lastOptionsItemSelected.getTitle().toString() };
			new SelectionDialog(this, R.string.dialog_repeat, selections, false)
			{
				@Override
				public boolean onSelection(int which)
				{
					onOptionsItemSelected(lastOptionsItemSelected);
					return true;
				}
			}.show();
		}
	}

	private void changeExponentInterval()
	{
		AlertDialog.Builder dialog = new InputDialog(this, R.string.dialog_exponent, R.string.suggest, false, FractalColoration.STORAGE_EXPONENT_INTERVAL, InputDialog.SIGNED_FLOAT_NUMBER)
		{
			@Override
			public boolean onNeutralClicked()
			{
				FractalGenerator.State state = imageView.getGeneratorState();
				if (state != null && !(state.measuredMaxExp == 0 && state.measuredMinExp == 0))
				{
					float min = NumberUtil.align(state.measuredMinExp);
					float max = NumberUtil.align(state.measuredMaxExp);
					firstValue.setText("" + min);
					secondValue.setText("" + max);
				}
				return true;
			}
		};
		dialog.show();
	}

	public void onClick(View v)
	{
		imageView.stopGenerator(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction() & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_DOWN:
			touchEventHandler.touchDown(event);
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
			touchEventHandler.multiTouchDown(event);
			return true;
		case MotionEvent.ACTION_MOVE:
			touchEventHandler.touchMoved(event);
			return true;
		case MotionEvent.ACTION_UP:
			touchEventHandler.touchReleased(event);
			return true;
		case MotionEvent.ACTION_POINTER_UP:
			touchEventHandler.multiTouchReleased(event);
			return true;
		case MotionEvent.ACTION_CANCEL:
			touchEventHandler.touchCancelled(event);
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == REQUEST_GRADIENT)
			{
				onStorageChange(data.getStringExtra(GradientEditor.GRADIENT));
			}
			else
			{
				String[] filePath = data.getStringArrayExtra(FileSelection.RESULT_PATH);
				for (int i = 0; i < filePath.length; i++)
				{
					switch (requestCode)
					{
					case REQUEST_IMPORT:
						presets.importFromFile(filePath[i]);
						break;
					case REQUEST_EXPORT:
						presets.exportToFile(data.getStringExtra(Presets.SETTINGS_NAME), filePath[i]);
						break;
					case REQUEST_SAVE_IMAGE:
						imageView.saveFractalAsImage(filePath[i]);
						break;
					}
				}
			}
		}
	}

	private void setFullscreen(boolean fullScreen)
	{
		if (fullScreen)
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		else
		{
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		mainView.requestLayout();
	}

	private void randomizeParameters()
	{
		Random r = new Random(System.currentTimeMillis() + new Long(System.currentTimeMillis()).hashCode());

		String sequence = "" + (char) ('A' + r.nextInt(2));
		for (int i = 1 + r.nextInt(19); i > 0; i--)
		{
			sequence += (char) ('A' + r.nextInt(3));
		}
		Storage.set(FractalGenerator.STORAGE_SEQUENCE, sequence);

		Storage.set(FractalGenerator.STORAGE_ITERATIONS, "" + (1 + r.nextInt(101)));

		Storage.set(FractalGenerator.STORAGE_WARMUP, "" + (1 + r.nextInt(101)));

		// double a1 = 7 - r.nextDouble() * 14;
		// double a2 = 7 - r.nextDouble() * 14;
		// Storage.set(FractalGenerator.STORAGE_A_INTERVAL, Math.min(a1, a2) + "," + Math.max(a1, a2));

		// double b1 = 7 - r.nextDouble() * 14;
		// double b2 = 7 - r.nextDouble() * 14;
		// Storage.set(FractalGenerator.STORAGE_B_INTERVAL, Math.min(b1, b2) + "," + Math.max(b1, b2));

		Storage.set(FractalGenerator.STORAGE_X0, "" + r.nextFloat());

		imageView.generateFractal();
	}

	private void randomizeColors()
	{
		Random r = new Random(System.currentTimeMillis() + new Long(System.currentTimeMillis()).hashCode());

		String gradient = "";
		int count = r.nextInt(10) + 2;
		for (int i = 0; i < count; i++)
		{
			gradient += ColorUtil.toRGBString(Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
			if (i + 1 < count)
			{
				gradient += ",";
			}
		}
		Storage.set(FractalColoration.STORAGE_STABILITY_GRADIENT, gradient);

		gradient = "";
		count = r.nextInt(10) + 2;
		for (int i = 0; i < count; i++)
		{
			gradient += ColorUtil.toRGBString(Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
			if (i + 1 < count)
			{
				gradient += ",";
			}
		}
		Storage.set(FractalColoration.STORAGE_CHAOS_GRADIENT, gradient);

		Storage.set(FractalColoration.STORAGE_GRADATIONS, "" + (count + r.nextInt(2048)));

		// Storage.set(FractalColoration.STORAGE_ONE_GRADIENT, "" + r.nextBoolean());

		// Storage.set(FractalGenerator.STORAGE_CYCLIC_COLORATION, "" + r.nextBoolean());

		// Storage.set(FractalColoration.STORAGE_EXPONENT_INTERVAL, (0 - 6 * r.nextFloat()) + "," + (6 *
		// r.nextFloat()));

		imageView.recalcColors(false);
	}

	public void onStorageChange(String storageID)
	{
		if (colorParams == null)
		{
			colorParams = new HashSet<String>();
			colorParams.add(FractalColoration.STORAGE_EXPONENT_INTERVAL);
			colorParams.add(FractalColoration.STORAGE_STABILITY_GRADIENT);
			colorParams.add(FractalColoration.STORAGE_CHAOS_GRADIENT);
			colorParams.add(FractalColoration.STORAGE_CYCLIC_COLORATION);
			colorParams.add(FractalColoration.STORAGE_ONE_GRADIENT);
			colorParams.add(FractalColoration.STORAGE_GRADATIONS);
			colorParams.add(FractalColoration.STORAGE_HISTOGRAM);
			colorParams.add(FractalColoration.STORAGE_AUTO_EXP_INTERVAL);
		}
		if (generatorParams == null)
		{
			generatorParams = new HashSet<String>();
			generatorParams.add(FractalGenerator.STORAGE_SEQUENCE);
			generatorParams.add(FractalGenerator.STORAGE_ITERATIONS);
			generatorParams.add(FractalGenerator.STORAGE_A_INTERVAL);
			generatorParams.add(FractalGenerator.STORAGE_B_INTERVAL);
			generatorParams.add(FractalGenerator.STORAGE_X0);
			generatorParams.add(FractalGenerator.STORAGE_WARMUP);
			generatorParams.add(FractalGenerator.STORAGE_ROTATE_LEFT);
			generatorParams.add(FractalGenerator.STORAGE_EQUATION);
		}
		if (colorParams.contains(storageID))
		{
			imageView.recalcColors(false);
		}
		else if (generatorParams.contains(storageID))
		{
			imageView.generateFractal();
		}
	}
}
