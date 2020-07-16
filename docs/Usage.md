# Usage
```java
...

import com.tenginekit.FaceManager;

public class CameraActivity extends AppCompatActivity implements Camera.PreviewCallback{
    private static final String TAG = "CameraActicity";

    // camera preview width
    protected int previewWidth;
    // camera preview height
    protected int previewHeight;
    // content display screen width
    public static float ScreenWidth;
    // content display screen height
    public static float ScreenHeight;

    // nv21 data from camera
    protected byte[] mNV21Bytes;

    ...

    public void Init() {
        mNV21Bytes = new byte[previewHeight * previewWidth];

        /**
         * init
         * */
        Face.init(
                this,
                AndroidConfig
                        .create()
                        .setCameraMode()
                        .setDefaultFunc()
                        .setDefaultInputImageFormat()
                        .setNormalMode()
                        .setInputImageSize(previewWidth, previewHeight)
                        .setOutputImageSize((int) ScreenWidth, (int) ScreenHeight)
        );
    }

    /**
     * Callback for android.hardware.Camera API
     */
    @Override
    public void onPreviewFrame(final byte[] bytes, final Camera camera) {
        if (isProcessingFrame) {
            return;
        }
        isProcessingFrame = true;
        try {
            if (mNV21Bytes == null) {

                ...

                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                previewHeight = previewSize.height;
                previewWidth = previewSize.width;
                Init();
                
                ...
            }
        } catch (final Exception e) {
            MyLogger.logError(TAG, "onPreviewFrame: " + e);
            return;
        }
        
        processImage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * release
         * */
        Face.release();
    }
   

    protected void processImage() {

        ...

        Face.FaceDetect faceDetect = Face.detect(mNV21Bytes);
        List<FaceDetectInfo> faceDetectInfos = new ArrayList<>();
        List<FaceLandmarkInfo> landmarkInfos = new ArrayList<>();
        if(faceDetect.getFaceCount() > 0){
            faceDetectInfos = faceDetect.getDetectInfos();
            landmarkInfos = faceDetect.landmark2d();
        }

        if (faceDetectInfos != null && faceDetectInfos.size() > 0) {
            Rect[] face_rect = new Rect[faceDetectInfos.size()];

            List<List<FaceLandmarkPoint>> face_landmarks = new ArrayList<>();
            for (int i = 0; i < faceDetectInfos.size(); i++) {
                Rect rect = new Rect();
                rect = faceDetectInfos.get(i).asRect();
                face_rect[i] = rect;
                face_landmarks.add(landmarkInfos.get(i).landmarks);
            }

            // do something with face_rect, face_landmarks
        }

        ...

    }
}

```