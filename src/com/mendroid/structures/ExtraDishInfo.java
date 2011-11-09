package com.mendroid.structures;

import java.io.Serializable;

public class ExtraDishInfo implements Serializable{


	/**
	 * 
	 */
	private static final long serialVersionUID = -4642059842182441847L;

	public boolean dishExists() {
		return dishExists;
	}

	public boolean hasImage() {
		return hasImage;
	}

	public boolean hasComment() {
		return hasComment;
	}

	public String getComments() {
		return comments;
	}

	private ExtraDishInfo() {
		this.dishExists = false;
		this.hasImage = false;
		this.hasComment = false;
		this.comments = "";
	}
	
    public ExtraDishInfo(boolean hasImage,
			boolean hasComment, String comments) {
		this.dishExists = true;
		this.hasImage = hasImage;
		this.hasComment = hasComment;
		this.comments = comments;
	}

	private final boolean dishExists;
	private final boolean hasImage;
	private final boolean hasComment;
	
	private final String comments;
	
	public static ExtraDishInfo getDummy() {
		return new ExtraDishInfo();
	}
	
	
}
