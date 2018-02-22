package com.maxdemarzi;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class ReplicantTest {

    private static final ObjectMapper mapper = new ObjectMapper();

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
        assertEquals(actual.get("a1").get("email").get("address").getTextValue(), "max@neo4j.com");
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.replicant({email:'max@neo4j.com', addresses:['9641 Sunset Blvd Beverly Hills CA 90210'], phone:'3102762251'}) yield value return value")));


    @Test
    public void testReplicantExistingPhone() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY2);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        JsonNode actual = response.get("results").get(0).get("data").get(0).get("row").get(0);
        assertEquals(actual.get("a1").get("phone").get("number").getTextValue(), "3125137509");
    }

    private static final Map QUERY2 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.replicant({email:'some@email.com', addresses:['9641 Sunset Blvd Beverly Hills CA 90210'], phone:'3125137509'}) yield value return value")));



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

            "CREATE (add1:Address { line1:'175 North Harbor Dr.', city:'Chicago', state:'IL', zip:'60605', lat: 41.886069, lon:-87.61567, geohash:'dp3wq0z'})" +
            "CREATE (add2:Address { line1:'520 South State St.', city:'Chicago', state:'IL', zip:'60605', lat:41.875418, lon:-87.627636 , geohash:'dp3wjzp'})" +
            "CREATE (add3:Address { line1:'111 E 5th Avenue', city:'San Mateo', state:'CA', zip:'94401', lat:37.562841, lon:-122.322973, geohash:'9q9j8qp'})" +
            "CREATE (add4:Address { line1:'3900 Yorktowne Blvd', city:'Port Orange', state:'FL', zip:'32129', lat:29.113762, lon:-81.027617, geohash:'djnms5v'})" +

            "CREATE (z1:Zip { code: '60605' })" +
            "CREATE (z2:Zip { code: '94401' })" +
            "CREATE (z3:Zip { code: '32129' })" +

            "CREATE (a1)-[:HAS_EMAIL]->(e1)" +
            "CREATE (a2)-[:HAS_EMAIL]->(e2)" +
            "CREATE (a3)-[:HAS_EMAIL]->(e3)" +

            "CREATE (a1)-[:HAS_PHONE]->(p1)" +
            "CREATE (a2)-[:HAS_PHONE]->(p2)" +
            "CREATE (a3)-[:HAS_PHONE]->(p3)" +

            "CREATE (add1)-[:HAS_ZIP]->(z1)" +
            "CREATE (add2)-[:HAS_ZIP]->(z1)" +
            "CREATE (add3)-[:HAS_ZIP]->(z2)" +
            "CREATE (add4)-[:HAS_ZIP]->(z3)" +

            "CREATE (a1)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add1)" +
            "CREATE (a2)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add2)" +
            "CREATE (a3)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add3)" +
            "CREATE (a1)-[:HAS_ADDRESS { billing:true, shipping:true, current:false }]->(add4)"

            ;
}
