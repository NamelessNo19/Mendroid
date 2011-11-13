package com.mendroid.sky;

import java.util.ArrayList;

import com.mendroid.structures.DishStruct;
import com.mendroid.structures.MensaLines;
import com.mendroid.structures.MensaList;
import com.mendroid.structures.MensaStruct;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;


public class MensaPagerAdapter extends PagerAdapter {
	
	private final MensaList mList;

	private final MensaView menView;
	
	public MensaPagerAdapter(MensaView menView, MensaList data) {
		
		if (data == null || data.getList().size() < 1) {
			throw new IllegalArgumentException("Data empty!");
		}
	
		mList = data;
		this.menView = menView;
	}

	@Override
    public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((ListView) view);
    }


	@Override
	public void finishUpdate(View arg0) {
		//menView.onDateSwiped();		
	}

	@Override
	public int getCount() {
		return mList.getList().size();
	}

	@Override
	public Object instantiateItem(View container, int position) {
		ListView lv = new ListView(menView);
		ListElementContainer[] liec = generateList(mList.getList().get(position));
		Log.d("Mendroid", "Instantiating " + String.valueOf(position + 1) + ". list.");
		lv.setAdapter(new MyArrayAdapter(menView, liec));
		lv.setOnItemClickListener(menView);
		
		((ViewPager) container).addView(lv, 0);

		return lv;
	}

	@Override
	 public boolean isViewFromObject(View view, Object object) {
		return view==((ListView)object);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private ListElementContainer[] generateList(MensaStruct mMensa) {

		ArrayList<ListElementContainer> buffer = new ArrayList<ListElementContainer>();
		for (MensaLines curLine : MensaLines.values()) {
			buffer.add(new ListElementContainer(curLine));

			if (mMensa.getLines()[curLine.ordinal()].isClosed()) {
				buffer.add(new ListElementContainer((DishStruct) null));
			} else {
				DishStruct[] dishes = mMensa.getLines()[curLine.ordinal()]
						.getDishes();
				for (DishStruct curDish : dishes) {
					buffer.add(new ListElementContainer(curDish));
				}
			}

		}

		ListElementContainer[] liec = new ListElementContainer[buffer.size()];
		buffer.toArray(liec);
		return liec;

	}

}
