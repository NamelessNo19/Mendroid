package com.mendroid.structures;

import java.io.Serializable;
import java.util.Date;




public class MensaStruct implements Comparable<MensaStruct>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6296402084342572855L;
	/**
	 * 
	 */

	private FoodLineStruct[] lines;
	private Date day;
	
	public MensaStruct() {
		lines = null;
		day = null;
	}

	public MensaStruct(Date day, FoodLineStruct[] lines) {
		this.day = day;
		this.lines = lines;
	}

	public FoodLineStruct[] getLines() {
		return lines;
	}

	public Date getDay() {
		return day;
	}


	@Override
	public boolean equals(Object o) {
		return o instanceof MensaStruct && this.day.equals(((MensaStruct) o).day);
	}

	@Override
	public int compareTo(MensaStruct o) {
		return this.day.compareTo(o.day);
	}



}