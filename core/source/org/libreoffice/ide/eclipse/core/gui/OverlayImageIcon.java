/*************************************************************************
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.gui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.Point;

/**
 * This class is used for overlaying image icons.
 *
 */
public class OverlayImageIcon extends CompositeImageDescriptor {

    public static final int TOP_LEFT = 0;
    public static final int TOP_RIGHT = 1;
    public static final int BOTTOM_LEFT = 2;
    public static final int BOTTOM_RIGHT = 3;

    /**
     * Base image of the object.
     */
    private Image mBaseImage;

    /**
     * Size of the base image.
     */
    private Point mSizeOfImage;

    /**
     * Demo Image instance.
     */
    private Image mImage;

    private int mLocation;

    /**
     * Constructor.
     *
     * @param baseImage
     *            the image to decorate
     * @param image
     *            the decorator image
     * @param location
     *            the location of the decorator image on the base image.
     */
    public OverlayImageIcon(Image baseImage, Image image, int location) {
        // Base image of the object
        mBaseImage = baseImage;

        mImage = image;
        mLocation = location;
        mSizeOfImage = new Point(baseImage.getBounds().width, baseImage.getBounds().height);
    }

    /**
     * this method is called to draw the composite image.
     *
     * @param lower
     *            the first image to draw
     * @param upper
     *            the top image to draw
     *
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
     *
     *
     */
    @Override
    protected void drawCompositeImage(int lower, int upper) {
        // Draw the base image
        drawImage(createCachedImageDataProvider(mBaseImage), 0, 0);

        ImageDataProvider provider = createCachedImageDataProvider(mImage);
        switch (mLocation) {
            // Draw on the top left corner
            case TOP_LEFT:
                drawImage(provider, 0, 0);
                break;

                // Draw on top right corner
            case TOP_RIGHT:
                drawImage(provider, mSizeOfImage.x - mImage.getBounds().width, 0);
                break;

                // Draw on bottom left
            case BOTTOM_LEFT:
                drawImage(provider, 0, mSizeOfImage.y - mImage.getBounds().height);
                break;

                // Draw on bottom right corner
            case BOTTOM_RIGHT:
                drawImage(provider, mSizeOfImage.x - mImage.getBounds().width,
                    mSizeOfImage.y - mImage.getBounds().height);
                break;

        }
    }

    /**
     * @return Get the size of the composite image
     *
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
     */
    @Override
    protected Point getSize() {
        return mSizeOfImage;
    }

    /**
     * Get the image formed by overlaying different images on the base image.
     *
     * @return composite image
     */
    public Image getImage() {
        return createImage();
    }
}