package sonixbp.domain;

/**
 * Created by IntelliJ IDEA.
 * User: cnolet
 * Date: 7/13/12
 * Time: 9:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class PredicateDescription {

    String predicate;
    String visibility;
    String datatype;


    public PredicateDescription(String predicate, String visibility, String datatype) {
        this.predicate = predicate;
        this.visibility = visibility;
        this.datatype = datatype;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
