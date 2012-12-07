/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui;

import android.content.*;
import android.os.Bundle; // disambiguation
import android.view.*;
import android.view.Window;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

import org.jitsi.android.gui.account.*;
import org.jitsi.android.gui.call.*;
import org.jitsi.service.osgi.*;
import org.osgi.framework.*;

/**
 * Implements a test <tt>Activity</tt> which employs OSGi.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
 */
public class Jitsi
    extends OSGiActivity
{
    /**
     * The SIP address of the callee to establish a call to from the SIP account
     * defined by {@link #USER_ID} and {@link #PASSWORD}. The value committed to
     * the version control system is empty and each developer is to fill in the
     * desired value in their local sandbox. If the value is (left) empty, no
     * attempt to establish a call to <tt>CALLEE</tt> will be made.
     */
    private static final String CALLEE = "";

    private static final String DISPLAY_NAME = "";

    private static final String PASSWORD = "";

    private static final String USER_ID = "";

    private BundleContext bundleContext;

    private final RegistrationStateChangeListener
        registrationStateChangeListener
            = new RegistrationStateChangeListener()
        {
            public void registrationStateChanged(
                    RegistrationStateChangeEvent event)
            {
                Jitsi.this.registrationStateChanged(event);
            }
        };

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

    private final ServiceListener serviceListener
        = new ServiceListener()
        {
            public void serviceChanged(ServiceEvent event)
            {
                Jitsi.this.serviceChanged(event);
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (bundleContext == null)
        {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            setProgressBarIndeterminateVisibility(true);
        }

//        setContentView(R.layout.main);

//        Button sendProcessSignal
//            = (Button) findViewById(R.id.sendProcessSignal);
//
//        if (sendProcessSignal != null)
//        {
//            sendProcessSignal.setOnClickListener(
//                    new View.OnClickListener()
//                    {
//                        public void onClick(View view)
//                        {
//                            Process.sendSignal(Process.myPid(), 3);
//                        }
//                    });
//        }
//
//        Button stopOSGiFramework
//            = (Button) findViewById(R.id.stopOSGiFramework);
//
//        if (stopOSGiFramework != null)
//        {
//            stopOSGiFramework.setOnClickListener(
//                    new View.OnClickListener()
//                    {
//                        public void onClick(View view)
//                        {
//                            synchronized (this)
//                            {
//                                if (bundleContext != null)
//                                {
//                                    try
//                                    {
//                                        bundleContext.getBundle(0).stop();
//                                    }
//                                    catch (BundleException be)
//                                    {
//                                        be.printStackTrace(System.err);
//                                    }
//                                }
//                            }
//                        }
//                    });
//        }
    }

    @Override
    protected void onDestroy()
    {
        try
        {
            synchronized (this)
            {
                if (bundleContext != null)
                    try
                    {
                        stop(bundleContext);
                    }
                    catch (Throwable t)
                    {
                        if (t instanceof ThreadDeath)
                            throw (ThreadDeath) t;
                    }
            }
        }
        finally
        {
            super.onDestroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void registrationStateChanged(RegistrationStateChangeEvent event)
    {
//        ProtocolProviderService pps = event.getProvider();
//
//        if ((CALLEE != null)
//                && (CALLEE.length() != 0)
//                && RegistrationState.REGISTERED.equals(event.getNewState())
//                && pps.isRegistered())
//        {
//            OperationSetBasicTelephony<?> osbt
//                = pps.getOperationSet(OperationSetBasicTelephony.class);
//            OperationSetVideoTelephony osvt
//                = pps.getOperationSet(OperationSetVideoTelephony.class);
//
//            if ((osbt != null) && (osvt != null))
//            {
//                Call call = null;
//
//                try
//                {
//                    call = osvt.createVideoCall(CALLEE);
//                }
//                catch (OperationFailedException ofe)
//                {
//                    ofe.printStackTrace(System.err);
//                }
//                catch (java.text.ParseException pe)
//                {
//                    pe.printStackTrace(System.err);
//                }
//                if (call != null)
//                {
//                    Iterator<? extends CallPeer> callPeerIter
//                        = call.getCallPeers();
//
//                    if (callPeerIter.hasNext())
//                        addVideoListener(callPeerIter.next());
//                }
//            }
//        }
    }

    private void serviceChanged(ServiceEvent event)
    {
        switch (event.getType())
        {
        case ServiceEvent.REGISTERED:
            {
            ServiceReference<?> serviceReference = event.getServiceReference();

            synchronized (this)
            {
                if (bundleContext != null)
                {
                    Object service = bundleContext.getService(serviceReference);

                    if (service instanceof ProtocolProviderService)
                    {
                        ProtocolProviderService pps
                            = (ProtocolProviderService) service;

                        pps.addRegistrationStateChangeListener(
                                registrationStateChangeListener);
                        try
                        {
                            pps.register(securityAuthority);
                        }
                        catch (OperationFailedException ofe)
                        {
                            ofe.printStackTrace(System.err);
                        }
                    }
                }
            }
            }
            break;

        case ServiceEvent.UNREGISTERING:
            {
            ServiceReference<?> serviceReference = event.getServiceReference();

            synchronized (this)
            {
                if (bundleContext != null)
                {
                    Object service = bundleContext.getService(serviceReference);

                    if (service instanceof ProtocolProviderService)
                    {
                        ProtocolProviderService pps
                            = (ProtocolProviderService) service;

                        pps.removeRegistrationStateChangeListener(
                                registrationStateChangeListener);
                    }
                }
            }
            }
            break;
        }
    }

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
                initActivity();
            }
        });

        /*
         * If there is no account stored from previous runs of the application,
         * create a new SIP account using USER_ID and PASSWORD.
         */
        bundleContext.addServiceListener(serviceListener);

        AccountManager accountManager
            = ServiceUtils.getService(
                    bundleContext,
                    AccountManager.class);

        if ((USER_ID != null)
                && (USER_ID.length() != 0)
                && !accountManager.hasStoredAccounts(ProtocolNames.SIP, false))
        {
            Collection<ServiceReference<ProtocolProviderFactory>> ppfs
                = bundleContext.getServiceReferences(
                        ProtocolProviderFactory.class,
                        '('
                            + ProtocolProviderFactory.PROTOCOL
                            + '='
                            + ProtocolNames.SIP
                            + ')');
            Iterator<ServiceReference<ProtocolProviderFactory>> ppfi
                = ppfs.iterator();

            if (ppfi.hasNext())
            {
                ProtocolProviderFactory ppf
                    = bundleContext.getService(ppfi.next());

                if (ppf != null)
                {
                    Map<String, String> accountProperties
                        = new HashMap<String, String>();
                    String userID = USER_ID;
                    String serverAddress
                        = userID.substring(userID.indexOf('@') + 1);

                    accountProperties.put(
                            ProtocolProviderFactory.DEFAULT_ENCRYPTION,
                            Boolean.FALSE.toString());
                    accountProperties.put(
                            ProtocolProviderFactory.DEFAULT_SIPZRTP_ATTRIBUTE,
                            Boolean.FALSE.toString());
                    accountProperties.put(
                            ProtocolProviderFactory.DISPLAY_NAME,
                            DISPLAY_NAME);
                    accountProperties.put(
                            ProtocolProviderFactory.PASSWORD,
                            PASSWORD);
                    accountProperties.put(
                            ProtocolProviderFactory.PROTOCOL,
                            ProtocolNames.SIP);
                    accountProperties.put(
                            ProtocolProviderFactory.PROXY_ADDRESS,
                            serverAddress);
                    accountProperties.put(
                            ProtocolProviderFactory.SERVER_ADDRESS,
                            serverAddress);
                    accountProperties.put(
                            ProtocolProviderFactory.USER_ID,
                            userID);

                    ppf.installAccount(userID, accountProperties);
                }
            }
        }
    }

    @Override
    protected synchronized void stop(BundleContext bundleContext)
        throws Exception
    {
        if (this.bundleContext != null)
        {
            this.bundleContext.removeServiceListener(serviceListener);
            this.bundleContext = null;
        }
    }

    private void initActivity()
    {
        setProgressBarIndeterminateVisibility(false);

        AccountManager accountManager
            = ServiceUtils.getService(bundleContext, AccountManager.class);

        Iterator<AccountID> storedAccounts
            = accountManager.getStoredAccounts().iterator();

        Intent nextIntent = null;
        if (storedAccounts.hasNext())
        {
            nextIntent = new Intent(Jitsi.this, CallContact.class);
        }
        else
            nextIntent = new Intent(Jitsi.this, NewAccount.class);

//        ((ViewStub) findViewById(R.id.new_account_form)).inflate();

        Jitsi.this.startActivity(nextIntent);
    }

}
