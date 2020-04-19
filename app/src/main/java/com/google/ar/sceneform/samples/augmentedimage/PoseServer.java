package com.google.ar.sceneform.samples.augmentedimage;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.google.ar.sceneform.samples.augmentedimage.AugmentedImageActivity;
import com.google.ar.sceneform.samples.augmentedimage.PoseInfo;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PoseServer extends AsyncTask<PoseInfo, Void, PoseInfo[]> {
    private final String TAG = "PoseServer";
    private boolean upload = false;
    private long startTime;
    private final String SERVER_ADDRESS;
    private AugmentedImageActivity activity;


    public PoseServer(AugmentedImageActivity activity,boolean upload) {
        this.activity = activity;
        this.upload = upload;
        SERVER_ADDRESS = activity.SERVER_ADDRESS+":5000/pose";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "uploading start");
        startTime = SystemClock.uptimeMillis();
    }

    @Override
    protected PoseInfo[] doInBackground(PoseInfo... infos) {
        if (upload) {
            return post(infos[0], SERVER_ADDRESS);
        } else {
            return get(infos[0], SERVER_ADDRESS);
        }

    }

    @Override
    protected void onPostExecute(PoseInfo[] poseInfos) {
        super.onPostExecute(poseInfos);
        if(!upload){
            activity.poseInfos = poseInfos;
            activity.hasPlacedLabels=false;
        }
        Log.i(TAG, "uploading done, elapsed: " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms");
    }

    private PoseInfo[] get(PoseInfo infos, String serverAddr) {
        try {
            URL url = new URL(serverAddr);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setDoInput(true);
            c.setRequestMethod("GET");
            c.connect();

            Log.i(TAG, "connected, elapsed: " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms");
            PoseInfo[] poseInfos;
            Gson res = new Gson();
            if (c.getResponseCode() < 400) {
                InputStreamReader inputStreamReader = new InputStreamReader(c.getInputStream());

                poseInfos = res.fromJson(inputStreamReader, PoseInfo[].class);
                Log.i(TAG, "response fetched, elapsed: " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms");
                Log.i(TAG, "Successfully get pose: " + poseInfos.length + " items");
            } else {
                InputStreamReader inputStreamReader = new InputStreamReader(c.getInputStream());
                PoseInfo poseInfo;
                poseInfo = res.fromJson(inputStreamReader, PoseInfo.class);

                poseInfos = new PoseInfo[1];
                poseInfos[0]=poseInfo;

                Log.e(TAG, "Error posting pose: " + poseInfo.response);
            }
            return poseInfos;
        } catch (IOException e) {
            Log.e(TAG, "Error in POST", e);
            return null;
        }
    }

    private PoseInfo[] post(PoseInfo info, String serverAddr) {
        try {
            URL url = new URL(serverAddr);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setDoInput(true);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.connect();

            Log.i(TAG, "connected, elapsed: " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms");
            OutputStream output = c.getOutputStream();

            Gson gson = new Gson();
            output.write(gson.toJson(info, PoseInfo.class).getBytes());
            output.close();

            Log.i(TAG, "json transferred, elapsed: " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms");

            PoseInfo[] poseInfos = new PoseInfo[1];
            if (c.getResponseCode() < 400) {
                InputStreamReader inputStreamReader = new InputStreamReader(c.getInputStream());
                poseInfos[0] = gson.fromJson(inputStreamReader, PoseInfo.class);
                Log.i(TAG, "response fetched, elapsed: " + String.valueOf(SystemClock.uptimeMillis() - startTime) + "ms");
                Log.i(TAG, "Successfully posted pose: " + poseInfos[0].response);
            } else {
                InputStreamReader inputStreamReader = new InputStreamReader(c.getInputStream());
                poseInfos[0] = gson.fromJson(inputStreamReader, PoseInfo.class);

                Log.e(TAG, "Error posting pose: " + poseInfos[0].response);
            }
            return poseInfos;
        } catch (IOException e) {
            Log.e(TAG, "Error in POST", e);
            return null;
        }
    }
}
