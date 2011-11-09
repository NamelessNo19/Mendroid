package com.mendroid.structures;

import java.io.Serializable;



public class DishStruct implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6679818762351285080L;
	private String name;
	private String desc;
	private float price;
	private FoodAttribute[] attributes;
	
	public DishStruct() {
		name = null;
		desc = null;
		price = 0;
		attributes = null;
	}
	
	public DishStruct(String name, String desc, float price, FoodAttribute[] attributes) {
		 this.name = name;
		 this.desc = desc;
		 this.price = price;
		 this.attributes = attributes;
	}


	public String getName() {
		return name;
	}

	public String getDescription() {
		return desc;
	}
	
	public void setDescription(String newDesc) {
		desc = newDesc;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}

	public float getPrice() {
		return price;
	}

	public FoodAttribute[] getAttributes() {
		return attributes;
	}

	public boolean hasAttribute(FoodAttribute att) {
		if (attributes == null) {
			return false;
		}
		
		for(FoodAttribute tmp: attributes) {
			if (tmp == att) {
				return true;
			}
		}
		return false;
	}
	
	

}
