/*************************************************************************
 *
 * $RCSfile: OverlayImageIcon.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:28 $
 *
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
package org.openoffice.ide.eclipse.core.gui;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * This class is used for overlaying image icons.
 * 
 * @author cedricbosdo
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
     * @param pBaseImage the image to decorate
     * @param pImage the decorator image
     * @param pLocation the location of the decorator image on the base image.
     */
    public OverlayImageIcon(Image pBaseImage, Image pImage, int pLocation) {
        // Base image of the object
        mBaseImage = pBaseImage;

        mImage = pImage;
        mLocation = pLocation; 
        mSizeOfImage = new Point(pBaseImage.getBounds().width, 
                pBaseImage.getBounds().height);
    }

    /**
     * this method is called to draw the composite image.
     * 
     * @param pLower the first image to draw
     * @param pUpper the top image to draw 
     * 
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#drawCompositeImage(int, int)
     * 
     * 
     */
    protected void drawCompositeImage(int pLower, int pUpper) {
        // Draw the base image
        drawImage(mBaseImage.getImageData(), 0, 0);

        ImageData imageData = mImage.getImageData();
        switch(mLocation) {
            // Draw on the top left corner
            case TOP_LEFT:
                drawImage(imageData, 0, 0);
                break;

            // Draw on top right corner  
            case TOP_RIGHT:
                drawImage(imageData, mSizeOfImage.x - imageData.width, 0);
                break;

            // Draw on bottom left  
            case BOTTOM_LEFT:
                drawImage(imageData, 0, mSizeOfImage.y - imageData.height);
                break;

            // Draw on bottom right corner  
            case BOTTOM_RIGHT:
                drawImage(imageData, mSizeOfImage.x - imageData.width,
                        mSizeOfImage.y - imageData.height);
                break;

        }
    }


    /**
     * @return Get the size of the composite image
     * 
     * @see org.eclipse.jface.resource.CompositeImageDescriptor#getSize()
     */
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