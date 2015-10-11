/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.btm.client.collector.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.hawkular.btm.api.model.btxn.BusinessTransaction;
import org.hawkular.btm.api.model.btxn.Component;
import org.hawkular.btm.api.model.btxn.Consumer;
import org.hawkular.btm.api.model.btxn.Content;
import org.hawkular.btm.api.model.btxn.CorrelationIdentifier;
import org.hawkular.btm.api.model.btxn.CorrelationIdentifier.Scope;
import org.hawkular.btm.api.model.btxn.Message;
import org.hawkular.btm.api.model.btxn.NodeType;
import org.hawkular.btm.api.model.config.CollectorConfiguration;
import org.hawkular.btm.api.model.config.Direction;
import org.hawkular.btm.api.model.config.btxn.BusinessTxnConfig;
import org.hawkular.btm.api.model.config.btxn.DataSource;
import org.hawkular.btm.api.model.config.btxn.LiteralExpression;
import org.hawkular.btm.api.model.config.btxn.Processor;
import org.hawkular.btm.api.model.config.btxn.ProcessorAction;
import org.hawkular.btm.api.model.config.btxn.ProcessorAction.ActionType;
import org.hawkular.btm.api.model.config.btxn.TextExpression;
import org.junit.Test;

/**
 * @author gbrown
 */
public class ProcessorManagerTest {

    @Test
    public void testNodeTypeInNoURIFilter() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);
        p1.getActions().add(pa1);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", service.getDetails().get("test"));
    }

    @Test
    public void testNodeTypeInNoURIFilterWithPredicate() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        LiteralExpression literal = new LiteralExpression();
        literal.setValue("true");
        pa1.setPredicate(literal);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", service.getDetails().get("test"));
    }

    @Test
    public void testNodeTypeInNoURIFilterWithPredicateNoAction() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        LiteralExpression literal = new LiteralExpression();
        literal.setValue("false");
        pa1.setPredicate(literal);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertFalse(service.getDetails().containsKey("test"));
    }

    @Test
    public void testNodeTypeOutNoURIFilterNoAction() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.Out, null, "first", "second");

        assertFalse(service.getDetails().containsKey("test"));
    }

    @Test
    public void testMismatchedNodeTypeInNoURIFilterNoAction() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertFalse(service.getDetails().containsKey("test"));
    }

    @Test
    public void testNodeTypeInWithURIFilter() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);
        p1.setUriFilter("include");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        service.setUri("should include this");
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", service.getDetails().get("test"));
    }

    @Test
    public void testNodeTypeInWithURIFilterNoAction() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);
        p1.setUriFilter("include");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        service.setUri("should exclude this");
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertFalse(service.getDetails().containsKey("test"));
    }

    @Test
    public void testNodeTypeInNoURIFilterSetDetails() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", service.getDetails().get("test"));
    }

    @Test
    public void testNodeTypeInNoURIFilterSetFault() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetFault);
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", service.getFault());
    }

    @Test
    public void testNodeTypeInNoURIFilterSetFaultDescription() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetFaultDescription);
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", service.getFaultDescription());
    }

    @Test
    public void testNodeTypeInNoURIFilterSetProperty() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", btxn.getProperties().get("test"));
    }

    @Test
    public void testNodeTypeInNoURIFilterAddContent() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.AddContent);
        pa1.setName("test");
        pa1.setType("MessageType");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(1, service.getIn().getContent().size());

        Content content = service.getIn().getContent().get("test");

        assertNotNull(content);

        assertEquals("MessageType", content.getType());
        assertEquals("second", content.getValue());
    }

    @Test
    public void testNodeTypeInAddCorrelationId() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.AddCorrelationId);
        pa1.setScope(Scope.Global);
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(1, service.getCorrelationIds().size());

        assertEquals(CorrelationIdentifier.Scope.Global, service.getCorrelationIds().get(0).getScope());
        assertEquals("second", service.getCorrelationIds().get(0).getValue());
    }

    @Test
    public void testNodeTypeInFaultFilterNoFault() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);
        p1.setFaultFilter("MyFault");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");

        LiteralExpression literal = new LiteralExpression();
        literal.setValue("FaultRecorded");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(0, btxn.getProperties().size());
    }

    @Test
    public void testNodeTypeInFaultFilterWithMismatchFault() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);
        p1.setFaultFilter("MyFault");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("FaultRecorded");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        service.setFault("NotSameFault");

        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(0, btxn.getProperties().size());
    }

    @Test
    public void testNodeTypeInFaultFilterWithMatchingFault() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);
        p1.setFaultFilter("MyFault");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("FaultRecorded");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        service.setFault("MyFault");

        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(1, btxn.getProperties().size());
        assertTrue(btxn.getProperties().containsKey("result"));
    }

    @Test
    public void testNodeTypeInNoOperation() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);
        p1.setOperation("MyOp");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("OperationFound");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(0, btxn.getProperties().size());
    }

    @Test
    public void testNodeTypeInWithMismatchOperation() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);
        p1.setOperation("MyOp");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("FaultRecorded");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        service.setOperation("NotSameOperation");

        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(0, btxn.getProperties().size());
    }

    @Test
    public void testNodeTypeInWithMatchingFault() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);
        p1.setOperation("MyOp");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("FaultRecorded");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        service.setOperation("MyOp");

        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals(1, btxn.getProperties().size());
        assertTrue(btxn.getProperties().containsKey("result"));
    }

    @Test
    public void testIsProcessedTrue() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);
        p1.setOperation("MyOp");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("FaultRecorded");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        service.setOperation("MyOp");

        btxn.getNodes().add(service);
        btxn.setName("testapp");

        assertTrue(pm.isProcessed(btxn, service, Direction.In));
    }

    @Test
    public void testIsProcessedFalse() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);
        p1.setUriFilter("include");

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        service.setUri("should exclude this");
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        assertFalse(pm.isProcessed(btxn, service, Direction.In));
    }

    @Test
    public void testIsContentProcessedFalse() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("result");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("FaultRecorded");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();

        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        assertFalse(pm.isContentProcessed(btxn, service, Direction.In));
    }

    @Test
    public void testIsContentProcessedTrueForAddContent() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.AddContent);
        pa1.setName("test");
        pa1.setType("MessageType");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        assertTrue(pm.isContentProcessed(btxn, service, Direction.In));
    }

    @Test
    public void testIsContentProcessedTrueForSetProperty() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Consumer);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetProperty);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Consumer service = new Consumer();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Message req = new Message();
        service.setIn(req);

        assertTrue(pm.isContentProcessed(btxn, service, Direction.In));
    }

    @Test
    public void testProcessInNoHeadersOrContent() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        LiteralExpression literal = new LiteralExpression();
        literal.setValue("hello");
        pa1.setExpression(literal);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null);

        assertEquals("hello", service.getDetails().get("test"));
    }

    @Test
    public void testProcessInNoHeaders() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");

        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Header);
        expr.setKey("hello");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null);

        assertFalse(service.getDetails().containsKey("test"));
    }

    @Test
    public void testProcessInWithHeaders() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Header);
        expr.setKey("hello");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("hello", "world");
        pm.process(btxn, service, Direction.In, headers);

        assertTrue(service.getDetails().containsKey("test"));
    }

    @Test
    public void testProcessInNoContent() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("0");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null);

        assertFalse(service.getDetails().containsKey("test"));
    }

    @Test
    public void testProcessInWithContent() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("0");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "hello");

        assertTrue(service.getDetails().containsKey("test"));
    }

    @Test
    public void testProcessorPredicateTrue() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        LiteralExpression literal=new LiteralExpression();
        literal.setValue("true");
        p1.setPredicate(literal);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertEquals("second", service.getDetails().get("test"));
    }

    @Test
    public void testProcessorPredicateFalse() {
        CollectorConfiguration cc = new CollectorConfiguration();

        BusinessTxnConfig btc = new BusinessTxnConfig();
        cc.getBusinessTransactions().put("testapp", btc);

        Processor p1 = new Processor();
        btc.getProcessors().add(p1);

        p1.setNodeType(NodeType.Component);
        p1.setDirection(Direction.In);

        LiteralExpression literal=new LiteralExpression();
        literal.setValue("false");
        p1.setPredicate(literal);

        ProcessorAction pa1 = new ProcessorAction();
        p1.getActions().add(pa1);

        pa1.setActionType(ActionType.SetDetail);
        pa1.setName("test");
        TextExpression expr = new TextExpression();
        expr.setSource(DataSource.Value);
        expr.setKey("1");
        pa1.setExpression(expr);

        ProcessorManager pm = new ProcessorManager(cc);

        BusinessTransaction btxn = new BusinessTransaction();
        Component service = new Component();
        btxn.getNodes().add(service);
        btxn.setName("testapp");

        pm.process(btxn, service, Direction.In, null, "first", "second");

        assertFalse(service.getDetails().containsKey("test"));
    }

}
