// src/test/java/com/hugof/graphql/batchmappingsubcription/HouseControllerIntegrationTest.java
package com.hugof.graphql.batchmappingsubcription;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureGraphQlTester
class HouseControllerIntegrationTest {

    @Autowired
    private BatchMappingCounter batchMappingCounter;

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void testMeQuery() {
        String query = "{ me { id name } }";
        graphQlTester.document(query)
                .execute()
                .path("me.id").entity(Integer.class).isEqualTo(2)
                .path("me.name").entity(String.class).isEqualTo("Brian");
    }

    @Test
    void testOwnersQuery() {
        String query = "{ owners { id name } }";
        graphQlTester.document(query)
                .execute()
                .path("owners").entityList(HouseController.Owner.class)
                .hasSize(10);
    }

    @Test
    void testAllOwnersSubscription() {
        String subscription = "subscription { allOwners { id name } }";
        val responseFlux = graphQlTester.document(subscription)
                .executeSubscription()
                .toFlux();

        // Verify the response
        StepVerifier.create(responseFlux)
                .assertNext(response -> {
                    val owners = response.path("allOwners").entityList(HouseController.Owner.class).get();
                    System.out.println("Owners: " + owners);
                    assert owners.size() == 10;
                })
                .expectNoEvent(Duration.ofSeconds(5))
                .thenCancel()
                .verify();


    }

    @Test
    void testBatchWorkingOnSubscription() {
        batchMappingCounter.reset();
        val responseFlux = graphQlTester.document("""
                subscription {
                    allOwners {
                        id
                        name
                        schemaMappingEntities {
                            id
                            name
                        }
                        batchMappingEntities {
                            id
                            name
                        }
                    }
                }
                """)
                .executeSubscription()
                .toFlux();

        StepVerifier.create(responseFlux)
                .assertNext(response -> {
                    val owners = response.path("allOwners").entityList(OwnerResponse.class).get();
                    System.out.println("Owners: " + owners);
                    assert owners.size() == 10;

                    // Check schemaMappingEntities
                    for (OwnerResponse owner : owners) {
                        assert owner.schemaMappingEntities().size() == 1;
                        assert owner.schemaMappingEntities().get(0).id() != null;
                        assert owner.schemaMappingEntities().get(0).name() != null;
                    }

                    // Check batchMappingEntities
                    for (OwnerResponse owner : owners) {
                        assert owner.batchMappingEntities().size() == 1;
                        assert owner.batchMappingEntities().get(0).id() != null;
                        assert owner.batchMappingEntities().get(0).name() != null;
                    }

                    // Check batch mapping counter it should be 1 since we are using batch mapping but for some reason it is not batching
                    assertEquals(1, batchMappingCounter.getCount(), "Batch mapping counter should be 10");
                })
                .expectNoEvent(Duration.ofSeconds(5))
                .thenCancel()
                .verify();
    }


    private record OwnerResponse(
            Integer id,
            String name,
            List<HouseController.SchemaMappingEntity> schemaMappingEntities,
            List<HouseController.BatchMappingEntity> batchMappingEntities
    ) {}

}
