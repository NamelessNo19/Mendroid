package com.mendroid.sky;

import com.mendroid.structures.MendroidUser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class Preferences extends PreferenceActivity implements
		UserManager.UserRequestCallbackListener, OnPreferenceClickListener {

	private static final int CON_FAIL_DIA = 0;
	private static final int NOT_REG_DIA = 1;
	
	private Preference loginPref;
	private Preference nickPref;
	private Preference upermPref;
	
	private ProgressDialog pgDia;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.mainprefs);
		if (!UserManager.isInitialized()) {
			UserManager.init(getApplicationContext());
		}

		Preference uidPref = (Preference) getPreferenceScreen().findPreference(
				getString(R.string.KEY_USRID));
		uidPref.setSummary(String.valueOf(UserManager.getUID()));

		loginPref = (Preference) getPreferenceScreen().findPreference(
				getString(R.string.KEY_LOGIN));
		loginPref.setOnPreferenceClickListener(this);

		nickPref = (Preference) getPreferenceScreen().findPreference(
				getString(R.string.KEY_NICKNAME_BTN));
		upermPref = (Preference) getPreferenceScreen().findPreference(
				getString(R.string.KEY_UPERM_BTN));

		if (UserManager.isLoggedIn()) {
			loginPref.setTitle(R.string.CAP_LOGOFF);
			MendroidUser usr = UserManager.getUser();
			nickPref.setSummary(usr.getNickname());
			upermPref.setSummary((usr.hasUploadPermission()) ? R.string.MSG_YES
					: R.string.MSG_NO);
		} else {
			nickPref.setSummary("-");
			upermPref.setSummary("-");

		}

	}

	@Override
	public void onUserRequestCallback(MendroidUser result, int errorCode) {
		Log.d("Mendroid", "User request callback " + String.valueOf(errorCode));
		
		if (pgDia != null) {pgDia.dismiss();}

		if (errorCode == UserManager.ERR_SUCCESS) {
			Log.d("Mendroid", "Nickname " + result.getNickname());
			loginPref.setTitle(R.string.CAP_LOGOFF);
			MendroidUser usr = UserManager.getUser();
			nickPref.setSummary(usr.getNickname());
			upermPref.setSummary((usr.hasUploadPermission()) ? R.string.MSG_YES
					: R.string.MSG_NO);
		} else if (errorCode == UserManager.ERR_CONNECTION_FAILED) {
			showDialog(CON_FAIL_DIA);
		} else if (errorCode == UserManager.ERR_NOT_REGISTERED) {
			showDialog(NOT_REG_DIA);
		}

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == loginPref) {
			if (UserManager.isLoggedIn()) {
				Log.d("Mendroid", "Logging off.");
				nickPref.setSummary("-");
				upermPref.setSummary("-");
				loginPref.setTitle(R.string.CAP_LOGIN);
				UserManager.clear();
			} else {
				Log.d("Mendroid", "Logging in.");
				pgDia = ProgressDialog.show(this, "Anmelden",
						"Verbinde mit Server..", true);
				UserManager.loadFromServer(this);
			}
		}
		return false;
	}
	
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		switch (id) {
		case CON_FAIL_DIA:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Fehler");
			builder.setMessage("Verbindung fehlgeschlagen.");
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setPositiveButton(getString(R.string.MSG_OK), null);
			return builder.create();
		case NOT_REG_DIA:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Fehler");
			builder.setMessage("Benutzer ID unbekannt.");
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setPositiveButton(getString(R.string.MSG_OK), null);
			return builder.create();
		default:
			return null;
		}
	}
}