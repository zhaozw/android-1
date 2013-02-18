
/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.call;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.AccountManager;

import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.account.*;
import net.java.sip.communicator.util.call.*;

import org.jitsi.*;
import org.jitsi.android.gui.*;
import org.jitsi.android.gui.menu.*;
import org.jitsi.android.gui.util.*;
import org.osgi.framework.*;

import android.accounts.*;
import android.os.Bundle;
import android.telephony.*;
import android.view.*;
import android.widget.*;

/**
 * Tha <tt>CallContactActivity</tt> is the one shown when we have a registered
 * account.
 *
 * @author Yana Stamcheva
 */
public class CallContactActivity
        extends MainMenuActivity
{
    /**
     * The bundle context.
     */
    private BundleContext bundleContext;

    /**
     * The protocol provider used for calling.
     */
    private ProtocolProviderService protocolProvider;

    /**
     * Starts this osgi activity.
     *
     * @param bundleContext the osgi <tt>BundleContext</tt>
     * @throws Exception
     */
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

        //initAndroidAccounts();

        new Thread()
        {
            public void run()
            {
                initAccounts();
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.call_contact);

        final ImageView callButton
                = (ImageView) findViewById(R.id.callButtonFull);

        if (protocolProvider == null || !protocolProvider.isRegistered())
            callButton.setEnabled(false);
        callButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                final EditText callField
                        = (EditText) findViewById(R.id.callField);
                String contact = callField.getText().toString();
                if(contact.isEmpty())
                {
                    System.err.println("Contact is empty");
                    return;
                }
                System.err.println("Calling "+contact);
                
                if (AccountUtils.getRegisteredProviders().size() > 1)
                    showCallViaMenu(callButton, contact);
                else
                    createCall(contact);
            }
        });

        String phoneNumber = null;
        if (getIntent().getDataString() != null)
            phoneNumber = PhoneNumberUtils.getNumberFromIntent( getIntent(),
                    getApplicationContext());

        if (phoneNumber != null && phoneNumber.length() > 0)
        {
            TextView callField = (TextView) findViewById(R.id.callField);
            callField.setText(phoneNumber);
        }
    }

    private void createCall(final String destination)
    {
        new Thread()
        {
            public void run()
            {
                try
                {
                    CallManager.createCall(protocolProvider, destination);
                }
                catch(Throwable t)
                {
                    AndroidUtils.showAlertDialog(
                        CallContactActivity.this,
                        getString(R.string.service_gui_ERROR),
                        t.getMessage());
                }
            }
        }.start();
    }

    private void initAndroidAccounts()
    {
        android.accounts.AccountManager androidAccManager
            = android.accounts.AccountManager.get(this);

        Account[] androidAccounts
            = androidAccManager.getAccountsByType(
                getString(R.string.ACCOUNT_TYPE));

        for (Account account: androidAccounts)
        {
            System.err.println("ACCOUNT======" + account);
        }
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
            System.err.println("Trying account "+accountID.getDisplayName()+
                    " hidden? "+isHidden);
            if (isHidden)
                continue;

            if (accountManager.isAccountLoaded(accountID))
            {
                ProtocolProviderService protocolProvider
                    = AccountUtils.getRegisteredProviderForAccount(accountID);

                if (this.protocolProvider == null && protocolProvider != null)
                {
                    this.protocolProvider = protocolProvider;

                    if (!protocolProvider.isRegistered())
                    {
                        Jitsi.getLoginManager().login(protocolProvider);
                    }
                    else
                    {
                        System.err.print("Acc "+accountID+" is logged in");
                        setCallButtonEnabled();
                    }
                    break;
                }else
                {
                    System.err.println("No provider for "+accountID);
                }
            }else
            {
                System.err.println("Account not loaded: "+accountID);
            }

        }
    }

    /**
     * Enables the call button.
     */
    private void setCallButtonEnabled()
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                final ImageView callButton
                    = (ImageView) findViewById(R.id.callButtonFull);

                callButton.setEnabled(true);
            }
        });
    }

    /**
     * 
     * @param v
     * @param destination
     */
    private void showCallViaMenu(View v, final String destination)
    {
        PopupMenu popup = new PopupMenu(this, v);

        Menu menu = popup.getMenu();

        Iterator<ProtocolProviderService> registeredProviders
            = AccountUtils.getRegisteredProviders().iterator();
        Map<Integer, ProtocolProviderService> providerIds
            = new Hashtable<Integer, ProtocolProviderService>();

        while (registeredProviders.hasNext())
        {
            String accountAddress
                = registeredProviders.next().getAccountID().getAccountAddress();

            MenuItem menuItem = menu.add(   Menu.NONE,
                                            Menu.NONE,
                                            Menu.NONE,
                                            accountAddress);

            menuItem.setOnMenuItemClickListener(
                new MenuItem.OnMenuItemClickListener()
            {
                public boolean onMenuItemClick(MenuItem item)
                {
                    createCall(destination);

                    return false;
                }
            });
        }

        popup.show();
    }
}