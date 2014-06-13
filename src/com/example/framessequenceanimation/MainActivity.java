package com.example.framessequenceanimation;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private FramesSequenceAnimation framesSequenceAnimation  = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ImageView imageView = (ImageView)findViewById(R.id.screen_view);
		imageView.setImageResource(R.drawable.ac_screen);
		onShowAnimByFrame();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	
	private void onShowAnimByFrame() {
		ImageView image  = (ImageView)findViewById(R.id.screen_view_anim);
		framesSequenceAnimation = new FramesSequenceAnimation(this,image, R.array.feed_icons, 30);
//		framesSequenceAnimation.setOneShot(true);
		framesSequenceAnimation.start();
	}
	
}
