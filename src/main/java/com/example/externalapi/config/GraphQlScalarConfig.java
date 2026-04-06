package com.example.externalapi.config;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

@Configuration
public class GraphQlScalarConfig {

    @Bean
    GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("ISO-8601 offset date-time")
                .coercing(new Coercing<OffsetDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof OffsetDateTime value) {
                            return value.toString();
                        }
                        if (dataFetcherResult instanceof String value) {
                            return value;
                        }
                        throw new CoercingSerializeException("Expected OffsetDateTime value");
                    }

                    @Override
                    public OffsetDateTime parseValue(Object input) {
                        if (input instanceof String value) {
                            try {
                                return OffsetDateTime.parse(value);
                            } catch (DateTimeParseException ex) {
                                throw new CoercingParseValueException("Invalid DateTime value", ex);
                            }
                        }
                        throw new CoercingParseValueException("Expected DateTime string value");
                    }

                    @Override
                    public OffsetDateTime parseLiteral(Object input) {
                        if (input instanceof StringValue value) {
                            try {
                                return OffsetDateTime.parse(value.getValue());
                            } catch (DateTimeParseException ex) {
                                throw new CoercingParseLiteralException("Invalid DateTime literal", ex);
                            }
                        }
                        throw new CoercingParseLiteralException("Expected DateTime string literal");
                    }
                })
                .build();
    }

    @Bean
    RuntimeWiringConfigurer runtimeWiringConfigurer(GraphQLScalarType dateTimeScalar) {
        return wiringBuilder -> wiringBuilder.scalar(dateTimeScalar);
    }
}