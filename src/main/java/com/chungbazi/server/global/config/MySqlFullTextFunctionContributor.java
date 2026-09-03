package com.chungbazi.server.global.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class MySqlFullTextFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry().registerPattern(
                "match_against_boolean_phrase",
                "match (?1, ?2, ?3) against (?4 in boolean mode)",
                functionContributions.getTypeConfiguration()
                        .getBasicTypeForJavaType(Double.class)
        );
    }
}
