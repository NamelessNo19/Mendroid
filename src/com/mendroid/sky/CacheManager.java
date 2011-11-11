package com.mendroid.sky;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.mendroid.structures.MensaList;

import android.util.Log;

public class CacheManager {
	
	private static CacheManager singleton;
	
	private String path;
	public final static String CACHE_FILE = "cache.bin";

	private CacheManager() {
		path = null;
		singleton = this;
	}
	
	
	private static CacheManager getSingleton() {
		return (singleton == null) ? new CacheManager() : singleton;
	}
	
	public static boolean exists() {
		if (getPath() == null) {
			Log.w("Mendroid", "No cache path set!");
			return false;
		} else {
			File f = new File(singleton.path + CACHE_FILE);
			return f.exists();
		}		
	}
	
	public static void setDirectory(File cacheDir) {
		getSingleton().path = cacheDir.getPath();
		Log.d("Mendroid", "Cache path set to " + cacheDir.getPath());
	}
	
	public static boolean save(MensaList data) {
		Log.v("Mendroid", "Saving Cache");
		
		if (getPath() == null) {
			Log.w("Mendroid", "No cache path set!");
			return false;
		}
		

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		try {
			fos = new FileOutputStream(singleton.path + CACHE_FILE);
			out = new ObjectOutputStream(fos);
			out.writeObject(data);
			out.close();
		} catch (IOException e) {
			Log.w("Mendroid", "IO Exception while Saving Cache");
			return false;
		}

		return true;
	}

	public static MensaList load() {
		Log.v("Mendroid", "Loading cache");

		
		if (!exists()) {
			// No cache
			Log.v("Mendroid", "No cache found.");
			return null;
		}
		
		FileInputStream fis = null;
		ObjectInputStream in = null;
		MensaList data = null;

		try {
			fis = new FileInputStream(singleton.path + CACHE_FILE);
			in = new ObjectInputStream(fis);
			data = (MensaList) in.readObject();
			in.close();
		} catch (IOException ex) {
			Log.w("Mendroid", "IO Exception while loading cache.");
			return null;
		} catch (ClassNotFoundException e) {
			Log.w("Mendroid", "CNF Exception while loading cache.");
			return null;
		}
		Log.v("Mendroid", "Cache loaded");
		return data;

	}
	
	public static boolean delete() {
		Log.v("Mendroid", "Deleting cache");
		if (!exists()) {
			return false;
		}
		File f = new File(getPath() + CACHE_FILE);
		return f.delete();
	}
	
	private static String getPath() {
		return getSingleton().path;
	}
	
}
