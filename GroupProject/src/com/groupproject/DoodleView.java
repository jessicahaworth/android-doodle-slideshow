// DoodleView.java
// Main View for the Doodlz app.
package com.groupproject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

// the main screen that is painted
public class DoodleView extends View {
	// used to determine whether user moved a finger enough to draw again
	private static final float TOUCH_TOLERANCE = 10;

	private Bitmap bitmap; // drawing area for display or saving
	private Canvas bitmapCanvas; // used to draw on bitmap
	private Paint paintScreen; // use to draw bitmap onto screen
	private Paint paintLine; // used to draw lines onto bitmap
	private HashMap<Integer, Path> pathMap; // current Paths being drawn
	private HashMap<Integer, Point> previousPointMap; // current Points

	public Uri saveURI;

	private Bitmap bMap;

	// DoodleView constructor initializes the DoodleView
	public DoodleView(Context context, AttributeSet attrs) {
		super(context, attrs); // pass context to View's constructor

		paintScreen = new Paint(); // used to display bitmap onto screen

		// set the initial display settings for the painted line
		paintLine = new Paint();
		paintLine.setAntiAlias(true); // smooth edges of drawn line
		paintLine.setColor(Color.BLACK); // default color is black
		paintLine.setStyle(Paint.Style.STROKE); // solid line
		paintLine.setStrokeWidth(5); // set the default line width
		paintLine.setStrokeCap(Paint.Cap.ROUND); // rounded line ends
		pathMap = new HashMap<Integer, Path>();
		previousPointMap = new HashMap<Integer, Point>();
	} // end DoodleView constructor

	// Method onSizeChanged creates BitMap and Canvas after app displays
	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		// bitmapCanvas = new Canvas(bitmap);

		// potential code to load a background image
		bMap = BitmapFactory
				.decodeFile("/sdcard/DCIM/Camera/1335401735175.jpg").copy(
						Bitmap.Config.ARGB_8888, true);
		bitmap = bMap;
		// if (bMap.isMutable())
		bitmapCanvas = new Canvas(bitmap);

		// bitmapCanvas.drawBitmap(bMap, 0f, 0f, null);

		// bitmap.eraseColor(Color.WHITE); // erase the BitMap with white
		bitmap = bMap;
	} // end method onSizeChanged

	// clear the painting
	public void clear() {
		pathMap.clear(); // remove all paths
		previousPointMap.clear(); // remove all previous points
		bitmap.eraseColor(Color.WHITE); // clear the bitmap
		invalidate(); // refresh the screen
	} // end method clear

	// set the painted line's color
	public void setDrawingColor(int color) {
		paintLine.setColor(color);
	} // end method setDrawingColor

	// return the painted line's color
	public int getDrawingColor() {
		return paintLine.getColor();
	} // end method getDrawingColor

	// set the painted line's width
	public void setLineWidth(int width) {
		paintLine.setStrokeWidth(width);
	} // end method setLineWidth

	// return the painted line's width
	public int getLineWidth() {
		return (int) paintLine.getStrokeWidth();
	} // end method getLineWidth

	// called each time this View is drawn
	@Override
	protected void onDraw(Canvas canvas) {
		// draw the background screen
		canvas.drawBitmap(bitmap, 0, 0, paintScreen);

		// for each path currently being drawn
		for (Integer key : pathMap.keySet())
			canvas.drawPath(pathMap.get(key), paintLine); // draw line
	} // end method onDraw

	// handle touch event
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// get the event type and the ID of the pointer that caused the event
		int action = event.getActionMasked(); // event type
		int actionIndex = event.getActionIndex(); // pointer (i.e., finger)

		// determine which type of action the given MotionEvent
		// represents, then call the corresponding handling method
		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_POINTER_DOWN) {
			touchStarted(event.getX(actionIndex), event.getY(actionIndex),
					event.getPointerId(actionIndex));
		} // end if
		else if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_POINTER_UP) {
			touchEnded(event.getPointerId(actionIndex));
		} // end else if
		else {
			touchMoved(event);
		} // end else

		invalidate(); // redraw
		return true; // consume the touch event
	} // end method onTouchEvent

	// called when the user touches the screen
	private void touchStarted(float x, float y, int lineID) {
		Path path; // used to store the path for the given touch id
		Point point; // used to store the last point in path

		// if there is already a path for lineID
		if (pathMap.containsKey(lineID)) {
			path = pathMap.get(lineID); // get the Path
			path.reset(); // reset the Path because a new touch has started
			point = previousPointMap.get(lineID); // get Path's last point
		} // end if
		else {
			path = new Path(); // create a new Path
			pathMap.put(lineID, path); // add the Path to Map
			point = new Point(); // create a new Point
			previousPointMap.put(lineID, point); // add the Point to the Map
		} // end else

		// move to the coordinates of the touch
		path.moveTo(x, y);
		point.x = (int) x;
		point.y = (int) y;
	} // end method touchStarted

	// called when the user drags along the screen
	private void touchMoved(MotionEvent event) {
		// for each of the pointers in the given MotionEvent
		for (int i = 0; i < event.getPointerCount(); i++) {
			// get the pointer ID and pointer index
			int pointerID = event.getPointerId(i);
			int pointerIndex = event.findPointerIndex(pointerID);

			// if there is a path associated with the pointer
			if (pathMap.containsKey(pointerID)) {
				// get the new coordinates for the pointer
				float newX = event.getX(pointerIndex);
				float newY = event.getY(pointerIndex);

				// get the Path and previous Point associated with
				// this pointer
				Path path = pathMap.get(pointerID);
				Point point = previousPointMap.get(pointerID);

				// calculate how far the user moved from the last update
				float deltaX = Math.abs(newX - point.x);
				float deltaY = Math.abs(newY - point.y);

				// if the distance is significant enough to matter
				if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
					// move the path to the new location
					path.quadTo(point.x, point.y, (newX + point.x) / 2,
							(newY + point.y) / 2);

					// store the new coordinates
					point.x = (int) newX;
					point.y = (int) newY;
				} // end if
			} // end if
		} // end for
	} // end method touchMoved

	// called when the user finishes a touch
	private void touchEnded(int lineID) {
		Path path = pathMap.get(lineID); // get the corresponding Path
		bitmapCanvas.drawPath(path, paintLine); // draw to bitmapCanvas
		path.reset(); // reset the Path
	} // end method touch_ended

	// save the current image to the Gallery
	// "saved" means that it's just being saved
	// false means that we're hacking this to add it to the gallery
	// and silently save
	public void saveImage(Boolean saved) {
		// use "Doodlz" followed by current time as the image file name
		String fileName = "Doodlz" + System.currentTimeMillis();

		// create a ContentValues and configure new image's data
		ContentValues values = new ContentValues();
		values.put(Images.Media.TITLE, fileName);
		values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
		values.put(Images.Media.MIME_TYPE, "image/jpg");

		// get a Uri for the location to save the file
		Uri uri = getContext().getContentResolver().insert(
				Images.Media.EXTERNAL_CONTENT_URI, values);
		saveURI = uri;

		try {
			// get an OutputStream to uri
			OutputStream outStream = getContext().getContentResolver()
					.openOutputStream(uri);

			// copy the bitmap to the OutputStream
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);

			// flush and close the OutputStream
			outStream.flush(); // empty the buffer
			outStream.close(); // close the stream
			Toast message = Toast
					.makeText(getContext(), "", Toast.LENGTH_SHORT);
			if (saved) {
				// display a message indicating that the image was saved
				message = Toast.makeText(getContext(), R.string.message_saved,
						Toast.LENGTH_SHORT);

			} else {
				message = Toast.makeText(getContext(), R.string.message_added,
						Toast.LENGTH_SHORT);
			}
			message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
					message.getYOffset() / 2);
			message.show(); // display the Toast

		} // end try
		catch (IOException ex) {
			// display a message indicating that the image was saved
			Toast message = Toast.makeText(getContext(),
					R.string.message_error_saving, Toast.LENGTH_SHORT);
			message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
					message.getYOffset() / 2);
			message.show(); // display the Toast
		} // end catch
	} // end method saveImage
} // end class DoodleView

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
