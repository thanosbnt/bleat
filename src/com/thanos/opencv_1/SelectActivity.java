package com.thanos.opencv_1;


import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class SelectActivity extends ActionBarActivity {
	ImageView image;
	ImageView image2;
	ImageView image3;

	Button button;
	
    private static final int SELECT_PICTURE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.select_activity);
        addListenerOnButton();
    }
    
	public void addListenerOnButton() {
        // Our three images: The first is the folder icon, second is the blue sky  and third is the  forest
		image = (ImageView) findViewById(R.id.imageView1);
		image2 = (ImageView) findViewById(R.id.imageView2);
		image3 = (ImageView) findViewById(R.id.imageView3);

        // Set up the listeners that will call the OpenCVCamera classes
		image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {            	   	
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), SELECT_PICTURE);

            }
        });

		image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {            	
                Intent i = new Intent(getApplicationContext(), OpenCVCamera.class);
            	
            	String strName = "blue_sky.jpg";
            	i.putExtra("votka", strName);

            	startActivity(i);
            }
        });		
		image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {            	
                Intent i = new Intent(getApplicationContext(), OpenCVCamera.class);
            	
            	String strName = "forest.jpg";
            	i.putExtra("votka", strName);

            	startActivity(i);
            }
        });			

	}
	
	// Save the chosen image to a bitmap and start the activity
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            	Bitmap bitmap  = getPath(data.getData());
            	saveImage(bitmap);
            	Intent i = new Intent(getApplicationContext(), OpenCVCameraGallery.class);
            	startActivity(i);
          	
        }
    }
	
	// Save image function
	public void saveImage (Bitmap bitmap) {
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream("mnt/sdcard/votka.jpg");
		    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out); // bmp is your Bitmap instance
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
	
	   // Get the Uri path
	   private Bitmap getPath(Uri uri) {

			String[] projection = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(uri, projection, null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String filePath = cursor.getString(column_index);
			Bitmap bitmap = BitmapFactory.decodeFile(filePath);
			return bitmap;
	}
}
