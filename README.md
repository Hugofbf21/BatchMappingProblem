# BatchMappingProblem

This repository demonstrates an issue where GraphQL `@BatchMapping` methods are invoked multiple times when executed through subscriptions in a Spring Boot application.

## Overview

The project uses:
- Java
- Spring Boot
- Spring GraphQL
- Maven

It defines a simple domain with `Owner` entities and exposes GraphQL queries, subscriptions, and batch mappings. The main focus is to showcase how, under certain conditions, the `@BatchMapping` method is called more than once per subscription event, which may not be the expected behavior.

Access the GraphQL endpoint (default: `http://localhost:8080/graphql`) using a GraphQL client.

## Reproducing the Issue

- Use a GraphQL subscription that triggers the batch mapping.
- Observe the logs or run project tests to see that the `@BatchMapping` method is called multiple times per subscription event.

## Tests
The repo is equipped with some simple tests, most passing but with one failing, `testBatchWorkingOnSubscription` it will only pass if the batch mapping method is only called once, currently it is being called 10 times, because that is the number of owners being returned

## Observations
The batchmapping is indeed working for queries, but it seems that when triggered by a subscription it does not batch entities

## License

This project is for demonstration purposes.
