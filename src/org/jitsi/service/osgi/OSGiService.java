/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.service.osgi;

import org.jitsi.impl.osgi.*;

import android.app.*;
import android.content.*;
import android.os.*;

/**
 * Implements an Android {@link Service} which (automatically) starts and stops
 * an OSGi framework (implementation).
 *
 * @author Lyubomir Marinov
 */
public class OSGiService
    extends Service
{
    /**
     * The very implementation of this Android <tt>Service</tt> which is split
     * out of the class <tt>OSGiService</tt> so that the class
     * <tt>OSGiService</tt> may remain in a <tt>service</tt> package and be
     * treated as public from the Android point of view and the class
     * <tt>OSGiServiceImpl</tt> may reside in an <tt>impl</tt> package and be
     * recognized as internal from the Jitsi point of view.
     */
    private final OSGiServiceImpl impl;

    /**
     * Initializes a new <tt>OSGiService</tt> implementation.
     */
    public OSGiService()
    {
        impl = new OSGiServiceImpl(this);
    }

    public IBinder onBind(Intent intent)
    {
        return impl.onBind(intent);
    }

    @Override
    public void onCreate()
    {
        impl.onCreate();
    }

    @Override
    public void onDestroy()
    {
        impl.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return impl.onStartCommand(intent, flags, startId);
    }
}
