package kin.com.kinstrgam.Camera;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CameraProfile;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;

import kin.com.kinstrgam.R;
import kin.com.kinstrgam.Util.ArcImageView;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private ImageView mPreviewView;
    private static final int CAMERA_ID = 0;
    private int mJpegOrientation;
    private ArcImageView mShutterButton;
    private CameraCallback mCameraCallback;
    private View mRetakeButton;
    private ImageView mSwitchCameraButton;
    private Uri mFileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        initCamera();
        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new CameraPreview(this, mCamera);
        TextureView preview = findViewById(R.id.camera_preview);
        preview.setSurfaceTextureListener(mCameraPreview);

        mShutterButton = findViewById(R.id.shutter_button);
        mShutterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraCallback.takePhoto();
            }
        });


        mCameraCallback.getTakePictureTrigger().subscribe(
                out -> {
                    if (out != null) {
                        //once image captured, it display on the view
                        byte[] imageBytes = out.toByteArray();
                        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        Matrix m = getBitmapTransform();
                        Bitmap target = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
                        // For debug
                        SaveImage(target);
                        mPreviewView.setImageBitmap(target);
                        mPreviewView.setVisibility(View.VISIBLE);
                        mRetakeButton.setVisibility(View.VISIBLE);
                        mShutterButton.setVisibility(View.INVISIBLE);
                        mSwitchCameraButton.setImageResource(R.drawable.ic_send);


                    }
                }
        );

        mPreviewView = findViewById(R.id.picture_preview_view);
        mRetakeButton = findViewById(R.id.retake_photo_button);
        mRetakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPreviewView.setVisibility(View.GONE);
                mRetakeButton.setVisibility(View.INVISIBLE);
                mSwitchCameraButton.setImageResource(R.drawable.ic_switch);
                mShutterButton.setVisibility(View.VISIBLE);
            }
        });

        mSwitchCameraButton = findViewById(R.id.switch_camera);

        mSwitchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPreviewView.getVisibility() == View.VISIBLE) {
                    Log.d(TAG, "onClick: onBack");
                    onBack();
                } else {
                    //TODO switch camera
                }
            }
        });


    }

    public void onBack(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra("imageUri", mFileUri.toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }


    public void  initCamera() {
        // Setup the camera and the preview object
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(640, 480);

        // Set flash mode.
        String flashMode = Camera.Parameters.FLASH_MODE_OFF;

        List<String> supportedFlash = parameters.getSupportedFlashModes();
        if (isSupported(flashMode, supportedFlash)) {
            parameters.setFlashMode(flashMode);
        } else {
            flashMode = parameters.getFlashMode();
            if (flashMode == null) {
                flashMode = Camera.Parameters.FLASH_MODE_OFF;
            }
        }

        // Set white balance parameter.
        String whiteBalance = Camera.Parameters.WHITE_BALANCE_AUTO;
        if (isSupported(whiteBalance, parameters.getSupportedWhiteBalance())) {
            parameters.setWhiteBalance(whiteBalance);
        } else {
            whiteBalance = parameters.getWhiteBalance();
            if (whiteBalance == null) {
                whiteBalance = Camera.Parameters.WHITE_BALANCE_AUTO;
            }
        }

        // Set zoom.
        if (parameters.isZoomSupported()) {
            parameters.setZoom(0);
        }

        // Set continuous autofocus. API9
        List<String> supportedFocus = parameters.getSupportedFocusModes();
        if (isSupported(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedFocus)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        String stabSupported = parameters.get("video-stabilization-supported");
        if ("true".equals(stabSupported)) {
            parameters.set("video-stabilization", "true");
        }

        // Set JPEG quality.
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(CameraProfile.QUALITY_HIGH);
        parameters.setJpegQuality(jpegQuality);
        mCamera.setParameters(parameters);

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(CAMERA_ID, info);

        mJpegOrientation = getJpegRotation(info, 0);

        mCameraCallback = new CameraCallback(this);
        mCamera.setPreviewCallback(mCameraCallback);

    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }



    private int getJpegRotation(Camera.CameraInfo info, int orientation) {
        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        int rotation = 0;
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            int camera_orientation = info.orientation;

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (camera_orientation - orientation + 360) % 360;
            } else {  // back-facing camera
                rotation = (camera_orientation + orientation) % 360;
            }
        }

        return rotation;
    }

    public Matrix getBitmapTransform() {

        Matrix m = new Matrix();
        m.postRotate(mJpegOrientation);

        // Append a horizontal flip for front-facing
        //m.postScale(-1, 1);

        return m;
    }

    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            mFileUri = Uri.fromFile(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
