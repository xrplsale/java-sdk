package com.xrplsale.spring;

import com.xrplsale.Environment;
import com.xrplsale.XRPLSaleClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * Spring Boot Auto Configuration for XRPL.Sale SDK
 * 
 * <p>This configuration automatically sets up the XRPL.Sale client based on
 * application properties and provides Spring-specific integrations.</p>
 * 
 * <p>To enable auto-configuration, add the following to your application.properties:</p>
 * <pre>
 * xrplsale.api-key=your-api-key
 * xrplsale.environment=production
 * xrplsale.webhook-secret=your-webhook-secret
 * </pre>
 */
@Slf4j
@Configuration
@ConditionalOnClass(XRPLSaleClient.class)
@ConditionalOnProperty(prefix = "xrplsale", name = "api-key")
@EnableConfigurationProperties(XRPLSaleProperties.class)
public class XRPLSaleAutoConfiguration {
    
    /**
     * Creates the XRPL.Sale client bean
     */
    @Bean
    @ConditionalOnMissingBean
    public XRPLSaleClient xrplSaleClient(XRPLSaleProperties properties) {
        log.info("Configuring XRPL.Sale client with environment: {}", properties.getEnvironment());
        
        XRPLSaleClient.XRPLSaleClientBuilder builder = XRPLSaleClient.builder()
            .apiKey(properties.getApiKey())
            .environment(Environment.valueOf(properties.getEnvironment().toUpperCase()));
        
        if (properties.getBaseUrl() != null) {
            builder.baseUrl(properties.getBaseUrl());
        }
        
        if (properties.getConnectTimeout() != null) {
            builder.connectTimeout(Duration.ofMillis(properties.getConnectTimeout()));
        }
        
        if (properties.getReadTimeout() != null) {
            builder.readTimeout(Duration.ofMillis(properties.getReadTimeout()));
        }
        
        if (properties.getWriteTimeout() != null) {
            builder.writeTimeout(Duration.ofMillis(properties.getWriteTimeout()));
        }
        
        if (properties.getMaxRetries() != null) {
            builder.maxRetries(properties.getMaxRetries());
        }
        
        if (properties.getRetryDelay() != null) {
            builder.retryDelay(Duration.ofMillis(properties.getRetryDelay()));
        }
        
        if (properties.getWebhookSecret() != null) {
            builder.webhookSecret(properties.getWebhookSecret());
        }
        
        builder.debug(properties.isDebug());
        
        return builder.build();
    }
    
    /**
     * Creates the webhook interceptor for Spring MVC
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(HandlerInterceptor.class)
    public XRPLSaleWebhookInterceptor xrplSaleWebhookInterceptor(
            XRPLSaleClient client, 
            XRPLSaleProperties properties) {
        return new XRPLSaleWebhookInterceptor(client, properties.getWebhookPath());
    }
    
    /**
     * Creates the webhook controller for handling webhook events
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "xrplsale", name = "webhook-path")
    public XRPLSaleWebhookController xrplSaleWebhookController(XRPLSaleClient client) {
        return new XRPLSaleWebhookController(client);
    }
}