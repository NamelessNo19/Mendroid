package com.mendroid.connection;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

public class ImageUploader {

	public static final int ERR_UNKNOWN = -1;
	public static final int ERR_SUCCESS = 0;
	public static final int ERR_ABORTED = 1;
	public static final int ERR_CONNECTION_FAILED = 2;
	public static final int ERR_NO_PERMISSION = 4;
	public static final int ERR_INVALID_URI = 5;
	public static final int ERR_TOO_LARGE = 6;

	private static final int MAX_SIZE_KB = 200;

	private static final String PATH = "mendroidbackend/receptor/";

	private String sessionCode;
	private ImageUploaderCallbackListener listener;
	private final String host;

	
	public ImageUploader(String host, ImageUploaderCallbackListener listener) {
		this.listener = listener;
		if (host.endsWith("/")) {
			this.host = host;
		} else {
			this.host = host + "/";
		}
		sessionCode = "";
	}

	public void startUpload(Bitmap bmp, String sessionCode) {
		this.sessionCode = sessionCode;
		new UploaderTask().execute(bmp);
	}

	private class UploaderTask extends AsyncTask<Bitmap, Void, Integer> {

		private int errorCode;

		@Override
		protected void onPostExecute(Integer result) {
			listener.onImageUploadCallback(result);
		}

		@Override
		protected Integer doInBackground(Bitmap... params) {
			errorCode = ERR_UNKNOWN;
			Log.d("Mendroid", "Initiating Upload");

			Bitmap bmp = downscale(params[0]);
			final byte[] data = compress(bmp);
			if (data != null) {
				upload(data);
			}

			return errorCode;
		}
		
		// --- Downscale ---
		private Bitmap downscale(Bitmap bmp) {
			//final float ratio = (float)bmp.getWidth() / (float)bmp.getHeight();
			Bitmap ret = Bitmap.createScaledBitmap(bmp, 320, 240, true);
			return ret;
		}

		// --- Compress ---
		private byte[] compress(Bitmap bmp) {
	
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, data);
			Log.v("Mendroid",
					"JPEG size: " + String.valueOf(data.size() / 1024) + " KB");
			
			if (data.size() > MAX_SIZE_KB * 1024) {
				Log.v("Mendroid", "Exceeds Limit, compressing...");
				bmp.compress(Bitmap.CompressFormat.JPEG, 70, data);
				Log.d("Mendroid",
						"New JPEG size: " + String.valueOf(data.size() / 1024) + " KB");
				if (data.size() > MAX_SIZE_KB * 1024) {
					Log.d("Mendroid", "Image still too large. Aborting.");
					errorCode = ERR_TOO_LARGE;
					return null;
				}
				
			}

			return data.toByteArray();
		}

		// --- Upload ---
		private void upload(byte[] data) {
			HttpClient httpclient = new DefaultHttpClient();

			String uriWithSCode = "http://" + host + PATH;
		
			uriWithSCode += sessionCode;
			uriWithSCode = uriWithSCode.trim();
			Log.d("Mendroid", "Upload to: " + uriWithSCode);
			HttpPost httppost = null;

			try {
				httppost = new HttpPost(uriWithSCode);

				ByteArrayBody bin = new ByteArrayBody(data, "image/jpeg",
						"upload.jpg");

				MultipartEntity reqEntity = new MultipartEntity();
				reqEntity.addPart("bin", bin);

				httppost.setEntity(reqEntity);

				Log.d("Mendroid", "Uploading...");
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity resEntity = response.getEntity();
				Log.d("Mendroid", "Reading Response.");
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						resEntity.getContent()));
				String line;
				while ((line = rd.readLine()) != null) {
					Log.d("Mendroid", "SERVER: " + line);
					line = line.toLowerCase();
					if (line.equals("access denied")) {
						errorCode = ERR_NO_PERMISSION;
					} else if (line.equals("upload ok")) {
						errorCode = ERR_SUCCESS;
					} else if (line.equals("upload failed") && errorCode != ERR_UNKNOWN) {
						errorCode = ERR_UNKNOWN;
					}
				}
			} catch (IllegalArgumentException iae) {
				errorCode = ERR_INVALID_URI;
				Log.w("Mendroid", "Caught exception: " + iae.getMessage());
				return;
			} catch (ClientProtocolException cpe) {
				errorCode = ERR_CONNECTION_FAILED;
				Log.w("Mendroid", "Caught exception: " + cpe.getMessage());
				return;
			} catch (IOException ioe) {
				errorCode = ERR_CONNECTION_FAILED;
				Log.w("Mendroid", "Caught exception: " + ioe.getMessage());
				return;
			} catch (Exception ex) {
				Log.e("Mendroid", "Uncaught exception while uploading: " + ex.getMessage());
			}	
		}

	}

}
