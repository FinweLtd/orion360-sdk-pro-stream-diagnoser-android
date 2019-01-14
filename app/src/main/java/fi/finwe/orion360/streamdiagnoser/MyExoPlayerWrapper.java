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

import android.content.Context;

import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;

import java.lang.reflect.Field;

import fi.finwe.log.Logger;
import fi.finwe.orion360.sdk.pro.source.ExoPlayerWrapper;

/**
 * Custom wrapper for ExoPlayer.
 */
public class MyExoPlayerWrapper extends ExoPlayerWrapper {

    /** Tag for debug logging. */
    private static final String TAG = MainActivity.class.getSimpleName();


    /**
     * Constructor.
     *
     * @param context the context.
     */
    public MyExoPlayerWrapper(Context context) {
        super(context);
    }

    void setAdaptiveMediaSourceEventListener(AdaptiveMediaSourceEventListener listener) {
        Logger.logF();

        // Use Java reflection to replace private listener with ours.
        try {
            Object cc = this;
            Field f1 = cc.getClass().getSuperclass().getDeclaredField("mAdaptiveMediaSourceEventListener");
            f1.setAccessible(true);
            f1.set(cc, listener);
        } catch (Exception e) {
            Logger.logE(TAG, "Failed", e);
        }
    }
}
