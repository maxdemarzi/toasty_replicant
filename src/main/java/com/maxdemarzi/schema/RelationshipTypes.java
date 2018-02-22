package com.maxdemarzi.schema;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    HAS_ADDRESS,
    HAS_EMAIL,
    HAS_PHONE,
    IN_ZIP,
}
