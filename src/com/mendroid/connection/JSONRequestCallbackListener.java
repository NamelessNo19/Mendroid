package com.mendroid.connection;

import java.io.Serializable;

public interface JSONRequestCallbackListener<T extends Serializable> {
	
	public void onJSONRequestCallback(T result, int errorCode);

}
