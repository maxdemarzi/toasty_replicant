package com.maxdemarzi;

import org.codehaus.jackson.JsonNode;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class ReplicantTest {

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(Replicant.class);

    @Test
    public void testReplicantExistingEmail() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        JsonNode actual = response.get("results").get(0).get("data").get(0).get("row").get(0);
        assertEquals(actual.get(0).get("address").getTextValue(), "max@neo4j.com");
        assertEquals(actual.get(1).get("id").getTextValue(), "a1");
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.replicant({email:'max@neo4j.com', addresses:['9641 Sunset Blvd Beverly Hills CA 90210'], phone:'3102762251'}) yield nodes return nodes")));

    @Test
    public void testReplicantExistingPhone() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY2);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        JsonNode actual = response.get("results").get(0).get("data").get(0).get("row").get(0);
        assertEquals(actual.get(0).get("number").getTextValue(), "3125137509");
        assertEquals(actual.get(1).get("id").getTextValue(), "a1");
    }

    private static final Map QUERY2 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.replicant({email:'some@email.com', addresses:['9641 Sunset Blvd Beverly Hills CA 90210'], phone:'3125137509'}) yield nodes return nodes")));

    @Test
    public void testReplicantExistingAddress() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY3);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        JsonNode actual = response.get("results").get(0).get("data").get(0).get("row").get(0);
        assertEquals(actual.get(0).get("geohash").getTextValue(), "dp3wq");
        assertEquals(actual.get(1).get("id").getTextValue(), "a1");
    }

    private static final Map QUERY3 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.replicant({email:'some@email.com', addresses:['175 North Harbor Dr. Chicago IL 60605'], phone:'1235550000'}) yield nodes return nodes")));

    @Test
    public void testReplicantTypoAddress() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY4);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        JsonNode actual = response.get("results").get(0).get("data").get(0).get("row").get(0);
        assertEquals(actual.get(0).get("geohash").getTextValue(), "dp3wq");
        assertEquals(actual.get(1).get("id").getTextValue(), "a1");
    }

    private static final Map QUERY4 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.replicant({email:'some@email.com', addresses:['175 Norht Hrabro Dr. Chicago IL 60605'], phone:'1235550000'}) yield nodes return nodes")));

    private static final String MODEL_STATEMENT =
            "CREATE (a1:Account { id: 'a1' })" +
            "CREATE (a2:Account { id: 'a2' })" +
            "CREATE (a3:Account { id: 'a3' })" +
            "CREATE (e1:Email { address: 'max@neo4j.com' })" +
            "CREATE (e2:Email { address: 'maxdemarzi@gmail.com' })" +
            "CREATE (e3:Email { address: 'maxdemarzi@hotmail.com' })" +
            "CREATE (p1:Phone { number: '3125137509' })" +
            "CREATE (p2:Phone { number: '3128675309' })" +
            "CREATE (p3:Phone { number: '3125551234' })" +

            "CREATE (add1:Address { line1:'175 North Harbor Dr.', city:'Chicago', state:'IL', zip:'60605', lat: 41.886069, lon:-87.61567, geohash:'dp3wq'})" +
            "CREATE (add2:Address { line1:'520 South State St.', city:'Chicago', state:'IL', zip:'60605', lat:41.875418, lon:-87.627636 , geohash:'dp3wj'})" +
            "CREATE (add3:Address { line1:'111 E 5th Avenue', city:'San Mateo', state:'CA', zip:'94401', lat:37.562841, lon:-122.322973, geohash:'9q9j8'})" +
            "CREATE (add4:Address { line1:'3900 Yorktowne Blvd', city:'Port Orange', state:'FL', zip:'32129', lat:29.113762, lon:-81.027617, geohash:'djnms'})" +

            "CREATE (a1)-[:HAS_EMAIL]->(e1)" +
            "CREATE (a2)-[:HAS_EMAIL]->(e2)" +
            "CREATE (a3)-[:HAS_EMAIL]->(e3)" +

            "CREATE (a1)-[:HAS_PHONE]->(p1)" +
            "CREATE (a2)-[:HAS_PHONE]->(p2)" +
            "CREATE (a3)-[:HAS_PHONE]->(p3)" +

            "CREATE (a1)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add1)" +
            "CREATE (a2)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add2)" +
            "CREATE (a3)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add3)" +
            "CREATE (a1)-[:HAS_ADDRESS { billing:true, shipping:true, current:false }]->(add4)"

            ;
}
