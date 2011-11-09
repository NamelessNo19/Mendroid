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

import android.os.AsyncTask;
import android.util.Log;


public class SimpleServerConnector extends ServerConnector {
	
	private SimpleRequestTask currentTask;
	private SimpleRequestCallbackListener listener;
	
	
	public SimpleServerConnector (String serverURI, String UserID, int timeout, SimpleRequestCallbackListener listener) {
		super(serverURI, UserID, timeout);
		currentTask = new SimpleRequestTask();
		this.listener = listener;
	}

	@Override
	protected void startRequest(String... requestData) {
		currentTask = new SimpleRequestTask();
		currentTask.execute(requestData);
	}

	@Override
	protected void stopRequest() {
		currentTask.cancel(true);
	}

	@Override
	protected void callback(Object result, int errorCode) {
		listener.onSimpleRequestCallback((String) result, errorCode) ;
	}
	
	private class SimpleRequestTask extends AsyncTask<String, Void, String> {

		private int errorCode;

		
		@Override
		protected void onPostExecute(String result) {
			callback(result.trim(), errorCode);
		}

		@Override
		protected void onCancelled() {
			callback(null, ERR_ABORTED);
		}

		
		@Override
		protected String doInBackground(String... params) {

			errorCode = ERR_UNKNOWN;


			
			Log.v("Mendroid", "Simple Request Started");
			
			String result = postRequest(params);
			
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
						String.valueOf(params.length)));
				nameValuePairs.add(new BasicNameValuePair("USER", user));

				for (int i = 0; i < params.length; i++) {
					nameValuePairs.add(new BasicNameValuePair("KEY_"
							+ String.valueOf(i + 1), params[i]));
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

				Log.d("Mendroid", "Getting response");
				String line;
				while ((line = read.readLine()) != null) {
					total.append(line);
					Log.v("Mendroid", "Server: " + line);
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
			errorCode = ERR_SUCCESS;
			
			resStr.trim();
			return resStr;
		}

	};

}
