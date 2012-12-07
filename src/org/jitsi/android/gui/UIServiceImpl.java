/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import org.jitsi.android.gui.login.*;
import net.java.sip.communicator.service.contactlist.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.service.gui.Container;
import net.java.sip.communicator.service.gui.event.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.Logger;
import org.jitsi.util.*;

/**
 * An implementation of the <tt>UIService</tt> that gives access to other
 * bundles to this particular swing ui implementation.
 *
 * @author Yana Stamcheva
 * @author Lubomir Marinov
 * @author Damian Minkov
 */
public class UIServiceImpl
    implements UIService
{
    /**
     * The <tt>Logger</tt> used by the <tt>UIServiceImpl</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger = Logger.getLogger(UIServiceImpl.class);

    /**
     * Creates an instance of <tt>UIServiceImpl</tt>.
     */
    public UIServiceImpl()
    {
    }

    /**
     * Implements <code>UISercie.getSupportedContainers</code>. Returns the
     * list of supported containers by this implementation .
     *
     * @return an Iterator over all supported containers.
     */
    public Iterator<Container> getSupportedContainers()
    {
        return new ArrayList<Container>().iterator();
    }

    /**
     * Implements <code>isVisible</code> in the UIService interface. Checks if
     * the main application window is visible.
     *
     * @return <code>true</code> if main application window is visible,
     *         <code>false</code> otherwise
     * @see UIService#isVisible()
     */
    public boolean isVisible()
    {
        return false;
    }

    /**
     * Implements <code>setVisible</code> in the UIService interface. Shows or
     * hides the main application window depending on the parameter
     * <code>visible</code>.
     *
     * @param isVisible true if we are to show the main application frame and
     * false otherwise.
     *
     * @see UIService#setVisible(boolean)
     */
    public void setVisible(final boolean isVisible)
    {
    }

    /**
     * Locates the main application window to the new x and y coordinates.
     *
     * @param x The new x coordinate.
     * @param y The new y coordinate.
     */
    public void setLocation(int x, int y)
    {
    }

    /**
     * Returns the current location of the main application window. The returned
     * point is the top left corner of the window.
     *
     * @return The top left corner coordinates of the main application window.
     */
    public Point getLocation()
    {
        return null;
    }

    /**
     * Returns the size of the main application window.
     *
     * @return the size of the main application window.
     */
    public Dimension getSize()
    {
        return null;
    }

    /**
     * Sets the size of the main application window.
     *
     * @param width The width of the window.
     * @param height The height of the window.
     */
    public void setSize(int width, int height)
    {
    }

    /**
     * Implements <code>minimize</code> in the UIService interface. Minimizes
     * the main application window.
     *
     * @see UIService#minimize()
     */
    public void minimize()
    {
    }

    /**
     * Implements <code>maximize</code> in the UIService interface. Maximizes
     * the main application window.
     *
     * @see UIService#maximize()
     */
    public void maximize()
    {
    }

    /**
     * Implements <code>restore</code> in the UIService interface. Restores
     * the main application window.
     *
     * @see UIService#restore()
     */
    public void restore()
    {
    }

    /**
     * Implements <code>resize</code> in the UIService interface. Resizes the
     * main application window.
     *
     * @param height the new height of tha main application frame.
     * @param width the new width of the main application window.
     *
     * @see UIService#resize(int, int)
     */
    public void resize(int width, int height)
    {
    }

    /**
     * Implements <code>move</code> in the UIService interface. Moves the main
     * application window to the point with coordinates - x, y.
     *
     * @param x the value of X where the main application frame is to be placed.
     * @param y the value of Y where the main application frame is to be placed.
     *
     * @see UIService#move(int, int)
     */
    public void move(int x, int y)
    {
    }

    /**
     * Brings the focus to the main application window.
     */
    public void bringToFront()
    {
    }

    /**
     * Implements {@link UIService#setExitOnMainWindowClose}. Sets the boolean
     * property which indicates whether the application should be exited when
     * the main application window is closed.
     *
     * @param exitOnMainWindowClose <tt>true</tt> if closing the main
     * application window should also be exiting the application; otherwise,
     * <tt>false</tt>
     */
    public void setExitOnMainWindowClose(boolean exitOnMainWindowClose)
    {
    }

    /**
     * Implements {@link UIService#getExitOnMainWindowClose()}. Gets the boolean
     * property which indicates whether the application should be exited when
     * the main application window is closed.
     *
     * @return determines whether the UI impl would exit the application when
     * the main application window is closed.
     */
    public boolean getExitOnMainWindowClose()
    {
        return false;
    }

    /**
     * Implements <code>getSupportedExportedWindows</code> in the UIService
     * interface. Returns an iterator over a set of all windows exported by
     * this implementation.
     *
     * @return an Iterator over all windows exported by this implementation of
     * the UI service.
     *
     * @see UIService#getSupportedExportedWindows()
     */
    public Iterator<WindowID> getSupportedExportedWindows()
    {
        return new HashMap<WindowID, Object>().keySet().iterator();
    }

    /**
     * Implements the <code>getExportedWindow</code> in the UIService interface.
     * Returns the window corresponding to the given <tt>WindowID</tt>.
     *
     * @param windowID the id of the window we'd like to retrieve.
     * @param params the params to be passed to the returned window.
     * @return a reference to the <tt>ExportedWindow</tt> instance corresponding
     *         to <tt>windowID</tt>.
     * @see UIService#getExportedWindow(WindowID)
     */
    public ExportedWindow getExportedWindow(WindowID windowID, Object[] params)
    {
        return null;
    }

    /**
     * Implements the <code>getExportedWindow</code> in the UIService
     * interface. Returns the window corresponding to the given
     * <tt>WindowID</tt>.
     *
     * @param windowID the id of the window we'd like to retrieve.
     *
     * @return a reference to the <tt>ExportedWindow</tt> instance corresponding
     * to <tt>windowID</tt>.
     * @see UIService#getExportedWindow(WindowID)
     */
    public ExportedWindow getExportedWindow(WindowID windowID)
    {
        return getExportedWindow(windowID, null);
    }

    /**
     * Implements the <code>UIService.isExportedWindowSupported</code> method.
     * Checks if there's an exported component for the given
     * <tt>WindowID</tt>.
     *
     * @param windowID the id of the window that we're making the query for.
     *
     * @return true if a window with the corresponding windowID is exported by
     * the UI service implementation and false otherwise.
     *
     * @see UIService#isExportedWindowSupported(WindowID)
     */
    public boolean isExportedWindowSupported(WindowID windowID)
    {
        return false;
    }

    public WizardContainer getAccountRegWizardContainer()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Implements <code>getPopupDialog</code> in the UIService interface.
     * Returns a <tt>PopupDialog</tt> that could be used to show simple
     * messages, warnings, errors, etc.
     *
     * @return a <tt>PopupDialog</tt> that could be used to show simple
     * messages, warnings, errors, etc.
     *
     * @see UIService#getPopupDialog()
     */
    public PopupDialog getPopupDialog()
    {
        return null;
    }

    /**
     * Implements {@link UIService#getChat(Contact)}. If a chat for the given
     * contact exists already, returns it; otherwise, creates a new one.
     *
     * @param contact the contact that we'd like to retrieve a chat window for.
     * @return the <tt>Chat</tt> corresponding to the specified contact.
     * @see UIService#getChat(Contact)
     */
    public Chat getChat(Contact contact)
    {
        return null;
    }

    /**
     * Returns the <tt>Chat</tt> corresponding to the given <tt>ChatRoom</tt>.
     *
     * @param chatRoom the <tt>ChatRoom</tt> for which the searched chat is
     * about.
     * @return the <tt>Chat</tt> corresponding to the given <tt>ChatRoom</tt>.
     */
    public Chat getChat(ChatRoom chatRoom)
    {
        return null;
    }

    /**
     * Returns the selected <tt>Chat</tt>.
     *
     * @return the selected <tt>Chat</tt>.
     */
    public Chat getCurrentChat()
    {
        return null;
    }

    /**
     * Returns the phone number currently entered in the phone number field.
     *
     * @return the phone number currently entered in the phone number field.
     */
    public String getCurrentPhoneNumber()
    {
        return null;
    }

    /**
     * Changes the phone number currently entered in the phone number field.
     *
     * @param phoneNumber the phone number to enter in the phone number field.
     */
    public void setCurrentPhoneNumber(String phoneNumber)
    {

    }

    /**
     * Implements the <code>UIService.isContainerSupported</code> method.
     * Checks if the plugable container with the given Container is supported
     * by this implementation.
     *
     * @param containderID the id of the container that we're making the query
     * for.
     *
     * @return true if the container with the specified id is exported by the
     * implementation of the UI service and false otherwise.
     *
     */
    public boolean isContainerSupported(Container containderID)
    {
        return false;
    }

    /**
     * Returns a default implementation of the <tt>SecurityAuthority</tt>
     * interface that can be used by non-UI components that would like to launch
     * the registration process for a protocol provider. Initially this method
     * was meant for use by the systray bundle and the protocol URI handlers.
     *
     * @param protocolProvider the <tt>ProtocolProviderService</tt> for which
     * the authentication window is about.
     *
     * @return a default implementation of the <tt>SecurityAuthority</tt>
     * interface that can be used by non-UI components that would like to launch
     * the registration process for a protocol provider.
     */
    public SecurityAuthority getDefaultSecurityAuthority(
                    ProtocolProviderService protocolProvider)
    {
        SecurityAuthority secAuthority = GuiActivator.getSecurityAuthority(
            protocolProvider.getProtocolName());

        if (secAuthority == null)
            secAuthority = GuiActivator.getSecurityAuthority();

        if (secAuthority == null)
            secAuthority = new DefaultSecurityAuthority(protocolProvider);

        return secAuthority;
    }

    /**
     * Implements UIService#useMacOSXScreenMenuBar(). Indicates that the Mac OS
     * X screen menu bar is to be used on Mac OS X and the Windows-like
     * per-window menu bars are to be used on non-Mac OS X operating systems.
     *
     * @return <tt>true</tt> to indicate that MacOSX screen menu bar should be
     * used, <tt>false</tt> - otherwise
     */
    public boolean useMacOSXScreenMenuBar()
    {
        return OSUtils.IS_MAC;
    }

    /**
     * Returns a list containing all open Chats
     *
     * @return  A list of all open Chats.
     */
    public List<Chat> getChats()
    {
        return new ArrayList<Chat>();
    }

    public MetaContact getChatContact(Chat chat)
    {
        return null;
    }

    /**
     * Provides all currently instantiated <tt>Chats</tt>.
     *
     * @return all active <tt>Chats</tt>.
     */
    public Collection <Chat> getAllChats()
    {
        return new ArrayList <Chat> ();
    }

    /**
     * Repaints and revalidates the whole UI Tree.
     *
     * for every window owned by the application which cause UI skin and
     * layout repaint.
     */
    public void repaintUI()
    {
    }

    /**
     * Removes the registration of a <tt>NewChatListener</tt>.
     * @param listener listener to be unregistered
     */
    public void removeChatListener(ChatListener listener)
    {

    }

    /**
     * Shows or hides the "Tools &gt; Settings" configuration window.
     * <p>
     * The method hides the implementation-specific details of the configuration
     * window from its clients and allows the UI to completely control, for
     * example, how many instances of it are visible at one and the same time.
     * <p>
     *
     * @param visible <tt>true</tt> to show the "Tools &gt; Settings"
     *            configuration window; <tt>false</tt> to hide it
     *
     * @deprecated instead use getConfigurationContainer().setVisible(visible)
     */
    public void setConfigurationWindowVisible(boolean visible)
    {}

    public ConfigurationContainer getConfigurationContainer()
    {
        return null;
    }

    public CreateAccountWindow getCreateAccountWindow()
    {
        return null;
    }

    public void addWindowListener(WindowListener l)
    {

    }

    public void removeWindowListener(WindowListener l)
    {

    }

    /**
     * Registers a <tt>NewChatListener</tt> to be informed when new
     * <tt>Chats</tt> are created.
     * @param listener listener to be registered
     */
    public void addChatListener(ChatListener listener)
    {}

    /**
     * Returns the <tt>WizardContainer</tt> for the current UIService
     * implementation. The <tt>WizardContainer</tt> is meant to be implemented
     * by the UI service implementation in order to allow other modules to add
     * to the GUI <tt>AccountRegistrationWizard</tt> s. Each of these wizards is
     * made for a given protocol and should provide a sequence of user interface
     * forms through which the user could register a new account.
     *
     * @return Returns the <tt>AccountRegistrationWizardContainer</tt> for the
     *         current UIService implementation.
     */
//    public WizardContainer getAccountRegWizardContainer()
//    {
//        return null;
//    }

    /**
     * Returns the <tt>ConfigurationContainer</tt> associated with this
     * <tt>UIService</tt>.
     *
     * @return the <tt>ConfigurationContainer</tt> associated with this
     * <tt>UIService</tt>
     */
//    public ConfigurationContainer getConfigurationContainer()
//    {
//        return null;
//    }
    
    /**
     * Returns the create account window.
     *
     * @return the create account window
     */
//    public CreateAccountWindow getCreateAccountWindow()
//    {
//        return null;
//    }

    public void createCall(String[] participants)
    {}

    public void startChat(String[] participants)
    {}

    public ContactList createContactListComponent()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<Call> getInProgressCalls()
    {
        return new ArrayList<Call>();
    }
}