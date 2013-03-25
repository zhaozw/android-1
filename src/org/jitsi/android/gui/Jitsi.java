/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui;

import android.content.*;
import android.content.res.*;
import android.os.Bundle; // disambiguation
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.account.*;

import org.jitsi.*;
import org.jitsi.android.gui.account.*;
import org.jitsi.android.gui.menu.*;
import org.jitsi.android.gui.util.*;
import org.osgi.framework.*;

/**
 * Implements a test <tt>Activity</tt> which employs OSGi.
 *
 * @author Damian Minkov
 * @author Lyubomir Marinov
 * @author Yana Stamcheva
 */
public class Jitsi
    extends MainMenuActivity
{
    /**
     * The logger
     */
    private static final Logger logger = Logger.getLogger(Jitsi.class);
    
    /**
     * The OSGI bundle context.
     */
    public static BundleContext bundleContext;

    /**
     * The android context.
     */
    private static Context androidContext;

    /**
     * The {@link Resources} for application
     */
    private static Resources applicationResources;

    /**
     * A call back parameter.
     */
    public static final int OBTAIN_CREDENTIALS = 1;

    /**
     * The login manager.
     */
    private static LoginManager loginManager;

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
        if (bundleContext == null)
        {
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            setProgressBarIndeterminateVisibility(true);
        }

        super.onCreate(savedInstanceState);

        androidContext = this;

        applicationResources = getBaseContext().getResources();

        setContentView(R.layout.main);

        ImageView myImageView
            = (ImageView)findViewById(R.id.loadingImage);
        Animation myFadeInAnimation
            = AnimationUtils.loadAnimation(this, R.anim.fadein);
        myImageView.startAnimation(myFadeInAnimation);
    }

    /**
     * Called when an activity is destroyed.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        synchronized (this)
        {
            if (bundleContext != null)
                try
                {
                    stop(bundleContext);
                }
                catch (Throwable t)
                {
                    logger.error(
                            "Error stopping application:"
                                    + t.getLocalizedMessage(), t);
                    if (t instanceof ThreadDeath)
                        throw (ThreadDeath) t;
                }
        }
    }

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

        Jitsi.bundleContext = bundleContext;

        // Register the alert service android implementation.
        AlertUIServiceImpl alertServiceImpl = new AlertUIServiceImpl(this);

        bundleContext.registerService(  AlertUIService.class.getName(),
                                        alertServiceImpl,
                                        null);

        AndroidLoginRenderer loginRenderer = new AndroidLoginRenderer(this);
        loginManager = new LoginManager(loginRenderer);

        runOnUiThread(new Runnable()
        {
            public void run()
            {
                initActivity();
            }
        });
    }

    /**
     * Stops this osgi activity.
     *
     * @param bundleContext the osgi <tt>BundleContext</tt>
     * @throws Exception
     */
    @Override
    protected synchronized void stop(BundleContext bundleContext)
        throws Exception
    {
        if (Jitsi.bundleContext != null)
            Jitsi.bundleContext = null;
    }

    /**
     * Returns the login manager.
     *
     * @return the login manager
     */
    public static LoginManager getLoginManager()
    {
        return loginManager;
    }

    /**
     * Returns the android context.
     *
     * @return the android application context
     */
    public static Context getAndroidContext()
    {
        return androidContext;
    }

    /**
     * Returns the {@link Resources} of the application context
     *
     * @return the {@link Resources} object for the application
     */
    public static Resources getAppResources()
    {
        return applicationResources;
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent accountLoginIntent)
    {
        super.onActivityResult(requestCode, resultCode, accountLoginIntent);

        switch(requestCode)
        {
            case OBTAIN_CREDENTIALS:
                if(resultCode == RESULT_OK)
                {
                    System.err.println("ACCOUNT DATA STRING===="
                        + accountLoginIntent.getDataString());
                }
        }
    }

    /**
     * Initializes the first activity.
     */
    private void initActivity()
    {
        this.setProgressBarIndeterminateVisibility(false);

        AccountManager accountManager
            = ServiceUtils.getService(
                Jitsi.bundleContext, AccountManager.class);

        if (accountManager.getStoredAccounts().size() > 0)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    loginManager.runLogin();
                }
            }).start();
        }
        else
        {
            androidContext.startActivity(
                    new Intent(androidContext,
                            AccountLoginActivity.class));
        }
    }
}
