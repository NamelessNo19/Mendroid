package com.mendroid.sky;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mendroid.structures.DishStruct;
import com.mendroid.structures.MensaLines;
import com.mendroid.structures.MensaList;
import com.mendroid.structures.MensaStruct;

public class MensaView extends ListActivity {

	private static final String[] errorString = { "ERROR: No Data" };
	private ListElementContainer[] liec;
	private boolean firstShow;

	static final int DATE_DIALOG_ID = 0;

	private int mYear;
	private int mMonth;
	private int mDay;
	private boolean customTitle;

	@SuppressWarnings("unused")
	private TextView titleTvLeft;
	private TextView titleTvRight;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Mendroid", "MV Created!");
		firstShow = true;

		CacheManager.setDirectory(getCacheDir());

		MensaStruct mMensa = (MensaStruct) getIntent().getExtras()
				.getSerializable("MENSA");
		Log.v("Mendroid", "Mensa loaded");
		customTitle = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		if (mMensa == null) {
			this.setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, errorString));

		} else {

			Log.d("Mendroid", "Got Mensa of " + mMensa.getDay().toString());
			mYear = mMensa.getDay().getYear() + 1900;
			mMonth = mMensa.getDay().getMonth();
			mDay = mMensa.getDay().getDate();

			if (customTitle) {

				titleTvLeft = (TextView) findViewById(R.id.titleTvLeft); // Necessary;
																			// no
																			// idea
																			// why.
				getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
						R.layout.titlelayout);
				titleTvRight = (TextView) findViewById(R.id.titleTvRight);

				generateTitle();
			}
			Log.v("Mendroid", "Title set");
			generateList(mMensa);
			Log.v("Mendroid", "List generated");
			if (liec == null) {
				Log.w("Mendroid", "LIEC null!");
			} else {
				for (ListElementContainer i : liec) {
					if (i == null) {
						Log.w("Mendroid", "LIEC Element null!");
					}
				}
			}
			this.setListAdapter(new MyArrayAdapter(this, liec));
			Log.v("Mendroid", "List set");
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		Log.v("Mendroid", "MV resume");

		if (firstShow) {
			Log.v("Mendroid", "First show");
			Date d = (Date) getIntent().getExtras().getSerializable("DATE");

			SimpleDateFormat df = new SimpleDateFormat("dd.M HH:mm");
			Log.v("Mendroid", "Got date");
			Toast.makeText(this,
					getString(R.string.MSG_UPDATED) + df.format(d),
					Toast.LENGTH_SHORT).show();
			Log.v("Mendroid", "Toast created");
			firstShow = false;

		}
	}

	private void generateList(MensaStruct mMensa) {

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

		liec = new ListElementContainer[buffer.size()];
		buffer.toArray(liec);

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		ListElementContainer curCont = (ListElementContainer) getListAdapter()
				.getItem(position);

		if (curCont != null && !curCont.isLine() && curCont.getDish() != null) {
			Intent it = new Intent(this, DishDetail.class);
			it.putExtra("DISH", curCont.getDish());
			startActivity(it);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v("Mendroid", "Inflating menu");
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.viewmen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.it_del_cache:
			deleteCache();
			return true;
		case R.id.it_pick_date:
			Log.d("Mendroid",
					"Date Dialog called: " + String.valueOf(mDay) + "-"
							+ String.valueOf(mMonth) + "-"
							+ String.valueOf(mYear));
			showDialog(DATE_DIALOG_ID);
			return true;
		case R.id.it_quit:
			finish();
			return true;
		case R.id.it_pref:
			startActivity(new Intent(this, Preferences.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void deleteCache() {

		deleteImageCache();

		if (CacheManager.delete()) {
			Toast.makeText(this, getString(R.string.MSG_CACHE_DELETED),
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.ERR_DEL_CACHE),
					Toast.LENGTH_SHORT).show();
		}

	}

	private void deleteImageCache() {
		File imgCache = getDir("imagecache", Context.MODE_PRIVATE);
		for (File file : imgCache.listFiles()) {
			file.delete();
		}
	}

	

	private void changeDate(Date d) {
		MensaList cache = CacheManager.load();

		if (cache != null) {
			MensaStruct mMensa = cache.getByDay(d);
			if (mMensa == null) {
				Toast.makeText(this, getString(R.string.MSG_NO_DATA),
						Toast.LENGTH_SHORT).show();
			} else {
				generateList(mMensa);
				this.setListAdapter(new MyArrayAdapter(this, liec));
				mYear = d.getYear() + 1900;
				mMonth = d.getMonth();
				mDay = d.getDate();
				generateTitle();
			}
		}

	}

	private void generateTitle() {
		if (customTitle) {
			if (titleTvRight != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("E dd. MMM");
				Calendar cal = Calendar.getInstance();
				cal.clear();
				cal.set(mYear, mMonth, mDay);
				titleTvRight.setText(sdf.format(cal.getTime()));
			} else {
				Log.w("Mendroid", "Failed to access titlebar.");
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,
					mDay);
		}
		return null;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			changeDate(new Date(year - 1900, monthOfYear, dayOfMonth));
		}
	};

}
