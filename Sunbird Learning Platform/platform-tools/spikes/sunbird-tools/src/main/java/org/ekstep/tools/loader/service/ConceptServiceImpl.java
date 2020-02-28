/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ekstep.tools.loader.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.tools.loader.utils.Constants;
import org.ekstep.tools.loader.utils.JsonUtil;
import org.ekstep.tools.loader.utils.RestUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.BaseRequest;

/**
 *
 * @author feroz
 */
public class ConceptServiceImpl implements ConceptService {

    private static final Logger logger = LogManager.getLogger(ConceptServiceImpl.class);

	/**
	 * 
	 */
	public ConceptServiceImpl(ExecutionContext context) {
		init(context);
	}

	private void init(ExecutionContext context) {
		RestUtil.init(context, Constants.EKSTEP_API_TOKEN);
    }
        
    @Override
    public String create(JsonObject concept, ExecutionContext context) throws Exception {
        
		String createUrl = context.getString(Constants.API_CONCEPT_CREATE);
        String conceptType = JsonUtil.getFromObject(concept,"conceptType");
		if (conceptType.equalsIgnoreCase("dimension"))
			createUrl = context.getString(Constants.API_DIMENSION_CREATE);
        
        String conceptId = JsonUtil.getFromObject(concept,"identifier");
        String framework = JsonUtil.getFromObject(concept,"framework");
        
        // Set the parent object by searching for parent identifier
        setParent(concept, context);
        
        String body = JsonUtil.wrap(concept, "object").toString();
        BaseRequest request = Unirest.post(createUrl).routeParam("domain", framework).body(body);
        HttpResponse<JsonNode> createResponse = RestUtil.execute(request);
        
        if (RestUtil.isSuccessful(createResponse)) {
            conceptId = RestUtil.getFromResponse(createResponse, "result.node_id");
            concept.addProperty("identifier", conceptId);
            concept.addProperty("response", "OK");
            logger.debug("Created Concept " + conceptId);
        }
        else {
            
            String error = RestUtil.getFromResponse(createResponse, "params.errmsg");
            concept.addProperty("response", error);
            logger.debug("Create Concept Failed : " + error);
        }
        
        return conceptId;
    }

    @Override
    public String update(JsonObject concept, ExecutionContext context) throws Exception {
		String updateUrl = context.getString(Constants.API_CONCEPT_UPDATE);
        String conceptType = JsonUtil.getFromObject(concept,"conceptType");
		if (conceptType.equalsIgnoreCase("dimension"))
			updateUrl = context.getString(Constants.API_DIMENSION_UPDATE);
        
        // Set the parent object by searching for parent identifier
        setParent(concept, context);
        
        String conceptId = JsonUtil.getFromObject(concept,"identifier");
        String framework = JsonUtil.getFromObject(concept,"framework");
		concept.remove("identifier");
		concept.remove("framework");
        String body = JsonUtil.wrap(concept, "object").toString();
        
        BaseRequest request = Unirest.patch(updateUrl)
                .routeParam("domain", framework)
                .routeParam("concept_id", conceptId)
                .body(body);
        HttpResponse<JsonNode> updateResponse = RestUtil.execute(request);
        
        if (RestUtil.isSuccessful(updateResponse)) {
            conceptId = RestUtil.getFromResponse(updateResponse, "result.node_id");
            concept.addProperty("identifier", conceptId);
			concept.addProperty("framework", framework);
            concept.addProperty("response", "OK");
            logger.debug("Updated Concept " + conceptId);
        }
        else {
			conceptId = null;
            String error = RestUtil.getFromResponse(updateResponse, "params.errmsg");
            concept.addProperty("response", error);
            logger.debug("Update Concept Failed : " + error);
        }
        
        return conceptId;
    }

    @Override
	public String retire(String conceptId, ExecutionContext context, String domain) throws Exception {
		String retireUrl = context.getString(Constants.API_CONCEPT_RETIRE);
		JsonObject reqBody = new JsonObject();
		reqBody.add("request", new JsonObject());
		String body = reqBody.toString();
        
		BaseRequest retireRequest = Unirest.delete(retireUrl).routeParam("domain", domain)
				.routeParam("concept_id",
				conceptId).body(body);
        HttpResponse<JsonNode> retireResponse = RestUtil.execute(retireRequest);
        String response = "OK";
        if (RestUtil.isSuccessful(retireResponse)) {
			logger.debug("Retired " + conceptId);
            response = "OK";
        }
        else {
            response = RestUtil.getFromResponse(retireResponse, "params.errmsg");
            logger.debug("Failed to retire concept : " + response);
        }

        return response;
    }
    
    public static JsonObject setParent(JsonObject concept, ExecutionContext context) throws Exception {
        String parentConcept = JsonUtil.getFromObject(concept,"parent_code");
        if (StringUtils.isNotBlank(parentConcept)) {
            parentConcept = getConceptByCode(context, parentConcept);
            JsonArray array = new JsonArray();
            JsonObject pobj = new JsonObject();
            pobj.addProperty("identifier", parentConcept);
            array.add(pobj);
            concept.add("parent", array);
        }
        return concept;
    }
    
    public static String getConceptByCode(ExecutionContext context, String code) throws Exception {
        
		String searchUrl = context.getString(Constants.API_CONCEPT_SEARCH);
        String query = "{\"request\":{\"filters\":{\"objectType\":[\"Concept\", \"Dimension\"],\"code\":\"" + code +  "\", \"status\":[\"Live\", \"Draft\", \"Retired\"]},\"fields\":[\"name\",\"identifier\"],\"limit\":1,\"offset\":0}}";
        
        BaseRequest request = Unirest.post(searchUrl).body(query);
        HttpResponse<JsonNode> searchResponse = RestUtil.execute(request);
        
        try {
            
            JSONObject obj = searchResponse.getBody().getObject();
            JSONObject res = obj.getJSONObject("result");
            
            JSONArray parents;
            if (res.has("dimensions")) {
                parents = res.getJSONArray("dimensions");
            }
            else {
                parents = res.getJSONArray("concepts");
            }
            
            JSONObject first = parents.getJSONObject(0);
            String conceptId = first.getString("identifier");
            
            logger.debug("Found Concept " + conceptId);
            return conceptId;        
        }
        catch (Exception ex) {
            throw new NullPointerException("Concept not found: " + code + ". " + ex.getMessage());
        }
    }
}
