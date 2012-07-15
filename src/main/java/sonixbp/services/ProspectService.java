package sonixbp.services;

import org.apache.accumulo.core.security.Authorizations;
import sonixbp.TripleValueType;
import sonixbp.domain.SchemaSnapshot;
import sonixbp.domain.TripleIndexCount;
import sonixbp.domain.TripleIndexCountDescription;
import sonixbp.domain.TripleIndexDescription;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public interface ProspectService {

    /**
     * Gets an iterator with the prospects- most recent prospects come back first
     * @return
     */
    Iterator<Long> getProspects(Authorizations auths);

    /**
     * Gets an iterator with the prospects that occurred in a specific time range (most recent returned first)
     * @param beginTime
     * @param endTime
     * @return
     */
    Iterator<Long> getProspectsInRange(long beginTime, long endTime, Authorizations auths);

    /**
     * Gets a collection of schema snapshots given a list of prospect times for a specific scheme/type pair.
     * An optional datatype can be specified to return only those PredicateDescriptions that match the datatype.
     * @param prospectTimes
     * @param subject
     * @param dataType
     * @return
     */
    Collection<SchemaSnapshot> getSchemaSnapshots(List<Long> prospectTimes, String subject, String dataType, Authorizations auths);

    /**
     * Gets a schema snapshot for a single run of the prospector for a specific scheme/type pair.
     * An optional datatype can be specified to return only those PredciateDescriptions that match the datatype.
     * @param prospectTime
     * @param typeAndScheme
     * @param dataType
     * @return
     */
    SchemaSnapshot getSchemaSnapshot(Long prospectTime, String typeAndScheme, String dataType, Authorizations auths);

    /**
     * Gets an iterator containing all scheme/type pairs that contain the given preciate for the given prospect time.
     * Predicate can be searched only by name or an optional datatype can be specified to further refine results.
     * @param prospectTime
     * @param predicate
     * @param dataType
     * @return
     */
    Iterator<String> getTypesContainingPredicate(Long prospectTime, String predicate, String dataType, Authorizations auths);

    /**
     * Returns a collection containing the counts for a given index (s/p/o) for a list of prospect times.
     * An optional datatype can further refine the results.
     *
     * @param prospectTimes
     * @param type
     * @param index
     * @param dataType
     * @return
     */
    TripleIndexCountDescription getCountsForIndex(List<Long> prospectTimes, TripleValueType type, String index, String dataType, Authorizations auths);

    /**
     * Returns a count for a given index (s/p/o) for a single prospect time.
     * An optional datatype can further refine the results.
     *
     * @param prospectTime
     * @param type
     * @param index
     * @param dataType
     * @return
     */
    TripleIndexCountDescription getCountForIndex(Long prospectTime, TripleValueType type, String index, String dataType, Authorizations auths);

    /**
     * Returns the matches for a partial index (starts-with search). An optional TripleValueType and datatype can
     * further refine the search
     * @param type
     * @param partialIndex
     * @param dataType
     * @return
     */
    Iterator<TripleIndexDescription> getMatchesForPartialIndex(TripleValueType type, String partialIndex, String dataType, Authorizations auths);


}
