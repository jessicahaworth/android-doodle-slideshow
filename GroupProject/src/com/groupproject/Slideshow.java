// Slideshow.java
// Main Activity for the Slideshow class.
package com.groupproject;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Slideshow extends ListActivity {
	// used when adding slideshow name as an extra to an Intent
	public static final String NAME_EXTRA = "NAME";

	// public static final String PREF_FILE_NAME = "PrefFile";
	// SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME,
	// MODE_PRIVATE);

	static List<SlideshowInfo> slideshowList; // List of slideshows
	private ListView slideshowListView; // this ListActivity's ListView
	private SlideshowAdapter slideshowAdapter; // adapter for the ListView

	// called when the activity is first created
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		slideshowListView = getListView(); // get the built-in ListView

		// create and set the ListView's adapter
		slideshowList = new ArrayList<SlideshowInfo>();
		slideshowAdapter = new SlideshowAdapter(this, slideshowList);
		slideshowListView.setAdapter(slideshowAdapter);

		// create a new AlertDialog Builder
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.welcome_message_title);
		builder.setMessage(R.string.welcome_message);
		builder.setPositiveButton(R.string.button_ok, null);
		builder.show();
	} // end method onCreate

	// create the Activity's menu from a menu resource XML file
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.slideshow_menu, menu);
		return true;
	} // end method onCreateOptionsMenu

	// SlideshowEditor request code passed to startActivityForResult
	private static final int EDIT_ID = 0;

	// handle choice from options menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// get a reference to the LayoutInflater service
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// inflate slideshow_name_edittext.xml to create an EditText
		View view = inflater.inflate(R.layout.slideshow_name_edittext, null);
		final EditText nameEditText = (EditText) view
				.findViewById(R.id.nameEditText);

		// create an input dialog to get slideshow name from user
		AlertDialog.Builder inputDialog = new AlertDialog.Builder(this);
		inputDialog.setView(view); // set the dialog's custom View
		inputDialog.setTitle(R.string.dialog_set_name_title);

		inputDialog.setPositiveButton(R.string.button_set_slideshow_name,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// create a SlideshowInfo for a new slideshow
						String name = nameEditText.getText().toString().trim();

						if (name.length() != 0) {
							slideshowList.add(new SlideshowInfo(name));

							// create Intent to launch the SlideshowEditor
							// Activity,
							// add slideshow name as an extra and start the
							// Activity
							Intent editSlideshowIntent = new Intent(
									Slideshow.this, SlideshowEditor.class);
							editSlideshowIntent.putExtra(NAME_EXTRA, name);
							startActivityForResult(editSlideshowIntent, EDIT_ID);
						} // end if
						else {
							// display message that slideshow must have a name
							Toast message = Toast.makeText(Slideshow.this,
									R.string.message_name, Toast.LENGTH_SHORT);
							message.setGravity(Gravity.CENTER,
									message.getXOffset() / 2,
									message.getYOffset() / 2);
							message.show(); // display the Toast
						} // end else
					} // end method onClick
				} // end anonymous inner class
		); // end call to setPositiveButton

		inputDialog.setNegativeButton(R.string.button_cancel, null);
		inputDialog.show();

		return super.onOptionsItemSelected(item); // call super's method
	} // end method onOptionsItemSelected

	// refresh ListView after slideshow editing is complete
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		slideshowAdapter.notifyDataSetChanged(); // refresh the adapter
	} // end method onActivityResult

	// Class for implementing the "ViewHolder pattern"
	// for better ListView performance
	private static class ViewHolder {
		TextView nameTextView; // refers to ListView item's TextView
		ImageView imageView; // refers to ListView item's ImageView
		Button playButton; // refers to ListView item's Play Button
		Button editButton; // refers to ListView item's Edit Button
		Button shareButton;
		Button deleteButton; // refers to ListView item's Delete Button
	} // end class ViewHolder

	// ArrayAdapter subclass that displays a slideshow's name, first image
	// and "Play", "Edit" and "Delete" Buttons
	private class SlideshowAdapter extends ArrayAdapter<SlideshowInfo> {
		private List<SlideshowInfo> items;
		private LayoutInflater inflater;

		// public constructor for SlideshowAdapter
		public SlideshowAdapter(Context context, List<SlideshowInfo> items) {
			// call super constructor
			super(context, -1, items);
			this.items = items;
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		} // end SlideshowAdapter constructor

		// returns the View to display at the given position
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder; // holds references to current item's GUI

			// if convertView is null, inflate GUI and create ViewHolder;
			// otherwise, get existing ViewHolder
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.slideshow_list_item,
						null);

				// set up ViewHolder for this ListView item
				viewHolder = new ViewHolder();
				viewHolder.nameTextView = (TextView) convertView
						.findViewById(R.id.nameTextView);
				viewHolder.imageView = (ImageView) convertView
						.findViewById(R.id.slideshowImageView);
				viewHolder.playButton = (Button) convertView
						.findViewById(R.id.playButton);
				viewHolder.editButton = (Button) convertView
						.findViewById(R.id.editButton);
				viewHolder.shareButton = (Button) convertView
						.findViewById(R.id.shareButton);
				viewHolder.deleteButton = (Button) convertView
						.findViewById(R.id.deleteButton);
				convertView.setTag(viewHolder); // store as View's tag
			} // end if
			else
				// get the ViewHolder from the convertView's tag
				viewHolder = (ViewHolder) convertView.getTag();

			// get the slideshow the display its name in nameTextView
			SlideshowInfo slideshowInfo = items.get(position);
			viewHolder.nameTextView.setText(slideshowInfo.getName());

			// if there is at least one image in this slideshow
			if (slideshowInfo.size() > 0) {
				// create a bitmap using the slideshow's first image or video
				String firstItem = slideshowInfo.getImageAt(0);
				new LoadThumbnailTask().execute(viewHolder.imageView,
						Uri.parse(firstItem));
			} // end if

			// set tag and OnClickListener for the "Play" Button
			viewHolder.playButton.setTag(slideshowInfo);
			viewHolder.playButton.setOnClickListener(playButtonListener);

			// set tag and OnClickListener for the "Edit" Button
			viewHolder.editButton.setTag(slideshowInfo);
			viewHolder.editButton.setOnClickListener(editButtonListener);
			
			// set tag and OnClickListener for the "Share" Button
			viewHolder.shareButton.setTag(slideshowInfo);
			viewHolder.shareButton.setOnClickListener(shareButtonListener);

			// set and tag OnClickListener for the "Delete" Button
			viewHolder.deleteButton.setTag(slideshowInfo);
			viewHolder.deleteButton.setOnClickListener(deleteButtonListener);

			return convertView; // return the View for this position
		} // end getView
	} // end class SlideshowAdapter

	// task to load thumbnails in a separate thread
	private class LoadThumbnailTask extends AsyncTask<Object, Object, Bitmap> {
		ImageView imageView; // displays the thumbnail

		// load thumbnail: ImageView and Uri as args
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

	// respond to events generated by the "Play" Button
	OnClickListener playButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// create an intent to launch the SlideshowPlayer Activity
			Intent playSlideshow = new Intent(Slideshow.this,
					SlideshowPlayer.class);
			playSlideshow.putExtra(NAME_EXTRA,
					((SlideshowInfo) v.getTag()).getName());
			startActivity(playSlideshow); // launch SlideshowPlayer Activity
		} // end method onClick
	}; // end playButtonListener

	// respond to events generated by the "Edit" Button
	private OnClickListener editButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// create an intent to launch the SlideshowEditor Activity
			Intent editSlideshow = new Intent(Slideshow.this,
					SlideshowEditor.class);
			editSlideshow.putExtra(NAME_EXTRA,
					((SlideshowInfo) v.getTag()).getName());
			startActivityForResult(editSlideshow, 0);
		} // end method onClick
	}; // end playButtonListener

	// respond to events generated by the "Edit" Button
	private OnClickListener shareButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
            Intent sendMailIntent = new Intent(Intent.ACTION_SEND); 
            sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.slideshow_subject));
            sendMailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.slideshow_msg)); 
            sendMailIntent.setType("image/bmp");
        	sendMailIntent.putExtras(new Intent(Slideshow.this,
					SlideshowEditor.class));
            startActivity(Intent.createChooser(sendMailIntent, "Facebook / Message?"));
        
		} // end method onClick
	}; // end playButtonListener
	
	// respond to events generated by the "Delete" Button
	private OnClickListener deleteButtonListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			// create a new AlertDialog Builder
			AlertDialog.Builder builder = new AlertDialog.Builder(
					Slideshow.this);
			builder.setTitle(R.string.dialog_confirm_delete);
			builder.setMessage(R.string.dialog_confirm_delete_message);
			builder.setPositiveButton(R.string.button_ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Slideshow.slideshowList.remove((SlideshowInfo) v
									.getTag());
							slideshowAdapter.notifyDataSetChanged(); // refresh
						} // end method onClick
					} // end anonymous inner class
			); // end call to setPositiveButton
			builder.setNegativeButton(R.string.button_cancel, null);
			builder.show();
		} // end method onClick
	}; // end playButtonListener

	// utility method to locate SlideshowInfo object by slideshow name
	public static SlideshowInfo getSlideshowInfo(String name) {
		// locate and return slideshow with specified name
		for (SlideshowInfo slideshowInfo : slideshowList)
			if (slideshowInfo.getName().equals(name))
				return slideshowInfo;

		return null; // no matching object
	} // end method getSlideshowInfo

	// utility method to get a thumbnail image Bitmap
	public static Bitmap getThumbnail(Uri uri, ContentResolver cr,
			BitmapFactory.Options options) {
		int id = Integer.parseInt(uri.getLastPathSegment());

		Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
				MediaStore.Images.Thumbnails.MICRO_KIND, options);

		return bitmap;
	} // end method getThumbnail
} // end class Slideshow

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
