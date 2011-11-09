package com.mendroid.sky;

import com.mendroid.structures.DishStruct;
import com.mendroid.structures.MensaLines;

public class ListElementContainer {
	
	private final boolean bIsLine;
	private final MensaLines lineID;
	private final DishStruct mDish;
	
	public ListElementContainer(MensaLines lineId) {
		bIsLine = true;
		this.lineID = lineId;
		mDish = null;
	}
	
	public ListElementContainer(DishStruct dish) {
		bIsLine = false;
		this.lineID = null;
		mDish = dish;
	}
	
	public DishStruct getDish() {
		return mDish;
	}
	
	public MensaLines getLineId() {
		return lineID;
	}
	
	public boolean isLine() {
		return bIsLine;
	}
	
	public String getName() {
		if (bIsLine) {
			return lineID.getName();
		} else {
			return mDish.getName();
		}
	}

}
