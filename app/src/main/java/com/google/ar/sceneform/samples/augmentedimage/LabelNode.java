package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.NodeParent;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

public class LabelNode extends Node {
    private static float INFO_CARD_Y_POS_COEFF = 0.2f;


    LabelNode(NodeParent parent, Context context,String text){
        this.setParent(parent);
        this.setEnabled(true);
        this.setLocalPosition(new Vector3(0.0f, INFO_CARD_Y_POS_COEFF, -0.2f));

        ViewRenderable.builder()
                .setView(context, R.layout.planet_card_view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            this.setRenderable(renderable);
                            TextView textView = (TextView) renderable.getView();
                            textView.setText(text);
                        })
                .exceptionally(
                        (throwable) -> {
                            throw new AssertionError("Could not load plane card view.", throwable);
                        });
    }

    LabelNode(NodeParent parent, Context context,Pose pose,String text){
        this.setParent(parent);
        this.setEnabled(true);
        this.setLocalPosition(new Vector3(pose.tx(), pose.ty(), -pose.tz()));

        ViewRenderable.builder()
                .setView(context, R.layout.planet_card_view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            this.setRenderable(renderable);
                            TextView textView = (TextView) renderable.getView();
                            textView.setText(text);
                        })
                .exceptionally(
                        (throwable) -> {
                            throw new AssertionError("Could not load plane card view.", throwable);
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
