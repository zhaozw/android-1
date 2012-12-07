package net.java.sip.communicator.util.call;

import java.util.*;

import net.java.sip.communicator.service.protocol.*;

public class CallManager
{
    /**
     * A table mapping protocol <tt>Call</tt> objects to the GUI dialogs
     * that are currently used to display them.
     */
    private static Vector<Call> activeCalls = new Vector<Call>();

    public static void addActiveCall(Call call)
    {
        activeCalls.add(call);
    }

    public static void removeActiveCall(Call call)
    {
        activeCalls.remove(call);
    }

    public static Iterator<Call> getActiveCalls()
    {
        return activeCalls.iterator();
    }

    /**
     * Hang ups the given call.
     *
     * @param call the call to hang up
     */
    public static void hangupCall(final Call call)
    {
        new HangupCallThread(call).start();
    }

    /**
     * Hang-ups all call peers in the given call.
     */
    private static class HangupCallThread
        extends Thread
    {
        private final Call call;

        public HangupCallThread(Call call)
        {
            this.call = call;
        }

        @Override
        public void run()
        {
            ProtocolProviderService pps = call.getProtocolProvider();
            Iterator<? extends CallPeer> peers = call.getCallPeers();

            while (peers.hasNext())
            {
                CallPeer peer = peers.next();
                OperationSetBasicTelephony<?> telephony
                    = pps.getOperationSet(OperationSetBasicTelephony.class);

                try
                {
                    telephony.hangupCallPeer(peer);
                }
                catch (OperationFailedException e)
                {
                    System.err.println("Could not hang up : " + peer
                        + " caused by the following exception: " + e);
                }
            }

            removeActiveCall(call);
        }
    }
}
