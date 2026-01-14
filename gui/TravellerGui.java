package gui;

import jade.gui.GuiEvent;
import agents.TravellerAgent;
import data.WeatherManager;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Journey resarch Gui, communication with TravellerAgent throw GuiEvent
 *
 * @author modif. Emmanuel Adam - LAMIH
 */
@SuppressWarnings("serial")
public class TravellerGui extends JFrame {

    /**
     * Text area for results
     */
    private final JTextArea jTextArea;

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
        this.setBounds(10, 10, 800, 500);

        myAgent = a;
        if (a != null)
            setTitle(myAgent.getLocalName());

        jTextArea = new JTextArea();
        jTextArea.setBackground(new Color(255, 255, 240));
        jTextArea.setEditable(false);
        jTextArea.setColumns(10);
        jTextArea.setRows(8);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);

        // Initialize Ollama HTTP client after jTextArea is created
        initializeOllama();

        // Create top panel for natural language request
        JPanel requestPanel = createRequestPanel();
        getContentPane().add(requestPanel, BorderLayout.NORTH);

        // Create bottom panel for manual controls (as fallback)
        JPanel manualPanel = createManualControlPanel();
        getContentPane().add(manualPanel, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // SEND AN GUI EVENT TO THE AGENT !!!
                GuiEvent guiEv = new GuiEvent(this, TravellerAgent.EXIT);
                myAgent.postGuiEvent(guiEv);
                // END SEND AN GUI EVENT TO THE AGENT !!!
            }
        });
        setResizable(true);
        
        // Initialize weather info
        updateWeatherInfo();
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
            sliderTimeDeparture.setValue(timeInt);
            
            // Pas de debug, juste exécuter
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
            WeatherManager weatherManager = WeatherManager.getInstance();
            String weatherInfo = weatherManager.getWeatherImpactDescription();
            lblWeather.setText("<html>" + weatherInfo.replace("\n", "<br>") + "</html>");
            lblWeather.repaint();
        });
    }


    /**
     * add a string to the text area
     */
    public void println(String chaine) {
        String texte = jTextArea.getText();
        texte = texte + chaine + "\n";
        jTextArea.setText(texte);
        jTextArea.setCaretPosition(texte.length());
    }

    public void setColor(Color color) {
        jTextArea.setBackground(color);
    }

    public static void main(String[] args) {
        TravellerGui test = new TravellerGui(null);
        test.setVisible(true);
    }
}
