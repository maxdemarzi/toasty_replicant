package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class ReplicantTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(Replicant.class);

    @Test
    public void testTraversal() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.replicant() yield path return path")));

    private static final String MODEL_STATEMENT =
            "CREATE (a1:Account { id: 'a1' })" +
            "CREATE (a2:Account { id: 'a2' })" +
            "CREATE (a3:Account { id: 'a3' })" +
            "CREATE (a4:Account { id: 'a4' })" +
            "CREATE (add1:Address {line1:'175 North Harbor Dr.', city:'Chicago', state:'IL', zip:'60605'})" +
            "CREATE (add2:Address {line1:'17 East State St.', city:'Chicago', state:'IL', zip:'60605' })" +
            "CREATE (add3:Address {line1:'111 E 5th Avenue', city:'San Mateo', state:'CA', zip:'94401'})" +
            "CREATE (z1:Zip { code: '60605' })" +
            "CREATE (z2:Zip { code: '94401' })";
}
