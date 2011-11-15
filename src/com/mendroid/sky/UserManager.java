package com.mendroid.sky;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.mendroid.connection.JSONRequestCallbackListener;
import com.mendroid.connection.JSONServerConnector;
import com.mendroid.connection.ServerConnector;
import com.mendroid.structures.MendroidUser;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;

public class UserManager implements JSONRequestCallbackListener<MendroidUser> {
	
	private boolean loggedIn;
	private long userID;
	private Context cxt;
	private MendroidUser user;
	private boolean initialized;
	private UserRequestCallbackListener listener;
	
	private static UserManager singleton;
	
	public static int ERR_SUCCESS = 0;
	public static int ERR_CONNECTION_FAILED = 1;
	public static int ERR_NOT_REGISTERED = 2;
	
	
	private UserManager() {
		Log.d("Mendroid", "Creating User Manager");
		loggedIn = false;
		userID = 0;
		initialized = false;
		user = null;
		listener = null;
		singleton = this;
		
	}
	
	private static UserManager getSingleton() {
		return (singleton == null) ? new UserManager() : singleton;		
	}
	
	public static void init(Context context) {
		Log.d("Mendroid", "Initializing User Manager");
		UserManager snglt = getSingleton();
		snglt.cxt = context;
		snglt.userID = getSystemID();
		loadPreferences(snglt);
		snglt.initialized = true;
	}
	
	private static void loadPreferences(UserManager um) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(um.cxt);
		
		final String keyILI = um.cxt.getString(R.string.KEY_ISLOGGEDIN);
		final String keyHasPerm = um.cxt.getString(R.string.KEY_HASUPERM);
		final String keyNickName = um.cxt.getString(R.string.KEY_NICKNAME);
		
		
		if (preferences.getBoolean(keyILI, false)) {
			um.loggedIn = true;
			final String name = preferences.getString(keyNickName, "!ERROR");
			final boolean uPerm = preferences.getBoolean(keyHasPerm, false);
			um.user = new MendroidUser(um.userID, name, uPerm);
		}
	}

	public static long getUID() {
		return getSingleton().userID;
	}
	
	public static boolean isLoggedIn() {
		return getSingleton().loggedIn;
	}
	
	public static boolean isInitialized() {
		return getSingleton().initialized;
	}
	
	public static MendroidUser getUser() {
		return getSingleton().user;
	}


	
	private static long getSystemID() {
		String andID = Secure.getString(getSingleton().cxt.getContentResolver(),
				Secure.ANDROID_ID);
		if (andID == null || andID == "") {
			andID = "EMULATOR";
			Log.w("Mendroid", "Invalid Android ID, assuming emulator.");
		}
		final String md5Str = md5(andID);
		BigInteger id = new BigInteger(md5Str, 16);
		return id.longValue();
	}

	private static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			Log.e("Mendroid", "MD5 ERROR: No such Algorithm!");
		}
		return "";
	}
	
	public static void loadFromServer(UserRequestCallbackListener lstnr) {
		UserManager um = getSingleton();
		
		if (!um.initialized) {
			Log.e("Mendroid", "Request to uninitialized UserManager");
			throw new IllegalStateException("UserManager not initilaized");
		}
		
		um.listener = lstnr;
		
		Log.d("Mendroid", "Fetching user info");
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(um.cxt);
		String host = preferences.getString(um.cxt.getString(R.string.KEY_DEF_HOST),
				um.cxt.getString(R.string.default_host));

		String uri = "http://" + host + "/mendroidbackend/request/";
		
		JSONServerConnector<MendroidUser> reqCon = new JSONServerConnector<MendroidUser>(
				uri, String.valueOf(um.userID), 10000, MendroidUser.class, um);
		String[] request = new String[2];
		request[0] = "REQ_GET_USER";
		request[1] = String.valueOf(um.userID);
		reqCon.request(request);
	}

	@Override
	public void onJSONRequestCallback(MendroidUser result, int errorCode) {
		if (!getSingleton().initialized) {
			Log.e("Mendroid", "Unexpected JSON Callback in UserManager");
			return;
		}
		
		Log.d("Mendroid", "User Request returned " + String.valueOf(errorCode));
		if (errorCode == ServerConnector.ERR_SUCCESS && result != null) {
			if (result.getID() == 0) {
				storeUser(null);
				singleton.listener.onUserRequestCallback(null, ERR_NOT_REGISTERED);
			} else {
				storeUser(result);
				singleton.listener.onUserRequestCallback(result, ERR_SUCCESS);
			}
			
		} else {
			singleton.listener.onUserRequestCallback(null, ERR_CONNECTION_FAILED);
		}
	}
	
	public static void clear() {
		storeUser(null);
	}
	

	
	private static void storeUser(MendroidUser usr) {
		Log.d("Mendroid", "Storing user information");
		UserManager um = getSingleton();
		// Store locally
		
		if (usr == null) {
			um.loggedIn = false;
			um.user = null;
		} else {
			um.loggedIn = true;
			um.user = usr;
		}
		
		// Store in Preferences
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(um.cxt);
		final String keyILI = um.cxt.getString(R.string.KEY_ISLOGGEDIN);
		final String keyHasPerm = um.cxt.getString(R.string.KEY_HASUPERM);
		final String keyNickName = um.cxt.getString(R.string.KEY_NICKNAME);	
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putBoolean(keyILI, um.loggedIn);
		
		if (um.user == null) {
			editor.putString(keyNickName, "!ERROR");
			editor.putBoolean(keyHasPerm, false);	
		} else {
			editor.putString(keyNickName, um.user.getNickname());
			editor.putBoolean(keyHasPerm, um.user.hasUploadPermission());	
		}
		editor.commit();	
				
	}
	
	public interface UserRequestCallbackListener {
		public void onUserRequestCallback(MendroidUser result, int errorCode);	
	};


}
