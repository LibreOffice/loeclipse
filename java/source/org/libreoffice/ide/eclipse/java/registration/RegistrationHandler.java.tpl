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

import java.lang.reflect.Field;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;

/**
 * Component main registration class.
 * 
 * <p><strong>This class should not be modified.</strong></p>
 * 
 * @author Cedric Bosdonnat aka. cedricbosdo
 *
 */
public class RegistrationHandler '{'

    /**
    * Get a component factory for the implementations handled by this class.
    *
    * <p>This method retrieve the Class object associated with the class with the given
    * <code>implementation</code> String parameter. If this class has a <code>m_serviceNames</code>
    * static field then the list of its services will be used, otherwise the <code>implementation</code>
    * parameter will be the only entry in the list of supported services.
    * <strong>This method should not be modified.</strong></p>
    *
    * @param implementation the name of the implementation to create.
    *
    * @return the factory which can create the implementation.
    */
    public static XSingleComponentFactory __getComponentFactory(final String implementation) '{'
        Class<?> clazz = null;
        try '{'
            clazz = Class.forName(implementation);
        } catch (ClassNotFoundException e) '{'
            // Nothing to do: skip
            System.err.println("Error happened");
            e.printStackTrace();
        }
        XSingleComponentFactory factory = null;
        if (clazz != null) '{'
            String fieldName = "m_serviceNames";
            String [] services = null;
            try '{'
                int i = 0;
                Field[] fields = clazz.getDeclaredFields();
                while (i < fields.length && services == null) '{'
                    Field field = fields[i];
                    if (field.getName().equals(fieldName)) '{'
                        field.setAccessible(true);
                        services = (String[]) field.get(null);
                    }
                    i++;
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) '{'
                // Nothing to do: skip
                System.err.println("Error happened");
                e.printStackTrace();
            }
            if (services == null) '{'
                services = new String[] '{'implementation};
                System.err.println(String.format("No <%s> static field, defaulting to: '{'%s}", fieldName, implementation));
            }
            factory = Factory.createComponentFactory(clazz, services);
        }
        return factory;
    }

}
