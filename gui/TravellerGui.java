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
     * Liste des trajets réservés
     */
    private JList<String> tripsList;
    private DefaultListModel<String> tripsListModel;
    private List<String> bookedTrips;

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
        
        // Onglet Mes trajets
        tripsPanel = createTripsPanel();
        tabbedPane.addTab("📋 Mes trajets", tripsPanel);
        
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
        jListFrom.setPreferredSize(new Dimension(60, 25));
        panel.add(jListFrom, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("→"), gbc);
        gbc.gridx = 3;
        jListTo = new JComboBox<>(new String[]{"a", "b", "c", "d", "e", "f"});
        jListTo.setPreferredSize(new Dimension(60, 25));
        panel.add(jListTo, gbc);
        
        gbc.gridx = 4;
        panel.add(new JLabel("🚌"), gbc);
        gbc.gridx = 5;
        jListTransportType = new JComboBox<>(new String[]{"any", "bus", "car", "bike", "tram"});
        jListTransportType.setPreferredSize(new Dimension(70, 25));
        panel.add(jListTransportType, gbc);
        
        // Ligne 2: Heure, Critère, Bouton
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("⏰"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        sliderTimeDeparture = new JSlider(6, 22, 9);
        sliderTimeDeparture.setPreferredSize(new Dimension(120, 25));
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
        jListCriteria.setPreferredSize(new Dimension(80, 25));
        panel.add(jListCriteria, gbc);
        
        // Ligne 3: Météo et bouton recherche
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.anchor = GridBagConstraints.WEST;
        lblWeather = new JLabel("🌤️ Météo: Chargement...");
        lblWeather.setFont(new Font("SansSerif", Font.PLAIN, 10));
        panel.add(lblWeather, gbc);
        
        gbc.gridx = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        JButton searchButton = new JButton("🔍 Rechercher");
        searchButton.setPreferredSize(new Dimension(120, 30));
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
        scrollPane.setBorder(BorderFactory.createTitledBorder("📋 Trajets réservés"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel de boutons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton cancelButton = new JButton("❌ Annuler le trajet sélectionné");
        cancelButton.addActionListener(e -> cancelSelectedTrip());
        
        JButton detailsButton = new JButton("📄 Détails du trajet");
        detailsButton.addActionListener(e -> showTripDetails());
        
        buttonPanel.add(detailsButton);
        buttonPanel.add(cancelButton);
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
     * Annule le trajet sélectionné
     */
    private void cancelSelectedTrip() {
        int selectedIndex = tripsList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un trajet à annuler.", 
                "Aucune sélection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedTrip = tripsListModel.getElementAt(selectedIndex);
        
        int confirmation = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir annuler ce trajet?\n\n" + selectedTrip,
            "Confirmation d'annulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            tripsListModel.removeElementAt(selectedIndex);
            bookedTrips.remove(selectedIndex);
            JOptionPane.showMessageDialog(this, 
                "✅ Trajet annulé avec succès!", 
                "Annulation confirmée", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Affiche les détails du trajet sélectionné
     */
    private void showTripDetails() {
        int selectedIndex = tripsList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un trajet pour voir ses détails.", 
                "Aucune sélection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String selectedTrip = tripsListModel.getElementAt(selectedIndex);
        JOptionPane.showMessageDialog(this, 
            "📄 Détails du trajet:\n\n" + selectedTrip, 
            "Informations détaillées", 
            JOptionPane.INFORMATION_MESSAGE);
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
                    "✅ Trajet réservé avec succès!\n\nVous pouvez le voir dans l'onglet 'Mes trajets'", 
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
        requestField.setPreferredSize(new Dimension(400, 30));
        requestField.addActionListener(e -> processNaturalLanguageRequest());
        requestInputPanel.add(requestField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        
        JButton exampleButton = new JButton("💡 Examples");
        exampleButton.setToolTipText("Show example requests");
        exampleButton.addActionListener(e -> showExampleRequests());
        buttonPanel.add(exampleButton);
        
        JButton processButton = new JButton("🤖 Process Request with AI");
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

    public static void main(String[] args) {
        TravellerGui test = new TravellerGui(null);
        test.setVisible(true);
    }
}
