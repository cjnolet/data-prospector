package sonixbp.support;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.ColumnVisibility;
import org.apache.hadoop.io.Text;
import sonixbp.TripleValueType;
import sonixbp.domain.Triple;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 *  R				                        CF				 CQ				V

    TOP LEVEL INDEX (shows all available [unserialized] subject/predicate/objects)
    index\x00//subject		                subject			dataType
    index\x00//predicate	                predicate		dataType
    index\x00//object		                object			dataType

    AGGREGATED/COUNT INDEX (shows counts for a specific prospect time)
    subject\x00//subject\x00revIndexDate  	count	        dataType		1
    predicate\x00//predicate\x00revIdxDate  count	        dataType		1
    object\x00//object\x00revIdxDate        count           dataType		1

    SCHEMA INDEX (shows schema snapshot for prospect time)
    schema\x00//subject\x00rid		        //predicate		dataType
    schemaReverse\x00//predicate\x00rid	    dataType	 	//subject

    METADATA
    metadata                                prospectDate    revIdxDate
 */
public class ProspectorMutationFactory {

    private static Value EMPTY = new Value("".getBytes());
    public static String DELIM = "\u0000";

    public static String INDEX = "index";
    public static String COUNT = "count";
    public static String SCHEMA = "schema";
    public static String SCHEMA_REVERSE = "schemaReverse";
    public static String METADATA = "metadata";
    public static String PROSPECT_TIME = "prospectTime";

    final Triple triple;
    final long prospectTime;

    final String schemeAndType;

    public ProspectorMutationFactory(Triple triple, long prospectTime) {
        this.triple = triple;
        this.prospectTime = prospectTime;

        schemeAndType = triple.getSubject().split("#")[0];
    }

    public Collection<Mutation> buildTopLevelIndexRows() {

        Mutation subjectMutation = new Mutation(INDEX + DELIM + schemeAndType);
        Mutation predicateMutation = new Mutation(INDEX + DELIM + triple.getPredicate());
        Mutation objectMutation = new Mutation(INDEX + DELIM + triple.getObject());

        subjectMutation.put(new Text(TripleValueType.subject.toString()), new Text(triple.getType()), new ColumnVisibility(triple.getVisibility()), prospectTime, EMPTY);
        predicateMutation.put(new Text(TripleValueType.predicate.toString()), new Text(triple.getType()), new ColumnVisibility(triple.getVisibility()), prospectTime, EMPTY);
        objectMutation.put(new Text(TripleValueType.object.toString()), new Text(triple.getType()), new ColumnVisibility(triple.getVisibility()), prospectTime, EMPTY);

        return Arrays.asList(new Mutation[] { subjectMutation, predicateMutation, objectMutation });
    }

    public Collection<Mutation> buildAggregatedIndexRows(long count) {
        Mutation subjectMutation = new Mutation(TripleValueType.subject + DELIM + schemeAndType + DELIM + prospectTime);
        Mutation predicateMutation = new Mutation(TripleValueType.predicate + DELIM + triple.getPredicate() + DELIM + prospectTime);
        Mutation objectMutation = new Mutation(TripleValueType.object + DELIM + triple.getObject() + DELIM + prospectTime);

        subjectMutation.put(new Text(COUNT), new Text("xsd:uri"), new ColumnVisibility(triple.getVisibility()), prospectTime, buildCountValue(count));
        predicateMutation.put(new Text(COUNT), new Text("xsd:uri"), new ColumnVisibility(triple.getVisibility()), prospectTime, buildCountValue(count));
        objectMutation.put(new Text(COUNT), new Text(triple.getType()), new ColumnVisibility(triple.getVisibility()), prospectTime, buildCountValue(count));

        return Arrays.asList(new Mutation[] { subjectMutation, predicateMutation, objectMutation});
    }

    public Collection<Mutation> buildSchemaRows() {

        Mutation schemaMutation = new Mutation(SCHEMA + DELIM + triple.getSubject() + DELIM + prospectTime);
        Mutation reverseSchemaMutation = new Mutation(SCHEMA_REVERSE + DELIM + triple.getPredicate() + DELIM + prospectTime);

        schemaMutation.put(new Text(triple.getPredicate()), new Text(triple.getType()), new ColumnVisibility(triple.getVisibility()), prospectTime, EMPTY);
        reverseSchemaMutation.put(new Text(schemeAndType), new Text(triple.getType()), new ColumnVisibility(triple.getVisibility()), prospectTime, EMPTY);

        return Arrays.asList(new Mutation[] { schemaMutation, reverseSchemaMutation });
    }

    public static Mutation buildProspectTimeRow(long prospectTime) {

        Mutation prospectMutation = new Mutation(METADATA);
        prospectMutation.put(new Text(PROSPECT_TIME), new Text(Long.toString(prospectTime)), new ColumnVisibility(""), prospectTime, EMPTY);

        return prospectMutation;
    }

    private Value buildCountValue(long count) {
        return new Value(String.format("%d", count).getBytes());
    }
}
