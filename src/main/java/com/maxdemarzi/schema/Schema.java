package com.maxdemarzi.schema;

import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Schema {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    @Procedure(name = "com.maxdemarzi.schema.generate", mode = Mode.SCHEMA)
    @Description("CALL com.maxdemarzi.schema.generate() - generate schema")

    public Stream<StringResult> generate() throws IOException {

        ArrayList<StringResult> results = new ArrayList<>();

        org.neo4j.graphdb.schema.Schema schema = db.schema();
        if (!schema.getIndexes(Labels.Account).iterator().hasNext()) {
            schema.constraintFor(Labels.Account)
                    .assertPropertyIsUnique("id")
                    .create();
            results.add(new StringResult("(:Account {id}) constraint created"));
        }
        if (!schema.getIndexes(Labels.Email).iterator().hasNext()) {
            schema.constraintFor(Labels.Email)
                    .assertPropertyIsUnique("address")
                    .create();
            results.add(new StringResult("(:Email {address}) constraint created"));
        }
        if (!schema.getIndexes(Labels.Phone).iterator().hasNext()) {
            schema.constraintFor(Labels.Phone)
                    .assertPropertyIsUnique("number")
                    .create();
            results.add(new StringResult("(:Phone {number}) constraint created"));
        }
        if (!schema.getIndexes(Labels.Zip).iterator().hasNext()) {
            schema.constraintFor(Labels.Zip)
                    .assertPropertyIsUnique("code")
                    .create();
            results.add(new StringResult("(:Zip {code}) constraint created"));
        }
        results.add(new StringResult("Schema Created"));
        return results.stream();
    }
}
