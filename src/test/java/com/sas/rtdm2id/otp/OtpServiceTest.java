package com.sas.rtdm2id.otp;

import com.sas.rtdm2id.Rtdm2idApplicationTests;
import com.sas.rtdm2id.model.dto.rtdm.model.OAuthTokenResponse;
import com.sas.rtdm2id.model.id.core.Folder;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
class OtpServiceTest extends Rtdm2idApplicationTests {

    @Autowired
    private OtpService otpService;

    @Autowired
    private TreeUtil treeUtil;

    private String baseIp = "azureuse011343.my-trials.sas.com";
    private String protocol = "https";
    private String accessToken = "eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vbG9jYWxob3N0L1NBU0xvZ29uL3Rva2VuX2tleXMiLCJraWQiOiJsZWdhY3ktdG9rZW4ta2V5IiwidHlwIjoiSldUIn0.eyJqdGkiOiIwZTQ4YjhkMGZiMzQ0ZTcyOTU0MGFjZjUwMWE1ODM2NiIsImV4dF9pZCI6IjAwdWczeTI3ZXNSVWx3RklTMnA3IiwicmVtb3RlX2lwIjoiMTAuNDIuMC4xIiwic2Vzc2lvbl9zaWciOiI5OWRiYzg0MS0yOGY0LTQwMDktODFlMy02MzhmNTY5OTZlNTMiLCJhdXRob3JpdGllcyI6WyJhenVyZXVzZTAxMTM0M191c2VycyIsIkRhdGFCdWlsZGVycyIsIlNjaGVkdWxlU2VydmljZUFjY291bnRVc2VycyIsIkFwcGxpY2F0aW9uQWRtaW5pc3RyYXRvcnMiLCJCYXRjaFNlcnZpY2VBY2NvdW50VXNlcnMiLCJMYXVuY2hlclN1cGVyVXNlcnMiLCJFc3JpVXNlcnMiLCJEYXRhQWdlbnRBZG1pbmlzdHJhdG9ycyIsIlNDSU0iLCJEYXRhQWdlbnRQb3dlclVzZXJzIiwiVXNlciBDb250ZW50IFNoYXJpbmciLCJTQVNTY29yZVVzZXJzIiwiR2xvc3NhcnkuQnVzaW5lc3NHbG9zc2FyeUFkbWluaXN0cmF0b3IiLCJDYXRhbG9nLlN1YmplY3RNYXR0ZXJFeHBlcnRzIiwiQ29tcHV0ZVNlcnZpY2VBY2NvdW50VXNlcnMiXSwic3ViIjoiZmQ4YTViM2UtZWY4My00NGFiLTlkMGItZjdjMjhmMTdlMmM0Iiwic2NvcGUiOlsib3BlbmlkIiwidWFhLnVzZXIiXSwiY2xpZW50X2lkIjoic2FzLmNsaSIsImNpZCI6InNhcy5jbGkiLCJhenAiOiJzYXMuY2xpIiwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInVzZXJfaWQiOiJmZDhhNWIzZS1lZjgzLTQ0YWItOWQwYi1mN2MyOGYxN2UyYzQiLCJvcmlnaW4iOiJleHRlcm5hbF9vYXV0aCIsInVzZXJfbmFtZSI6Im1hcmt1c2NzYWJAZXh0Lm90cGJhbmsuaHUiLCJlbWFpbCI6Im1hcmt1c2NzYWJAZXh0Lm90cGJhbmsuaHUiLCJhdXRoX3RpbWUiOjE3MDgwMTU3OTgsInJldl9zaWciOiIzMzRlZjEwZCIsImlhdCI6MTcwODAxNTc5OCwiZXhwIjoxNzA4MDE5Mzk4LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L1NBU0xvZ29uL29hdXRoL3Rva2VuIiwiemlkIjoidWFhIiwiYXVkIjpbInVhYSIsIm9wZW5pZCIsInNhcy5jbGkiXX0.iU3l83SyBbONQC-lXgYOxfQ8D6c4MmXDuC40KC77lBmd032KuxbVkOIj-qBo2VL0yDKr5LbFXW5b_vCQnsFkc1-QAoxfkQsPJ90mAtbDOtzlAPZlsHR091_2-GJ2NDrMlITGSYn11snQLHv-11SL922R_oBO9Jv3MPasLvU2JexI96CxVeG5EZuZH-qnrkootgMLp3UV313ReK6M-_ij94UQskzjjMjl2e2_Ki7Hr1zSEQzehyTBGXoQdouJI1yKTG4MdMpsTjsIOnisqBKP8lB-kNfTlPQH_p8He7KpyRVbf0kgxBJ0FJVxjYwFMT4Mye6kMl-ohM3vQXxYLAqEDw";

    @Test
    void precessGroovyCode() throws JAXBException, IOException {

//        Resource res = new DefaultResourceLoader().getResource("classpath:DC_runAlternativeOffers_v23.xml");
        Resource res = new DefaultResourceLoader().getResource("classpath:DC_getOffers_v24.xml");

        JAXBContext context = JAXBContext.newInstance(Batch.class);
        Batch batch = (Batch) context.createUnmarshaller()
                .unmarshal(res.getInputStream());

        log.info("DC name: {}", batch.getLogicalUnit().getFlowDO().getName());

        ArrayList<String> processed = new ArrayList<>();
        for (ProcessNodeDataDO p : batch.getLogicalUnit().getProcessNodeDataDOs()) {
            String spName = p.getProcess().getPhysicalName();

            if (!processed.contains(spName) &&
                    "Java code".equalsIgnoreCase(p.getProcess().getProcessTypeDescription())) {

//                log.info("ProcessName: {}", p.getProcess().getPhysicalName());
                String ds2code = otpService.precessGroovyCode(p);
                log.debug("DS2CODE:\n{}", ds2code);
                try {
                    FileOutputStream os = new FileOutputStream("sp/" + spName + ".groovy");
                    os.write(ds2code.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                    throw new RuntimeException(e);
                }

                processed.add(spName);
            }
        }


    }

    @Test
    void extractSasProcesses() throws JAXBException, IOException {

        Resource res = new DefaultResourceLoader().getResource("classpath:DC_runAlternativeOffers_v23.xml");

        JAXBContext context = JAXBContext.newInstance(Batch.class);
        Batch batch = (Batch) context.createUnmarshaller()
                .unmarshal(res.getInputStream());

        log.info("DC name: {}", batch.getLogicalUnit().getFlowDO().getName());

        byte[] bytes = otpService.extractSasProcesses(batch);
        FileOutputStream out = new FileOutputStream("test.zip");
        out.write(bytes);
        out.flush();
        out.close();

    }

    @Test
    void processCalcVars() throws JAXBException, IOException {

        Resource res = new DefaultResourceLoader().getResource("classpath:DC_runAlternativeOffers_v23.xml");

        JAXBContext context = JAXBContext.newInstance(Batch.class);
        Batch batch = (Batch) context.createUnmarshaller()
                .unmarshal(res.getInputStream());

        log.info("DC name: {} - {}", batch.getLogicalUnit().getFlowDO().getName(), batch.getLogicalUnit().getFlowDO().getId());

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
            log.info(" ".repeat(level * 2)+"*** Use Calculated var (NodeId: {}) ***", nodeId);
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

    @Test
    void testToken() throws Exception {

        OAuthTokenResponse auth = otpService.getAuthTokenFromAuthorizationCode(baseIp,
                "wao-TLBBwSaJ0PkhrtuD7LO-ORXlVNSA", protocol);
        log.info("AccessToken: {}", auth.getAccessToken());

        // eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vbG9jYWxob3N0L1NBU0xvZ29uL3Rva2VuX2tleXMiLCJraWQiOiJsZWdhY3ktdG9rZW4ta2V5IiwidHlwIjoiSldUIn0.eyJqdGkiOiI0OTlhNWEwOWI0OGQ0ZGNjYjg2YWM4NjM0NThlYmZhOCIsImV4dF9pZCI6IjAwdWczeTI3ZXNSVWx3RklTMnA3IiwicmVtb3RlX2lwIjoiMTAuNDIuMC4xIiwic2Vzc2lvbl9zaWciOiI5OWRiYzg0MS0yOGY0LTQwMDktODFlMy02MzhmNTY5OTZlNTMiLCJhdXRob3JpdGllcyI6WyJhenVyZXVzZTAxMTM0M191c2VycyIsIkRhdGFCdWlsZGVycyIsIlNjaGVkdWxlU2VydmljZUFjY291bnRVc2VycyIsIkFwcGxpY2F0aW9uQWRtaW5pc3RyYXRvcnMiLCJCYXRjaFNlcnZpY2VBY2NvdW50VXNlcnMiLCJMYXVuY2hlclN1cGVyVXNlcnMiLCJFc3JpVXNlcnMiLCJEYXRhQWdlbnRBZG1pbmlzdHJhdG9ycyIsIlNDSU0iLCJEYXRhQWdlbnRQb3dlclVzZXJzIiwiVXNlciBDb250ZW50IFNoYXJpbmciLCJTQVNTY29yZVVzZXJzIiwiR2xvc3NhcnkuQnVzaW5lc3NHbG9zc2FyeUFkbWluaXN0cmF0b3IiLCJDYXRhbG9nLlN1YmplY3RNYXR0ZXJFeHBlcnRzIiwiQ29tcHV0ZVNlcnZpY2VBY2NvdW50VXNlcnMiXSwic3ViIjoiZmQ4YTViM2UtZWY4My00NGFiLTlkMGItZjdjMjhmMTdlMmM0Iiwic2NvcGUiOlsib3BlbmlkIiwidWFhLnVzZXIiXSwiY2xpZW50X2lkIjoic2FzLmNsaSIsImNpZCI6InNhcy5jbGkiLCJhenAiOiJzYXMuY2xpIiwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInVzZXJfaWQiOiJmZDhhNWIzZS1lZjgzLTQ0YWItOWQwYi1mN2MyOGYxN2UyYzQiLCJvcmlnaW4iOiJleHRlcm5hbF9vYXV0aCIsInVzZXJfbmFtZSI6Im1hcmt1c2NzYWJAZXh0Lm90cGJhbmsuaHUiLCJlbWFpbCI6Im1hcmt1c2NzYWJAZXh0Lm90cGJhbmsuaHUiLCJhdXRoX3RpbWUiOjE3MDgwMTUxNTcsInJldl9zaWciOiIzMzRlZjEwZCIsImlhdCI6MTcwODAxNTE1NywiZXhwIjoxNzA4MDE4NzU3LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L1NBU0xvZ29uL29hdXRoL3Rva2VuIiwiemlkIjoidWFhIiwiYXVkIjpbInVhYSIsIm9wZW5pZCIsInNhcy5jbGkiXX0.C0prfhFt-gcXmWISSNF_LT7zp2mr_mwDVNEMxBGjMGOh-GwUqsN3VYquPMHucTiTZvKJwWIv3IeaxI9eM84yk4_TImvitLm-hL4m-jjPB-5CLTPdPZtUQEPozn0EH5ODmI9PgQAn98DJ_fTc3LMtXXMM5s2lllcWp1ensKOVeiTOc8pbSBsapF3RhsrTpGgWHc--ii1lcU3ATKLVYlzesNqSoQQSLZoeM4AfOHKDlSEKj-STHpkWGvgE_UMvrbqJOvKS-HLABvz3sCpRm7abl2_HE8eb4KJB-PqTDliDNq-jW-Oe0E5w3csS7QCcF2lM743tecGr1kZLzT09MhtiSQ, tokenType=bearer, idToken=eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vbG9jYWxob3N0L1NBU0xvZ29uL3Rva2VuX2tleXMiLCJraWQiOiJsZWdhY3ktdG9rZW4ta2V5IiwidHlwIjoiSldUIn0.eyJzdWIiOiJmZDhhNWIzZS1lZjgzLTQ0YWItOWQwYi1mN2MyOGYxN2UyYzQiLCJhdWQiOlsic2FzLmNsaSJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0L1NBU0xvZ29uL29hdXRoL3Rva2VuIiwiZXhwIjoxNzA4MDE4NzU3LCJpYXQiOjE3MDgwMTUxNTcsImF6cCI6InNhcy5jbGkiLCJzY29wZSI6WyJvcGVuaWQiXSwiZW1haWwiOiJtYXJrdXNjc2FiQGV4dC5vdHBiYW5rLmh1IiwiemlkIjoidWFhIiwib3JpZ2luIjoiZXh0ZXJuYWxfb2F1dGgiLCJqdGkiOiI0OTlhNWEwOWI0OGQ0ZGNjYjg2YWM4NjM0NThlYmZhOCIsInByZXZpb3VzX2xvZ29uX3RpbWUiOjE3MDgwMDEzNjc5ODksImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiY2xpZW50X2lkIjoic2FzLmNsaSIsImNpZCI6InNhcy5jbGkiLCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwidXNlcl9uYW1lIjoibWFya3VzY3NhYkBleHQub3RwYmFuay5odSIsInJldl9zaWciOiI5YzI0NGE0OSIsInVzZXJfaWQiOiJmZDhhNWIzZS1lZjgzLTQ0YWItOWQwYi1mN2MyOGYxN2UyYzQiLCJhdXRoX3RpbWUiOjE3MDgwMTUxNTd9.AvuoaoiUrU-jbUvlXOAVhQ3GVU1ldfIE9P8UiOMkmnmLo_fNCTdc3lcLDP_608c0Sw5qRghqic-C9zrMsbrzcw0Kcl4ovHqwGiJxJSAvgpEF0WnzWuyg5pz2ycom85-7iRMd-wcoCOIv0bLCKslH3Aq7sDAWe527bWsEmvyszDt6F0Sfgp7J46Y6VlEkyDWqEKCj_5Knj_rnMvB3yGrcymg2BWTNe4XdOTktfYaSaYkt0W-Y6DpUBMiqZ9xXvQbZ1zoSNTr59hqeDOWJi9ttW0LFI5NnsYosCCt-Yov2A-1wIPoKKBrZEpe43bVnK_soJe16eyDdFm-yQV8fm7N3Ng

    }

    @Test
    void createFolder() throws Exception {

        Folder folder = otpService.findOrAddFolder(baseIp, "DC_valami_blabla", accessToken, "/Public/migr", protocol);
        log.info("Folder: {}", folder);

    }

}