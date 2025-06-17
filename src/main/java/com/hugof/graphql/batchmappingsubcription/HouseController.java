package com.hugof.graphql.batchmappingsubcription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HouseController {

    @Autowired
    private BatchMappingCounter batchMappingCounter;

    private final Map<Integer, Owner> owners = Map.of(
            1, new Owner(1, "Rossen"),
            2, new Owner(2, "Brian"),
            3, new Owner(3, "Donna"),
            4, new Owner(4, "Brad"),
            5, new Owner(5, "Andi"),
            6, new Owner(6, "Alice"),
            7, new Owner(7, "Bob"),
            8, new Owner(8, "Charlie"),
            9, new Owner(9, "Diana"),
            10, new Owner(10, "Ethan")
    );

    private static final Logger logger = LoggerFactory.getLogger(HouseController.class);


    @QueryMapping
    public Owner me() {
        return this.owners.get(2);
    }

    @QueryMapping
    public Collection<Owner> owners() {
        return this.owners.values();
    }

    @SubscriptionMapping
    public Flux<Collection<Owner>> allOwners() {
        return Flux.fromIterable(List.of(this.owners.values()));
    }

    @SchemaMapping(typeName = "Owner")
    public List<SchemaMappingEntity> schemaMappingEntities(Owner owner) {
        logger.debug("Loading A for owner: {} ", owner.name);
        return List.of(new SchemaMappingEntity(owner.id, owner.name));
    }

    @BatchMapping(typeName = "Owner")
    public Mono<Map<Owner, List<BatchMappingEntity>>> batchMappingEntities(List<Owner> owner) {
        logger.debug("Loading B for owners: " + owner.stream().map(Owner::name).toList());
        batchMappingCounter.increment();
        Map<Owner, List<BatchMappingEntity>> result = new HashMap<>();
        for (Owner o : owner) {
            result.put(o, List.of(new BatchMappingEntity(o.id, o.name)));
        }
        return Mono.just(result);
    }


    public record Owner(Integer id, String name) {
    }

    public record SchemaMappingEntity(Integer id, String name) {
    }

    public record BatchMappingEntity(Integer id, String name) {
    }

}
