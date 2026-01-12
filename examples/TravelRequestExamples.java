package examples;

/**
 * Examples of natural language travel requests for testing the Ollama integration
 * These examples demonstrate various ways users can formulate their travel needs
 * 
 * @author Assistant
 */
public class TravelRequestExamples {
    
    public static final String[] EXAMPLE_REQUESTS = {
        // Basic requests
        "Je veux aller de a vers c à 9h du matin",
        "Besoin d'un trajet de b à f vers 14h30",
        "Transport de d vers e demain matin à 8h",
        
        // With transport type
        "Je veux aller de a vers c à 9h en bus",
        "Trajet de b à f en vélo vers 15h",
        "Transport de d à e en tram à 18h30",
        "Aller de a vers f en voiture vers midi",
        
        // With criteria
        "Je veux aller de a vers c à 9h, option la moins chère",
        "Trajet de b à f en bus, le plus rapide possible",
        "Transport de d à e en vélo, maximum de confort",
        "Aller de a vers f vers 14h, meilleur rapport durée-prix",
        "Voyage de c vers e en tram, impact CO2 minimal",
        
        // Complete requests
        "Je veux aller de a vers c à 9h du matin en bus, option économique",
        "Besoin d'un trajet de b vers f vers 14h30 en vélo, le plus rapide",
        "Transport de d à e demain matin à 8h en tram, maximum confort",
        "Aller de a vers f vers midi en voiture, meilleur prix",
        
        // Varied formulations
        "Comment aller de la station a à la station c vers 9h en bus pas cher ?",
        "Cherche trajet rapide de b vers f en vélo après 14h",
        "Moyen de transport écologique de d à e vers 8h du matin",
        "Voyage confortable en tram de c vers f vers 16h",
        
        // Complex requests
        "Je dois être à la station c à partir de la station a pour 9h30, en bus si possible, pas trop cher",
        "Urgent: trajet de b vers f le plus vite possible, vélo de préférence",
        "Rendez-vous à e depuis d vers 8h15, tram ou bus, confort important",
        
        // Edge cases
        "a vers c maintenant",
        "b f velo 14h",
        "transport d e",
        "Station a station c bus 9h économique"
    };
    
    public static void printExamples() {
        System.out.println("=== Exemples de Demandes de Transport ===");
        System.out.println();
        
        for (int i = 0; i < EXAMPLE_REQUESTS.length; i++) {
            System.out.printf("%2d. %s%n", i + 1, EXAMPLE_REQUESTS[i]);
        }
        
        System.out.println();
        System.out.println("Instructions:");
        System.out.println("- Copiez et collez ces exemples dans le champ de demande");
        System.out.println("- Modifiez-les selon vos besoins");
        System.out.println("- Testez différentes formulations");
        System.out.println("- Observez comment l'IA extrait les paramètres");
    }
    
    public static String getRandomExample() {
        int index = (int) (Math.random() * EXAMPLE_REQUESTS.length);
        return EXAMPLE_REQUESTS[index];
    }
    
    public static void main(String[] args) {
        printExamples();
        
        System.out.println();
        System.out.println("Exemple aléatoire:");
        System.out.println("\"" + getRandomExample() + "\"");
    }
}