// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.trees.plans.physical;

import org.apache.doris.nereids.memo.GroupExpression;
import org.apache.doris.nereids.properties.LogicalProperties;
import org.apache.doris.nereids.trees.expressions.Expression;
import org.apache.doris.nereids.trees.plans.PlanType;
import org.apache.doris.nereids.trees.plans.visitor.PlanVisitor;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstract class for all physical scan plan.
 */
public abstract class PhysicalRelation extends PhysicalLeaf {

    protected final List<String> qualifier;

    /**
     * Constructor for PhysicalScan.
     *
     * @param type node type
     * @param qualifier table's name
     */
    public PhysicalRelation(PlanType type, List<String> qualifier, Optional<GroupExpression> groupExpression,
                            LogicalProperties logicalProperties) {
        super(type, groupExpression, logicalProperties);
        this.qualifier = Objects.requireNonNull(qualifier, "qualifier can not be null");
    }

    public List<String> getQualifier() {
        return qualifier;
    }

    @Override
    public <R, C> R accept(PlanVisitor<R, C> visitor, C context) {
        return visitor.visitPhysicalScan(this, context);
    }

    @Override
    public List<Expression> getExpressions() {
        return ImmutableList.of();
    }
}
