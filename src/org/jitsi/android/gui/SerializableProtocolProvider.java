/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui;

import java.io.*;

import net.java.sip.communicator.service.protocol.*;

public class SerializableProtocolProvider
    implements Serializable
{
    private final ProtocolProviderService provider;

    public SerializableProtocolProvider(ProtocolProviderService provider)
    {
        this.provider = provider;
    }

    public ProtocolProviderService getProtocolProvider()
    {
        return provider;
    }
}
