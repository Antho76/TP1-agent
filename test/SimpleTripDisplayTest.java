package test;

/**
 * Test simple pour démontrer le nouveau format d'affichage des trajets
 */
public class SimpleTripDisplayTest {
    
    private static String formatTime(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%dh%02d", hours, minutes);
    }
    
    public static void main(String[] args) {
        System.out.println("=== NOUVEAU FORMAT D'AFFICHAGE DES TRAJETS ===\n");
        
        System.out.println("Comparaison ancien vs nouveau format :\n");
        
        // Exemples de trajets
        System.out.println("📍 Trajet simple :");
        System.out.println("   Ancien: A → B | Bus | 15 min | 3,00€");
        System.out.println("   Nouveau: " + formatTime(900) + " - A → B | Bus | 15 min | 3,00€");
        System.out.println();
        
        System.out.println("📍 Trajet avec correspondance :");
        System.out.println("   Ancien: A → C → B | Tram+Bus | 40 min | 7,00€");
        System.out.println("   Nouveau: " + formatTime(1200) + " - A → C → B | Tram+Bus | 40 min | 7,00€");
        System.out.println();
        
        System.out.println("📍 Trajet tôt le matin :");
        System.out.println("   Ancien: A → B | Vélo | 25 min | 1,00€");
        System.out.println("   Nouveau: " + formatTime(730) + " - A → B | Vélo | 25 min | 1,00€");
        System.out.println();
        
        System.out.println("📍 Trajet en soirée :");
        System.out.println("   Ancien: A → B | Voiture | 20 min | 8,00€");
        System.out.println("   Nouveau: " + formatTime(1945) + " - A → B | Voiture | 20 min | 8,00€");
        System.out.println();
        
        System.out.println("✅ Amélioration : L'heure de départ est maintenant visible en début de ligne !");
        System.out.println("🕐 Format d'heure : HHhMM (ex: 9h30, 12h00, 19h45)");
    }
}