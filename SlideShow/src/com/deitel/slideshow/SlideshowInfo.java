// SlideshowInfo.java
// Stores the data for a single slideshow.
package com.deitel.slideshow;

import java.util.ArrayList;
import java.util.List;

public class SlideshowInfo
{
   private String name; // name of this slideshow
   private List<String> imageList; // this slideshow's images
   private String musicPath; // location of music to play
   
   // constructor 
   public SlideshowInfo(String slideshowName)
   {
      name = slideshowName; // set the slideshow name
      imageList = new ArrayList<String>(); 
      musicPath = null; // currently there is no music for the slideshow
   } // end SlideshowInfo constructor

   // return this slideshow's name
   public String getName()
   {
      return name;
   } // end method getName

   // return List of Strings pointing to the slideshow's images
   public List<String> getImageList()
   {
      return imageList;
   } // end method getImageList

   // add a new image path
   public void addImage(String path)
   {
      imageList.add(path);
   } // end method addImage
   
   // return String at position index
   public String getImageAt(int index)
   {
      if (index >= 0 && index < imageList.size())
         return imageList.get(index);
      else
         return null;
   } // end method getImageAt

   // return this slideshow's music
   public String getMusicPath()
   {
      return musicPath;
   } // end method getMusicPath

   // set this slideshow's music
   public void setMusicPath(String path)
   {
      musicPath = path;
   } // end method setMusicUri
   
   // return number of images/videos in the slideshow
   public int size()
   {
      return imageList.size();
   } // end method size
} // end class SlideshowInfo


/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/