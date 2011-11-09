package com.mendroid.sky;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class PictureView extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Mendroid", "Creating showImage");
		
		setContentView(R.layout.pictureviewlay);
		ImageView img = (ImageView)findViewById(R.id.picture);
		
		final String path = getCacheDir().getPath() + "download.jpg";
		File jpg = new File(path);
		
		if (jpg.exists()) {
			Bitmap bmp = BitmapFactory.decodeFile(path);
			if (bmp != null) {
				img.setImageBitmap(bmp);
			}
		}
		
	}

}
