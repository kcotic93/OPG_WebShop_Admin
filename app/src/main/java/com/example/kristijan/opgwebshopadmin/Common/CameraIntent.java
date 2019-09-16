package com.example.kristijan.opgwebshopadmin.Common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraIntent {

    Context context;
    private String mCurrentPhotoPath;

    private  int REQUEST_CAPTURE_IMAGE=100;


    public CameraIntent(Context context) {
        this.context = context;
    }

    public String getmCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public void setmCurrentPhotoPath(String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }

    public  int getRequestCaptureImage() {
        return REQUEST_CAPTURE_IMAGE;
    }


    public Intent openCameraIntent () {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
            File pictureFile = null;
            try {
                pictureFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(context,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show();
                return cameraIntent;
            }
            if (pictureFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context,
                        "com.example.kristijan.opgwebshopadmin.fileprovider",
                        pictureFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                return cameraIntent;
            }
        }
        return cameraIntent;
    }

    public File createImageFile () throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "ZOFTINO_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(pictureFile, ".jpg", storageDir);
        setmCurrentPhotoPath(image.getAbsolutePath());
        return image;
    }
}
