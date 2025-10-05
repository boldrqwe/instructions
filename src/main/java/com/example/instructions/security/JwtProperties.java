package com.example.instructions.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@ConfigurationProperties("app.jwt")
public class JwtProperties {

    private static final Duration DEFAULT_ACCESS_TOKEN_TTL = Duration.ofHours(1);
    private static final String DEFAULT_ISSUER = "instructions-api";

    private String secret;
    private Duration accessTokenTtl;
    private String issuer;

    public String getSecret() {
        Assert.hasText(secret, "JWT secret must not be empty");
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl != null ? accessTokenTtl : DEFAULT_ACCESS_TOKEN_TTL;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public String getIssuer() {
        return StringUtils.hasText(issuer) ? issuer : DEFAULT_ISSUER;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
