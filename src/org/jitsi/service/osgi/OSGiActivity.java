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
import android.os.Bundle; // disambiguation

import org.osgi.framework.*;

/**
 * Implements a base <tt>Activity</tt> which employs OSGi.
 *
 * @author Lyubomir Marinov
 */
public class OSGiActivity
    extends Activity
{
    private BundleActivator bundleActivator;

    private BundleContext bundleContext;

    private BundleContextHolder service;

    private ServiceConnection serviceConnection;

    /**
     * The EXIT action name that is broadcasted to all OSGiActivities 
     */
    static final String EXIT_ACTION = "org.jitsi.android.exit";

    /**
     * EXIT action listener that triggers closes the <tt>Activity</tt>
     */
    private ExitActionListener exitListener = new ExitActionListener();
    
    /**
     * Starts this osgi activity.
     *
     * @param bundleContext the osgi <tt>BundleContext</tt>
     * @throws Exception
     */
    private void internalStart(BundleContext bundleContext)
        throws Exception
    {
        this.bundleContext = bundleContext;

        boolean start = false;

        try
        {
            start(bundleContext);
            start = true;
        }
        finally
        {
            if (!start && (this.bundleContext == bundleContext))
                this.bundleContext = null;
        }
    }

    /**
     * Stops this osgi activity.
     *
     * @param bundleContext the osgi <tt>BundleContext</tt>
     * @throws Exception
     */
    private void internalStop(BundleContext bundleContext)
        throws Exception
    {
        if (this.bundleContext != null)
        {
            if (bundleContext == null)
                bundleContext = this.bundleContext;
            if (this.bundleContext == bundleContext)
                this.bundleContext = null;
            stop(bundleContext);
        }
    }

    /**
     * Called when the activity is starting. Initializes the corresponding
     * call interface.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ServiceConnection serviceConnection
            = new ServiceConnection()
            {
                public void onServiceConnected(
                        ComponentName name,
                        IBinder service)
                {
                    if (this == OSGiActivity.this.serviceConnection)
                        setService((BundleContextHolder) service);
                }

                public void onServiceDisconnected(ComponentName name)
                {
                    if (this == OSGiActivity.this.serviceConnection)
                        setService(null);
                }
            };

        this.serviceConnection = serviceConnection;

        boolean bindService = false;

        try
        {
            bindService
                = bindService(
                        new Intent(this, OSGiService.class),
                        serviceConnection,
                        BIND_AUTO_CREATE);
        }
        finally
        {
            if (!bindService)
                this.serviceConnection = null;
        }
        
        // Registers exit action listener
        this.registerReceiver(
                exitListener,
                new IntentFilter(EXIT_ACTION));
    }

    /**
     * Called when an activity is destroyed.
     */
    @Override
    protected void onDestroy()
    {
        // Unregisters exit action listener
        unregisterReceiver(exitListener);
        
        ServiceConnection serviceConnection = this.serviceConnection;

        this.serviceConnection = null;
        try
        {
            setService(null);
        }
        finally
        {
            if (serviceConnection != null)
                unbindService(serviceConnection);
        }

        super.onDestroy();
    }

    private void setService(BundleContextHolder service)
    {
        if (this.service != service)
        {
            if ((this.service != null) && (bundleActivator != null))
            {
                try
                {
                    this.service.removeBundleActivator(bundleActivator);
                    bundleActivator = null;
                }
                finally
                {
                    try
                    {
                        internalStop(null);
                    }
                    catch (Throwable t)
                    {
                        if (t instanceof ThreadDeath)
                            throw (ThreadDeath) t;
                    }
                }
            }

            this.service = service;

            if (this.service != null)
            {
                if (bundleActivator == null)
                {
                    bundleActivator
                        = new BundleActivator()
                        {
                            public void start(BundleContext bundleContext)
                                throws Exception
                            {
                                internalStart(bundleContext);
                            }

                            public void stop(BundleContext bundleContext)
                                throws Exception
                            {
                                internalStop(bundleContext);
                            }
                        };
                }
                this.service.addBundleActivator(bundleActivator);
            }
        }
    }

    protected void start(BundleContext bundleContext)
        throws Exception
    {
    }

    protected void stop(BundleContext bundleContext)
        throws Exception
    {
    }

    /**
     * Convenience method which starts a new activity
     * for given <tt>activityClass</tt> class
     *
     * @param activityClass the activity class
     */
    protected void startActivity(Class<?> activityClass)
    {
        Intent intent
                = new Intent(this, activityClass);
        startActivity(intent);
    }

    /**
     * Shutdowns the app by stopping <tt>OSGiService</tt> and broadcasting 
     * {@link #EXIT_ACTION}.
     * 
     */
    protected void shutdownApplication()
    {
        // Shutdown the OSGi service
        stopService(new Intent(this, OSGiService.class));
        // Broadcast the exit action
        Intent exitIntent = new Intent();
        exitIntent.setAction(EXIT_ACTION);
        sendBroadcast(exitIntent);
    }

    /**
     * Convenience method that switches from one activity to another.
     *
     * @param activityClass the activity class
     */
    protected void switchActivity(Class<?> activityClass)
    {
        startActivity(activityClass);
        finish();
    }

    /**
     * Broadcast listener that listens for {@link #EXIT_ACTION} and then 
     * finishes this <tt>Activity</tt>
     * 
     */
    class ExitActionListener 
        extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            finish();
        }
    }
}
