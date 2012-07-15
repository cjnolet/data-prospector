package sonixbp.services.impl;

import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.user.RegExFilter;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonixbp.TripleValueType;
import sonixbp.domain.*;
import sonixbp.services.ProspectService;
import sonixbp.support.Constants;
import sonixbp.support.ProspectorMutationFactory;

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

            System.out.println(prospectTimes);
            for(Long time : prospectTimes) {
                ranges.add(new Range(ProspectorMutationFactory.SCHEMA + ProspectorMutationFactory.DELIM + subject +
                            ProspectorMutationFactory.DELIM + time));
            }

            scanner.setRanges(ranges);

            if(dataType != null) {

                IteratorSetting setting = new IteratorSetting(15, "regex", RegExFilter.class);
                setting.addOption(RegExFilter.COLQ_REGEX, dataType);

                scanner.addScanIterator(setting);
            }

            for(Map.Entry<Key,Value> entry : scanner) {

                Long prospectTime = entry.getKey().getTimestamp();
                String predicate = entry.getKey().getColumnFamily().toString();
                String type = entry.getKey().getColumnQualifier().toString();

                System.out.println(entry);

                SchemaSnapshot snapshot = snapshots.get(prospectTime);
                if(snapshot == null) {
                    snapshot = new SchemaSnapshot(subject, prospectTime);
                    snapshots.put(prospectTime, snapshot);
                }

                PredicateDescription description = new PredicateDescription(predicate,
                        entry.getKey().getColumnVisibility().toString(), type);

                snapshot.addPredicate(description);
            }

            // Give a consistent result with at least the prospect times that were sent in- even if nothing exists
            for(Long time : prospectTimes) {

                if(snapshots.get(time) == null) {
                    snapshots.put(time, new SchemaSnapshot(subject, time));
                }
            }

            List<SchemaSnapshot> finalSnapshots = new ArrayList<SchemaSnapshot>();
            finalSnapshots.addAll(snapshots.values());

            Collections.sort(finalSnapshots);

            return finalSnapshots;

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

            final Scanner scanner = connector.createScanner(Constants.PROSPECTOR_TABLE, auths);

            scanner.setRange(new Range(ProspectorMutationFactory.SCHEMA_REVERSE + ProspectorMutationFactory.DELIM +
                                       predicate + ProspectorMutationFactory.DELIM + prospectTime));

            if(dataType != null) {

                IteratorSetting setting = new IteratorSetting(15, "regex", RegExFilter.class);
                setting.addOption(RegExFilter.COLQ_REGEX, dataType);

                scanner.addScanIterator(setting);
            }

            final Iterator<Map.Entry<Key,Value>> iterator = scanner.iterator();

            return new Iterator<String>() {

                String currentSubject;
                String nextSubject;

                boolean hasNext;

                public boolean hasNext() {

                    if(nextSubject == null) {

                        while(iterator.hasNext() && (nextSubject == null || nextSubject.equals(currentSubject))) {

                            nextSubject = iterator.next().getKey().getColumnFamily().toString();
                        }

                        if(nextSubject != null && !nextSubject.equals(currentSubject)) {

                            hasNext = true;
                        }

                        else {

                            hasNext =  false;
                        }

                    }

                    return hasNext;
                }

                public String next() {

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

    public TripleIndexCountDescription getCountsForIndex(List<Long> prospectTimes, TripleValueType type, String index, String dataType, Authorizations auths) {

        Validate.notNull(type);
        Validate.notNull(prospectTimes);
        Validate.notEmpty(prospectTimes);
        Validate.notNull(index);
        Validate.notNull(dataType);
        Validate.notNull(auths);

        Map<Long, TripleIndexCount> indexCounts = new HashMap<Long, TripleIndexCount>();
        TripleIndexCountDescription description = new TripleIndexCountDescription(index, type,  dataType);

        try {

            BatchScanner scanner = connector.createBatchScanner(Constants.PROSPECTOR_TABLE, auths, 10);

            List<Range> ranges = new ArrayList<Range>();
            for(Long time : prospectTimes) {

                ranges.add(new Range(type + ProspectorMutationFactory.DELIM + index + ProspectorMutationFactory.DELIM + time));
            }

            scanner.setRanges(ranges);

            IteratorSetting setting = new IteratorSetting(15, "regex", RegExFilter.class);
            setting.addOption(RegExFilter.COLF_REGEX, ProspectorMutationFactory.COUNT);
            setting.addOption(RegExFilter.COLQ_REGEX, dataType);
            scanner.addScanIterator(setting);

            for(Map.Entry<Key,Value> entry : scanner) {

                Long prospectTime = entry.getKey().getTimestamp();
                indexCounts.put(prospectTime, new TripleIndexCount(Long.parseLong(new String(entry.getValue().get())), prospectTime));
            }

        } catch (TableNotFoundException e) {

            logger.error("The prospector table was not found");
        }

        for(Long time : prospectTimes) {

            TripleIndexCount count = indexCounts.get(time);
            if(count == null) {
                description.addIndexCount(new TripleIndexCount(0, time));
            }

            else {
                description.addIndexCount(count);
            }
        }

        Collections.sort(description.getCounts());
        return description;
    }

    public TripleIndexCountDescription getCountForIndex(Long prospectTime, TripleValueType type, String index, String dataType, Authorizations auths) {

        return getCountsForIndex(Arrays.asList(new Long[] { prospectTime }), type, index, dataType, auths);
    }

    public Iterator<TripleIndexDescription> getMatchesForPartialIndex(TripleValueType type, String partialIndex, String dataType, Authorizations auths) {

        try {

            final Scanner scanner = connector.createScanner(Constants.PROSPECTOR_TABLE, auths);

            scanner.setRange(new Range(ProspectorMutationFactory.INDEX + ProspectorMutationFactory.DELIM + partialIndex,
                    ProspectorMutationFactory.INDEX + ProspectorMutationFactory.DELIM + partialIndex + "\uffff"));

            IteratorSetting setting = new IteratorSetting(15, "regex", RegExFilter.class);


            if(type != null) {

                setting.addOption(RegExFilter.COLF_REGEX, type.toString());
            }

            if(dataType != null) {

                setting.addOption(RegExFilter.COLQ_REGEX, dataType);
            }

            scanner.addScanIterator(setting);
            final Iterator<Map.Entry<Key,Value>> iterator = scanner.iterator();

            return new Iterator<TripleIndexDescription>() {

                public boolean hasNext() {

                    return iterator.hasNext();
                }

                public TripleIndexDescription next() {

                    Map.Entry<Key,Value> entry = iterator.next();
                    String theIndex = entry.getKey().getRow().toString().split(ProspectorMutationFactory.DELIM)[1];

                    return new TripleIndexDescription(theIndex, TripleValueType.valueOf(entry.getKey().getColumnFamily().toString()),
                            entry.getKey().getColumnQualifier().toString());
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
}
