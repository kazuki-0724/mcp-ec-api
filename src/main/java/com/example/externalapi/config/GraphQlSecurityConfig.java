package com.example.externalapi.config;

import graphql.analysis.MaxQueryComplexityInstrumentation;
import graphql.analysis.MaxQueryDepthInstrumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQlSecurityConfig {

    @Bean
    MaxQueryDepthInstrumentation maxQueryDepthInstrumentation(
            @Value("${app.graphql.max-depth:6}") int maxDepth
    ) {
        return new MaxQueryDepthInstrumentation(maxDepth);
    }

    @Bean
    MaxQueryComplexityInstrumentation maxQueryComplexityInstrumentation(
            @Value("${app.graphql.max-complexity:50}") int maxComplexity
    ) {
        return new MaxQueryComplexityInstrumentation(maxComplexity);
    }
}
