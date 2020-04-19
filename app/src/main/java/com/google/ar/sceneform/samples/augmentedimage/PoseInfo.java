package com.google.ar.sceneform.samples.augmentedimage;

import android.util.Log;

import com.google.ar.core.Pose;
import com.google.ar.sceneform.math.Vector3;

public class PoseInfo {
    public String name;
    public float[] pose;
    public String response;

    public PoseInfo(){
        pose = new float[7];
    }
    public PoseInfo(Pose pose, String name){
        this.name = name;
        this.pose = new float[7];
        this.pose[0]=pose.tx();
        this.pose[1]=pose.ty();
        this.pose[2]=pose.tz();
        this.pose[3]=pose.qx();
        this.pose[4]=pose.qy();
        this.pose[5]=pose.qz();
        this.pose[6]=pose.qw();
    }
    public PoseInfo(Vector3 pose, String name){
        this.name = name;
        this.pose = new float[7];
        this.pose[0]=pose.x;
        this.pose[1]=pose.y;
        this.pose[2]=pose.z;
    }

    public Pose getPose(){
        Pose pose=new Pose(new float[]{this.pose[0],this.pose[1],this.pose[2]},new float[]{this.pose[3],this.pose[4],this.pose[5],this.pose[6]});
        return pose;
    }

}
