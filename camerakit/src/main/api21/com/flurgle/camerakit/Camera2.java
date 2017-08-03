package com.flurgle.camerakit;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SizeF;
import android.view.MotionEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

@TargetApi(21)
class Camera2 extends CameraImpl {

    private CameraDevice mCamera;
    private CameraCharacteristics mCameraCharacteristics;
    private CameraManager mCameraManager;

    private String mCameraId;
    private int mFacing;

    private Size mCaptureSize;
    private Size mPreviewSize;

    private ConstantMapper.MapperImpl mMapper = new ConstantMapper.Mapper2();
    private final HashMap<String, ExtraProperties> mExtraPropertiesMap = new HashMap<>();

    @Override
    public void onSurfaceChanged() {

    }

    @Override
    public void onSurfaceAvailable() {

    }

    Camera2(CameraView.CameraCallbacks callback, PreviewImpl preview, Context context) {
        super(callback, preview);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        // Get all view angles
        try {
            for (final String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics =
                        mCameraManager.getCameraCharacteristics(cameraId);
                @SuppressWarnings("ConstantConditions")
                int orientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (orientation == CameraCharacteristics.LENS_FACING_BACK) {
                    float[] maxFocus = characteristics.get(
                            CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    if (maxFocus == null) {
                        continue;
                    }
                    SizeF size = characteristics.get(
                            CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    if (size == null) {
                        continue;
                    }
                    float w = size.getWidth();
                    float h = size.getHeight();
                    mExtraPropertiesMap.put(cameraId, new ExtraProperties(
                            (float) Math.toDegrees(2*Math.atan(w/(maxFocus[0]*2))),
                            (float) Math.toDegrees(2*Math.atan(h/(maxFocus[0]*2)))
                    ));
                }
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to get camera view angles", e);
        }
    }

    // CameraImpl:

    @Override
    void start() {

    }

    @Override
    void stop() {

    }

    @Override
    void onDisplayOffset(int displayOrientation) {

    }

    @Override
    void onDeviceOrientation(int deviceOrientation) {

    }

    @Override
    void setFacing(@Facing int facing) {
        int internalFacing = mMapper.mapFacing(facing);
        final String[] ids;
        try {
            ids = mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.e("CameraKit", e.toString());
            return;
        }

        if (ids.length == 0) {
            throw new RuntimeException("No camera available.");
        }
//
//        for (String id : ids) {
//            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(id);
//            Integer level = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
//            if (level == null || level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
//                continue;
//            }
//            Integer internal = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
//            if (internal == null) {
//                throw new NullPointerException("Unexpected state: LENS_FACING null");
//            }
//            if (internal == internalFacing) {
//                mCameraId = id;
//                mCameraCharacteristics = cameraCharacteristics;
//                return true;
//            }
//        }

        if (mFacing == facing && isCameraOpened()) {
            stop();
            start();
        }
    }

    @Override
    void setFlash(@Flash int flash) {

    }

    @Override
    void setFocus(@Focus int focus) {

    }

    @Override
    void setSessionType(@SessionType int sessionType) {

    }

    @Override
    void setZoom(@ZoomMode int zoom) {

    }

    @Override
    void setLocation(double latitude, double longitude) {

    }

    @Override
    void setWhiteBalance(@WhiteBalance int whiteBalance) {

    }

    @Override
    void setVideoQuality(int videoQuality) {

    }

    @Override
    void captureImage() {

    }

    @Override
    void startVideo(@NonNull File videoFile) {

    }

    @Override
    void endVideo() {

    }

    @Override
    Size getCaptureSize() {
        if (mCaptureSize == null && mCameraCharacteristics != null) {
            TreeSet<Size> sizes = new TreeSet<>();
            sizes.addAll(getAvailableCaptureResolutions());

            TreeSet<AspectRatio> aspectRatios = new CommonAspectRatioFilter(
                    getAvailablePreviewResolutions(),
                    getAvailableCaptureResolutions()
            ).filter();
            AspectRatio targetRatio = aspectRatios.size() > 0 ? aspectRatios.last() : null;

            Iterator<Size> descendingSizes = sizes.descendingIterator();
            Size size;
            while (descendingSizes.hasNext() && mCaptureSize == null) {
                size = descendingSizes.next();
                if (targetRatio == null || targetRatio.matches(size)) {
                    mCaptureSize = size;
                    break;
                }
            }
        }

        return mCaptureSize;
    }

    @Override
    Size getPreviewSize() {
        if (mPreviewSize == null && mCameraCharacteristics != null) {
            TreeSet<Size> sizes = new TreeSet<>();
            sizes.addAll(getAvailablePreviewResolutions());

            TreeSet<AspectRatio> aspectRatios = new CommonAspectRatioFilter(
                    getAvailablePreviewResolutions(),
                    getAvailableCaptureResolutions()
            ).filter();
            AspectRatio targetRatio = aspectRatios.size() > 0 ? aspectRatios.last() : null;

            Iterator<Size> descendingSizes = sizes.descendingIterator();
            Size size;
            while (descendingSizes.hasNext() && mPreviewSize == null) {
                size = descendingSizes.next();
                if (targetRatio == null || targetRatio.matches(size)) {
                    mPreviewSize = size;
                    break;
                }
            }
        }

        return mPreviewSize;
    }

    @Override
    boolean shouldFlipSizes() {
        return false;
    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    void onTouchEvent(MotionEvent event) {

    }

    @Nullable
    @Override
    ExtraProperties getExtraProperties() {
        if (mCamera == null) {
            return null;
        }
        return mExtraPropertiesMap.get(mCamera.getId());
    }
    // Internal

    private List<Size> getAvailableCaptureResolutions() {
        List<Size> output = new ArrayList<>();

        if (mCameraCharacteristics != null) {
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new IllegalStateException("Failed to get configuration map: " + mCameraId);
            }

            for (android.util.Size size : map.getOutputSizes(ImageFormat.JPEG)) {
                output.add(new Size(size.getWidth(), size.getHeight()));
            }
        }

        return output;
    }

    private List<Size> getAvailablePreviewResolutions() {
        List<Size> output = new ArrayList<>();

        if (mCameraCharacteristics != null) {
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                throw new IllegalStateException("Failed to get configuration map: " + mCameraId);
            }

            for (android.util.Size size : map.getOutputSizes(mPreview.getOutputClass())) {
                output.add(new Size(size.getWidth(), size.getHeight()));
            }
        }

        return output;
    }

}
