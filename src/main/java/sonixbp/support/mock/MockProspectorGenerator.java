package sonixbp.support.mock;


import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.iterators.AggregatingIterator;
import org.apache.accumulo.core.iterators.aggregation.StringSummation;
import org.apache.accumulo.core.security.Authorizations;
import sonixbp.domain.Triple;
import sonixbp.support.Constants;
import sonixbp.support.ProspectorMutationFactory;

import java.util.Collection;
import java.util.Map;

public class MockProspectorGenerator {


    private final Connector connector;
    private final Collection<Triple> triples;
    private final Long prospectTime;

    public MockProspectorGenerator(Connector connector, Long prospectTime, Collection<Triple> triples) {
        this.connector = connector;
        this.prospectTime = prospectTime;
        this.triples = triples;
    }

    public MockProspectorGenerator(Connector connector, Long prospectTime, MockTripleGenerator generator) {

        this(connector, prospectTime, generator.generateTriples());

        if(!connector.tableOperations().exists(Constants.PROSPECTOR_TABLE)) {
            try {
                connector.tableOperations().create(Constants.PROSPECTOR_TABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

   private void generateMockProspect() {

       try {
           BatchWriter writer = connector.createBatchWriter(Constants.PROSPECTOR_TABLE, 10000L, 100000L, 10);
           for(Triple triple : triples) {

               try {
                   ProspectorMutationFactory factory = new ProspectorMutationFactory(triple, prospectTime);

                   /**
                    * Persist top-level index rows
                    */
                   Collection<Mutation> indexMutations = factory.buildTopLevelIndexRows();
                   for(Mutation m : indexMutations) {

                       writer.addMutation(m);
                   }

                   /**
                    * Persist Aggregated index rows
                    */
                   Collection<Mutation> aggregatedMutations = factory.buildAggregatedIndexRows(1);
                   for(Mutation m : aggregatedMutations) {
                       writer.addMutation(m);
                   }

                   /**
                    * Persist schema mutations
                    */
                   Collection<Mutation> schemaMutations = factory.buildSchemaRows();
                   for(Mutation m : schemaMutations) {
                       writer.addMutation(m);
                   }

                   writer.addMutation(ProspectorMutationFactory.buildProspectTimeRow(prospectTime));

                   writer.flush();

               } catch(MutationsRejectedException e) {
                   e.printStackTrace();
               }
           }

           debugTable();

       } catch (TableNotFoundException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       }
   }

    private void debugTable() {

        try {
            Scanner scanner = connector.createScanner(Constants.PROSPECTOR_TABLE, new Authorizations());

            for(Map.Entry<Key,Value> entry : scanner) {
                System.out.println(entry);
            }
        } catch (TableNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
