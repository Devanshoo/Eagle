package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList


proc getCount {graph_id object_type domain_id} {

	set map [java::new HashMap]
	$map put "nodeType" "DATA_NODE"
	$map put "objectType" $object_type
	$map put "subject" $domain_id
	$map put "status" "Live"
	set search_criteria [create_search_criteria $map]
	set search_response [getNodesCount $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		return 0
	} else {
		set count [get_resp_value $search_response "count"]
		return $count
	}
}

proc getContentCount {graph_id domain_id} {

	set map [java::new HashMap]
	$map put "nodeType" "DATA_NODE"
	$map put "objectType" "Content"

	set domains [java::new ArrayList]
	$domains add $domain_id

	$map put "domain" $domains
	$map put "status" "Live"

	set contentTypes [java::new ArrayList]
	$contentTypes add "Story"
	$contentTypes add "Worksheet"
	$contentTypes add "Game"
	$contentTypes add "Collection"
	$map put "contentType" $contentTypes

	set search_criteria [create_search_criteria $map]
	set search_response [getNodesCount $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		return 0
	} else {
		set count [get_resp_value $search_response "count"]
		return $count
	}
}

set object_type "Domain"
set graph_id "domain"
set resp_get_node [getDataNode $graph_id $domain_id]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
	set returnFields false
	set is_fieldList_null [java::isnull $fields]
	if {$is_fieldList_null == 0} {
		set returnFields true
	}
	set result_map [java::new HashMap]
	java::try {
		set graph_node [get_resp_value $resp_get_node "node"]
		set resp_def_node [getDefinition $graph_id $object_type]
		set def_node [get_resp_value $resp_def_node "definition_node"]
		if {$returnFields} {
			set resp_object [convert_graph_node $graph_node $def_node $fields]
		} else {
			set resp_object [convert_graph_node $graph_node $def_node]
		}
		set dimension_count [getCount $graph_id "Dimension" $domain_id]
		set concept_count [getCount $graph_id "Concept" $domain_id]
		set misconception_count [getCount $graph_id "Misconception" $domain_id]
		set method_count [getCount $graph_id "Method" $domain_id]
		set content_count [getContentCount $graph_id $domain_id]

		$resp_object put "dimension_count" $dimension_count
		$resp_object put "concept_count" $concept_count
		$resp_object put "misconception_count" $misconception_count
		$resp_object put "method_count" $method_count
		$resp_object put "content_count" $content_count
	
		$result_map put "domain" $resp_object
	} catch {Exception err} {
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}