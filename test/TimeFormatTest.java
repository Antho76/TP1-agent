package test;

import javax.swing.*;

/**
 * Test pour vérifier le formatage de l'heure
 */
public class TimeFormatTest {
    
    private static String formatTime(int time) {
        int hours = time / 100;
        int minutes = time % 100;
        return String.format("%dh%02d", hours, minutes);
    }
    
    public static void main(String[] args) {
        // Test de différents formats d'heure
        System.out.println("Tests de formatage d'heure :");
        System.out.println("1200 -> " + formatTime(1200));
        System.out.println("930 -> " + formatTime(930));
        System.out.println("1515 -> " + formatTime(1515));
        System.out.println("800 -> " + formatTime(800));
        
        // Test dans un dialog
        SwingUtilities.invokeLater(() -> {
            String message = String.format(
                "🚨 TRAJET ANNULÉ\n\n" +
                "Trajet annulé: A → B (à %s)\n" +
                "Motif: Test\n\n" +
                "🔄 Préférences utilisées précédemment:\n" +
                "• Critère: Plus rapide\n" +
                "• Transport: Tous transports\n" +
                "• Heure de départ: %s\n\n" +
                "Souhaitez-vous rechercher un trajet de remplacement\n" +
                "avec les mêmes préférences et horaire ?",
                formatTime(1200), formatTime(1200));
                
            JOptionPane.showMessageDialog(null,
                message,
                "Test Formatage Heure",
                JOptionPane.INFORMATION_MESSAGE);
                
            System.exit(0);
        });
    }
}