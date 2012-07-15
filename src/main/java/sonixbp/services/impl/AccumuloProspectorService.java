package sonixbp.services.impl;

import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonixbp.TripleValueType;
import sonixbp.domain.PredicateDescription;
import sonixbp.domain.SchemaSnapshot;
import sonixbp.domain.TripleIndexCount;
import sonixbp.domain.TripleIndexDescription;
import sonixbp.services.ProspectService;
import sonixbp.support.Constants;
import sonixbp.support.ProspectorMutationFactory;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.util.*;


public class AccumuloProspectorService implements ProspectService {

    private static Logger logger = LoggerFactory.getLogger(AccumuloProspectorService.class);

    private final Connector connector;

    public AccumuloProspectorService(Connector connector) {
        this.connector = connector;
    }

    public Iterator<Long> getProspects(Authorizations auths) {

        try {
            final Scanner scanner = connector.createScanner(Constants.PROSPECTOR_TABLE, auths);
            scanner.setRange(new Range(ProspectorMutationFactory.METADATA));
            scanner.fetchColumnFamily(new Text(ProspectorMutationFactory.PROSPECT_TIME));

            final Iterator<Map.Entry<Key,Value>> iterator = scanner.iterator();

            return new Iterator<Long>() {

                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Long next() {

                    // date should be reverse indexed
                    return Long.parseLong(iterator.next().getKey().getColumnQualifier().toString());
                }

                public void remove() {

                    iterator.remove();
                }
            };

        } catch (TableNotFoundException e) {

            logger.error("The prospector table was not found");
        }

        return null;
    }

    public Iterator<Long> getProspectsInRange(long beginTime, long endTime, Authorizations auths) {

        try {
            final Scanner scanner = connector.createScanner(Constants.PROSPECTOR_TABLE, auths);

            scanner.setRange(new Range(new Key(new Text(ProspectorMutationFactory.METADATA),
                    new Text(ProspectorMutationFactory.PROSPECT_TIME), new Text(Long.toString(endTime))),
                    new Key(new Text(ProspectorMutationFactory.METADATA),
                            new Text(ProspectorMutationFactory.PROSPECT_TIME), new Text(Long.toString(beginTime)))));

            final Iterator<Map.Entry<Key,Value>> iterator = scanner.iterator();

            return new Iterator<Long>() {

                public boolean hasNext() {
                    return iterator.hasNext();
                }

                public Long next() {

                    // date should be reverse indexed
                    return Long.parseLong(iterator.next().getKey().getColumnQualifier().toString());
                }

                public void remove() {

                    iterator.remove();
                }
            };

        } catch (TableNotFoundException e) {

            logger.error("The prospector table was not found");
        }

        return null;
    }

    public Collection<SchemaSnapshot> getSchemaSnapshots(List<Long> prospectTimes, String subject, String dataType, Authorizations auths) {

        try {

            Map<Long, SchemaSnapshot> snapshots = new HashMap<Long, SchemaSnapshot>();

            BatchScanner scanner = connector.createBatchScanner(Constants.PROSPECTOR_TABLE, auths, 10);

            List<Range> ranges = new ArrayList<Range>();

            for(Long time : prospectTimes) {
                ranges.add(new Range(ProspectorMutationFactory.SCHEMA + ProspectorMutationFactory.DELIM + subject +
                            ProspectorMutationFactory.DELIM + time));
            }

            if(dataType != null) {

                IteratorSetting setting = new IteratorSetting(15, "regex", RegExFilter.class);
                setting.addOption(RegExFilter.COLQ_REGEX, dataType);

                scanner.addScanIterator(setting);
            }

            for(Map.Entry<Key,Value> entry : scanner) {

                Long prospectTime = entry.getKey().getTimestamp();
                String predicate = entry.getKey().getColumnFamily().toString();
                String type = entry.getKey().getColumnQualifier().toString();

                SchemaSnapshot snapshot = snapshots.get(prospectTime);
                if(snapshot == null) {
                    snapshot = new SchemaSnapshot(subject, prospectTime);
                }

                PredicateDescription description = new PredicateDescription(predicate,
                        entry.getKey().getColumnVisibility().toString(), type);

                snapshot.addPredicate(description);
            }

            return snapshots.values();

        } catch (TableNotFoundException e) {

            logger.error("The prospector table was not found");
        }

        return null;
    }

    public SchemaSnapshot getSchemaSnapshot(Long prospectTime, String typeAndScheme, String dataType, Authorizations auths) {
        Collection<SchemaSnapshot> snapshots =  getSchemaSnapshots(Arrays.asList(new Long[] { prospectTime }), typeAndScheme, dataType, auths);

        if(snapshots != null && snapshots.size() > 0) {
            return snapshots.iterator().next();
        }

        else {
            return null;
        }
    }

    public Iterator<String> getTypesContainingPredicate(Long prospectTime, String predicate, String dataType, Authorizations auths) {

        try {

            Map<Long, SchemaSnapshot> snapshots = new HashMap<Long, SchemaSnapshot>();

            Scanner scanner = connector.createScanner(Constants.PROSPECTOR_TABLE, auths);

            scanner.setRange(new Range(ProspectorMutationFactory.SCHEMA_REVERSE + ProspectorMutationFactory.DELIM +
                                       predicate + ProspectorMutationFactory.DELIM + prospectTime));


            if(dataType != null) {

                IteratorSetting setting = new IteratorSetting(15, "regex", RegExFilter.class);
                setting.addOption(RegExFilter.COLF_REGEX, dataType);

                scanner.addScanIterator(setting);
            }

            final Iterator<Map.Entry<Key,Value>> iterator = scanner.iterator();

            return new Iterator<String>() {

                String currentSubject;
                String nextSubject;

                public boolean hasNext() {

                    if(nextSubject == null) {

                        while(iterator.hasNext() && nextSubject.equals(currentSubject)) {

                            nextSubject = iterator.next().getKey().getColumnQualifier().toString();
                        }

                        if(!iterator.hasNext()) {
                            currentSubject = null;
                            return false;
                        }
                    }

                    return true;
                }

                public String next() {

                    String tmp = currentSubject;
                    currentSubject = nextSubject;
                    nextSubject = null;

                    return currentSubject;
                }

                public void remove() {
                    iterator.remove();
                }
            };

        } catch (TableNotFoundException e) {

            logger.error("The prospector table was not found");
        }

        return null;
    }

    public Collection<TripleIndexCount> getCountsForIndex(List<Long> prospectTimes, TripleValueType type, String index, String dataType, Authorizations auths) {

        List<TripleIndexCount> indexCounts = new ArrayList<TripleIndexCount>();

        try {

            BatchScanner scanner = connector.createBatchScanner(Constants.PROSPECTOR_TABLE, auths, 10);

            List<Range> ranges = new ArrayList<Range>();
            for(Long time : prospectTimes) {

                ranges.add(new Range(type + ProspectorMutationFactory.DELIM + index + ProspectorMutationFactory.DELIM + time));
            }

            IteratorSetting setting = new IteratorSetting(15, "regex", RegExFilter.class);
            setting.addOption(RegExFilter.COLF_REGEX, ProspectorMutationFactory.COUNT);
            setting.addOption(RegExFilter.COLQ_REGEX, dataType);

            scanner.addScanIterator(setting);

            for(Map.Entry<Key,Value> entry : scanner) {

                Long prospectTime = entry.getKey().getTimestamp();

                TripleIndexCount indexCount = new TripleIndexCount(type, index, Long.parseLong(new String(entry.getValue().get())), prospectTime);
                indexCounts.add(indexCount);
            }

        } catch (TableNotFoundException e) {

            logger.error("The prospector table was not found");
        }

        return indexCounts;
    }

    public TripleIndexCount getCountForIndex(Long prospectTime, TripleValueType type, String index, String dataType, Authorizations auths) {

        Collection<TripleIndexCount> indexCounts = getCountsForIndex(Arrays.asList(new Long[] { prospectTime }), type, index, dataType, auths);

        if(indexCounts.size() > 0) {

            return indexCounts.iterator().next();
        }

        return null;
    }

    public Iterator<TripleIndexDescription> getMatchesForPartialIndex(TripleValueType type, String partialIndex, String dataType, Authorizations auths) {

        return null;
    }
}
