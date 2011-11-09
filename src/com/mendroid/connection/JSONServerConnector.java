package com.mendroid.connection;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.BasicHttpParams;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mendroid.sky.Base64;

import android.os.AsyncTask;

public class JSONServerConnector<T extends Serializable> extends ServerConnector {

	
	private final JSONRequestCallbackListener<T> listener;
	private final Class<T> resultClass;

	private JSONRequestTask currentTask;


	public JSONServerConnector(String serverURI, String UserID, int timeout, Class<T> resultClass, 
			JSONRequestCallbackListener<T> listener) {
		super(serverURI, UserID, timeout);
		this.listener = listener;
		this.resultClass = resultClass;
		currentTask = null;
	}

	@SuppressWarnings("unchecked")
	protected void callback(Object result, int errorCode) {
		listener.onJSONRequestCallback((T) result, errorCode);
	}

	protected void startRequest(String... requestData) throws IllegalStateException {
			currentTask = new JSONRequestTask();
			currentTask.execute(requestData);
	}

	protected void stopRequest() throws IllegalStateException {
			currentTask.cancel(true);
	}

	private class JSONRequestTask extends AsyncTask<String, Void, T> {

		private int errorCode;

		@Override
		protected void onPostExecute(T result) {
			callback(result, errorCode);
		}
		
		@Override
		protected void onCancelled() {
			callback(null, ERR_ABORTED);
		}
		
		@Override
		protected T doInBackground(String... params) {
			
			errorCode = ERR_UNKNOWN;
			
			String B64Code = postRequest(params);		
			if (B64Code == null) {
				return null;
			}
			if (isCancelled()) {
				return null;
			}
			
			String json = decodeAndUnZIP(B64Code);
			if (json == null) {
				return null;
			}
			if (isCancelled()) {
				return null;
			}
			
			Gson gson = new Gson();
			T result;
			
			try {
				result = gson.fromJson(json, resultClass);
			} catch (JsonParseException pe) {
				errorCode = ERR_PARSING_FAILED;
				return null;
			}
			
			
			errorCode = ERR_SUCCESS;
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
						params.length + 2);
				nameValuePairs.add(new BasicNameValuePair("REQUEST_LENGTH",
						String.valueOf(params.length)));
				nameValuePairs.add(new BasicNameValuePair("USER",
						user));

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
		
		private String decodeAndUnZIP(String code) {
			
			String reJSON = "";

			try {
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
				errorCode = ERR_DECODING_FAILED;
				return null;
			}
			return reJSON;
		}

	};

}
