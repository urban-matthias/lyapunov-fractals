package com.urban.app.fractal.ljapunow.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Storage
{
	private static final Properties	storage	= new Properties();

	public interface OnChangeListener
	{
		public void onStorageChange(String storageID);
	}

	public static void set(String name, String value)
	{
		storage.put(name, value);
	}

	public static String get(String name)
	{
		Object value = storage.get(name);
		return value == null ? "" : (String) value;
	}

	public static boolean has(String name)
	{
		Object value = storage.get(name);
		return value != null;
	}

	public static void load(String filePath) throws Exception
	{
		load(new FileInputStream(filePath));
	}

	public static void load(InputStream stream) throws Exception
	{
		storage.load(stream);
	}

	public static void save(String filePath, String comment) throws Exception
	{
		storage.store(new FileOutputStream(filePath), comment);
	}

	public static void save(String serializedStorage, String filePath, String comment) throws Exception
	{
		Properties tmp = new Properties();
		try
		{
			tmp.load(new ByteArrayInputStream(serializedStorage.getBytes()));
		}
		catch (IOException e)
		{
		}
		tmp.store(new FileOutputStream(filePath), comment);
	}

	public static String serialize()
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		storage.save(out, "");
		return out.toString();
	}

	public static void deserialize(String serializedStorage)
	{
		try
		{
			storage.load(new ByteArrayInputStream(serializedStorage.getBytes()));
		}
		catch (IOException e)
		{
		}
	}
}
