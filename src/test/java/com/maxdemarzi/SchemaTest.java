package com.maxdemarzi;

import com.maxdemarzi.schema.Schema;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.server.rest.domain.JsonParseException;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

public class SchemaTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withProcedure(Schema.class);

    @Test
    public void shouldCreateSchema() throws JsonParseException {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        ArrayList<String> actual = new ArrayList<>();
        ArrayList<HashMap<String, ArrayList<String>>> parsedResponse = mapper.convertValue(response.get("results").get(0).get("data"), ArrayList.class);
        parsedResponse.forEach(entry -> {actual.add(entry.get("row").get(0));});
        Assert.assertEquals(expected, actual);
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.schema.generate() yield value return value")));

    private static final ArrayList expected = new ArrayList<String>() {{
        add("(:Account {id}) constraint created");
        add("(:Address {geohash}) index created");
        add("(:Email {address}) constraint created");
        add("(:Phone {number}) constraint created");
        add("(:Zip {code}) constraint created");
        add("Schema Created");
    }};
}