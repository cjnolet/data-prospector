package sonixbp.support.mock;

import sonixbp.support.AuthStrategy;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cnolet
 * Date: 7/15/12
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockAuthStrategy implements AuthStrategy{

    List<String> auths = Arrays.asList(new String[] { "U", "FOUO"});
    String username = "username";

    public MockAuthStrategy() {
    }

    public MockAuthStrategy(String username, List<String> auths) {
        this.username = username;
        this.auths = auths;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getAuthorizations() {
        return auths;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
