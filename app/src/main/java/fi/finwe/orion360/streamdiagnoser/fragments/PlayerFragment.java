// Copyright (c) 2018 Finwe Ltd. All Rights Reserved. http://www.finwe.fi
//
// You may use, distribute and modify this source code under the terms of the
// FINWE ORION360 STREAM DIAGNOSER license. You should have received a copy of the
// license with this file. If not, please visit http://www.finwe.fi or write to
// Finwe Ltd., Elektroniikkatie 2, 90590 Oulu, Finland.
//
// THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
// KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
// PARTICULAR PURPOSE.
//

package fi.finwe.orion360.streamdiagnoser.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import fi.finwe.log.Logger;
import fi.finwe.math.Vec2f;
import fi.finwe.orion360.sdk.pro.OrionContext;
import fi.finwe.orion360.sdk.pro.OrionScene;
import fi.finwe.orion360.sdk.pro.controllable.DisplayClickable;
import fi.finwe.orion360.sdk.pro.controller.RotationAligner;
import fi.finwe.orion360.sdk.pro.controller.TouchDisplayClickListener;
import fi.finwe.orion360.sdk.pro.controller.TouchPincher;
import fi.finwe.orion360.sdk.pro.controller.TouchRotater;
import fi.finwe.orion360.sdk.pro.item.OrionCamera;
import fi.finwe.orion360.sdk.pro.item.OrionPanorama;
import fi.finwe.orion360.sdk.pro.item.OrionSceneItem;
import fi.finwe.orion360.sdk.pro.licensing.LicenseManager;
import fi.finwe.orion360.sdk.pro.licensing.LicenseSource;
import fi.finwe.orion360.sdk.pro.licensing.LicenseStatus;
import fi.finwe.orion360.sdk.pro.licensing.LicenseVerifier;
import fi.finwe.orion360.sdk.pro.source.ExoPlayerWrapper;
import fi.finwe.orion360.sdk.pro.source.OrionTexture;
import fi.finwe.orion360.sdk.pro.source.OrionVideoTexture;
import fi.finwe.orion360.sdk.pro.source.VideoPlayerWrapper;
import fi.finwe.orion360.sdk.pro.view.OrionView;
import fi.finwe.orion360.sdk.pro.OrionViewport;
import fi.finwe.orion360.sdk.pro.widget.OrionWidget;
import fi.finwe.orion360.streamdiagnoser.MainActivity;
import fi.finwe.orion360.streamdiagnoser.MyControlPanel;
import fi.finwe.orion360.streamdiagnoser.MyExoPlayerWrapper;
import fi.finwe.orion360.streamdiagnoser.R;
import fi.finwe.util.ContextUtil;

/**
 * Orion360 player fragment
 */
public class PlayerFragment extends Fragment implements OrionVideoTexture.Listener {

    /** Tag for debug logging. */
    protected static final String TAG = PlayerFragment.class.getSimpleName();

    /** Listener for fragment events. */
    private PlayerListener mListener;

    /** Interface for listening fragment events. */
    public interface PlayerListener {

    }

    /** Context. */
    protected Context mContext;

    /** Root view. */
    protected View mRootView;

    /** Player status label. */
    protected TextView mPlayerStatusLabel;

    /** Player buffer status label. */
    protected TextView mPlayerBufferStatusLabel;

    /** Our custom implementation of an Orion360 control panel. */
    protected MyControlPanel mControlPanel;

    /** The root view of the inflated control panel. */
    protected View mControlPanelView;

    /** The layout container that will hold our inflated control panel. */
    protected ViewGroup mControlPanelContainer;

    /** OrionContext. */
    protected OrionContext mOrionContext;

    /** OrionView. */
    protected OrionView mView;

    /** OrionScene. */
    protected OrionScene mScene;

    /** OrionPanorama. */
    protected OrionPanorama mPanorama;

    /** OrionTexture. */
    protected OrionTexture mPanoramaTexture;

    /** The video player. */
    protected VideoPlayerWrapper mVideoPlayer;

    /** OrionCamera. */
    protected OrionCamera mCamera;

    /** TouchControllerWidget. */
    protected TouchControllerWidget mTouchController;

    /** Buffering indicator for normal mode. */
    private ProgressBar mBufferingIndicatorNormal;

    /** Handler for buffering indicators. */
    private Handler mBufferingIndicatorHandler;

    /** Polling interval for buffering indicator handler, in ms. */
    int mBufferingIndicatorIntervalMs = 500;


    /**
     * Empty constructor.
     */
    public PlayerFragment() {
        Logger.logF();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.logF();

        try {
            mListener = (PlayerListener) context;
        } catch (ClassCastException e) {
            Logger.logW(TAG, context.toString()
                    + " does not implement interface "
                    + PlayerListener.class.getSimpleName());
        }

        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.logF();

        // Propagate fragment lifecycle callbacks to the OrionContext object (singleton).
        mOrionContext = new OrionContext();
        mOrionContext.onCreate((Activity)mContext);

        // Perform Orion360 license check. A valid license file should be put to /assets folder!
        verifyOrionLicense();

        // Use a layout that has OrionVideoView.
        mRootView = inflater.inflate(R.layout.fragment_player, container, false);

        // Player status label.
        mPlayerStatusLabel = (TextView) mRootView.findViewById(R.id.player_status_label);

        // Player buffer status label.
        mPlayerBufferStatusLabel = (TextView) mRootView.findViewById(R.id.player_buffer_status);

        // Get buffering indicators and setup a handler for them.
        mBufferingIndicatorNormal = (ProgressBar) mRootView.findViewById(R.id.buffering_indicator);
        mBufferingIndicatorHandler = new Handler();

        // Configure Orion360 for playing full spherical monoscopic videos/images.
        configureOrionView();

        // Create an instance of our custom control panel.
        mControlPanel = new MyControlPanel(mOrionContext, (Activity)mContext);

        // Get the placeholder for our control panel from the XML layout.
        mControlPanelContainer = (ViewGroup) mRootView.findViewById(R.id.control_panel_container);

        // Inflate the control panel layout using the placeholder as the anchor view.
        mControlPanelView = mControlPanel.createLayout(inflater, mControlPanelContainer);

        // Add the inflated control panel root view to its container.
        mControlPanelContainer.addView(mControlPanelView);

        // Let control panel control our video texture (video, audio)
        mControlPanel.setControlledContent((OrionVideoTexture) mPanoramaTexture);

        // Let control panel control our camera (VR mode).
        mControlPanel.setControlledCamera(mCamera);

        // Listen for control panel events.
        mControlPanel.setPlayerControlsListener(new MyControlPanel.PlayerControlsListener() {
            @Override
            public void onLogoButtonClicked() {}

            @Override
            public void onCloseButtonClicked() {
                //VideoControls.this.finish();
            }

            @Override
            public void onVRModeChanged(boolean isEnabled) {
//                setVRMode(isEnabled);
//                if (isEnabled) {
//                    mControlPanel.hideTitlePanel();
//                    mControlPanel.hideControlPanel();
//                } else {
//                    mControlPanel.showTitlePanel();
//                    mControlPanel.showControlPanel();
//                }
            }
        });

        // Listen for single, double and long clicks.
        TouchDisplayClickListener listener = new TouchDisplayClickListener();
        listener.bindClickable(null, new TouchDisplayClickListener.Listener() {

            @Override
            public void onDisplayClick(DisplayClickable clickable, Vec2f displayCoords) {
                ((Activity)mContext).runOnUiThread (new Thread(new Runnable() {
                    public void run() {

                        // Toggle panels visibility in normal mode; hint about long tap in VR mode.
                        if (!mControlPanel.isVRModeEnabled()) {
                            mControlPanel.toggleTitlePanel();
                            mControlPanel.toggleControlPanel();
                        } else {
                            String message = getString(R.string.player_long_tap_hint_exit_vr_mode);
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        }

                    }
                }));
            }

            @Override
            public void onDisplayDoubleClick(DisplayClickable clickable, Vec2f displayCoords) {
                ((Activity)mContext).runOnUiThread (new Thread(new Runnable() {
                    public void run() {

                        // Toggle between play and pause states in normal mode.
                        if (!mControlPanel.isVRModeEnabled()) {
                            OrionVideoTexture t = (OrionVideoTexture) mPanoramaTexture;
                            if (t.getActualPlaybackState() == OrionTexture.PlaybackState.PLAYING) {
                                mControlPanel.pause();
                                mControlPanel.runPauseAnimation();
                            } else {
                                mControlPanel.play();
                                mControlPanel.runPlayAnimation();
                            }
                        }

                    }
                }));
            }

            @Override
            public void onDisplayLongClick(DisplayClickable clickable, Vec2f displayCoords) {

                ((Activity)mContext).runOnUiThread (new Thread(new Runnable() {
                    public void run() {

                        // Change VR mode (via control panel so that it stays in sync).
                        mControlPanel.toggleVRMode();

                    }
                }));
            }
        });

        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.logF();

        mOrionContext.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.logF();

        mOrionContext.onResume();

        // Hide buffering indicator when playback starts, even if device doesn't
        // properly notify that buffering has ended...
        mBufferingIndicatorRunnable.run();
    }

    @Override
    public void onPause() {
        Logger.logF();

        // Cancel buffering indicator handler (polling).
        mBufferingIndicatorHandler.removeCallbacks(mBufferingIndicatorRunnable);

        mOrionContext.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        Logger.logF();

        mOrionContext.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Logger.logF();

        mOrionContext.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Logger.logF();

        mListener = null;
        super.onDetach();
    }

    public void setVideoPlayer(VideoPlayerWrapper videoPlayer) {
        Logger.logF();

        mVideoPlayer = videoPlayer;
    }

    public void playVideo(String videoUri) {
        Logger.logF();

        if (mPanoramaTexture != null) {
            if (mPanoramaTexture instanceof OrionVideoTexture) {
                OrionVideoTexture texture = (OrionVideoTexture) mPanoramaTexture;
                texture.pause();
                texture.stop();
            }
            mPanoramaTexture.release();
            mPanoramaTexture.destroy();
            mPanoramaTexture = null;
            mPanorama.releaseTextures();
            mControlPanel.setControlledContent(null);
        }

        mPanoramaTexture = new OrionVideoTexture(mVideoPlayer, videoUri);
        mPanorama.bindTextureFull(0, mPanoramaTexture);

        mControlPanel.setControlledContent((OrionVideoTexture) mPanoramaTexture);

        if (mPanoramaTexture instanceof OrionVideoTexture) {
            ((OrionVideoTexture) mPanoramaTexture).addTextureListener(this);
        }

        // Hide buffering indicator when playback starts, even if device doesn't
        // properly notify that buffering has ended...
        mBufferingIndicatorRunnable.run();
    }

    /**
     * Verify Orion360 license. This is the first thing to do after creating OrionContext.
     */
    protected void verifyOrionLicense() {
        LicenseManager licenseManager = mOrionContext.getLicenseManager();
        List<LicenseSource> licenses = LicenseManager.findLicensesFromAssets(mContext);
        for (LicenseSource license : licenses) {
            LicenseVerifier verifier = licenseManager.verifyLicense(mContext, license);
            Log.i(TAG, "Orion360 license " + verifier.getLicenseSource().uri + " verified: "
                    + verifier.getLicenseStatus());
            if (verifier.getLicenseStatus() == LicenseStatus.OK) break;
        }
    }

    /**
     * Configure Orion360 for playing full spherical monoscopic videos/images.
     */
    private void configureOrionView() {

        // For compatibility with Google Cardboard 1.0 with magnetic switch, disable magnetometer
        // from sensor fusion. Also recommended for devices with poorly calibrated magnetometer.
        mOrionContext.getSensorFusion().setMagnetometerEnabled(false);

        // Create a new scene. This represents a 3D world where various objects can be placed.
        mScene = new OrionScene();

        // Bind sensor fusion as a controller. This will make it available for scene objects.
        mScene.bindController(mOrionContext.getSensorFusion());

        // Create a new panorama. This is a 3D object that will represent a spherical video/image.
        mPanorama = new OrionPanorama();
        mPanorama.setPanoramaType(OrionPanorama.PanoramaType.PANEL_SOURCE);
        mPanorama.setRenderingMode(OrionSceneItem.RenderingMode.CAMERA_DISABLED);

        // Create a new video (or image) texture from a video (or image) source URI.
        mVideoPlayer = new ExoPlayerWrapper(mContext);

        // Bind the panorama to the scene. This will make it part of our 3D world.
        mScene.bindSceneItem(mPanorama);

        // Create a new camera. This will become the end-user's eyes into the 3D world.
        mCamera = new OrionCamera();

        // Define maximum limit for zooming. As an example, at value 1.0 (100%) zooming in is
        // disabled. At 3.0 (300%) camera will never reduce the FOV below 1/3 of the base FOV.
        mCamera.setZoomMax(3.0f);

        // Set yaw angle to 0. Now the camera will always point to the same yaw angle
        // (to the horizontal center point of the equirectangular video/image source)
        // when starting the app, regardless of the orientation of the device.
        mCamera.setRotationYaw(0);

        // Bind camera as a controllable to sensor fusion. This will let sensors rotate the camera.
        mOrionContext.getSensorFusion().bindControllable(mCamera);

        // Create a new touch controller widget (convenience class), and let it control our camera.
        mTouchController = new TouchControllerWidget(mCamera);

        // Bind the touch controller widget to the scene. This will make it functional in the scene.
        mScene.bindWidget(mTouchController);

        // Find Orion360 view from the XML layout. This is an Android view where we render content.
        mView = mRootView.findViewById(R.id.orion_view);

        // Bind the scene to the view. This is the 3D world that we will be rendering to this view.
        mView.bindDefaultScene(mScene);

        // Bind the camera to the view. We will look into the 3D world through this camera.
        mView.bindDefaultCamera(mCamera);

        // The view can be divided into one or more viewports. For example, in VR mode we have one
        // viewport per eye. Here we fill the complete view with one (landscape) viewport.
        mView.bindViewports(OrionViewport.VIEWPORT_CONFIG_FULL,
                OrionViewport.CoordinateType.FIXED_LANDSCAPE);
    }

    /**
     * Convenience class for configuring typical touch control logic.
     */
    public class TouchControllerWidget implements OrionWidget {

        /** The camera that will be controlled by this widget. */
        private OrionCamera mCamera;

        /** Touch pinch-to-zoom/pinch-to-rotate gesture handler. */
        private TouchPincher mTouchPincher;

        /** Touch drag-to-pan gesture handler. */
        private TouchRotater mTouchRotater;

        /** Rotation aligner keeps the horizon straight at all times. */
        private RotationAligner mRotationAligner;


        /**
         * Constructs the widget.
         *
         * @param camera The camera to be controlled by this widget.
         */
        TouchControllerWidget(OrionCamera camera) {

            // Keep a reference to the camera that we control.
            mCamera = camera;

            // Create pinch-to-zoom/pinch-to-rotate handler.
            mTouchPincher = new TouchPincher();
            mTouchPincher.setMinimumDistanceDp(mOrionContext.getActivity(), 20);
            mTouchPincher.bindControllable(mCamera, OrionCamera.VAR_FLOAT1_ZOOM);

            // Create drag-to-pan handler.
            mTouchRotater = new TouchRotater();
            mTouchRotater.bindControllable(mCamera);

            // Create the rotation aligner, responsible for rotating the view so that the horizon
            // aligns with the user's real-life horizon when the user is not looking up or down.
            mRotationAligner = new RotationAligner();
            mRotationAligner.setDeviceAlignZ(-ContextUtil.getDisplayRotationDegreesFromNatural(
                    mOrionContext.getActivity()));
            mRotationAligner.bindControllable(mCamera);

            // Rotation aligner needs sensor fusion data in order to do its job.
            mOrionContext.getSensorFusion().bindControllable(mRotationAligner);
        }

        @Override
        public void onBindWidget(OrionScene scene) {
            // When widget is bound to scene, bind the controllers to it to make them functional.
            scene.bindController(mTouchPincher);
            scene.bindController(mTouchRotater);
            scene.bindController(mRotationAligner);
        }

        @Override
        public void onReleaseWidget(OrionScene scene) {
            // When widget is released from scene, release the controllers as well.
            scene.releaseController(mTouchPincher);
            scene.releaseController(mTouchRotater);
            scene.releaseController(mRotationAligner);
        }
    }

    /**
     * Show buffering indicator.
     */
    protected void showBufferingIndicator() {
        Logger.logF();

        mBufferingIndicatorNormal.setVisibility(View.VISIBLE);
    }

    /**
     * Hide buffering indicator.
     */
    protected void hideBufferingIndicator() {
        Logger.logF();

        mBufferingIndicatorNormal.setVisibility(View.GONE);
    }

    /**
     * Runnable for polling if video playback has already begun, and to hide buffering indicator.
     */
    Runnable mBufferingIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.logD(TAG, "Checking if playback has started...");
            if (mPanoramaTexture == null) return;
            long newPosition = mPanoramaTexture.getCurrentPosition();
            if (newPosition > 0) {
                Log.d(TAG, "Now playing video.");
                hideBufferingIndicator();
            } else {
                Log.d(TAG, "Still buffering.");
                mBufferingIndicatorHandler.postDelayed(mBufferingIndicatorRunnable,
                        mBufferingIndicatorIntervalMs);
            }
        }
    };

    @Override
    public void onInvalidURI(OrionTexture orionTexture) {
        Logger.logF();

    }

    @Override
    public void onException(OrionTexture orionTexture, Exception e) {
        Logger.logF();

        mPlayerStatusLabel.setText("Error: " + e.toString());
    }

    @Override
    public void onVideoPlayerCreated(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Player created");
    }

    @Override
    public void onVideoSourceURISet(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Player source URI set");

        // Assume buffering is needed when a new video stream URI is set. Show indicator.
        showBufferingIndicator();
    }

    @Override
    public void onVideoPrepared(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video prepared");
    }

    @Override
    public void onVideoRenderingStart(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video rendering starts now");
    }

    @Override
    public void onVideoStarted(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video playback started");
    }

    @Override
    public void onVideoPaused(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video playback paused");
    }

    @Override
    public void onVideoStopped(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video playback stopped");
    }

    @Override
    public void onVideoCompleted(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video playback completed");
    }

    @Override
    public void onVideoSeekStarted(OrionVideoTexture orionVideoTexture, long l) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video seek started");
    }

    @Override
    public void onVideoSeekCompleted(OrionVideoTexture orionVideoTexture, long l) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video seek completed");
    }

    @Override
    public void onVideoPositionChanged(OrionVideoTexture orionVideoTexture, long l) {

    }

    @Override
    public void onVideoDurationUpdate(OrionVideoTexture orionVideoTexture, long l) {

    }

    @Override
    public void onVideoSizeChanged(OrionVideoTexture orionVideoTexture, int i, int i1) {

    }

    @Override
    public void onVideoBufferingStart(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video buffering started");

        // Assume buffering is needed when a new video stream URI is set. Show indicator.
        showBufferingIndicator();
    }

    @Override
    public void onVideoBufferingEnd(OrionVideoTexture orionVideoTexture) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video buffering ended");

        // Video player tells it has stopped buffering. Hide indicator.
        hideBufferingIndicator();
    }

    @Override
    public void onVideoBufferingUpdate(OrionVideoTexture orionVideoTexture, int i, int i1) {
        Logger.logF();

        mPlayerBufferStatusLabel.setText(i + "/" + i1);
    }

    @Override
    public void onVideoError(OrionVideoTexture orionVideoTexture, int i, int i1) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video error: " + i + " " + i1);
    }

    @Override
    public void onVideoInfo(OrionVideoTexture orionVideoTexture, int i, String s) {
        Logger.logF();

        mPlayerStatusLabel.setText("Video info: " + i + " " + s);
    }
}
