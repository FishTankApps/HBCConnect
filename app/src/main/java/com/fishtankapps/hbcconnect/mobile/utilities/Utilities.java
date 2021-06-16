package com.fishtankapps.hbcconnect.mobile.utilities;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import com.fishtankapps.hbcconnect.mobile.activities.MainActivity;
import com.fishtankapps.hbcconnect.mobile.utilities.firebase.FirebaseDatabaseInterface;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class Utilities {

    private Utilities(){}

    public static int getColor(Context context, int id){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, true);

        return typedValue.data;
    }

    private static int isInternetAvailable = 0;
    public static synchronized boolean isInternetUnavailable() {
        new Thread(()->{
            isInternetAvailable = 0;
            try {
                InetAddress ipAddress = InetAddress.getByName("google.com");
                isInternetAvailable = (!ipAddress.toString().equals("")) ? 1 : -1;
            } catch (Exception e) {
                e.printStackTrace();
                isInternetAvailable = -1;
            }
        }).start();

        while(isInternetAvailable == 0)
            try{Thread.sleep(100);}catch (Exception ignore){}

        return isInternetAvailable != 1;
    }


    public static void getBackgroundImage(String id, OnBitmapFoundListener listener) {
        try{
            ContextWrapper cw = new ContextWrapper(MainActivity.getContext());
            File directory = cw.getDir("image_cache", Context.MODE_PRIVATE);
            File file = new File(directory, "Image-" + id + ".png");

            if(file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    listener.onBitmapFound(BitmapFactory.decodeStream(fis));
                    return;
                } catch (Exception e) {
                   Log.w("Utilities", "Error Reading Image");
                }
            }

            FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("image_links/" + id, (rawLink) -> {
                String imageURL = rawLink.toString();

                new Thread(() -> {
                        try {
                            URL url = new URL(imageURL);
                            Bitmap backgroundImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                            listener.onBitmapFound(backgroundImage);

                            try (FileOutputStream fos = new FileOutputStream(file)) {
                                backgroundImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface OnBitmapFoundListener {
        void onBitmapFound(Bitmap bitmap);
    }

    public static Bitmap getImageFromURL(String imageURL) {
        try{
            URL url = new URL(imageURL);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (Exception ignore){}

        return null;
    }


    public static void createImageLink(String imageURL, OnLinkCreatedListener listener) {
        Log.d("CreateImageLink", "Getting Current Link List...");
        if(imageURL.equals("")) {
            listener.onLinkCreated(-1);
            return;
        }

        FirebaseDatabaseInterface.getFirebaseDatabaseInterface().getValue("image_links", (rawLinkList) ->{
            Log.d("CreateImageLink", "Raw Link List: " + rawLinkList);

            @SuppressWarnings("unchecked")
            ArrayList<String> linkList = (ArrayList<String>) rawLinkList;

            int id = linkList.size();
            Log.d("CreateImageLink", "New Link ID: " + id);

            Log.d("CreateImageLink", "Creating New Link...");
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(imageURL))
                    .setDomainUriPrefix("https://hbcconnect.page.link/")
                    .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Log.d("CreateImageLink", "New Link: " + shortLink.toString());
                            Log.d("CreateImageLink", "Adding New Link to ArrayList... ");

                            listener.onLinkCreated(id);

                            linkList.add(shortLink.toString());
                            Log.d("CreateImageLink", "Updating Database... ");
                            FirebaseDatabaseInterface.getFirebaseDatabaseInterface().setValue("image_links", linkList);


                        } else {
                            Log.e("CreateImageLink", "Error Creating Link: " + task.getException());
                        }
                    });

        });
    }

    public interface OnLinkCreatedListener {
        void onLinkCreated(int imageID);
    }


    public static <E extends Comparable<E>> void quickSort(List<E> array) {
        quickSort(array, 0, array.size() - 1); // This method simply starts the recursive method.
    }

    private static <E extends Comparable<E>> void quickSort(List<E> array, int first, int last) {
        if (last > first) {                                          // First it checks if the indexes are valid,
            int pivotIndex = partition(array, first, last);		     //  if they are, it gets the split index of the
            quickSort(array, first, pivotIndex - 1);		         //  array, and recursively calls itself to sort each of those
            quickSort(array, pivotIndex + 1, last);                  //  two parts.
        }
    }
    private static <E extends Comparable<E>> int partition(List<E> array, int first, int last) {

        if(last - first > 2) {			              // This method starts with checking if there are at least 3 elements.
            E firstElement = array.get(first);            //  If there are, it stores the first three elements, and then compares them.
            E secondElement = array.get(first + 1);       // If either the second or third element is the mean value of the three, it
            E thirdElement = array.get(first + 2);        //  swaps that value with the first value, making that value the pivot value.

            if(secondElement.compareTo(firstElement) > 0 && secondElement.compareTo(thirdElement) < 0 ||
                    secondElement.compareTo(firstElement) < 0 && secondElement.compareTo(thirdElement) > 0) {
                array.set(first, secondElement);
                array.set(first + 1, firstElement);

            } else if(thirdElement.compareTo(firstElement) > 0 && thirdElement.compareTo(secondElement) < 0 ||
                    thirdElement.compareTo(firstElement) < 0 && thirdElement.compareTo(secondElement) > 0) {
                array.set(first, thirdElement);
                array.set(first + 2, firstElement);
            }
        }

        E pivot = array.get(first);	                                     //	It then makes the first value in the sub array the pivot value.
        int low = first + 1;                                         // Then it makes the start index the second index in the array,
        int high = last;	                                         //  and the high value the last index of the sub array.

        while (high > low) {                                         // Next it loops until high and low are equal to each other.
            while (low <= high && array.get(low).compareTo(pivot) <= 0)  // First it starts from the left side and loops until the low value
                low++;                                               //  is higher than high or it finds a value that is higher than
            //  the pivot value.
            while (low <= high && array.get(high).compareTo(pivot) > 0)  // Then it starts from the right side and loops until the low value
                high--;                                              //  if higher than high or it finds a value lower than the pivot
            //  value.
            if (high > low) {                                        // Then, if it found a two wrong values, it switches them.
                E temp = array.get(high);                                // Then it loops until all the values are on the right side or only
                array.set(high, array.get(low));                            //  one is on the wrong side.
                array.set(low, temp);
            }
        }

        while (high > first && array.get(high).compareTo(pivot) >= 0)    // Then it loops looking for a value less than the pivot.
            high--;


        if (pivot.compareTo(array.get(high)) > 0) {                      // If any values where swapped, it swaps the last checked index with
            array.set(first, array.get(high));                              //  the pivot to finish swapping the array.
            array.set(high, pivot);
            return high;                                             // Then it returns high as the pivot index.
        } else
            return first;	                                         // If all values were swapped, it returns first as the
    }
}
