package com.mendroid.connection;

public abstract class ServerConnector {
	
	public static final int ERR_UNKNOWN = -1;
	public static final int ERR_SUCCESS = 0;
	public static final int ERR_ABORTED = 1;
	public static final int ERR_CONNECTION_FAILED = 2;
	public static final int ERR_PARSING_FAILED = 3;
	public static final int ERR_TIMEOUT = 4;
	public static final int ERR_INVALID_URI = 5;
	public static final int ERR_DECODING_FAILED = 6;
	
	protected final String uri;
	protected final String user;	
	protected final int timeout;
	
	private boolean busy;
	
	protected ServerConnector (String serverURI, String UserID, int timeout) {
		user = UserID;
		uri = serverURI;
		this.timeout = timeout;
		busy = false;
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public void request(String... requestData) throws IllegalStateException {
		if (busy) {
			throw new IllegalStateException(
					"Cannot handle more than one request at a time!");
		} else {
			busy = true;
			startRequest(requestData);
		}
	}
	
	public void abort() throws IllegalStateException {
		if (busy) {
			stopRequest();
		} else {
			throw new IllegalStateException("No request to abort!");
		}
	}
	
	public void onCallback(Object result, int errorCode) {
		busy = false;
		callback(result, errorCode);
	}
	
	abstract protected void startRequest(String... requestData);
	abstract protected void stopRequest();
	abstract protected void callback(Object result, int errorCode);
}
