package sonixbp.domain;


import sonixbp.TripleValueType;

public class TripleIndexCount implements Comparable<TripleIndexCount> {

    long count;
    Long prospectTime;


    public TripleIndexCount(long count, Long prospectTime) {
        this.count = count;
        this.prospectTime = prospectTime;
    }

    public void increment(Long amount) {
        count += amount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public Long getProspectTime() {
        return prospectTime;
    }

    public void setProspectTime(long prospectTime) {
        this.prospectTime = prospectTime;
    }

    public int compareTo(TripleIndexCount tripleIndexCount) {
        return tripleIndexCount.getProspectTime().compareTo(getProspectTime());
    }
}
