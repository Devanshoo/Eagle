package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
java::import -package org.ekstep.graph.dac.model Node
set graph_id "domain"
set object_type "Content"
set resp_get_node [getDataNode $graph_id $content_id]
set check_error [check_response_error $resp_get_node]
set original_content_id $content_id
set isContentNull [java::isnull $content]
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
if {$check_error} {
	return $resp_get_node;
} else {
	set graph_node [get_resp_value $resp_get_node "node"]
	set node_object_type [java::prop $graph_node "objectType"]
	if {$node_object_type == $object_type} {
		set node_metadata [java::prop $graph_node "metadata"]
		set status_val [$node_metadata get "status"]
		set status_val_str [java::new String [$status_val toString]]
		set isReviewState [$status_val_str equalsIgnoreCase "Review"]
		set isLiveState [$status_val_str equalsIgnoreCase "Live"]
		set isUnlistedState [$status_val_str equalsIgnoreCase "Unlisted"]
		set isFlaggedState [$status_val_str equalsIgnoreCase "Flagged"]
		if {$isReviewState == 1} {
			set request [java::new HashMap]
			$request put "versionKey" [$node_metadata get "versionKey"]
			$request put "status" "Draft"
			$request put "objectType" $object_type
			$request put "identifier" $content_id
			if {$isContentNull == 0} {
				set rejectReasons [$content get "rejectReasons"]
				set rejectComment [$content get "rejectComment"]
				set isRejectReasonsNull [java::isnull $rejectReasons]
				if {$isRejectReasonsNull == 0} {
					set rejectReasons [java::cast ArrayList $rejectReasons]
					set hasRejectReasons [isNotEmpty $rejectReasons]
					if {$hasRejectReasons} {
						$request put "rejectReasons" $rejectReasons
					}
				}
				set isRejectCommentNull [java::isnull $rejectComment]
				if {$isRejectCommentNull == 0} {
					$request put "rejectComment" $rejectComment
				}
			}
			$request put "publishChecklist" [java::null]
			$request put "publishComment" [java::null]
			set resp_def_node [getDefinition $graph_id $object_type]
			set def_node [get_resp_value $resp_def_node "definition_node"]
			set domain_obj [convert_to_graph_node $request $def_node]
			set create_response [updateDataNode $graph_id $content_id $domain_obj]
			set check_error [check_response_error $create_response]
			return $create_response
		} elseif {$isLiveState == 1 || $isUnlistedState == 1 } {
			set content_image_id ${content_id}.img
			set resp_get_node [getDataNode $graph_id $content_image_id]
			set check_error [check_response_error $resp_get_node]
			if {$check_error} {
				set result_map [java::new HashMap]
				$result_map put "code" "ERR_CONTENT_NOT_IN_REVIEW"
				$result_map put "message" "Content $content_id is not in review state to reject"
				$result_map put "responseCode" [java::new Integer 400]
				set response_list [create_error_response $result_map]
				return $response_list
			} else {
				set image_node [get_resp_value $resp_get_node "node"]
				set image_metadata [java::prop $image_node "metadata"]
				set status_val [$image_metadata get "status"]
				set status_val_str [java::new String [$status_val toString]]
				set isReviewState [$status_val_str equalsIgnoreCase "Review"]
				if {$isReviewState == 1} {
					$image_metadata put "status" "Draft"
					if {$isContentNull == 0} {
						set rejectReasons [$content get "rejectReasons"]
						set rejectComment [$content get "rejectComment"]
						set isRejectReasonsNull [java::isnull $rejectReasons]
						if {$isRejectReasonsNull == 0} {
							set rejectReasons [java::cast ArrayList $rejectReasons]
							set hasRejectReasons [isNotEmpty $rejectReasons]
							if {$hasRejectReasons} {
								$image_metadata put "rejectReasons" $rejectReasons
							}
						}
						set isRejectCommentNull [java::isnull $rejectComment]
						if {$isRejectCommentNull == 0} {
							$image_metadata put "rejectComment" $rejectComment
						}
					}
					$image_metadata put "publishChecklist" [java::null]
					$image_metadata put "publishComment" [java::null]
					set create_image_response [updateDataNode $graph_id $content_image_id $image_node]
					set check_error [check_response_error $create_image_response]
					$create_image_response put "node_id" $original_content_id 
					return $create_image_response
				} else {
					set result_map [java::new HashMap]
					$result_map put "code" "ERR_CONTENT_NOT_IN_REVIEW"
					$result_map put "message" "Content $content_id is not in review state to reject"
					$result_map put "responseCode" [java::new Integer 400]
					set response_list [create_error_response $result_map]
					return $response_list
				}
			}
		}  elseif {$isFlaggedState == 1} {
			set content_image_id ${content_id}.img
			set resp_get_node [getDataNode $graph_id $content_image_id]
			set check_error [check_response_error $resp_get_node]
			if {$check_error} {
				set result_map [java::new HashMap]
				$result_map put "code" "ERR_CONTENT_NOT_IN_REVIEW"
				$result_map put "message" "Content $content_id is not in review state to reject"
				$result_map put "responseCode" [java::new Integer 400]
				set response_list [create_error_response $result_map]
				return $response_list
			} else {
				set image_node [get_resp_value $resp_get_node "node"]
				set image_metadata [java::prop $image_node "metadata"]
				set status_val [$image_metadata get "status"]
				set status_val_str [java::new String [$status_val toString]]
				set isReviewState [$status_val_str equalsIgnoreCase "FlagReview"]
				if {$isReviewState == 1} {
					$image_metadata put "status" "FlagDraft"
					if {$isContentNull == 0} {
						set rejectReasons [$content get "rejectReasons"]
						set rejectComment [$content get "rejectComment"]
						set isRejectReasonsNull [java::isnull $rejectReasons]
						if {$isRejectReasonsNull == 0} {
							set rejectReasons [java::cast ArrayList $rejectReasons]
							set hasRejectReasons [isNotEmpty $rejectReasons]
							if {$hasRejectReasons} {
								$image_metadata put "rejectReasons" $rejectReasons
							}
						}
						set isRejectCommentNull [java::isnull $rejectComment]
						if {$isRejectCommentNull == 0} {
							$image_metadata put "rejectComment" $rejectComment
						}
					}
					$image_metadata put "publishChecklist" [java::null]
					$image_metadata put "publishComment" [java::null]
					set create_image_response [updateDataNode $graph_id $content_image_id $image_node]
					set check_error [check_response_error $create_image_response]
					$create_image_response put "node_id" $original_content_id 
					return $create_image_response
				} else {
					set result_map [java::new HashMap]
					$result_map put "code" "ERR_CONTENT_NOT_IN_FLAG_REVIEW"
					$result_map put "message" "Content $content_id is not in flag review state to reject"
					$result_map put "responseCode" [java::new Integer 400]
					set response_list [create_error_response $result_map]
					return $response_list
				}
			}
		} else {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT_NOT_IN_REVIEW"
			$result_map put "message" "Content $content_id is not in review state to reject"
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