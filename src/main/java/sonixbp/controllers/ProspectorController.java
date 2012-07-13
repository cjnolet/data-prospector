package sonixbp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sonixbp.TripleValueType;

import java.util.Collection;

@Controller()
public class ProspectorController {

    /**
     * Lists the prospects in storage (most recent will be first in list). Most recent is returned
     * if maxResults is null
     * @param maxResults
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String listProspects(@RequestParam(required = false) Integer maxResults) {

        if(maxResults == null) {
            return "no max results";
        }

        else {

            return Integer.toString(maxResults);
        }
    }

    /**
     * Gets a schema for a list of prospect times (or most recent prospect if no times specified)
     * @param prospectTimes
     * @return
     */
    @RequestMapping(value = "/schema", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getSchema(@RequestParam(required = false) Collection<Long> prospectTimes) {

        if(prospectTimes != null) {
            return "TIMES: " + prospectTimes.toString();
        }

        else {
            return "NO input times";
        }
    }

    /**
     * Gets a schema
     * @param prospectTimes
     * @return
     */
    @RequestMapping(value = "/schemaReverse", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getSchemaReverse(@RequestParam(required = false) Collection<Long> prospectTimes) {

        return "TIMES: " + prospectTimes.toString();
    }


    /**
     *
     * @param indexType
     * @param index
     * @return
     */
    @RequestMapping(value = "/matches", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getMatches(@RequestParam(required = false) TripleValueType indexType,
                             @RequestParam(required = true)  String index) {

        return "MATCHES";
    }

    /**
     *
     * @param indexType
     * @param index
     * @return
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String getTripleCount(@RequestParam(required = false) Collection<Long> prospectTimes,
                                 @RequestParam(required = true)  String index,
                                 @RequestParam(required = true)  TripleValueType indexType) {

        return "COUNT";
    }
}
