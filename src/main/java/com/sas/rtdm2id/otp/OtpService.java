package com.sas.rtdm2id.otp;

import com.sas.rtdm2id.model.dto.rtdm.model.OAuthTokenResponse;
import com.sas.rtdm2id.model.id.core.Folder;
import com.sas.rtdm2id.model.id.core.Member;
import com.sas.rtdm2id.model.id.decision.SignatureTerm;
import com.sas.rtdm2id.model.rtdm.Batch;
import com.sas.rtdm2id.model.rtdm.ProcessNodeDataDO;
import com.sas.rtdm2id.model.rtdm.ValueTypeVarInfoDO;
import com.sas.rtdm2id.util.ViyaApi;
import com.sas.rtdm2id.util.object.processing.CommonProcessing;
import com.sas.rtdm2id.util.object.processing.ReservedWords;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.sas.rtdm2id.util.model.RTDM2IDConstants.DOUBLE_CONSTANT;
import static com.sas.rtdm2id.util.model.RTDM2IDConstants.GLOBALS_FOLDER;

@Slf4j
@Service
public class OtpService {
    private static final String NEW_LINE_STRING = "\n";
    private static final String DS2_INDENT = "    ";
    private static final String PARAM_IN = "in";
    private static final String PARAM_OUT = "out";
    private static final int DEFAULT_VARCHAR_LENGTH = 200;
    private static final String PARAM_FORMAT = "%s %s \"%s\""; //""in_out varchar "fortune""
    private static final String EMPTY_STRING = "";
    private static final String UNDERSCORE = "_";

    private final OtpGroovyEngineConfig otpGroovyEngineConfig;
    private final CommonProcessing commonProcessing;
    private final RestTemplate restTemplate;

    private final Environment env;
    private final DefaultResourceLoader loader = new DefaultResourceLoader();



    public OtpService(OtpGroovyEngineConfig otpGroovyEngineConfig, CommonProcessing commonProcessing, RestTemplate restTemplate, Environment env) {
        this.otpGroovyEngineConfig = otpGroovyEngineConfig;
        this.commonProcessing = commonProcessing;
        this.restTemplate = restTemplate;
        this.env = env;
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
                .map(p -> makeNameValid(getVarName(p))).collect(Collectors.toList());
        List<String> outVars = process.getOutputVariableList().getIBVariableDOs().stream()
                .map(p -> makeNameValid(getVarName(p))).collect(Collectors.toList());

        log.info("SAS process: {}", spName);
        log.info("Input variables: {}", inVars);
        log.info("Output variables: {}", outVars);

        inputsPackage.addAll(process.getInputVariableList()
                .getIBVariableDOs()
                .stream()
                .filter(f -> !outVars.contains(getVarName(f)))
                .map(p -> createInVariablesForExecuteMethod(p, PARAM_IN))
                .collect(Collectors.toList()));

        List<String> inputs = process.getInputVariableList()
                .getIBVariableDOs()
                .stream().map(this::getVarName)
                .collect(Collectors.toList());

        List<String> inputAssigns = process.getInputVariableList()
                .getIBVariableDOs()
                .stream().map(p -> SignatureTerm.DataTypeEnum.DATAGRID.equals(commonProcessing.getDatatypeOfVar(p.getTypeDescription())) ?
                        String.format("inputs['%s'] = json.loads(%s)", getVarName(p), getVarName(p)):
                        String.format("inputs['%s'] = %s", getVarName(p), getVarName(p)))
                .collect(Collectors.toList());

        List<String> outputsPackage = process.getOutputVariableList()
                .getIBVariableDOs()
                .stream()
                .map(p -> createOutVariablesForExecuteMethod(p, PARAM_OUT))
                .collect(Collectors.toList());

        List<String> outputs = process.getOutputVariableList()
                .getIBVariableDOs()
                .stream().map(this::getVarName)
                .collect(Collectors.toList());

        List<String> outputAssigns = process.getOutputVariableList()
                .getIBVariableDOs()
                .stream().map(p -> SignatureTerm.DataTypeEnum.DATAGRID.equals(commonProcessing.getDatatypeOfVar(p.getTypeDescription())) ?
                        String.format("%s = json.dumps(out['%s'])", getVarName(p), getVarName(p)):
                        String.format("%s = out['%s']", getVarName(p), getVarName(p)))
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
        ds2Code.append("run;").append(NEW_LINE_STRING);

        process.setDs2code(ds2Code.toString());

        if (StringUtils.isNotEmpty(otpGroovyEngineConfig.getSpExtractPath())) {
            String path = otpGroovyEngineConfig.getSpExtractPath();
            path += path.endsWith("/") ? "": "/";
            try {
                File file = loader.getResource(path + spName + ".groovy").getFile();
                if (file.exists()) {
                    if (otpGroovyEngineConfig.isSpExtractOverwrite()) {
                        if (!file.delete()) {
                            throw new Exception("File delete failed! (Path: "+path + spName + ")");
                        }
                    } else {
                        throw new FileAlreadyExistsException("SAS process file already exists! (Path: "+path + spName + ".groovy"+")");
                    }
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(ds2Code.toString().getBytes(StandardCharsets.UTF_8));
                fos.flush();
                fos.close();
            } catch (FileAlreadyExistsException e) {
                log.error(e.getLocalizedMessage());
            } catch (Exception e) {
                log.error("Extract SAS process failed! (Path: "+path + spName + ".groovy"+")");
                log.error(e.getLocalizedMessage());
            }
        }

        return ds2Code.toString();
    }

    private String createInVariablesForExecuteMethod(ProcessNodeDataDO.Process.InputVariableList.IBVariableDO ibVariableDO, String direction) {
        SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());
        StringBuilder variable = new StringBuilder();

        String datatypeOfVarString;
        if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATAGRID)) {
            datatypeOfVarString = "package datagrid";
        } else if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL) ||
                datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER) ||
                datatypeOfVar.equals(SignatureTerm.DataTypeEnum.BOOLEAN) ||
                datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATETIME)) {
            datatypeOfVarString = DOUBLE_CONSTANT;
        } else {
            datatypeOfVarString = "varchar("+DEFAULT_VARCHAR_LENGTH+")";
        }

        if (direction.equals(PARAM_OUT)) variable.append("in_out ");
        variable
                .append(datatypeOfVarString)
                .append(" ")
                .append(getVarName(ibVariableDO));
        return variable.toString();
    }

    private String createOutVariablesForExecuteMethod(ProcessNodeDataDO.Process.OutputVariableList.IBVariableDO ibVariableDO, String direction) {
        SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());
        StringBuilder variable = new StringBuilder();

        String datatypeOfVarString;
        if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATAGRID)) {
            datatypeOfVarString = "package datagrid";
        } else if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL) ||
                datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER) ||
                datatypeOfVar.equals(SignatureTerm.DataTypeEnum.BOOLEAN) ||
                datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATETIME)) {
            datatypeOfVarString = DOUBLE_CONSTANT;
        } else {
            datatypeOfVarString = "varchar("+DEFAULT_VARCHAR_LENGTH+")";
        }
        if (direction.equals(PARAM_OUT)) variable.append("in_out ");
        variable
                .append(datatypeOfVarString)
                .append(" ")
                .append(getVarName(ibVariableDO));
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

            String parName = getVarName(ibVariableDO);
            SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());

            String setter = "String";
            if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL) ||
                    datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER) ||
                    datatypeOfVar.equals(SignatureTerm.DataTypeEnum.BOOLEAN) ||
                    datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATETIME)) {
                setter = "Double";
            }

            if ("data grid".equalsIgnoreCase(ibVariableDO.getTypeDescription())) {
                result.append(DS2_INDENT)
                        .append(String.format("rc = py.setString('%s', %s.serialize());  ", parName, parName))
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

            String parName = getVarName(ibVariableDO);
            SignatureTerm.DataTypeEnum datatypeOfVar = commonProcessing.getDatatypeOfVar(ibVariableDO.getTypeDescription());

            String setter = "String";
            if (datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DECIMAL) ||
                    datatypeOfVar.equals(SignatureTerm.DataTypeEnum.INTEGER) ||
                    datatypeOfVar.equals(SignatureTerm.DataTypeEnum.BOOLEAN) ||
                    datatypeOfVar.equals(SignatureTerm.DataTypeEnum.DATETIME)) {
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

    public OAuthTokenResponse getAuthTokenFromAuthorizationCode(String baseIp, String authorizationCode, String protocol) throws Exception {

        URI uri = ViyaApi.createUri(baseIp, "/SASLogon/oauth/token", protocol);
        String body = String.format("grant_type=authorization_code&code=%s", authorizationCode);
        HttpHeaders header = new HttpHeaders();
        header.setBasicAuth("sas.cli","");
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<OAuthTokenResponse> oauth = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(body, header), OAuthTokenResponse.class);
        if (oauth.getStatusCode().value() != 200) {
            throw new Exception("Token generation failed!");
        }

        return oauth.getBody();
    }

    public Folder findOrAddFolder(String baseIp, String folderName, String accessToken, String parentFolderUri, String protocol)
            throws Exception {

        URI uri = ViyaApi.createUri(baseIp, "/folders/folders/@item?path="+parentFolderUri+"/"+folderName, protocol);
        try {
            ResponseEntity<Member> member = restTemplate.exchange(uri, HttpMethod.GET, createHTTPEntity(accessToken, null), Member.class);
            if (member.getStatusCode().value() == 200) {
                uri = ViyaApi.createUri(baseIp, "/folders/folders/" + Objects.requireNonNull(member.getBody()).getId(), protocol);
                ResponseEntity<Folder> folderResp = restTemplate.exchange(uri, HttpMethod.GET, createHTTPEntity(accessToken, null), Folder.class);
                return folderResp.getBody();
            }
        } catch (HttpClientErrorException.NotFound e) {

            uri = ViyaApi.createUri(baseIp, "/folders/folders/@item?path="+parentFolderUri, protocol);
            ResponseEntity<Member> parentMember = restTemplate.exchange(uri, HttpMethod.GET, createHTTPEntity(accessToken, null), Member.class);
            if (parentMember.getStatusCode().value() != 200) {
                throw new Exception("ParentFolder not found!");
            }

            Folder newFolder = new Folder();
            newFolder.setName(folderName);
            newFolder.setDescription(folderName);
            newFolder.setType("folder");
            URI uriCreate = ViyaApi.createUri(baseIp, "/folders/folders?parentFolderUri=/folders/folders/" +
                    Objects.requireNonNull(parentMember.getBody()).getId(), protocol);
            ResponseEntity<Folder> createFolder = restTemplate.exchange(uriCreate, HttpMethod.POST, createHTTPEntity(accessToken, newFolder), Folder.class);
            if (createFolder.getStatusCode().value() != 201) {
                throw new Exception("Folder creation failed! (HTTP StatusCode: "+createFolder.getStatusCode().value()+")");
            }

            return createFolder.getBody();


        }

        return null;

    }

    public byte[] extractSasProcesses(Batch batch) throws IOException {

        List<String> processed = new ArrayList<>();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ZipOutputStream zip = new ZipOutputStream(baos);

        for (ProcessNodeDataDO processNodeDataDO : batch.getLogicalUnit().getProcessNodeDataDOs()) {
            if ("Java code".equalsIgnoreCase(processNodeDataDO.getProcess().getProcessTypeDescription())) {
                String spName = processNodeDataDO.getProcess().getPhysicalName();
                if (!processed.contains(spName)) {

                    byte[] ds2 = processNodeDataDO.getProcess().getDs2code().getBytes(StandardCharsets.UTF_8);
                    ZipEntry entry = new ZipEntry(spName + ".groovy");
                    entry.setSize(ds2.length);
                    zip.putNextEntry(entry);
                    zip.write(ds2);
                    zip.closeEntry();

                    processed.add(spName);
                }
            }
        }
        zip.close();

        return baos.toByteArray();
    }

    public boolean isOtpEnvironment() {
        return List.of(env.getActiveProfiles()).contains("otp");
    }

    public ValueTypeVarInfoDO createValueTypeVarInfoDOForGlobalVar(String varName) {
        ValueTypeVarInfoDO varInfo = new ValueTypeVarInfoDO();

        varInfo.setArchivedPublishState((byte) -1);
        varInfo.setCodeChangeUnpublished("false");
        varInfo.setCodeEverBeenPublished("false");
        varInfo.setFirstOccurrenceToPublish((byte) 1);
        varInfo.setLoadedFromOldDavLocation("false");
        varInfo.setLoading("false");
        varInfo.setNeedToPersist("false");
//        varInfo.setOldVersionNumber();
        varInfo.setPersisting("false");
        varInfo.setPublishState((byte) 2);
        varInfo.setVarInfoArray("false");
        varInfo.setVarInfoId(GLOBALS_FOLDER + "." + varName);
        varInfo.setVarName(varName);
        varInfo.setVarInfoPhysicalName(varName);
        varInfo.setVarInfoSource("Global");
        varInfo.setVarInfoSubtype("None");
        varInfo.setType("Char");
        varInfo.setVersionNumber(6.4f);

        return varInfo;
    }

    public ProcessNodeDataDO.Process.InputVariableList.IBVariableDO.Value createValue(ValueTypeVarInfoDO valueTypeVarInfoDO) {

        ProcessNodeDataDO.Process.InputVariableList.IBVariableDO.Value value = new ProcessNodeDataDO.Process.InputVariableList.IBVariableDO.Value();

        value.setArchivedPublishState((byte) -1);
        value.setCodeChangeUnpublished("false");
        value.setCodeEverBeenPublished("false");
        value.setFirstOccurrenceToPublish((byte) 1);
        value.setLoadedFromOldDavLocation("false");
        value.setLoading("false");
        value.setNeedToPersist("false");
//        value.setOldVersionNumber();
        value.setValueTypeVarInfoDO(valueTypeVarInfoDO);
        value.setPersisting("false");
        value.setPublishState((byte) 2);
        value.setType((byte) 11);
        value.setVersionNumber(6.4f);

        return value;
    }

    public ProcessNodeDataDO.Process.InputVariableList.IBVariableDO.Value createValue(String stringValue) {

        ProcessNodeDataDO.Process.InputVariableList.IBVariableDO.Value value = new ProcessNodeDataDO.Process.InputVariableList.IBVariableDO.Value();

        value.setArchivedPublishState((byte) -1);
        value.setCodeChangeUnpublished("false");
        value.setCodeEverBeenPublished("false");
        value.setFirstOccurrenceToPublish((byte) 1);
        value.setLoadedFromOldDavLocation("false");
        value.setLoading("false");
        value.setNeedToPersist("false");
//        value.setOldVersionNumber();
        value.setStringValue(stringValue);
        value.setPersisting("false");
        value.setPublishState((byte) 2);
        value.setType((byte) 2);
        value.setVersionNumber(6.4f);

        return value;
    }


    public ProcessNodeDataDO.Process.InputVariableList.IBVariableDO createIBVariableDO(String varName,
                                                                                       ProcessNodeDataDO.Process.InputVariableList.IBVariableDO.Value value) {

        ProcessNodeDataDO.Process.InputVariableList.IBVariableDO spvar = new ProcessNodeDataDO.Process.InputVariableList.IBVariableDO();

        spvar.setArchivedPublishState((byte) -1);
        spvar.setAttachments("false");
        spvar.setCodeChangeUnpublished("false");
        spvar.setCodeEverBeenPublished("false");
        spvar.setContainsAllPossibleValues("false");
        spvar.setDefaultValueIsMissing("false");
//            spvar.setDescription(spvar.set/Description(
        spvar.setFirstOccurrenceToPublish((byte) 1);
        spvar.setForceOverwrite("false");
        spvar.setForced("false");
        spvar.setHidden("false");
        spvar.setHideVariable("false");
//            spvar.setId(spvar.set/Id(
        spvar.setLevel("Nominal");
        spvar.setLoadedFromOldDavLocation("false");
        spvar.setLoading("false");
//            spvar.setLockedBy(spvar.set/LockedBy(
        spvar.setMetadataType("Group");
        spvar.setName(varName);
        spvar.setNeedToPersist("false");
        spvar.setNoWritePermission("false");
//            spvar.setOldVersionNumber(spvar.set/OldVersionNumber(
        spvar.setPersistState("persistStateOK");
        spvar.setPersisting("false");
        spvar.setPhysicalName(varName);
        spvar.setPresentInDS2Code("false");
//            spvar.setProcessVariableName(_ACTIN_HAHHVCXSDNEBQM3M_BASEURL_5I1BMQ_Cspvar.set/ProcessVariableName(
        spvar.setPublishState((byte) 2);
        spvar.setReadOnly("false");
        spvar.setRequired("false");
        spvar.setSelected("true");
        spvar.setShared("false");
//            spvar.setSharedIn EmptyList="true"(spvar.set/SharedIn(
//            spvar.setTestVarInfoId(spvar.set/TestVarInfoId(
        spvar.setType((byte) 1);
        spvar.setTypeDescription("string");
        spvar.setValue(value);
        spvar.setVersionNumber(6.4f);
        spvar.setWhereClauseOp((byte) 0);
//            spvar.setWhoModified></WhoModified>


        return spvar;
    }

    private String getVarName(ProcessNodeDataDO.Process.InputVariableList.IBVariableDO ibVariableDO) {
        return ibVariableDO.getPhysicalName();
//        return makeNameValid(ibVariableDO.getName());
    }

    private String getVarName(ProcessNodeDataDO.Process.OutputVariableList.IBVariableDO ibVariableDO) {
        return ibVariableDO.getPhysicalName();
//        return makeNameValid(ibVariableDO.getName());
    }

    private boolean isReservedWord(String value) {
        return value != null && ReservedWords.isReservedWord(value.toUpperCase() );
    }

    private String makeNameValid(String name) {
        if (isReservedWord(name)) {
            // Some reserved words start with an underscore so to avoid confusion prefix with a letter
            name = "r" + UNDERSCORE + name;
        }

        // Check if the string starts with a numeric character
        boolean startsWithNumeric = Character.isDigit(name.charAt(0));

        // If it starts with a numeric character, prefix with an underscore
        if (startsWithNumeric) {
            name = UNDERSCORE + name;
        }

        if (name.matches("(?i)[a-z][a-z0-9_]*")) {
            return name;
        } else {
            // signature item only allows alphanumeric and underscore
            return name.replaceAll("[^a-zA-Z0-9_]", EMPTY_STRING);
        }
    }

}
