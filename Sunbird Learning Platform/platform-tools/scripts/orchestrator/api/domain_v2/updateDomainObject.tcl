package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set object_null [java::isnull $object]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_DOMAIN_INVALID_OBJECT"
	$result_map put "message" "Invalid Request"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} else {
	set object_type_res [getDomainObjectType $type]
	set check_obj_type_error [check_response_error $object_type_res]
	if {$check_obj_type_error} {
		return $object_type_res
	} else {
		set object_type [get_resp_value $object_type_res "result"]
		set graph_id "domain"
		set resp_def_node [getDefinition $graph_id $object_type]
		set def_node [get_resp_value $resp_def_node "definition_node"]
		$object put "objectType" $object_type
		$object put "identifier" $object_id
		$object put "subject" $domain_id
		set domain_obj [convert_to_graph_node $object $def_node]
		set create_response [updateDataNode $graph_id $object_id $domain_obj]
		return $create_response
	}
}