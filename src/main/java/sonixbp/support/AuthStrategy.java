package sonixbp.support;

import java.util.List;

public interface AuthStrategy {

    String getUsername();
    List<String> getAuthorizations();
}
