package com.xrplsale.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xrplsale.XRPLSaleClient;
import com.xrplsale.exceptions.XRPLSaleException;
import com.xrplsale.models.*;
import com.xrplsale.requests.CreateProjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * Service for managing token sale projects on the XRPL.Sale platform
 * 
 * <p>This service provides methods for creating, updating, launching, and managing
 * token sale projects. It also includes functionality for retrieving project
 * statistics, investors, and tiers.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class ProjectsService {
    
    private final XRPLSaleClient client;
    
    /**
     * Lists all projects with optional filtering and pagination
     * 
     * @param options Query parameters for filtering and pagination
     * @return Paginated response containing projects
     * @throws XRPLSaleException if the API request fails
     */
    public PaginatedResponse<Project> list(ProjectListOptions options) throws XRPLSaleException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(client.getConfig().getBaseUrl() + "/projects").newBuilder();
        
        if (options != null) {
            if (options.getStatus() != null) {
                urlBuilder.addQueryParameter("status", options.getStatus());
            }
            if (options.getPage() != null) {
                urlBuilder.addQueryParameter("page", options.getPage().toString());
            }
            if (options.getLimit() != null) {
                urlBuilder.addQueryParameter("limit", options.getLimit().toString());
            }
            if (options.getSortBy() != null) {
                urlBuilder.addQueryParameter("sort_by", options.getSortBy());
            }
            if (options.getSortOrder() != null) {
                urlBuilder.addQueryParameter("sort_order", options.getSortOrder());
            }
        }
        
        Request request = client.createRequestBuilder("")
            .url(urlBuilder.build())
            .get()
            .build();
        
        return client.makeRequest(request, new TypeReference<PaginatedResponse<Project>>() {});
    }
    
    /**
     * Retrieves active projects
     * 
     * @param page Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated response containing active projects
     * @throws XRPLSaleException if the API request fails
     */
    public PaginatedResponse<Project> getActive(int page, int limit) throws XRPLSaleException {
        ProjectListOptions options = ProjectListOptions.builder()
            .status("active")
            .page(page)
            .limit(limit)
            .build();
        
        return list(options);
    }
    
    /**
     * Retrieves upcoming projects
     * 
     * @param page Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated response containing upcoming projects
     * @throws XRPLSaleException if the API request fails
     */
    public PaginatedResponse<Project> getUpcoming(int page, int limit) throws XRPLSaleException {
        ProjectListOptions options = ProjectListOptions.builder()
            .status("upcoming")
            .page(page)
            .limit(limit)
            .build();
        
        return list(options);
    }
    
    /**
     * Retrieves completed projects
     * 
     * @param page Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated response containing completed projects
     * @throws XRPLSaleException if the API request fails
     */
    public PaginatedResponse<Project> getCompleted(int page, int limit) throws XRPLSaleException {
        ProjectListOptions options = ProjectListOptions.builder()
            .status("completed")
            .page(page)
            .limit(limit)
            .build();
        
        return list(options);
    }
    
    /**
     * Retrieves a specific project by ID
     * 
     * @param projectId The project ID
     * @return The project details
     * @throws XRPLSaleException if the API request fails or project is not found
     */
    public Project get(String projectId) throws XRPLSaleException {
        Request request = client.createRequestBuilder("/projects/" + projectId)
            .get()
            .build();
        
        return client.makeRequest(request, Project.class);
    }
    
    /**
     * Creates a new project
     * 
     * @param createRequest The project creation request
     * @return The created project
     * @throws XRPLSaleException if the API request fails or validation errors occur
     */
    public Project create(CreateProjectRequest createRequest) throws XRPLSaleException {
        try {
            String jsonBody = client.getObjectMapper().writeValueAsString(createRequest);
            RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.get("application/json"));
            
            Request request = client.createRequestBuilder("/projects")
                .post(body)
                .build();
            
            return client.makeRequest(request, Project.class);
        } catch (Exception e) {
            throw new XRPLSaleException("Failed to create project", e);
        }
    }
    
    /**
     * Updates an existing project
     * 
     * @param projectId The project ID
     * @param updates Map of fields to update
     * @return The updated project
     * @throws XRPLSaleException if the API request fails
     */
    public Project update(String projectId, Map<String, Object> updates) throws XRPLSaleException {
        try {
            String jsonBody = client.getObjectMapper().writeValueAsString(updates);
            RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.get("application/json"));
            
            Request request = client.createRequestBuilder("/projects/" + projectId)
                .patch(body)
                .build();
            
            return client.makeRequest(request, Project.class);
        } catch (Exception e) {
            throw new XRPLSaleException("Failed to update project", e);
        }
    }
    
    /**
     * Launches a project (makes it active)
     * 
     * @param projectId The project ID
     * @return The updated project
     * @throws XRPLSaleException if the API request fails
     */
    public Project launch(String projectId) throws XRPLSaleException {
        RequestBody body = RequestBody.create("", okhttp3.MediaType.get("application/json"));
        
        Request request = client.createRequestBuilder("/projects/" + projectId + "/launch")
            .post(body)
            .build();
        
        return client.makeRequest(request, Project.class);
    }
    
    /**
     * Pauses a project
     * 
     * @param projectId The project ID
     * @return The updated project
     * @throws XRPLSaleException if the API request fails
     */
    public Project pause(String projectId) throws XRPLSaleException {
        RequestBody body = RequestBody.create("", okhttp3.MediaType.get("application/json"));
        
        Request request = client.createRequestBuilder("/projects/" + projectId + "/pause")
            .post(body)
            .build();
        
        return client.makeRequest(request, Project.class);
    }
    
    /**
     * Resumes a paused project
     * 
     * @param projectId The project ID
     * @return The updated project
     * @throws XRPLSaleException if the API request fails
     */
    public Project resume(String projectId) throws XRPLSaleException {
        RequestBody body = RequestBody.create("", okhttp3.MediaType.get("application/json"));
        
        Request request = client.createRequestBuilder("/projects/" + projectId + "/resume")
            .post(body)
            .build();
        
        return client.makeRequest(request, Project.class);
    }
    
    /**
     * Cancels a project
     * 
     * @param projectId The project ID
     * @return The updated project
     * @throws XRPLSaleException if the API request fails
     */
    public Project cancel(String projectId) throws XRPLSaleException {
        RequestBody body = RequestBody.create("", okhttp3.MediaType.get("application/json"));
        
        Request request = client.createRequestBuilder("/projects/" + projectId + "/cancel")
            .post(body)
            .build();
        
        return client.makeRequest(request, Project.class);
    }
    
    /**
     * Retrieves project statistics
     * 
     * @param projectId The project ID
     * @return Project statistics
     * @throws XRPLSaleException if the API request fails
     */
    public ProjectStats getStats(String projectId) throws XRPLSaleException {
        Request request = client.createRequestBuilder("/projects/" + projectId + "/stats")
            .get()
            .build();
        
        return client.makeRequest(request, ProjectStats.class);
    }
    
    /**
     * Retrieves project investors with pagination
     * 
     * @param projectId The project ID
     * @param page Page number (1-based)
     * @param limit Number of items per page
     * @return Paginated response containing investors
     * @throws XRPLSaleException if the API request fails
     */
    public PaginatedResponse<Investor> getInvestors(String projectId, int page, int limit) throws XRPLSaleException {
        HttpUrl url = HttpUrl.parse(client.getConfig().getBaseUrl() + "/projects/" + projectId + "/investors")
            .newBuilder()
            .addQueryParameter("page", String.valueOf(page))
            .addQueryParameter("limit", String.valueOf(limit))
            .build();
        
        Request request = client.createRequestBuilder("")
            .url(url)
            .get()
            .build();
        
        return client.makeRequest(request, new TypeReference<PaginatedResponse<Investor>>() {});
    }
    
    /**
     * Retrieves project tiers
     * 
     * @param projectId The project ID
     * @return List of project tiers
     * @throws XRPLSaleException if the API request fails
     */
    public List<Tier> getTiers(String projectId) throws XRPLSaleException {
        Request request = client.createRequestBuilder("/projects/" + projectId + "/tiers")
            .get()
            .build();
        
        return client.makeRequest(request, new TypeReference<List<Tier>>() {});
    }
    
    /**
     * Updates project tiers
     * 
     * @param projectId The project ID
     * @param tiers List of tiers to update
     * @return Updated list of tiers
     * @throws XRPLSaleException if the API request fails
     */
    public List<Tier> updateTiers(String projectId, List<Tier> tiers) throws XRPLSaleException {
        try {
            Map<String, Object> requestBody = Map.of("tiers", tiers);
            String jsonBody = client.getObjectMapper().writeValueAsString(requestBody);
            RequestBody body = RequestBody.create(jsonBody, okhttp3.MediaType.get("application/json"));
            
            Request request = client.createRequestBuilder("/projects/" + projectId + "/tiers")
                .put(body)
                .build();
            
            return client.makeRequest(request, new TypeReference<List<Tier>>() {});
        } catch (Exception e) {
            throw new XRPLSaleException("Failed to update project tiers", e);
        }
    }
    
    /**
     * Searches projects based on query string
     * 
     * @param query Search query
     * @param options Additional search options
     * @return Paginated response containing matching projects
     * @throws XRPLSaleException if the API request fails
     */
    public PaginatedResponse<Project> search(String query, ProjectSearchOptions options) throws XRPLSaleException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(client.getConfig().getBaseUrl() + "/projects/search")
            .newBuilder()
            .addQueryParameter("q", query);
        
        if (options != null) {
            if (options.getPage() != null) {
                urlBuilder.addQueryParameter("page", options.getPage().toString());
            }
            if (options.getLimit() != null) {
                urlBuilder.addQueryParameter("limit", options.getLimit().toString());
            }
            if (options.getStatus() != null) {
                urlBuilder.addQueryParameter("status", options.getStatus());
            }
        }
        
        Request request = client.createRequestBuilder("")
            .url(urlBuilder.build())
            .get()
            .build();
        
        return client.makeRequest(request, new TypeReference<PaginatedResponse<Project>>() {});
    }
    
    /**
     * Retrieves featured projects
     * 
     * @param limit Maximum number of projects to return
     * @return List of featured projects
     * @throws XRPLSaleException if the API request fails
     */
    public List<Project> getFeatured(int limit) throws XRPLSaleException {
        HttpUrl url = HttpUrl.parse(client.getConfig().getBaseUrl() + "/projects/featured")
            .newBuilder()
            .addQueryParameter("limit", String.valueOf(limit))
            .build();
        
        Request request = client.createRequestBuilder("")
            .url(url)
            .get()
            .build();
        
        return client.makeRequest(request, new TypeReference<List<Project>>() {});
    }
    
    /**
     * Retrieves trending projects
     * 
     * @param period Time period (24h, 7d, 30d)
     * @param limit Maximum number of projects to return
     * @return List of trending projects
     * @throws XRPLSaleException if the API request fails
     */
    public List<Project> getTrending(String period, int limit) throws XRPLSaleException {
        HttpUrl url = HttpUrl.parse(client.getConfig().getBaseUrl() + "/projects/trending")
            .newBuilder()
            .addQueryParameter("period", period)
            .addQueryParameter("limit", String.valueOf(limit))
            .build();
        
        Request request = client.createRequestBuilder("")
            .url(url)
            .get()
            .build();
        
        return client.makeRequest(request, new TypeReference<List<Project>>() {});
    }
}