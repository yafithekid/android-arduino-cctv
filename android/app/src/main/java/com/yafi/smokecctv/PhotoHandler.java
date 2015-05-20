package com.yafi.smokecctv;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//get the picture and upload to server
public class PhotoHandler implements Camera.PictureCallback {
    private final Context context;
    //untuk penanda extra antara handler dan activity
    private String resultImagePath;
    private String serverUri;

    public PhotoHandler(Context context,String resultImagePath,String serverUri){
        this.context = context;
        this.resultImagePath = resultImagePath;
        this.serverUri = serverUri;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Log.d(CCTVActivity.DEBUG_TAG,"Running handler");
        File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(CCTVActivity.DEBUG_TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;

        }
        Log.d(CCTVActivity.DEBUG_TAG,"Creating photoname");
        String photoFile = createPhotoName();
        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        File pictureFile = new File(filename);

        Log.d(CCTVActivity.DEBUG_TAG,"Saving pictures");
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:" + pictureFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
            this.resultImagePath = new String(pictureFile.getAbsolutePath());
            //upload the file
            if (serverUri.isEmpty()){
                Toast.makeText(context,"Warning: server url is null",Toast.LENGTH_SHORT).show();
            } else {
                (new UploaderTask(this.serverUri,pictureFile.getAbsolutePath())).execute();
            }
        } catch (Exception error) {
            Log.d(CCTVActivity.DEBUG_TAG, "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private String createPhotoName(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        return "Picture_" + date + ".jpg";
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, context.getString(R.string.app_name));
    }
}
