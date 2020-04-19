package com.google.ar.sceneform.samples.common.helpers;

import android.media.Image;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.google.ar.sceneform.samples.augmentedimage.AugmentedImageActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ImageUploader extends AsyncTask<Image,Void,Boolean> {
    private final String TAG = "ImageUploader";
    private UploadController uploadController = null;
    private long startTime;
//    private String SERVER_ADDRESS = "http://192.168.1.127:5000/predict";
    private final String SERVER_ADDRESS = "http://47.111.148.247:5000/predict";

    public ImageUploader(AugmentedImageActivity activity,UploadController uploadController){
        this.uploadController = uploadController;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "uploading start");
        startTime = SystemClock.uptimeMillis();

    }

    @Override
    protected Boolean doInBackground(Image... bitmaps) {
        return upload(bitmaps[0],SERVER_ADDRESS);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        Log.i(TAG,"uploading done, elapsed: "+String.valueOf(SystemClock.uptimeMillis()-startTime)+"ms");
        uploadController.setUploadingDone();
        uploadController.clearTextView();
        uploadController.drawTextView();
        uploadController=null;

    }

    private Boolean upload(Image image, String serverAddr){
        try {
            URL url = new URL(serverAddr);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setDoInput(true);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.connect();
            Log.i(TAG,"connected, elapsed: "+String.valueOf(SystemClock.uptimeMillis()-startTime)+"ms");
            OutputStream output = c.getOutputStream();
            ImageHelper.Save(image,output);
            output.close();
            Log.i(TAG,"image transferred, elapsed: "+String.valueOf(SystemClock.uptimeMillis()-startTime)+"ms");
            if(c.getResponseCode()<400){
                Scanner result = new Scanner(c.getInputStream());
                String res = result.nextLine();
                if(!res.equals("[]")){
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(res);
                    while (result.hasNextLine()){
                        stringBuilder.append(result.nextLine());
                    }
                    String response = stringBuilder.toString();
                    Log.i(TAG,"response fetched, elapsed: "+String.valueOf(SystemClock.uptimeMillis()-startTime)+"ms");
                    Log.i(TAG, "Successfully uploaded image: "+response);
                    uploadController.setResults(response);

                }else {
                    Log.i(TAG, "Successfully uploaded image, no object detected");
                    uploadController.clearTextView();
                }
                result.close();
            }else{
                Scanner result = new Scanner(c.getErrorStream());
                Log.e(TAG, "Error uploading image: " + result.nextLine());
                while (result.hasNextLine()){
                    Log.e(TAG,  result.nextLine());
                }
                result.close();
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error uploading image", e);
            return false;
        }
    }
}
