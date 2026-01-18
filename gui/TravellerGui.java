package gui;

import jade.gui.GuiEvent;
import agents.TravellerAgent;
import data.WeatherManager;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Journey resarch Gui, communication with TravellerAgent throw GuiEvent
 *
 * @author modif. Emmanuel Adam - LAMIH
 */
@SuppressWarnings("serial")
public class TravellerGui extends JFrame {

    /**
     * Onglets principaux
     */
    private JTabbedPane tabbedPane;
    
    /**
     * Panel de recherche
     */
    private JPanel searchPanel;
    
    /**
     * Panel mes trajets
     */
    private JPanel tripsPanel;

    /**
     * Zone d'affichage pour les résultats de recherche
     */
    private final JTextArea searchResultsArea;
    
    /**
     * Zone d'affichage pour les logs (cachée)
     */
    private final JTextArea debugLogArea;

    /**
     * Liste des trajets réservés (affichage)
     */
    private JList<String> tripsList;
    private DefaultListModel<String> tripsListModel;
    private List<String> bookedTrips;
    
    /**
     * Liste des billets individuels - NOUVEAU: stockage séparé de chaque segment
     */
    private List<data.Journey> individualTickets;
    
    /**
     * Compteur de trajets pour nommage
     */
    private int tripCounter = 1;
    
    /**
     * Liste des trajets réels avec leurs objets ComposedJourney
     */
    private List<data.ComposedJourney> bookedJourneys;

    /**
     * Text field for natural language request
     */
    private JTextField requestField;

    /**
     * Ollama integration
     */
    private HttpClient httpClient;
    private String baseUrl = "http://localhost:11434";
    private String modelName = "granite3.3:latest";

    private final TravellerAgent myAgent;
    private JLabel lblPrice;
    private JLabel lblWeather;
    private JComboBox<String> jListFrom;
    private JComboBox<String> jListTo;
    private JComboBox<String> jListCriteria;
    private JComboBox<String> jListCity;
    private JComboBox<String> jListTransportType;
    private JSlider sliderTimeDeparture;

    private String departure;
    private String arrival;
    private int time;
    private String transportType;

    public TravellerGui(TravellerAgent a) {
        this.setBounds(10, 10, 900, 600);

        myAgent = a;
        if (a != null)
            setTitle("Client 1 - " + myAgent.getLocalName());

        // Initialisation des listes
        bookedTrips = new ArrayList<>();
        individualTickets = new ArrayList<>(); // NOUVEAU: liste des billets individuels
        bookedJourneys = new ArrayList<>();
        tripsListModel = new DefaultListModel<>();

        // Zone de texte pour les résultats de recherche
        searchResultsArea = new JTextArea();
        searchResultsArea.setBackground(new Color(255, 255, 240));
        searchResultsArea.setEditable(false);
        searchResultsArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Zone de texte cachée pour les logs de debug
        debugLogArea = new JTextArea();
        debugLogArea.setBackground(new Color(240, 240, 240));
        debugLogArea.setEditable(false);
        debugLogArea.setFont(new Font("Monospaced", Font.PLAIN, 10));

        // Créer l'interface avec onglets
        createTabbedInterface();

        // Initialize Ollama HTTP client
        initializeOllama();

        // Make the agent terminate when the user closes the GUI
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GuiEvent guiEv = new GuiEvent(this, TravellerAgent.EXIT);
                myAgent.postGuiEvent(guiEv);
            }
        });
        setResizable(true);
    }

    /**
     * Crée l'interface principale avec des onglets
     */
    private void createTabbedInterface() {
        setLayout(new BorderLayout());
        
        // Création des onglets
        tabbedPane = new JTabbedPane();
        
        // Onglet Recherche
        searchPanel = createSearchPanel();
        tabbedPane.addTab("🔍 Rechercher un trajet", searchPanel);
        
        // Onglet Mes billets - MODIFIÉ: stockage individuel
        tripsPanel = createTripsPanel();
        tabbedPane.addTab("🎫 Mes billets", tripsPanel);
        
        // Ajouter les onglets à la fenêtre
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Crée le panel de recherche de trajets
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panel de demande en langage naturel (en haut)
        JPanel aiRequestPanel = createAIRequestPanel();
        panel.add(aiRequestPanel, BorderLayout.NORTH);
        
        // Panel de formulaire de recherche manuelle
        JPanel formPanel = createSearchForm();
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Zone de résultats de recherche
        JScrollPane resultScrollPane = new JScrollPane(searchResultsArea);
        resultScrollPane.setBorder(BorderFactory.createTitledBorder("📊 Résultats de recherche"));
        resultScrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(resultScrollPane, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Crée le panel de demande IA en langage naturel
     */
    private JPanel createAIRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("🤖 Demande en langage naturel avec IA"));
        panel.setBackground(new Color(240, 248, 255));
        
        // Instructions
        JLabel instructionLabel = new JLabel("<html><b>Décrivez votre voyage en français :</b><br>" +
                "Exemples: \"Je veux aller de a vers c à 14h en bus, le moins cher\" ou " +
                "\"Trajet de b à f à 9h du matin, le plus rapide\"</html>");
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        panel.add(instructionLabel, BorderLayout.NORTH);
        
        // Zone de saisie et boutons
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        
        // Champ de texte pour la demande
        requestField = new JTextField();
        requestField.setPreferredSize(new Dimension(400, 30));
        requestField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        requestField.addActionListener(e -> processNaturalLanguageRequest());
        inputPanel.add(requestField, BorderLayout.CENTER);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        JButton exampleButton = new JButton("💡");
        exampleButton.setToolTipText("Voir des exemples");
        exampleButton.addActionListener(e -> showExampleRequests());
        buttonPanel.add(exampleButton);
        
        JButton processButton = new JButton("🤖 Réserver avec IA");
        processButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        processButton.setBackground(new Color(70, 130, 180));
        processButton.setForeground(Color.WHITE);
        processButton.addActionListener(e -> processNaturalLanguageRequest());
        buttonPanel.add(processButton);
        
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Crée le formulaire de recherche manuelle
     */
    private JPanel createSearchForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("📝 Recherche manuelle (alternative)"));
        panel.setBackground(new Color(248, 248, 248));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        
        // Ligne 1: Origine, Destination, Transport
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("📍"), gbc);
        gbc.gridx = 1;
        jListFrom = new JComboBox<>(new String[]{"a", "b", "c", "d", "e", "f"});
        jListFrom.setPreferredSize(new Dimension(120, 35));
        jListFrom.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(jListFrom, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("→"), gbc);
        gbc.gridx = 3;
        jListTo = new JComboBox<>(new String[]{"a", "b", "c", "d", "e", "f"});
        jListTo.setPreferredSize(new Dimension(120, 35));
        jListTo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(jListTo, gbc);
        
        gbc.gridx = 4;
        panel.add(new JLabel("🚌"), gbc);
        gbc.gridx = 5;
        jListTransportType = new JComboBox<>(new String[]{"any", "bus", "car", "bike", "tram"});
        jListTransportType.setPreferredSize(new Dimension(140, 35));
        jListTransportType.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(jListTransportType, gbc);
        
        // Ligne 2: Heure, Critère, Bouton
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("⏰"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        sliderTimeDeparture = new JSlider(6, 22, 9);
        sliderTimeDeparture.setPreferredSize(new Dimension(180, 35));
        sliderTimeDeparture.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPrice = new JLabel("09:00");
        sliderTimeDeparture.addChangeListener(e -> {
            int value = sliderTimeDeparture.getValue();
            lblPrice.setText(String.format("%02d:00", value));
        });
        panel.add(sliderTimeDeparture, gbc);
        
        gbc.gridx = 3; gbc.gridwidth = 1;
        panel.add(lblPrice, gbc);
        
        gbc.gridx = 4;
        panel.add(new JLabel("⚖️"), gbc);
        gbc.gridx = 5;
        jListCriteria = new JComboBox<>(new String[]{"cost", "duration", "confort", "duration-cost"});
        jListCriteria.setPreferredSize(new Dimension(160, 35));
        jListCriteria.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(jListCriteria, gbc);
        
        // Ligne 3: Météo et bouton recherche
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.WEST;
        lblWeather = new JLabel("🌤️ Météo: Chargement...");
        lblWeather.setFont(new Font("SansSerif", Font.PLAIN, 10));
        panel.add(lblWeather, gbc);
        
        gbc.gridx = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        JButton searchButton = new JButton("🔍 Rechercher");
        searchButton.setPreferredSize(new Dimension(140, 40));
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        searchButton.addActionListener(e -> performSearch());
        panel.add(searchButton, gbc);
        
        // Initialiser les informations météo après la création du composant
        SwingUtilities.invokeLater(this::updateWeatherInfo);
        
        return panel;
    }

    /**
     * Crée le panel des trajets réservés
     */
    private JPanel createTripsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Liste des trajets
        tripsList = new JList<>(tripsListModel);
        tripsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tripsList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(tripsList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("🎫 Mes billets (stockage individuel)"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        // Bouton Détails
        JButton detailsButton = new JButton("📄 Détails");
        detailsButton.setPreferredSize(new Dimension(120, 40));
        detailsButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        detailsButton.addActionListener(e -> showTicketDetails());
        buttonPanel.add(detailsButton);
        
        // Bouton Revendre (enchères)
        JButton resellButton = new JButton("🔔 Revendre");
        resellButton.setPreferredSize(new Dimension(120, 40));
        resellButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        resellButton.setBackground(new Color(255, 193, 7)); // Jaune/orange
        resellButton.setToolTipText("Mettre ce billet aux enchères");
        resellButton.addActionListener(e -> resellSelectedTicket());
        buttonPanel.add(resellButton);
        
        // Bouton Annuler
        JButton cancelButton = new JButton("❌ Annuler");
        cancelButton.setPreferredSize(new Dimension(120, 40));
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        cancelButton.setBackground(new Color(220, 53, 69)); // Rouge
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setToolTipText("Annuler et rembourser ce billet");
        cancelButton.addActionListener(e -> cancelSelectedTicket());
        buttonPanel.add(cancelButton);
        
        // Message informatif
        JLabel infoLabel = new JLabel("<html><div style='text-align: center; color: #666;'>" +
            "🎫 Sélectionnez un billet pour voir ses détails, le revendre aux enchères ou l'annuler.<br>" +
            "🔔 La revente permet aux autres voyageurs de racheter votre billet." +
            "</div></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(infoLabel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * Effectue une recherche de trajet
     */
    private void performSearch() {
        String origin = (String) jListFrom.getSelectedItem();
        String dest = (String) jListTo.getSelectedItem();
        int hour = sliderTimeDeparture.getValue() * 100; // Convertir en format HHMM
        String criteria = (String) jListCriteria.getSelectedItem();
        String transport = (String) jListTransportType.getSelectedItem();
        
        // Validation
        if (origin.equals(dest)) {
            JOptionPane.showMessageDialog(this, 
                "⚠️ L'origine et la destination doivent être différentes!", 
                "Erreur de saisie", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Afficher un message de recherche en cours
        searchResultsArea.setText("🔍 Recherche en cours...\n\n");
        searchResultsArea.append(String.format("📍 Trajet: %s → %s\n", origin, dest));
        searchResultsArea.append(String.format("⏰ Heure: %02d:00\n", hour/100));
        searchResultsArea.append(String.format("🚌 Transport: %s\n", transport));
        searchResultsArea.append(String.format("⚖️ Critère: %s\n\n", criteria));
        
        // Envoyer l'événement à l'agent
        GuiEvent guiEvent = new GuiEvent(this, TravellerAgent.BUY_TRAVEL);
        guiEvent.addParameter(origin);
        guiEvent.addParameter(dest);
        guiEvent.addParameter(hour);
        guiEvent.addParameter(criteria);
        guiEvent.addParameter(transport);
        myAgent.postGuiEvent(guiEvent);
    }

    /**
     * NOUVEAU: Affiche les détails du billet individuel sélectionné
     */
    private void showTicketDetails() {
        int selectedIndex = tripsList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un billet pour voir ses détails.", 
                "Aucune sélection", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String selectedItem = tripsListModel.getElementAt(selectedIndex);
        
        // Vérifier si c'est un en-tête de trajet (commence par 📍)
        if (selectedItem.startsWith("📍")) {
            JOptionPane.showMessageDialog(this, 
                "Ceci est un en-tête de trajet.\nVeuillez sélectionner un billet individuel pour voir ses détails.", 
                "Sélection d'en-tête", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Trouver l'index réel du billet dans la liste des tickets individuels
        int ticketIndex = findTicketIndex(selectedIndex);
        if (ticketIndex == -1 || ticketIndex >= individualTickets.size()) {
            JOptionPane.showMessageDialog(this, 
                "Erreur: Billet non trouvé.", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        data.Journey selectedTicket = individualTickets.get(ticketIndex);
        
        // Créer un message détaillé pour le billet
        StringBuilder detailsMessage = new StringBuilder();
        detailsMessage.append("🎫 DÉTAILS DE VOTRE BILLET\n");
        detailsMessage.append("═".repeat(35)).append("\n\n");
        
        // Informations du billet
        detailsMessage.append("� Trajet: ").append(selectedTicket.getStart()).append(" → ").append(selectedTicket.getStop()).append("\n");
        detailsMessage.append("🚌 Transport: ").append(selectedTicket.getMeans()).append("\n");
        
        // Horaires formatés
        int depHours = selectedTicket.getDepartureDate() / 100;
        int depMinutes = selectedTicket.getDepartureDate() % 100;
        int arrHours = selectedTicket.getArrivalDate() / 100;
        int arrMinutes = selectedTicket.getArrivalDate() % 100;
        
        detailsMessage.append("🕒 Départ: ").append(String.format("%02d:%02d", depHours, depMinutes)).append("\n");
        detailsMessage.append("🏁 Arrivée: ").append(String.format("%02d:%02d", arrHours, arrMinutes)).append("\n");
        detailsMessage.append("⏱️ Durée: ").append(selectedTicket.getDuration()).append(" minutes\n");
        detailsMessage.append("💰 Prix: ").append(String.format("%.2f€", selectedTicket.getCost())).append("\n");
        detailsMessage.append("🌿 CO2: ").append(selectedTicket.getCo2()).append(" g\n");
        detailsMessage.append("⭐ Confort: ").append(selectedTicket.getConfort()).append("/10\n\n");
        detailsMessage.append("✅ Status: CONFIRMÉ - Billet individuel\n");
        
        JOptionPane.showMessageDialog(this, 
            detailsMessage.toString(), 
            "Détails du billet réservé", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * NOUVEAU: Trouve l'index réel d'un billet dans la liste des tickets individuels
     * en tenant compte des en-têtes de trajets
     */
    private int findTicketIndex(int displayIndex) {
        int ticketCount = 0;
        
        for (int i = 0; i <= displayIndex && i < tripsListModel.getSize(); i++) {
            String item = tripsListModel.getElementAt(i);
            
            // Si ce n'est pas un en-tête de trajet, c'est un billet
            if (!item.startsWith("📍")) {
                if (i == displayIndex) {
                    return ticketCount; // Index trouvé
                }
                ticketCount++;
            }
        }
        
        return -1; // Pas trouvé
    }

    /**
     * Revend le billet sélectionné aux enchères
     */
    private void resellSelectedTicket() {
        int selectedIndex = tripsList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un billet à revendre.", 
                "Aucune sélection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedItem = tripsListModel.getElementAt(selectedIndex);
        
        // Vérifier si c'est un en-tête de trajet
        if (selectedItem.startsWith("📍")) {
            JOptionPane.showMessageDialog(this, 
                "Sélectionnez un billet individuel, pas un en-tête de trajet.", 
                "Sélection invalide", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Trouver le billet
        int ticketIndex = findTicketIndex(selectedIndex);
        if (ticketIndex == -1 || ticketIndex >= individualTickets.size()) {
            JOptionPane.showMessageDialog(this, 
                "Erreur: Billet non trouvé.", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        data.Journey ticket = individualTickets.get(ticketIndex);
        
        // Proposer la revente à l'utilisateur via l'agent
        boolean auctionStarted = myAgent.proposeAuction(ticket, "Revente volontaire");
        
        if (auctionStarted) {
            // Retirer le billet de la liste locale (il sera transféré si vendu)
            individualTickets.remove(ticketIndex);
            tripsListModel.remove(selectedIndex);
            
            // Mettre à jour l'en-tête du trajet si nécessaire
            updateTripHeaders();
            
            println("🔔 Billet mis en enchère: " + ticket.getStart() + " → " + ticket.getStop());
        }
    }

    /**
     * Annule le billet sélectionné (remboursement)
     */
    private void cancelSelectedTicket() {
        int selectedIndex = tripsList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un billet à annuler.", 
                "Aucune sélection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedItem = tripsListModel.getElementAt(selectedIndex);
        
        // Vérifier si c'est un en-tête de trajet
        if (selectedItem.startsWith("📍")) {
            JOptionPane.showMessageDialog(this, 
                "Sélectionnez un billet individuel, pas un en-tête de trajet.", 
                "Sélection invalide", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Trouver le billet
        int ticketIndex = findTicketIndex(selectedIndex);
        if (ticketIndex == -1 || ticketIndex >= individualTickets.size()) {
            JOptionPane.showMessageDialog(this, 
                "Erreur: Billet non trouvé.", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        data.Journey ticket = individualTickets.get(ticketIndex);
        
        // Demander confirmation avec option de revente
        Object[] options = {"Annuler simplement", "Mettre en enchère", "Ne rien faire"};
        int choice = JOptionPane.showOptionDialog(this,
            "Que souhaitez-vous faire avec ce billet?\n\n" +
            "🎫 " + ticket.getStart() + " → " + ticket.getStop() + "\n" +
            "💰 Valeur: " + String.format("%.2f€", ticket.getCost()) + "\n\n" +
            "• Annuler: remboursement standard\n" +
            "• Enchère: revendre à d'autres voyageurs",
            "❌ Annulation de billet",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 0) {
            // Annulation simple
            int confirm = JOptionPane.showConfirmDialog(this,
                "Confirmer l'annulation du billet?\n\n" +
                "🎫 " + ticket.getStart() + " → " + ticket.getStop() + "\n" +
                "💰 Remboursement estimé: " + String.format("%.2f€", ticket.getCost() * 0.8) + " (80%)",
                "Confirmation d'annulation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // Annuler le billet et restaurer le stock
                ticket.cancelBooking();
                
                // Retirer de la liste
                individualTickets.remove(ticketIndex);
                tripsListModel.remove(selectedIndex);
                
                // Mettre à jour l'en-tête du trajet
                updateTripHeaders();
                
                println("❌ Billet annulé: " + ticket.getStart() + " → " + ticket.getStop());
                println("💰 Remboursement: " + String.format("%.2f€", ticket.getCost() * 0.8));
                
                JOptionPane.showMessageDialog(this,
                    "✅ Billet annulé avec succès!\n\n" +
                    "💰 Remboursement: " + String.format("%.2f€", ticket.getCost() * 0.8),
                    "Annulation confirmée",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (choice == 1) {
            // Mettre en enchère
            resellSelectedTicket();
        }
        // choice == 2 : ne rien faire
    }

    /**
     * Met à jour les en-têtes de trajets après suppression d'un billet
     */
    private void updateTripHeaders() {
        // Parcourir la liste et supprimer les en-têtes sans billets
        for (int i = tripsListModel.getSize() - 1; i >= 0; i--) {
            String item = tripsListModel.getElementAt(i);
            if (item.startsWith("📍")) {
                // Vérifier s'il y a des billets après cet en-tête
                boolean hasTickets = false;
                for (int j = i + 1; j < tripsListModel.getSize(); j++) {
                    String nextItem = tripsListModel.getElementAt(j);
                    if (nextItem.startsWith("📍")) {
                        break; // Prochain en-tête trouvé
                    }
                    if (!nextItem.startsWith("📍")) {
                        hasTickets = true;
                        break;
                    }
                }
                
                if (!hasTickets) {
                    tripsListModel.remove(i);
                    if (i < bookedTrips.size()) {
                        bookedTrips.remove(i);
                    }
                }
            }
        }
        
        // Forcer le rafraîchissement
        tripsList.revalidate();
        tripsList.repaint();
    }

    /**
     * Ajoute un trajet à la liste des trajets réservés avec confirmation
     */
    public void addBookedTripWithConfirmation(String tripDetails, String fullDetails) {
        SwingUtilities.invokeLater(() -> {
            int confirmation = JOptionPane.showConfirmDialog(this,
                "Confirmer la réservation de ce trajet?\n\n" + fullDetails,
                "Confirmation de réservation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                // Ajouter à la liste des trajets
                bookedTrips.add(tripDetails);
                tripsListModel.addElement(tripDetails);
                
                // Debug: vérifier que l'ajout a bien eu lieu
                System.out.println("DEBUG: Trajet ajouté - " + tripDetails);
                System.out.println("DEBUG: Nombre de trajets dans la liste: " + tripsListModel.getSize());
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Trajet réservé avec succès!\n\n" +
                    "Votre réservation est confirmée et enregistrée.\n" +
                    "Consultez l'onglet 'Mes trajets' pour voir tous vos trajets réservés.", 
                    "Réservation confirmée", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Basculer vers l'onglet "Mes trajets"
                tabbedPane.setSelectedIndex(1);
                
                // Nettoyer la zone de recherche
                searchResultsArea.setText("✅ Recherche terminée avec succès!\n\nVotre trajet a été ajouté à vos réservations.");
                
                // Forcer le rafraîchissement de la liste
                tripsList.revalidate();
                tripsList.repaint();
                
            } else {
                searchResultsArea.append("\n❌ Réservation annulée par l'utilisateur.");
            }
        });
    }

    /**
     * Ajoute un trajet avec gestion complète des stocks
     */
    public void addBookedTripWithConfirmation(data.ComposedJourney journey, String tripDetails, String fullDetails) {
        SwingUtilities.invokeLater(() -> {
            int confirmation = JOptionPane.showConfirmDialog(this,
                "Confirmer la réservation de ce trajet?\n\n" + fullDetails,
                "Confirmation de réservation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirmation == JOptionPane.YES_OPTION) {
                // Réserver les places dans les agences
                myAgent.bookJourneyPlaces(journey);
                
                // Ajouter à la liste de l'agent (source de vérité)
                myAgent.addBookedJourney(journey);
                
                // NOUVEAU: Décomposer le trajet en billets individuels avec organisation
                String tripName = "Trajet " + tripCounter;
                data.Journey firstSegment = journey.getJourneys().get(0);
                data.Journey lastSegment = journey.getJourneys().get(journey.getJourneys().size() - 1);
                String tripSummary = tripName + " : " + firstSegment.getStart() + " → " + lastSegment.getStop();
                
                // Ajouter l'en-tête du trajet
                bookedTrips.add("📍 " + tripSummary + " (" + journey.getJourneys().size() + " billets)");
                tripsListModel.addElement("📍 " + tripSummary + " (" + journey.getJourneys().size() + " billets)");
                
                // Ajouter chaque billet avec indentation
                for (int i = 0; i < journey.getJourneys().size(); i++) {
                    data.Journey individualTicket = journey.getJourneys().get(i);
                    individualTickets.add(individualTicket);
                    String ticketDisplay = createGroupedTicketDisplay(individualTicket, i + 1);
                    bookedTrips.add(ticketDisplay);
                    tripsListModel.addElement(ticketDisplay);
                }
                
                tripCounter++;
                
                // Maintenir la compatibilité
                bookedJourneys.add(journey);
                
                System.out.println("DEBUG: Trajet décomposé en " + journey.getJourneys().size() + " billets individuels");
                for (int i = 0; i < journey.getJourneys().size(); i++) {
                    data.Journey ticket = journey.getJourneys().get(i);
                    System.out.println("  Billet " + (i+1) + ": " + ticket.getStart() + " → " + ticket.getStop());
                }
                
                // Generate personalized message with Ollama
                String confirmationMessage = generateConfirmationMessageWithOllama(journey);
                
                JOptionPane.showMessageDialog(this, 
                    confirmationMessage, 
                    "Réservation confirmée", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Basculer vers l'onglet "Mes billets"
                tabbedPane.setSelectedIndex(1);
                
                // Nettoyer la zone de recherche
                searchResultsArea.setText("✅ Recherche terminée avec succès!\n\nVotre trajet a été décomposé en billets individuels.");
                
                // Forcer le rafraîchissement de la liste
                tripsList.revalidate();
                tripsList.repaint();
                
            } else {
                searchResultsArea.append("\n❌ Réservation annulée par l'utilisateur.");
            }
        });
    }

    /**
     * Rafraîchit la liste des trajets affichée en se basant sur les données de l'agent
     */
    public void refreshTripsList() {
        SwingUtilities.invokeLater(() -> {
            // Effacer la liste actuelle
            tripsListModel.clear();
            bookedTrips.clear();
            
            // Récupérer les trajets actuels de l'agent
            List<data.ComposedJourney> currentJourneys = myAgent.getBookedJourneys();
            
            // Reconstruire la liste affichée
            for (data.ComposedJourney journey : currentJourneys) {
                List<data.Journey> segments = journey.getJourneys();
                
                // Vérifier s'il y a une rupture de continuité
                boolean hasGap = false;
                for (int i = 0; i < segments.size() - 1; i++) {
                    if (!segments.get(i).getStop().equals(segments.get(i + 1).getStart())) {
                        hasGap = true;
                        break;
                    }
                }
                
                // Si rupture de continuité, afficher chaque segment comme un billet séparé
                if (hasGap && segments.size() > 1) {
                    for (int i = 0; i < segments.size(); i++) {
                        String ticketDisplay = createIndividualTicketDisplay(segments.get(i));
                        bookedTrips.add(ticketDisplay);
                        tripsListModel.addElement(ticketDisplay);
                    }
                } else {
                    // Trajet normal, affichage classique
                    String tripSummary = createTripSummaryFromJourney(journey);
                    bookedTrips.add(tripSummary);
                    tripsListModel.addElement(tripSummary);
                }
            }
            
            // Mettre à jour bookedJourneys pour rester synchronisé
            bookedJourneys.clear();
            bookedJourneys.addAll(currentJourneys);
            
            // Forcer le rafraîchissement visuel
            tripsList.revalidate();
            tripsList.repaint();
            
            System.out.println("🔄 Liste des trajets rafraîchie - " + currentJourneys.size() + " trajets affichés");
        });
    }

    /**
     * Force la synchronisation complète entre les données de l'agent et l'interface
     */
    public void forceSynchronization() {
        SwingUtilities.invokeLater(() -> {
            // Effacer complètement toutes les listes
            tripsListModel.clear();
            bookedTrips.clear();
            bookedJourneys.clear();
            
            // Récupérer les données fraîches de l'agent
            List<data.ComposedJourney> agentJourneys = myAgent.getBookedJourneys();
            
            // Reconstruire complètement les listes
            for (data.ComposedJourney journey : agentJourneys) {
                List<data.Journey> segments = journey.getJourneys();
                
                // Vérifier s'il y a une rupture de continuité
                boolean hasGap = false;
                for (int i = 0; i < segments.size() - 1; i++) {
                    if (!segments.get(i).getStop().equals(segments.get(i + 1).getStart())) {
                        hasGap = true;
                        break;
                    }
                }
                
                // Si rupture de continuité, afficher chaque segment comme un billet séparé
                if (hasGap && segments.size() > 1) {
                    for (int i = 0; i < segments.size(); i++) {
                        String ticketDisplay = createIndividualTicketDisplay(segments.get(i));
                        bookedTrips.add(ticketDisplay);
                        tripsListModel.addElement(ticketDisplay);
                    }
                    bookedJourneys.add(journey);
                } else {
                    // Trajet normal, affichage classique
                    String tripSummary = createTripSummaryFromJourney(journey);
                    bookedTrips.add(tripSummary);
                    tripsListModel.addElement(tripSummary);
                    bookedJourneys.add(journey);
                }
            }
            
            // Forcer la mise à jour visuelle
            tripsList.clearSelection();
            tripsList.revalidate();
            tripsList.repaint();
            
            // Mettre à jour le conteneur parent
            if (tripsPanel != null) {
                tripsPanel.revalidate();
                tripsPanel.repaint();
            }
            
            System.out.println("🔄 Synchronisation forcée terminée - " + agentJourneys.size() + " trajets synchronisés");
        });
    }

    /**
     * Ajoute un billet acheté aux enchères à la liste des trajets du client
     * Cette méthode est appelée après une enchère réussie
     * @param ticket Le billet acheté
     * @param pricePaid Le prix payé pour le billet
     */
    public void addAuctionPurchasedTicket(data.Journey ticket, double pricePaid) {
        SwingUtilities.invokeLater(() -> {
            // Créer un ComposedJourney à partir du billet unique
            data.ComposedJourney composedJourney = new data.ComposedJourney();
            composedJourney.add(ticket);
            
            // Ajouter à la liste de l'agent (source de vérité)
            myAgent.addBookedJourney(composedJourney);
            
            // Créer l'affichage du billet acheté aux enchères avec marquage spécial
            String tripSummary = createAuctionTicketDisplay(ticket, pricePaid);
            
            // Ajouter à l'affichage
            bookedTrips.add(tripSummary);
            tripsListModel.addElement(tripSummary);
            
            // Maintenir la compatibilité avec bookedJourneys local
            bookedJourneys.add(composedJourney);
            individualTickets.add(ticket);
            
            // Notification dans la console
            println("🎉 Nouveau billet ajouté à vos réservations (acheté aux enchères):");
            println("   📍 " + ticket.getStart() + " → " + ticket.getStop());
            println("   💰 Prix payé: " + String.format("%.2f€", pricePaid));
            
            // Forcer le rafraîchissement visuel
            tripsList.revalidate();
            tripsList.repaint();
            
            // Basculer vers l'onglet "Mes billets" pour montrer le nouvel achat
            tabbedPane.setSelectedIndex(1);
            
            System.out.println("✅ Billet aux enchères ajouté: " + ticket.getStart() + " → " + ticket.getStop() + 
                             " pour " + String.format("%.2f€", pricePaid));
        });
    }

    /**
     * Supprime un billet vendu aux enchères de la liste des trajets du vendeur
     * Utilise l'ID unique du billet pour garantir la suppression du bon billet
     * @param ticket Le billet vendu
     */
    public void removeSoldAuctionTicket(data.Journey ticket) {
        SwingUtilities.invokeLater(() -> {
            if (ticket == null) {
                return;
            }
            
            long ticketId = ticket.getTicketId();
            String ticketStart = ticket.getStart();
            String ticketStop = ticket.getStop();
            String ticketMeans = ticket.getMeans();
            int ticketDeparture = ticket.getDepartureDate();
            
            System.out.println("🔍 Recherche du billet vendu à supprimer (ID: " + ticketId + "):");
            System.out.println("   Trajet: " + ticketStart + " → " + ticketStop);
            
            // 1. D'abord, trouver le billet dans individualTickets par son ID unique
            int indexToRemove = -1;
            for (int i = 0; i < individualTickets.size(); i++) {
                data.Journey indTicket = individualTickets.get(i);
                if (indTicket.getTicketId() == ticketId) {
                    indexToRemove = i;
                    System.out.println("✅ Billet trouvé dans individualTickets à l'index " + i + " (ID: " + ticketId + ")");
                    break;
                }
            }
            
            // 2. Trouver l'index correspondant dans l'affichage
            // On compte les billets individuels (pas les en-têtes de trajet)
            if (indexToRemove >= 0) {
                individualTickets.remove(indexToRemove);
                
                // Parcourir la liste d'affichage pour trouver le bon billet
                // en comptant les billets individuels rencontrés
                int ticketCount = 0;
                for (int i = 0; i < tripsListModel.getSize(); i++) {
                    String displayText = tripsListModel.getElementAt(i);
                    
                    // Vérifier si c'est un billet (pas un en-tête de trajet)
                    if (displayText.contains("🎫") || displayText.contains("├─") || displayText.contains("└─") ||
                        (displayText.contains("→") && !displayText.startsWith("📍") && !displayText.startsWith("⚠️"))) {
                        
                        // Vérifier si ce billet correspond au billet vendu
                        String formattedDepTime = String.format("%02d:%02d", ticketDeparture / 100, ticketDeparture % 100);
                        boolean matchesRoute = displayText.contains(ticketStart + "→" + ticketStop) || 
                                               displayText.contains(ticketStart + " → " + ticketStop);
                        boolean matchesTime = displayText.contains(formattedDepTime);
                        boolean matchesMeans = displayText.toLowerCase().contains(ticketMeans.toLowerCase());
                        
                        if (matchesRoute && matchesTime && matchesMeans) {
                            // Vérifier si c'est le bon index dans la liste des billets
                            if (ticketCount == indexToRemove) {
                                tripsListModel.remove(i);
                                if (i < bookedTrips.size()) {
                                    bookedTrips.remove(i);
                                }
                                System.out.println("✅ Billet supprimé de l'affichage à l'index " + i);
                                break;
                            }
                        }
                        ticketCount++;
                    }
                }
            } else {
                // Fallback: chercher par correspondance exacte des attributs
                System.out.println("⚠️ ID non trouvé, recherche par attributs...");
                for (int i = individualTickets.size() - 1; i >= 0; i--) {
                    data.Journey indTicket = individualTickets.get(i);
                    if (indTicket.getStart().equals(ticketStart) && 
                        indTicket.getStop().equals(ticketStop) &&
                        indTicket.getMeans().equals(ticketMeans) &&
                        indTicket.getDepartureDate() == ticketDeparture) {
                        
                        individualTickets.remove(i);
                        System.out.println("✅ Billet supprimé de individualTickets par attributs");
                        break;
                    }
                }
                
                // Supprimer de l'affichage
                String formattedDepTime = String.format("%02d:%02d", ticketDeparture / 100, ticketDeparture % 100);
                for (int i = tripsListModel.getSize() - 1; i >= 0; i--) {
                    String displayText = tripsListModel.getElementAt(i);
                    boolean matchesRoute = displayText.contains(ticketStart + "→" + ticketStop) || 
                                           displayText.contains(ticketStart + " → " + ticketStop);
                    boolean matchesTime = displayText.contains(formattedDepTime);
                    boolean matchesMeans = displayText.toLowerCase().contains(ticketMeans.toLowerCase());
                    
                    if (matchesRoute && matchesTime && matchesMeans) {
                        tripsListModel.remove(i);
                        if (i < bookedTrips.size()) {
                            bookedTrips.remove(i);
                        }
                        System.out.println("✅ Billet supprimé de l'affichage par attributs");
                        break;
                    }
                }
            }
            
            // 3. Supprimer des bookedJourneys de l'agent (source de vérité)
            List<data.ComposedJourney> agentJourneys = myAgent.getBookedJourneys();
            for (int i = agentJourneys.size() - 1; i >= 0; i--) {
                data.ComposedJourney journey = agentJourneys.get(i);
                for (data.Journey segment : journey.getJourneys()) {
                    if (segment.getTicketId() == ticketId) {
                        if (journey.getJourneys().size() == 1) {
                            myAgent.removeBookedJourney(journey);
                            System.out.println("✅ Trajet complet supprimé des réservations de l'agent");
                        }
                        break;
                    }
                }
            }
            
            // Mettre à jour les en-têtes de trajets (supprimer ceux qui sont vides)
            updateTripHeaders();
            
            // Forcer le rafraîchissement visuel
            tripsList.revalidate();
            tripsList.repaint();
            
            // Message dans la console
            String formattedTime = String.format("%02d:%02d", ticketDeparture / 100, ticketDeparture % 100);
            println("🏷️ Billet vendu aux enchères retiré de vos réservations:");
            println("   📍 " + ticketStart + " → " + ticketStop + " (" + formattedTime + ") [ID:" + ticketId + "]");
        });
    }
    
    /**
     * Crée l'affichage spécial pour un billet acheté aux enchères
     */
    private String createAuctionTicketDisplay(data.Journey ticket, double pricePaid) {
        if (ticket == null) {
            return "🏷️ Billet aux enchères invalide";
        }
        
        // Formatage de l'heure de départ
        int depHours = ticket.getDepartureDate() / 100;
        int depMinutes = ticket.getDepartureDate() % 100;
        String formattedDepTime = String.format("%02d:%02d", depHours, depMinutes);
        
        // Formatage de l'heure d'arrivée
        int arrHours = ticket.getArrivalDate() / 100;
        int arrMinutes = ticket.getArrivalDate() % 100;
        String formattedArrTime = String.format("%02d:%02d", arrHours, arrMinutes);
        
        // Format spécial pour les billets achetés aux enchères: "🏷️ [ENCHÈRE] 09:00-09:30 A→B Bus 30min 3,50€"
        return String.format("🏷️ [ENCHÈRE] %s-%s %s→%s %s %dmin %.2f€",
            formattedDepTime, formattedArrTime,
            ticket.getStart(), ticket.getStop(), 
            ticket.getMeans(), ticket.getDuration(), pricePaid);
    }

    /**
     * Crée un résumé d'un trajet composé
     */
    private String createTripSummaryFromJourney(data.ComposedJourney journey) {
        if (journey == null || journey.getJourneys().isEmpty()) {
            return "Trajet invalide";
        }
        
        List<data.Journey> segments = journey.getJourneys();
        
        // Vérifier s'il y a une rupture de continuité
        boolean hasGap = false;
        for (int i = 0; i < segments.size() - 1; i++) {
            if (!segments.get(i).getStop().equals(segments.get(i + 1).getStart())) {
                hasGap = true;
                break;
            }
        }
        
        // Si rupture de continuité, afficher chaque segment séparément
        if (hasGap && segments.size() > 1) {
            StringBuilder sb = new StringBuilder("⚠️ Segments non connectés : ");
            for (int i = 0; i < segments.size(); i++) {
                data.Journey segment = segments.get(i);
                int hours = segment.getDepartureDate() / 100;
                int minutes = segment.getDepartureDate() % 100;
                String formattedTime = String.format("%02d:%02d", hours, minutes);
                
                sb.append(String.format("%s %s→%s (%s, %d min, %.2f€)",
                    formattedTime,
                    segment.getStart(),
                    segment.getStop(),
                    segment.getMeans(),
                    segment.getDuration(),
                    segment.getCost()));
                
                if (i < segments.size() - 1) {
                    sb.append(" | ");
                }
            }
            return sb.toString();
        }
        
        // Sinon, affichage normal (trajet continu)
        data.Journey firstSegment = segments.get(0);
        data.Journey lastSegment = segments.get(segments.size() - 1);
        
        String departure = firstSegment.getStart();
        String destination = lastSegment.getStop();
        int departureTime = firstSegment.getDepartureDate();
        
        // Calculer durée totale et prix total
        int totalDuration = 0;
        double totalCost = 0.0;
        String transportType = firstSegment.getMeans();
        
        for (data.Journey segment : segments) {
            totalDuration += segment.getDuration();
            totalCost += segment.getCost();
        }
        
        // Formatage de l'heure (convertir 900 en "09:00")
        int hours = departureTime / 100;
        int minutes = departureTime % 100;
        String formattedTime = String.format("%02d:%02d", hours, minutes);
        
        // Format: "09:00 - A → B | Bus | 10 min | 3,00€"
        return String.format("%s - %s → %s | %s | %d min | %.2f€",
            formattedTime, departure, destination, transportType, totalDuration, totalCost);
    }

    /**
     * NOUVEAU: Crée l'affichage pour un billet individuel
     */
    private String createIndividualTicketDisplay(data.Journey ticket) {
        if (ticket == null) {
            return "🎫 Billet invalide";
        }
        
        // Formatage de l'heure de départ
        int depHours = ticket.getDepartureDate() / 100;
        int depMinutes = ticket.getDepartureDate() % 100;
        String formattedDepTime = String.format("%02d:%02d", depHours, depMinutes);
        
        // Formatage de l'heure d'arrivée
        int arrHours = ticket.getArrivalDate() / 100;
        int arrMinutes = ticket.getArrivalDate() % 100;
        String formattedArrTime = String.format("%02d:%02d", arrHours, arrMinutes);
        
        // Format: "🎫 09:00-09:30 A→B Bus 30min 3,50€"
        return String.format("🎫 %s-%s %s→%s %s %dmin %.2f€",
            formattedDepTime, formattedArrTime,
            ticket.getStart(), ticket.getStop(), 
            ticket.getMeans(), ticket.getDuration(), ticket.getCost());
    }

    /**
     * NOUVEAU: Crée l'affichage pour un billet regroupé avec numérotation
     */
    private String createGroupedTicketDisplay(data.Journey ticket, int billetNum) {
        if (ticket == null) {
            return "  └─ 🎫 Billet invalide";
        }
        
        // Formatage de l'heure de départ
        int depHours = ticket.getDepartureDate() / 100;
        int depMinutes = ticket.getDepartureDate() % 100;
        String formattedDepTime = String.format("%02d:%02d", depHours, depMinutes);
        
        // Formatage de l'heure d'arrivée
        int arrHours = ticket.getArrivalDate() / 100;
        int arrMinutes = ticket.getArrivalDate() % 100;
        String formattedArrTime = String.format("%02d:%02d", arrHours, arrMinutes);
        
        // Format avec indentation: "  └─ Billet 1: 09:00-09:30 A→B Bus 30min 3,50€"
        String prefix = billetNum == 1 ? "  ├─" : "  └─";
        return String.format("%s 🎫 Billet %d: %s-%s %s→%s %s %dmin %.2f€",
            prefix, billetNum, formattedDepTime, formattedArrTime,
            ticket.getStart(), ticket.getStop(), 
            ticket.getMeans(), ticket.getDuration(), ticket.getCost());
    }

    /**
     * Initialize Ollama HTTP client
     */
    private void initializeOllama() {
        try {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();
            println("🤖 Assistant IA activé pour l'analyse de vos demandes");
        } catch (Exception e) {
            println("⚠️ Assistant IA non disponible - utilisez les contrôles manuels");
        }
    }

    /**
     * Create the natural language request panel
     */
    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Natural Language Travel Request"));

        // Weather info panel
        JPanel weatherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        weatherPanel.add(new JLabel("Current city:"));
        
        jListCity = new JComboBox<>(new String[]{"Lille", "Paris", "Lyon", "Marseille", "Toulouse", "Nice", "Nantes", "Strasbourg", "Montpellier", "Bordeaux"});
        jListCity.setSelectedItem("Lille");
        jListCity.setPreferredSize(new Dimension(150, 35));
        jListCity.setFont(new Font("SansSerif", Font.PLAIN, 14));
        jListCity.addActionListener(e -> {
            String selectedCity = (String) jListCity.getSelectedItem();
            WeatherManager.getInstance().setCurrentCity(selectedCity);
            updateWeatherInfo();
        });
        weatherPanel.add(jListCity);
        
        JButton refreshWeatherBtn = new JButton("🌤️ Refresh Weather");
        refreshWeatherBtn.addActionListener(e -> {
            WeatherManager.getInstance().refreshWeatherData();
            updateWeatherInfo();
        });
        weatherPanel.add(refreshWeatherBtn);
        
        lblWeather = new JLabel("Weather: Loading...");
        weatherPanel.add(lblWeather);
        
        panel.add(weatherPanel, BorderLayout.NORTH);

        // Request input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        
        JLabel promptLabel = new JLabel("<html>Describe your travel need in natural language:<br>" +
                "Example: \"I need to go from Lille to Paris tomorrow at 8am by train, cheapest option\"</html>");
        inputPanel.add(promptLabel, BorderLayout.NORTH);
        
        JPanel requestInputPanel = new JPanel(new BorderLayout(5, 5));
        requestField = new JTextField();
        requestField.setPreferredSize(new Dimension(500, 40));
        requestField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        requestField.addActionListener(e -> processNaturalLanguageRequest());
        requestInputPanel.add(requestField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        
        JButton exampleButton = new JButton("💡 Examples");
        exampleButton.setPreferredSize(new Dimension(120, 40));
        exampleButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        exampleButton.setToolTipText("Show example requests");
        exampleButton.addActionListener(e -> showExampleRequests());
        buttonPanel.add(exampleButton);
        
        JButton processButton = new JButton("🤖 Process Request with AI");
        processButton.setPreferredSize(new Dimension(200, 40));
        processButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        processButton.addActionListener(e -> processNaturalLanguageRequest());
        buttonPanel.add(processButton);
        
        requestInputPanel.add(buttonPanel, BorderLayout.EAST);
        inputPanel.add(requestInputPanel, BorderLayout.CENTER);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Create the manual control panel as fallback
     */
    private JPanel createManualControlPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Manual Controls (Fallback)"));
        panel.setLayout(new GridLayout(0, 5, 5, 5));
        
        panel.add(new JLabel("From:"));
        panel.add(new JLabel("To:"));
        panel.add(new JLabel("Transport Type:"));
        
        lblPrice = new JLabel("Departure: 6:00");
        panel.add(lblPrice);
        
        panel.add(new JLabel("Criteria"));

        jListFrom = new JComboBox<>(new String[]{"-", "a", "b", "c", "d", "e", "f"});
        jListFrom.setSelectedIndex(0);
        panel.add(jListFrom);

        jListTo = new JComboBox<>(new String[]{"-", "a", "b", "c", "d", "e", "f"});
        jListTo.setSelectedIndex(0);
        panel.add(jListTo);

        jListTransportType = new JComboBox<>(new String[]{"any", "bus", "car", "bike", "tram"});
        jListTransportType.setSelectedIndex(0);
        panel.add(jListTransportType);

        sliderTimeDeparture = new JSlider();
        sliderTimeDeparture.setPreferredSize(new Dimension(100, 10));
        sliderTimeDeparture.setMinimum(600);
        sliderTimeDeparture.setMaximum(2200);
        sliderTimeDeparture.setMajorTickSpacing(100);
        sliderTimeDeparture.setMinorTickSpacing(25);
        sliderTimeDeparture.setSnapToTicks(true);
        sliderTimeDeparture.setPaintTicks(true);
        sliderTimeDeparture.addChangeListener(event -> {
            int hh = sliderTimeDeparture.getValue() / 100;
            int mm = (int) (sliderTimeDeparture.getValue() % 100 / 100d * 60d);
            String smm = (mm < 10) ? ("0" + mm) : String.valueOf(mm);
            lblPrice.setText("Departure: " + hh + ":" + smm);
            lblPrice.repaint();
        });
        panel.add(sliderTimeDeparture);

        jListCriteria = new JComboBox<>(new String[]{"-", "cost", "co2", "confort", "duration", "duration-cost"});
        jListCriteria.setSelectedIndex(0);
        panel.add(jListCriteria);

        JButton buyButton = new JButton("Buy Travel");
        buyButton.addActionListener(event -> {
            try {
                departure = (String) jListFrom.getSelectedItem();
                arrival = (String) jListTo.getSelectedItem();
                transportType = (String) jListTransportType.getSelectedItem();
                time = sliderTimeDeparture.getValue();
                int hh = sliderTimeDeparture.getValue() / 100;
                int mm = (int) (sliderTimeDeparture.getValue() % 100 / 100d * 60d);
                time = hh * 100 + mm;
                
                // SEND AN GUI EVENT TO THE AGENT !!!
                GuiEvent guiEv = new GuiEvent(this, TravellerAgent.BUY_TRAVEL);
                guiEv.addParameter(departure);
                guiEv.addParameter(arrival);
                guiEv.addParameter(time);
                guiEv.addParameter(jListCriteria.getSelectedItem());
                guiEv.addParameter(transportType); // Add transport type
                myAgent.postGuiEvent(guiEv);
                // END SEND AN GUI EVENT TO THE AGENT !!!
            } catch (Exception e) {
                JOptionPane.showMessageDialog(TravellerGui.this, "Invalid values. " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(buyButton);
        
        return panel;
    }

    /**
     * Process natural language travel request using Ollama
     */
    private void processNaturalLanguageRequest() {
        String request = requestField.getText().trim();
        if (request.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a travel request", "Empty Request", JOptionPane.WARNING_MESSAGE);
            return;
        }

        println("📝 Analyse de votre demande: \"" + request + "\"");
        
        // Show loading message
        SwingUtilities.invokeLater(() -> {
            requestField.setEnabled(false);
            println("🤖 Traitement en cours...");
        });

        // Process in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                String extractedInfo = analyzeRequestWithOllama(request);
                SwingUtilities.invokeLater(() -> {
                    println("✅ Demande comprise ! Recherche des meilleurs trajets...");
                    parseAndExecuteRequest(extractedInfo, request);
                    requestField.setEnabled(true);
                    requestField.setText("");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    println("❌ Problème d'analyse automatique.");
                    println("🔧 Veuillez utiliser les contrôles manuels ci-dessous.");
                    requestField.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * Analyze travel request using Ollama LLM
     */
    private String analyzeRequestWithOllama(String request) throws Exception {
        String prompt = "Extract travel information from this request and return ONLY a JSON object: " +
                "{\"from\": \"departure_location\", \"to\": \"destination_location\", \"time\": \"HHMM_format\", " +
                "\"transport_type\": \"bus|car|bike|tram|any\", \"criteria\": \"cost|co2|confort|duration|duration-cost\"}\n\n" +
                "Map locations to codes: a=station_a, b=station_b, c=station_c, d=station_d, e=station_e, f=station_f.\n" +
                "If missing info, use defaults: time=0800, transport_type=any, criteria=cost.\n" +
                "Return ONLY valid JSON.\n\n" +
                "Request: " + request;

        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", modelName);
        jsonRequest.put("prompt", prompt);
        jsonRequest.put("stream", false);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .timeout(Duration.ofMinutes(5))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("response").trim();
        } else {
            throw new RuntimeException("HTTP Error: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Parse AI response and execute travel request
     */
    private void parseAndExecuteRequest(String aiResponse, String originalRequest) {
        try {
            // Try to extract JSON from response (handle potential extra text)
            String jsonStr = aiResponse;
            int start = jsonStr.indexOf('{');
            int end = jsonStr.lastIndexOf('}');
            if (start >= 0 && end > start) {
                jsonStr = jsonStr.substring(start, end + 1);
            }

            JSONObject parsed = new JSONObject(jsonStr);
            
            // Extract information
            String from = parsed.optString("from", "a");
            String to = parsed.optString("to", "b");
            String timeStr = parsed.optString("time", "0800");
            String transportType = parsed.optString("transport_type", "any");
            String criteria = parsed.optString("criteria", "cost");

            // Convert time string to integer
            int timeInt = Integer.parseInt(timeStr.replaceAll(":", ""));
            
            // Update UI components
            setComboBoxValue(jListFrom, from);
            setComboBoxValue(jListTo, to);
            setComboBoxValue(jListTransportType, transportType);
            setComboBoxValue(jListCriteria, criteria);
            sliderTimeDeparture.setValue(timeInt / 100); // Convert HHMM to hour for slider
            
            // Afficher les informations comprises
            println(String.format("✅ Demande analysée: %s → %s à %s (%s, %s)", 
                    from, to, timeStr, transportType, criteria));
            println("🔍 Recherche automatique en cours...");
            
            // Lancer automatiquement la recherche et réservation
            GuiEvent guiEv = new GuiEvent(this, TravellerAgent.BUY_TRAVEL);
            guiEv.addParameter(from);
            guiEv.addParameter(to);
            guiEv.addParameter(timeInt);
            guiEv.addParameter(criteria);
            guiEv.addParameter(transportType);
            myAgent.postGuiEvent(guiEv);

        } catch (Exception e) {
            println("❌ Problème de traitement de votre demande.");
            println("🔧 Utilisez les contrôles manuels pour faire votre recherche.");
        }
    }

    /**
     * Helper method to set combo box value safely
     */
    private void setComboBoxValue(JComboBox<String> comboBox, String value) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equals(value)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        // If value not found, set to first item or default
        comboBox.setSelectedIndex(0);
    }

    /**
     * Show example natural language requests to help users
     */
    private void showExampleRequests() {
        String[] examples = {
            "Je veux aller de a vers c à 9h en bus, option économique",
            "Trajet rapide de b vers f en vélo après 14h", 
            "Transport confortable de d à e en tram vers 8h",
            "Aller de a vers f vers midi, meilleur prix",
            "Voyage écologique de c vers e vers 16h"
        };
        
        StringBuilder exampleText = new StringBuilder();
        exampleText.append("Examples of natural language travel requests:\n\n");
        
        for (int i = 0; i < examples.length; i++) {
            exampleText.append(String.format("%d. %s\n", i + 1, examples[i]));
        }
        
        exampleText.append("\nStations available: a, b, c, d, e, f");
        exampleText.append("\nTransport types: bus, car, bike, tram, any");
        exampleText.append("\nCriteria: cost, duration, confort, co2, duration-cost");
        
        // Show examples in a dialog
        JTextArea textArea = new JTextArea(exampleText.toString());
        textArea.setEditable(false);
        textArea.setRows(15);
        textArea.setColumns(50);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        Object[] options = {"Use Random Example", "Close"};
        int choice = JOptionPane.showOptionDialog(
            this,
            scrollPane,
            "Travel Request Examples",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options,
            options[1]
        );
        
        if (choice == 0) { // Use Random Example
            String randomExample = examples[(int)(Math.random() * examples.length)];
            requestField.setText(randomExample);
        }
    }

    /**
     * Update weather information display
     */
    private void updateWeatherInfo() {
        SwingUtilities.invokeLater(() -> {
            // Vérifier que le composant est initialisé
            if (lblWeather == null) {
                return; // Ne pas essayer de mettre à jour si le composant n'existe pas encore
            }
            
            WeatherManager weatherManager = WeatherManager.getInstance();
            String weatherInfo = weatherManager.getWeatherImpactDescription();
            lblWeather.setText("<html>" + weatherInfo.replace("\n", "<br>") + "</html>");
            lblWeather.repaint();
        });
    }


    /**
     * add a string to the search results area (for user interactions)
     */
    public void println(String chaine) {
        SwingUtilities.invokeLater(() -> {
            searchResultsArea.append(chaine + "\n");
            searchResultsArea.setCaretPosition(searchResultsArea.getText().length());
        });
    }

    /**
     * add a string to the debug log area (hidden from user)
     */
    public void printDebug(String chaine) {
        SwingUtilities.invokeLater(() -> {
            debugLogArea.append(chaine + "\n");
            debugLogArea.setCaretPosition(debugLogArea.getText().length());
        });
    }

    public void setColor(Color color) {
        searchResultsArea.setBackground(color);
    }

    /**
     * Récupère la liste des trajets réservés
     * @return Liste des trajets réservés
     */
    public List<data.ComposedJourney> getBookedJourneys() {
        return new ArrayList<>(bookedJourneys);
    }
    
    /**
     * Récupère la liste des billets individuels
     * @return Liste des billets individuels
     */
    public List<data.Journey> getIndividualTickets() {
        return new ArrayList<>(individualTickets);
    }
    
    /**
     * Méthode de test pour ajouter des trajets directement sans agent
     * @param journey Le trajet composé à ajouter
     * @param tripDesc Description du trajet
     */
    public void addTripForTest(data.ComposedJourney journey, String tripDesc) {
        // NOUVEAU: Décomposer le trajet en billets individuels avec organisation
        String tripName = "Trajet " + tripCounter;
        data.Journey firstSegment = journey.getJourneys().get(0);
        data.Journey lastSegment = journey.getJourneys().get(journey.getJourneys().size() - 1);
        String tripSummary = tripName + " : " + firstSegment.getStart() + " → " + lastSegment.getStop();
        
        // Ajouter l'en-tête du trajet
        bookedTrips.add("📍 " + tripSummary + " (" + journey.getJourneys().size() + " billets)");
        tripsListModel.addElement("📍 " + tripSummary + " (" + journey.getJourneys().size() + " billets)");
        
        // Ajouter chaque billet avec indentation
        for (int i = 0; i < journey.getJourneys().size(); i++) {
            data.Journey individualTicket = journey.getJourneys().get(i);
            individualTickets.add(individualTicket);
            String ticketDisplay = createGroupedTicketDisplay(individualTicket, i + 1);
            bookedTrips.add(ticketDisplay);
            tripsListModel.addElement(ticketDisplay);
        }
        
        tripCounter++;
        
        // Maintenir la compatibilité
        bookedJourneys.add(journey);
        
        System.out.println("DEBUG: Trajet décomposé en " + journey.getJourneys().size() + " billets individuels");
        for (int i = 0; i < journey.getJourneys().size(); i++) {
            data.Journey ticket = journey.getJourneys().get(i);
            System.out.println("  Billet " + (i+1) + ": " + ticket.getStart() + " → " + ticket.getStop());
        }
    }
    
    /**
     * Supprime un trajet annulé des réservations
     */
    public void removeCancelledJourney(String start, String stop, String means, int departure) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Trouver et supprimer le trajet correspondant
                boolean removed = false;
                
                for (int i = bookedJourneys.size() - 1; i >= 0; i--) {
                    data.ComposedJourney journey = bookedJourneys.get(i);
                    if (hasMatchingSegment(journey, start, stop, means, departure)) {
                        // Supprimer de la liste des objets
                        bookedJourneys.remove(i);
                        
                        // Supprimer aussi de la liste d'affichage
                        if (i < bookedTrips.size()) {
                            bookedTrips.remove(i);
                            tripsListModel.remove(i);
                        }
                        
                        removed = true;
                        System.out.println("DEBUG: Trajet annulé supprimé des réservations: " + 
                                         start + " → " + stop);
                        break;
                    }
                }
                
                if (removed) {
                    // Rafraîchir l'affichage
                    tripsList.revalidate();
                    tripsList.repaint();
                    
                    // Message de confirmation
                    JOptionPane.showMessageDialog(this,
                        "✅ Le trajet annulé a été supprimé de vos réservations.",
                        "Trajet Supprimé",
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
            } catch (Exception e) {
                System.err.println("Erreur lors de la suppression du trajet annulé: " + e.getMessage());
            }
        });
    }
    
    /**
     * Vérifie si un trajet composé contient un segment correspondant
     */
    private boolean hasMatchingSegment(data.ComposedJourney journey, String start, String stop, String means, int departure) {
        if (journey == null || journey.getJourneys() == null) return false;
        
        for (data.Journey segment : journey.getJourneys()) {
            if (segment.getStart().equalsIgnoreCase(start) &&
                segment.getStop().equalsIgnoreCase(stop) &&
                segment.getMeans().equalsIgnoreCase(means) &&
                segment.getDepartureDate() == departure) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generate a personalized confirmation message using Ollama
     */
    private String generateConfirmationMessageWithOllama(data.ComposedJourney journey) {
        try {
            // Build journey summary for Ollama
            data.Journey firstSegment = journey.getJourneys().get(0);
            data.Journey lastSegment = journey.getJourneys().get(journey.getJourneys().size() - 1);
            
            double totalCost = journey.getJourneys().stream().mapToDouble(data.Journey::getCost).sum();
            double totalDuration = journey.getJourneys().stream().mapToDouble(data.Journey::getDuration).sum();
            double totalCo2 = journey.getJourneys().stream().mapToDouble(data.Journey::getCo2).sum();
            
            String journeyInfo = String.format(
                "User booked a trip from %s to %s with %d segments. Total cost: %.2f€, Duration: %.0f minutes, CO2: %.0f. " +
                "Journey details: %s",
                firstSegment.getStart(), lastSegment.getStop(), 
                journey.getJourneys().size(),
                totalCost, totalDuration, totalCo2,
                journey.getJourneys().stream()
                    .map(j -> j.getMeans() + "(" + j.getStart() + "→" + j.getStop() + ")")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("N/A")
            );
            
            String prompt = "Generate a SHORT and FRIENDLY French confirmation message (2-3 sentences max) for this booking. " +
                    "Be enthusiastic but concise. Include emojis. End with one practical tip about the trip.\n\n" +
                    "Booking info: " + journeyInfo;

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("model", modelName);
            jsonRequest.put("prompt", prompt);
            jsonRequest.put("stream", false);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String ollamaResponse = jsonResponse.getString("response").trim();
                
                // Format the response nicely
                return "✅ Réservation confirmée!\n\n" + ollamaResponse + 
                       "\n\nVos " + journey.getJourneys().size() + " billets sont prêts dans l'onglet 'Mes billets'.";
            }
        } catch (Exception e) {
            System.err.println("Ollama confirmation generation failed: " + e.getMessage());
        }
        
        // Fallback to default message if Ollama fails
        return "✅ Trajet réservé avec succès!\n\n" +
               "Votre trajet a été décomposé en " + journey.getJourneys().size() + " billets individuels.\n" +
               "Les places ont été réservées auprès des agences.\n" +
               "Consultez l'onglet 'Mes billets' pour voir tous vos billets.";
    }

    public static void main(String[] args) {
        TravellerGui test = new TravellerGui(null);
        test.setVisible(true);
    }
}
