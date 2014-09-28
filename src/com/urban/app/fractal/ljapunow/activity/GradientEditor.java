package com.urban.app.fractal.ljapunow.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.urban.app.fractal.ljapunow.R;
import com.urban.app.fractal.ljapunow.dialog.ConfirmDialog;
import com.urban.app.fractal.ljapunow.dialog.InputDialog;
import com.urban.app.fractal.ljapunow.dialog.SelectionDialog;
import com.urban.app.fractal.ljapunow.util.ColorUtil;
import com.urban.app.fractal.ljapunow.util.NumberUtil;
import com.urban.app.fractal.ljapunow.util.Storage;
import com.urban.app.fractal.ljapunow.view.GradientView;

public class GradientEditor extends Activity
{
	public static final String	GRADIENT			= "GRADIENT";

	private ListView			color_list			= null;
	private TextView			red_value			= null;
	private TextView			green_value			= null;
	private TextView			blue_value			= null;
	private GradientView		gradient_preview	= null;
	private GradientView		main_colors			= null;
	private GradientView		brightness			= null;
	private GradientView		saturation			= null;
	private ColorListAdapter	colors_adapter		= null;
	private List<Integer>		current_gradient	= null;
	private Map<String, String>	predef_gradients	= null;
	private int					selectedColor		= 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		current_gradient = parseGradient(Storage.get(getIntent().getStringExtra(GRADIENT)));

		initPredefinedGradients();

		setContentView(R.layout.gradient_editor_main);

		color_list = (ListView) findViewById(R.id.color_list);
		colors_adapter = new ColorListAdapter(this, R.layout.gradient_editor_color, new ArrayList<Integer>(current_gradient));
		color_list.setAdapter(colors_adapter);

		gradient_preview = new GradientView(this, GradientView.CURRENT_GRADIENT);
		LinearLayout gradient_parent = (LinearLayout) findViewById(R.id.gradient_preview);
		gradient_parent.addView(gradient_preview);

		main_colors = new GradientView(this, GradientView.MAIN_COLORS);
		LinearLayout main_colors_parent = (LinearLayout) findViewById(R.id.main_colors);
		main_colors_parent.addView(main_colors);

		saturation = new GradientView(this, GradientView.SATURATION);
		saturation.setColor(current_gradient.get(0));
		LinearLayout saturation_parent = (LinearLayout) findViewById(R.id.saturation);
		saturation_parent.addView(saturation);

		brightness = new GradientView(this, GradientView.BRIGHTNESS);
		brightness.setColor(current_gradient.get(0));
		LinearLayout brightness_parent = (LinearLayout) findViewById(R.id.brightness);
		brightness_parent.addView(brightness);

		red_value = (TextView) findViewById(R.id.red_value);
		red_value.setText("" + Color.red(current_gradient.get(0)));
		red_value.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				ColorInputDialog dialog = new ColorInputDialog(view.getContext(), NumberUtil.toInt(red_value.getText().toString()), ColorInputDialog.RED);
				dialog.show();
			}
		});

		green_value = (TextView) findViewById(R.id.green_value);
		green_value.setText("" + Color.green(current_gradient.get(0)));
		green_value.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				ColorInputDialog dialog = new ColorInputDialog(view.getContext(), NumberUtil.toInt(green_value.getText().toString()), ColorInputDialog.GREEN);
				dialog.show();
			}
		});

		blue_value = (TextView) findViewById(R.id.blue_value);
		blue_value.setText("" + Color.blue(current_gradient.get(0)));
		blue_value.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				ColorInputDialog dialog = new ColorInputDialog(view.getContext(), NumberUtil.toInt(blue_value.getText().toString()), ColorInputDialog.BLUE);
				dialog.show();
			}
		});
	}

	public List<Integer> getCurrentGradient()
	{
		return current_gradient;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gradient_editor_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.load_gradient:
			String[] gradientNameArray = getGradientNames(true);
			List<String> gradientNameList = new ArrayList<String>();
			gradientNameList.addAll(Arrays.asList(gradientNameArray));
			GradientListAdapter adapter = new GradientListAdapter(this, R.layout.gradient_editor_gradient, gradientNameList);
			new SelectionDialog(this, R.string.menu_load_gradient, gradientNameArray, adapter)
			{
				@Override
				public boolean onSelection(int which)
				{
					loadGradient(which);
					return true;
				}
			}.show();
			break;
		case R.id.save_gradient:
			saveCurrentGradient();
			break;
		case R.id.discard_gradient:
			new SelectionDialog(this, R.string.menu_discard_gradient, getGradientNames(false), true)
			{
				@Override
				public boolean onSelection(Set<Integer> selected)
				{
					String[] names = getGradientNames(false);
					SharedPreferences settings = getSharedPreferences(GRADIENT, 0);
					SharedPreferences.Editor editor = settings.edit();
					Iterator<Integer> i = selected.iterator();
					while (i.hasNext())
					{
						editor.remove(names[i.next()]);
					}
					editor.commit();
					return true;
				}
			}.show();
			break;
		case R.id.reverse_gradient:
			Collections.reverse(current_gradient);
			onColorSelectionChanged();
			break;
		case R.id.rotate_gradient:
			new InputDialog(this, R.string.dialog_rotate_gradient, true, "180", InputDialog.SIGNED_NUMBER)
			{
				@Override
				public boolean onOkClicked(String degree)
				{
					rotateGradient(NumberUtil.toInt(degree));
					return true;
				}
			}.show();
			break;
		case R.id.duplicate_gradient:
			current_gradient.addAll(current_gradient);
			onColorSelectionChanged();
			break;
		case R.id.shuffle_gradient:
			Collections.shuffle(current_gradient);
			onColorSelectionChanged();
			break;
		case R.id.randomize_gradient:
			randomizeGradient();
			break;
		default:
			return false;
		}
		return true;
	}

	private void randomizeGradient()
	{
		Random r = new Random(System.currentTimeMillis() + new Long(System.currentTimeMillis()).hashCode());
		for (int i = 0; i < current_gradient.size(); i++)
		{
			current_gradient.set(i, Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
		}
		onColorSelectionChanged();
	}

	public void rotateGradient(int degree)
	{
		float[] hsv = new float[3];
		for (int i = 0; i < current_gradient.size(); i++)
		{
			Integer color = current_gradient.get(i);
			Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
			hsv[0] += degree;
			while (hsv[0] > 360)
			{
				hsv[0] -= 360;
			}
			while (hsv[0] < 0)
			{
				hsv[0] += 360;
			}
			color = Color.HSVToColor(hsv);
			current_gradient.set(i, color);
		}
		onColorSelectionChanged();
	}

	public void saturateGradient(float offset)
	{
		for (int i = 0; i < current_gradient.size(); i++)
		{
			Integer color = current_gradient.get(i);
			current_gradient.set(i, ColorUtil.saturateRelative(color, offset));
		}
		onColorSelectionChanged();
	}

	public void brightenGradient(float offset)
	{
		for (int i = 0; i < current_gradient.size(); i++)
		{
			Integer color = current_gradient.get(i);
			current_gradient.set(i, ColorUtil.brightenRelative(color, offset));
		}
		onColorSelectionChanged();
	}

	public void onColorChanged(int red, int green, int blue, float sat, float bright)
	{
		Integer color = current_gradient.get(selectedColor);
		if (red != -1)
		{
			color = ColorUtil.setRed(color, red);
		}
		if (green != -1)
		{
			color = ColorUtil.setGreen(color, green);
		}
		if (blue != -1)
		{
			color = ColorUtil.setBlue(color, blue);
		}
		if (sat != -1)
		{
			color = ColorUtil.saturate(color, sat);
		}
		if (bright != -1)
		{
			color = ColorUtil.brighten(color, bright);
		}
		current_gradient.set(selectedColor, color);
		onColorSelectionChanged();
	}

	private void onColorSelectionChanged()
	{
		onColorSelectionChanged(true);
	}

	private void onColorSelectionChanged(boolean redrawGradient)
	{
		while (!colors_adapter.isEmpty())
		{
			colors_adapter.remove(colors_adapter.getItem(0));
		}
		Iterator<Integer> i = current_gradient.iterator();
		while (i.hasNext())
		{
			colors_adapter.add(i.next());
		}
		colors_adapter.notifyDataSetChanged();

		if (redrawGradient)
		{
			gradient_preview.invalidate();
		}

		saturation.setColor(current_gradient.get(selectedColor));
		brightness.setColor(current_gradient.get(selectedColor));

		Integer color = current_gradient.get(selectedColor);
		red_value.setText("" + Color.red(color));
		green_value.setText("" + Color.green(color));
		blue_value.setText("" + Color.blue(color));
	}

	private void initPredefinedGradients()
	{
		predef_gradients = new LinkedHashMap<String, String>();
		predef_gradients.put("Rainbow", "#ff0000,#ff8000,#ffff00,#008000,#0000ff,#4B0082,#9400D3");
		predef_gradients.put("Hippie", "#99042f,#6735a1,#6cfce3,#78d36d,#fb25ae,#f6894f,#f6a520,#01e408");
		predef_gradients.put("Sepia 1", "#79443B,#3D2B1F,#F5F5DC,#9F8170,#A52A2A");
		predef_gradients.put("Sepia 2", "#E97451,#900020,#F0DC82,#964B00,#CD7F32");
		predef_gradients.put("Bright Sepia", "#6C541E,#E5AA70,#C19A6B,#C2B280,#E1A95F");
		predef_gradients.put("Dark Sepia", "#D2B48C,#CB410B,#882D17,#704214,#321414");
		predef_gradients.put("Zircon Stable", "#2a0c00,#812f2f,#c5c5ad,#446544,#2a0c00,#812f2f,#c5c5ad,#2a0c00");
		predef_gradients.put("Zircon Chaos", "#513b3b,#9c836a,#bc9576");
		predef_gradients.put("Caribbean Sea", "#DCF98B,#A2D592,#63AE99,#1F84A0,#0069A3");
		predef_gradients.put("Deep Sea", "#026374,#45ADD0,#72C5F7,#185EB3,#011955");
		predef_gradients.put("Abyss", "#125C65,#36838B,#2B8899,#085686,#011955");
		predef_gradients.put("Ocean", "#006167,#03ABAC,#3FAADE,#72C5F7,#74C6D4");
		predef_gradients.put("Horizon", "#0D1536,#314A73,#6587B8,#B4DEFD,#F9FFDA,#FEDE71,#DC7B38,#70452C,#332117");
		predef_gradients.put("Neon", "#F0670F,#7F0880,#00FFFF,#FF00B0,#0007FF,#B0F800,#AF08F0");
		predef_gradients.put("Fire", "#340000,#860600,#FF8205,#FFFFCC");
		predef_gradients.put("Candle Wax", "#e26e00,#361400,#7b0f12,#eadf86");
		predef_gradients.put("Flower", "#863949,#E0768C,#C28161,#F2D6D5,#F4D791,#DEE04E,#FCFECC");
		predef_gradients.put("Rainforest", "#1F2F12,#254C17,#72A94E,#C6FF4C,#9FDE2F,#7EBE2A,#52571D,#6B5A2E,#A95E41");
		predef_gradients.put("Thicket", "#0A1505,#243508,#4C6023,#7A914B,#9FAA72,#412E10,#8A6B32,#D1BC79,#F1E5BB");
		predef_gradients.put("Snow Mountain", "#B0B0B2,#505962,#69839C,#3F4A5C,#8DA2B5,#C9CCD3,#313D4D,#6893B6,#94A8C1");
		predef_gradients.put("Light Wood", "#C3833C,#CE8F42,#B87639,#D39145,#D08F3F,#B97D37,#CF9043,#BE7D39,#CD8E41,#98693D");
		predef_gradients.put("Dark Cherry", "#3f111d,#6B1D2B,#591E22,#802532,#440817,#621C27,#290c11");
		predef_gradients.put("Bright Earth", "#78603C,#B09D7F,#508491,#90C4D2,#A2AB34,#C3C66B");
		predef_gradients.put("Earth", "#412F23,#877166,#1D383F,#588F96,#3F6019,#68923E");
		predef_gradients.put("Dark Earth", "#271E15,#574738,#0D2934,#233B45,#1E311D,#41603E");
		predef_gradients.put("Early Spring", "#BD968F,#67241E,#A46A45,#C6935C,#BA997A,#C69F5A,#BD9E7F");
		predef_gradients.put("Spring", "#E2C871,#E3BA68,#BDAB5F,#338F46,#6CAD51,#BFBD68,#A7A967,#387681,#3E7792,#54979D,#22235B");
		predef_gradients.put("Early Summer", "#485178,#9BA4A9,#677894,#60517A,#87709A,#7B9A94,#959C7D,#4A7C61,#24574E,#B8AA7B");
		predef_gradients.put("Summer", "#CAB5B2,#BDAB93,#7F645B,#552F3C,#7C7A87,#413C53,#201C33");
		predef_gradients.put("Late Summer", "#B9A19F,#CA6891,#AD2951,#5E1533,#907391,#AE5B85,#8E2D50,#79315F,#5F334E");
		predef_gradients.put("Early Autumn", "#CD9B46,#A87E4C,#A87E4C,#99A446,#55864F,#615F36,#282C37,#695077,#447B82,#293948");
		predef_gradients.put("Autumn", "#6E7A6C,#6D6447,#463C3D,#E53542,#C93F4C,#AE2C46,#E98A74,#EC6D76,#792839,#B74B31,#A56F19,#CE753B,#D15434");
		predef_gradients.put("Late Autumn", "#56413C,#6E584A,#A17D5B,#653042,#6B4145,#A08870,#C6AA85");
		predef_gradients.put("Winter", "#282F66,#292557,#2B6482,#235175,#281E37,#2C2A2D,#9A8975,#88958C,#AD9B6D,#948C97,#A4858A");
		predef_gradients.put("Warm Ice", "#8499AC,#5279A0,#B1B9BB,#8090A9,#BCBABB,#D5CCC5,#BDC3C3,#DDD9D0,#B1BFC2,#D8D1C9,#3F7AA4");
		predef_gradients.put("Cold Ice", "#74BEDB,#94CFDF,#A6D5DB,#C0DFE4,#D4E6F0,#C3E0E6,#D3E7EE");
		predef_gradients.put("Dark Ice", "#0D2D44,#0D597B,#23759A,#B4BBCE,#367084,#5683A2,#BBC9D2,#E7EAF1,#BED4E2,#5583A4");
	}

	private String[] getGradientNames(boolean include_predef)
	{
		List<String> names = new ArrayList<String>();

		if (include_predef)
		{
			names.addAll(predef_gradients.keySet());
		}

		SharedPreferences settings = getSharedPreferences(GRADIENT, 0);
		names.addAll(settings.getAll().keySet());

		return names.toArray(new String[0]);
	}

	private void saveCurrentGradient()
	{
		final SharedPreferences settings = getSharedPreferences(GRADIENT, 0);
		final SharedPreferences.Editor editor = settings.edit();

		InputDialog dialog = new InputDialog(this, R.string.menu_save, true, "", InputDialog.TEXT)
		{
			@Override
			public boolean onOkClicked(final String name)
			{
				boolean exists = settings.contains(name);
				if (exists)
				{
					ConfirmDialog confirm = new ConfirmDialog(GradientEditor.this, name, R.string.confirm_preset_save)
					{
						@Override
						public boolean onOkClicked()
						{
							editor.putString(name, serializeCurrentGradient());
							editor.commit();
							return true;
						};
					};
					confirm.show();
				}
				else
				{
					editor.putString(name, serializeCurrentGradient());
					editor.commit();
				}
				return true;
			}
		};
		dialog.show();
	}

	public String serializeCurrentGradient()
	{
		String gradient = "";
		Iterator<Integer> i = current_gradient.iterator();
		while (i.hasNext())
		{
			gradient += ColorUtil.toRGBString(i.next());
			if (i.hasNext())
			{
				gradient += ",";
			}
		}
		return gradient;
	}

	private List<Integer> parseGradient(String string)
	{
		String[] gradient = string.split(",");
		List<Integer> colors = new ArrayList<Integer>();
		for (int i = 0; i < gradient.length; i++)
		{
			colors.add(ColorUtil.toARGBColor(gradient[i]));
		}
		if (colors.size() == 0)
		{
			// add a default color
			colors.add(Color.BLACK);
		}
		return colors;
	}

	private String getGradient(String name)
	{
		String gradient = "";
		if (predef_gradients.containsKey(name))
		{
			gradient = predef_gradients.get(name);
		}
		else
		{
			SharedPreferences settings = getSharedPreferences(GRADIENT, 0);
			gradient = settings.getString(name, "");
		}
		return gradient;
	}

	private void loadGradient(int which)
	{
		String name = getGradientNames(true)[which];
		String gradient = getGradient(name);
		loadGradient(gradient);
	}

	private void loadGradient(String gradient)
	{
		current_gradient = parseGradient(gradient);
		onColorSelectionChanged();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		gradient_preview.invalidate();
		main_colors.invalidate();
		brightness.invalidate();
		saturation.invalidate();
		super.onConfigurationChanged(newConfig);
	}

	private class ColorListAdapter extends ArrayAdapter<Integer>
	{
		public ColorListAdapter(Context context, int textViewResourceId, List<Integer> objects)
		{
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			int color = getItem(position);
			View view = convertView;
			if (view == null)
			{
				view = getLayoutInflater().inflate(R.layout.gradient_editor_color, parent, false);
			}
			view.setBackgroundColor(color);
			view.setTag(position);
			if (position == selectedColor)
			{
				GradientDrawable border = (GradientDrawable) getResources().getDrawable(R.drawable.rounded_border);
				border.setColor(color);
				view.setBackgroundDrawable(border);
			}
			view.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					selectedColor = (Integer) view.getTag();
					onColorSelectionChanged(false);
				}
			});

			TextView color_name = (TextView) view.findViewById(R.id.color_name);
			color_name.setText(ColorUtil.toRGBString(color));

			ImageButton add_color = (ImageButton) view.findViewById(R.id.add_color);
			add_color.setTag(position);
			add_color.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					int position = (Integer) view.getTag();
					selectedColor = position + 1;
					Integer color = current_gradient.get(position);
					if (position + 1 == current_gradient.size())
					{
						current_gradient.add(color);
					}
					else
					{
						current_gradient.add(position + 1, color);
					}
					onColorSelectionChanged();
				}
			});

			ImageButton remove_color = (ImageButton) view.findViewById(R.id.remove_color);
			remove_color.setTag(position);
			remove_color.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					int position = (Integer) view.getTag();
					if (current_gradient.size() > 1)
					{
						if (position <= selectedColor)
						{
							if (selectedColor > 0)
							{
								selectedColor--;
							}
						}
						current_gradient.remove(position);
						onColorSelectionChanged();
					}
				}
			});

			ImageButton move_up = (ImageButton) view.findViewById(R.id.move_up);
			move_up.setTag(position);
			move_up.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					int position = (Integer) view.getTag();
					if (position > 0)
					{
						if (position == selectedColor)
						{
							selectedColor--;
						}
						else if (position == selectedColor + 1)
						{
							selectedColor++;
						}
						Integer color = current_gradient.remove(position);
						current_gradient.add(position - 1, new Integer(color));
						onColorSelectionChanged();
					}
				}
			});

			ImageButton move_down = (ImageButton) view.findViewById(R.id.move_down);
			move_down.setTag(position);
			move_down.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					int position = (Integer) view.getTag();
					if (position + 1 < current_gradient.size())
					{
						if (position == selectedColor)
						{
							selectedColor++;
						}
						else if (position == selectedColor - 1)
						{
							selectedColor--;
						}
						Integer color = current_gradient.remove(position + 1);
						current_gradient.add(position, new Integer(color));
						onColorSelectionChanged();
					}
				}
			});
			return view;
		}
	}

	private class GradientListAdapter extends ArrayAdapter<String>
	{
		public GradientListAdapter(Context context, int textViewResourceId, List<String> objects)
		{
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			String name = getItem(position);
			String colorlist = getGradient(name);

			View view = convertView;
			if (view == null)
			{
				view = getLayoutInflater().inflate(R.layout.gradient_editor_gradient, parent, false);
			}

			TextView nameview = (TextView) view.findViewById(R.id.gradient_name);
			nameview.setText(name);

			GradientView gradientview = (GradientView) view.findViewById(R.id.gradient_preview);
			gradientview.setKind(GradientView.PREVIEW_GRADIENT);
			gradientview.setGradientEditor(GradientEditor.this);
			gradientview.setColorList(colorlist);

			return view;
		}
	}

	private class ColorInputDialog extends Builder implements DialogInterface.OnClickListener
	{
		public static final int	RED		= 0;
		public static final int	GREEN	= 1;
		public static final int	BLUE	= 2;

		private EditText		colorInput;
		private int				colorPortion;

		public ColorInputDialog(Context context, int value, int portion)
		{
			super(context);
			colorPortion = portion;
			colorInput = new EditText(context);
			colorInput.setText("" + value);
			colorInput.setInputType(InputType.TYPE_CLASS_NUMBER);
			colorInput.setSingleLine();
			setView(colorInput);
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
			int value = NumberUtil.toInt(colorInput.getText().toString());
			if (value < 0)
				value = 0;
			else if (value > 255)
				value = 255;
			if (colorPortion == RED)
				onColorChanged(value, -1, -1, -1, -1);
			else if (colorPortion == GREEN)
				onColorChanged(-1, value, -1, -1, -1);
			else if (colorPortion == BLUE)
				onColorChanged(-1, -1, value, -1, -1);
			dialog.dismiss();
		}
	}
}
