package com.mendroid.structures;

import java.io.Serializable;

public class MendroidUser implements Serializable {
	
	private static final long serialVersionUID = -7156901664927337157L;
	private final long ID;
	private final String nickname;
	private final boolean uploadPermission;
	
	public MendroidUser(long ID, String nickname, boolean uploadPermission) {
		this.ID = ID;
		this.nickname = nickname;
		this.uploadPermission = uploadPermission;
	}

	public long getID() {
		return ID;
	}

	public String getNickname() {
		return nickname;
	}

	public boolean hasUploadPermission() {
		return uploadPermission;
	}

}
