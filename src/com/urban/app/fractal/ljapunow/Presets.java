package com.urban.app.fractal.ljapunow;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.urban.app.fractal.ljapunow.activity.FileSelection;
import com.urban.app.fractal.ljapunow.dialog.ConfirmDialog;
import com.urban.app.fractal.ljapunow.dialog.DialogRunner;
import com.urban.app.fractal.ljapunow.dialog.InputDialog;
import com.urban.app.fractal.ljapunow.dialog.SelectionDialog;
import com.urban.app.fractal.ljapunow.util.Logger;
import com.urban.app.fractal.ljapunow.util.Storage;
import com.urban.app.fractal.ljapunow.view.ImageView;

public class Presets
{
	public static final String	STORAGE_SETTINGS			= "com.urban.app.fractal.ljapunow.Settings.settings";
	public static final String	STORAGE_SETTINGS_VERSION	= "com.urban.app.fractal.ljapunow.Settings.settings.version";
	public static final String	STORAGE_SETTINGS_LAST		= "com.urban.app.fractal.ljapunow.Settings.settings.last";
	public static final String	SETTINGS_NAME				= "name";
	public static final String	SETTINGS_VERSION			= "version";
	public static final String	SETTINGS_LAST				= "last";
	public static final String	SETTINGS_DEFAULT			= "Default";

	private Activity			activity					= null;

	public Presets(Activity activity)
	{
		this.activity = activity;
		addPredefined();
		loadLastOrDefault();
		Storage.set(SETTINGS_NAME, "Name");
	}

	public void save()
	{
		final SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS, 0);
		final SharedPreferences.Editor editor = settings.edit();

		InputDialog dialog = new InputDialog(activity, R.string.menu_save, true, SETTINGS_NAME, InputDialog.TEXT)
		{
			@Override
			public boolean onOkClicked(final String name)
			{
				boolean exists = settings.contains(name);
				if (exists)
				{
					ConfirmDialog confirm = new ConfirmDialog(activity, name, R.string.confirm_preset_save)
					{
						@Override
						public boolean onOkClicked()
						{
							Storage.set(SETTINGS_NAME, name);
							editor.putString(name, Storage.serialize());
							editor.commit();
							return true;
						};
					};
					confirm.show();
				}
				else
				{
					Storage.set(SETTINGS_NAME, name);
					editor.putString(name, Storage.serialize());
					editor.commit();
				}
				return true;
			}
		};
		dialog.show();
	}

	public void saveAsLast()
	{
		SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS_LAST, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(SETTINGS_LAST, Storage.serialize());
		editor.commit();
	}

	public void load()
	{
		load(null);
	}

	public void load(final ImageView view)
	{
		final SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS, 0);
		String[] items = settings.getAll().keySet().toArray(new String[0]);
		Arrays.sort(items);

		SelectionDialog dialog = new SelectionDialog(activity, R.string.menu_load, items, false)
		{
			@Override
			public boolean onSelection(int which)
			{
				Storage.deserialize(settings.getString(selections[which], ""));
				if (view != null)
				{
					view.generateFractal();
				}
				return true;
			}
		};
		dialog.show();
	}

	public void remove()
	{
		final SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS, 0);
		String[] items = settings.getAll().keySet().toArray(new String[0]);
		Arrays.sort(items);

		SelectionDialog dialog = new SelectionDialog(activity, R.string.menu_remove, items, true)
		{
			@Override
			public boolean onSelection(Set<Integer> selected)
			{
				DialogRunner runner = new DialogRunner(activity);
				for (final Integer which : selected)
				{
					ConfirmDialog confirm = new ConfirmDialog(activity, selections[which].toString(), R.string.confirm_preset_remove)
					{
						@Override
						public boolean onOkClicked()
						{
							SharedPreferences.Editor editor = settings.edit();
							editor.remove(selections[which].toString());
							editor.commit();
							return true;
						};
					};
					runner.add(confirm);
				}
				runner.execute();
				return true;
			}
		};
		dialog.show();
	}

	public void importFromFile(String filePath)
	{
		try
		{
			final String oldPreset = Storage.serialize();
			Storage.load(filePath);

			final SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS, 0);
			if (settings.contains(Storage.get(SETTINGS_NAME)))
			{
				ConfirmDialog confirm = new ConfirmDialog(activity, Storage.get(SETTINGS_NAME), R.string.confirm_preset_save)
				{
					@Override
					public boolean onOkClicked()
					{
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(Storage.get(SETTINGS_NAME), Storage.serialize());
						editor.commit();
						Storage.deserialize(oldPreset);
						return true;
					}

					@Override
					public boolean onCancelClicked()
					{
						Storage.deserialize(oldPreset);
						return true;
					}
				};
				confirm.show();
			}
			else
			{
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(Storage.get(SETTINGS_NAME), Storage.serialize());
				editor.commit();
				Storage.deserialize(oldPreset);
			}
		}
		catch (Exception e)
		{
			Logger.error(activity, e);
		}
	}

	public void importFromResource(int resourceID)
	{
		try
		{
			Storage.load(activity.getResources().openRawResource(resourceID));

			SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString(Storage.get(SETTINGS_NAME), Storage.serialize());
			editor.commit();
		}
		catch (Exception e)
		{
			Logger.error(activity, e);
		}
	}

	public void exportToFile(final String name, final String filePath)
	{
		File file = new File(filePath);
		if (file.exists())
		{
			ConfirmDialog confirm = new ConfirmDialog(activity, filePath, R.string.confirm_preset_export)
			{
				@Override
				public boolean onOkClicked()
				{
					saveFile(name, filePath);
					return true;
				}
			};
			confirm.show();
		}
		else
		{
			saveFile(name, filePath);
		}
	}

	private void saveFile(String name, String filePath)
	{
		try
		{
			SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS, 0);
			Storage.save(settings.getString(name, ""), filePath, "Ljapunow Fractals Settings, Version " + activity.getString(R.string.version));
		}
		catch (Exception e)
		{
			Logger.error(activity, e);
		}
	}

	public void loadLastOrDefault()
	{
		SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS_LAST, 0);
		if (settings.contains(SETTINGS_LAST))
		{
			Storage.deserialize(settings.getString(SETTINGS_LAST, ""));
		}
		else
		{
			loadDefaultLogistic();
		}
	}

	public void loadDefaultLogistic()
	{
		try
		{
			Storage.load(activity.getResources().openRawResource(R.raw.default_logistic));
		}
		catch (Exception e)
		{
			Logger.error(activity, e);
		}
	}

	public void loadDefaultNewton3rd()
	{
		try
		{
			Storage.load(activity.getResources().openRawResource(R.raw.default_newton_3rd));
		}
		catch (Exception e)
		{
			Logger.error(activity, e);
		}
	}

	public void loadDefaultNewton()
	{
		try
		{
			Storage.load(activity.getResources().openRawResource(R.raw.default_newton));
		}
		catch (Exception e)
		{
			Logger.error(activity, e);
		}
	}

	public void export(final MainActivity activity)
	{
		final SharedPreferences settings = activity.getSharedPreferences(STORAGE_SETTINGS, 0);
		String[] items = settings.getAll().keySet().toArray(new String[0]);
		Arrays.sort(items);

		SelectionDialog dialog = new SelectionDialog(activity, R.string.menu_export, items, true)
		{
			@Override
			public boolean onSelection(int which)
			{
				Intent intent = new Intent(activity.getBaseContext(), FileSelection.class);
				intent.putExtra(FileSelection.TITLE, activity.getString(R.string.menu_export) + ": " + selections[which]);
				intent.putExtra(FileSelection.START_PATH, Environment.getExternalStorageDirectory().getPath());
				intent.putExtra(FileSelection.FILE_NAME, normalizeFileName(selections[which] + ".lja"));
				intent.putExtra(SETTINGS_NAME, selections[which]);
				activity.startActivityForResult(intent, MainActivity.REQUEST_EXPORT);
				return true;
			}
		};
		dialog.show();
	}

	public void import_(final MainActivity activity)
	{
		Intent intent = new Intent(activity.getBaseContext(), FileSelection.class);
		intent.putExtra(FileSelection.TITLE, activity.getString(R.string.menu_import));
		intent.putExtra(FileSelection.START_PATH, Environment.getExternalStorageDirectory().getPath());
		intent.putExtra(FileSelection.SELECTION_MODE, FileSelection.MODE_OPEN);
		activity.startActivityForResult(intent, MainActivity.REQUEST_IMPORT);
	}

	private String normalizeFileName(String fileName)
	{
		StringBuffer normalized = new StringBuffer();
		for (int i = 0; i < fileName.length(); i++)
		{
			char c = fileName.charAt(i);
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == ' ' || c == '.')
			{
				normalized.append(c);
			}
		}
		return normalized.toString();
	}

	public void addPredefined()
	{
		// Only add if version changed
		int version = 0;
		try
		{
			version = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
		}
		catch (NameNotFoundException e)
		{
		}
		SharedPreferences prefs = activity.getSharedPreferences(STORAGE_SETTINGS_VERSION, 0);
		if (prefs.contains(SETTINGS_VERSION) && prefs.getInt(SETTINGS_VERSION, -1) == version)
		{
			return;
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(SETTINGS_VERSION, version);
		editor.commit();

		// Add the predefined settings
		forceAddPredefined();
	}

	public void forceAddPredefined()
	{
		String currentPreset = Storage.serialize();

		importFromResource(R.raw.elysian_fields);
		importFromResource(R.raw.filigree_entities);
		importFromResource(R.raw.floral_stage);
		importFromResource(R.raw.foreign_planet_surface);
		importFromResource(R.raw.luscious_uranus_fern);
		importFromResource(R.raw.planetary_vein);
		importFromResource(R.raw.space_travel);
		importFromResource(R.raw.old_fossil);
		importFromResource(R.raw.desert_valley);
		importFromResource(R.raw.wraith);
		importFromResource(R.raw.distant_view);
		importFromResource(R.raw.classic_aabab);
		importFromResource(R.raw.classic_ab);
		importFromResource(R.raw.classic_zircon_zity);
		importFromResource(R.raw.zircon_zity_night);
		importFromResource(R.raw.autumn_leaves);
		importFromResource(R.raw.blood_cells);
		importFromResource(R.raw.brass_belt);
		importFromResource(R.raw.curves);
		importFromResource(R.raw.gilligans_island);
		importFromResource(R.raw.green_crown);
		importFromResource(R.raw.japanese_garden);
		importFromResource(R.raw.neon_eye);
		importFromResource(R.raw.saturn_rings);
		importFromResource(R.raw.witch_fire);
		importFromResource(R.raw.angel_dance);
		importFromResource(R.raw.butterflies);
		importFromResource(R.raw.cthulhu);
		importFromResource(R.raw.flowers);
		importFromResource(R.raw.glowing_wing);
		importFromResource(R.raw.insect_swarm);
		importFromResource(R.raw.nosferatu);
		importFromResource(R.raw.souls_ascent);
		importFromResource(R.raw.the_prayer);
		importFromResource(R.raw.aqua_jewel);
		importFromResource(R.raw.classic_newton);
		importFromResource(R.raw.escape_from_hades);
		importFromResource(R.raw.giant_blossom);
		importFromResource(R.raw.heart_and_crown);
		importFromResource(R.raw.king_bat);
		importFromResource(R.raw.royal_coat_of_arms);
		importFromResource(R.raw.swirling_crosses);
		importFromResource(R.raw.thorns_island);
		importFromResource(R.raw.secret_seal);
		importFromResource(R.raw.batic_flowers);
		importFromResource(R.raw.made_of_wax);

		Storage.deserialize(currentPreset);
	}
}
