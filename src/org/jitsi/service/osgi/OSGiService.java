/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.service.osgi;

import android.app.*;
import android.content.*;
import android.os.*;

import net.java.sip.communicator.util.*;
import org.jitsi.impl.osgi.*;

/**
 *
 * @author Lyubomir Marinov
 */
public class OSGiService
    extends Service
{
    private final OSGiServiceImpl impl;

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
