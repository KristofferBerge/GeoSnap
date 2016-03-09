package com.kristomb.geosnap.Controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by krist on 09-Mar-16.
 */
public class ImageProcessor {


    //99.9% copypaste. This class handles problems with some phones rotating images. It compresses to avoid OutOfMemoryException, reads metadata about rotation, rotates if needed and saves.
    //http://stackoverflow.com/questions/12777386/android-captured-image-to-be-in-portrait


    public static void resampleImageAndSaveToNewLocation(Context context) throws Exception
    {
        String dir = String.valueOf(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        String filepath = dir + "/cachedImage.jpg";
        Bitmap bmp = resampleImage(filepath, 800);
        OutputStream out = new FileOutputStream(filepath);
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
    }

    public static Bitmap resampleImage(String path, int maxDim) throws Exception
    {
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bfo);

        BitmapFactory.Options optsDownSample = new BitmapFactory.Options();
        optsDownSample.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);

        Bitmap bmpt = BitmapFactory.decodeFile(path, optsDownSample);

        Matrix m = new Matrix();

        if (bmpt.getWidth() > maxDim || bmpt.getHeight() > maxDim)
        {
            BitmapFactory.Options optsScale = getResampling(bmpt.getWidth(), bmpt.getHeight(), maxDim);
            m.postScale((float)optsScale.outWidth  / (float)bmpt.getWidth(), (float)optsScale.outHeight / (float)bmpt.getHeight());
        }

        int sdk = new Integer(Build.VERSION.SDK).intValue();
        if (sdk > 4)
        {
            int rotation = getExifRotation(path);
            if (rotation != 0)
            {
                m.postRotate(rotation);
            }
        }

        return Bitmap.createBitmap(bmpt, 0, 0, bmpt.getWidth(), bmpt.getHeight(), m, true);
    }

    private static BitmapFactory.Options getResampling(int cx, int cy, int max)
    {
        float scaleVal = 1.0f;
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        if (cx > cy)
        {
            scaleVal = (float)max / (float)cx;
        }
        else if (cy > cx)
        {
            scaleVal = (float)max / (float)cy;
        }
        else
        {
            scaleVal = (float)max / (float)cx;
        }
        bfo.outWidth  = (int)(cx * scaleVal + 0.5f);
        bfo.outHeight = (int)(cy * scaleVal + 0.5f);
        return bfo;
    }

    private static int getClosestResampleSize(int cx, int cy, int maxDim)
    {
        /*Log.e("cx",""+cx);
        Log.e("cy",""+cy);*/
        int max = Math.max(cx, cy);

        int resample = 1;
        for (resample = 1; resample < Integer.MAX_VALUE; resample++)
        {
            if (resample * maxDim > max)
            {
                resample--;
                break;
            }
        }

        if (resample > 0)
        {
            return resample;
        }
        return 1;
    }

    public static BitmapFactory.Options getBitmapDims(String path) throws Exception
    {
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bfo);
        return bfo;
    }

    public static int getExifRotation(String imgPath)
    {
        try
        {
            ExifInterface exif = new ExifInterface(imgPath);
            String rotationAmount = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            if (!TextUtils.isEmpty(rotationAmount))
            {
                int rotationParam = Integer.parseInt(rotationAmount);
                switch (rotationParam)
                {
                    case ExifInterface.ORIENTATION_NORMAL:
                        System.out.println("ROTATION NORMAL");
                        return 0;
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        System.out.println("ROTATION 90");
                        return 90;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        System.out.println("ROTATION 180");
                        return 180;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        System.out.println("ROTATION 270");
                        return 270;
                    default:
                        return 0;
                }
            }
            else
            {
                return 0;
            }
        }
        catch (Exception ex)
        {
            return 0;
        }
    }

}
