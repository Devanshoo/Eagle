package require java
java::import -package java.util HashMap Map Date
java::import -package java.util ArrayList List
java::import -package java.util HashSet Set
java::import -package java.util Arrays
java::import -package org.ekstep.graph.dac.model Node
java::import -package org.ekstep.graph.common DateUtils

set graph_id "domain"
set object_type "Content"

proc isNotEmpty {list} {
	set exist false
	set isListNull [java::isnull $list]
	if {$isListNull == 0} {
		set listSize [$list size]
		if {$listSize > 0} {
			set exist true
		}
	}
	return $exist
}

proc addFlagReasons {flagResaons node_metadata} {
	set existingFlagReasons [$node_metadata get "flagReasons"]	
	set isExistingFlagReasonsNull [java::isnull $existingFlagReasons]
	if {$isExistingFlagReasonsNull == 0} {
		set arr_instance [java::instanceof $existingFlagReasons {String[]}]
		if {$arr_instance == 1} {
			set existingFlagReasons [java::cast {String[]} $existingFlagReasons]
			set existingFlagReasons [java::call Arrays asList $existingFlagReasons]
		} else {
			set existingFlagReasons [java::cast ArrayList $existingFlagReasons]
		}

		if {[isNotEmpty $existingFlagReasons]} {
			set flagReasonsSet [java::new HashSet $existingFlagReasons]
			$flagReasonsSet addAll $flagResaons
			set flagReasonsList [java::new ArrayList $flagReasonsSet]
			return $flagReasonsList
		}
	}

	return $flagResaons
}

proc addFlaggedBy {flaggedBy node_metadata} {
	set flaggedByList [java::new ArrayList]
	$flaggedByList add $flaggedBy
	set existingFlaggedBy [$node_metadata get "flaggedBy"]	
	set isExistingFlaggedByNull [java::isnull $existingFlaggedBy]
	if {$isExistingFlaggedByNull == 0} {
		set arr_instance [java::instanceof $existingFlaggedBy {String[]}]
		if {$arr_instance == 1} {
			set existingFlaggedBy [java::cast {String[]} $existingFlaggedBy]
			set existingFlaggedBy [java::call Arrays asList $existingFlaggedBy]
		} else {
			set existingFlaggedBy [java::cast ArrayList $existingFlaggedBy]
		}
		if {[isNotEmpty $existingFlaggedBy]} {
			set flaggedBySet [java::new HashSet $existingFlaggedBy]
			$flaggedBySet addAll $flaggedByList
			set flaggedByList [java::new ArrayList $flaggedBySet]
			return $flaggedByList
		}
	}

	return $flaggedByList
}

set resp_get_node [getDataNode $graph_id $content_id]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
	set graph_node [get_resp_value $resp_get_node "node"]
	set node_object_type [java::prop $graph_node "objectType"]
	if {$node_object_type == $object_type} {
		set node_metadata [java::prop $graph_node "metadata"]
		set status_val [$node_metadata get "status"]
		set status_val_str [java::new String [$status_val toString]]
		set isLiveState [$status_val_str equalsIgnoreCase "Live"]
		set isProcessingState [$status_val_str equalsIgnoreCase "Processing"]
		set isFlaggedState [$status_val_str equalsIgnoreCase "Flagged"]
		if {$isLiveState == 1 || $isFlaggedState == 1 || $isProcessingState == 1} {
			set request [java::new HashMap]              
			set flaggedList [addFlaggedBy $flaggedBy $node_metadata]
		        set flaggedList [java::cast ArrayList $flaggedList]
		        set arraySize [$flaggedList size]
            		if {($arraySize > 0)} {
            			$request put "lastUpdatedBy" [$flaggedList get 0]
			}
           		$request put "flaggedBy" [addFlaggedBy $flaggedBy $node_metadata]
			$request put "flags" $flags
			$request put "versionKey" $versionKey
			$request put "status" "Flagged"
			$request put "lastFlaggedOn" [java::call DateUtils format [java::new Date]]
			set isFlagReasonsNull [java::isnull $flagReasons]
			if {$isFlagReasonsNull == 0} {
				set flagReasons [java::cast ArrayList $flagReasons]
				set hasFlagReasons [isNotEmpty $flagReasons]
				if {$hasFlagReasons} {
					set flagResaons [addFlagReasons $flagReasons $node_metadata]
					$request put "flagReasons" $flagResaons
				}
			}
			$request put "objectType" $object_type
			$request put "identifier" $content_id
			set resp_def_node [getDefinition $graph_id $object_type]
			set def_node [get_resp_value $resp_def_node "definition_node"]
			set domain_obj [convert_to_graph_node $request $def_node]
			set create_response [updateDataNode $graph_id $content_id $domain_obj]
			set check_error [check_response_error $create_response]
			if {$check_error} {
			} else {
				
				$node_metadata putAll $request
				$node_metadata put "prevState" $status_val_str
				set log_response [log_content_lifecycle_event $content_id $node_metadata]
			}
			return $create_response
		} else {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT_NOT_FLAGGABLE"
			$result_map put "message" "Unpublished Content $content_id cannot be flagged"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
		}
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_NODE_NOT_FOUND"
		$result_map put "message" "$object_type $content_id not found"
		$result_map put "responseCode" [java::new Integer 404]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}
