package com.thanos.opencv_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class OpenCVCameraGallery extends ActionBarActivity implements CvCameraViewListener  {
	private CascadeClassifier cascadeClassifier;
	private Mat grayscaleImage;
	private int absoluteLogoSize;
	
    private static final String TAG = "OpenCVCamera";
    private CameraBridgeViewBase cameraBridgeViewBase;
    
    public static Bitmap bitmap;
    Date curDate = new Date();
    
    
    //  Initialise OpenCV dependencies before we do anything
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                	initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
 
	  private void initializeOpenCVDependencies() {
	      try {
	    	  // Copy the resource into a temp file so OpenCV can load it
	          InputStream is = getResources().openRawResource(R.raw.cascade);
	          File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
	          File mCascadeFile = new File(cascadeDir, "cascade.xml");
	          FileOutputStream os = new FileOutputStream(mCascadeFile);

	          byte[] buffer = new byte[4096];
	          int bytesRead;
	          while ((bytesRead = is.read(buffer)) != -1) {
	              os.write(buffer, 0, bytesRead);
	          }
	          is.close();
	          os.close();
	          
	          // Load the cascade classifier
	          cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
	
	      } catch (Exception e) {
	          Log.e("OpenCVActivity", "Error loading cascade", e);
	      }
	      // Initialise the camera view
	      cameraBridgeViewBase.enableView();
	    }    
    
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        setContentView(R.layout.activity_opencv_camera);
	        
	        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_view);
	        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
	        cameraBridgeViewBase.setCvCameraViewListener(this);

	    }
 
	    @Override
	    public void onResume(){
	        super.onResume();
	        if (!OpenCVLoader.initDebug()) {
	            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
	            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, baseLoaderCallback);
	        } else {
	            Log.d(TAG, "OpenCV library found inside package. Using it!");
	            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
	        }
	    }
    
	    @Override
	    public void onPause() {
	        super.onPause();
	
	        if (cameraBridgeViewBase != null)
	        	cameraBridgeViewBase.disableView();
	    }
    
	    public void onDestroy() {
	        super.onDestroy();
	        if (cameraBridgeViewBase != null)
	        	cameraBridgeViewBase.disableView();
	    }
	    @Override
	    public void onCameraViewStarted(int width, int height) {
	      // Make the image greyscale
	      grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
	
	      // The burger king logo will be a 20% of the height of the screen
	      absoluteLogoSize = (int) (height* 0.2);// * 0.2

	    }

	    @Override
	    public void onCameraViewStopped() {
	 
	    }
	    
	    // We start processing the input image streams
		@Override
	    public Mat onCameraFrame(final Mat inputFrame) {
	
	      Imgproc.cvtColor(inputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
	      MatOfRect burger_king = new MatOfRect();
	      
	     // Use the classifier to detect the logos
	      if (cascadeClassifier != null) {
	          cascadeClassifier.detectMultiScale(grayscaleImage, burger_king, 1.1,3,3,
	                  new Size(absoluteLogoSize, absoluteLogoSize), new Size());
	      }
	      
	      // Placeholder for the logos found
	      Rect[] burger_kingArray = burger_king.toArray();
	      
	      // Placeholder matrix for the choice of image
	      Mat bSel = new Mat();
	      
	      // Get the chosen image
	      Bitmap bitmap = BitmapFactory.decodeFile("mnt/sdcard/votka.jpg");
	      Utils.bitmapToMat(bitmap, bSel);
	      
	      // scale it down
	      Imgproc.resize(bSel, bSel, new Size(100, 100));
	      
	      // Placeholder matrix for the choice of image to be placed in burger king's place
	      Mat b = new Mat();
	      
	      // Loop through all logos found and get their bounding rectangle coordinates
		  for (int i = 0; i <burger_kingArray.length; i++){
			  	double xd1 = burger_kingArray[i].tl().x;
			  	double yd1 = burger_kingArray[i].tl().y;
			  	double xd2 = burger_kingArray[i].br().x;
			  	double yd2 = burger_kingArray[i].br().y;
			  	int ixd1 = (int) xd1;
			  	int iyd1 = (int) yd1;
			  	int ixd2 = (int) xd2;
			  	int iyd2 = (int) yd2;
			  	
			    // Create a rectangle around it
			    Core.rectangle(inputFrame, burger_kingArray[i].tl(), burger_kingArray[i].br(), new Scalar(0, 0, 0, 0), 0);
			    Rect roi = new Rect(ixd1, iyd1, ixd2 - ixd1, iyd2 - iyd1);
			    
			    // Convert the image of choice (bSel) to the matrix placeholder we defined (b)
			    Imgproc.resize(bSel, b, new Size(ixd2 - ixd1, iyd2 - iyd1));
			    
			    // Place the image of our choice in the rectangle
			    Mat sub  = inputFrame.submat(roi);
			    Imgproc.cvtColor(sub, sub, Imgproc.COLOR_RGBA2GRAY);
			    Imgproc.cvtColor(sub, sub, Imgproc.COLOR_GRAY2RGBA);
			    b.copyTo(inputFrame.submat(roi));           
			  
		     }
	
		  // The below saves the image in sdcard and takes care of uploading to Instagram.sending via email
		  Button takePic = (Button) findViewById(R.id.photo);
		  takePic.setOnClickListener(new View.OnClickListener() {
		         @Override
		         public void onClick(View v) {
		       	     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		    	     String DateToStr = format.format(curDate);
		             Imgproc.cvtColor(inputFrame, inputFrame, Imgproc.COLOR_RGB2BGRA);
		
		        	 Highgui.imwrite("mnt/sdcard/votka_exete"+DateToStr+".jpg", inputFrame);
		 			 File root = android.os.Environment.getExternalStorageDirectory();
		        	 File file = new File(root, "votka_exete"+DateToStr+".jpg");
		
		 			 Intent emailIntent = new Intent(Intent.ACTION_SEND);
		        	 emailIntent.setType("message/rfc822");
		        	 emailIntent.putExtra(Intent.EXTRA_SUBJECT, "breaking_the_monoform");
		        	 emailIntent.putExtra(Intent.EXTRA_TEXT   , "body of email");
		
		        	Uri uri = Uri.fromFile(file);
				    emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
				    
				    Intent shareIntent = new Intent();
				    shareIntent.setAction(Intent.ACTION_SEND);
				    try {
						shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media
						.insertImage(getContentResolver(), "mnt/sdcard/votka_exete"+DateToStr+".jpg", "", "")));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    shareIntent.setType("image/jpeg");
				    
				    PackageManager pm = getPackageManager();
				    Intent openInChooser = Intent.createChooser(shareIntent, "");
				    List<ResolveInfo> resInfo = pm.queryIntentActivities(shareIntent, 0);
				    List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();    
				    for (int i = 0; i < resInfo.size(); i++) {
				        // Extract the label, append it, and repackage it in a LabeledIntent
				        ResolveInfo ri = resInfo.get(i);
				        String packageName = ri.activityInfo.packageName;
				        if(packageName.contains("instagram")) {
				        	shareIntent.setPackage(packageName);
				        }
			            intentList.add(new LabeledIntent(shareIntent, packageName, ri.loadLabel(pm), ri.icon));
		
				    }
		
				    // convert intentList to array
				    LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
		
				    openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
				    startActivity(Intent.createChooser(shareIntent, "send"));
				 
				    
		        	 try {
		        	     startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		        	 } catch (android.content.ActivityNotFoundException ex) {
		        	     Toast.makeText(OpenCVCameraGallery.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		        	 }
		         }
		     });
	      return  inputFrame;
	    }

}
