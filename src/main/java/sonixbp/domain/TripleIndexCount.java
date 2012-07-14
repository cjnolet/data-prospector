package sonixbp.domain;


import sonixbp.TripleValueType;

public class TripleIndexCount {

    TripleValueType type;
    String index;
    long count;
    long prospectTime;

    public TripleIndexCount(TripleValueType type, String index, long count, long prospectTime) {
        this.type = type;
        this.index = index;
        this.count = count;
        this.prospectTime = prospectTime;
    }

    public TripleValueType getType() {
        return type;
    }

    public void setType(TripleValueType type) {
        this.type = type;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getProspectTime() {
        return prospectTime;
    }

    public void setProspectTime(long prospectTime) {
        this.prospectTime = prospectTime;
    }

}
