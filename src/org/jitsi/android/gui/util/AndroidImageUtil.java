/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jitsi.android.gui.util;

import android.graphics.*;

/**
 * Class containing utility methods for Android's Displayable and Bitmap
 */
public class AndroidImageUtil
{
    /**
     * Converts given array of bytes to {@link Bitmap}
     *
     * @param imageBlob array of bytes with raw image data
     *
     * @return {@link Bitmap} created from <tt>imageBlob</tt>
     */
    static public Bitmap bitmapFromBytes(byte[] imageBlob)
    {
        if(imageBlob != null)
        {
            Bitmap icon = BitmapFactory.decodeByteArray(
                    imageBlob, 0, imageBlob.length);
            return icon;
        }
        return null;
    }
}
