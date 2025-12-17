package data;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service for enhancing text messages using Ollama LLM
 * Improves communication between traveller agent and human
 */
public class TextEnhancementService {
    private static TextEnhancementService instance;
    private HttpClient httpClient;
    private String baseUrl = "http://localhost:11434";
    private String modelName = "llama2"; // Default model
    private boolean ollamaAvailable = false;
    
    /**
     * Message types for different enhancement styles
     */
    public enum MessageType {
        JOURNEY_PROPOSAL,     // Travel suggestions and proposals
        BOOKING_CONFIRMATION, // Booking confirmations and receipts  
        ERROR_MESSAGE,        // Error messages and problems
        WEATHER_IMPACT,       // Weather-related information
        GENERAL              // General agent communication
    }
    
    private TextEnhancementService() {
        initializeOllama();
    }
    
    public static synchronized TextEnhancementService getInstance() {
        if (instance == null) {
            instance = new TextEnhancementService();
        }
        return instance;
    }
    
    /**
     * Initialize connection to Ollama service
     */
    private void initializeOllama() {
        try {
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            // Test connection and get available models
            String[] models = listModels();
            if (models.length > 0) {
                ollamaAvailable = true;
                // Use first available model or prefer specific ones
                for (String model : models) {
                    if (model.contains("llama2") || model.contains("llama3")) {
                        modelName = model;
                        break;
                    }
                }
                if (modelName.equals("llama2") && models.length > 0) {
                    modelName = models[0]; // Use first available if no llama2/3
                }
                System.out.println("TextEnhancementService: Connected to Ollama, using model: " + modelName);
            }
        } catch (Exception e) {
            ollamaAvailable = false;
            System.err.println("TextEnhancementService: Ollama not available - " + e.getMessage());
        }
    }
    
    /**
     * Get list of available models from Ollama
     */
    private String[] listModels() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tags"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray modelsArray = jsonResponse.getJSONArray("models");

            String[] modelNames = new String[modelsArray.length()];
            for (int i = 0; i < modelsArray.length(); i++) {
                JSONObject model = modelsArray.getJSONObject(i);
                modelNames[i] = model.getString("name");
            }
            return modelNames;
        }
        return new String[0];
    }
    
    /**
     * Enhance a message based on its type
     */
    public String enhanceMessage(String originalMessage, MessageType messageType) {
        if (!ollamaAvailable || originalMessage == null || originalMessage.trim().isEmpty()) {
            return originalMessage;
        }
        
        try {
            String systemPrompt = getSystemPrompt(messageType);
            String enhancedMessage = simpleChat(systemPrompt, originalMessage);
            
            // Return enhanced message or original if enhancement failed
            return (enhancedMessage != null && !enhancedMessage.trim().isEmpty()) 
                ? enhancedMessage.trim() 
                : originalMessage;
                
        } catch (Exception e) {
            System.err.println("Text enhancement failed: " + e.getMessage());
            return originalMessage;
        }
    }
    
    /**
     * Enhanced travel proposal with weather context
     */
    public String enhanceTravelProposal(String proposal, WeatherManager.WeatherCondition weatherCondition) {
        if (!ollamaAvailable) return proposal;
        
        String weatherContext = "";
        if (weatherCondition != null) {
            weatherContext = " Current weather condition: " + weatherCondition.toString();
        }
        
        String systemPrompt = "You are a helpful travel assistant. Make travel proposals more engaging and informative. " +
                            "Consider weather conditions in your response. Keep it concise but friendly." + weatherContext;
        
        try {
            return simpleChat(systemPrompt, proposal);
        } catch (Exception e) {
            return proposal;
        }
    }
    
    /**
     * Get system prompt based on message type
     */
    private String getSystemPrompt(MessageType messageType) {
        switch (messageType) {
            case JOURNEY_PROPOSAL:
                return "You are a helpful travel assistant. Rewrite travel proposals to be more engaging, " +
                       "informative and user-friendly. Keep the essential information but make it sound more natural and appealing. " +
                       "Be concise but warm.";
                       
            case BOOKING_CONFIRMATION:
                return "You are a professional booking assistant. Rewrite booking confirmations to be clear, " +
                       "reassuring and well-organized. Include all important details but present them in a friendly, " +
                       "professional manner. Add a touch of enthusiasm.";
                       
            case ERROR_MESSAGE:
                return "You are a helpful customer service assistant. Rewrite error messages to be more " +
                       "understanding, helpful and solution-oriented. Maintain the important information but " +
                       "make the tone more supportive and less technical.";
                       
            case WEATHER_IMPACT:
                return "You are a weather-aware travel advisor. Rewrite weather-related travel information " +
                       "to be informative and helpful. Explain weather impacts clearly and suggest alternatives " +
                       "when appropriate. Be encouraging but realistic.";
                       
            case GENERAL:
            default:
                return "You are a friendly and helpful travel agent assistant. Rewrite messages to be more " +
                       "natural, engaging and user-friendly while keeping all important information. " +
                       "Use a warm, professional tone.";
        }
    }
    
    /**
     * Simple chat interaction with Ollama
     */
    private String simpleChat(String systemPrompt, String userMessage) throws Exception {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", modelName);
        jsonRequest.put("stream", false);

        JSONArray messages = new JSONArray();
        
        // System message
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.put(systemMessage);
        
        // User message
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.put(userMsg);
        
        jsonRequest.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONObject message = jsonResponse.getJSONObject("message");
            return message.getString("content");
        } else {
            throw new RuntimeException("HTTP Error: " + response.statusCode());
        }
    }
    
    /**
     * Check if Ollama service is available
     */
    public boolean isOllamaAvailable() {
        return ollamaAvailable;
    }
    
    /**
     * Check if the service is available
     */
    public boolean isAvailable() {
        return ollamaAvailable;
    }
    
    /**
     * Refresh connection to Ollama (useful if service was restarted)
     */
    public void refresh() {
        initializeOllama();
    }
    
    /**
     * Get current model name
     */
    public String getCurrentModel() {
        return modelName;
    }
    
    /**
     * Get service status information
     */
    public String getServiceStatus() {
        if (ollamaAvailable) {
            return "✅ Text Enhancement: Active (Model: " + modelName + ")";
        } else {
            return "❌ Text Enhancement: Offline - Ollama not available";
        }
    }
    
    /**
     * Set custom model (if available)
     */
    public boolean setModel(String newModel) {
        try {
            String[] models = listModels();
            for (String model : models) {
                if (model.equals(newModel)) {
                    this.modelName = newModel;
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}