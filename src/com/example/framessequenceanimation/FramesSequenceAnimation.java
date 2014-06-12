package com.example.framessequenceanimation;

import java.lang.ref.SoftReference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.widget.ImageView;

public class FramesSequenceAnimation {

	private int [] animationFrames; // animation frames
	private int currentFrames;   // current frame
	private boolean shouldRun;   // true if the animation should continue running. Used to stop the animation
	private boolean isRunning;   // true if the animation currently running. prevents starting the animation twice
	private SoftReference<ImageView> mSoftReferenceImageView; // Used to prevent holding ImageView when it should be dead.
	private Handler handler;
	private int     delayMillis; // the gap of frames
	private int     framesResourceID;
	private boolean oneShot = false;
	private Context context;
	private FramesSequenceAnimationListener onAnimationStoppedListener;
    private Bitmap bitmap = null;
    private BitmapFactory.Options bitmapOptions;
    
    public FramesSequenceAnimation(Context context,ImageView imageView,int framesResourceID,int fps) {
    	this.context = context;
    	this.framesResourceID = framesResourceID;
        currentFrames = -1;
        shouldRun = false;
        isRunning = false;
        delayMillis = 1000 / fps;
        mSoftReferenceImageView = new SoftReference<ImageView>(imageView);
    	handler = new Handler();
    	getFramesResource();
        imageView.setImageResource(animationFrames[0]);
        
        // use in place bitmap to save GC work (when animation images are the same size & type)
        if (Build.VERSION.SDK_INT >= 11) {
            Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            Bitmap.Config config = bmp.getConfig();
            bitmap = Bitmap.createBitmap(width, height, config);
            bitmapOptions = new BitmapFactory.Options();
            // setup bitmap reuse options. 
            bitmapOptions.inBitmap = bitmap; // reuse this bitmap when loading content
            bitmapOptions.inMutable = true;
            bitmapOptions.inSampleSize = 1;
        }
    }
    
    /**
     * set animation oneshot
     * @param oneShot
     */
    public void setOneShot(boolean oneShot) { 
    	this.oneShot = oneShot;
    }
    
    /**
     * get next frame
     * @return
     */
    private int getNext() {
        currentFrames++;
        if (currentFrames >= animationFrames.length) {
        	if (oneShot) {
        		shouldRun = false;
        		currentFrames = animationFrames.length - 1;
        	} else {
            	currentFrames = 0;
        	}
        } 
        return animationFrames[currentFrames];
    }

    
    /**
     * Starts the animation
     */
    public synchronized void start() {
        shouldRun = true;
        if (isRunning) {
            return;
        }

        Runnable runnable = new Runnable() {
			@Override
            public void run() {
                ImageView imageView = mSoftReferenceImageView.get();
                if (!shouldRun || imageView == null) {
                	isRunning = false;
                    if (onAnimationStoppedListener != null) {
                    	onAnimationStoppedListener.AnimationStopped();
                    }
                    return;
                }

                isRunning = true;
                handler.postDelayed(this, delayMillis);

                if (imageView.isShown()) {
                    int imageRes = getNext();
                    if (bitmap != null) { // so Build.VERSION.SDK_INT >= 11
                        bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeResource(imageView.getResources(), imageRes, bitmapOptions);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(imageRes);
                            bitmap.recycle();
                            bitmap = null;
                        }
                    } else {
                        imageView.setImageResource(imageRes);
                    }
                }

            }
        };

        handler.post(runnable);
    }
    
    /**
     * Stops the animation
     */
    public synchronized void stop() {
    	shouldRun = false;
    }
  
	private void getFramesResource() {
		TypedArray typedArray = context.getResources().obtainTypedArray(framesResourceID);
		int longth = typedArray.length();
		animationFrames = new int[longth];
		for(int index = 0; index < longth; index ++) {
			int feedResId = typedArray.getResourceId(index, 0);
			animationFrames[index] = feedResId;
		}
	}

    public void setFramesSequenceAnimationListener(FramesSequenceAnimationListener onAnimationStoppedListener) {
    	this.onAnimationStoppedListener = onAnimationStoppedListener;
    }
    
    public interface FramesSequenceAnimationListener {
    	public void AnimationStopped();
    	public void AnimationStarted();
    }

}
