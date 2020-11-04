package org.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Algorithm;
import org.bytedeco.javacpp.opencv_imgproc;
import  org.bytedeco.javacpp.opencv_shape;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_imgproc.*;
import org.kivy.android.PythonActivity;


import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.nio.file.Files;

import android.content.SharedPreferences;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.resetTrace;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static  org.bytedeco.javacpp.opencv_imgproc.*;

//import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
/**
 * Methods that are expected to be called via JNI, to access the
 * device's non-screen hardware. (For example, the vibration and
 * accelerometer.)
 */
public class Hardware {
    public static int state;

    public static void train() {
        state=0;
        Log.v("My",Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files");
        String[] args = {Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files" ,Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/1-selfie_1.png"};
        String trainingDir = args[0];
        Log.v("My","Hi");
        Mat testImage = imread(args[1], CV_LOAD_IMAGE_GRAYSCALE);
        String testFile = args[1];

        File root = new File(trainingDir);
        Log.v("My","Hi");
        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                boolean bool_=false;
                if (name.endsWith(".png")){
                    bool_=true;
                }
                if (name.startsWith("predict")){
                    bool_=false;
                }
                return bool_;
            }
        };
        Log.v("My","Hi");
        File[] imageFiles = root.listFiles(imgFilter);
        MatVector images = new MatVector(imageFiles.length+7);

        Mat labels = new Mat(imageFiles.length+7, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();
        Log.v("My","Hi");
        opencv_objdetect.CascadeClassifier face_cascade = new opencv_objdetect.CascadeClassifier(
                Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/app/lbpcascade_frontalface_improved.xml");
        Log.v("My","Hero");
        opencv_core.RectVector faces = new opencv_core.RectVector();



        int counter = 0;
        for (int i=0; i<7; i++){
            Mat img = imread(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/app/gg"+Integer.toString(i)+"g.png", CV_LOAD_IMAGE_GRAYSCALE);
            face_cascade.detectMultiScale(img, faces);
            int label = Integer.parseInt("0");
            Mat mat = new Mat(img,faces.get(0));
            if (counter != 0) opencv_imgproc.resize(mat, mat, images.get(0).size());
            images.put(counter, mat);
            labelsBuf.put(counter, label);
            counter++;
            Log.v("Hi",Integer.toString(i));
        }
        for (File image : imageFiles) {
            if (!image.getName().startsWith("predict")){
                Log.v("My",image.getName());
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            face_cascade.detectMultiScale(img, faces);
                int label = Integer.parseInt(image.getName().split("\\-")[0])+1;
                Mat mat = new Mat(img,faces.get(0));
                    if (counter != 0) opencv_imgproc.resize(mat, mat, images.get(0).size());
                images.put(counter, mat);
                imwrite(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/app/ggfff"+Integer.toString(counter)+"g.png",mat);
                labelsBuf.put(counter, label);
                counter++;
            }
        }
       // FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
         //FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
        FaceRecognizer faceRecognizer = createLBPHFaceRecognizer(2,10,10,10,20.0);
        Log.v("My","Hi");
        faceRecognizer.train(images, labels);
        Log.v("My","Hi");
        //IntPointer label = new IntPointer(1);
        Log.v("My","Hi");
        //DoublePointer confidence = new DoublePointer(1);
        Log.v("My","Hi");

        /*SharedPreferences prefs = PythonActivity.getContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("size2",images.get(0).size().height());
        editor.putInt("size1",images.get(0).size().width()).apply();*/

        Log.v("My","Hi");
        //face_cascade.detectMultiScale(testImage, faces);
        //Mat mat = new Mat(testImage,faces.get(0));
        //opencv_imgproc.resize(mat, mat, images.get(0).size());
        //faceRecognizer.predict(mat, label, confidence);
        Log.v("My","Hi");
        //int predictedLabel = label.get(0);
        Log.v("My","Hi");
        faceRecognizer.save(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/mymodel.xml");
        //System.out.println("Predicted label: " + predictedLabel);
        //System.out.println("Predicted: " + confidence.get(0));
        state=1;
    }
}