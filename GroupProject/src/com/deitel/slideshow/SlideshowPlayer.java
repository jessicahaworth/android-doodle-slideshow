// SlideshowPlayer.java
// Plays the selected slideshow that's passed as an Intent extra
package com.deitel.slideshow;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class SlideshowPlayer extends Activity {
	private static final String TAG = "SLIDESHOW"; // error logging tag

	// constants for saving slideshow state when config changes
	private static final String MEDIA_TIME = "MEDIA_TIME";
	private static final String IMAGE_INDEX = "IMAGE_INDEX";
	private static final String SLIDESHOW_NAME = "SLIDESHOW_NAME";

	private static final int DURATION = 5000; // 5 seconds per slide
	private ImageView imageView; // displays the current image
	private String slideshowName; // name of current slideshow
	private SlideshowInfo slideshow; // slideshow being played
	private BitmapFactory.Options options; // options for loading images
	private Handler handler; // used to update the slideshow
	private int nextItemIndex; // index of the next image to display
	private int mediaTime; // time in ms from which media should play
	private MediaPlayer mediaPlayer; // plays the background music, if any

	// initializes the SlideshowPlayer Activity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slideshow_player);

		imageView = (ImageView) findViewById(R.id.imageView);

		if (savedInstanceState == null) // Activity starting
		{
			// get slideshow name from Intent's extras
			slideshowName = getIntent().getStringExtra(Slideshow.NAME_EXTRA);
			mediaTime = 0; // position in media clip
			nextItemIndex = 0; // start from first image
		} // end if
		else // Activity resuming
		{
			// get the play position that was saved when config changed
			mediaTime = savedInstanceState.getInt(MEDIA_TIME);

			// get index of image that was displayed when config changed
			nextItemIndex = savedInstanceState.getInt(IMAGE_INDEX);

			// get name of slideshow that was playing when config changed
			slideshowName = savedInstanceState.getString(SLIDESHOW_NAME);
		} // end else

		// get SlideshowInfo for slideshow to play
		slideshow = Slideshow.getSlideshowInfo(slideshowName);

		// configure BitmapFactory.Options for loading images
		options = new BitmapFactory.Options();
		options.inSampleSize = 4; // sample at 1/4 original width/height

		// if there is music to play
		if (slideshow.getMusicPath() != null) {
			// try to create a MediaPlayer to play the music
			try {
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDataSource(this,
						Uri.parse(slideshow.getMusicPath()));
				mediaPlayer.prepare(); // prepare the MediaPlayer to play
				mediaPlayer.setLooping(true); // loop the music
				mediaPlayer.seekTo(mediaTime); // seek to mediaTime
			} // end try
			catch (Exception e) {
				Log.v(TAG, e.toString());
			} // end catch
		} // end if

		handler = new Handler(); // create handler to control slideshow
	} // end method onCreate

	// called after onCreate and sometimes onStop
	@Override
	protected void onStart() {
		super.onStart();
		handler.post(updateSlideshow); // post updateSlideshow to execute
	} // end method onStart

	// called when the Activity is paused
	@Override
	protected void onPause() {
		super.onPause();

		if (mediaPlayer != null)
			mediaPlayer.pause(); // pause playback
	} // end method onPause

	// called after onStart or onPause
	@Override
	protected void onResume() {
		super.onResume();

		if (mediaPlayer != null)
			mediaPlayer.start(); // resume playback
	} // end method onResume

	// called when the Activity stops
	@Override
	protected void onStop() {
		super.onStop();

		// prevent slideshow from operating when in background
		handler.removeCallbacks(updateSlideshow);
	} // end method onStop

	// called when the Activity is destroyed
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mediaPlayer != null)
			mediaPlayer.release(); // release MediaPlayer resources
	} // end method onDestroy

	// save slideshow state so it can be restored in onCreate
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// if there is a mediaPlayer, store media's current position
		if (mediaPlayer != null)
			outState.putInt(MEDIA_TIME, mediaPlayer.getCurrentPosition());

		// save nextItemIndex and slideshowName
		outState.putInt(IMAGE_INDEX, nextItemIndex - 1);
		outState.putString(SLIDESHOW_NAME, slideshowName);
	} // end method onSaveInstanceState

	// anonymous inner class that implements Runnable to control slideshow
	private Runnable updateSlideshow = new Runnable() {
		@Override
		public void run() {
			if (nextItemIndex >= slideshow.size()) {
				// if there is music playing
				if (mediaPlayer != null && mediaPlayer.isPlaying())
					mediaPlayer.reset(); // slideshow done, reset mediaPlayer
				finish(); // return to launching Activity
			} // end if
			else {
				String item = slideshow.getImageAt(nextItemIndex);
				new LoadImageTask().execute(Uri.parse(item));
				++nextItemIndex;
			} // end else
		} // end method run

		// task to load thumbnails in a separate thread
		class LoadImageTask extends AsyncTask<Uri, Object, Bitmap> {
			// load iamges
			@Override
			protected Bitmap doInBackground(Uri... params) {
				return getBitmap(params[0], getContentResolver(), options);
			} // end method doInBackground

			// set thumbnail on ListView
			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				BitmapDrawable next = new BitmapDrawable(result);
				next.setGravity(android.view.Gravity.CENTER);
				Drawable previous = imageView.getDrawable();

				// if previous is a TransitionDrawable,
				// get its second Drawable item
				if (previous instanceof TransitionDrawable)
					previous = ((TransitionDrawable) previous).getDrawable(1);

				if (previous == null)
					imageView.setImageDrawable(next);
				else {
					Drawable[] drawables = { previous, next };
					TransitionDrawable transition = new TransitionDrawable(
							drawables);
					imageView.setImageDrawable(transition);
					transition.startTransition(1000);
				} // end else

				handler.postDelayed(updateSlideshow, DURATION);
			} // end method onPostExecute
		} // end class LoadImageTask

		// utility method to get a Bitmap from a Uri
		public Bitmap getBitmap(Uri uri, ContentResolver cr,
				BitmapFactory.Options options) {
			Bitmap bitmap = null;

			// get the image
			try {
				InputStream input = cr.openInputStream(uri);
				bitmap = BitmapFactory.decodeStream(input, null, options);
			} // end try
			catch (FileNotFoundException e) {
				Log.v(TAG, e.toString());
			} // end catch

			return bitmap;
		} // end method getBitmap
	}; // end Runnable updateSlideshow
} // end class SlideshowPlayer

/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and * Pearson Education,
 * Inc. All Rights Reserved. * * DISCLAIMER: The authors and publisher of this
 * book have used their * best efforts in preparing the book. These efforts
 * include the * development, research, and testing of the theories and programs
 * * to determine their effectiveness. The authors and publisher make * no
 * warranty of any kind, expressed or implied, with regard to these * programs
 * or to the documentation contained in these books. The authors * and publisher
 * shall not be liable in any event for incidental or * consequential damages in
 * connection with, or arising out of, the * furnishing, performance, or use of
 * these programs. *
 **************************************************************************/
