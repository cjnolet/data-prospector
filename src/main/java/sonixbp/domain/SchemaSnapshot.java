package sonixbp.domain;


import java.util.ArrayList;
import java.util.Collection;

public class SchemaSnapshot {

    String schemeAndType;
    long prospectTime;
    Collection<PredicateDescription> predicates;

    public SchemaSnapshot(String schemeAndType, long prospectTime) {
        this.schemeAndType = schemeAndType;
        this.prospectTime = prospectTime;
        this.predicates = new ArrayList<PredicateDescription>();
    }

    public void addPredicate(PredicateDescription description) {
        predicates.add(description);
    }

    public String getSchemeAndType() {
        return schemeAndType;
    }

    public void setSchemeAndType(String schemeAndType) {
        this.schemeAndType = schemeAndType;
    }

    public long getProspectTime() {
        return prospectTime;
    }

    public void setProspectTime(long prospectTime) {
        this.prospectTime = prospectTime;
    }

    public Collection<PredicateDescription> getPredicates() {
        return predicates;
    }

    public void setPredicates(Collection<PredicateDescription> predicates) {
        this.predicates = predicates;
    }

}
