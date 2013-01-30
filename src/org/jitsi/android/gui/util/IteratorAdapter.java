/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.util;

import android.view.*;
import android.widget.*;

import java.util.*;

/**
 * Convenience class wrapping {@link Iterator} of elements into {@link Adapter}
 *
 * @param <T> class of the elements contained in this adapter
 */
public abstract class IteratorAdapter<T>
        extends BaseAdapter
{
    /**
     * The {@link LayoutInflater} used to create the views
     */
    private final LayoutInflater layoutInflater;
    /**
     * List of elements handled by this adapter
     */
    private final List<T> items;

    /**
     * Creates a new instance of {@link IteratorAdapter}
     *
     * @param iterator the {@link Iterator} of objects for this adapter
     * @param layoutInflater the {@link LayoutInflater} for current context
     */
    public IteratorAdapter(
            Iterator<T> iterator,
            LayoutInflater layoutInflater)
    {
        items = new ArrayList<T>();
        while (iterator.hasNext())
            items.add(iterator.next());

        this.layoutInflater = layoutInflater;
    }

    public int getCount()
    {
        return items.size();
    }

    public Object getItem(int i)
    {
        return items.get(i);
    }

    public long getItemId(int i)
    {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup)
    {
        return getView(items.get(i), viewGroup, layoutInflater);
    }

    /**
     * Convenience method for creating new Views for each adapter's object
     *
     * @param item the item for which a new View shall be created
     * @param parent {@link ViewGroup} parent View
     * @param inflater the {@link LayoutInflater} for creating new Views
     *
     * @return a {@link View} for given <tt>item</tt>
     */
    protected abstract View getView( T item,
                                     ViewGroup parent,
                                     LayoutInflater inflater);
}