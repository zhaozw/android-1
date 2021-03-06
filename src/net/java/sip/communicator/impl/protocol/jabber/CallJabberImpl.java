/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber;

import java.lang.ref.*;
import java.util.*;

import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.jinglesdp.*;
import net.java.sip.communicator.service.protocol.*;
import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.service.protocol.media.*;
import net.java.sip.communicator.util.*;

import org.jitsi.service.neomedia.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.packet.*;

/**
 * A Jabber implementation of the <tt>Call</tt> abstract class encapsulating
 * Jabber jingle sessions.
 *
 * @author Emil Ivov
 * @author Lyubomir Marinov
 */
public class CallJabberImpl
    extends AbstractCallJabberGTalkImpl<CallPeerJabberImpl>
{
    /**
     * The <tt>Logger</tt> used by the <tt>CallJabberImpl</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger = Logger.getLogger(CallJabberImpl.class);

    /**
     * The Jitsi VideoBridge conference which the local peer represented by this
     * instance is a focus of.
     */
    private ColibriConferenceIQ colibri;

    /**
     * The shared <tt>CallPeerMediaHandler</tt> state which is to be used by the
     * <tt>CallPeer</tt>s of this <tt>Call</tt> which use {@link #colibri}.
     */
    private MediaHandler colibriMediaHandler;

    /**
     * Contains one ColibriStreamConnector for each <tt>MediaType</tt>
     */
    private final List<WeakReference<ColibriStreamConnector>>
        colibriStreamConnectors;

    /**
     * Initializes a new <tt>CallJabberImpl</tt> instance.
     *
     * @param parentOpSet the {@link OperationSetBasicTelephonyJabberImpl}
     * instance in the context of which this call has been created.
     */
    protected CallJabberImpl(
            OperationSetBasicTelephonyJabberImpl parentOpSet)
    {
        super(parentOpSet);

        int mediaTypeValueCount = MediaType.values().length;

        colibriStreamConnectors
            = new ArrayList<WeakReference<ColibriStreamConnector>>(
                    mediaTypeValueCount);
        for (int i = 0; i < mediaTypeValueCount; i++)
            colibriStreamConnectors.add(null);

        //let's add ourselves to the calls repo. we are doing it ourselves just
        //to make sure that no one ever forgets.
        parentOpSet.getActiveCallsRepository().addCall(this);
    }

    /**
     * Closes a specific <tt>ColibriStreamConnector</tt> which is associated with
     * a <tt>MediaStream</tt> of a specific <tt>MediaType</tt> upon request from
     * a specific <tt>CallPeer</tt>.
     * 
     * @param peer the <tt>CallPeer</tt> which requests the closing of the
     * specified <tt>colibriStreamConnector</tt>
     * @param mediaType the <tt>MediaType</tt> of the <tt>MediaStream</tt> with
     * which the specified <tt>colibriStreamConnector</tt> is associated
     * @param colibriStreamConnector the <tt>ColibriStreamConnector</tt> to close on
     * behalf of the specified <tt>peer</tt>
     */
    public void closeColibriStreamConnector(
            CallPeerJabberImpl peer,
            MediaType mediaType,
            ColibriStreamConnector colibriStreamConnector)
    {
        colibriStreamConnector.close();
    }

    /**
     * {@inheritDoc}
     *
     * Sends a <tt>content</tt> message to each of the <tt>CallPeer</tt>s
     * associated with this <tt>CallJabberImpl</tt> in order to include/exclude
     * the &quot;isfocus&quot; attribute. 
     */
    @Override
    protected void conferenceFocusChanged(boolean oldValue, boolean newValue)
    {
        try
        {
            Iterator<CallPeerJabberImpl> peers = getCallPeers();

            while (peers.hasNext())
            {
                CallPeerJabberImpl callPeer = peers.next();

                if (callPeer.getState() == CallPeerState.CONNECTED)
                    callPeer.sendCoinSessionInfo();
            }
        }
        finally
        {
            super.conferenceFocusChanged(oldValue, newValue);
        }
    }

    /**
     * Allocates colibri (conference) channels for a specific <tt>MediaType</tt>
     * to be used by a specific <tt>CallPeer</tt>.
     *
     * @param peer the <tt>CallPeer</tt> which is to use the allocated colibri
     * (conference) channels
     * @param rdpes the <tt>RtpDescriptionPacketExtension</tt>s which specify
     * the <tt>MediaType</tt>s for which colibri (conference) channels are to be
     * allocated
     * @return a <tt>ColibriConferenceIQ</tt> which describes the allocated colibri
     * (conference) channels for the specified <tt>mediaTypes</tt> which are to
     * be used by the specified <tt>peer</tt>; otherwise, <tt>null</tt>
     */
    public ColibriConferenceIQ createColibriChannels(
            CallPeerJabberImpl peer,
            Iterable<RtpDescriptionPacketExtension> rdpes)
    {
        if (!getConference().isJitsiVideoBridge())
            return null;

        /*
         * For a colibri conference to work properly, all CallPeers in the
         * conference must share one and the same CallPeerMediaHandler state
         * i.e. they must use a single set of MediaStreams as if there was a
         * single CallPeerMediaHandler.
         */
        CallPeerMediaHandler<?> peerMediaHandler = peer.getMediaHandler();

        if (peerMediaHandler.getMediaHandler() != colibriMediaHandler)
        {
            for (MediaType mediaType : MediaType.values())
                if (peerMediaHandler.getStream(mediaType) != null)
                    return null;
        }

        ProtocolProviderServiceJabberImpl protocolProvider
            = getProtocolProvider();
        String jitsiVideoBridge
            = (colibri == null)
                ? protocolProvider.getJitsiVideoBridge()
                : colibri.getFrom();

        if ((jitsiVideoBridge == null) || (jitsiVideoBridge.length() == 0))
        {
            if (logger.isInfoEnabled())
                logger.info("Failed to allocate colibri channels: no " +
                        " videobridge found.");
            return null;
        }

        ColibriConferenceIQ conferenceRequest = new ColibriConferenceIQ();

        if (colibri != null)
            conferenceRequest.setID(colibri.getID());

        for (RtpDescriptionPacketExtension rdpe : rdpes)
        {
            MediaType mediaType = MediaType.parseString(rdpe.getMedia());
            String contentName = mediaType.toString();
            ColibriConferenceIQ.Content contentRequest
                = new ColibriConferenceIQ.Content(contentName);

            conferenceRequest.addContent(contentRequest);

            boolean requestLocalChannel = true;

            if (colibri != null)
            {
                ColibriConferenceIQ.Content content
                    = colibri.getContent(contentName);

                if ((content != null) && (content.getChannelCount() > 0))
                    requestLocalChannel = false;
            }
            if (requestLocalChannel)
            {
                ColibriConferenceIQ.Channel localChannelRequest
                    = new ColibriConferenceIQ.Channel();

                for (PayloadTypePacketExtension ptpe : rdpe.getPayloadTypes())
                    localChannelRequest.addPayloadType(ptpe);
                contentRequest.addChannel(localChannelRequest);
            }

            ColibriConferenceIQ.Channel remoteChannelRequest
                = new ColibriConferenceIQ.Channel();

            for (PayloadTypePacketExtension ptpe : rdpe.getPayloadTypes())
                remoteChannelRequest.addPayloadType(ptpe);
            contentRequest.addChannel(remoteChannelRequest);
        }

        XMPPConnection connection = protocolProvider.getConnection();
        PacketCollector packetCollector
            = connection.createPacketCollector(
                    new PacketIDFilter(conferenceRequest.getPacketID()));

        conferenceRequest.setTo(jitsiVideoBridge);
        conferenceRequest.setType(IQ.Type.GET);
        connection.sendPacket(conferenceRequest);

        Packet response
            = packetCollector.nextResult(
                    SmackConfiguration.getPacketReplyTimeout());

        packetCollector.cancel();

        if (response == null)
        {
            if (logger.isInfoEnabled())
                logger.info("Failed to allocate colibri channels: response " +
                        "is null. Maybe the response timeouted.");
            return null;
        }
        else if (response.getError() != null)
        {
            if (logger.isInfoEnabled())
                logger.info("Failed to allocate colibri channels: " +
                        response.getError());
            return null;
        }
        else if (!(response instanceof ColibriConferenceIQ))
        {
            if (logger.isInfoEnabled())
                logger.info("Failed to allocate colibri channels: response is" +
                        "not a colibri conference");
            return null;
        }

        ColibriConferenceIQ conferenceResponse = (ColibriConferenceIQ) response;
        String conferenceResponseID = conferenceResponse.getID();

        /*
         * Update the complete ColibriConferenceIQ representation maintained by
         * this instance with the information given by the (current) response.
         */
        if (colibri == null)
        {
            colibri = conferenceResponse;
        }
        else
        {
            String colibriID = colibri.getID();

            if (colibriID == null)
                colibri.setID(conferenceResponseID);
            else if (!colibriID.equals(conferenceResponseID))
                throw new IllegalStateException("conference.id");

            for (ColibriConferenceIQ.Content contentResponse
                    : conferenceResponse.getContents())
            {
                ColibriConferenceIQ.Content content
                    = colibri.getOrCreateContent(contentResponse.getName());

                for (ColibriConferenceIQ.Channel channelResponse
                        : contentResponse.getChannels())
                    content.addChannel(channelResponse);
            }
        }

        /*
         * Formulate the result to be returned to the caller which
         * is a subset of the whole conference information kept by
         * this CallJabberImpl and includes the remote channels
         * explicitly requested by the method caller and their
         * respective local channels.
         */
        ColibriConferenceIQ conferenceResult = new ColibriConferenceIQ();

        conferenceResult.setID(conferenceResponseID);

        for (RtpDescriptionPacketExtension rdpe : rdpes)
        {
            MediaType mediaType = MediaType.parseString(rdpe.getMedia());
            ColibriConferenceIQ.Content contentResponse
                = conferenceResponse.getContent(mediaType.toString());

            if (contentResponse != null)
            {
                String contentName = contentResponse.getName();
                ColibriConferenceIQ.Content contentResult
                    = new ColibriConferenceIQ.Content(contentName);

                conferenceResult.addContent(contentResult);

                /*
                 * The local channel may have been allocated in a previous
                 * method call as part of the allocation of the first remote
                 * channel in the respective content. Anyway, the current method
                 * caller still needs to know about it.
                 */
                ColibriConferenceIQ.Content content
                    = colibri.getContent(contentName);
                ColibriConferenceIQ.Channel localChannel = null;

                if ((content != null) && (content.getChannelCount() > 0))
                {
                    localChannel = content.getChannel(0);
                    contentResult.addChannel(localChannel);
                }

                String localChannelID
                    = (localChannel == null) ? null : localChannel.getID();

                for (ColibriConferenceIQ.Channel channelResponse
                        : contentResponse.getChannels())
                {
                    if ((localChannelID == null)
                            || !localChannelID.equals(channelResponse.getID()))
                        contentResult.addChannel(channelResponse);
                }
            }
        }

        /*
         * The specified CallPeer will participate in the colibri conference
         * organized by this Call so it must use the shared CallPeerMediaHandler
         * state of all CallPeers in the same colibri conference.
         */
        if (colibriMediaHandler == null)
            colibriMediaHandler = new MediaHandler();
        peerMediaHandler.setMediaHandler(colibriMediaHandler);

        return conferenceResult;
    }

    /**
     * Initializes a <tt>ColibriStreamConnector</tt> on behalf of a specific
     * <tt>CallPeer</tt> to be used in association with a specific
     * <tt>ColibriConferenceIQ.Channel</tt> of a specific <tt>MediaType</tt>.
     *
     * @param peer the <tt>CallPeer</tt> which requests the initialization of a
     * <tt>ColibriStreamConnector</tt>
     * @param mediaType the <tt>MediaType</tt> of the stream which is to use the
     * initialized <tt>ColibriStreamConnector</tt> for RTP and RTCP traffic
     * @param channel the <tt>ColibriConferenceIQ.Channel</tt> to which RTP and
     * RTCP traffic is to be sent and from which such traffic is to be received
     * via the initialized <tt>ColibriStreamConnector</tt>
     * @param factory a <tt>StreamConnectorFactory</tt> implementation which is
     * to allocate the sockets to be used for RTP and RTCP traffic
     * @return a <tt>ColibriStreamConnector</tt> to be used for RTP and RTCP
     * traffic associated with the specified <tt>channel</tt>
     */
    public ColibriStreamConnector createColibriStreamConnector(
                CallPeerJabberImpl peer,
                MediaType mediaType,
                ColibriConferenceIQ.Channel channel,
                StreamConnectorFactory factory)
    {
        String channelID = channel.getID();

        if (channelID == null)
            throw new IllegalArgumentException("channel");

        if (colibri == null)
            throw new IllegalStateException("colibri");

        ColibriConferenceIQ.Content content
            = colibri.getContent(mediaType.toString());

        if (content == null)
            throw new IllegalArgumentException("mediaType");
        if ((content.getChannelCount() < 1)
                || !channelID.equals((channel = content.getChannel(0)).getID()))
            throw new IllegalArgumentException("channel");

        ColibriStreamConnector colibriStreamConnector;

        synchronized (colibriStreamConnectors)
        {
            int index = mediaType.ordinal();
            WeakReference<ColibriStreamConnector> weakReference
                = colibriStreamConnectors.get(index);

            colibriStreamConnector
                = (weakReference == null) ? null : weakReference.get();
            if (colibriStreamConnector == null)
            {
                StreamConnector streamConnector
                    = factory.createStreamConnector();

                if (streamConnector != null)
                {
                    colibriStreamConnector
                        = new ColibriStreamConnector(streamConnector);
                    colibriStreamConnectors.set(
                        index,
                        new WeakReference<ColibriStreamConnector>(
                            colibriStreamConnector));
                }
            }
        }

        return colibriStreamConnector;
    }

    /**
     * Expires specific (colibri) conference channels used by a specific
     * <tt>CallPeer</tt>.
     *
     * @param peer the <tt>CallPeer</tt> which uses the specified (colibri)
     * conference channels to be expired
     * @param conference a <tt>ColibriConferenceIQ</tt> which specifies the
     * (colibri) conference channels to be expired
     */
    public void expireColibriChannels(
            CallPeerJabberImpl peer,
            ColibriConferenceIQ conference)
    {
        // Formulate the ColibriConferenceIQ request which is to be sent.
        if (colibri != null)
        {
            String conferenceID = colibri.getID();

            if (conferenceID.equals(conference.getID()))
            {
                ColibriConferenceIQ conferenceRequest = new ColibriConferenceIQ();

                conferenceRequest.setID(conferenceID);

                for (ColibriConferenceIQ.Content content
                        : conference.getContents())
                {
                    ColibriConferenceIQ.Content colibriContent
                        = colibri.getContent(content.getName());

                    if (colibriContent != null)
                    {
                        ColibriConferenceIQ.Content contentRequest
                            = conferenceRequest.getOrCreateContent(
                            colibriContent.getName());

                        for (ColibriConferenceIQ.Channel channel
                                : content.getChannels())
                        {
                            ColibriConferenceIQ.Channel colibriChannel
                                = colibriContent.getChannel(channel.getID());

                            if (colibriChannel != null)
                            {
                                ColibriConferenceIQ.Channel channelRequest
                                    = new ColibriConferenceIQ.Channel();

                                channelRequest.setExpire(0);
                                channelRequest.setID(colibriChannel.getID());
                                contentRequest.addChannel(channelRequest);
                            }
                        }
                    }
                }

                /*
                 * Remove the channels which are to be expired from the internal
                 * state of the conference managed by this CallJabberImpl.
                 */
                for (ColibriConferenceIQ.Content contentRequest
                        : conferenceRequest.getContents())
                {
                    ColibriConferenceIQ.Content colibriContent
                        = colibri.getContent(contentRequest.getName());

                    for (ColibriConferenceIQ.Channel channelRequest
                            : contentRequest.getChannels())
                    {
                        ColibriConferenceIQ.Channel colibriChannel
                            = colibriContent.getChannel(channelRequest.getID());

                        colibriContent.removeChannel(colibriChannel);

                        /*
                         * If the last remote channel is to be expired, expire
                         * the local channel as well.
                         */
                        if (colibriContent.getChannelCount() == 1)
                        {
                            colibriChannel = colibriContent.getChannel(0);

                            channelRequest = new ColibriConferenceIQ.Channel();
                            channelRequest.setExpire(0);
                            channelRequest.setID(colibriChannel.getID());
                            contentRequest.addChannel(channelRequest);

                            colibriContent.removeChannel(colibriChannel);

                            break;
                        }
                    }
                }

                /*
                 * At long last, send the ColibriConferenceIQ request to expire
                 * the channels.
                 */
                conferenceRequest.setTo(colibri.getFrom());
                conferenceRequest.setType(IQ.Type.SET);
                getProtocolProvider().getConnection().sendPacket(
                        conferenceRequest);
            }
        }
    }

    /**
     * Creates a <tt>CallPeerJabberImpl</tt> from <tt>calleeJID</tt> and sends
     * them <tt>session-initiate</tt> IQ request.
     *
     * @param calleeJID the party that we would like to invite to this call.
     * @param discoverInfo any discovery information that we have for the jid
     * we are trying to reach and that we are passing in order to avoid having
     * to ask for it again.
     * @param sessionInitiateExtensions a collection of additional and optional
     * <tt>PacketExtension</tt>s to be added to the <tt>session-initiate</tt>
     * {@link JingleIQ} which is to init this <tt>CallJabberImpl</tt>
     *
     * @return the newly created <tt>CallPeerJabberImpl</tt> corresponding to
     * <tt>calleeJID</tt>. All following state change events will be
     * delivered through this call peer.
     *
     * @throws OperationFailedException with the corresponding code if we fail
     *  to create the call.
     */
    public CallPeerJabberImpl initiateSession(
            String calleeJID,
            DiscoverInfo discoverInfo,
            Iterable<PacketExtension> sessionInitiateExtensions)
        throws OperationFailedException
    {
        // create the session-initiate IQ
        CallPeerJabberImpl callPeer = new CallPeerJabberImpl(calleeJID, this);

        callPeer.setDiscoveryInfo(discoverInfo);

        addCallPeer(callPeer);

        callPeer.setState(CallPeerState.INITIATING_CALL);

        // If this was the first peer we added in this call, then the call is
        // new and we need to notify everyone of its creation.
        if (getCallPeerCount() == 1)
            parentOpSet.fireCallEvent(CallEvent.CALL_INITIATED, this);

        CallPeerMediaHandlerJabberImpl mediaHandler
            = callPeer.getMediaHandler();

        /* enable video if it is a video call */
        mediaHandler.setLocalVideoTransmissionEnabled(localVideoAllowed);
        /* enable remote-control if it is a desktop sharing session */
        mediaHandler.setLocalInputEvtAware(getLocalInputEvtAware());

        /*
         * Set call state to connecting so that the user interface would start
         * playing the tones. We do that here because we may be harvesting
         * STUN/TURN addresses in initiateSession() which would take a while.
         */
        callPeer.setState(CallPeerState.CONNECTING);

        // if initializing session fails, set peer to failed
        boolean sessionInitiated = false;

        try
        {
            callPeer.initiateSession(sessionInitiateExtensions);
            sessionInitiated = true;
        }
        finally
        {
            // if initialization throws an exception
            if (!sessionInitiated)
                callPeer.setState(CallPeerState.FAILED);
        }
        return callPeer;
    }

    /**
     * Sends a <tt>content-modify</tt> message to each of the current
     * <tt>CallPeer</tt>s to reflect a possible change in the media setup
     * related to video.
     *
     * @param allowed <tt>true</tt> if the streaming of the local video to the
     * remote peer is allowed; otherwise, <tt>false</tt>
     * @throws OperationFailedException if a problem occurred during message
     * generation or there was a network problem
     */
    public void modifyVideoContent(boolean allowed)
        throws OperationFailedException
    {
        if (logger.isInfoEnabled())
        {
            logger.info(
                    (allowed ? "Start" : "Stop") + " local video streaming");
        }

        for (CallPeerJabberImpl peer : getCallPeerList())
            peer.sendModifyVideoContent(allowed);
    }

    /**
     * Notifies this instance that a specific <tt>ColibriConferenceIQ</tt> has
     * been received.
     *
     * @param conferenceIQ the <tt>ColibriConferenceIQ</tt> which has been
     * received
     * @return <tt>true</tt> if the specified <tt>conferenceIQ</tt> was
     * processed by this instance and no further processing is to be performed
     * by other possible processors of <tt>ColibriConferenceIQ</tt>s; otherwise,
     * <tt>false</tt>. Because a <tt>ColibriConferenceIQ</tt> request sent from
     * the Jitsi VideoBridge server to the application as its client concerns a
     * specific <tt>CallJabberImpl</tt> implementation, no further processing by
     * other <tt>CallJabberImpl</tt> instances is necessary once the
     * <tt>ColibriConferenceIQ</tt> is processed by the associated
     * <tt>CallJabberImpl</tt> instance.
     */
    boolean processColibriConferenceIQ(ColibriConferenceIQ conferenceIQ)
    {
        if (colibri == null)
        {
            /*
             * This instance has not set up any conference using the Jitsi
             * VideoBridge server-side technology yet so it cannot be bothered
             * with related requests.
             */
            return false;
        }
        else if (conferenceIQ.getID().equals(colibri.getID()))
        {
            /*
             * Remove the local Channels (from the specified conferenceIQ) i.e.
             * the Channels on which the local peer/user is sending to the Jitsi
             * VideoBridge server because they concern this Call only and not
             * its CallPeers.
             */
            for (MediaType mediaType : MediaType.values())
            {
                String contentName = mediaType.toString();
                ColibriConferenceIQ.Content content
                    = conferenceIQ.getContent(contentName);

                if (content != null)
                {
                    ColibriConferenceIQ.Content thisContent
                        = colibri.getContent(contentName);

                    if ((thisContent != null)
                            && (thisContent.getChannelCount() > 0))
                    {
                        ColibriConferenceIQ.Channel thisChannel
                            = thisContent.getChannel(0);
                        ColibriConferenceIQ.Channel channel
                            = content.getChannel(thisChannel.getID());

                        if (channel != null)
                            content.removeChannel(channel);
                    }
                }
            }

            for (CallPeerJabberImpl callPeer : getCallPeerList())
                callPeer.processColibriConferenceIQ(conferenceIQ);

            /*
             * We have removed the local Channels from the specified
             * conferenceIQ. Consequently, it is no longer the same and fit for
             * processing by other CallJabberImpl instances.
             */
            return true;
        }
        else
        {
            /*
             * This instance has set up a conference using the Jitsi VideoBridge
             * server-side technology but it is not the one referred to by the
             * specified conferenceIQ i.e. the specified conferenceIQ does not
             * concern this instance.
             */
            return false;
        }
    }

    /**
     * Creates a new call peer and sends a RINGING response.
     *
     * @param jingleIQ the {@link JingleIQ} that created the session.
     *
     * @return the newly created {@link CallPeerJabberImpl} (the one that sent
     * the INVITE).
     */
    public CallPeerJabberImpl processSessionInitiate(JingleIQ jingleIQ)
    {
        String remoteParty = jingleIQ.getInitiator();
        boolean autoAnswer = false;
        CallPeerJabberImpl attendant = null;
        OperationSetBasicTelephonyJabberImpl basicTelephony = null;

        //according to the Jingle spec initiator may be null.
        if (remoteParty == null)
            remoteParty = jingleIQ.getFrom();

        CallPeerJabberImpl callPeer
            = new CallPeerJabberImpl(remoteParty, this, jingleIQ);

        addCallPeer(callPeer);

        /*
         * We've already sent ack to the specified session-initiate so if it has
         * been sent as part of an attended transfer, we have to hang up on the
         * attendant.
         */
        try
        {
            TransferPacketExtension transfer
                = (TransferPacketExtension)
                    jingleIQ.getExtension(
                            TransferPacketExtension.ELEMENT_NAME,
                            TransferPacketExtension.NAMESPACE);

            if (transfer != null)
            {
                String sid = transfer.getSID();

                if (sid != null)
                {
                    ProtocolProviderServiceJabberImpl protocolProvider
                        = getProtocolProvider();
                    basicTelephony
                        = (OperationSetBasicTelephonyJabberImpl)
                            protocolProvider.getOperationSet(
                                    OperationSetBasicTelephony.class);
                    CallJabberImpl attendantCall
                        = basicTelephony
                            .getActiveCallsRepository()
                                .findSID(sid);

                    if (attendantCall != null)
                    {
                        attendant = attendantCall.getPeer(sid);
                        if ((attendant != null)
                                && basicTelephony
                                    .getFullCalleeURI(attendant.getAddress())
                                        .equals(transfer.getFrom())
                                && protocolProvider.getOurJID().equals(
                                        transfer.getTo()))
                        {
                            //basicTelephony.hangupCallPeer(attendant);
                            autoAnswer = true;
                        }
                    }
                }
            }
        }
        catch (Throwable t)
        {
            logger.error(
                    "Failed to hang up on attendant"
                        + " as part of session transfer",
                    t);

            if (t instanceof ThreadDeath)
                throw (ThreadDeath) t;
        }

        CoinPacketExtension coin
            = (CoinPacketExtension)
                jingleIQ.getExtension(
                        CoinPacketExtension.ELEMENT_NAME,
                        CoinPacketExtension.NAMESPACE);

        if (coin != null)
        {
            boolean b
                = Boolean.parseBoolean(
                        (String)
                            coin.getAttribute(
                                    CoinPacketExtension.ISFOCUS_ATTR_NAME));

            callPeer.setConferenceFocus(b);
        }

        //before notifying about this call, make sure that it looks alright
        callPeer.processSessionInitiate(jingleIQ);

        // if paranoia is set, to accept the call we need to know that
        // the other party has support for media encryption
        if (getProtocolProvider().getAccountID().getAccountPropertyBoolean(
                ProtocolProviderFactory.MODE_PARANOIA, false)
            && callPeer.getMediaHandler().getAdvertisedEncryptionMethods()
                    .length
                == 0)
        {
            //send an error response;
            String reasonText
                = JabberActivator.getResources().getI18NString(
                        "service.gui.security.encryption.required");
            JingleIQ errResp
                = JinglePacketFactory.createSessionTerminate(
                        jingleIQ.getTo(),
                        jingleIQ.getFrom(),
                        jingleIQ.getSID(),
                        Reason.SECURITY_ERROR,
                        reasonText);

            callPeer.setState(CallPeerState.FAILED, reasonText);
            getProtocolProvider().getConnection().sendPacket(errResp);

            return null;
        }

        if (callPeer.getState() == CallPeerState.FAILED)
            return null;

        callPeer.setState( CallPeerState.INCOMING_CALL );

        // in case of attended transfer, auto answer the call
        if (autoAnswer)
        {
            /* answer directly */
            try
            {
                callPeer.answer();
            }
            catch(Exception e)
            {
                logger.info(
                        "Exception occurred while answer transferred call",
                        e);
                callPeer = null;
            }

            // hang up now
            try
            {
                basicTelephony.hangupCallPeer(attendant);
            }
            catch(OperationFailedException e)
            {
                logger.error(
                        "Failed to hang up on attendant as part of session"
                            + " transfer",
                        e);
            }

            return callPeer;
        }

        /* see if offer contains audio and video so that we can propose
         * option to the user (i.e. answer with video if it is a video call...)
         */
        List<ContentPacketExtension> offer
            = callPeer.getSessionIQ().getContentList();
        Map<MediaType, MediaDirection> directions
            = new HashMap<MediaType, MediaDirection>();

        directions.put(MediaType.AUDIO, MediaDirection.INACTIVE);
        directions.put(MediaType.VIDEO, MediaDirection.INACTIVE);

        for (ContentPacketExtension c : offer)
        {
            String contentName = c.getName();
            MediaDirection remoteDirection
                = JingleUtils.getDirection(c, callPeer.isInitiator());

            if (MediaType.AUDIO.toString().equals(contentName))
                directions.put(MediaType.AUDIO, remoteDirection);
            else if (MediaType.VIDEO.toString().equals(contentName))
                directions.put(MediaType.VIDEO, remoteDirection);
        }

        // If this was the first peer we added in this call, then the call is
        // new and we need to notify everyone of its creation.
        if (getCallPeerCount() == 1)
        {
            parentOpSet.fireCallEvent(
                    CallEvent.CALL_RECEIVED,
                    this,
                    directions);
        }

        // Manages auto answer with "audio only", or "audio/video" answer.
        OperationSetAutoAnswerJabberImpl autoAnswerOpSet
            = (OperationSetAutoAnswerJabberImpl)
                getProtocolProvider().getOperationSet(
                        OperationSetBasicAutoAnswer.class);

        if (autoAnswerOpSet != null)
            autoAnswerOpSet.autoAnswer(this, directions);

        return callPeer;
    }
}
