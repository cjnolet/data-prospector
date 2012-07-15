package sonixbp.domain;

import sonixbp.TripleValueType;

import java.util.ArrayList;
import java.util.List;

public class TripleIndexCountDescription extends TripleIndexDescription {

    List<TripleIndexCount> counts;

    public TripleIndexCountDescription(String index, TripleValueType type, String dataType) {
        super(index, type, dataType);
        this.counts = new ArrayList<TripleIndexCount>();
    }

    public List<TripleIndexCount> getCounts() {
        return counts;
    }


    public void addIndexCount(TripleIndexCount count) {
        this.counts.add(count);
    }
}
