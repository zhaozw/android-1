/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android;

import android.app.*;
import android.content.*;
import org.jitsi.android.gui.call.*;

/**
 * Jitsi application used, as a global context.
 *
 * @author Pawel Domas
 */
public class JitsiApplication
    extends Application
{
    /**
     * The home activity class.
     */
    private static final Class<?> HOME_SCREEN_CLASS = CallContactActivity.class;

    /**
     * Static instance holder.
     */
    private static JitsiApplication instance;

    public JitsiApplication()
    {
        instance = this;
    }

    /**
     * Returns global application context.
     *
     * @return Returns global application <tt>Context</tt>.
     */
    public static Context getGlobalContext()
    {
        return instance.getApplicationContext();
    }

    /**
     * Returns home <tt>Activity</tt> class.
     * @return Returns home <tt>Activity</tt> class.
     */
    public static Class<?> getHomeScreenActivityClass()
    {
        return HOME_SCREEN_CLASS;
    }
}
