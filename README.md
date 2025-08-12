# XRPL.Sale Java SDK

Official Java SDK for integrating with the XRPL.Sale platform - the native XRPL launchpad for token sales and project funding.

[![Maven Central](https://img.shields.io/maven-central/v/com.xrplsale/xrplsale-java-sdk.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.xrplsale%22%20AND%20a:%22xrplsale-java-sdk%22)
[![Java](https://img.shields.io/badge/Java-11+-orange.svg)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Features

- ☕ **Modern Java 11+** - Built with modern Java features and best practices
- 🚀 **Spring Boot Integration** - Auto-configuration and starter support
- 🔄 **Reactive Support** - Optional Project Reactor integration
- 🔐 **XRPL Wallet Authentication** - Seamless wallet integration
- 📊 **Project Management** - Create, launch, and manage token sales
- 💰 **Investment Tracking** - Monitor investments and analytics
- 🔔 **Webhook Support** - Real-time event notifications with signature verification
- 📈 **Analytics & Reporting** - Comprehensive data insights
- 🛡️ **Error Handling** - Structured exception hierarchy
- 🔄 **Auto-retry Logic** - Resilient API calls with exponential backoff
- ⚡ **Thread Safe** - Safe for concurrent usage

## Installation

### Maven

```xml
<dependency>
    <groupId>com.xrplsale</groupId>
    <artifactId>xrplsale-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.xrplsale:xrplsale-java-sdk:1.0.0'
```

### Spring Boot Starter

For Spring Boot applications, use the starter:

```xml
<dependency>
    <groupId>com.xrplsale</groupId>
    <artifactId>xrplsale-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### Basic Usage

```java
import com.xrplsale.XRPLSaleClient;
import com.xrplsale.Environment;
import com.xrplsale.models.Project;
import com.xrplsale.requests.CreateProjectRequest;

public class XRPLSaleExample {
    public static void main(String[] args) {
        // Initialize the client
        XRPLSaleClient client = XRPLSaleClient.builder()
            .apiKey("your-api-key")
            .environment(Environment.PRODUCTION) // or Environment.TESTNET
            .debug(true)
            .build();
        
        try {
            // Create a new project
            CreateProjectRequest request = CreateProjectRequest.builder()
                .name("My DeFi Protocol")
                .description("Revolutionary DeFi protocol on XRPL")
                .tokenSymbol("MDP")
                .totalSupply("100000000")
                .tier(Tier.builder()
                    .tier(1)
                    .pricePerToken("0.001")
                    .totalTokens("20000000")
                    .build())
                .saleStartDate(Instant.parse("2025-02-01T00:00:00Z"))
                .saleEndDate(Instant.parse("2025-03-01T00:00:00Z"))
                .build();
            
            Project project = client.getProjects().create(request);
            System.out.println("Project created: " + project.getId());
            
        } catch (XRPLSaleException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```

### Spring Boot Integration

Add configuration to your `application.properties`:

```properties
# XRPL.Sale Configuration
xrplsale.api-key=your-api-key
xrplsale.environment=production
xrplsale.webhook-secret=your-webhook-secret
xrplsale.webhook-path=/webhooks/xrplsale
xrplsale.debug=false

# Optional timeout settings
xrplsale.connect-timeout=10000
xrplsale.read-timeout=30000
xrplsale.max-retries=3
```

Use dependency injection in your services:

```java
@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final XRPLSaleClient xrplSaleClient;
    
    public List<Project> getActiveProjects() throws XRPLSaleException {
        PaginatedResponse<Project> response = xrplSaleClient.getProjects()
            .getActive(1, 10);
        return response.getData();
    }
    
    public Project createProject(CreateProjectRequest request) throws XRPLSaleException {
        return xrplSaleClient.getProjects().create(request);
    }
}
```

## Authentication

### XRPL Wallet Authentication

```java
// Generate authentication challenge
AuthChallenge challenge = client.getAuth()
    .generateChallenge("rYourWalletAddress...");

// Sign the challenge with your wallet
// (implementation depends on your wallet library)
String signature = signMessage(challenge.getChallenge());

// Authenticate
AuthRequest authRequest = AuthRequest.builder()
    .walletAddress("rYourWalletAddress...")
    .signature(signature)
    .timestamp(challenge.getTimestamp())
    .build();

AuthResponse authResponse = client.getAuth().authenticate(authRequest);
System.out.println("Authentication successful: " + authResponse.getToken());

// The token is automatically set in the client for subsequent requests
```

## Core Services

### Projects Service

```java
// List active projects
PaginatedResponse<Project> projects = client.getProjects().getActive(1, 10);

// Get project details
Project project = client.getProjects().get("proj_abc123");

// Launch a project
Project launchedProject = client.getProjects().launch("proj_abc123");

// Get project statistics
ProjectStats stats = client.getProjects().getStats("proj_abc123");
System.out.println("Total raised: " + stats.getTotalRaisedXRP() + " XRP");

// Search projects
ProjectSearchOptions searchOptions = ProjectSearchOptions.builder()
    .status("active")
    .page(1)
    .limit(10)
    .build();
PaginatedResponse<Project> searchResults = client.getProjects()
    .search("DeFi", searchOptions);
```

### Investments Service

```java
// Create an investment
CreateInvestmentRequest investmentRequest = CreateInvestmentRequest.builder()
    .projectId("proj_abc123")
    .amountXRP("100")
    .investorAccount("rInvestorAddress...")
    .build();
Investment investment = client.getInvestments().create(investmentRequest);

// List investments for a project
PaginatedResponse<Investment> investments = client.getInvestments()
    .getByProject("proj_abc123", 1, 10);

// Get investor summary
InvestorSummary summary = client.getInvestments()
    .getInvestorSummary("rInvestorAddress...");

// Simulate an investment
SimulateInvestmentRequest simulation = SimulateInvestmentRequest.builder()
    .projectId("proj_abc123")
    .amountXRP("100")
    .build();
SimulationResult result = client.getInvestments().simulate(simulation);
System.out.println("Expected tokens: " + result.getTokenAmount());
```

### Analytics Service

```java
// Get platform analytics
PlatformAnalytics analytics = client.getAnalytics().getPlatformAnalytics();
System.out.println("Total raised: " + analytics.getTotalRaisedXRP() + " XRP");

// Get project-specific analytics
LocalDate startDate = LocalDate.of(2025, 1, 1);
LocalDate endDate = LocalDate.of(2025, 1, 31);
ProjectAnalytics projectAnalytics = client.getAnalytics()
    .getProjectAnalytics("proj_abc123", startDate, endDate);

// Get market trends
MarketTrends trends = client.getAnalytics().getMarketTrends("30d");

// Export data
ExportDataRequest exportRequest = ExportDataRequest.builder()
    .type("projects")
    .format("csv")
    .startDate("2025-01-01")
    .endDate("2025-01-31")
    .build();
ExportResult export = client.getAnalytics().exportData(exportRequest);
System.out.println("Download URL: " + export.getDownloadUrl());
```

## Webhook Integration

### Spring Boot Webhook Handler

The SDK provides automatic webhook handling for Spring Boot applications:

```java
@Component
@EventListener
public class WebhookEventHandler {
    
    @EventListener
    public void handleInvestmentCreated(InvestmentCreatedEvent event) {
        System.out.println("New investment: " + event.getAmountXRP() + " XRP");
        // Process the investment...
    }
    
    @EventListener
    public void handleProjectLaunched(ProjectLaunchedEvent event) {
        System.out.println("Project launched: " + event.getProjectId());
        // Process the project launch...
    }
    
    @EventListener
    public void handleTierCompleted(TierCompletedEvent event) {
        System.out.println("Tier completed: " + event.getTier());
        // Process the tier completion...
    }
}
```

### Manual Webhook Handling

```java
@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    
    private final XRPLSaleClient client;
    
    @PostMapping("/xrplsale")
    public ResponseEntity<String> handleWebhook(
            @RequestBody byte[] payload,
            @RequestHeader("X-XRPL-Sale-Signature") String signature) {
        
        // Verify signature
        if (!client.verifyWebhookSignature(payload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid signature");
        }
        
        try {
            // Parse webhook event
            WebhookEvent event = client.parseWebhookEvent(payload);
            
            // Handle different event types
            switch (event.getType()) {
                case "investment.created":
                    handleNewInvestment(event.getData());
                    break;
                case "project.launched":
                    handleProjectLaunched(event.getData());
                    break;
                case "tier.completed":
                    handleTierCompleted(event.getData());
                    break;
                default:
                    System.out.println("Unknown event type: " + event.getType());
            }
            
            return ResponseEntity.ok("OK");
        } catch (XRPLSaleException e) {
            return ResponseEntity.badRequest().body("Invalid payload");
        }
    }
    
    private void handleNewInvestment(Map<String, Object> data) {
        // Process new investment
        System.out.println("New investment: " + data.get("amount_xrp") + " XRP");
    }
}
```

## Error Handling

```java
try {
    Project project = client.getProjects().get("invalid-id");
} catch (NotFoundException e) {
    System.out.println("Project not found");
} catch (AuthenticationException e) {
    System.out.println("Authentication failed: " + e.getMessage());
} catch (ValidationException e) {
    System.out.println("Validation error: " + e.getMessage());
    System.out.println("Details: " + e.getDetails());
} catch (RateLimitException e) {
    System.out.println("Rate limit exceeded. Retry after: " + e.getRetryAfter());
} catch (XRPLSaleException e) {
    System.out.println("API error: " + e.getMessage());
    System.out.println("Code: " + e.getErrorCode());
}
```

## Configuration Options

```java
XRPLSaleClient client = XRPLSaleClient.builder()
    .apiKey("your-api-key")                          // Required
    .environment(Environment.PRODUCTION)             // or Environment.TESTNET
    .baseUrl("https://custom-api.example.com/v1")    // Custom API URL (optional)
    .connectTimeout(Duration.ofSeconds(10))          // Connection timeout
    .readTimeout(Duration.ofSeconds(30))             // Read timeout
    .writeTimeout(Duration.ofSeconds(30))            // Write timeout
    .maxRetries(3)                                   // Maximum retry attempts
    .retryDelay(Duration.ofSeconds(1))               // Base delay between retries
    .webhookSecret("your-webhook-secret")            // For webhook verification
    .debug(false)                                    // Enable debug logging
    .build();
```

## Reactive Support

The SDK provides optional reactive support using Project Reactor:

```java
// Add reactor-core dependency for reactive support
dependencies {
    implementation 'io.projectreactor:reactor-core:3.6.0'
}
```

```java
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

// Reactive projects service
Mono<PaginatedResponse<Project>> projectsMono = client.getProjects()
    .getActiveReactive(1, 10);

projectsMono.subscribe(
    projects -> System.out.println("Received " + projects.getData().size() + " projects"),
    error -> System.err.println("Error: " + error.getMessage())
);

// Reactive chaining
client.getProjects().getActiveReactive(1, 10)
    .flatMapMany(response -> Flux.fromIterable(response.getData()))
    .filter(project -> project.getStatus().equals("active"))
    .take(5)
    .doOnNext(project -> System.out.println("Project: " + project.getName()))
    .subscribe();
```

## Pagination

```java
// Manual pagination
ProjectListOptions options = ProjectListOptions.builder()
    .status("active")
    .page(1)
    .limit(50)
    .sortBy("created_at")
    .sortOrder("desc")
    .build();

PaginatedResponse<Project> response = client.getProjects().list(options);

for (Project project : response.getData()) {
    System.out.println("Project: " + project.getName());
}

System.out.printf("Page %d of %d%n", 
    response.getPagination().getPage(), 
    response.getPagination().getTotalPages());
System.out.println("Total projects: " + response.getPagination().getTotal());

// Automatic pagination helper
List<Project> allProjects = new ArrayList<>();
int page = 1;
PaginatedResponse<Project> currentPage;

do {
    currentPage = client.getProjects().getActive(page++, 100);
    allProjects.addAll(currentPage.getData());
} while (currentPage.getPagination().getHasNext());
```

## Testing

```bash
# Run tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run integration tests
mvn verify -P integration-tests

# Check code style
mvn checkstyle:check

# Run static analysis
mvn spotbugs:check
```

## Development

```bash
# Clone the repository
git clone https://github.com/xrplsale/java-sdk.git
cd java-sdk

# Install dependencies
mvn install

# Run tests
mvn test

# Build JAR
mvn package

# Install to local repository
mvn install
```

## Support

- 📖 [Documentation](https://xrpl.sale/docs)
- 💬 [Discord Community](https://discord.gg/xrpl-sale)
- 🐛 [Issue Tracker](https://github.com/xrplsale/java-sdk/issues)
- 📧 [Email Support](mailto:developers@xrpl.sale)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Links

- [XRPL.Sale Platform](https://xrpl.sale)
- [API Documentation](https://xrpl.sale/docs/api)
- [Other SDKs](https://xrpl.sale/docs/developers/sdk-downloads)
- [GitHub Organization](https://github.com/xrplsale)

---

Made with ❤️ by the XRPL.Sale team