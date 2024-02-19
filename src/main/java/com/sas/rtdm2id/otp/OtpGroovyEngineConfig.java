package com.sas.rtdm2id.otp;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "groovy-eng")
@PropertySource("classpath:otp_config.properties")
public class OtpGroovyEngineConfig {
    private String url;
    private String username;
    private String password;
    private String pythonRestCallTemplateName;
    @ToString.Exclude
    private byte[] pythonRestCallTemplate;
    private String spIdSubdir;
    private String spExtractPath;
    private boolean spExtractOverwrite;

    public OtpGroovyEngineConfig() {
        super();
    }

    @PostConstruct
    private void init() {

        try {
            InputStream loader = new DefaultResourceLoader().getResource(pythonRestCallTemplateName).getInputStream();
            this.pythonRestCallTemplate = loader.readAllBytes();
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }

    }

}
