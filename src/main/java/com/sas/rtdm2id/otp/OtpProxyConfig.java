package com.sas.rtdm2id.otp;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "proxy")
@PropertySource("classpath:otp_config.properties")
public class OtpProxyConfig {
    private String host;
    private Integer port;
    private String user;
    @ToString.Exclude
    private String pass;

}
