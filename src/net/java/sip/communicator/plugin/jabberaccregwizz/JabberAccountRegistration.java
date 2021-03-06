/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.jabberaccregwizz;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.util.wizard.*;

/**
 * The <tt>JabberAccountRegistration</tt> is used to store all user input data
 * through the <tt>JabberAccountRegistrationWizard</tt>.
 *
 * @author Yana Stamcheva
 * @author Boris Grozev
 */
public class JabberAccountRegistration
    extends SecurityAccountRegistration
    implements EncodingsRegistration
{
    /**
     * The default value of server port for jabber accounts.
     */
    public static final String DEFAULT_PORT = "5222";

    /**
     * The default value of the priority property.
     */
    public static final String DEFAULT_PRIORITY = "30";

    /**
     * The default value of the resource property.
     */
    public static final String DEFAULT_RESOURCE = "jitsi";

    /**
     * The default value of stun server port for jabber accounts.
     */
    public static final String DEFAULT_STUN_PORT = "3478";

    /**
     * Default value for resource auto generating.
     */
    public static final boolean DEFAULT_RESOURCE_AUTOGEN = true;

    /**
     * The default value for DTMF method.
     */
    private String defaultDTMFMethod = "AUTO_DTMF";

    /**
     * The default value of minimale DTMF tone duration.
     */
    public static String DEFAULT_MINIMAL_DTMF_TONE_DURATION = Integer.toString(
            OperationSetDTMF.DEFAULT_DTMF_MINIMAL_TONE_DURATION);

    /**
     * The user identifier.
     */
    private String userID;

    /**
     * The password.
     */
    private String password;

    /**
     * Indicates if the password should be remembered.
     */
    private boolean rememberPassword = true;

    /**
     * The server address.
     */
    private String serverAddress;

    /**
     * The default domain.
     */
    private String defaultUserSufix;

    /**
     * The override domain for phone call.
     *
     * If Jabber account is able to call PSTN number and if domain name of the
     * switch is different than the domain of the account (gw.domain.org vs
     * domain.org), you can use this property to set the switch domain.
     */
    private String overridePhoneSuffix = null;

    /**
     * Always call with gtalk property.
     *
     * It is used to bypass capabilities checks: some softwares do not advertise
     * GTalk support (but they support it).
     */
    private boolean bypassGtalkCaps = false;

    /**
     * Domain name that will bypass GTalk caps.
     */
    private String domainBypassCaps = null;

    /**
     * Is jingle disabled for this account.
     */
    private boolean disableJingle = false;

    /**
     * The port.
     */
    private int port = new Integer(DEFAULT_PORT).intValue();

    /**
     * The resource property, initialized to the default resource.
     */
    private String resource = DEFAULT_RESOURCE;

    /**
     * The priority property.
     */
    private int priority = new Integer(DEFAULT_PRIORITY).intValue();

    /**
     * Indicates if keep alive packets should be send.
     */
    private boolean sendKeepAlive = true;

    /**
     * Indicates if gmail notifications should be enabled.
     */
    private boolean enableGmailNotification = false;

    /**
     * Indicates if Google Contacts should be enabled.
     */
    private boolean enableGoogleContacts = false;

    /**
     * Indicates if ICE should be used.
     */
    private boolean isUseIce = false;

    /**
     * Indicates if Google ICE should be used.
     */
    private boolean isUseGoogleIce = false;

    /**
     * Indicates if STUN server should be automatically discovered.
     */
    private boolean isAutoDiscoverStun = false;

    /**
     * Indicates if default STUN server should be used.
     */
    private boolean isUseDefaultStunServer = false;

    /**
     * The list of additional STUN servers entered by user.
     */
    private List<StunServerDescriptor> additionalStunServers
        = new ArrayList<StunServerDescriptor>();

    /**
     * Indicates if JingleNodes relays should be used.
     */
    private boolean isUseJingleNodes = false;

    /**
     * Indicates if JingleNodes relay server should be automatically discovered.
     */
    private boolean isAutoDiscoverJingleNodes = false;

    /**
     * The list of additional JingleNodes (tracker or relay) entered by user.
     */
    private List<JingleNodeDescriptor> additionalJingleNodes
        = new ArrayList<JingleNodeDescriptor>();

    /**
     * Indicates if UPnP should be used.
     */
    private boolean isUseUPNP = false;

    /**
     * If non-TLS connection is allowed.
     */
    private boolean isAllowNonSecure = false;

    /**
     * Indicates if the server is overriden.
     */
    private boolean isServerOverridden = false;

    /**
     * Is resource auto generate enabled.
     */
    private boolean resourceAutogenerated = DEFAULT_RESOURCE_AUTOGEN;

    /**
     * The account display name.
     */
    private String accountDisplayName;

    /**
     * The sms default server.
     */
    private String smsServerAddress;

    /**
     * DTMF method.
     */
    private String dtmfMethod = null;

    /**
     * The minimal DTMF tone duration set.
     */
    private String dtmfMinimalToneDuration = DEFAULT_MINIMAL_DTMF_TONE_DURATION;

    /**
     * The client TLS certificate ID.
     */
    private String clientCertificateId = null;

    /**
     * Initializes a new JabberAccountRegistration.
     */
    public JabberAccountRegistration()
    {
        super();
    }

     /**
      *  Whether to override global encoding settings.
      */
	 private boolean overrideEncodingSettings = false;
	 /**
	  * Encoding properties associated with this account.
	  */
    private Map<String, String> encodingProperties
	    = new HashMap<String, String>();


    /**
     * Returns the password of the jabber registration account.
     * @return the password of the jabber registration account.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the password of the jabber registration account.
     * @param password the password of the jabber registration account.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Returns TRUE if password has to remembered, FALSE otherwise.
     * @return TRUE if password has to remembered, FALSE otherwise
     */
    public boolean isRememberPassword()
    {
        return rememberPassword;
    }

    /**
     * Sets the rememberPassword value of this jabber account registration.
     * @param rememberPassword TRUE if password has to remembered, FALSE
     * otherwise
     */
    public void setRememberPassword(boolean rememberPassword)
    {
        this.rememberPassword = rememberPassword;
    }

    /**
     * Returns the User ID of the jabber registration account.
     * @return the User ID of the jabber registration account.
     */
    public String getUserID()
    {
        return userID;
    }

    /**
     * Returns the user sufix.
     *
     * @return the user sufix
     */
    public String getDefaultUserSufix()
    {
        return defaultUserSufix;
    }

    /**
     * Returns the override phone suffix.
     *
     * @return the phone suffix
     */
    public String getOverridePhoneSuffix()
    {
        return overridePhoneSuffix;
    }

    /**
     * Returns the alwaysCallWithGtalk value.
     *
     * @return the alwaysCallWithGtalk value
     */
    public boolean getBypassGtalkCaps()
    {
        return bypassGtalkCaps;
    }

    /**
     * Returns telephony domain that bypass GTalk caps.
     *
     * @return telephony domain
     */
    public String getTelephonyDomainBypassCaps()
    {
        return domainBypassCaps;
    }

    /**
     * Gets if Jingle is disabled for this account.
     *
     * @return True if jingle is disabled for this account. False otherwise.
     */
    public boolean isJingleDisabled()
    {
        return this.disableJingle;
    }

    /**
     * The address of the server we will use for this account
     * @return String
     */
    public String getServerAddress()
    {
        return serverAddress;
    }

    /**
     * The port on the specified server
     * @return the server port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Determines whether sending of keep alive packets is enabled.
     *
     * @return <tt>true</tt> if keep alive packets are to be sent for this
     * account and <tt>false</tt> otherwise.
     */
    public boolean isSendKeepAlive()
    {
        return sendKeepAlive;
    }

    /**
     * Determines whether SIP Communicator should be querying Gmail servers
     * for unread mail messages.
     *
     * @return <tt>true</tt> if we are to enable Gmail notifications and
     * <tt>false</tt> otherwise.
     */
    public boolean isGmailNotificationEnabled()
    {
        return enableGmailNotification;
    }

    /**
     * Determines whether SIP Communicator should use Google Contacts as
     * ContactSource
     *
     * @return <tt>true</tt> if we are to enable Google Contacts and
     * <tt>false</tt> otherwise.
     */
    public boolean isGoogleContactsEnabled()
    {
        return enableGoogleContacts;
    }

    /**
     * Sets the User ID of the jabber registration account.
     *
     * @param userID the identifier of the jabber registration account.
     */
    public void setUserID(String userID)
    {
        this.userID = userID;
    }

    /**
     * Sets the default value of the user sufix.
     *
     * @param userSufix the user name sufix (the domain name after the @ sign)
     */
    public void setDefaultUserSufix(String userSufix)
    {
        this.defaultUserSufix = userSufix;
    }

    /**
     * Sets the override value of the phone suffix.
     *
     * @param phoneSuffix the phone name suffix (the domain name after the @
     * sign)
     */
    public void setOverridePhoneSufix(String phoneSuffix)
    {
        this.overridePhoneSuffix = phoneSuffix;
    }

    /**
     * Sets value for alwaysCallWithGtalk.
     *
     * @param bypassGtalkCaps true to enable, false otherwise
     */
    public void setBypassGtalkCaps(boolean bypassGtalkCaps)
    {
        this.bypassGtalkCaps = bypassGtalkCaps;
    }

    /**
     * Sets telephony domain that bypass GTalk caps.
     *
     * @param text telephony domain to set
     */
    public void setTelephonyDomainBypassCaps(String text)
    {
        this.domainBypassCaps = text;
    }

    /**
     * Sets if Jingle is disabled for this account.
     *
     * @param True if jingle is disabled for this account. False otherwise.
     */
    public void setDisableJingle(boolean disabled)
    {
        this.disableJingle = disabled;
    }

    /**
     * Sets the server
     *
     * @param serverAddress the IP address or FQDN of the server.
     */
    public void setServerAddress(String serverAddress)
    {
        this.serverAddress = serverAddress;
    }

    /**
     * Indicates if the server address has been overridden.
     *
     * @return <tt>true</tt> if the server address has been overridden,
     * <tt>false</tt> - otherwise.
     */
    public boolean isServerOverridden()
    {
        return isServerOverridden;
    }

    /**
     * Sets <tt>isServerOverridden</tt> property.
     * @param isServerOverridden indicates if the server is overridden
     */
    public void setServerOverridden(boolean isServerOverridden)
    {
        this.isServerOverridden = isServerOverridden;
    }

    /**
     * Sets the server port number.
     *
     * @param port the server port number
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * Specifies whether SIP Communicator should send send keep alive packets
     * to keep this account registered.
     *
     * @param sendKeepAlive <tt>true</tt> if we are to send keep alive packets
     * and <tt>false</tt> otherwise.
     */
    public void setSendKeepAlive(boolean sendKeepAlive)
    {
        this.sendKeepAlive = sendKeepAlive;
    }

    /**
     * Specifies whether SIP Communicator should be querying Gmail servers
     * for unread mail messages.
     *
     * @param enabled <tt>true</tt> if we are to enable Gmail notification and
     * <tt>false</tt> otherwise.
     */
    public void setGmailNotificationEnabled(boolean enabled)
    {
        this.enableGmailNotification = enabled;
    }

    /**
     * Specifies whether SIP Communicator should use Google Contacts as
     * ContactSource.
     *
     * @param enabled <tt>true</tt> if we are to enable Google Contacts and
     * <tt>false</tt> otherwise.
     */
    public void setGoogleContactsEnabled(boolean enabled)
    {
        this.enableGoogleContacts = enabled;
    }

    /**
     * Returns the resource.
     * @return the resource
     */
    public String getResource()
    {
        return resource;
    }

    /**
     * Sets the resource.
     * @param resource the resource for the jabber account
     */
    public void setResource(String resource)
    {
        this.resource = resource;
    }

    /**
     * Returns the priority property.
     * @return priority
     */
    public int getPriority()
    {
        return priority;
    }

    /**
     * Sets the priority property.
     * @param priority the priority to set
     */
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    /**
     * Indicates if ice should be used for this account.
     * @return <tt>true</tt> if ICE should be used for this account, otherwise
     * returns <tt>false</tt>
     */
    public boolean isUseIce()
    {
        return isUseIce;
    }

    /**
     * Sets the <tt>useIce</tt> property.
     * @param isUseIce <tt>true</tt> to indicate that ICE should be used for
     * this account, <tt>false</tt> - otherwise.
     */
    public void setUseIce(boolean isUseIce)
    {
        this.isUseIce = isUseIce;
    }

    /**
     * Indicates if ice should be used for this account.
     * @return <tt>true</tt> if ICE should be used for this account, otherwise
     * returns <tt>false</tt>
     */
    public boolean isUseGoogleIce()
    {
        return isUseGoogleIce;
    }

    /**
     * Sets the <tt>useGoogleIce</tt> property.
     * @param isUseIce <tt>true</tt> to indicate that ICE should be used for
     * this account, <tt>false</tt> - otherwise.
     */
    public void setUseGoogleIce(boolean isUseIce)
    {
        this.isUseGoogleIce = isUseIce;
    }

    /**
     * Indicates if the stun server should be automatically discovered.
     * @return <tt>true</tt> if the stun server should be automatically
     * discovered, otherwise returns <tt>false</tt>.
     */
    public boolean isAutoDiscoverStun()
    {
        return isAutoDiscoverStun;
    }

    /**
     * Sets the <tt>autoDiscoverStun</tt> property.
     * @param isAutoDiscover <tt>true</tt> to indicate that stun server should
     * be auto-discovered, <tt>false</tt> - otherwise.
     */
    public void setAutoDiscoverStun(boolean isAutoDiscover)
    {
        this.isAutoDiscoverStun = isAutoDiscover;
    }

    /**
     * Indicates if the stun server should be automatically discovered.
     * @return <tt>true</tt> if the stun server should be automatically
     * discovered, otherwise returns <tt>false</tt>.
     */
    public boolean isUseDefaultStunServer()
    {
        return isUseDefaultStunServer;
    }

    /**
     * Sets the <tt>useDefaultStunServer</tt> property.
     * @param isUseDefaultStunServer <tt>true</tt> to indicate that default
     * stun server should be used if no others are available, <tt>false</tt>
     * otherwise.
     */
    public void setUseDefaultStunServer(boolean isUseDefaultStunServer)
    {
        this.isUseDefaultStunServer = isUseDefaultStunServer;
    }

    /**
     * Adds the given <tt>stunServer</tt> to the list of additional stun servers.
     *
     * @param stunServer the <tt>StunServer</tt> to add
     */
    public void addStunServer(StunServerDescriptor stunServer)
    {
        additionalStunServers.add(stunServer);
    }

    /**
     * Returns the <tt>List</tt> of all additional stun servers entered by the
     * user. The list is guaranteed not to be <tt>null</tt>.
     *
     * @return the <tt>List</tt> of all additional stun servers entered by the
     * user.
     */
    public List<StunServerDescriptor> getAdditionalStunServers()
    {
        return additionalStunServers;
    }

    /**
     * Sets the <tt>autoDiscoverJingleNodes</tt> property.
     *
     * @param isAutoDiscover <tt>true</tt> to indicate that relay server should
     * be auto-discovered, <tt>false</tt> - otherwise.
     */
    public void setAutoDiscoverJingleNodes(boolean isAutoDiscover)
    {
        this.isAutoDiscoverJingleNodes = isAutoDiscover;
    }

    /**
     * Indicates if the JingleNodes relay server should be automatically
     * discovered.
     *
     * @return <tt>true</tt> if the relay server should be automatically
     * discovered, otherwise returns <tt>false</tt>.
     */
    public boolean isAutoDiscoverJingleNodes()
    {
        return isAutoDiscoverJingleNodes;
    }

    /**
     * Sets the <tt>useJingleNodes</tt> property.
     *
     * @param isUseJingleNodes <tt>true</tt> to indicate that Jingle Nodes
     * should be used for this account, <tt>false</tt> - otherwise.
     */
    public void setUseJingleNodes(boolean isUseJingleNodes)
    {
        this.isUseJingleNodes = isUseJingleNodes;
    }

    /**
     * Sets the <tt>useJingleNodes</tt> property.
     *
     * @param isUseJingleNodes <tt>true</tt> to indicate that JingleNodes relays
     * should be used for this account, <tt>false</tt> - otherwise.
     */
    public void isUseJingleNodes(boolean isUseJingleNodes)
    {
        this.isUseJingleNodes = isUseJingleNodes;
    }

    /**
     * Indicates if JingleNodes relay should be used.
     *
     * @return <tt>true</tt> if JingleNodes should be used, <tt>false</tt>
     * otherwise
     */
    public boolean isUseJingleNodes()
    {
        return isUseJingleNodes;
    }

    /**
     * Adds the given <tt>node</tt> to the list of additional JingleNodes.
     *
     * @param node the <tt>node</tt> to add
     */
    public void addJingleNodes(JingleNodeDescriptor node)
    {
        additionalJingleNodes.add(node);
    }

    /**
     * Returns the <tt>List</tt> of all additional stun servers entered by the
     * user. The list is guaranteed not to be <tt>null</tt>.
     *
     * @return the <tt>List</tt> of all additional stun servers entered by the
     * user.
     */
    public List<JingleNodeDescriptor> getAdditionalJingleNodes()
    {
        return additionalJingleNodes;
    }

    /**
     * Indicates if UPnP should be used for this account.
     * @return <tt>true</tt> if UPnP should be used for this account, otherwise
     * returns <tt>false</tt>
     */
    public boolean isUseUPNP()
    {
        return isUseUPNP;
    }

    /**
     * Sets the <tt>useUPNP</tt> property.
     * @param isUseUPNP <tt>true</tt> to indicate that UPnP should be used for
     * this account, <tt>false</tt> - otherwise.
     */
    public void setUseUPNP(boolean isUseUPNP)
    {
        this.isUseUPNP = isUseUPNP;
    }

    /**
     * Indicates if non-TLS is allowed for this account
     * @return <tt>true</tt> if non-TLS is allowed for this account, otherwise
     * returns <tt>false</tt>
     */
    public boolean isAllowNonSecure()
    {
        return isAllowNonSecure;
    }

    /**
     * Sets the <tt>isAllowNonSecure</tt> property.
     * @param isAllowNonSecure <tt>true</tt> to indicate that non-TLS is allowed
     * for this account, <tt>false</tt> - otherwise.
     */
    public void setAllowNonSecure(boolean isAllowNonSecure)
    {
        this.isAllowNonSecure = isAllowNonSecure;
    }

    /**
     * Is resource auto generate enabled.
     *
     * @return true if resource is auto generated
     */
    public boolean isResourceAutogenerated()
    {
        return resourceAutogenerated;
    }

    /**
     * Set whether resource autogenerate is enabled.
     * @param resourceAutogenerated
     */
    public void setResourceAutogenerated(boolean resourceAutogenerated)
    {
        this.resourceAutogenerated = resourceAutogenerated;
    }

    /**
     * Returns the account display name.
     *
     * @return the account display name
     */
    public String getAccountDisplayName()
    {
        return accountDisplayName;
    }

    /**
     * Sets the account display name.
     *
     * @param accountDisplayName the account display name
     */
    public void setAccountDisplayName(String accountDisplayName)
    {
        this.accountDisplayName = accountDisplayName;
    }

    /**
     * Returns the default sms server.
     *
     * @return the account default sms server
     */
    public String getSmsServerAddress()
    {
        return smsServerAddress;
    }

    /**
     * Sets the default sms server.
     *
     * @param serverAddress the sms server to set as default
     */
    public void setSmsServerAddress(String serverAddress)
    {
        this.smsServerAddress = serverAddress;
    }

    /**
     * Returns the DTMF method.
     *
     * @return the DTMF method.
     */
    public String getDTMFMethod()
    {
        return dtmfMethod;
    }

    /**
     * Sets the DTMF method.
     *
     * @param dtmfMethod the DTMF method to set
     */
    public void setDTMFMethod(String dtmfMethod)
    {
        this.dtmfMethod = dtmfMethod;
    }

    /**
     * @return the defaultDTMFMethod
     */
    public String getDefaultDTMFMethod()
    {
        return defaultDTMFMethod;
    }

    /**
     * @param defaultDTMFMethod the defaultDTMFMethod to set
     */
    public void setDefaultDTMFMethod(String defaultDTMFMethod)
    {
        this.defaultDTMFMethod = defaultDTMFMethod;
    }

    /**
     * Returns the minimal DTMF tone duration.
     *
     * @return The minimal DTMF tone duration.
     */
    public String getDtmfMinimalToneDuration()
    {
        return dtmfMinimalToneDuration;
    }

    /**
     * Sets the minimal DTMF tone duration.
     *
     * @param dtmfMinimalToneDuration The minimal DTMF tone duration to set.
     */
    public void setDtmfMinimalToneDuration(String dtmfMinimalToneDuration)
    {
        this.dtmfMinimalToneDuration = dtmfMinimalToneDuration;
    }

    /**
     * Sets the method used for RTP/SAVP indication.
     */
    public void setSavpOption(int savpOption)
    {
        // SAVP option is not useful for XMPP account.
        // Thereby, do nothing.
    }

    /**
    * Whether override encodings is enabled
    * @return Whether override encodings is enabled
    */
    public boolean isOverrideEncodings()
    {
        return overrideEncodingSettings;
    }
    
    /**
    * Set the override encodings setting to <tt>override</tt>
    * @param override The value to set the override ecoding settings to.
    */
    public void setOverrideEncodings(boolean override)
    {
        overrideEncodingSettings = override;
    }
    
    /**
    * Get the stored encoding properties
    * @return The stored encoding properties.
    */
    public Map<String, String> getEncodingProperties()
    {
        return encodingProperties;
    }
    
    /**
    * Set the encoding properties
    * @param encodingProperties The encoding properties to set.
    */
    public void setEncodingProperties(Map<String, String> encodingProperties)
    {
        this.encodingProperties = encodingProperties;
    }

    /**
     * Sets the client certificate configuration entry ID.
     * @param clientCertificateId the client certificate configuration entry ID.
     */
    public void setClientCertificateId(String clientCertificateId)
    {
        this.clientCertificateId = clientCertificateId;
    }

    /**
     * Gets the client certificate configuration entry ID.
     * @returns the client certificate configuration entry ID.
     */
    public String getClientCertificateId()
    {
        return clientCertificateId;
    }
}
