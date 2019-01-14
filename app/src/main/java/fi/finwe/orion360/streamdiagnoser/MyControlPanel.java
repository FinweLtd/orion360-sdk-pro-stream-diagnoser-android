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

package fi.finwe.orion360.streamdiagnoser;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import fi.finwe.log.Logger;
import fi.finwe.orion360.sdk.pro.OrionContext;
import fi.finwe.orion360.sdk.pro.widget.ControlPanel;

/**
 * Custom Orion360 control panel implementation.
 */
public class MyControlPanel extends ControlPanel {

    /** Tag for debug logging. */
    protected static final String TAG = MainActivity.class.getSimpleName();

    /** Interface for listening component events. */
    public interface PlayerControlsListener {

        /** Called when logo button has been clicked. */
        void onLogoButtonClicked();

        /** Called when close button has been clicked. */
        void onCloseButtonClicked();

        /** Called when VR mode button has been clicked. */
        void onVRModeChanged(boolean isEnabled);

    }

    /** Layout root view. */
    private ViewGroup mRootView;

    /** Title panel. */
    private View mTitlePanelView;

    /** Title panel animation in. */
    private Animation mTitlePanelAnimationIn;

    /** Title panel animation out. */
    private Animation mTitlePanelAnimationOut;

    /** Logo button. */
    private ImageView mLogoButton;

    /** Title text. */
    private TextView mTitle;

    /** Close button. */
    private ImageView mCloseButton;

    /** Control panel view. */
    private View mControlPanelView;

    /** Control panel animation in. */
    private Animation mControlPanelAnimationIn;

    /** Control panel animation out. */
    private Animation mControlPanelAnimationOut;

    /** Play button. */
    private ImageButton mPlayButton;

    /** Elapsed time text. */
    private TextView mElapsedTime;

    /** Seek bar. */
    private SeekBar mSeekBar;

    /** Duration time text. */
    private TextView mDurationTime;

    /** Remaining time text. */
    private TextView mRemainingTime;

    /** Audio mute/unmute button. */
    private ImageButton mAudioMuteButton;

    /** VR/normal mode button. */
    private ImageButton mVRModeButton;

    /** Play overlay. */
    private ImageView mPlayOverlay;

    /** Play animation. */
    private Animation mPlayAnimation;

    /** Pause overlay. */
    private ImageView mPauseOverlay;

    /** Pause animation. */
    private Animation mPauseAnimation;

    /** Normal mode (single) buffering indicator. */
    private ImageView mBufferingIndicatorNormal;

    /** VR mode (left) buffering indicator. */
    private ImageView mBufferingIndicatorVRLeft;

    /** VR mode (right) buffering indicator. */
    private ImageView mBufferingIndicatorVRRight;

    /** Buffering indicator custom animation. */
    private Animation mBufferingIndicatorAnimation;

    /** Flag for indicating if buffering indicators are currently visible, or not. */
    private boolean mBufferingIndicatorVisible = false;

    /** Flag for indicating if VR mode is active, or not. */
    private boolean mIsVRModeEnabled = false;

    /** Flag indicating if control panel is visible or not. */
    private boolean mControlPanelVisible = true;

    /** Flag indicating if title panel is visible or not. */
    private boolean mTitlePanelVisible = true;

    /** Listener for component events. */
    private PlayerControlsListener mListener;


    /**
     * Constructor with activity.
     *
     * @param context The Orion context.
     * @param activity The activity.
     */
    public MyControlPanel(OrionContext context, Activity activity) {
        super(context, activity);
        Logger.logF();
    }

    @Override
    public View createLayout(LayoutInflater inflater, ViewGroup anchorView) {
        Logger.logF();

        // Inflate the layout for this component.
        mRootView = (ViewGroup) inflater.inflate(R.layout.video_controls, anchorView, false);

        // Title panel.
        mTitlePanelView = mRootView.findViewById(R.id.player_title_panel);
        mTitlePanelAnimationIn = AnimationUtils.loadAnimation(
                mOrionContext.getActivity(), R.anim.player_title_panel_enter);
        mTitlePanelAnimationIn.setFillAfter(true);
        mTitlePanelAnimationIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitlePanelVisible = true;
                mLogoButton.setClickable(true);
                mLogoButton.setEnabled(true);
                mTitle.setClickable(true);
                mTitle.setEnabled(true);
                mCloseButton.setClickable(true);
                mCloseButton.setEnabled(true);
            }

        });
        mTitlePanelAnimationOut = AnimationUtils.loadAnimation(
                mOrionContext.getActivity(), R.anim.player_title_panel_exit);
        mTitlePanelAnimationOut.setFillAfter(true);
        mTitlePanelAnimationOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitlePanelVisible = false;
                mLogoButton.setClickable(false);
                mLogoButton.setEnabled(false);
                mTitle.setClickable(false);
                mTitle.setEnabled(false);
                mCloseButton.setClickable(false);
                mCloseButton.setEnabled(false);
            }

        });

        // Logo button.
        mLogoButton = (ImageView) mRootView.findViewById(R.id.player_title_panel_logo_button);
        mLogoButton.setVisibility(View.GONE);
        mLogoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onLogoButtonClicked();
                }
            }

        });

        // Title text.
        mTitle = (TextView) mRootView.findViewById(R.id.player_title_panel_title);

        // Close button.
        mCloseButton = (ImageView) mRootView.findViewById(R.id.player_title_panel_close_button);
        mCloseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onCloseButtonClicked();
                }
            }

        });

        // Control panel.
        mControlPanelView = mRootView.findViewById(R.id.player_controls_panel);
        mControlPanelAnimationIn = AnimationUtils.loadAnimation(
                mOrionContext.getActivity(), R.anim.player_control_panel_enter);
        mControlPanelAnimationIn.setFillAfter(true);
        mControlPanelAnimationIn.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mControlPanelVisible = true;

                // Enable all control panel components.
                mPlayButton.setClickable(true);
                mPlayButton.setEnabled(true);
                mElapsedTime.setClickable(true);
                mElapsedTime.setEnabled(true);
                mSeekBar.setClickable(true);
                mSeekBar.setEnabled(true);
                mRemainingTime.setClickable(true);
                mRemainingTime.setEnabled(true);
                mDurationTime.setClickable(true);
                mDurationTime.setEnabled(true);
                mAudioMuteButton.setClickable(true);
                mAudioMuteButton.setEnabled(true);
                mVRModeButton.setClickable(true);
                mVRModeButton.setEnabled(true);
            }

        });
        mControlPanelAnimationOut = AnimationUtils.loadAnimation(
                mOrionContext.getActivity(), R.anim.player_control_panel_exit);
        mControlPanelAnimationOut.setFillAfter(true);
        mControlPanelAnimationOut.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mControlPanelVisible = false;

                // Disable all control panel components.
                mPlayButton.setClickable(false);
                mPlayButton.setEnabled(false);
                mElapsedTime.setClickable(false);
                mElapsedTime.setEnabled(false);
                mSeekBar.setClickable(false);
                mSeekBar.setEnabled(false);
                mRemainingTime.setClickable(false);
                mRemainingTime.setEnabled(false);
                mDurationTime.setClickable(false);
                mDurationTime.setEnabled(false);
                mAudioMuteButton.setClickable(false);
                mAudioMuteButton.setEnabled(false);
                mVRModeButton.setClickable(false);
                mVRModeButton.setEnabled(false);
            }
        });

        // Play/pause button.
        mPlayButton = (ImageButton) mRootView.findViewById(R.id.player_controls_play_button);
        setPlayButton(mPlayButton,
                R.mipmap.player_play_icon,
                R.mipmap.player_pause_icon,
                R.mipmap.player_play_icon);

        // Position (elapsed time) text.
        mElapsedTime = (TextView) mRootView.findViewById(R.id.player_controls_position_text);
        setPositionLabel(mElapsedTime);

        // Seek bar.
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.player_controls_seekbar);
        setSeekBar(mSeekBar);

        // Duration (total time) text.
        mDurationTime = (TextView) mRootView.findViewById(R.id.player_controls_duration_text);
        setDurationLabel(mDurationTime);
        mDurationTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDurationTime.setVisibility(View.GONE);
                mRemainingTime.setVisibility(View.VISIBLE);
            }

        });

        // Remaining time text.
        mRemainingTime = (TextView) mRootView.findViewById(R.id.player_controls_remaining_text);
        setRemainingLabel(mRemainingTime);
        mRemainingTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDurationTime.setVisibility(View.VISIBLE);
                mRemainingTime.setVisibility(View.GONE);
            }

        });

        // Audio mute button.
        mAudioMuteButton = (ImageButton) mRootView.findViewById(
                R.id.player_controls_audio_button);
        setAudioMuteButton(mAudioMuteButton,
                R.mipmap.player_unmute_icon,
                R.mipmap.player_mute_icon);

        // Configure VR mode on/off button.
        mVRModeButton = (ImageButton) mRootView.findViewById(R.id.player_controls_vr_button);
        mVRModeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleVRMode();
            }

        });

        // Play overlay.
        mPlayOverlay = (ImageView) mRootView.findViewById(R.id.play_overlay);
        mPlayAnimation = AnimationUtils.loadAnimation(
                mOrionContext.getActivity(), R.anim.fast_fadeinout_animation);
        mPlayAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mPlayOverlay.setVisibility(View.GONE);
            }

        });

        // Pause overlay.
        mPauseOverlay = (ImageView) mRootView.findViewById(R.id.pause_overlay);
        mPauseAnimation = AnimationUtils.loadAnimation(
                mOrionContext.getActivity(), R.anim.fast_fadeinout_animation);
        mPauseAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                mPauseOverlay.setVisibility(View.GONE);
            }

        });

        // Buffering indicator.
        mBufferingIndicatorNormal = (ImageView) mRootView.findViewById(
                R.id.player_hud_progressbar_normal_image);
        mBufferingIndicatorVRLeft = (ImageView) mRootView.findViewById(
                R.id.player_hud_progressbar_vr_left_image);
        mBufferingIndicatorVRRight = (ImageView) mRootView.findViewById(
                R.id.player_hud_progressbar_vr_right_image);
        mBufferingIndicatorAnimation = AnimationUtils.loadAnimation(
                mOrionContext.getActivity(), R.anim.rotate_around_center_point);

        return mRootView;
    }

    /**
     * Set listener for control panel events.
     *
     * @param listener the listener to be set.
     */
    public void setPlayerControlsListener(PlayerControlsListener listener) {
        Logger.logF();

        mListener = listener;
    }

    /**
     * Check if VR mode is currently enabled, or not.
     */
    public boolean isVRModeEnabled() {
        Logger.logF();

        return mIsVRModeEnabled;
    }

    /**
     * Toggle VR mode.
     */
    public void toggleVRMode() {
        Logger.logF();

        if (mIsVRModeEnabled) {
            disableVRMode();
        } else {
            enableVRMode();
        }
    }

    /**
     * Enable VR mode.
     */
    void enableVRMode() {
        Logger.logF();

        mIsVRModeEnabled = true;

        // Handle the case where VR mode is toggled and progress bar
        // also needs to be swapped between normal and VR mode.
        if (mBufferingIndicatorVisible) {
            showBufferingIndicator();
        }

        if (null != mListener) {
            mListener.onVRModeChanged(mIsVRModeEnabled);
        }
    }

    /**
     * Disable VR mode.
     */
    void disableVRMode() {
        Logger.logF();

        mIsVRModeEnabled = false;

        // Handle the case where VR mode is toggled and progress bar
        // also needs to be swapped between normal and VR mode.
        if (mBufferingIndicatorVisible) {
            showBufferingIndicator();
        }

        if (null != mListener) {
            mListener.onVRModeChanged(mIsVRModeEnabled);
        }
    }

    /**
     * Toggle title panel.
     */
    public void toggleTitlePanel() {
        Logger.logF();

        if (mTitlePanelVisible) {
            hideTitlePanel();
        } else {
            showTitlePanel();
        }
    }

    /**
     * Show title panel.
     */
    public void showTitlePanel() {
        Logger.logF();

        if(!mTitlePanelVisible) {
            mTitlePanelView.startAnimation(mTitlePanelAnimationIn);
        }
    }

    /**
     * Hide title panel.
     */
    public void hideTitlePanel() {
        Logger.logF();

        if (mTitlePanelVisible) {
            mTitlePanelView.startAnimation(mTitlePanelAnimationOut);
        }
    }

    /**
     * Toggle control panel.
     */
    public void toggleControlPanel() {
        Logger.logF();

        if (mControlPanelVisible) {
            hideControlPanel();
        } else {
            showControlPanel();
        }
    }

    /**
     * Show control panel.
     */
    void showControlPanel() {
        Logger.logF();

        if(!mControlPanelVisible) {
            mControlPanelView.startAnimation(mControlPanelAnimationIn);
        }
    }

    /**
     * Hide control panel.
     */
    void hideControlPanel() {
        Logger.logF();

        if(mControlPanelVisible) {
            mControlPanelView.startAnimation(mControlPanelAnimationOut);
        }
    }

    /**
     * Run Play animation.
     */
    public void runPlayAnimation() {
        Logger.logF();

        mPlayOverlay.setVisibility(View.VISIBLE);
        mPlayOverlay.startAnimation(mPlayAnimation);
    }

    /**
     * Run Pause animation.
     */
    public void runPauseAnimation() {
        Logger.logF();

        mPauseOverlay.setVisibility(View.VISIBLE);
        mPauseOverlay.startAnimation(mPauseAnimation);
    }

    /**
     * Show buffering indicator.
     */
    void showBufferingIndicator() {
        Logger.logF();

        mBufferingIndicatorVisible = true;

        mBufferingIndicatorAnimation.cancel();
        mBufferingIndicatorNormal.clearAnimation();
        mBufferingIndicatorVRLeft.clearAnimation();
        mBufferingIndicatorVRRight.clearAnimation();

        if (mIsVRModeEnabled) {
            mBufferingIndicatorNormal.setVisibility(View.INVISIBLE);
            mBufferingIndicatorVRLeft.startAnimation(mBufferingIndicatorAnimation);
            mBufferingIndicatorVRRight.startAnimation(mBufferingIndicatorAnimation);
            mBufferingIndicatorVRLeft.setVisibility(View.VISIBLE);
            mBufferingIndicatorVRRight.setVisibility(View.VISIBLE);
        } else {
            mBufferingIndicatorVRLeft.setVisibility(View.INVISIBLE);
            mBufferingIndicatorVRRight.setVisibility(View.INVISIBLE);
            mBufferingIndicatorNormal.startAnimation(mBufferingIndicatorAnimation);
            mBufferingIndicatorNormal.setVisibility(ImageView.VISIBLE);
        }
    }

    /**
     * Hide buffering indicator.
     */
    void hideBufferingIndicator() {
        Logger.logF();

        mBufferingIndicatorVisible = false;

        mBufferingIndicatorAnimation.cancel();
        mBufferingIndicatorNormal.clearAnimation();
        mBufferingIndicatorVRLeft.clearAnimation();
        mBufferingIndicatorVRRight.clearAnimation();

        mBufferingIndicatorVRRight.setVisibility(ImageView.INVISIBLE);
        mBufferingIndicatorVRLeft.setVisibility(ImageView.INVISIBLE);
        mBufferingIndicatorNormal.setVisibility(ImageView.INVISIBLE);
    }
}
