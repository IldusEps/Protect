package org.myapp;

import android.os.Environment;
import android.util.Log;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_objdetect;
import org.kivy.android.PythonActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.nio.file.Files;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

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

        File root = new File(trainingDir);
        Log.v("My","Hi");
        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                boolean bool_=false;
                if ( name.endsWith(".png")){
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

        MatVector images = new MatVector(imageFiles.length);

        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.createBuffer();
        Log.v("My","Hi");
        opencv_objdetect.CascadeClassifier face_cascade = new opencv_objdetect.CascadeClassifier(
                Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/app/lbpcascade_frontalface.xml");
        Log.v("My","Hero");
        opencv_core.RectVector faces = new opencv_core.RectVector();
        int counter = 0;

        for (File image : imageFiles) {
            if (!image.getName().startsWith("predict")){
                Log.v("My",image.getName());
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            face_cascade.detectMultiScale(img, faces);

            int label = Integer.parseInt(image.getName().split("\\-")[0]);

            images.put(counter, new Mat(img,faces.get(0)));

            labelsBuf.put(counter, label);

            counter++;}
        }

        //FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
        // FaceRecognizer faceRecognizer = EigenFaceRecognizer.create();
         FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();
        Log.v("My","Hi");
        faceRecognizer.train(images, labels);
        Log.v("My","Hi");
        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);
        faceRecognizer.predict(testImage, label, confidence);
        int predictedLabel = label.get(0);
        faceRecognizer.save(Environment.getDataDirectory().getAbsolutePath()+"/data/org.kivy.protectid/files/mymodel.xml");
        System.out.println("Predicted label: " + predictedLabel);
        System.out.println("Predicted: " + confidence.get(0));
        state=1;
    }
}