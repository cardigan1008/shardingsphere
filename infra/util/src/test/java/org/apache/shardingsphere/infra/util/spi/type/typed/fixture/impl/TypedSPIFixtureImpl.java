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

package org.apache.shardingsphere.infra.util.spi.type.typed.fixture.impl;

import lombok.Getter;
import org.apache.shardingsphere.infra.util.spi.type.typed.fixture.TypedSPIFixture;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

@Getter
public final class TypedSPIFixtureImpl implements TypedSPIFixture {
    
    private String value;
    
    @Override
    public void init(final Properties props) {
        value = props.getProperty("key");
    }
    
    @Override
    public String getType() {
        return "TYPED.FIXTURE";
    }
    
    @Override
    public Collection<Object> getTypeAliases() {
        return Collections.singleton("TYPED.ALIAS");
    }
}
