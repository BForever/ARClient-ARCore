package com.google.ar.sceneform.samples.common.helpers;

import android.app.Activity;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.ar.core.Frame;
import java.util.LinkedList;
import java.util.List;

public class UploadController {
    private Activity activity=null;
    private Boolean uploading = false;
    private Image image = null;
    private Frame frame = null;
    private FrameLayout frameLayout = null;

    public class DetectResult {
        public String name;
        private float probability;
        private float center_x, center_y, width, height;//origin is left-top
    }

    private List<DetectResult> results = null;
    private List<View> views = null;

    public UploadController(Activity activity, FrameLayout frameLayout){
        this.activity = activity;
        this.frameLayout=frameLayout;
    }

    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public void setUploading(Image image, Frame frame) {
        uploading = true;
        this.image = image;
        this.frame = frame;
    }

    public void setUploadingDone() {
        uploading = false;
        image.close();
        frame = null;
    }

    public Boolean getUploading() {
        return uploading;
    }

    public void setResults(String result) {
        String midres = result.substring(1,result.length()-1);
        midres = midres.replace("\"","");
        midres = midres.replace("(","");
        midres = midres.replace(")","");
        String []res = midres.split(",");
        results = new LinkedList<>();

        for(int i=0;i<res.length/6;i++){
            DetectResult detectResult = new DetectResult();
            detectResult.name = res[i*6];
            detectResult.name = detectResult.name.substring(detectResult.name.indexOf('\'')+1,detectResult.name.length()-1);
            detectResult.probability = Float.valueOf(res[i*6+1]);
            detectResult.center_x = Float.valueOf(res[i*6+2]);
            detectResult.center_y = Float.valueOf(res[i*6+3]);
            detectResult.width = Float.valueOf(res[i*6+4]);
            detectResult.height = Float.valueOf(res[i*6+5]);
            results.add(detectResult);
        }
//        Gson gson = new Gson();
//        UploadController.DetectResult[] detectResults = gson.fromJson(result, UploadController.DetectResult[].class);
//        res = Arrays.asList(detectResults);
    }

    public void drawTextView() {
        views = new LinkedList<>();
        if(results!=null){
            for (UploadController.DetectResult it : results) {
                TextView textView = new TextView(activity);
                frameLayout.addView(textView);
                views.add(textView);
                textView.setText(it.name);
                textView.setTextSize(20);
                textView.setTextColor(Color.GREEN);
                textView.setVisibility(View.VISIBLE);
                textView.setBackgroundColor(Color.argb(60,0,255,0));
                int width = (int)(frameLayout.getWidth()*it.width/640f);
                int height = (int)(frameLayout.getHeight()*it.height/480f);
//                int width = (int)(100*it.width/640f);
//                int height = (int)(100*it.height/480f);
                Log.e("UIscale-width",String.valueOf(width));
                Log.e("UIscale-height",String.valueOf(height));
                Log.e("UIscale-frameLayoutwidth",String.valueOf(frameLayout.getWidth()));
                Log.e("UIscale-frameLayoutheight",String.valueOf(frameLayout.getHeight()));
                textView.getLayoutParams().width=width;
                textView.getLayoutParams().height=height;
                textView.requestLayout();
//                textView.setWidth(width);
//                textView.setHeight(height);
                textView.setTranslationX(frameLayout.getWidth()*(it.center_x-it.width/2)/640f);
                textView.setTranslationY(frameLayout.getHeight()*(it.center_y-it.height/2)/480f);

            }
            results=null;
        }

    }

    public void clearTextView() {
//        results = null;
//        if(views!=null){
//            for(View view :views){
//                frameLayout.removeView(view);
//            }
//            views = null;
//        }

        frameLayout.removeAllViewsInLayout();
//        frameLayout.removeAllViews();
    }
}
