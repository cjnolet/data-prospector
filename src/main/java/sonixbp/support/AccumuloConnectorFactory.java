package sonixbp.support;


import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;

public class AccumuloConnectorFactory {

    public static Connector getConnector(Instance instance, String username, String password) throws AccumuloException, AccumuloSecurityException {

        return instance.getConnector(username, password.getBytes());
    }
}
