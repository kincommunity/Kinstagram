package kin.com.kinstagram.Camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by kyungsoohong on 11/4/17.
 */

public class CameraCallback implements Camera.PreviewCallback {
    Context _context;


    private boolean _forRegister = false;

    private PublishSubject<ByteArrayOutputStream> _takePictureTrigger = PublishSubject.create();

    public CameraCallback(Context context) {
        _context = context;
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        // Process the camera data here
        try {

            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            //when user click Camera button, it save buffer and return to MainActivity
            if(_forRegister){
                _forRegister = false;
                _takePictureTrigger.onNext(getPhoto(data, size.width, size.height));
            }
            _forRegister = false;

        } catch (Exception e) {
            Toast toast = Toast
                    .makeText(_context, e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private ByteArrayOutputStream getPhoto(byte[] data, int width, int height) {

        YuvImage yuv = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0, yuv.getWidth(), yuv.getHeight()), 100, out);

        return out;
    }

    public void takePhoto() {
        _forRegister = true;
    }

    public Observable<ByteArrayOutputStream> getTakePictureTrigger(){
        return _takePictureTrigger;
    }
}
