/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.impl.osgi;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.jitsi.impl.osgi.framework.*;
import org.jitsi.impl.osgi.framework.launch.*;
import org.jitsi.service.configuration.*;
import org.jitsi.service.osgi.*;

import org.jitsi.util.*;
import org.osgi.framework.*;
import org.osgi.framework.launch.*;
import org.osgi.framework.startlevel.*;

/**
 * Implements the actual, internal functionality of {@link OSGiService}.
 *
 * @author Lyubomir Marinov
 */
public class OSGiServiceImpl
{
//    /**
//     */
//    private static final String[][] BUNDLES
//        = {
//            {
//                "net.java.sip.communicator.impl.libjitsi.LibJitsiActivator",
//                "net.java.sip.communicator.util.UtilActivator"
//            },
//            {
//                "net.java.sip.communicator.impl.fileaccess.FileAccessActivator"
//            },
//            {
//                "net.java.sip.communicator.impl.configuration.ConfigurationActivator",
//                "net.java.sip.communicator.impl.resources.ResourceManagementActivator",
//                "net.java.sip.communicator.impl.netaddr.NetaddrActivator",
//                "net.java.sip.communicator.impl.sysactivity.SysActivityActivator"
//            },
//            {
//                "net.java.sip.communicator.impl.credentialsstorage.CredentialsStorageActivator",
//                "net.java.sip.communicator.plugin.defaultresourcepack.DefaultResourcePackActivator",
//                "net.java.sip.communicator.impl.packetlogging.PacketLoggingActivator"
//            },
//            {
//                "net.java.sip.communicator.impl.version.VersionActivator",
//                "net.java.sip.communicator.impl.certificate.CertificateVerificationActivator"
//            },
//            {
//                "net.java.sip.communicator.service.protocol.ProtocolProviderActivator",
//                "net.java.sip.communicator.service.protocol.media.ProtocolMediaActivator"
//            },
//            {
//                "net.java.sip.communicator.impl.neomedia.NeomediaActivator",
//                "net.java.sip.communicator.impl.protocol.sip.SipActivator",
//    //                "net.java.sip.communicator.impl.protocol.jabber.JabberActivator",
//                "net.java.sip.communicator.plugin.reconnectplugin.ReconnectPluginActivator"
//            },
//            {
//                "net.java.sip.communicator.service.notification.NotificationServiceActivator"
//            },
//            {
//                "net.java.sip.communicator.impl.notification.NotificationActivator",
//                "net.java.sip.communicator.plugin.notificationwiring.NotificationWiringActivator",
//                "net.java.sip.communicator.plugin.loggingutils.LoggingUtilsActivator"
//            },
//            {
//                "org.jitsi.android.gui.GuiActivator",
//                "net.java.sip.communicator.plugin.sipaccregwizz.SIPAccountRegistrationActivator"
//            },
//    //            {
//    //                "net.java.sip.communicator.slick.protocol.sip.SipProtocolProviderServiceLick",
//    //                "net.java.sip.communicator.slick.runner.SipCommunicatorSlickRunner"
//    //            },
//            {
//                "org.jitsi.impl.osgi.OSGiServiceActivator"
//            }
//        };

    private final OSGiServiceBundleContextHolder bundleContextHolder
        = new OSGiServiceBundleContextHolder();

    private final AsyncExecutor<Runnable> executor
        = new AsyncExecutor<Runnable>(5, TimeUnit.MINUTES);

    /**
     * The <tt>org.osgi.framework.launch.Framework</tt> instance which
     * represents the OSGi instance launched by this <tt>OSGiServiceImpl</tt>.
     */
    private Framework framework;

    /**
     * The <tt>Object</tt> which synchronizes the access to {@link #framework}.
     */
    private final Object frameworkSyncRoot = new Object();

    private final OSGiService service;

    public OSGiServiceImpl(OSGiService service)
    {
        this.service = service;
    }

    public IBinder onBind(Intent intent)
    {
        return bundleContextHolder;
    }

    public void onCreate()
    {
        try
        {
            setScHomeDir();
        }
        catch (Throwable t)
        {
            if (t instanceof ThreadDeath)
                throw (ThreadDeath) t;
        }
        try
        {
            setJavaUtilLoggingConfigFile();
        }
        catch (Throwable t)
        {
            if (t instanceof ThreadDeath)
                throw (ThreadDeath) t;
        }

        executor.execute(new OnCreateCommand());
    }

    public void onDestroy()
    {
        synchronized (executor)
        {
            executor.execute(new OnDestroyCommand());
            executor.shutdown();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return Service.START_STICKY;
    }

    /**
     * Sets up <tt>java.util.logging.LogManager</tt> by assigning values to the
     * system properties which allow more control over reading the initial
     * configuration.
     */
    private void setJavaUtilLoggingConfigFile()
    {
    }

    private void setScHomeDir()
    {
        String name = null;

        if (System.getProperty(ConfigurationService.PNAME_SC_HOME_DIR_LOCATION)
                == null)
        {
            File filesDir = service.getFilesDir();
            String location = filesDir.getParentFile().getAbsolutePath();

            name = filesDir.getName();

            System.setProperty(
                    ConfigurationService.PNAME_SC_HOME_DIR_LOCATION,
                    location);
        }
        if (System.getProperty(ConfigurationService.PNAME_SC_HOME_DIR_NAME)
                == null)
        {
            if ((name == null) || (name.length() == 0))
            {
                ApplicationInfo info = service.getApplicationInfo();

                name = info.name;
                if ((name == null) || (name.length() == 0))
                    name = "Jitsi";
            }

            System.setProperty(
                    ConfigurationService.PNAME_SC_HOME_DIR_NAME,
                    name);
        }

        /*
         * Set the System property user.home as well because it may be relied
         * upon (e.g. FMJ).
         */
        String location
            = System.getProperty(
                    ConfigurationService.PNAME_SC_HOME_DIR_LOCATION);

        if ((location != null) && (location.length() != 0))
        {
            name
                = System.getProperty(
                        ConfigurationService.PNAME_SC_HOME_DIR_NAME);
            if ((name != null) && (name.length() != 0))
            {
                System.setProperty(
                        "user.home",
                        new File(location, name).getAbsolutePath());
            }
        }
    }

    private class OnCreateCommand
        implements Runnable
    {
        public void run()
        {
            FrameworkFactory frameworkFactory = new FrameworkFactoryImpl();
            Map<String, String> configuration = new HashMap<String, String>();

            TreeMap<Integer, List<String>> BUNDLES = getBundlesConfig(service);

            configuration.put(
                    Constants.FRAMEWORK_BEGINNING_STARTLEVEL,
                    Integer.toString(BUNDLES.lastKey()));

            Framework framework = frameworkFactory.newFramework(configuration);

            try
            {
                framework.init();

                BundleContext bundleContext = framework.getBundleContext();

                bundleContext.registerService(OSGiService.class, service, null);
                bundleContext.registerService(
                        BundleContextHolder.class,
                        bundleContextHolder,
                        null);

                for(Map.Entry<Integer, List<String>> startLevelEntry :
                    BUNDLES.entrySet())
                {
                    int startLevel = startLevelEntry.getKey();

                    for (String location : startLevelEntry.getValue())
                    {
                        org.osgi.framework.Bundle bundle
                            = bundleContext.installBundle(location);

                        if (bundle != null)
                        {
                            BundleStartLevel bundleStartLevel
                                = bundle.adapt(BundleStartLevel.class);

                            if (bundleStartLevel != null)
                                bundleStartLevel.setStartLevel(startLevel);
                        }
                    }
                }

                framework.start();
            }
            catch (BundleException be)
            {
                throw new RuntimeException(be);
            }

            synchronized (frameworkSyncRoot)
            {
                OSGiServiceImpl.this.framework = framework;
            }
        }

        /**
         * Loads bundles configuration from the configured or default
         * file name location.
         *
         * @param context the context to use
         * @return the locations of the OSGi bundles (or rather of the class files
         *  of their <tt>BundleActivator</tt> implementations) comprising the
         *  Jitsi core/library and the application which is currently using it.
         *  And the corresponding start levels
         */
        private TreeMap<Integer, List<String>> getBundlesConfig(Context context)
        {
            InputStream is = null;

            try
            {
                String fileName
                    = System.getProperty("osgi.config.properties");

                if (fileName == null)
                    fileName = "lib/osgi.client.run.properties";

                if (OSUtils.IS_ANDROID)
                {
                        if (context != null)
                        {
                            is = context.getAssets().open(
                                        fileName,
                                        AssetManager.ACCESS_UNKNOWN);
                        }
                }
                else
                {
                    is = new FileInputStream(fileName);
                }

                Properties properties = new Properties();
                properties.load(is);

                TreeMap<Integer, List<String>>
                    startLevels = new TreeMap<Integer, List<String>>();

                Enumeration<String> propNames =
                    (Enumeration<String>)properties.propertyNames();
                while(propNames.hasMoreElements())
                {
                    String prop = propNames.nextElement().trim();

                    if(prop.contains("auto.start."))
                    {
                        String startLevel =
                            prop.substring("auto.start.".length());

                        try
                        {
                            int startLevelInt = Integer.parseInt(startLevel);

                            StringTokenizer classTokens =
                                new StringTokenizer((String)properties.get(prop),
                                                    " ");

                            ArrayList<String> classNameList =
                                new ArrayList<String>();

                            while(classTokens.hasMoreTokens())
                            {
                                String className = classTokens.nextToken().trim();

                                if(className != null && className.length() > 0
                                    && !className.startsWith("#"))
                                    classNameList.add(className);
                            }

                            startLevels.put(startLevelInt, classNameList);
                        }
                        catch(Throwable t)
                        {}
                    }
                }

                return startLevels;
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                try
                {
                    if (is != null)
                        is.close();
                }
                catch(IOException e){}
            }
        }
    }

    private class OnDestroyCommand
        implements Runnable
    {
        public void run()
        {
            Framework framework;

            synchronized (frameworkSyncRoot)
            {
                framework = OSGiServiceImpl.this.framework;
                OSGiServiceImpl.this.framework = null;
            }

            if (framework != null)
                try
                {
                    framework.stop();
                }
                catch (BundleException be)
                {
                    throw new RuntimeException(be);
                }
        }
    }
}
