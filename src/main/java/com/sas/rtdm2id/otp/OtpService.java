package com.sas.rtdm2id.otp;

import com.sas.rtdm2id.mapper.exception.FolderCreationException;
import com.sas.rtdm2id.model.id.core.Folder;
import com.sas.rtdm2id.model.id.core.FolderCollection;
import com.sas.rtdm2id.model.id.core.Member;
import com.sas.rtdm2id.model.id.decision.SignatureTerm;
import com.sas.rtdm2id.model.rtdm.ProcessNodeDataDO;
import com.sas.rtdm2id.util.ViyaApi;
import com.sas.rtdm2id.util.object.processing.CommonProcessing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.sas.rtdm2id.util.model.RTDM2IDConstants.DOUBLE_CONSTANT;

@Slf4j
@Service
public class OtpService {
    private static final String NEW_LINE_STRING = "\n";
    private static final String DS2_INDENT = "    ";
    private static final String PARAM_IN = "in";
    private static final String PARAM_OUT = "out";
    private static final int DEFAULT_VARCHAR_LENGTH = 200;
    private static final String PARAM_FORMAT = "%s %s \"%s\""; //""in_out varchar "fortune""

    private final OtpGroovyEngineConfig otpGroovyEngineConfig;
    private final CommonProcessing commonProcessing;
    private final RestTemplate restTemplate;


    public OtpService(OtpGroovyEngineConfig otpGroovyEngineConfig, CommonProcessing commonProcessing, RestTemplate restTemplate) {
        this.otpGroovyEngineConfig = otpGroovyEngineConfig;
        this.commonProcessing = commonProcessing;
        this.restTemplate = restTemplate;
        log.info("OtpGroovyEngineConfig: {}", otpGroovyEngineConfig);
    }

    public String precessGroovyCode(ProcessNodeDataDO processNodeDataDO) {

        ProcessNodeDataDO.Process process = processNodeDataDO.getProcess();
        String spName = process.getPhysicalName();

        List<String> inputsPackage = new ArrayList<>();
        inputsPackage.add("varchar("+DEFAULT_VARCHAR_LENGTH+") ge_url");
        inputsPackage.add("varchar("+DEFAULT_VARCHAR_LENGTH+") ge_username");
        inputsPackage.add("varchar("+DEFAULT_VARCHAR_LENGTH+") ge_pass");
        inputsPackage.add("varchar("+DEFAULT_VARCHAR_LENGTH+") sp_name");
        inputsPackage.add("varchar("+DEFAULT_VARCHAR_LENGTH+") correlation_id");

        List<String> inVars = process.getInputVariableList().getIBVariableDOs().stream()
                .map(ProcessNodeDataDO.Process.InputVariableList.IBVariableDO::getPhysicalName).collect(Collectors.toList());
        List<String> outVars = process.getOutputVariableList().getIBVariableDOs().stream()
                .map(ProcessNodeDataDO.Process.OutputVariableList.IBVariableDO::getPhysicalName).collect(Collectors.toList());

        log.info("Input variables: {}", inVars);
        log.info("Output variables: {}", outVars);

        inputsPackage.addAll(process.getInputVariableList()
                .getIBVariableDOs()
                .stream()
                .filter(f -> !outVars.contains(f.getPhysicalName()))
                .map(p -> createInVariablesForExecuteMethod(p, PARAM_IN))
                .collect(Collectors.toList()));

        List<String> inputs = process.getInputVariableList()
                .getIBVariableDOs()
                .stream().map(ProcessNodeDataDO.Process.InputVariableList.IBVariableDO::getPhysicalName)
                .collect(Collectors.toList());

        List<String> inputAssigns = process.getInputVariableList()
                .getIBVariableDOs()
                .stream().map(p -> String.format("inputs['%s'] = %s", p.getPhysicalName(), p.getPhysicalName()))
                .collect(Collectors.toList());

        List<String> outputsPackage = process.getOutputVariableList()
                .getIBVariableDOs()
                .stream()
                .map(p -> createOutVariablesForExecuteMethod(p, PARAM_OUT))
                .collect(Collectors.toList());

        List<String> outputs = process.getOutputVariableList()
                .getIBVariableDOs()
                .stream().map(ProcessNodeDataDO.Process.OutputVariableList.IBVariableDO::getPhysicalName)
                .collect(Collectors.toList());

        List<String> outputAssigns = process.getOutputVariableList()
                .getIBVariableDOs()
                .stream().map(p -> String.format("%s = out['%s']", p.getPhysicalName(), p.getPhysicalName()))
                .collect(Collectors.toList());


        String template = new String(otpGroovyEngineConfig.getPythonRestCallTemplate(), StandardCharsets.UTF_8);
        template = template.replace("{INPUT_PARAMETERS}", String.join(", ", inputs));
        template = template.replace("{OUTPUT_PARAMETERS}", String.join(", ", outputs));
        template = template.replace("{INPUT_PARAMETER_ASSIGNS}", String.join( "\n    ", inputAssigns));
        template = template.replace("{OUTPUT_PARAMETER_ASSIGNS}", String.join("\n        ", outputAssigns));

        StringBuilder ds2Code = new StringBuilder();
        ds2Code.append("package \"${PACKAGE_NAME}\" /overwrite=yes ;").append(NEW_LINE_STRING);
        ds2Code.append("/*   to be generated from \""+process.getPhysicalName()+"\"   */").append(NEW_LINE_STRING);
        ds2Code.append(
                    String.format("  method execute(%s);",
                            String.join(", ", inputsPackage) + ", " + String.join(", ", outputsPackage))
                ).append(NEW_LINE_STRING);


        ds2Code.append(DS2_INDENT).append("dcl package pymas py;").append(NEW_LINE_STRING);
        ds2Code.append(DS2_INDENT).append("dcl double pystop;").append(NEW_LINE_STRING);
        ds2Code.append(DS2_INDENT).append("dcl nvarchar(10485760) pypgm;").append(NEW_LINE_STRING);
        ds2Code.append(DS2_INDENT).append("dcl double revision;").append(NEW_LINE_STRING);
        ds2Code.append(DS2_INDENT).append("dcl double rc;").append(NEW_LINE_STRING);



        ds2Code.append("    py = _new_ pymas();").append(NEW_LINE_STRING);
        ds2Code.append("    rc = py.useModule('\"CODE_FILE_SCORE_py_-"+spName+"\"', 1);").append(NEW_LINE_STRING);
        ds2Code.append("    rc = py.appendSrcLine(''''''' List all output parameters as comma-separated values in the \"Output:\" docString. Do not specify \"None\" if there is no output parameter. ''''''');").append(NEW_LINE_STRING);

        String pythonized = Arrays.stream(template.split("\n"))
                .map(s ->  String.format("rc = py.appendSrcLine('%s');", s.replace("'", "''")))
                .collect(Collectors.joining("\n" + DS2_INDENT));
        ds2Code.append(DS2_INDENT).append(pythonized).append(NEW_LINE_STRING);


        ds2Code.append(DS2_INDENT).append("pypgm = py.getSource();").append(NEW_LINE_STRING);
        ds2Code.append(DS2_INDENT).append("revision = py.publish(pypgm, '\"CODE_FILE_SCORE_py_-"+spName+"\"');").append(NEW_LINE_STRING);
        ds2Code.append(DS2_INDENT).append("rc = py.useMethod('execute');").append(NEW_LINE_STRING);
        ds2Code.append(NEW_LINE_STRING);

        ds2Code.append(
                createVarSettingAndExecute(
                    process.getInputVariableList().getIBVariableDOs(),
                    process.getOutputVariableList().getIBVariableDOs()));

        ds2Code.append(NEW_LINE_STRING);

        ds2Code.append("  end;").append(NEW_LINE_STRING);
        ds2Code.append("endpackage;").append(NEW_LINE_STRING);
        //ds2Code.append("run;").append(NEW_LINE_STRING);

        process.setDs2code(ds2Code.toString());

        return ds2Code.toString();
    }

    private String createInVariablesForExecuteMethod(ProcessNodeDataDO.Process.InputVariableList.IBVariableDO ibVariableDO, String direction) {
        SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());
        StringBuilder variable = new StringBuilder();

        String datatypeOfVarString;
        if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATAGRID)) {
            datatypeOfVarString = "package datagrid";
        } else if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL)
                || datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER)) {
            datatypeOfVarString = DOUBLE_CONSTANT;
        } else {
            datatypeOfVarString = "varchar("+DEFAULT_VARCHAR_LENGTH+")";
        }
        if (direction.equals(PARAM_OUT)) variable.append("in_out ");
        variable
                .append(datatypeOfVarString)
                .append(" ")
                .append(ibVariableDO.getName());
        return variable.toString();
    }

    private String createOutVariablesForExecuteMethod(ProcessNodeDataDO.Process.OutputVariableList.IBVariableDO ibVariableDO, String direction) {
        SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());
        StringBuilder variable = new StringBuilder();

        String datatypeOfVarString;
        if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATAGRID)) {
            datatypeOfVarString = "package datagrid";
        } else if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL)
                || datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER)) {
            datatypeOfVarString = DOUBLE_CONSTANT;
        } else {
            datatypeOfVarString = "varchar";
        }
        if (direction.equals(PARAM_OUT)) variable.append("in_out ");
        variable
                .append(datatypeOfVarString)
                .append(" ")
                .append(ibVariableDO.getName());
        return variable.toString();
    }

    private StringBuilder createVarSettingAndExecute(List<ProcessNodeDataDO.Process.InputVariableList.IBVariableDO> inVariableDOs,
                                                     List<ProcessNodeDataDO.Process.OutputVariableList.IBVariableDO> outVariableDOs) {

        StringBuilder result = new StringBuilder();

        for (String s : List.of("ge_url", "ge_username", "ge_pass", "sp_name", "correlation_id")) {
            result.append(DS2_INDENT)
                    .append(String.format("rc = py.setString('%s', %s);", s, s))
                    .append(NEW_LINE_STRING);
        }

        for (ProcessNodeDataDO.Process.InputVariableList.IBVariableDO ibVariableDO
                : inVariableDOs) {

            String parName = ibVariableDO.getPhysicalName();
            SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());

            String setter = "String";
            if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL)
                    || datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER)) {
                setter = "Double";
            }

            if ("data grid".equalsIgnoreCase(ibVariableDO.getTypeDescription())) {
                result.append(DS2_INDENT)
                        .append(String.format("rc = py.setString('%s', datagrid_tostring(%s));  ", parName, parName))
                        .append(NEW_LINE_STRING);
            } else {
                result.append(DS2_INDENT)
                        .append(String.format("rc = py.set%s('%s', %s);", setter, parName, parName))
                        .append(NEW_LINE_STRING);
            }

        }

        result.append(NEW_LINE_STRING);
        result.append(DS2_INDENT).append("rc = py.execute();\n");
        result.append(NEW_LINE_STRING);

        for (ProcessNodeDataDO.Process.OutputVariableList.IBVariableDO ibVariableDO
                : outVariableDOs) {

            String parName = ibVariableDO.getPhysicalName();
            SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());

            String setter = "String";
            if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL)
                    || datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER)) {
                setter = "Double";
            }

            if ("data grid".equalsIgnoreCase(ibVariableDO.getTypeDescription())) {
                result.append(DS2_INDENT)
                        .append(String.format("%s.deserialize(py.getString('%s'));", parName, parName))
                        .append(NEW_LINE_STRING);
            } else {
                result.append(DS2_INDENT)
                        .append(String.format("%s = py.get%s('%s');", parName, setter, parName))
                        .append(NEW_LINE_STRING);
            }
        }


        return result;
    }

    public static HttpEntity<Object> createHTTPEntity(String accessToken, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public Folder findOrAddFolder(String baseIp, String folderName, String accessToken, String parentFolderUri, String protocol)
            throws Exception {

        URI uri = ViyaApi.createUri(baseIp, "/folders/folders/@item?path="+parentFolderUri+"/"+folderName, protocol);
        ResponseEntity<Member> member = restTemplate.exchange(uri, HttpMethod.GET, createHTTPEntity(accessToken, null), Member.class);
        if (member.getStatusCode().value() == 200) {
            uri = ViyaApi.createUri(baseIp, "/folders/folders/" + Objects.requireNonNull(member.getBody()).getId(), protocol);
            ResponseEntity<Folder> folderResp = restTemplate.exchange(uri, HttpMethod.GET, createHTTPEntity(accessToken, null), Folder.class);
            return folderResp.getBody();
        }

        uri = ViyaApi.createUri(baseIp, "/folders/folders/@item?path="+parentFolderUri, protocol);
        ResponseEntity<Member> parentMember = restTemplate.exchange(uri, HttpMethod.GET, createHTTPEntity(accessToken, null), Member.class);
        if (parentMember.getStatusCode().value() != 200) {
            throw new Exception("ParentFolder not found!");
        }

        Folder newFolder = new Folder();
        newFolder.setName(folderName);
        newFolder.setDescription(folderName);
        newFolder.setType("folder");
        URI uriCreate = ViyaApi.createUri(baseIp, "/folders/folders", protocol);
        ResponseEntity<Folder> createFolder = restTemplate.exchange(uriCreate, HttpMethod.POST, createHTTPEntity(accessToken, newFolder), Folder.class);
        if (createFolder.getStatusCode().value() != 200) {
            throw new Exception("Folder creation failed! (HTTP StatusCode: "+createFolder.getStatusCode().value()+")");
        }

        return createFolder.getBody();
    }

    public Folder findOrAddSubFolder(String baseIp, String folderName, String accessToken, String parentFolderUri, String protocol) {
        Folder folder = null;
        try {
            URI uri = ViyaApi.createUriWithParams(baseIp, "/folders/folders", "name", folderName, protocol);

            ResponseEntity<FolderCollection> getResponse = restTemplate.exchange(
                    uri, HttpMethod.GET, ViyaApi.createGetByNameForFolders(accessToken), FolderCollection.class);
            if (getResponse.getStatusCode().value() == 200) {
                Optional<Folder> first = getResponse.getBody()
                        .getItems()
                        .stream()
                        .filter(o -> o.getParentFolderUri().endsWith(parentFolderUri))
                        .findFirst();
                if (first.isPresent()) {
                    folder = first.get();
                } else {
                    // Create folder
                    log.info("Creating folder \"{}\"", folderName);
                    folder = new Folder();
                    folder.setName(folderName);
                    folder.setDescription(folderName);
                    folder.setType("folder");

                    ResponseEntity<Folder> getResponseCreate = restTemplate.exchange(
                            baseIp + "/folders/folders?" +
                                    "parentFolderUri=" +
                                    "/folders/folders/"+parentFolderUri
                            , HttpMethod.POST,
                            ViyaApi.createPostFolders(folder, accessToken), Folder.class);
                    if (getResponseCreate.getStatusCode().value() == 201) {
                        folder = getResponseCreate.getBody();
                    } else {
                        folder = null;
                        throw new FolderCreationException(String.format("Folder create error! (HttpStatusCode: %d)",getResponseCreate.getStatusCode().value()));
                    }
                }

            } else {
                throw new FolderCreationException(String.format("Folder find error! (HttpStatusCode: %d)",getResponse.getStatusCode().value()));
            }

        } catch (URISyntaxException e) {
            throw new FolderCreationException(e.getMessage(), e);
        }

        return folder;
    }


}