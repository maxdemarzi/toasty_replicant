package com.maxdemarzi;

import ch.hsr.geohash.GeoHash;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.maxdemarzi.results.NodeListResult;
import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.json.JSONObject;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.DamerauLevenshtein;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class Replicant {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    private static final AddressSimplifier addressSimplifier = new AddressSimplifier();
    private static final StringMetric metric = new DamerauLevenshtein();

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    @Procedure(name = "com.maxdemarzi.replicant", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.replicant(facts)")
    public Stream<NodeListResult> findReplicant(@Name("facts") Map<String, Object> facts) throws IOException, UnirestException {

        Map<Node, List<Node>> results = new HashMap<>();
        Node email = db.findNode(Labels.Email, "address", facts.get("email"));

        if (email != null) {
            email.getRelationships(Direction.INCOMING, RelationshipTypes.HAS_EMAIL)
                    .forEach(relationship -> {
                            results.put(relationship.getStartNode(), new ArrayList<Node>(){{ add( email); }});
                    });
        }

        Node phone = db.findNode(Labels.Phone, "number", facts.get("phone"));

        if (phone != null) {
            phone.getRelationships(Direction.INCOMING, RelationshipTypes.HAS_PHONE)
                    .forEach(relationship -> {
                        List<Node> reasons;
                        if (results.containsKey(relationship.getStartNode())) {
                            reasons = results.get(relationship.getStartNode());
                        } else {
                            reasons =  new ArrayList<>();
                        }
                        reasons.add( phone);
                        results.put(relationship.getStartNode(), reasons);
                    });
        }

        ArrayList<String> encodedAddresses = new ArrayList<>();
        ArrayList<String> addresses = (ArrayList<String>) facts.get("addresses");
        addresses.forEach(address -> {
            try {
                encodedAddresses.add(URLEncoder.encode(address, StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        });

        HashMap<String, Node> possibleAddresses = new HashMap<>();

        for (String encodedAddress : encodedAddresses ) {
            HttpResponse<JsonNode> postResponse = Unirest.get("http://www.datasciencetoolkit.org/street2coordinates/" + encodedAddress)
                    .asJson();

            String key = postResponse.getBody().getObject().keySet().iterator().next();
            JSONObject data = (JSONObject)postResponse.getBody().getObject().get(key);
            Double latitude = data.getDouble("latitude");
            Double longitude = data.getDouble("longitude");

            // A GeoHash of precision 5 is ±2.4 km
            GeoHash hash = GeoHash.withCharacterPrecision(latitude, longitude, 5);

            // Get the addresses at this geohash location
            Iterator<Node> iter = db.findNodes(Labels.Address, "geohash", hash.toBase32());
            while (iter.hasNext()) {
                Node node = iter.next();
                possibleAddresses.put( getAddress(node), node);
            }

            // Check the neighboring location just in case
            GeoHash[] adjacent = hash.getAdjacent();
            for (GeoHash adjacentHash : adjacent) {
                Iterator<Node> iterAdj = db.findNodes(Labels.Address, "geohash", adjacentHash.toBase32());
                while (iterAdj.hasNext()) {
                    Node node = iterAdj.next();
                    possibleAddresses.put( getAddress(node), node);
                }
            }
        }

        // Now that we have our list of potential addresses, clean them and check them for typos

        for (String address : addresses) {
            String simplifiedAddress = addressSimplifier.simplify(address);
            for (Map.Entry<String, Node> entry : possibleAddresses.entrySet()) {
                Float score = metric.compare(simplifiedAddress, addressSimplifier.simplify(entry.getKey()));
                if (score > 0.90) {
                    entry.getValue().getRelationships(Direction.INCOMING, RelationshipTypes.HAS_ADDRESS)
                    .forEach(relationship -> {
                        List<Node> reasons;
                        if (results.containsKey(relationship.getStartNode())) {
                            reasons = results.get(relationship.getStartNode());
                        } else {
                            reasons = new ArrayList<>();
                        }
                        reasons.add( entry.getValue());
                        results.put(relationship.getStartNode(), reasons);
                    });
                }
            }
        }
        return results.entrySet().stream().map(entry -> {
            entry.getValue().add(entry.getKey());
            return new NodeListResult(entry.getValue());
        });

    }


    private String getAddress(Node address) {
        return address.getProperty("line1", "") + " " +
                        address.getProperty("city", "") + " " +
                        address.getProperty("state", "") + " " +
                        address.getProperty("zip", "") + "-" +
                        address.getProperty("zip_plus_4", "");
    }
}
