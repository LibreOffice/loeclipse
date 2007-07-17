/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package {0};

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;

public class RegistrationHandler '{'
	
	public static XSingleComponentFactory __getComponentFactory(String sImplementationName ) '{'
        XSingleComponentFactory xFactory = null;
    
        Class[] classes = findServicesImplementationClasses();
        
        int i = 0;
        while (i < classes.length && xFactory == null) '{'
        	Class clazz = classes[i];
        	if ( sImplementationName.equals( clazz.getCanonicalName() ) ) '{'
        		try '{'
        			Class[] getTypes = new Class[]'{'String.class};
        			Method getFactoryMethod = clazz.getMethod("__getComponentFactory", getTypes);
        			Object o = getFactoryMethod.invoke(null, sImplementationName);
        			xFactory = (XSingleComponentFactory)o;
        		} catch (Exception e) '{'
        			// Nothing to do: skip
        			System.err.println("Error happened");
        			e.printStackTrace();
        		}
        	}
        	i++;
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey ) '{'
    	
    	Class[] classes = findServicesImplementationClasses();
    	
    	boolean success = true;
        int i = 0;
        while (i < classes.length && success) '{'
        	Class clazz = classes[i];
        	try '{'
        		Class[] writeTypes = new Class[]'{'XRegistryKey.class};
        		Method getFactoryMethod = clazz.getMethod("__writeRegistryServiceInfo", writeTypes);
        		Object o = getFactoryMethod.invoke(null, xRegistryKey);
        		success = success && ((Boolean)o).booleanValue();
        	} catch (Exception e) '{'
        		success = false;
    			e.printStackTrace();
        	}
        	i++;
        }
        return success;
    }
    
    private static Class[] findServicesImplementationClasses() '{'
    	
    	ArrayList<Class> classes = new ArrayList<Class>();
    	
    	InputStream in = RegistrationHandler.class.getResourceAsStream("RegistrationHandler.classes");
    	LineNumberReader reader = new LineNumberReader(new InputStreamReader(in));
    	
    	try '{'
    		String line = reader.readLine();
    		while (line != null) '{'
    			if (!line.equals("")) '{'
    				line = line.trim();
    				try '{'
    					Class clazz = Class.forName(line);
    					
    					Class[] writeTypes = new Class[]'{'XRegistryKey.class};
    					Class[] getTypes = new Class[]'{'String.class};
    					
    					Method writeRegMethod = clazz.getMethod("__writeRegistryServiceInfo", writeTypes);
    					Method getFactoryMethod = clazz.getMethod("__getComponentFactory", getTypes);

    					if (writeRegMethod != null && getFactoryMethod != null) '{'
    						classes.add(clazz);
    					}

    				} catch (Exception e) '{'
    					e.printStackTrace();
    				}
    			}
    			line = reader.readLine();
    		}
    	} catch (IOException e) '{'
    		e.printStackTrace();
    	} finally '{'
    		try '{'
    			reader.close();
    			in.close();
    		} catch (Exception e) '{'};
    	}
    	
    	return classes.toArray(new Class[classes.size()]);
    }
}
