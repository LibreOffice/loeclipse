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

 
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * This is a test suite for an OOo extension unit tests: 
 * it bootstraps an OOo instance and handles the connection with it.
 * 
 * The extension to test has to be already installed on OOo for the
 * current user.
 * 
 * @author cbosdo
 *
 */
public class UnoTestSuite extends TestSuite '{'
    
    private XComponentContext mContext;
    private boolean mHandleConnection = false;
    
    
    public UnoTestSuite() '{'
        super();
    }

    public UnoTestSuite(Class pTheClass, String pName) '{'
        super(pTheClass, pName);
    }

    public UnoTestSuite(Class pTheClass) '{'
        super(pTheClass);
    }

    public UnoTestSuite(Class[] pClasses, String pName) '{'
        super(pClasses, pName);
    }

    public UnoTestSuite(Class[] pClasses) '{'
        // Override to create UnoTestSuites instead of TestSuites
        for (int i= 0; i < pClasses.length; i++) '{'
            addTest(new UnoTestSuite(pClasses[i]));
        }
    }

    public UnoTestSuite(String pName) '{'
        super(pName);
    }
    
    public void run(TestResult pResult) '{'
        
        try '{'
            startOffice();
            
            super.run(pResult);
        } catch (Exception e) '{'
            System.err.println("Unable to run the tests");
            e.printStackTrace();
        }
        
        stopOffice();
    }
    
    public void runTest(Test pTest, TestResult pResult) '{'
        // Try to set the context on the test
        if (pTest instanceof UnoTestCase) '{'
            ((UnoTestCase)pTest).setContext(mContext);
        } else if (pTest instanceof UnoTestSuite) '{'
            UnoTestSuite suite = (UnoTestSuite)pTest;
            suite.setContext(mContext);
        }
        
        super.runTest(pTest, pResult);
    }
    
    protected void setContext(XComponentContext pContext) '{'
        mContext = pContext;
    }
    
    protected void setHandleConnection(boolean pHandle) '{'
        mHandleConnection = pHandle;
    }
    
    /**
     * Starts LibreOffice only if the context isn't set.
     * 
     * @throws Exception if anything wrong happens when starting the office
     */
    private void startOffice() throws Exception '{'
        if (mContext == null) '{'
            String path = System.getProperty("openoffice.program.path");
            if (path == null) '{'
                throw new Exception("The openoffice.install.path variable is missing");
            }

            File baseDir = new File(path);
            URLClassLoader classLoader = new URLClassLoader(new URL[]'{'baseDir.toURI().toURL()});
            mContext = Bootstrap.bootstrap(classLoader);
            mHandleConnection = true;
        }
    }
    
    private void stopOffice() '{'
        try '{'
            if (mContext != null && mHandleConnection) '{'
                // Only the uno test suite which started the office can stop it
                XMultiComponentFactory xMngr = mContext.getServiceManager();
                Object oDesktop = xMngr.createInstanceWithContext("com.sun.star.frame.Desktop", mContext);
                XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, oDesktop);
                
                xDesktop.terminate();
            }
        } catch (Exception e) '{'
            e.printStackTrace();
        }
    }
}
