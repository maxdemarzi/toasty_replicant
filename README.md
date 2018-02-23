# Toasty Replicant
POC to find replicant accounts seeking to get free toasters

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/replicants-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/replicants-1.0-SNAPSHOT.jar neo4j-enterprise-3.3.3/plugins/.
    
Edit your Neo4j/conf/neo4j.conf file by adding this line:

    dbms.security.procedures.unrestricted=com.maxdemarzi.*    

Restart your Neo4j Server.

Create the Schema by running this stored procedure:

    CALL com.maxdemarzi.schema.generate
    
Create some test data:

        CREATE (a1:Account { id: 'a1' })
        CREATE (a2:Account { id: 'a2' })
        CREATE (a3:Account { id: 'a3' })
        CREATE (e1:Email { address: 'max@neo4j.com' })
        CREATE (e2:Email { address: 'maxdemarzi@gmail.com' })
        CREATE (e3:Email { address: 'maxdemarzi@hotmail.com' })
        CREATE (p1:Phone { number: '3125137509' })
        CREATE (p2:Phone { number: '3128675309' })
        CREATE (p3:Phone { number: '3125551234' })
        
        CREATE (add1:Address { line1:'175 North Harbor Dr.', city:'Chicago', state:'IL', zip:'60605', lat: 41.886069, lon:-87.61567, geohash:'dp3wq'})
        CREATE (add2:Address { line1:'520 South State St.', city:'Chicago', state:'IL', zip:'60605', lat:41.875418, lon:-87.627636 , geohash:'dp3wj'})
        CREATE (add3:Address { line1:'111 E 5th Avenue', city:'San Mateo', state:'CA', zip:'94401', lat:37.562841, lon:-122.322973, geohash:'9q9j8'})
        CREATE (add4:Address { line1:'3900 Yorktowne Blvd', city:'Port Orange', state:'FL', zip:'32129', lat:29.113762, lon:-81.027617, geohash:'djnms'})
        
        CREATE (a1)-[:HAS_EMAIL]->(e1)
        CREATE (a2)-[:HAS_EMAIL]->(e2)
        CREATE (a3)-[:HAS_EMAIL]->(e3)
        
        CREATE (a1)-[:HAS_PHONE]->(p1)
        CREATE (a2)-[:HAS_PHONE]->(p2)
        CREATE (a3)-[:HAS_PHONE]->(p3)
        
        CREATE (a1)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add1)
        CREATE (a2)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add2)
        CREATE (a3)-[:HAS_ADDRESS { billing:true, shipping:true, current:true }]->(add3)
        CREATE (a1)-[:HAS_ADDRESS { billing:true, shipping:true, current:false }]->(add4)
        
Try it, with the same email:

    CALL com.maxdemarzi.replicant({email:'max@neo4j.com', addresses:['9641 Sunset Blvd Beverly Hills CA 90210'], phone:'3102762251'}) yield value return value;
    
Same phone:
    
    CALL com.maxdemarzi.replicant({email:'some@email.com', addresses:['9641 Sunset Blvd Beverly Hills CA 90210'], phone:'3125137509'}) yield value return value;

Same address:

    CALL com.maxdemarzi.replicant({email:'some@email.com', addresses:['175 North Harbor Dr. Chicago IL 60605'], phone:'1235550000'}) yield value return value;

Address typos:

    CALL com.maxdemarzi.replicant({email:'some@email.com', addresses:['175 Norht Hrabro Dr. Chicago IL 60605'], phone:'1235550000'}) yield value return value;            
    
Two matching items:

    CALL com.maxdemarzi.replicant({email:'max@neo4j.com', addresses:['175 Norht Hrabro Dr. Chicago IL 60605'], phone:'1235550000'}) yield value return value;    