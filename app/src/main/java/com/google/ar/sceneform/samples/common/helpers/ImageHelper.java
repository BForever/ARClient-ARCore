package com.google.ar.sceneform.samples.common.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ImageHelper {
    private static String TAG="ImageHelper";

    private static byte[] NV21toJPEG(byte[] nv21, int width, int height,int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), quality, out);
        return out.toByteArray();
    }
    private static void NV21toJPEG(byte[] nv21, int width, int height,OutputStream out) {
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), 30, out);
    }

    public static Bitmap ImagetoBitmap(Image image){
        byte[] bytes = NV21toJPEG(YUV_420_888toNV21(image),image.getWidth(),image.getHeight(),100);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        return bitmap;
    }


//    public static void WriteImageInformation(Image image, String path) {
//        byte[] data = null;
//        data = NV21toJPEG(YUV_420_888toNV21(image),
//                image.getWidth(), image.getHeight());
//        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
//        bos.write(data);
//        bos.flush();
//        bos.close();
//    }

    private static byte[] YUV_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    public static void Save(Image image, File path,String filename){
//        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//        byte[] bytes = new byte[buffer.capacity()];
//        buffer.get(bytes);
//        byte[] bytes = null;
//        bytes = NV21toJPEG(YUV_420_888toNV21(image),
//                image.getWidth(), image.getHeight());
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);


//        if(bitmapImage!=null){
//            Log.e("bitmap","bitmap decode success");
//        }else {
//            Log.e("bitmap","bitmap decode fail");
//        }

        File file;
        file = new File(path, filename+".jpg");

        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            NV21toJPEG(YUV_420_888toNV21(image), image.getWidth(), image.getHeight(),stream);
//            stream.write(bytes);
            Log.e("AugmentedImageActivity",file.getAbsolutePath());
//            bitmapImage.compress(Bitmap.CompressFormat.JPEG,100,stream);

            stream.flush();
            stream.close();

        }catch (IOException e) // Catch the exception
        {
            Log.e("AugmentedImageActivity",e.getMessage());
            e.printStackTrace();
        }
    }

    public static void Save(Image image,OutputStream out){
        NV21toJPEG(YUV_420_888toNV21(image), image.getWidth(), image.getHeight(),out);
    }
}
