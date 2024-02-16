/*
Copyright © 2023, SAS Institute Inc., Cary, NC, USA.  All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.sas.rtdm2id.controller;

import com.sas.rtdm2id.model.dto.Meta;
import com.sas.rtdm2id.model.dto.Rtdm2IdResponse;
import com.sas.rtdm2id.model.dto.rtdm.model.ClientIdResponse;
import com.sas.rtdm2id.model.rtdm.Batch;
import com.sas.rtdm2id.otp.OtpService;
import com.sas.rtdm2id.service.ApplicationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin
@RequestMapping("api/rtdm2id")
@Api(tags = "Application methods")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final OtpService otpService;

    public ApplicationController(ApplicationService applicationService, OtpService otpService) {
        this.applicationService = applicationService;
        this.otpService = otpService;
    }

    @PostMapping(value = "create-diagram", consumes = MediaType.APPLICATION_XML_VALUE)
    @ApiOperation(value = "Create diagram",
            notes = "Create diagram inside ID")
    public ResponseEntity<Rtdm2IdResponse<String>> createDiagram(
            @ApiParam(value = "Base ip of Viya server", required = true) @RequestParam String baseIp,
            @ApiParam(value = "RTDM XML", required = true) @RequestBody Batch xml,
            @ApiParam(value = "token") @RequestParam String token,
            @ApiParam(value = "login") @RequestParam String login,
            @ApiParam(value = "password") @RequestParam String password,
            @RequestParam(value = "parentFolderUri", defaultValue = "/folders/folders/@myFolder", required = false) String parentFolderUri,
            @RequestParam(value = "protocol", defaultValue = "http", required = false) String protocol) throws Exception {
        return new ResponseEntity<>(new Rtdm2IdResponse<>(new Meta(0, "OK"), applicationService.createDiagram(baseIp, xml, token, login, password, protocol, parentFolderUri))
                , HttpStatus.CREATED);
    }

    @PostMapping(value = "extract-sas-processes", consumes = MediaType.APPLICATION_XML_VALUE,
            produces = "application/zip")
    @ApiOperation(value = "Extract SAS processes",
            notes = "Extract SAS processes from RTDM xml")
    public ResponseEntity<byte[]> extractSasProcesses(
            @ApiParam(value = "RTDM XML", required = true) @RequestBody Batch xml) throws IOException {

        return new ResponseEntity<>(otpService.extractSasProcesses(xml), HttpStatus.CREATED);
    }


    @PostMapping("register-client")
    public ResponseEntity<Rtdm2IdResponse<ClientIdResponse>> registerClient(@ApiParam(value = "Base IP of Viya server", required = true)
                                                                            @RequestParam String baseIp,
                                                                            @RequestParam(value = "protocol", defaultValue = "http", required = false) String protocol,
                                                                            @ApiParam(value = "Consul token got from Viya server", required = true)
                                                                            @RequestParam String consulToken,
                                                                            @ApiParam(value = "Id of new user", required = true)
                                                                            @RequestParam String clientId,
                                                                            @ApiParam(value = "Secret of new user", required = true)
                                                                            @RequestParam String clientSecret) {
        return new ResponseEntity<>(new Rtdm2IdResponse<>(new Meta(0, "OK"), applicationService.registerClient(baseIp, consulToken, clientId, clientSecret,protocol)),
                HttpStatus.CREATED);
    }

}
