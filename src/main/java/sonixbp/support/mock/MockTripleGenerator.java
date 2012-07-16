package sonixbp.support.mock;


import sonixbp.domain.PredicateDescription;
import sonixbp.domain.Triple;

import java.util.*;

public class MockTripleGenerator {

    private int numTriples = 150;
    private List<String> typesAndSchemes = Arrays.asList(new String[] { "triple://artifact", "triple://person", "triple://place", "triple://thing", "triple://somethingElse" });
    private List<String> predicates = Arrays.asList(new String[] { "triple://name", "triple://description", "triple://location", "triple://activity", "triple://trend" });
    private List<String> visibilities = Arrays.asList(new String[] { "U", "U&FOUO", "FOUO", "TEST" });
    private List<String> objects = Arrays.asList(new String[] { "object1", "object2", "object3", "anotherObject1", "anObject1", "a fold in time" });
    private List<String> dataTypes = Arrays.asList(new String[] { "xsd:string", "xsd:integer"  });

    public MockTripleGenerator() { }

    public Collection<Triple> generateTriples() {

        List<Triple> triples = new ArrayList<Triple>();
        for(int i = 0; i < numTriples; i++) {

            Random random = new Random();
            int subjectIndex = random.nextInt(typesAndSchemes.size());
            int predicateIndex = random.nextInt(predicates.size());
            int objectIndex = random.nextInt(objects.size());
            int visIndex = random.nextInt(visibilities.size());
            int datatypeIndex = random.nextInt(dataTypes.size());

            String subject = typesAndSchemes.get(subjectIndex);
            String predicate = predicates.get(predicateIndex);
            String object = objects.get(objectIndex);
            String vis = visibilities.get(visIndex);
            String dataType = dataTypes.get(datatypeIndex);

            triples.add(new Triple(subject, predicate, object, dataType, vis));
        }

        return triples;
    }


    public int getNumTriples() {
        return numTriples;
    }

    public void setNumTriples(int numTriples) {
        this.numTriples = numTriples;
    }

    public List<String> getTypesAndSchemes() {
        return typesAndSchemes;
    }

    public void setTypesAndSchemes(List<String> typesAndSchemes) {
        this.typesAndSchemes = typesAndSchemes;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<String> predicates) {
        this.predicates = predicates;
    }

    public List<String> getVisibilities() {
        return visibilities;
    }

    public void setVisibilities(List<String> visibilities) {
        this.visibilities = visibilities;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    public List<String> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(List<String> dataTypes) {
        this.dataTypes = dataTypes;
    }
}
