# Breaking the Monoform

(Breaking the) Monoform in Public Space” is a work  between a visual artist and a data scientist/developer. It is an open license prototype augmented reality camera application for mobile devices. The camera, with the use of real time image recognition and computer vision techniques, can identify commercial brands in the photographic frame of the user and give them the option to replace them with whatever they intend to. The user can select an image from their own archive and use it to replace the selected advertised space. Computer vision and image recognition is used today on the web, to categorize and auto-brand our user made images. In order to make the users data accessible and available for advertising purposes. Consequently, the user by uploading the image they made on social networks, can not only make a statement about the commercialization of both the virtual and physical public space, but also interfere with the process of advertising in social media. In addition, the user can think of the alternative possible use of the physical public space, re-imagine and create its image  by replacing the space now occupied by commercial advertisements with whatever they want. creating an image might seem immaterial in the physical world, but if we stop and think that this image is taking physical space in a real structure that is used as a storage somewhere and that after its creation its containing data have the potential to be used for profit that will eventually be measured in gold, then the act of changing the image of physical space in its virtual representation starts having a very material shape.



## How it works
There is a lot of research when it comes object detection from images and for a good reason too: it is a difficult task with many things to consider: Do we use global features such as the distribution of pixel values or local features such as the geometric properties of a subset of the image? Do we want the our recognition to be invariant to shape, scaling and brightness? The list goes on.
 
The problem becomes even harder when we want to make this happen in real time. Naturally, there is not one universal solution that fits all needs. For this particular project we used a method called Haar Cascade classification for three reasons: It is relatively straightforward to understand the concept behind the code, it is well documented and there are ready made tools for the process and it is fast.
 
In a nutshell, we are using a small xml file that contains some of the features of the object we want to detect. A very good explanation of the procedure can be found on the fantastic [OpenCV project website](http://opencv-python-tutroals.readthedocs.io/en/latest/py_tutorials/py_objdetect/py_face_detection/py_face_detection.html).

The catch is that we have to generate one such file for every single object that we want to detect, and generating such a file is a lengthy procedure and requires a degree of dedication. On the plus side, there are a lot of resources out there that can help you through the process if you want to generate your own cascade: [1](https://pythonprogramming.net/haar-cascade-object-detection-python-opencv-tutorial/), [2](http://docs.opencv.org/2.4.13.2/doc/user_guide/ug_traincascade.html), [3](http://memememememememe.me/post/training-haar-cascades/) to point to just a few. For this project we generated a Haar cascade for the logo of Burger King, but the procedure is similar for any logo out there. This file can be found under res\raw\cascade.xml
 
The Android app itself is composed of four activity classes:
* MainActivity: The introduction window of the app. Tapping the Black Box will navigate to…
* SelectActivity: This class allows one to use one of the preloaded pictures for the logo replacement: a majestic forest or a magnificent blue sky. Tapping on the folder icon you can navigate to the phone's gallery so you can choose any other image you like.
* OpenCVCamera: Tapping the blue sky or forest image launches the object detection class. Here is where all the work is happening.
* OpenCVCameraGallery: Tapping the folder icon of SelectActivity class launches this class. It is essentially the same as OpenCVCamera but now we are fetching and compressing (as far as possible) the image choice of the user.

## What tools
We want to make this work in android phones phones so we need some sorts of android IDE. We are also using the OpenCV library for android, as well as the implementation of the OpenCV for our computers. If you are on mac, homebrew will make your life much easier:

```
brew tap homebrew/science
brew install opencv
``` 

We are using Android opencv library 2.4.11 and not opencv 3 as it was easier to fish out resources for this version online. You can get it from  [here](https://sourceforge.net/projects/opencvlibrary/files/opencv-android/2.4.11/). Also please note that the android code is tested for Android version up to 4.3 (Jelly Bean) so if you try to run the code on devices with API higher than 18 you might get disappointed as we did. The issue was not on the object detection side of things, but rather on the storing and accessing the resulting images which is embarrassing. But then again, we are not Android developers so i'm sure a lot of brilliant people are out there can make it work for more recent versions.

## How to replace a Haar Cascade
OpenCVCamera and OpenCVCameraGallery classes generally do the following:
* Initialise the classifier which translates to loading the Burger King Haar Cascade xml file


``` java
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
``` 
* Using the classifier to detect the Burger king logo:
``` java
MatOfRect burger_king = new MatOfRect();
 
     // Use the classifier to detect the logo. Play with the parameters to improve detection
      if (cascadeClassifier != null) {
              cascadeClassifier.detectMultiScale(grayscaleImage, burger_king, 1.1,3,3,
                  new Size(absoluteLogoSize, absoluteLogoSize), new Size());
      }

``` 

* Draw a rectangle around that logo and place the image choice
``` java
// Save the logos in an array
Rect[] burger_kingArray = burger_king.toArray();
 
// Placeholder matrix for our image of choice
Mat b = new Mat();
 
// Loop through all logos 
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
          
    // Convert the image of choice (bitmap) to the matrix placeholder we defined (b)
          Utils.bitmapToMat(bitmap, b);
          Imgproc.resize(b, b, new Size(ixd2 - ixd1, iyd2 - iyd1));
         
// Place the image of our choice in the rectangle 
          Mat sub  = inputFrame.submat(roi);
          Imgproc.cvtColor(sub, sub, Imgproc.COLOR_RGBA2GRAY);
          Imgproc.cvtColor(sub, sub, Imgproc.COLOR_GRAY2RGB);
          b.copyTo(inputFrame.submat(roi));            
      }
 
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
``` 
 

 

