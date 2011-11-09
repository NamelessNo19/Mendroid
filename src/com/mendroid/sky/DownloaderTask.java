package com.mendroid.sky;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
//import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

class DownloaderTask extends AsyncTask<URL, Integer, String> {

	private final MendroidISMain caller;
	private final TextView splashOut;

	private final static int CON_TIMEOUT_SEC = 5;
	private final static int READ_TIMEOUT_SEC = 10;

	public DownloaderTask(MendroidISMain caller, TextView splashout) {
		Log.v("Mendroid", "Downloader task created.");
		this.caller = caller;
		this.splashOut = splashout;
	}

	@Override
	protected String doInBackground(URL... params) {
		Log.v("Mendroid", "Downloader task executed");

		/*
		 * publishProgress(-2);
		 * 
		 * if (!awaitConnection(MendroidISMain.HOST)) { Log.i("Mendroid",
		 * "No response from host, trying anyway...."); //return null; } else {
		 * Log.v("Mendroid", "Got response from host."); }
		 */

		InputStream is = null;
		BufferedReader bReader;
		String s;
		String res = "";

		Log.v("Mendroid", "Connecting.");
		publishProgress(-3);
		
		HttpURLConnection con = null;

		try {
			con = (HttpURLConnection) params[0]
					.openConnection();
			con.setConnectTimeout(CON_TIMEOUT_SEC * 1000);
			con.setReadTimeout(READ_TIMEOUT_SEC * 1000);
			is = new BufferedInputStream(con.getInputStream());
		} catch (SocketTimeoutException e) {
			Log.i("Mendroid", "Connection timed out" + e.getLocalizedMessage());
			return null;
		} catch (IOException e) {
			Log.w("Mendroid",
					"IO Exception whlie connecting: " + e.getLocalizedMessage());
			return null;

		}

		bReader = new BufferedReader(new InputStreamReader(is));
		publishProgress(0);

		Log.v("Mendroid", "Downloading " + String.valueOf(con.getContentLength()) + " Bytes.");
		try {
			while ((s = bReader.readLine()) != null) {
				res += s + "\n";
			}
		} catch (SocketTimeoutException e) {
			Log.i("Mendroid", "Download timed out" + e.getLocalizedMessage());
			return null;

		} catch (IOException e) {
			Log.w("Mendroid",
					"IO Exception whlie downloading: "
							+ e.getLocalizedMessage());
			return null;
		}
		Log.i("Mendroid", "Loaded " + String.valueOf(res.length())
				+ " Characters");

		return res;

	}

	/*
	 * protected boolean awaitConnection(String host) { Log.v("Mendroid",
	 * "Awaiting connection to " + host); InetAddress adr; try { adr =
	 * InetAddress.getByName(host); } catch (UnknownHostException e1) {
	 * Log.w("Mendroid", "Unkonwn Host Exception whlie waiting: " +
	 * e1.getLocalizedMessage()); return false; }
	 * 
	 * try { return adr.isReachable(TIMEOUT_SEC * 1000); } catch (IOException
	 * e1) { Log.w("Mendroid", "IO Exception whlie waiting: " +
	 * e1.getLocalizedMessage()); return false; } }
	 */

	protected void onProgressUpdate(Integer... progress) {

		Log.v("Mendroid", "Publishing progress: " + String.valueOf(progress[0]));

		if (progress[0] >= 0) {
			splashOut.setText(caller.getString(R.string.MSG_DOWNLOADING));
		} else {
			switch (progress[0]) {
			case -2:
				splashOut.setText(caller.getString(R.string.MSG_AW_RESP));
				break;
			case -3:
				splashOut.setText(caller.getString(R.string.MSG_CONNECTING));
				break;

			}

		}
	}

	@Override
	protected void onPostExecute(String result) {
		Log.v("Mendroid", "Downloader task finished.");

		if (result == null) {
			caller.gotData();
		} else {
			splashOut.setText(caller.getString(R.string.MSG_PARSING));
			caller.parse(result);
		}
	}

}
