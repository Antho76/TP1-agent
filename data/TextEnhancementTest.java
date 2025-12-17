package data;

/**
 * Simple test for TextEnhancementService
 * This test checks if Ollama is available and tests text enhancement
 */
public class TextEnhancementTest {
    public static void main(String[] args) {
        System.out.println("=== Test du Service d'Amélioration de Texte ===");
        
        TextEnhancementService service = TextEnhancementService.getInstance();
        
        System.out.println("Status: " + service.getServiceStatus());
        System.out.println("Ollama disponible: " + service.isOllamaAvailable());
        System.out.println("Modèle utilisé: " + service.getCurrentModel());
        System.out.println();
        
        if (service.isOllamaAvailable()) {
            System.out.println("=== Tests d'amélioration ===");
            
            // Test message d'erreur
            String errorMsg = "no journey found !!!";
            String enhancedError = service.enhanceMessage(errorMsg, TextEnhancementService.MessageType.ERROR_MESSAGE);
            System.out.println("Erreur originale: " + errorMsg);
            System.out.println("Erreur améliorée: " + enhancedError);
            System.out.println();
            
            // Test proposition de voyage
            String journeyMsg = "I choose this journey : Bus from Lille to Paris, departure 14:30, duration 3h45, cost 25€";
            String enhancedJourney = service.enhanceMessage(journeyMsg, TextEnhancementService.MessageType.JOURNEY_PROPOSAL);
            System.out.println("Proposition originale: " + journeyMsg);
            System.out.println("Proposition améliorée: " + enhancedJourney);
            System.out.println();
            
            // Test avec contexte météo
            WeatherManager.WeatherCondition weatherCondition = WeatherManager.WeatherCondition.RAIN;
            String weatherProposal = service.enhanceTravelProposal(journeyMsg, weatherCondition);
            System.out.println("Avec contexte météo (RAIN): " + weatherProposal);
            System.out.println();
            
            // Test confirmation de réservation
            String confirmMsg = "Agent Agency1 : Booking confirmed for journey Bus Lille-Paris 14:30";
            String enhancedConfirm = service.enhanceMessage(confirmMsg, TextEnhancementService.MessageType.BOOKING_CONFIRMATION);
            System.out.println("Confirmation originale: " + confirmMsg);
            System.out.println("Confirmation améliorée: " + enhancedConfirm);
            
        } else {
            System.out.println("⚠️ Ollama n'est pas disponible. Assurez-vous qu'Ollama est installé et en cours d'exécution.");
            System.out.println("Pour installer Ollama: https://ollama.com/");
            System.out.println("Puis lancez: ollama run llama2");
        }
        
        System.out.println("\n=== Test terminé ===");
    }
}