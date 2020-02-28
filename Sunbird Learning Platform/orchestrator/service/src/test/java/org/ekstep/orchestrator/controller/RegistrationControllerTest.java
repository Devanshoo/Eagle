package org.ekstep.orchestrator.controller;

import java.util.Random;

import org.ekstep.cassandra.CassandraTestSetup;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@FixMethodOrder(MethodSorters.DEFAULT)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({ "classpath:servlet-context.xml" })
public class RegistrationControllerTest extends CassandraTestSetup {

	@Autowired
	private WebApplicationContext context;
	MockMvc mockMvc;
	private ResultActions actions;

	private static String basePath = "/v1/orchestrator";

	int rn = generateRandomInt(0, 9999999);
	String scriptBodyJson = "{\"id\":null,\"name\":\"testScript_" + rn
			+ "\",\"apiId\":\"ekstep.language.wordlist.update\",\"version\":\"3.0\",\"description\":\"\",\"body\":\"package require java\\njava::import -package java.util ArrayList List\\njava::import -package java.util HashMap Map\\njava::import -package org.ekstep.graph.dac.model Node\\nset lemma_list [java::new ArrayList]\\nset object_type \\\"Word\\\"\\nset collection_type \\\"SET\\\"\\nset set_type \\\"WordList\\\"\\nset wordIds [java::new ArrayList]\\nset isWordNull [java::isnull $words]\\nif {$isWordNull == 0} {\\nset map [java::new HashMap]\\n$map put \\\"nodeType\\\" \\\"DATA_NODE\\\"\\n$map put \\\"objectType\\\" $object_type\\n$map put \\\"lemma\\\" $words\\nset search_criteria [create_search_criteria $map]\\nset search_response [searchNodes $language_id $search_criteria]\\nset check_error [check_response_error $search_response]\\nif {$check_error} {\\nreturn $search_response;\\n} else {\\nset graph_nodes [get_resp_value $search_response \\\"node_list\\\"]\\nset word_id_list [java::new ArrayList]\\njava::for {Node graph_node} $graph_nodes {\\nset word_id [java::prop $graph_node \\\"identifier\\\"]\\n$word_id_list add $word_id\\n}\\n$wordIds addAll $word_id_list\\nset resp [addMembers $language_id $wordlist_id $collection_type $word_id_list]\\nset check_error_add_member [check_response_error $resp]\\nif {$check_error_add_member} {\\nreturn $resp\\n}\\n}\\n}\\nset isRemoveWordsNull [java::isnull $removeWords]\\nif {$isRemoveWordsNull == 0} {\\nset rm_map [java::new HashMap]\\n$rm_map put \\\"nodeType\\\" \\\"DATA_NODE\\\"\\n$rm_map put \\\"objectType\\\" $object_type\\n$rm_map put \\\"lemma\\\" $removeWords\\nset rm_search_criteria [create_search_criteria $rm_map]\\nset rm_search_response [searchNodes $language_id $rm_search_criteria]\\nset rm_check_error [check_response_error $rm_search_response]\\nif {$rm_check_error} {\\nreturn $rm_search_response;\\n} else {\\nset rm_graph_nodes [get_resp_value $rm_search_response \\\"node_list\\\"]\\nset rm_word_id_list [java::new ArrayList]\\njava::for {Node rm_graph_node} $rm_graph_nodes {\\nset rm_word_id [java::prop $rm_graph_node \\\"identifier\\\"]\\n$rm_word_id_list add $rm_word_id\\n}\\n$wordIds addAll $rm_word_id_list\\nset rm_resp [removeMembers $language_id $wordlist_id $collection_type $rm_word_id_list]\\nset check_error_rm_member [check_response_error $rm_resp]\\nif {$check_error_rm_member} {\\nreturn $rm_resp\\n}\\n}\\n}\\nupdateWordListMembership $language_id $wordIds\\nset get_resp [getWordList $language_id $wordlist_id]\\nset get_resp_check_error [check_response_error $get_resp]\\nif {$get_resp_check_error} {\\nreturn $get_resp;\\n}\\nset word_list_node [get_resp_value $get_resp \\\"wordlist\\\"]\\nset words_list_obj [$word_list_node get \\\"words\\\"]\\nset words_list [java::cast ArrayList $words_list_obj]\\nset words_list_size [$words_list size]\\nif { $words_list_size > 0 } {\\nset up_map [java::new HashMap]\\n$up_map put \\\"nodeType\\\" \\\"DATA_NODE\\\"\\n$up_map put \\\"objectType\\\" $object_type\\n$up_map put \\\"lemma\\\" $words_list\\nset up_search_criteria [create_search_criteria $up_map]\\nset up_search_response [searchNodes $language_id $up_search_criteria]\\nset up_check_error [check_response_error $up_search_response]\\nif {$up_check_error} {\\nreturn $up_search_response;\\n} else {\\nset up_graph_nodes [get_resp_value $up_search_response \\\"node_list\\\"]\\nset up_word_id_list [java::new ArrayList]\\njava::for {Node up_graph_node} $up_graph_nodes {\\nset up_word_id [java::prop $up_graph_node \\\"identifier\\\"]\\n$up_word_id_list add $up_word_id\\n}\\nset set_node [java::new Node]\\njava::prop $set_node \\\"identifier\\\" $wordlist_id\\njava::prop $set_node \\\"metadata\\\" $metadata\\nset up_resp [updateSet $language_id $up_word_id_list $set_type $object_type $set_node]\\nset check_error_up_resp [check_response_error $up_resp]\\nreturn $up_resp\\n}\\n} else {\\nreturn \\\"Ok\\\"\\n}\",\"type\":\"SCRIPT\",\"cmdClass\":\"\",\"parameters\":[{\"name\":\"language_id\",\"datatype\":\"\",\"index\":0,\"routingParam\":true,\"routingId\":\"\"},{\"name\":\"wordlist_id\",\"datatype\":\"\",\"index\":1,\"routingParam\":true,\"routingId\":\"\"},{\"name\":\"words\",\"datatype\":\"java.util.ArrayList\",\"index\":2,\"routingParam\":false,\"routingId\":\"\"},{\"name\":\"metadata\",\"datatype\":\"java.util.HashMap\",\"index\":3,\"routingParam\":false,\"routingId\":\"\"},{\"name\":\"removeWords\",\"datatype\":\"java.util.ArrayList\",\"index\":4,\"routingParam\":false,\"routingId\":\"\"}],\"actorPath\":{\"manager\":null,\"operation\":null,\"router\":null},\"requestPath\":{\"type\":\"PATCH\",\"url\":\"/v3/wordlists/testScript_"
			+ rn
			+ "/*\",\"pathParams\":[\"wordlist_id\"],\"requestParams\":[\"language_id\"],\"bodyParams\":[]},\"async\":null}";
	String commandBodyJson = "{\"id\":null,\"name\":\"testCommand_" + rn
			+ "\",\"apiId\":\"\",\"version\":\"\",\"description\":\"\",\"body\":\"\",\"type\":\"COMMAND\",\"cmdClass\":\"org.ekstep.orchestrator.interpreter.command.ConvertToGraphNode\",\"parameters\":[],\"actorPath\":{\"manager\":null,\"operation\":null,\"router\":null},\"requestPath\":{\"type\":null,\"url\":null,\"pathParams\":null,\"requestParams\":null,\"bodyParams\":null},\"async\":null}";

	String scriptBodyJsonFail = "{\"id\":null,\"name\":\"\",\"apiId\":\"ekstep.language.wordlist.update\",\"version\":\"3.0\",\"description\":\"\",\"body\":\"package require java\\njava::import -package java.util ArrayList List\\njava::import -package java.util HashMap Map\\njava::import -package org.ekstep.graph.dac.model Node\\nset lemma_list [java::new ArrayList]\\nset object_type \\\"Word\\\"\\nset collection_type \\\"SET\\\"\\nset set_type \\\"WordList\\\"\\nset wordIds [java::new ArrayList]\\nset isWordNull [java::isnull $words]\\nif {$isWordNull == 0} {\\nset map [java::new HashMap]\\n$map put \\\"nodeType\\\" \\\"DATA_NODE\\\"\\n$map put \\\"objectType\\\" $object_type\\n$map put \\\"lemma\\\" $words\\nset search_criteria [create_search_criteria $map]\\nset search_response [searchNodes $language_id $search_criteria]\\nset check_error [check_response_error $search_response]\\nif {$check_error} {\\nreturn $search_response;\\n} else {\\nset graph_nodes [get_resp_value $search_response \\\"node_list\\\"]\\nset word_id_list [java::new ArrayList]\\njava::for {Node graph_node} $graph_nodes {\\nset word_id [java::prop $graph_node \\\"identifier\\\"]\\n$word_id_list add $word_id\\n}\\n$wordIds addAll $word_id_list\\nset resp [addMembers $language_id $wordlist_id $collection_type $word_id_list]\\nset check_error_add_member [check_response_error $resp]\\nif {$check_error_add_member} {\\nreturn $resp\\n}\\n}\\n}\\nset isRemoveWordsNull [java::isnull $removeWords]\\nif {$isRemoveWordsNull == 0} {\\nset rm_map [java::new HashMap]\\n$rm_map put \\\"nodeType\\\" \\\"DATA_NODE\\\"\\n$rm_map put \\\"objectType\\\" $object_type\\n$rm_map put \\\"lemma\\\" $removeWords\\nset rm_search_criteria [create_search_criteria $rm_map]\\nset rm_search_response [searchNodes $language_id $rm_search_criteria]\\nset rm_check_error [check_response_error $rm_search_response]\\nif {$rm_check_error} {\\nreturn $rm_search_response;\\n} else {\\nset rm_graph_nodes [get_resp_value $rm_search_response \\\"node_list\\\"]\\nset rm_word_id_list [java::new ArrayList]\\njava::for {Node rm_graph_node} $rm_graph_nodes {\\nset rm_word_id [java::prop $rm_graph_node \\\"identifier\\\"]\\n$rm_word_id_list add $rm_word_id\\n}\\n$wordIds addAll $rm_word_id_list\\nset rm_resp [removeMembers $language_id $wordlist_id $collection_type $rm_word_id_list]\\nset check_error_rm_member [check_response_error $rm_resp]\\nif {$check_error_rm_member} {\\nreturn $rm_resp\\n}\\n}\\n}\\nupdateWordListMembership $language_id $wordIds\\nset get_resp [getWordList $language_id $wordlist_id]\\nset get_resp_check_error [check_response_error $get_resp]\\nif {$get_resp_check_error} {\\nreturn $get_resp;\\n}\\nset word_list_node [get_resp_value $get_resp \\\"wordlist\\\"]\\nset words_list_obj [$word_list_node get \\\"words\\\"]\\nset words_list [java::cast ArrayList $words_list_obj]\\nset words_list_size [$words_list size]\\nif { $words_list_size > 0 } {\\nset up_map [java::new HashMap]\\n$up_map put \\\"nodeType\\\" \\\"DATA_NODE\\\"\\n$up_map put \\\"objectType\\\" $object_type\\n$up_map put \\\"lemma\\\" $words_list\\nset up_search_criteria [create_search_criteria $up_map]\\nset up_search_response [searchNodes $language_id $up_search_criteria]\\nset up_check_error [check_response_error $up_search_response]\\nif {$up_check_error} {\\nreturn $up_search_response;\\n} else {\\nset up_graph_nodes [get_resp_value $up_search_response \\\"node_list\\\"]\\nset up_word_id_list [java::new ArrayList]\\njava::for {Node up_graph_node} $up_graph_nodes {\\nset up_word_id [java::prop $up_graph_node \\\"identifier\\\"]\\n$up_word_id_list add $up_word_id\\n}\\nset set_node [java::new Node]\\njava::prop $set_node \\\"identifier\\\" $wordlist_id\\njava::prop $set_node \\\"metadata\\\" $metadata\\nset up_resp [updateSet $language_id $up_word_id_list $set_type $object_type $set_node]\\nset check_error_up_resp [check_response_error $up_resp]\\nreturn $up_resp\\n}\\n} else {\\nreturn \\\"Ok\\\"\\n}\",\"type\":\"SCRIPT\",\"cmdClass\":\"\",\"parameters\":[{\"name\":\"language_id\",\"datatype\":\"\",\"index\":0,\"routingParam\":true,\"routingId\":\"\"},{\"name\":\"wordlist_id\",\"datatype\":\"\",\"index\":1,\"routingParam\":true,\"routingId\":\"\"},{\"name\":\"words\",\"datatype\":\"java.util.ArrayList\",\"index\":2,\"routingParam\":false,\"routingId\":\"\"},{\"name\":\"metadata\",\"datatype\":\"java.util.HashMap\",\"index\":3,\"routingParam\":false,\"routingId\":\"\"},{\"name\":\"removeWords\",\"datatype\":\"java.util.ArrayList\",\"index\":4,\"routingParam\":false,\"routingId\":\"\"}],\"actorPath\":{\"manager\":null,\"operation\":null,\"router\":null},\"requestPath\":{\"type\":\"PATCH\",\"url\":\"/v3/wordlists/testScript_"
			+ rn
			+ "/*\",\"pathParams\":[\"wordlist_id\"],\"requestParams\":[\"language_id\"],\"bodyParams\":[]},\"async\":null}";
	String commandBodyJsonFail = "{\"id\":null,\"name\":\"\",\"apiId\":\"\",\"version\":\"\",\"description\":\"\",\"body\":\"\",\"type\":\"COMMAND\",\"cmdClass\":\"org.ekstep.orchestrator.interpreter.command.ConvertToGraphNode\",\"parameters\":[],\"actorPath\":{\"manager\":null,\"operation\":null,\"router\":null},\"requestPath\":{\"type\":null,\"url\":null,\"pathParams\":null,\"requestParams\":null,\"bodyParams\":null},\"async\":null}";

	static String query_1 = "CREATE KEYSPACE IF NOT EXISTS script_store WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}";
	static String query_2 = "CREATE TABLE IF NOT EXISTS script_store.script_data (name text, type text, reqmap text, PRIMARY KEY (name))";

	@BeforeClass
	public static void setup() throws Exception {
		executeScript(query_1, query_2);
	}

	@Test
	public void testRegisterScriptWithValidName() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String path = basePath + "/register/script";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(scriptBodyJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testRegisterCommandWithValidName() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String path = basePath + "/register/command";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(commandBodyJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testRegisterScriptWithBlankName() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String path = basePath + "/register/script";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(scriptBodyJsonFail));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testRegisterCommandWithBlankName() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String path = basePath + "/register/command";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(commandBodyJsonFail));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetScriptWithValidId() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String path = basePath + "/register/script";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(scriptBodyJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

		String id = "";
		if (actions.andReturn().getResponse().getStatus() == 200)
			id = "testScript_" + rn;
		path = basePath + "/" + id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetScriptWithBlankId() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String id = "";
		String path = basePath + "/" + id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(500, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testGetScriptWithWrongId() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String path = basePath + "/register/script";
		actions = mockMvc.perform(
				MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(scriptBodyJson));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

		String id = "";
		if (actions.andReturn().getResponse().getStatus() == 200)
			id = "testScript_" + rn + "_no";
		path = basePath + "/" + id;
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, actions.andReturn().getResponse().getStatus());
	}

	@Test
	public void testExecuteRegisteredScript() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

		String json = "{\"id\":null,\"name\":\"getDomainObjectType\",\"apiId\":\"\",\"version\":\"\",\"description\":\"\",\"body\":\"return 'ABC'\",\"type\":\"SCRIPT\",\"cmdClass\":\"\",\"parameters\":[{\"name\":\"type\",\"datatype\":\"\",\"index\":0,\"routingParam\":false,\"routingId\":\"\"}],\"actorPath\":{\"manager\":null,\"operation\":null,\"router\":null},\"requestPath\":{\"type\":\"POST\",\"url\":\"/v1/exec/getDomainObjectType\",\"pathParams\":[],\"requestParams\":[],\"bodyParams\":[]},\"async\":null}";
		String path = "/v1/orchestrator/register/script";
		actions = mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(json));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = basePath + "/load/commands";
		actions = mockMvc.perform(MockMvcRequestBuilders.get(path).contentType(MediaType.APPLICATION_JSON));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());

		path = "/v1/exec/getDomainObjectType";
		json = "{\"type\": \"methods\"}";
		actions = mockMvc
				.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content(json));
		Assert.assertEquals(200, actions.andReturn().getResponse().getStatus());
	}

	public int generateRandomInt(int min, int max) {
		Random random = new Random();
		int randomInt = random.nextInt((max - min) + 1) + min;
		return randomInt;

	}
}
