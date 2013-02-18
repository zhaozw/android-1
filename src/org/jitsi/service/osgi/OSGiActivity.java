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
    }

    /**
     * Called when an activity is destroyed.
     */
    @Override
    protected void onDestroy()
    {
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
     * Convinience class thats switches from one activity to another.
     *
     * @param activityClass the activity class
     */
    protected void switchActivity(Class<?> activityClass)
    {
        startActivity(activityClass);
        finish();
    }
}
