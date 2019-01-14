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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.IOException;

import fi.finwe.log.Logger;
import fi.finwe.orion360.streamdiagnoser.fragments.PlayerFragment;


/**
 * Main activity controls the main UI and manages its fragments.
 */
public class MainActivity extends Activity {

    /** Tag for debug logging. */
    protected static final String TAG = MainActivity.class.getSimpleName();

    // TIP: Add here your own URLs for each access without typing/copy-pasting!
    String[] mDefaultVideoURIs = {
            "https://player.vimeo.com/external/186333842.m3u8?s=93e42bd5d8ccff2817bb1e8fff7985d3abd83df1",
    };

    private PlayerFragment mPlayerFragment;

    private MyExoPlayerWrapper myExoPlayerWrapper;

    private TextView mMessageLog;

    private long mStartTime;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageLog = (TextView) findViewById(R.id.config_message_log);
        mMessageLog.setMovementMethod(new ScrollingMovementMethod());

        myExoPlayerWrapper = new MyExoPlayerWrapper(this);
        myExoPlayerWrapper.setAdaptiveMediaSourceEventListener(myAdaptiveMediaSourceEventListener);

        mPlayerFragment = (PlayerFragment) getFragmentManager().findFragmentById(
                R.id.fragment_player);

        if (null != mPlayerFragment) {
            mPlayerFragment.setVideoPlayer(myExoPlayerWrapper);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_singlechoice, mDefaultVideoURIs);
        final AutoCompleteTextView acTextView = (AutoCompleteTextView) findViewById(
                R.id.config_video_uri_editor);
        acTextView.setThreshold(0);
        acTextView.setAdapter(adapter);
        acTextView.setImeActionLabel("Play", KeyEvent.KEYCODE_ENTER);
        acTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String videoUri = acTextView.getText().toString();
                    Logger.logD(TAG, "User selected/typed video URI: " + videoUri);
                    if (null != mPlayerFragment) {
                        mStartTime = System.currentTimeMillis();
                        mMessageLog.setText(System.currentTimeMillis() - mStartTime + ": Playing video: " + videoUri);
                        mPlayerFragment.playVideo(videoUri);
                    }
                }

                return false;
            }
        });
    }

    private AdaptiveMediaSourceEventListener myAdaptiveMediaSourceEventListener = new AdaptiveMediaSourceEventListener() {
        public void onLoadStarted(DataSpec dataSpec, int i, int i1, Format format, int i2, Object o, long l, long l1, long l2) {
            Logger.logF();

            String segment = "";
            if (dataSpec != null) {
                try {
                    String spec = dataSpec.toString();
                    int start = spec.lastIndexOf('/') + 1;
                    int end = spec.indexOf('?');
                    if (end == -1) end = spec.indexOf(',');
                    if (end == -1) end = spec.length() - 1;
                    segment = spec.substring(start, end);
                } catch (Exception e) {}
            }

            if (format == null) {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load started "
                        + "Manifest=" + segment);
            } else {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load started "
                        + segment
                        + " Format=" + (format != null ? format.width + "x" + format.height + "@" + format.frameRate : "")
                        + " Time=[" + l + "," + l1 + "]");
            }
        }

        public void onLoadCompleted(DataSpec dataSpec, int i, int i1, Format format, int i2, Object o, long l, long l1, long l2, long l3, long l4) {
            Logger.logF();

            String segment = "";
            if (dataSpec != null) {
                try {
                    String spec = dataSpec.toString();
                    int start = spec.lastIndexOf('/') + 1;
                    int end = spec.indexOf('?');
                    if (end == -1) end = spec.indexOf(',');
                    if (end == -1) end = spec.length() - 1;
                    segment = spec.substring(start, end);
                } catch (Exception e) {}
            }

            if (format == null) {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load completed "
                        + "Manifest=" + segment);
            } else {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load completed "
                        + segment
                        + " Format=" + (format != null ? format.width + "x" + format.height + "@" + format.frameRate : "")
                        + " Time=[" + l + "," + l1 + "]"
                        + " Bytes=" + l4);
            }
        }

        public void onLoadCanceled(DataSpec dataSpec, int i, int i1, Format format, int i2, Object o, long l, long l1, long l2, long l3, long l4) {
            Logger.logF();

            String segment = "";
            if (dataSpec != null) {
                try {
                    String spec = dataSpec.toString();
                    int start = spec.lastIndexOf('/') + 1;
                    int end = spec.indexOf('?');
                    if (end == -1) end = spec.indexOf(',');
                    if (end == -1) end = spec.length() - 1;
                    segment = spec.substring(start, end);
                } catch (Exception e) {}
            }

            if (format == null) {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load canceled "
                        + "Manifest=" + segment);
            } else {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load canceled "
                        + segment
                        + " Format=" + (format != null ? format.width + "x" + format.height + "@" + format.frameRate : "")
                        + " Time=[" + l + "," + l1 + "]"
                        + " Bytes=" + l4);
            }
        }

        public void onLoadError(DataSpec dataSpec, int i, int i1, Format format, int i2, Object o, long l, long l1, long l2, long l3, long l4, IOException e, boolean b) {
            Logger.logF();

            String segment = "";
            if (dataSpec != null) {
                try {
                    String spec = dataSpec.toString();
                    int start = spec.lastIndexOf('/') + 1;
                    int end = spec.indexOf('?');
                    if (end == -1) end = spec.indexOf(',');
                    if (end == -1) end = spec.length() - 1;
                    segment = spec.substring(start, end);
                } catch (Exception e2) {}
            }

            if (format == null) {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load error "
                        + "Manifest=" + segment);
            } else {
                mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": Load error "
                        + segment
                        + " Format=" + (format != null ? format.width + "x" + format.height + "@" + format.frameRate : "")
                        + " Time=[" + l + "," + l1 + "]"
                        + " Bytes=" + l4);
            }
        }

        public void onUpstreamDiscarded(int i, long l, long l1) {
            Logger.logF();

            mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": onUpstreamDiscarded"
                    + " Time=[" + l + "," + l1 + "]");
        }

        public void onDownstreamFormatChanged(int i, Format format, int i1, Object o, long l) {
            Logger.logF();

            mMessageLog.append("\n" + (System.currentTimeMillis() - mStartTime) + ": onDownstreamFormatChanged"
                    + " Format=" + (format != null ? format.width + "x" + format.height + "@" + format.frameRate : ""));
        }
    };

    public void onSendEmail(View v) {
        Logger.logF();

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","support@finwe.fi", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Orion360 Stream Diagnoser Log");
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Message:\n[type your message here]\n\nDevice: " + Build.MANUFACTURER
                        + " " + Build.MODEL + " " + android.os.Build.VERSION.SDK_INT + " " + Build.SERIAL
                + "\nLog:\n" + mMessageLog.getText().toString());
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
