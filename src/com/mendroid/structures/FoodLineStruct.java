package com.mendroid.structures;

import java.io.Serializable;




public class FoodLineStruct implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2055428766934342792L;
	private boolean closed;
	private DishStruct[] dishes;
	
	
	
	public FoodLineStruct() {
		closed = true;
		dishes = null;
	}
	
	public FoodLineStruct(boolean closed, DishStruct[] dishes) {
		this.closed = closed;
		this.dishes = dishes;
	}
	

	public boolean isClosed() {
		return closed;
	}

	public DishStruct[] getDishes() {
		return dishes;
	}
	
	

}
