package org.ekstep.graph.model.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ekstep.common.dto.Request;
import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.common.mgr.BaseGraphManager;
import org.ekstep.graph.dac.enums.SystemNodeTypes;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.exception.GraphRelationErrorCodes;

public class UsedBySetRelation extends AbstractRelation {

    public static final String RELATION_NAME = "usedBySet";

    public UsedBySetRelation(BaseGraphManager manager, String graphId, String startNodeId, String endNodeId) {
        super(manager, graphId, startNodeId, endNodeId);
    }

    @Override
    public String getRelationType() {
        return RELATION_NAME;
    }

    @Override
    public Map<String, List<String>> validateRelation(Request request) {
        try {
            List<String> futures = new ArrayList<String>();
            Node startNode = getNode(request, this.startNodeId);
            Node endNode = getNode(request, this.endNodeId);
            String startNodeMsg = null;
            
            if (null == startNode) {
            	startNodeMsg = "Start Node Id is invalid";
            } else {
                String nodeType = startNode.getNodeType();
                if (StringUtils.equals(SystemNodeTypes.METADATA_NODE.name(), nodeType)
                        || StringUtils.equals(SystemNodeTypes.RELATION_NODE.name(), nodeType)
                        || StringUtils.equals(SystemNodeTypes.VALUE_NODE.name(), nodeType)) {
					startNodeMsg = null;
                } else {
                	startNodeMsg = "Start Node " + startNodeId + " should be a Metadata Node, Relation Node or a Value Node";
                }
            }
            
            futures.add(startNodeMsg);
			String endNodeMsg = getNodeTypeFuture(this.endNodeId, endNode, new String[] { SystemNodeTypes.SET.name() });
            futures.add(endNodeMsg);
			return getMessageMap(futures);
        } catch (Exception e) {
            throw new ServerException(GraphRelationErrorCodes.ERR_RELATION_VALIDATE.name(), e.getMessage(), e);
        }
    }

}
