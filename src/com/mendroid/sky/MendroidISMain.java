package com.mendroid.sky;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;

import com.mendroid.sky.R;
import com.mendroid.structures.MensaStruct;
import com.mendroid.structures.MensaList;

//import android.util.Base64;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MendroidISMain extends Activity {

	public final static String PATH = "/mendroidbackend/data";
	public final static String CACHE_FILE = "cache.bin";

	private final static int UPDATE_FREQ_MIN = 60;

	private final static int DIALOG_NODATA_ID = 0;
	private final static int DIALOG_MESSAGE_ID = 1;

	private TextView splashOut;
	private ProgressBar pgBar;

	private MensaList myMensa;

	private boolean hasOnlineData;
	private boolean hasCacheData;
	private boolean hasNetwork;

	private String serverMessage;
	
	private boolean prefLocked;
	
	SharedPreferences preferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Mendroid", "MendroidISMain created.");

		// Force Portait Orientation
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Open Splash screen
		setContentView(R.layout.splashscreen);

	
		// Find GUI Elements
		splashOut = (TextView) findViewById(R.id.splashOut);
		pgBar = (ProgressBar) findViewById(R.id.progressBar1);

		// Initialize Variables
		hasOnlineData = false;
		hasCacheData = false;
		prefLocked = true;
		
		PreferenceManager.setDefaultValues(this, R.xml.mainprefs, false);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Check for Internet connection
		checkConnection();

		// Try to load Cache
		myMensa = loadCache();
		hasCacheData = (myMensa != null);
		serverMessage = null;

		// Check cache age
		boolean cacheOutdated = true;
		if (hasCacheData) {
			final Calendar c = Calendar.getInstance();
			long diff = c.getTimeInMillis() - myMensa.getLastUpdate().getTime();
			cacheOutdated = diff > UPDATE_FREQ_MIN * 60000;
		}

		if (cacheOutdated && hasNetwork) {
			String host = preferences.getString(getString(R.string.KEY_DEF_HOST), getString(R.string.default_host));
			
			Log.v("Mendroid", "Initiating download from: " + host + PATH);
			URL[] params = new URL[1];

			try {
				params[0] = new URL("http://" + host + PATH + "/" + getString(R.string.svr_ver_code));
			} catch (MalformedURLException e) {
				Log.w("Mendroid",
						"Malformed URL Exception: " + e.getLocalizedMessage());
				gotData();
			}

			new DownloaderTask(this, splashOut).execute(params);

		} else {
			gotData();
		}
				
	}

	@Override
	public void onRestart() {
		super.onRestart();
		// End Application
		Log.d("Mendroid", "MendroidISMain restarted. Finishing.");
		this.finish();
	}

	public void gotData() {
		Log.v("Mendroid", "gotData called");
		

		// Stop progress bar
		pgBar.setVisibility(View.INVISIBLE);

		// Checking available Data
		if (!hasNetwork) {
			splashOut.setText(getString(R.string.MSG_NO_CON));
		} else if (!hasOnlineData) {
			splashOut.setText(getString(R.string.MSG_DOWNLOAD_FAILED));
		}


		if (serverMessage != null && serverMessage.length() > 0) {
			// Showing Message dialog first...
			showDialog(DIALOG_MESSAGE_ID);
		} else {
			// or start immediately
			startView();
		}
		
		prefLocked = false;

	}

	private void startView() {
		
		if (!hasCacheData && !hasOnlineData) {
			// No Data
			showDialog(DIALOG_NODATA_ID);
			return;
		}
		
		
		splashOut.setText(getString(R.string.MSG_STARTING));
		final Calendar today = Calendar.getInstance();
		MensaStruct todaysMensa = myMensa.getByDay(today.getTime());

		if (todaysMensa == null) {
			// ... Earliest otherwise
			todaysMensa = myMensa.getList().get(0);
		}

		// Starting Listview activity
		Intent it = new Intent(this, MensaView.class);
		it.putExtra("MENSA", todaysMensa);
		Log.d("Mendroid", "Putting Mensa of " + todaysMensa.getDay().toString());
		it.putExtra("DATE", myMensa.getLastUpdate());
		Log.d("Mendroid", "Calling MensaView");
		startActivity(it);
	}

	/* Rewrite cache file */
	private boolean saveCache(MensaList myMensa) {
		Log.v("Mendroid", "Saving Cache");
		final String filename = getCacheDir().getPath() + CACHE_FILE;

		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		splashOut.setText(getString(R.string.MSG_WRITING_CACHE));

		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(myMensa);
			out.close();
		} catch (IOException e) {
			splashOut.setText(getString(R.string.ERR_CACHE_WRITE));
			Log.w("Mendroid", "IO Exception while Saving Cache");
			return false;
		}

		return true;
	}

	private MensaList loadCache() {
		Log.v("Mendroid", "Loading cache");
		final String filename = getCacheDir().getPath() + CACHE_FILE;

		File f = new File(filename);

		splashOut.setText(getString(R.string.MSG_LOADING_CACHE));

		if (!f.exists()) {
			// No cache
			Log.v("Mendroid", "No cache found.");
			return null;
		}

		FileInputStream fis = null;
		ObjectInputStream in = null;
		MensaList data = null;

		try {
			fis = new FileInputStream(filename);
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

	public void parse(String code) {
		hasOnlineData = false;

		// Intercept Messages

		boolean isValid = true;
		serverMessage = null;

		String[] data = code.split("<<");

		for (int i = 1; i < data.length; i++) {
			if (data[i].startsWith("INVALID")) {
				isValid = false;
			} else if (data[i].startsWith("MSG:")) {
				serverMessage = data[i].substring(4);
			}
		}

		if (isValid) {
			String[] params = new String[1];
			params[0] = data[0];
			new ParserTask().execute(params);
		} else {
			gotData();
		}
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		switch (id) {
		case DIALOG_NODATA_ID:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.MSG_NO_DATA));
			builder.setMessage(getString(R.string.MSG_UN_RET_DATA));
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setPositiveButton(getString(R.string.MSG_OK), null);
			return builder.create();
		case DIALOG_MESSAGE_ID:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.MSG_SERVER_MSG));
			builder.setMessage(serverMessage);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setPositiveButton(getString(R.string.MSG_OK), new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startView();
				}
			});
			return builder.create();
		default:
			return null;
		}
	}

	private void checkConnection() {
		hasNetwork = false;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		if (cm != null) {
			NetworkInfo ni = cm.getActiveNetworkInfo();

			if (ni != null && ni.isConnected()) {
				hasNetwork = true;
			}
		}
	}
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
		  if (!prefLocked) {	
			startActivity(new Intent(this, Preferences.class));
		  }
			return true;
		}		
		return super.onKeyDown(keyCode, event);	
	}

	class ParserTask extends AsyncTask<String, Void, MensaList> {

		@Override
		protected void onPreExecute() {
			splashOut.setText(getString(R.string.MSG_PARSING));
		}

		@Override
		protected MensaList doInBackground(String... params) {

			MensaList newMensaCol = null;

			if (params[0] != null && params[0].length() > 0) {
				String json = decode(params[0]);
				Log.v("Mendroid", "Parsing");
				Gson gson = new Gson();
				newMensaCol = gson.fromJson(json, MensaList.class);
			}

			return newMensaCol;
		}

		private String decode(String code) {
			Log.v("Mendroid", "Decoding");

			String reJSON = "";

			try {
				// Decode and unzip
				ByteArrayInputStream bais = new ByteArrayInputStream(
				Base64.decode(code, Base64.DEFAULT));
				GZIPInputStream gzis = new GZIPInputStream(bais);
				InputStreamReader reader = new InputStreamReader(gzis, "UTF-8");
				BufferedReader in = new BufferedReader(reader);
				String readed;
				while ((readed = in.readLine()) != null) {
					reJSON += readed;
				}
			} catch (Exception e) {
				Log.w("Mendroid", "Exception while decoding.");
				return null;
			}
			return reJSON;
		}

		@Override
		protected void onPostExecute(MensaList result) {
			if (result != null) {
				Log.v("Mendroid", "Parsing succeeded");
				result.update();
				myMensa = result;
				saveCache(result);
				hasOnlineData = true;
			} else {
				Log.w("Mendroid", "Parsing failed");
			}
			gotData();
		}

	};

}