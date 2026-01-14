package test;

/**
 * Test class for the enhanced TravellerGui with Ollama integration
 * This test can be run independently to verify the GUI functionality
 */
public class TestTravellerGuiWithOllama {
    
    public static void main(String[] args) {
        System.out.println("Starting TravellerGui test with Ollama integration...");
        
        // Create a GUI instance without an agent (for testing UI only)
        TravellerGui gui = new TravellerGui(null);
        gui.setVisible(true);
        
        gui.println("=== TravellerGui Test Mode ===");
        gui.println("This is a test of the enhanced interface with Ollama integration.");
        gui.println("");
        gui.println("Features available:");
        gui.println("✓ Natural language travel request input");
        gui.println("✓ AI-powered request analysis with Ollama");
        gui.println("✓ Transport type selection");
        gui.println("✓ Weather information integration");
        gui.println("✓ Manual controls as fallback");
        gui.println("");
        gui.println("To test the AI integration:");
        gui.println("1. Make sure Ollama is running locally (http://localhost:11434)");
        gui.println("2. Ensure you have a model like 'llama3.2:latest' available");
        gui.println("3. Try requests like: 'I need to go from station a to station c at 9am by bus, cheapest option'");
        gui.println("");
        gui.println("Note: Without a real agent, the 'Buy Travel' function won't work, but you can test the AI parsing.");
    }
}