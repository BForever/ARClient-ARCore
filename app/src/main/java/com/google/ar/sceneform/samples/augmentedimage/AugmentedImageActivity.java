/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.augmentedimage;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.CameraIntrinsics;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * ArAugmentedImage_getTrackingMethod() and render only when the tracking method equals to
 * AR_AUGMENTED_IMAGE_TRACKING_METHOD_FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/c/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public class AugmentedImageActivity extends AppCompatActivity {
    private String TAG="AugmentedImageActivity";
    // UI
    public ArFragment arFragment;
    public ArSceneView arSceneView;
    public LinearLayout linearLayout;
    public Button button;
    public EditText editText;
    public GestureDetector gestureDetector;
    // AR setting
    public Scene scene;
    public Session session;
    // Pose
    public AnchorNode anchorNode;
    public Pose anchorPose;
    private Pose hittedPose;
    // Labels
    public boolean hasPlacedLabels;
    public PoseInfo[] poseInfos;
    public String SERVER_ADDRESS="http://47.103.3.12";
    public String UI_SERVER_PORT="8000";


    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);


        gestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                onSingleTap(e);
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });


//        uploadController = new UploadController(this, frameLayout);
        linearLayout = findViewById(R.id.linearlayout);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);

        button.setOnClickListener(this::buttonOnClick);

        hasPlacedLabels = true;

        PoseServer poseServer = new PoseServer(this,false);
        PoseInfo[] poseInfos = new PoseInfo[1];
        poseInfos[0]=new PoseInfo();
        poseServer.execute(poseInfos);


    }


    private Pose getOffsetPose(){
        return this.anchorPose.inverse().compose(hittedPose);
    }
    private Vector3 getOffsetPosition(){

        float[] hittrans = hittedPose.getTranslation();
        hittrans = this.anchorNode.getAnchor().getPose().inverse().rotateVector(hittrans);
        float[] offset = this.anchorNode.getAnchor().getPose().inverse().transformPoint(hittrans);
        return new Vector3(offset[0],offset[1],offset[2]);
    }
    private void buttonOnClick(View view){
        String text = editText.getText().toString();
        Log.e("text",text);

        Node card = new WebNode(anchorNode, this, getOffsetPose(),SERVER_ADDRESS+":"+UI_SERVER_PORT+"/"+text+".html");
        PoseInfo info = new PoseInfo(getOffsetPose(),text);

        PoseServer poseServer = new PoseServer(this,true);
        poseServer.execute(info);

        linearLayout.setVisibility(View.INVISIBLE);
        linearLayout.requestLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (session == null) {
                session = new Session(this);

                CameraConfigFilter filter = new CameraConfigFilter(session);
//                filter.setTargetFps(EnumSet.of(CameraConfig.TargetFps.TARGET_FPS_30));
                filter.setDepthSensorUsage(EnumSet.of(CameraConfig.DepthSensorUsage.
                        DO_NOT_USE));
                CameraConfig cameraConfig = session.getCameraConfig();
                session.setCameraConfig(cameraConfig);


                Config config = session.getConfig();
                config.setFocusMode(Config.FocusMode.AUTO);
                config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                ((AugmentedImageFragment) arFragment).setupAugmentedImageDatabase(config, session);
                session.configure(config);

                arSceneView = arFragment.getArSceneView();
                arSceneView.setupSession(session);

                scene = arFragment.getArSceneView().getScene();
                scene.addOnUpdateListener(this::onUpdateFrame);
                scene.setOnTouchListener((HitTestResult hitTestResult, MotionEvent event) -> {

                    gestureDetector.onTouchEvent(event);
                    return true;

                });
            }
        } catch (UnavailableException e) {
            handleSessionException(this, e);
        }
    }

    private void onSingleTap(MotionEvent tap) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        if (frame != null) {
//            CameraIntrinsics cameraIntrinsics = frame.getCamera().getImageIntrinsics();
//            float[]focal = cameraIntrinsics.getFocalLength();
//            int[]dims = cameraIntrinsics.getImageDimensions();
//            float[]prin = cameraIntrinsics.getPrincipalPoint();
//            Log.e("intrinsic",""+focal[0]+" "+focal[1]);
//            Log.e("intrinsic",""+dims[0]+" "+dims[1]);
//            Log.e("intrinsic",""+prin[0]+" "+prin[1]);
            placeLabel(tap, frame);
        }
    }

    private void placeLabel(MotionEvent tap, Frame frame) {
        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                Log.i(TAG, hit.getTrackable().getClass().getName());
            }
            for (HitResult hit : frame.hitTest(tap)) {
                Trackable trackable = hit.getTrackable();
//                if (trackable instanceof Point) {
                // Create the Anchor.
//                Anchor anchor = hit.createAnchor();
//                AnchorNode anchorNode = new AnchorNode(anchor);
//                anchorNode.setParent(arSceneView.getScene());
//                Node card = new WebNode(scene, this,"http://10.181.155.21:8000/chenhongcao.html");
                hittedPose = hit.getHitPose();
                Log.e("Pose", hit.getHitPose().toString());
                linearLayout.setVisibility(View.VISIBLE);
                linearLayout.requestLayout();
//                }
            }
        }
    }

    private void prePlaceLabels() {
        if(this.poseInfos==null){
            return;
        }
        for(PoseInfo info : this.poseInfos){
            Log.d(TAG,"placing item: "+info.name+info.pose[0]+info.pose[1]+info.pose[2]);
//            new WebNode(anchorNode,this, new Vector3(info.pose[0],info.pose[1],info.pose[2]),SERVER_ADDRESS+":8000/"+info.name+".html");
            new WebNode(anchorNode,this, info.getPose(),SERVER_ADDRESS+":"+UI_SERVER_PORT+"/"+info.name+".html");
        }
//        Node card = new LabelNode(anchorNode, this, Pose.makeTranslation(3.6f, 0.2f, 0.2f), "刘汶鑫");
//        card = new LabelNode(anchorNode, this, Pose.makeTranslation(1.2f, 0.2f, 0.2f), "蔡振宇");
//        card = new LabelNode(anchorNode, this, Pose.makeTranslation(2.4f, 0.2f, 0.2f), "曾思钰");
//        card = new LabelNode(anchorNode, this, Pose.makeTranslation(4.8f, 0.2f, 0.2f), "袁宇");
//        card = new LabelNode(anchorNode, this, Pose.makeTranslation(6f, 0.2f, 0.2f), "周寒");
//        Node web = new WebNode(anchorNode, this, new Vector3(0, 0.2f, 0f), "范宏昌");

    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {

        if (!hasPlacedLabels&&anchorNode!=null) {
            Log.e(TAG,"start placeing...");
            prePlaceLabels();
            hasPlacedLabels = true;
        }
        Frame frame = arFragment.getArSceneView().getArFrame();
        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
//                    try {
//                        Image image = frame.acquireCameraImage();
//                        Bitmap bitmap = ImageHelper.ImagetoBitmap(image);
//                        String result = QRHelper.getReult(bitmap);
//                        Log.e("QR", "result: "+result);
//
//                    } catch (NotYetAvailableException ex) {
//                        ex.printStackTrace();
//                    }
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
//          fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        AugmentedImageNode node = new AugmentedImageNode(this);
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        arFragment.getArSceneView().getScene().addChild(node);

                        String text = "Detected Image " + augmentedImage.getName() + " Pose: " + augmentedImage.getCenterPose().toString();
                        Log.e("pose", augmentedImage.getCenterPose().toString());
                        anchorPose = augmentedImage.getCenterPose();
                        Log.e(TAG,anchorPose.toString());
                        if(anchorNode==null) {
                            anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
                            anchorNode.setParent(scene);
                        }
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    public static void handleSessionException(
            Activity activity, UnavailableException sessionException) {

        String message;
        if (sessionException instanceof UnavailableArcoreNotInstalledException) {
            message = "Please install ARCore";
        } else if (sessionException instanceof UnavailableApkTooOldException) {
            message = "Please update ARCore";
        } else if (sessionException instanceof UnavailableSdkTooOldException) {
            message = "Please update this app";
        } else if (sessionException instanceof UnavailableDeviceNotCompatibleException) {
            message = "This device does not support AR";
        } else {
            message = "Failed to create AR session";
            Log.e(activity.getLocalClassName(), "Exception: " + sessionException);
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
    }
}
