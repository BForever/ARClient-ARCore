package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

public class WebNode extends Node {

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

    private void setupWebView(Context context,String url){
        ViewRenderable.builder()
                .setView(context, R.layout.web_view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            this.setRenderable(renderable);
                            WebView webView = (WebView)renderable.getView();
                            //支持缩放
                            WebSettings webSettings = webView.getSettings();
                            webSettings.setUseWideViewPort(true);
                            webSettings.setLoadWithOverviewMode(true);
                            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
                            webView.setInitialScale(55);
                            webView.setWebViewClient(new WebViewClient(){
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    return false;// 返回false
                                }
                            });
                            webView.loadUrl(url);
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
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        this.setWorldRotation(lookRotation);
    }
}
