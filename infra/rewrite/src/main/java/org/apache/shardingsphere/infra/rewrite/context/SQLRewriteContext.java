/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.rewrite.context;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerators;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.builder.DefaultTokenGeneratorBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL rewrite context.
 */
@Getter
public final class SQLRewriteContext {
    
    private final ShardingSphereDatabase database;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final ParameterBuilder parameterBuilder;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    @Getter(AccessLevel.NONE)
    private final SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
    
    private final ConnectionContext connectionContext;
    
    public SQLRewriteContext(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext, final String sql, final List<Object> params,
                             final ConnectionContext connectionContext, final HintValueContext hintValueContext) {
        this.database = database;
        this.sqlStatementContext = sqlStatementContext;
        this.sql = sql;
        parameters = params;
        this.connectionContext = connectionContext;
        if (!hintValueContext.isSkipSQLRewrite()) {
            addSQLTokenGenerators(new DefaultTokenGeneratorBuilder(sqlStatementContext).getSQLTokenGenerators());
        }
        parameterBuilder = sqlStatementContext instanceof InsertStatementContext && null == ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                ? new GroupedParameterBuilder(
                        ((InsertStatementContext) sqlStatementContext).getGroupedParameters(), ((InsertStatementContext) sqlStatementContext).getOnDuplicateKeyUpdateParameters())
                : new StandardParameterBuilder(params);
    }
    
    /**
     * Add SQL token generators.
     *
     * @param sqlTokenGenerators SQL token generators
     */
    public void addSQLTokenGenerators(final Collection<SQLTokenGenerator> sqlTokenGenerators) {
        this.sqlTokenGenerators.addAll(sqlTokenGenerators);
    }
    
    /**
     * Generate SQL tokens.
     */
    public void generateSQLTokens() {
        sqlTokens.addAll(sqlTokenGenerators.generateSQLTokens(database.getName(), database.getSchemas(), sqlStatementContext, parameters, connectionContext));
    }
}
