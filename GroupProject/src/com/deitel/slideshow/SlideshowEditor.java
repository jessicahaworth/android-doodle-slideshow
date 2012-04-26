// SlideshowEditor.java
// Activity for building and Editing a slideshow.
package com.deitel.slideshow;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

public class SlideshowEditor extends ListActivity {
	// slideshowEditorAdapter to display slideshow in ListView
	private SlideshowEditorAdapter slideshowEditorAdapter;
	private SlideshowInfo slideshow; // slideshow data

	// called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slideshow_editor);

		// retrieve the slideshow
		String name = getIntent().getStringExtra(Slideshow.NAME_EXTRA);
		slideshow = Slideshow.getSlideshowInfo(name);

		// set appropriate OnClickListeners for each Button
		Button doneButton = (Button) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(doneButtonListener);

		Button addPictureButton = (Button) findViewById(R.id.addPictureButton);
		addPictureButton.setOnClickListener(addPictureButtonListener);

		Button addDrawingButton = (Button) findViewById(R.id.addDrawingButton);
		addDrawingButton.setOnClickListener(addDrawingButtonListener);

		Button playButton = (Button) findViewById(R.id.playButton);
		playButton.setOnClickListener(playButtonListener);

		// get ListView and set its adapter for displaying list of images
		slideshowEditorAdapter = new SlideshowEditorAdapter(this,
				slideshow.getImageList());
		getListView().setAdapter(slideshowEditorAdapter);
	} // end method onCreate

	// set IDs for each type of media result
	private static final int PICTURE_ID = 1;
	private static final int MUSIC_ID = 2;

	// called when an Activity launched from this Activity returns
	@Override
	protected final void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (resultCode == RESULT_OK) // if there was no error
		{
			Uri selectedUri = data.getData();

			// if the Activity returns an image
			if (requestCode == PICTURE_ID) {
				// add new image path to the slideshow
				slideshow.addImage(selectedUri.toString());

				// refresh the ListView
				slideshowEditorAdapter.notifyDataSetChanged();
			} // end if
			else if (requestCode == MUSIC_ID)  // Activity returns music
			{
				// add new image path to the slideshow
				String uriNew = data.getStringExtra("NewDrawing");
				slideshow.addImage(uriNew);

				// refresh the ListView
				slideshowEditorAdapter.notifyDataSetChanged();
			}
		} // end if
	} // end method onActivityResult

	// called when the user touches the "Done" Button
	private OnClickListener doneButtonListener = new OnClickListener() {
		// return to the previous Activity
		@Override
		public void onClick(View v) {
			finish();
		} // end method onClick
	}; // end OnClickListener doneButtonListener

	// called when the user touches the "Add Picture" Button
	private OnClickListener addPictureButtonListener = new OnClickListener() {
		// launch image choosing activity
		@Override
		public void onClick(View v) {
			// MUST do the broadcast below
			// so the phone rescans the SD directory
			// otherwise if won't find any new drawings
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory())));
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(
					Intent.createChooser(intent,
							getResources().getText(R.string.chooser_image)),
					PICTURE_ID);
		} // end method onClick
	}; // end OnClickListener addPictureButtonListener

	// called when the user touches the "Add Music" Button
	private OnClickListener addDrawingButtonListener = new OnClickListener() {
		// launch music choosing activity
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(v.getContext(), Doodlz.class);
			startActivityForResult(
					intent, MUSIC_ID);
			// Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			// intent.setType("audio/*");
			// startActivityForResult(
			// Intent.createChooser(intent,
			// getResources().getText(R.string.chooser_music)),
			// MUSIC_ID);
		} // end method onClick
	}; // end OnClickListener addMusicButtonListener

	// called when the user touches the "Play" Button
	private OnClickListener playButtonListener = new OnClickListener() {
		// plays the current slideshow
		@Override
		public void onClick(View v) {
			// create new Intent to launch the Slideshowplayer Activity
			Intent playSlideshow = new Intent(SlideshowEditor.this,
					SlideshowPlayer.class);

			// include the slideshow's name as an extra
			playSlideshow.putExtra(Slideshow.NAME_EXTRA, slideshow.getName());
			startActivity(playSlideshow); // launch the Activity
		} // end method onClick
	}; // end playButtonListener

	// called when the user touches the "Delete" Button next
	// to an ImageView
	private OnClickListener deleteButtonListener = new OnClickListener() {
		// removes the image
		@Override
		public void onClick(View v) {
			slideshowEditorAdapter.remove((String) v.getTag());
		} // end method onClick
	}; // end OnClickListener deleteButtonListener

	// Class for implementing the "ViewHolder pattern"
	// for better ListView performance
	private static class ViewHolder {
		ImageView slideImageView; // refers to ListView item's ImageView
		Button deleteButton; // refers to ListView item's Button
	} // end class ViewHolder

	// ArrayAdapter displaying Slideshow images
	private class SlideshowEditorAdapter extends ArrayAdapter<String> {
		private List<String> items; // list of image Uris
		private LayoutInflater inflater;

		public SlideshowEditorAdapter(Context context, List<String> items) {
			super(context, -1, items); // -1 indicates we're customizing view
			this.items = items;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		} // end SlideshoweditorAdapter constructor

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder; // holds references to current item's GUI

			// if convertView is null, inflate GUI and create ViewHolder;
			// otherwise, get existing ViewHolder
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.slideshow_edit_item,
						null);

				// set up ViewHolder for this ListView item
				viewHolder = new ViewHolder();
				viewHolder.slideImageView = (ImageView) convertView
						.findViewById(R.id.slideshowImageView);
				viewHolder.deleteButton = (Button) convertView
						.findViewById(R.id.deleteButton);
				convertView.setTag(viewHolder); // store as View's tag
			} // end if
			else
				// get the ViewHolder from the convertView's tag
				viewHolder = (ViewHolder) convertView.getTag();

			// get and display a thumbnail Bitmap image
			String item = items.get(position); // get current image
			new LoadThumbnailTask().execute(viewHolder.slideImageView,
					Uri.parse(item));

			// configure the "Delete" Button
			viewHolder.deleteButton.setTag(item);
			viewHolder.deleteButton.setOnClickListener(deleteButtonListener);

			return convertView;
		} // end method getView
	} // end class SlideshowEditorAdapter

	// task to load thumbnails in a separate thread
	private class LoadThumbnailTask extends AsyncTask<Object, Object, Bitmap> {
		ImageView imageView; // displays the thumbnail

		// load thumbnail: ImageView, MediaType and Uri as args
		@Override
		protected Bitmap doInBackground(Object... params) {
			imageView = (ImageView) params[0];

			return Slideshow.getThumbnail((Uri) params[1],
					getContentResolver(), new BitmapFactory.Options());
		} // end method doInBackground

		// set thumbnail on ListView
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			imageView.setImageBitmap(result);
		} // end method onPostExecute
	} // end class LoadThumbnailTask
} // end class SlideshowEditor

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
