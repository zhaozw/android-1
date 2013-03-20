/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.util.event;

import java.util.*;

/**
 * Utility class that should be used for storing {@link ChangeEventListener}s
 *
 * @param <T> the listeners class
 * @param <S> the class of event source
 * 
 * @author Pawel Domas
 */
public class ChangeEventListenerList<T extends ChangeEventListener<S>, S>
{
    /**
     * The list of {@link ChangeEventListener}
     */
    private ArrayList<T> listeners = new ArrayList<T>();

    /**
     * Adds the <tt>listener</tt> to the list
     *
     * @param listener the {@link ChangeEventListener} that will
     *  be added to the list
     */
    public void addEventListener(T listener)
    {
        listeners.add(listener);
    }

    /**
     * Removes the <tt>listener</tt> from the list
     *
     * @param listener the {@link ChangeEventListener} that will
     *  be removed from the list
     */
    public void removeEventListener(T listener)
    {
        listeners.remove(listener);
    }

    /**
     * Runs the event change notification on listeners list
     *
     * @param sourceObject the source object of the event
     */
    public void notifyEventListeners(S sourceObject)
    {
        for(T l : listeners)
        {
            l.onChangeEvent(sourceObject);
        }
    }

    /**
     * Clears the listeners list
     */
    public void clear()
    {
        listeners.clear();
    }
}
