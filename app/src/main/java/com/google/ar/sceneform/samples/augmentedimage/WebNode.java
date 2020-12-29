package com.google.ar.sceneform.samples.augmentedimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.rendering.ViewSizer;
import com.google.ar.sceneform.samples.common.helpers.GeoHelper;

public class WebNode extends Node {
    public Renderable renderable;
    public boolean focus =false;
    public long debounce = 0;

    // create new node upon parent
    WebNode(NodeParent parent, Context context, String url){
        float INFO_CARD_Y_POS_COEFF = 0.2f;
        this.setParent(parent);
        this.setEnabled(true);
        this.setLocalPosition(new Vector3(0.0f, INFO_CARD_Y_POS_COEFF, -0.2f));
        this.setWorldRotation(Quaternion.identity());


        setupWebView(context,url);
    }
    // create new node in front of image
    WebNode(NodeParent parent, Context context, Vector3 position, String url){
        this.setParent(parent);
        this.setEnabled(true);
        this.setLocalPosition(new Vector3(position.x, position.y, position.z));
//        this.setLocalRotation(Quaternion.axisAngle(Vector3.left(),90f));

        setupWebView(context,url);
    }

    WebNode(NodeParent parent, Context context, Vector3 position,Pose anchorPose, String url){
        this.setParent(parent);
        this.setEnabled(true);
        float[] pos = new float[3];
        pos[0]=position.x;
        pos[1]=position.y;
        pos[2]=position.z;
        pos = anchorPose.rotateVector(pos);

        this.setLocalPosition(new Vector3(pos[0],pos[1],pos[2]));
//        this.setLocalRotation(Quaternion.axisAngle(Vector3.left(),90f));

        setupWebView(context,url);
    }

    // create new node in world position
    WebNode(NodeParent parent, Context context, Pose pose, String url){
        this.setParent(parent);
        this.setEnabled(true);


        Vector3 pos = new Vector3(pose.tx(),pose.ty(),pose.tz());
//        float[] trans = pose.getTranslation();
//        Vector3 pos = new Vector3(trans[0],trans[1],trans[2]);
        Quaternion rot = new Quaternion(pose.qx(),pose.qy(),pose.qz(),pose.qw());
        this.setLocalPosition(pos);
        this.setLocalRotation(rot);

//        this.setLocalRotation(Quaternion.axisAngle(Vector3.left(),90f));

        setupWebView(context,url);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupWebView(Context context, String url){
        ViewRenderable.builder()
                .setView(context, R.layout.web_view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            this.setRenderable(renderable);
                            this.renderable = renderable;
                            renderable.setSizer(new ViewSizer() {
                                @Override
                                public Vector3 getSize(View view) {
                                    return new Vector3(0.5f,0.4f,1f);
                                }
                            });
                            WebView webView = (WebView)renderable.getView();
                            //支持缩放
                            WebSettings webSettings = webView.getSettings();
                            webSettings.setUseWideViewPort(true);
                            webSettings.setLoadWithOverviewMode(true);
                            webSettings.setJavaScriptEnabled(true);
                            webSettings.setDomStorageEnabled(true);
                            webSettings.setAllowFileAccessFromFileURLs(true);
//                            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
                            webView.setInitialScale(280);
//                            webView.setWebViewClient(new WebViewClient(){
//                                @Override
//                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                                    return false;// 返回false
//                                }
//                            });
                            webView.loadUrl(url);
                            webView.setOnTouchListener(new View.OnTouchListener(){
                                @Override
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    Log.e("SIZE","setSizer");
                                    if(SystemClock.uptimeMillis()-debounce<500){
                                        debounce = SystemClock.uptimeMillis();
                                        return false;
                                    }
                                    if(!focus){
                                        renderable.setSizer(new ViewSizer() {
                                            @Override
                                            public Vector3 getSize(View view) {
                                                return new Vector3(1f,0.8f,1f);
                                            }
                                        });
//                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600,480);
//                                        view.setLayoutParams(params);
                                        focus = true;
                                    }else {
                                        renderable.setSizer(new ViewSizer() {
                                            @Override
                                            public Vector3 getSize(View view) {
                                                return new Vector3(0.5f,0.4f,1f);
                                            }
                                        });
//                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300,240);
//                                        view.setLayoutParams(params);
                                        focus = false;
                                    }
                                    debounce = SystemClock.uptimeMillis();
                                    return true;
                                }
                            });
                        })
                .exceptionally(
                        (throwable) -> {
                            throw new AssertionError("Could not load web view.", throwable);
                        });

    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = this.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        direction.y = 0;
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
//        Vector3 angles = GeoHelper.ToEulerAngles(lookRotation);
//        this.setWorldRotation(Quaternion.axisAngle(Vector3.up(),angles.z));
        this.setWorldRotation(lookRotation);
    }
}
