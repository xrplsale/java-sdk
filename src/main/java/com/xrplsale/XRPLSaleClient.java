package com.xrplsale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.xrplsale.exceptions.XRPLSaleException;
import com.xrplsale.interceptors.AuthInterceptor;
import com.xrplsale.interceptors.RetryInterceptor;
import com.xrplsale.services.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Main XRPL.Sale SDK client for Java applications
 * 
 * <p>This client provides access to all XRPL.Sale platform services including
 * project management, investment tracking, analytics, webhooks, and authentication.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * XRPLSaleClient client = XRPLSaleClient.builder()
 *     .apiKey("your-api-key")
 *     .environment(Environment.PRODUCTION)
 *     .build();
 * 
 * List<Project> projects = client.getProjects().getActive(1, 10);
 * }</pre>
 */
@Slf4j
@Getter
public class XRPLSaleClient {
    
    private static final String VERSION = "1.0.0";
    private static final String DEFAULT_PRODUCTION_URL = "https://api.xrpl.sale/v1";
    private static final String DEFAULT_TESTNET_URL = "https://api-testnet.xrpl.sale/v1";
    
    private final XRPLSaleConfig config;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Services
    private final ProjectsService projects;
    private final InvestmentsService investments;
    private final AnalyticsService analytics;
    private final WebhooksService webhooks;
    private final AuthService auth;
    
    private String authToken;
    
    /**
     * Creates a new XRPL.Sale client with the specified configuration
     */
    public XRPLSaleClient(XRPLSaleConfig config) {
        this.config = config;
        this.objectMapper = createObjectMapper();
        this.httpClient = createHttpClient(config);
        
        // Initialize services
        this.projects = new ProjectsService(this);
        this.investments = new InvestmentsService(this);
        this.analytics = new AnalyticsService(this);
        this.webhooks = new WebhooksService(this);
        this.auth = new AuthService(this);
    }
    
    /**
     * Creates a builder for configuring the client
     */
    public static XRPLSaleClientBuilder builder() {
        return new XRPLSaleClientBuilder();
    }
    
    /**
     * Creates a client with just an API key (uses production environment)
     */
    public static XRPLSaleClient create(String apiKey) {
        return builder()
            .apiKey(apiKey)
            .environment(Environment.PRODUCTION)
            .build();
    }
    
    /**
     * Sets the authentication token for subsequent requests
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }
    
    /**
     * Verifies a webhook signature using HMAC-SHA256
     */
    public boolean verifyWebhookSignature(byte[] payload, String signature) {
        if (config.getWebhookSecret() == null || config.getWebhookSecret().isEmpty()) {
            return false;
        }
        
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                config.getWebhookSecret().getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(payload);
            String expectedSignature = "sha256=" + bytesToHex(hash);
            
            return MessageDigest.isEqual(
                expectedSignature.getBytes(),
                signature.getBytes()
            );
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    /**
     * Parses a webhook event from JSON bytes
     */
    public WebhookEvent parseWebhookEvent(byte[] payload) throws XRPLSaleException {
        try {
            return objectMapper.readValue(payload, WebhookEvent.class);
        } catch (IOException e) {
            throw new XRPLSaleException("Failed to parse webhook event", e);
        }
    }
    
    /**
     * Makes an HTTP request and returns the response body as a string
     */
    public String makeRequest(Request request) throws XRPLSaleException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw XRPLSaleException.fromResponse(response);
            }
            
            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        } catch (IOException e) {
            throw new XRPLSaleException("HTTP request failed", e);
        }
    }
    
    /**
     * Makes an HTTP request and parses the response as the specified type
     */
    public <T> T makeRequest(Request request, Class<T> responseType) throws XRPLSaleException {
        String responseBody = makeRequest(request);
        
        try {
            return objectMapper.readValue(responseBody, responseType);
        } catch (IOException e) {
            throw new XRPLSaleException("Failed to parse response", e);
        }
    }
    
    /**
     * Creates a request builder with common headers
     */
    public Request.Builder createRequestBuilder(String endpoint) {
        String url = config.getBaseUrl() + endpoint;
        
        Request.Builder builder = new Request.Builder()
            .url(url)
            .header("User-Agent", "XRPL.Sale-Java-SDK/" + VERSION)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json");
        
        // Add API key if available and no auth token is set
        if (authToken == null && config.getApiKey() != null) {
            builder.header("X-API-Key", config.getApiKey());
        }
        
        // Add auth token if available
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        
        return builder;
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    private OkHttpClient createHttpClient(XRPLSaleConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeout())
            .readTimeout(config.getReadTimeout())
            .writeTimeout(config.getWriteTimeout());
        
        // Add retry interceptor
        if (config.getMaxRetries() > 0) {
            builder.addInterceptor(new RetryInterceptor(config.getMaxRetries(), config.getRetryDelay()));
        }
        
        // Add auth interceptor
        builder.addInterceptor(new AuthInterceptor(this));
        
        // Add logging if debug is enabled
        if (config.isDebug()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
        
        return builder.build();
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Builder class for creating XRPLSaleClient instances
     */
    public static class XRPLSaleClientBuilder {
        private String apiKey;
        private Environment environment = Environment.PRODUCTION;
        private String baseUrl;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private Duration retryDelay = Duration.ofSeconds(1);
        private String webhookSecret;
        private boolean debug = false;
        
        public XRPLSaleClientBuilder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
        
        public XRPLSaleClientBuilder environment(Environment environment) {
            this.environment = environment;
            return this;
        }
        
        public XRPLSaleClientBuilder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        public XRPLSaleClientBuilder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }
        
        public XRPLSaleClientBuilder readTimeout(Duration timeout) {
            this.readTimeout = timeout;
            return this;
        }
        
        public XRPLSaleClientBuilder writeTimeout(Duration timeout) {
            this.writeTimeout = timeout;
            return this;
        }
        
        public XRPLSaleClientBuilder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }
        
        public XRPLSaleClientBuilder retryDelay(Duration delay) {
            this.retryDelay = delay;
            return this;
        }
        
        public XRPLSaleClientBuilder webhookSecret(String secret) {
            this.webhookSecret = secret;
            return this;
        }
        
        public XRPLSaleClientBuilder debug(boolean debug) {
            this.debug = debug;
            return this;
        }
        
        public XRPLSaleClient build() {
            if (baseUrl == null) {
                baseUrl = environment == Environment.TESTNET ? 
                    DEFAULT_TESTNET_URL : DEFAULT_PRODUCTION_URL;
            }
            
            XRPLSaleConfig config = XRPLSaleConfig.builder()
                .apiKey(apiKey)
                .environment(environment)
                .baseUrl(baseUrl)
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .writeTimeout(writeTimeout)
                .maxRetries(maxRetries)
                .retryDelay(retryDelay)
                .webhookSecret(webhookSecret)
                .debug(debug)
                .build();
            
            return new XRPLSaleClient(config);
        }
    }
}