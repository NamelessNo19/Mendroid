package com.mendroid.sky;

import com.mendroid.sky.R;
//import com.mendroid.structures.FoodAttribute;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class MyArrayAdapter extends ArrayAdapter<ListElementContainer> {
	
	private final Activity context;
	private final ListElementContainer[] elements;

	
	public MyArrayAdapter(Activity context, ListElementContainer[] elements) {
		super(context, R.layout.elementlay, elements);
		this.context = context;
		this.elements = elements;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View rowView = convertView;
		
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.elementlay, null, true);
		}
		
		TextView rowLabel = (TextView)rowView.findViewById(R.id.label);
		ImageView rowIcon = (ImageView)rowView.findViewById(R.id.icon);
		
		
		
		final ListElementContainer curElement = elements[position];
		rowIcon.setVisibility(View.GONE);


		try {
		
		if(curElement.isLine()) {
			rowLabel.setText(curElement.getName());
			rowLabel.setTextColor(Color.RED);
		} else {
			if (curElement.getDish() == null) {
				rowLabel.setText(" - GESCHLOSSEN - ");
				rowLabel.setTextColor(Color.GRAY);	
			} else {
				rowLabel.setText(curElement.getName());
				rowLabel.setTextColor(Color.WHITE);
				
				/*
				if(curElement.getDish().hasAttribute(FoodAttribute.AT_S)) {
					rowIcon.setVisibility(View.VISIBLE);
					rowIcon.setImageResource(R.drawable.pig);
				} else if (curElement.getDish().hasAttribute(FoodAttribute.AT_VEG)) {
					rowIcon.setVisibility(View.VISIBLE);
					rowIcon.setImageResource(R.drawable.carrot);
				} else if (curElement.getDish().hasAttribute(FoodAttribute.AT_VG)) {
					rowIcon.setVisibility(View.VISIBLE);
					rowIcon.setImageResource(R.drawable.crop);
				} else if (curElement.getDish().hasAttribute(FoodAttribute.AT_R)) {
					rowIcon.setVisibility(View.VISIBLE);
					rowIcon.setImageResource(R.drawable.cow);
				} */
				
			}
		} } catch (Exception e) {
			Log.w("Mendroid", "Exception Caught!: " + e.getLocalizedMessage());
			rowLabel.setText("E!");
		}
		
		return rowView;

	
		
	}


}
