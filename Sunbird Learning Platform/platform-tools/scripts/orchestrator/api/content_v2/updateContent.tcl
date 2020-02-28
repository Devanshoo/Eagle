package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map Date
java::import -package java.util HashSet Set
java::import -package org.ekstep.graph.dac.model Node Relation
java::import -package org.ekstep.graph.common DateUtils

proc proc_isNotNull {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 0} {
			set exist true
		}
	} catch {Exception err} {
    	set exist false
	}
	return $exist
}

proc proc_isEmpty {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 1} {
			set exist true
		} else {
			set strValue [$value toString]
			set newStrValue [java::new String $strValue]
			set strLength [$newStrValue length]
			if {$strLength == 0} {
				set exist true
			}
		}
	} catch {Exception err} {
    	set exist true
	}
	return $exist
}

proc proc_isNotEmpty {relations} {
	set exist false
	set hasRelations [java::isnull $relations]
	if {$hasRelations == 0} {
		set relationsSize [$relations size]
		if {$relationsSize > 0} {
			set exist true
		}
	}
	return $exist
}

set object_null [java::isnull $content]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_CONTENT_INVALID_OBJECT"
	$result_map put "message" "Invalid Request"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} else {
	set original_content_id $content_id
	set object_type "Content"
	set content_image_object_type "ContentImage"
	set graph_id "domain"
	set resp_def_node [getDefinition $graph_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	$content put "objectType" $object_type
	$content put "identifier" $content_id

	set osId_Error false
	set contentType [$content get "contentType"]
	set contentTypeNotNull [proc_isNotNull $contentType]
	set contentTypeEmpty false
	if {$contentTypeNotNull} {
		set contentTypeEmpty [proc_isEmpty $contentType]
	}
	if {!$contentTypeEmpty} {
		set isImageObjectCreationNeeded 0
		set imageObjectExists 0
		set osId [$content get "osId"]
		set osIdNotNull [proc_isNotNull $osId]
		set osIdEmpty false
		if {$osIdNotNull} {
			set osIdEmpty [proc_isEmpty $osId]
		}
		set osIdCheck 1
		if {$contentTypeNotNull} {
			set osIdCheck [[java::new String [$contentType toString]] equalsIgnoreCase "Game"]
		}
		if {$osIdCheck == 1 && $osIdEmpty} {
			set osId_Error false
		}
		if {$osId_Error} {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT_INVALID_OSID"
			$result_map put "message" "OSId cannot be empty"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
		} else {
			set content_image_id ${content_id}.img
			set get_node_response [getDataNode $graph_id $content_image_id]
			set get_node_response_error [check_response_error $get_node_response]
			if {$get_node_response_error} {
				set isImageObjectCreationNeeded 1
				set get_node_response [getDataNode $graph_id $content_id]
				set get_node_response_error [check_response_error $get_node_response]
			} else {
				set imageObjectExists 1
			}
			if {$get_node_response_error} {
				return $get_node_response;
			} else {
				set externalProps [java::new HashMap]
				set body [$content get "body"]
				set bodyEmpty [proc_isEmpty $body]
				if {!$bodyEmpty} {
					$externalProps put "body" $body
					$content put "artifactUrl" [java::null]
					$content put "body" [java::null]
				}
				set oldBody [$content get "oldBody"]
				set oldBodyEmpty [proc_isEmpty $oldBody]
				if {!$oldBodyEmpty} {
					$externalProps put "oldBody" $oldBody
					$content put "oldBody" [java::null]
				}
				set stageIcons [$content get "stageIcons"]
				set stageIconsEmpty [proc_isEmpty $stageIcons]
				if {!$stageIconsEmpty} {
					$content put "stageIcons" [java::null]
					$externalProps put "stageIcons" $stageIcons
				}

				set graph_node [get_resp_value $get_node_response "node"]
				set metadata [java::prop $graph_node "metadata"]
				set mimeType [$metadata get "mimeType"]
				set domain_val [$metadata get "domain"]
				set domain_val_null [java::isnull $domain_val]
				if {$domain_val_null == 0} {
					set domain_val_instance [java::instanceof $domain_val {String[]}]
					if {$domain_val_instance == 0} {
						set input_domain [$content get "domain"]
						set input_domain_null [java::isnull $input_domain]
						if {$input_domain_null == 1} {
							set domain_list [java::new ArrayList]
							$domain_list add $domain_val
							$content put "domain" $domain_list
						}
					}
				}
				set audience_val [$metadata get "audience"]
				set audience_val_null [java::isnull $audience_val]
                                if {$audience_val_null == 0} {
					set audience_val_instance [java::instanceof $audience_val {String[]}]                       
					if {$audience_val_instance == 0} {
						set audience_list [java::new ArrayList]
						$audience_list add $audience_val
						$metadata put "audience" $audience_list
						set input_audience [$content get "audience"]
						set input_audience_null [java::isnull $input_audience]
						if {$input_audience_null == 1} {
							$content put "audience" $audience_list
						}
					}
				}

				set status_val [$metadata get "status"]
				set status_val_str [java::new String [$status_val toString]]
				set isReviewState [$status_val_str equalsIgnoreCase "Review"]
				set isFlaggedReviewState [$status_val_str equalsIgnoreCase "FlagReview"]
				set isFlaggedState [$status_val_str equalsIgnoreCase "Flagged"]
				set isLiveState [$status_val_str equalsIgnoreCase "Live"]
				set input_status [$content get "status"]
				set input_status_null [java::isnull $input_status]
				set log_event 0
				if {$input_status_null == 0} {
					set input_status_str [java::new String [$input_status toString]]
					set updateToReviewState [$input_status_str equalsIgnoreCase "Review"]
					set updateToFlagReviewState [$input_status_str equalsIgnoreCase "FlagReview"]
					if {( $updateToReviewState == 1 || $updateToFlagReviewState == 1 ) && ( $isReviewState != 1 || $isFlaggedReviewState != 1 )} {
						$content put "lastSubmittedOn" [java::call DateUtils format [java::new Date]]
					}
					if {![$input_status_str equals $status_val_str]} {
						set log_event 1
					}
				}
				set check_error false
				set create_response [java::null]
				if {$isLiveState == 1 || $isFlaggedState == 1} {
					if {$isImageObjectCreationNeeded == 1} {
						java::prop $graph_node "identifier" $content_image_id	
						java::prop $graph_node "objectType" $content_image_object_type
						#if {$isFlaggedState == 1} {
							#$metadata put "status" "FlagDraft"	
						#} else {
							$metadata put "status" "Draft"
						#}
						set lastUpdatedBy [$content get "lastUpdatedBy"]
						set isLastUpdateNotNull [proc_isNotNull $lastUpdatedBy]
                        if {$isLastUpdateNotNull} {
							$metadata put "lastUpdatedBy" $lastUpdatedBy
						}
						set create_response [createDataNode $graph_id $graph_node]
						set check_error [check_response_error $create_response]
						if {!$check_error} {
							set externalPropFields [java::new ArrayList]
							$externalPropFields add "body"
							$externalPropFields add "oldBody"
							$externalPropFields add "stageIcons"
							set bodyResponse [getContentProperties $content_id $externalPropFields]
							set check_error [check_response_error $bodyResponse]
							if {!$check_error} {
								set extValues [get_resp_value $bodyResponse "values"]
								set is_extValues_null [java::isnull $extValues]
								if {$is_extValues_null == 0} {
									set extValuesMap [java::cast Map $extValues]
									set bodyResponse [updateContentProperties $content_image_id $extValuesMap]
								}
							}
							$content put "versionKey" [get_resp_value $create_response "versionKey"]
						}
					}
					set content_id $content_image_id
				} elseif {$imageObjectExists == 1} {
					set content_id $content_image_id
				}
				if {$check_error} {
					return $create_response
				} else {
					set domain_obj [convert_to_graph_node $content $def_node $graph_node]
					set create_response [updateDataNode $graph_id $content_id $domain_obj]
					set check_error [check_response_error $create_response]
					if {$check_error} {
						return $create_response
					} else {
						$create_response put "node_id" $original_content_id
						if {$log_event == 1} {
							$metadata putAll $content
							$metadata put "prevState" $status_val_str
							set log_response [log_content_lifecycle_event $original_content_id $metadata]
						}
						if {!$bodyEmpty || !$oldBodyEmpty} {
							set bodyResponse [updateContentProperties $content_id $externalProps]
							set check_error [check_response_error $bodyResponse]
							if {$check_error} {
								return $bodyResponse
							} else {
								return $create_response
							}
						} else {
							return $create_response
						}
					}
				}
			}
		}
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_INVALID_CONTENT_TYPE"
		$result_map put "message" "Content Type cannot be empty"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}
