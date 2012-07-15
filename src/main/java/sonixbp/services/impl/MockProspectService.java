package sonixbp.services.impl;

import org.apache.accumulo.core.security.Authorizations;
import sonixbp.TripleValueType;
import sonixbp.domain.PredicateDescription;
import sonixbp.domain.SchemaSnapshot;
import sonixbp.domain.TripleIndexCount;
import sonixbp.domain.TripleIndexDescription;
import sonixbp.services.ProspectService;

import javax.sql.rowset.Predicate;
import java.util.*;


public class MockProspectService implements ProspectService{


    public Iterator<Long> getProspects(Authorizations auths) {

        Collection<Long> stuff = new ArrayList<Long>();

        for(int i = 0; i < 100000000; i+=50000) {
            stuff.add(System.currentTimeMillis() - i);
        }

        return stuff.iterator();
    }

    public Iterator<Long> getProspectsInRange(long beginTime, long endTime, Authorizations auths) {

        Collection<Long> stuff = new ArrayList<Long>();

        for(long i = beginTime; i < endTime; i+=5000) {
            stuff.add(System.currentTimeMillis() - i);
        }

        return stuff.iterator();
    }

    public Collection<SchemaSnapshot> getSchemaSnapshots(List<Long> prospectTimes, String subject, String dataType, Authorizations auths) {

        Collection<SchemaSnapshot> snapshots = new ArrayList<SchemaSnapshot>();
        for(Long prospect : prospectTimes) {

            SchemaSnapshot snapshot = new SchemaSnapshot(subject, prospect);
            snapshot.addPredicate(new PredicateDescription("name", "U&FOUO", "xsd:string"));

            snapshots.add(snapshot);
        }

        return snapshots;
    }

    public SchemaSnapshot getSchemaSnapshot(Long prospectTime, String typeAndScheme, String dataType, Authorizations auths) {

        List<Long> prospects = new ArrayList<Long>();
        prospects.add(prospectTime);

        return getSchemaSnapshots(prospects, typeAndScheme, dataType, auths).iterator().next();
    }

    public Iterator<String> getTypesContainingPredicate(Long prospectTime, String predicate, String dataType, Authorizations auths) {

        return Arrays.asList(new String[] { "gem://sensor", "gem://tipper", "gem://comment", "gem://activityGroup"}).iterator();
    }

    public Collection<TripleIndexCount> getCountsForIndex(List<Long> prospectTimes, TripleValueType type, String index, String dataType, Authorizations auths) {
        return null;
    }

    public TripleIndexCount getCountForIndex(Long prospectTime, TripleValueType type, String index, String dataType, Authorizations auths) {
        return null;
    }

    public Iterator<TripleIndexDescription> getMatchesForPartialIndex(TripleValueType type, String partialIndex, String dataType, Authorizations auths) {

        Collection<TripleIndexDescription> indexes = new ArrayList<TripleIndexDescription>();
        indexes.add(new TripleIndexDescription("gem://sensor", TripleValueType.subject, "xsd:anyUri"));
        indexes.add(new TripleIndexDescription("1.1.1.1", TripleValueType.object, "gem:ip"));
        indexes.add(new TripleIndexDescription("gem://name", TripleValueType.predicate, "xsd:anyUri"));

        return indexes.iterator();
    }
}
