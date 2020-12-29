package com.google.ar.sceneform.samples.common.helpers;

import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class GeoHelper {
    static public Vector3 ToEulerAngles(Quaternion q) {
        Vector3 angles = new Vector3();

        // roll (x-axis rotation)
        double sinr_cosp = 2. * (q.w * q.x + q.y * q.z);
        double cosr_cosp = 1. - 2. * (q.x * q.x + q.y * q.y);
        angles.x =  (float)Math.atan2 (sinr_cosp, cosr_cosp);

        // pitch (y-axis rotation)
        double sinp = 2. * (q.w * q.y - q.z * q.x);
        if (Math.abs (sinp) >= 1)
        angles.y = (float)Math.copySign (Math.PI / 2., sinp); // use 90 degrees if out of range
    else
        angles.y = (float)Math.asin (sinp);

        // yaw (z-axis rotation)
        double siny_cosp = 2. * (q.w * q.z + q.x * q.y);
        double cosy_cosp = 1. - 2. * (q.y * q.y + q.z * q.z);
        angles.z = (float)Math.atan2 (siny_cosp, cosy_cosp);

        return angles;
    }

    static public Quaternion ChangeXaxis(Vector3 angles){
        float rad = 0.0174532925199444f;

        Quaternion qx = new Quaternion(new Vector3(1,0,0),-angles.x*rad);// 得出在中间过渡坐标系C中绕X轴的旋转四元数
        Quaternion qy = new Quaternion(new Vector3(0,1,0),-angles.y*rad);// 得出在中间过渡坐标系C中绕Y轴的旋转四元数
        Quaternion qz = new Quaternion(new Vector3(0,0,1),-angles.z*rad);//  得出在中间过渡坐标系C中绕Z轴的旋转四元数

        // 中间过渡坐标系的旋转顺序为ZYX，则中间过渡坐标系的计算结果如下：
        Quaternion qFirst = Quaternion.multiply(Quaternion.multiply(qz,qy),qx);
        Quaternion qSecond= new Quaternion(new Vector3(0,1,0),-90*rad); // 绕Y轴顺时针旋转90度。
        return Quaternion.multiply(qFirst, qSecond);
    }

}
