package gui;

import jade.gui.GuiEvent;
import agents.TravellerAgent;
import data.WeatherManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Journey resarch Gui, communication with TravellerAgent throw GuiEvent
 *
 * @author modif. Emmanuel Adam - LAMIH
 */
@SuppressWarnings("serial")
public class TravellerGui extends JFrame {

    /**
     * Text area
     */
    private final JTextArea jTextArea;


    private final TravellerAgent myAgent;
    private final JLabel lblPrice;
    private JLabel lblWeather;
    private JComboBox<String> jListFrom;
    private JComboBox<String> jListTo;
    private JComboBox<String> jListCriteria;
    private JComboBox<String> jListCity;
    private JSlider sliderTimeDeparture;

    private String departure;
    private String arrival;
    private int time;

    public TravellerGui(TravellerAgent a) {
        this.setBounds(10, 10, 700, 300);

        myAgent = a;
        if (a != null)
            setTitle(myAgent.getLocalName());

        jTextArea = new JTextArea();
        jTextArea.setBackground(new Color(255, 255, 240));
        jTextArea.setEditable(false);
        jTextArea.setColumns(10);
        jTextArea.setRows(5);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        getContentPane().add(BorderLayout.CENTER, jScrollPane);

        // Create top panel for weather information
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
        
        getContentPane().add(weatherPanel, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(0, 4, 0, 0));
        p.add(new JLabel("From:"));
        p.add(new JLabel("To:"));

        lblPrice = new JLabel("Departure: 6:00");
        p.add(lblPrice);

        p.add(new JLabel("Criteria"));

        getContentPane().add(p, BorderLayout.SOUTH);

        JButton addButton = new JButton("Buy");
        addButton.addActionListener(event -> {
            try {
                departure = (String) jListFrom.getSelectedItem();
                arrival = (String) jListTo.getSelectedItem();
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
                myAgent.postGuiEvent(guiEv);
                // END SEND AN GUI EVENT TO THE AGENT !!!
            } catch (Exception e) {
                JOptionPane.showMessageDialog(TravellerGui.this, "Invalid values. " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        jListFrom = new JComboBox<>(new String[]{"-", "a", "b", "c", "d", "e", "f"});
        jListFrom.setSelectedIndex(0);
        p.add(jListFrom);

        jListTo = new JComboBox<>(new String[]{"-", "a", "b", "c", "d", "e", "f"});
        jListTo.setSelectedIndex(0);
        p.add(jListTo);

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
        p.add(sliderTimeDeparture);

        jListCriteria = new JComboBox<>(new String[]{"-", "cost", "co2", "confort", "duration", "duration-cost"});
        jListCriteria.setSelectedIndex(0);
        p.add(jListCriteria);

        p.add(addButton);
        p.add(new JLabel());
        p.add(new JLabel("Arrival"));

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
