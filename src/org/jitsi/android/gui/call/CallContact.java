/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.call;

import java.text.*;
import java.util.*;

import org.jitsi.service.osgi.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.call.*;

import org.jitsi.*;
import org.osgi.framework.*;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class CallContact
    extends OSGiActivity
{
    private BundleContext bundleContext;

    private ProtocolProviderService protocolProvider;

    private static final Map<Object, ProtocolProviderFactory>
        providerFactoriesMap = new Hashtable<Object, ProtocolProviderFactory>();

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

        initAccounts();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.call_contact);

        final ImageView callButton = (ImageView) findViewById(R.id.callButton);

        if (protocolProvider == null || !protocolProvider.isRegistered())
            callButton.setEnabled(false);

        callButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                final EditText callField
                    = (EditText) findViewById(R.id.callField);

                createVideoCall(callField.getText().toString());
            }
        });
    }

    private void createVideoCall(final String destination)
    {
        new Thread()
        {
            public void run()
            {
                OperationSetVideoTelephony opSetVideoTelephony
                    = protocolProvider.getOperationSet(
                        OperationSetVideoTelephony.class);

                if (opSetVideoTelephony != null)
                {
                    Call call = null;
                    try
                    {
                        call = opSetVideoTelephony
                            .createVideoCall(destination);

                        CallManager.addActiveCall(call);

                        Intent videoCallIntent
                            = new Intent(CallContact.this, VideoCall.class);

                        CallContact.this.startActivity(videoCallIntent);
                    }
                    catch (OperationFailedException e)
                    {
                        e.printStackTrace();
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
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
                    = getRegisteredProviderForAccount(accountID);

                if (this.protocolProvider == null)
                {
                    this.protocolProvider = protocolProvider;

                    if (!protocolProvider.isRegistered())
                        login(protocolProvider);
                    else
                        setCallButtonEnabled();

                    break;
                }
//                if (protocolProvider != null)
//                {
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
//                }
            }

        }
    }

    private final SecurityAuthority securityAuthority
        = new SecurityAuthority()
        {
            public boolean isUserNameEditable()
            {
                // TODO Auto-generated method stub
                return false;
            }

            public UserCredentials obtainCredentials(
                    String realm,
                    UserCredentials defaultValues)
            {
                // TODO Auto-generated method stub
                return null;
            }

            public UserCredentials obtainCredentials(
                    String realm,
                    UserCredentials defaultValues,
                    int reasonCode)
            {
                // TODO Auto-generated method stub
                return null;
            }

            public void setUserNameEditable(boolean isUserNameEditable)
            {
                // TODO Auto-generated method stub
            }
        };

        /**
         * Registers the given protocol provider.
         *
         * @param protocolProvider the ProtocolProviderService to register.
         */
        public void login(ProtocolProviderService protocolProvider)
        {
            protocolProvider.addRegistrationStateChangeListener(
                registrationStateChangeListener);

            new RegisterProvider(protocolProvider, securityAuthority).start();
        }

        /**
         * Registers a protocol provider in a separate thread.
         */
        private class RegisterProvider
            extends Thread
        {
            private final ProtocolProviderService protocolProvider;

            private final SecurityAuthority secAuth;

            RegisterProvider(ProtocolProviderService protocolProvider,
                SecurityAuthority secAuth)
            {
                this.protocolProvider = protocolProvider;
                this.secAuth = secAuth;
            }

            /**
             * Registers the contained protocol provider and process all possible
             * errors that may occur during the registration process.
             */
            public void run()
            {
                try
                {
                    protocolProvider.register(secAuth);
                }
                catch (OperationFailedException ex)
                {
                    
                }
                catch (Throwable ex)
                {
                    
                }
            }
        }

        private final RegistrationStateChangeListener
            registrationStateChangeListener
                = new RegistrationStateChangeListener()
            {
                public void registrationStateChanged(
                        RegistrationStateChangeEvent event)
                {
                    if (RegistrationState.REGISTERED.equals(event.getNewState())
                        && protocolProvider.isRegistered())
                    {
                        setCallButtonEnabled();
                    }
                }
            };

    private void setCallButtonEnabled()
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                final ImageView callButton
                    = (ImageView) findViewById(R.id.callButton);

                callButton.setEnabled(true);
            }
        });
    }

    /**
     * Returns all <tt>ProtocolProviderFactory</tt>s obtained from the bundle
     * context.
     *
     * @return all <tt>ProtocolProviderFactory</tt>s obtained from the bundle
     *         context
     */
    private Map<Object, ProtocolProviderFactory>
        getProtocolProviderFactories()
    {
        ServiceReference[] serRefs = null;
        try
        {
            // get all registered provider factories
            serRefs
                = bundleContext.getServiceReferences(
                        ProtocolProviderFactory.class.getName(),
                        null);
        }
        catch (InvalidSyntaxException e)
        {
            System.err.println("LoginManager : " + e);
        }

        if (serRefs != null)
        {
            for (ServiceReference serRef : serRefs)
            {
                ProtocolProviderFactory providerFactory
                    = (ProtocolProviderFactory)
                        bundleContext.getService(serRef);

                providerFactoriesMap.put(
                        serRef.getProperty(ProtocolProviderFactory.PROTOCOL),
                        providerFactory);
            }
        }
        return providerFactoriesMap;
    }

    /**
     * Returns the <tt>ProtocolProviderService</tt> corresponding to the given
     * account identifier that is registered in the given factory
     * @param accountID the identifier of the account
     * @return the <tt>ProtocolProviderService</tt> corresponding to the given
     * account identifier that is registered in the given factory
     */
    private ProtocolProviderService getRegisteredProviderForAccount(
        AccountID accountID)
    {
        for (ProtocolProviderFactory factory
                : getProtocolProviderFactories().values())
        {
            if (factory.getRegisteredAccounts().contains(accountID))
            {
                ServiceReference serRef
                    = factory.getProviderForAccount(accountID);

                if (serRef != null)
                {
                    return
                        (ProtocolProviderService)
                            bundleContext.getService(serRef);
                }
            }
        }
        return null;
    }
}
