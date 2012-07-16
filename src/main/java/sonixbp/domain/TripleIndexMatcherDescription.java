package sonixbp.domain;

import sonixbp.TripleValueType;

/**
 * Created by IntelliJ IDEA.
 * User: cnolet
 * Date: 7/15/12
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class TripleIndexMatcherDescription extends TripleIndexDescription {

    private String visibility;

    public TripleIndexMatcherDescription(String index, TripleValueType type, String dataType, String visibility) {
        super(index, type, dataType);

        this.visibility = visibility;
    }

    public String getVisibility() {
        return visibility;
    }
}
