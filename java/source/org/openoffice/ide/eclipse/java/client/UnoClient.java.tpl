package {0};

import com.artofsolving.jodconverter.openoffice.connection.AbstractOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.PipeOpenOfficeConnection;

import com.sun.star.uno.XComponentContext;

public class UnoClient '{'

    public static void main(String[] args) '{'
        AbstractOpenOfficeConnection cnx = null;
        
        try '{'
            {1}
            System.out.println("Connecting to OOo...");
            cnx.connect();
            
            System.out.println( "Trying to get the component context" );
            if ( cnx.getComponentContext() != null ) '{'
                XComponentContext xCtx = cnx.getComponentContext();
                
                // TODO Use the UNO connection here
                
            }
        } catch (Exception e) '{'
            e.printStackTrace();
        } finally '{'
            if ( cnx != null ) '{'
                System.out.println( "Disconnecting from OOo..." );
                cnx.disconnect();
            }
        }
    }
}
