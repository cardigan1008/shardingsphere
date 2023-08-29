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

package org.apache.shardingsphere.infra.binder.segment.select.projection.util;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.util.ProjectionUtils;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProjectionUtilsTest {
    
    private final IdentifierValue alias = new IdentifierValue("Data", QuoteCharacter.NONE);
    
    @Test
    void assertGetColumnLabelFromAlias() {
        assertThat(ProjectionUtils.getColumnLabelFromAlias(new IdentifierValue("Data", QuoteCharacter.QUOTE), new PostgreSQLDatabaseType()), is("Data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")), is("data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, TypedSPILoader.getService(DatabaseType.class, "openGauss")), is("data"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, TypedSPILoader.getService(DatabaseType.class, "Oracle")), is("DATA"));
        assertThat(ProjectionUtils.getColumnLabelFromAlias(alias, TypedSPILoader.getService(DatabaseType.class, "MySQL")), is("Data"));
    }
    
    @Test
    void assertGetColumnNameFromFunction() {
        String functionName = "Function";
        String functionExpression = "FunctionExpression";
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")), is("function"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, TypedSPILoader.getService(DatabaseType.class, "openGauss")), is("function"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, TypedSPILoader.getService(DatabaseType.class, "Oracle")), is("FUNCTIONEXPRESSION"));
        assertThat(ProjectionUtils.getColumnNameFromFunction(functionName, functionExpression, TypedSPILoader.getService(DatabaseType.class, "MySQL")), is("FunctionExpression"));
    }
}
