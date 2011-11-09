package com.mendroid.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.mendroid.sky.Base64;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ImageServerConnector extends ServerConnector{


	
	private final ImageCallbackListener listener;
	private ImageRequestTask currentTask;

	
	
	private final static String imgReqCode = "REQ_IMAGE";

	public ImageServerConnector(String serverURI, String UserID, int timeout,
			ImageCallbackListener listener) {
		super(serverURI, UserID, timeout);
		this.listener = listener;
		currentTask = null;
	}

	

	protected void callback(Object result, int errorCode) {
		listener.onImageCallback((Bitmap) result, errorCode);
	}

	protected void startRequest(String... requestData) throws IllegalStateException {
			currentTask = new ImageRequestTask();
			currentTask.execute(requestData);
	}
	
	public void request() {
		this.request(new String[0]);
	}

	protected void stopRequest() throws IllegalStateException {
			currentTask.cancel(true);
	}

	private class ImageRequestTask extends AsyncTask<String, Void, Bitmap> {

		private int errorCode;

		
		@Override
		protected void onPostExecute(Bitmap result) {
			callback(result, errorCode);
		}

		@Override
		protected void onCancelled() {
			callback(null, ERR_ABORTED);
		}

		
		@Override
		protected Bitmap doInBackground(String... params) {

			errorCode = ERR_UNKNOWN;


			
			Log.v("Mendroid", "Image Request Started");
			
			String B64Code = postRequest(params);
			if (B64Code == null) {
				return null;
			}
			

			
			if (isCancelled()) {
				return null;
			}

			byte[] data;
			try {
				data = Base64.decode(B64Code, Base64.DEFAULT);
			} catch (IllegalArgumentException e) {
				errorCode = ERR_DECODING_FAILED;
				return null;
			}


			Bitmap result = BitmapFactory.decodeByteArray(data, 0, data.length);
			errorCode = (result == null) ? ERR_PARSING_FAILED
					: ERR_SUCCESS;
			
			return result;
		}

		private String postRequest(String... params) {

			String resStr = "";

			try {
				// Create a new HttpClient and Post Header
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
				HttpConnectionParams.setSoTimeout(httpParams, timeout);
				HttpClient httpclient = new DefaultHttpClient(httpParams);
				HttpPost httppost = new HttpPost(uri);

				// Add Request data
				List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(
						params.length + 3);
				nameValuePairs.add(new BasicNameValuePair("REQUEST_LENGTH",
						String.valueOf(params.length + 1)));
				nameValuePairs.add(new BasicNameValuePair("USER", user));
				nameValuePairs.add(new BasicNameValuePair("KEY_1", imgReqCode));

				for (int i = 0; i < params.length; i++) {
					nameValuePairs.add(new BasicNameValuePair("KEY_"
							+ String.valueOf(i + 2), params[i]));
				}
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);

				if (isCancelled()) {
					return null;
				}

				// Process Response

				InputStreamReader in = new InputStreamReader(response
						.getEntity().getContent());

				BufferedReader read = new BufferedReader(in);

				StringBuilder total = new StringBuilder();

				String line;
				while ((line = read.readLine()) != null) {
					total.append(line);
					total.append("\n");
					if (isCancelled()) {
						return null;
					}
				}
				
				resStr = total.toString();
				

			} catch (ConnectTimeoutException cte) {
				errorCode = ERR_TIMEOUT;
				return null;
			} catch (SocketTimeoutException ste) {
				errorCode = ERR_TIMEOUT;
				return null;
			} catch (IllegalArgumentException iae) {
				errorCode = ERR_INVALID_URI;
				return null;
			} catch (ClientProtocolException cpe) {
				errorCode = ERR_CONNECTION_FAILED;
				return null;
			} catch (IOException e) {
				errorCode = ERR_CONNECTION_FAILED;
				return null;
			}
			resStr.trim();
			return resStr;
		}

	};

}
