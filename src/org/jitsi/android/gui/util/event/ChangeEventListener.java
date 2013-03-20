/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.util.event;

/**
 * The listener interface that is intended to listen for changes
 * on the <tt>T</tt> class object
 *
 * @param <T> class affected by the change
 *
 * @author Pawel Domas
 */
public interface ChangeEventListener<T>
{
    /**
     * Method fired when change occurs on the <tt>eventObject</tt>
     *
     * @param eventObject the instance that has been changed
     */
    void onChangeEvent(T eventObject);
}
