/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.menu;

import org.jitsi.service.osgi.*;

import android.os.*;
import org.jitsi.*;

public class MainMenu
    extends OSGiActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.menu.main_menu);
    }
}
