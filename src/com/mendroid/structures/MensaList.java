package com.mendroid.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class MensaList implements Serializable, Iterable<MensaStruct> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 153726494027309291L;
	private Date updated;
	private ArrayList<MensaStruct> mensae;
	
	public MensaList() {
		mensae = new ArrayList<MensaStruct>();
		updated = new Date();
	}
	
	public void add(MensaStruct struct) {
		mensae.add(struct);
	}
	
	public Date getLastUpdate() {
		return updated;
	}
	
	@Override
	public Iterator<MensaStruct> iterator() {
		return mensae.iterator();
	}
	
	public ArrayList<MensaStruct> getList() {
		return mensae;
	}
	
	public void update() {
		updated = new Date();
	}
	
	public void setUpdate(Date date) {
		updated = date;
	}

	public MensaStruct getByDay(Date d) {
		final int index = getIndexByDay(d);
		if (index < 0) {
			return null;
		} else {
			return mensae.get(index);
		}
	}
	
	public int getIndexByDay(Date d) {
		int i = 0;
		for (MensaStruct tmp : mensae) {
			if (tmp.getDay().getYear() == d.getYear()
					&& tmp.getDay().getMonth() == d.getMonth()
					&& tmp.getDay().getDate() == d.getDate()) {
				return i;
			}
			i++;
		}
		return -1;
	}
		
	
}
