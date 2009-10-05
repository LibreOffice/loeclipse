package {0};

import org.openoffice.connection.AbstractConnection;
import org.openoffice.connection.PipeConnection;
import org.openoffice.connection.SocketConnection;

import com.sun.star.uno.XComponentContext;

public class UnoClient '{'

    public static void main(String[] args) '{'
        AbstractConnection cnx = null;
        
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
