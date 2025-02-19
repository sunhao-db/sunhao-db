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

package org.apache.doris.nereids.pattern;

import org.apache.doris.nereids.rules.RulePromise;
import org.apache.doris.nereids.trees.TreeNode;
import org.apache.doris.nereids.trees.plans.BinaryPlan;
import org.apache.doris.nereids.trees.plans.GroupPlan;
import org.apache.doris.nereids.trees.plans.LeafPlan;
import org.apache.doris.nereids.trees.plans.Plan;
import org.apache.doris.nereids.trees.plans.UnaryPlan;
import org.apache.doris.nereids.trees.plans.logical.LogicalBinary;
import org.apache.doris.nereids.trees.plans.logical.LogicalLeaf;
import org.apache.doris.nereids.trees.plans.logical.LogicalRelation;
import org.apache.doris.nereids.trees.plans.logical.LogicalUnary;
import org.apache.doris.nereids.trees.plans.physical.PhysicalBinary;
import org.apache.doris.nereids.trees.plans.physical.PhysicalLeaf;
import org.apache.doris.nereids.trees.plans.physical.PhysicalRelation;
import org.apache.doris.nereids.trees.plans.physical.PhysicalUnary;

/**
 * An interface provided some PatternDescriptor.
 * Child Interface(RuleFactory) can use to declare a pattern shape, then convert to a rule.
 * In the future, we will generate this interface by codegen.
 */
public interface Patterns {
    // need implement
    RulePromise defaultPromise();

    /* special pattern descriptors */

    default <T extends RULE_TYPE, RULE_TYPE extends TreeNode<RULE_TYPE>> PatternDescriptor<T, RULE_TYPE> any() {
        return new PatternDescriptor<>(Pattern.ANY, defaultPromise());
    }

    default <T extends RULE_TYPE, RULE_TYPE extends TreeNode<RULE_TYPE>> PatternDescriptor<T, RULE_TYPE> multi() {
        return new PatternDescriptor<>(Pattern.MULTI, defaultPromise());
    }

    default PatternDescriptor<GroupPlan, Plan> group() {
        return new PatternDescriptor<>(Pattern.GROUP, defaultPromise());
    }

    default PatternDescriptor<GroupPlan, Plan> multiGroup() {
        return new PatternDescriptor<>(Pattern.MULTI_GROUP, defaultPromise());
    }

    /* abstract plan patterns */

    /**
     * create a leafPlan pattern.
     */
    default PatternDescriptor<LeafPlan, Plan> leafPlan() {
        return new PatternDescriptor(new TypePattern(LeafPlan.class), defaultPromise());
    }

    /**
     * create a unaryPlan pattern.
     */
    default PatternDescriptor<UnaryPlan<GroupPlan>, Plan> unaryPlan() {
        return new PatternDescriptor(new TypePattern(UnaryPlan.class, Pattern.GROUP), defaultPromise());
    }

    /**
     * create a unaryPlan pattern.
     */
    default <C extends Plan> PatternDescriptor<UnaryPlan<C>, Plan>
            unaryPlan(PatternDescriptor<C, Plan> child) {
        return new PatternDescriptor(new TypePattern(UnaryPlan.class, child.pattern), defaultPromise());
    }

    /**
     * create a binaryPlan pattern.
     */
    default PatternDescriptor<BinaryPlan<GroupPlan, GroupPlan>, Plan> binaryPlan() {
        return new PatternDescriptor(
                new TypePattern(BinaryPlan.class, Pattern.GROUP, Pattern.GROUP),
                defaultPromise()
        );
    }

    /**
     * create a binaryPlan pattern.
     */
    default <LEFT_CHILD_TYPE extends Plan, RIGHT_CHILD_TYPE extends Plan>
            PatternDescriptor<BinaryPlan<LEFT_CHILD_TYPE, RIGHT_CHILD_TYPE>, Plan> binaryPlan(
                PatternDescriptor<LEFT_CHILD_TYPE, Plan> leftChild,
                PatternDescriptor<RIGHT_CHILD_TYPE, Plan> rightChild) {
        return new PatternDescriptor(
                new TypePattern(BinaryPlan.class, leftChild.pattern, rightChild.pattern),
                defaultPromise()
        );
    }

    /* abstract logical plan patterns */

    /**
     * create a logicalLeaf pattern.
     */
    default PatternDescriptor<LogicalLeaf, Plan> logicalLeaf() {
        return new PatternDescriptor(new TypePattern(LogicalLeaf.class), defaultPromise());
    }

    /**
     * create a logicalUnary pattern.
     */
    default PatternDescriptor<LogicalUnary<GroupPlan>, Plan> logicalUnary() {
        return new PatternDescriptor(new TypePattern(LogicalUnary.class, Pattern.GROUP), defaultPromise());
    }

    /**
     * create a logicalUnary pattern.
     */
    default <C extends Plan> PatternDescriptor<LogicalUnary<C>, Plan>
            logicalUnary(PatternDescriptor<C, Plan> child) {
        return new PatternDescriptor(new TypePattern(LogicalUnary.class, child.pattern), defaultPromise());
    }

    /**
     * create a logicalBinary pattern.
     */
    default PatternDescriptor<LogicalBinary<GroupPlan, GroupPlan>, Plan> logicalBinary() {
        return new PatternDescriptor(
                new TypePattern(LogicalBinary.class, Pattern.GROUP, Pattern.GROUP),
                defaultPromise()
        );
    }

    /**
     * create a logicalBinary pattern.
     */
    default <LEFT_CHILD_TYPE extends Plan, RIGHT_CHILD_TYPE extends Plan>
            PatternDescriptor<LogicalBinary<LEFT_CHILD_TYPE, RIGHT_CHILD_TYPE>, Plan>
            logicalBinary(
                PatternDescriptor<LEFT_CHILD_TYPE, Plan> leftChild,
                PatternDescriptor<RIGHT_CHILD_TYPE, Plan> rightChild) {
        return new PatternDescriptor(
                new TypePattern(LogicalBinary.class, leftChild.pattern, rightChild.pattern),
                defaultPromise()
        );
    }

    /**
     * create a logicalRelation pattern.
     */
    default PatternDescriptor<LogicalRelation, Plan> logicalRelation() {
        return new PatternDescriptor(new TypePattern(LogicalRelation.class), defaultPromise());
    }

    /* abstract physical plan patterns */

    /**
     * create a physicalLeaf pattern.
     */
    default PatternDescriptor<PhysicalLeaf, Plan> physicalLeaf() {
        return new PatternDescriptor(new TypePattern(PhysicalLeaf.class), defaultPromise());
    }

    /**
     * create a physicalUnary pattern.
     */
    default PatternDescriptor<PhysicalUnary<GroupPlan>, Plan> physicalUnary() {
        return new PatternDescriptor(new TypePattern(PhysicalUnary.class, Pattern.GROUP), defaultPromise());
    }

    /**
     * create a physicalUnary pattern.
     */
    default <C extends Plan> PatternDescriptor<PhysicalUnary<C>, Plan>
            physicalUnary(PatternDescriptor<C, Plan> child) {
        return new PatternDescriptor(new TypePattern(PhysicalUnary.class, child.pattern), defaultPromise());
    }

    /**
     * create a physicalBinary pattern.
     */
    default PatternDescriptor<PhysicalBinary<GroupPlan, GroupPlan>, Plan> physicalBinary() {
        return new PatternDescriptor(
                new TypePattern(PhysicalBinary.class, Pattern.GROUP, Pattern.GROUP),
                defaultPromise()
        );
    }

    /**
     * create a physicalBinary pattern.
     */
    default <LEFT_CHILD_TYPE extends Plan, RIGHT_CHILD_TYPE extends Plan>
            PatternDescriptor<PhysicalBinary<LEFT_CHILD_TYPE, RIGHT_CHILD_TYPE>, Plan>
            physicalBinary(
                PatternDescriptor<LEFT_CHILD_TYPE, Plan> leftChild,
                PatternDescriptor<RIGHT_CHILD_TYPE, Plan> rightChild) {
        return new PatternDescriptor(
                new TypePattern(PhysicalBinary.class, leftChild.pattern, rightChild.pattern),
                defaultPromise()
        );
    }

    /**
     * create a physicalRelation pattern.
     */
    default PatternDescriptor<PhysicalRelation, Plan> physicalRelation() {
        return new PatternDescriptor(new TypePattern(PhysicalRelation.class), defaultPromise());
    }
}
