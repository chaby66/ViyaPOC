package com.sas.rtdm2id.otp;

import com.sas.rtdm2id.Rtdm2idApplicationTests;
import com.sas.rtdm2id.model.rtdm.Batch;
import com.sas.rtdm2id.model.rtdm.ProcessNodeDataDO;
import com.sas.rtdm2id.model.rtdm.ValueTypeVarInfoDO;
import com.sas.rtdm2id.model.rtdm.extension.CalculatedItemDO;
import com.sas.rtdm2id.util.model.RTDMObject;
import com.sas.rtdm2id.util.tree.impl.GenericTree;
import com.sas.rtdm2id.util.tree.impl.GenericTreeNode;
import com.sas.rtdm2id.util.tree.impl.TreeUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
class OtpServiceTest extends Rtdm2idApplicationTests {

    @Autowired
    private OtpService otpService;

    @Autowired
    private TreeUtil treeUtil;

    @Test
    void precessGroovyCode() throws JAXBException, IOException {

        Resource res = new DefaultResourceLoader().getResource("classpath:DC_runAlternativeOffers_v23.xml");

        JAXBContext context = JAXBContext.newInstance(Batch.class);
        Batch batch = (Batch) context.createUnmarshaller()
                .unmarshal(res.getInputStream());

        log.info("DC name: {}", batch.getLogicalUnit().getFlowDO().getName());

        AtomicBoolean processed = new AtomicBoolean(false);
        batch.getLogicalUnit().getProcessNodeDataDOs().forEach(p -> {
            if ("SP_call_runThmCalculation_v3".equals(p.getProcess().getPhysicalName()) && !processed.get()) {
                log.info("ProcessName: {}", p.getProcess().getPhysicalName());
                String ds2code = otpService.precessGroovyCode(p);
                log.info("DS2CODE:\n{}", ds2code);
                processed.set(true);
            }
        });




    }

    @Test
    void processCalcVars() throws JAXBException, IOException {

        Resource res = new DefaultResourceLoader().getResource("classpath:DC_runAlternativeOffers_v23.xml");

        JAXBContext context = JAXBContext.newInstance(Batch.class);
        Batch batch = (Batch) context.createUnmarshaller()
                .unmarshal(res.getInputStream());

        log.info("DC name: {}", batch.getLogicalUnit().getFlowDO().getName());

        GenericTree<Short> tree = treeUtil.createTree(batch.getLogicalUnit());

        LinkedHashMap<String, List<CalculatedItemDO>> calcVars = new LinkedHashMap<>();

        for (CalculatedItemDO calculatedItemDO : batch.getLogicalUnit().getFlowDO().getCalculatedDataItemList().getCalculatedItemDOs()) {
//            log.info("Name: {}, NodeId: {}, Expression: {}", calculatedItemDO.getName(), calculatedItemDO.getNodeId(), calculatedItemDO.getExpression());
            if (!calcVars.containsKey(calculatedItemDO.getNodeId())) {
                calcVars.put(calculatedItemDO.getNodeId(), new ArrayList<>());
            }
            calcVars.get(calculatedItemDO.getNodeId()).add(calculatedItemDO);
        }

        procTree(treeUtil, tree.getRoot(), calcVars, 0);


    }

    private void procTree(TreeUtil treeUtil, GenericTreeNode<Short> root, LinkedHashMap<String,
            List<CalculatedItemDO>> calcVars, int level) {
        RTDMObject rtdm = treeUtil.getRTDMObjectByObjId(root.getData());
        String nodeId = treeUtil.getObjIdToNodeIdMap().get(root.getData());

        ProcessNodeDataDO procNode = null;
        String nodeName = null;
        if (rtdm.getObject().getClass() == ProcessNodeDataDO.class) {
            procNode = (ProcessNodeDataDO) rtdm.getObject();
            nodeName = procNode.getNodeName();
        }

        log.info(" ".repeat(level * 2)+"{} - {} {} -> {} ({})", root.getData(), nodeId, nodeName,
                rtdm.getObjectType(), rtdm.getObject().getClass());

        if (calcVars.containsKey(nodeId)) {
            log.info(" ".repeat(level * 2)+"*** Create calculated var (NodeId: {}) ***", nodeId);
            for (CalculatedItemDO calculatedItemDO : calcVars.get(nodeId)) {
                log.info(" ".repeat(level * 2)+"  Name: {}, Id:{}", calculatedItemDO.getName(), calculatedItemDO.getId());
            }
        }
        List<ValueTypeVarInfoDO> calcs = getLinkedCalulatedItems(procNode);
        if (!calcs.isEmpty()) {
            log.info(" ".repeat(level * 2)+"*** Use Calculated var (NodeId: {}): {}", nodeId);
            for (ValueTypeVarInfoDO calc : calcs) {
                log.info(" ".repeat(level * 2)+"  VarInfoId: {}", calc.getVarInfoId());
            }
        }

        if (root.hasChildren()) {
            for (GenericTreeNode<Short> child : root.getChildren()) {
                procTree(treeUtil, child, calcVars, level + 1);
            }
        }
    }

    private List<ValueTypeVarInfoDO> getLinkedCalulatedItems(ProcessNodeDataDO node) {
        List<ValueTypeVarInfoDO> result = new ArrayList<>();

        if (node != null) {
            for (ProcessNodeDataDO.Process.InputVariableList.IBVariableDO ibVariableDO : node.getProcess().getInputVariableList().getIBVariableDOs()) {
                if (ibVariableDO.getValue() != null && ibVariableDO.getValue().getValueTypeVarInfoDO() != null &&
                        "CalculatedDataItem".equals(ibVariableDO.getValue().getValueTypeVarInfoDO().getVarInfoSource())) {
                    result.add(ibVariableDO.getValue().getValueTypeVarInfoDO());
                }
            }
        }

        return result;
    }

}