package org.ekstep.orchestrator.interpreter.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.RelationCriterion;
import org.ekstep.graph.dac.model.RelationCriterion.DIRECTION;
import org.ekstep.graph.dac.model.RelationFilter;
import org.ekstep.graph.dac.model.SearchConditions;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.dac.model.Sort;
import org.ekstep.orchestrator.interpreter.ICommand;

import tcl.lang.Command;
import tcl.lang.Interp;
import tcl.lang.TclException;
import tcl.lang.TclNumArgsException;
import tcl.lang.TclObject;
import tcl.pkg.java.ReflectObject;

public class CreateSearchCriteria implements ICommand, Command {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void cmdProc(Interp interp, TclObject[] argv) throws TclException {
        if (argv.length == 2) {
            try {
                TclObject tclObject = argv[1];
                Object obj = ReflectObject.get(interp, tclObject);
                Map<String, Object> map = (Map<String, Object>) obj;

                SearchCriteria sc = new SearchCriteria();
                List<Filter> filters = new ArrayList<Filter>();
                List<String> sortFields = new ArrayList<String>();
                String order = Sort.SORT_ASC;
                if (null != map && !map.isEmpty()) {
                    for (Entry<String, Object> entry : map.entrySet()) {
                        if (StringUtils.equalsIgnoreCase("objectType", entry.getKey())) {
                            String objectType = (String) map.get("objectType");
                            if (StringUtils.isNotBlank(objectType))
                                sc.setObjectType(objectType);
                        } else if (StringUtils.equalsIgnoreCase("nodeType", entry.getKey())) {
                            String nodeType = (String) map.get("nodeType");
                            if (StringUtils.isNotBlank(nodeType))
                                sc.setNodeType(nodeType);
                        } else if (StringUtils.equalsIgnoreCase("fields", entry.getKey())) {
                            List<String> fields = (List<String>) map.get("fields");
                            if (null != fields && !fields.isEmpty())
                                sc.setFields(fields);
                        } else if (StringUtils.equalsIgnoreCase("count", entry.getKey())) {
                            Boolean countQuery = (Boolean) map.get("count");
                            if (null != countQuery)
                                sc.setCountQuery(countQuery.booleanValue());
                        } else if (StringUtils.equalsIgnoreCase("resultSize", entry.getKey())) {
                            Integer resultSize = (Integer) map.get("resultSize");
                            if (null != resultSize && resultSize.intValue() > 0)
                                sc.setResultSize(resultSize);
                        } else if (StringUtils.equalsIgnoreCase("startPosition", entry.getKey())) {
                            Integer startPosition = (Integer) map.get("startPosition");
                            if (null != startPosition && startPosition.intValue() > 0)
                                sc.setStartPosition(startPosition);
                        }  else if (StringUtils.equalsIgnoreCase("status", entry.getKey())) {
                            List<String> list = getList(entry.getValue(), true);
                            if (null != list && !list.isEmpty())
                                filters.add(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
                        } else if (StringUtils.equalsIgnoreCase("sortBy", entry.getKey())) {
                            sortFields = getList(map.get("sortBy"), true);
                        } else if (StringUtils.equalsIgnoreCase("order", entry.getKey())) {
                            order = (String) map.get("order");
                        } else if (StringUtils.equalsIgnoreCase("filters", entry.getKey())) {
                            List<Map> list = (List<Map>) map.get("filters");
                            if (null != list && !list.isEmpty()) {
                                for (Map filterObj : list) {
                                    String strObject = mapper.writeValueAsString(filterObj);
                                    Filter dto = (Filter) mapper.readValue(strObject, Filter.class);
                                    if (null != dto)
                                        filters.add(dto);
                                }
                            }
                        } else if (StringUtils.equalsIgnoreCase("relationCriteria", entry.getKey())) {
                            List<Map> list = (List<Map>) map.get("relationCriteria");
                            if (null != list && !list.isEmpty()) {
                                for (Map relationMap : list) {
                                    String relation = (String) relationMap.get("name");
                                    List<RelationFilter> relations = (List<RelationFilter>) relationMap.get("filters");
                                    String objectType = (String) relationMap.get("objectType");
                                    RelationCriterion rc = null;
                                    if (null != relations && !relations.isEmpty()) {
                                        rc = new RelationCriterion(relations, objectType);
                                    } else if (StringUtils.isNotBlank(relation)) {
                                        rc = new RelationCriterion(relation, objectType);
                                    }
                                    if (null != rc) {
                                        List<String> identifiers = (List<String>) relationMap.get("identifiers");
                                        if (null != identifiers && !identifiers.isEmpty())
                                            rc.setIdentifiers(identifiers);
                                        Integer fromDepth = (Integer) relationMap.get("fromDepth");
                                        if (null != fromDepth)
                                            rc.setFromDepth(fromDepth);
                                        Integer toDepth = (Integer) relationMap.get("toDepth");
                                        if (null != toDepth)
                                            rc.setToDepth(toDepth);
                                        String direction = (String) relationMap.get("direction");
                                        if (StringUtils.isNotBlank(direction)) {
                                            try {
                                                rc.setDirection(DIRECTION.valueOf(direction.toUpperCase()));
                                            } catch(Exception e) {
                                            }
                                        }
                                        sc.addRelationCriterion(rc);
                                    }
                                }
                            }
                        } else {
                            Object val = entry.getValue();
                            List<String> list = getList(val, false);
                            if (null != list && !list.isEmpty())
                                filters.add(new Filter(entry.getKey(), SearchConditions.OP_IN, list));
                            else if (null != val && StringUtils.isNotBlank(val.toString())) {
                                if (val instanceof String) {
                                    filters.add(new Filter(entry.getKey(), SearchConditions.OP_LIKE, val.toString()));
                                } else {
                                    filters.add(new Filter(entry.getKey(), SearchConditions.OP_EQUAL, val));
                                }
                            }
                        }
                    }
                }
                if (null != filters && !filters.isEmpty()) {
                    MetadataCriterion mc = MetadataCriterion.create(filters);
                    sc.addMetadata(mc);
                }
                if (null != sortFields && !sortFields.isEmpty()) {
                    if (StringUtils.equalsIgnoreCase(Sort.SORT_DESC, order))
                        order = Sort.SORT_DESC;
                    else
                        order = Sort.SORT_ASC;
                    for (String sortField : sortFields)
                        sc.sort(new Sort(sortField, order));
                }
                TclObject tclResp = ReflectObject.newInstance(interp, sc.getClass(), sc);
                interp.setResult(tclResp);
            } catch (Exception e) {
                throw new TclException(interp, "Unable to read response: " + e.getMessage());
            }
        } else {
            throw new TclNumArgsException(interp, 1, argv, "Invalid arguments to check_response_error command");
        }
    }

    @Override
    public String getCommandName() {
        return "create_search_criteria";
    }

    private ObjectMapper mapper = new ObjectMapper();
    
    @SuppressWarnings("rawtypes")
    private List getList(Object object, boolean returnList) {
        if (null != object && StringUtils.isNotBlank(object.toString())) {
            try {
                String strObject = mapper.writeValueAsString(object);
                List list = mapper.readValue(strObject.toString(), List.class);
                return list;
            } catch (Exception e) {
                if (returnList) {
                    List<String> list = new ArrayList<String>();
                    list.add(object.toString());
                    return list;
                }
            }
        }
        return null;
    }

}
