/*
 *   OpenOffice.org extension for syntax highlighting
 *   Copyright (C) 2008  Cédric Bosdonnat cedricbosdo@openoffice.org
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; 
 *   version 2 of the License.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package {0}.tests.base;

import junit.framework.TestCase;

import com.sun.star.uno.XComponentContext;

/**
 * Base for all test cases to use along with '{'@link UnoTestSuite}.
 * 
 *
 */
public abstract class UnoTestCase extends TestCase '{'
    
    /**
     * Default constructor.
     */
    public UnoTestCase() '{'
        super();
    }

    /**
     * Constructor to use to run only one test
     *  
     * @param pName the test name
     */
    public UnoTestCase(String pName) '{'
        super(pName);
    }
    

    private XComponentContext mContext;

    /**
     * Set the office context to use in the tests.
     * 
     * @param pContext the remote office context
     */
    public void setContext(XComponentContext pContext) '{'
        mContext = pContext;
    }
    
    /**
     * Get the office context to use in the tests.
     * 
     * @return the remote office context
     */
    protected XComponentContext getContext() '{'
        return mContext;
    }
}
