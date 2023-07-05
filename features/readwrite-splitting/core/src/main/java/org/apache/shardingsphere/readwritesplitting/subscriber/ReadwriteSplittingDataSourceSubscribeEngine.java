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

package org.apache.shardingsphere.readwritesplitting.subscriber;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.subsciber.RuleItemChangedSubscribeEngine;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.event.datasource.AlterReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.datasource.DropReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Readwrite-splitting data source subscribe engine.
 */
public final class ReadwriteSplittingDataSourceSubscribeEngine extends RuleItemChangedSubscribeEngine<ReadwriteSplittingRuleConfiguration, ReadwriteSplittingDataSourceRuleConfiguration> {
    
    @Override
    protected ReadwriteSplittingDataSourceRuleConfiguration swapRuleItemConfigurationFromEvent(final AlterRuleItemEvent event, final String yamlContent) {
        YamlReadwriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig = YamlEngine.unmarshal(yamlContent, YamlReadwriteSplittingDataSourceRuleConfiguration.class);
        return new ReadwriteSplittingDataSourceRuleConfiguration(((AlterNamedRuleItemEvent) event).getItemName(), yamlDataSourceRuleConfig.getWriteDataSourceName(),
                yamlDataSourceRuleConfig.getReadDataSourceNames(), getTransactionalReadQueryStrategy(yamlDataSourceRuleConfig), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    private TransactionalReadQueryStrategy getTransactionalReadQueryStrategy(final YamlReadwriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return Strings.isNullOrEmpty(yamlDataSourceRuleConfig.getTransactionalReadQueryStrategy())
                ? TransactionalReadQueryStrategy.DYNAMIC
                : TransactionalReadQueryStrategy.valueOf(yamlDataSourceRuleConfig.getTransactionalReadQueryStrategy());
    }
    
    @Override
    protected ReadwriteSplittingRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        return rule.map(optional -> (ReadwriteSplittingRuleConfiguration) optional.getConfiguration())
                .orElseGet(() -> new ReadwriteSplittingRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
    }
    
    @Override
    protected void changeRuleItemConfiguration(final AlterRuleItemEvent event, final ReadwriteSplittingRuleConfiguration currentRuleConfig,
                                               final ReadwriteSplittingDataSourceRuleConfiguration toBeChangedItemConfig) {
        // TODO refactor DistSQL to only persist config
        currentRuleConfig.getDataSources().removeIf(each -> each.getName().equals(toBeChangedItemConfig.getName()));
        currentRuleConfig.getDataSources().add(toBeChangedItemConfig);
    }
    
    @Override
    protected void dropRuleItemConfiguration(final DropRuleItemEvent event, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getDataSources().removeIf(each -> each.getName().equals(((DropNamedRuleItemEvent) event).getItemName()));
    }
    
    @Override
    public String getType() {
        return AlterReadwriteSplittingDataSourceEvent.class.getName();
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return Collections.singleton(DropReadwriteSplittingDataSourceEvent.class.getName());
    }
}
