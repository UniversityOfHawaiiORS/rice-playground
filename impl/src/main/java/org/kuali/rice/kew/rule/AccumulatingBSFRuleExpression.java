/*
 * Copyright 2007-2008 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.rule;

import java.util.ArrayList;
import java.util.List;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.log4j.Logger;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.exception.WorkflowException;


/**
 * An extension of BSFRuleExpression that makes it easier to accumulate a list of responsibilities
 * to emit. 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class AccumulatingBSFRuleExpression extends BSFRuleExpression {
    private static final Logger LOG = Logger.getLogger(AccumulatingBSFRuleExpression.class);

    @Override
    protected void declareBeans(BSFManager manager, Rule rule, RouteContext context) throws BSFException {
        // define the standard beans
        super.declareBeans(manager, rule, context);
        // define our special rule helper class
        RuleHelper rh = new RuleHelper(rule, context);
        manager.declareBean("metarule", rh, RuleHelper.class); // backwards compatibility with existing KRAMetaRuleExpression usage
        manager.declareBean("rulehelper", rh, RuleHelper.class);
    }

    /**
     * Helper class that is exposed to groovy scripts
     */
    private static final class RuleHelper {
        private Rule rule;
        private WorkflowRuleAPI workflow;
        /**
         * Responsibilities accumulated during the evaluation
         */
        private List<RuleResponsibility> responsibilities = new ArrayList<RuleResponsibility>();
        private int responsibilityPriority = 0;

        private RuleHelper(Rule rule, RouteContext context) {
            this.workflow = new WorkflowRuleAPI(context);
            this.rule = rule;
        }

        /**
         * @return the accumulated responsibilities
         */
        public List<RuleResponsibility> getResponsibilities() {
            return responsibilities;
        }

        /**
         * @param success whether the result should be successful
         * @return the RuleExpressionResult with success flag and accumulated responsibilities
         */
        public RuleExpressionResult getResult(boolean success) {
            return new RuleExpressionResult(rule, success, responsibilities);
        }

        /**
         * Adds the responsibilities in the result to the list of accumulated responsibilities 
         * @param result a RuleExpressionResult whose responsibilities to accumulate
         */
        public void accumulate(RuleExpressionResult result) {
            if (result.getResponsibilities() == null || result.getResponsibilities().size() == 0) return;

            Integer curPriority = Integer.valueOf(responsibilityPriority);
            for (RuleResponsibility responsibility: result.getResponsibilities()) {
                responsibility.setPriority(curPriority);
                responsibilities.add(responsibility);
            }
            // increment responsibilityPriority for next rule expression result responsibilities
            responsibilityPriority++;
        }

        /**
         * Evaluates a named rule accumulating responsibilities regardless of rule success
         * @param ruleName the name of the rule to evaluate
         * @return whether the rule was successful
         */
        public boolean evalRule(String ruleName) throws WorkflowException {
            RuleExpressionResult result = workflow.invokeRule(ruleName);
            accumulate(result);
            return result.isSuccess();
        }

        /**
         * Evaluates a named rule and if it is successful accumulates the rule responsibilities
         * @param ruleName the name of the rule to evaluate
         * @param accumOnSuccess whether to accumulate the rules responsibilities on success (true), or on failure (false)
         * @return whether the rule was successful
         */
        public boolean evalRule(String ruleName, boolean accumOnSuccess) throws WorkflowException {
            RuleExpressionResult result = workflow.invokeRule(ruleName);
            if (accumOnSuccess == result.isSuccess()) {
                accumulate(result);
            }
            return result.isSuccess();
        }

    }
}
