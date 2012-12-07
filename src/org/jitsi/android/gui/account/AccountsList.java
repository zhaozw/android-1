/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.account;

import java.util.*;

import org.jitsi.android.gui.*;
import org.jitsi.android.gui.call.*;
import org.jitsi.service.osgi.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class AccountsList
    extends OSGiActivity
{
    private BundleContext bundleContext;

    private LinearLayout layout;

    @Override
    protected synchronized void start(BundleContext bundleContext)
        throws Exception
    {
        /*
         * If there are unit tests to be run, do not run anything else and just
         * perform the unit tests.
         */
        if (System.getProperty(
                    "net.java.sip.communicator.slick.runner.TEST_LIST")
                != null)
            return;

        this.bundleContext = bundleContext;

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                initAccounts();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);

        ScrollView sv = new ScrollView(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        sv.addView(layout);

        setContentView(sv);
    }

    /**
     * Initializes accounts.
     */
    private void initAccounts()
    {
        AccountManager accountManager
            = ServiceUtils.getService(bundleContext, AccountManager.class);

        Iterator<AccountID> storedAccounts
            = accountManager.getStoredAccounts().iterator();

        while (storedAccounts.hasNext())
        {
            AccountID accountID = storedAccounts.next();

            boolean isHidden = accountID.getAccountPropertyBoolean(
                ProtocolProviderFactory.IS_PROTOCOL_HIDDEN, false);

            if (isHidden)
                continue;

            if (accountManager.isAccountLoaded(accountID))
            {
                ProtocolProviderService protocolProvider
                    = GuiActivator.getRegisteredProviderForAccount(accountID);

                if (protocolProvider != null)
                {
//                    protocolProvider.addRegistrationStateChangeListener(this);
//
//                    OperationSetPresence presence
//                        = protocolProvider
//                            .getOperationSet(OperationSetPresence.class);
//
//                    if (presence != null)
//                    {
//                        presence.addProviderPresenceStatusListener(this);
//                    }
                }
            }

            TextView accountView = new TextView(this);
            accountView.setText(accountID.getDisplayName());
            accountView.setClickable(true);

            accountView.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    Intent callContactIntent
                        = new Intent(AccountsList.this, CallContact.class);

                    AccountsList.this.startActivity(callContactIntent);
                }
            });

            layout.addView(accountView);
        }
    }
}
