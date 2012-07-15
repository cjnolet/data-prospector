package sonixbp.controllers;

import org.apache.accumulo.core.security.Authorizations;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sonixbp.TripleValueType;
import sonixbp.domain.SchemaSnapshot;
import sonixbp.domain.TripleIndexCount;
import sonixbp.domain.TripleIndexDescription;
import sonixbp.services.ProspectService;

import java.io.IOException;
import java.util.*;

@Controller()
public class ProspectorController {

    private static final Integer MAX_RESULTS = 50;

    ProspectService service;
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setProspectService(ProspectService service) {

        this.service = service;
    }

    public ProspectorController() {
        objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);    }


    /**
     * Lists the prospects in storage (most recent will be first in list). Most recent is returned
     * if maxResults is null
     * @param maxResults
     * @param startTime
     * @param stopTime
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public String listProspects(@RequestParam(required = false) Integer maxResults,
                                @RequestParam(required = false) Long startTime,
                                @RequestParam(required = false) Long stopTime) throws IOException {

        // by default, if they pass in a stop time without a start time, return the past 24 hours.
        if(startTime == null && stopTime != null) {
            startTime = stopTime - (1000 * 60 * 60 * 24);
        }

        else if(startTime != null && stopTime == null) {
            stopTime = System.currentTimeMillis();
        }

        System.out.println(String.format("%d-%d", startTime, stopTime));


        Iterator<Long> prospectIter = startTime == null ? service.getProspects(new Authorizations()) :
                service.getProspectsInRange(startTime, stopTime, new Authorizations());

        Collection<Long> prospects = new ArrayList<Long>();

        if(maxResults == null) {

            if(prospectIter.hasNext()) {
                prospects.add(prospectIter.next());
            }
        }

        else {

            int count = 0;
            while(count < maxResults && prospectIter.hasNext()) {

                prospects.add(prospectIter.next());
                count++;
            }
        }

        return objectMapper.writeValueAsString(prospects);
    }

    /**
     * Gets a schema for a list of prospect times (or most recent prospect if no times specified)
     * Optional datatype will only return predicates of that type.
     * @param prospectTimes
     * @param schemeAndType
     * @param dataType
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/schema", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getSchema(@RequestParam(required = false) List<Long> prospectTimes,
                            @RequestParam(required = true)  String schemeAndType,
                            @RequestParam(required = false) String dataType) throws IOException {

        Collection<SchemaSnapshot> snapshots = new ArrayList<SchemaSnapshot>();
        if(prospectTimes != null) {

            System.out.println("PROSPECTTIMES: " + prospectTimes);
            snapshots.addAll(service.getSchemaSnapshots(prospectTimes, schemeAndType, dataType, new Authorizations()));
        }

        else {

            Iterator<Long> prospectIter = service.getProspects(new Authorizations());
            if(prospectIter.hasNext()) {
                snapshots.add(service.getSchemaSnapshot(prospectIter.next(), schemeAndType, dataType, new Authorizations()));
            }
        }

        return objectMapper.writeValueAsString(snapshots);
    }

    /**
     * Returns the scheme/type pairs that contain a specific predicate. Datatype is optional but should be included
     * if predicate-in-question's value is assumed to be a type other than string literal or uri literal.
     * @param prospectTime
     * @param maxResults
     * @param predicate
     * @param dataType
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/schemaReverse", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getSchemaReverse(@RequestParam(required = false) Long prospectTime,
                                   @RequestParam(required = false) Integer maxResults,
                                   @RequestParam(required = true)  String predicate,
                                   @RequestParam(required = false) String dataType) throws IOException {

        if(prospectTime == null) {

            prospectTime = service.getProspects(new Authorizations()).next();
        }

        Iterator<String> subjs = service.getTypesContainingPredicate(prospectTime, predicate, dataType, new Authorizations());

        Collection<String> subjects = new ArrayList<String>();
        if(maxResults == null) {

            maxResults = MAX_RESULTS;
        }

        int count = 0;
        while(count < maxResults && subjs.hasNext()) {
            subjects.add(subjs.next());
            count++;
        }

        return objectMapper.writeValueAsString(subjects);
    }


    /**
     * Performs a fuzzy match on
     * @param indexType
     * @param index
     * @param maxResults
     * @param dataType
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/matches", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getMatches(@RequestParam(required = false) TripleValueType indexType,
                             @RequestParam(required = true)  String index,
                             @RequestParam(required = false) Integer maxResults,
                             @RequestParam(required = false)  String dataType) throws IOException {

        if(maxResults == null) {
            maxResults = MAX_RESULTS;
        }

        Iterator<TripleIndexDescription> indexItr = service.getMatchesForPartialIndex(indexType, index, dataType, new Authorizations());

        Collection<TripleIndexDescription> descriptions = new ArrayList<TripleIndexDescription>();

        int count = 0;
        while(count < maxResults && indexItr.hasNext()) {

            descriptions.add(indexItr.next());
            count++;
        }

        return objectMapper.writeValueAsString(descriptions);
    }

    /**
     *
     * @param prospectTimes
     * @param index
     * @param indexType
     * @return
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getTripleCount(@RequestParam(required = false) List<Long> prospectTimes,
                                 @RequestParam(required = true)  String index,
                                 @RequestParam(required = true)  TripleValueType indexType,
                                 @RequestParam(required = false) String dataType) throws IOException {

        if(prospectTimes == null) {

            prospectTimes = Arrays.asList(new Long[]{service.getProspects(new Authorizations()).next()});
        }

        // subjects & predicates are ALL uri
        if(indexType.equals(TripleValueType.subject) || indexType.equals(TripleValueType.predicate)) {
            dataType = "xsd:uri";
        }

        else {

            // string literal assumed for default
            if(dataType == null) {
                dataType = "xsd:string";
            }
        }

        TripleIndexDescription counts = service.getCountsForIndex(prospectTimes, indexType, index, dataType, new Authorizations());

        return objectMapper.writeValueAsString(counts);
    }
}
