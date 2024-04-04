import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat; // імпорт класу SimpleDateFormat

public class RailwayApp extends JFrame {
    private TrainSchedule trainSchedule;

    // Виведення вікна додатку.
    public RailwayApp() {
        setTitle("Railway System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        trainSchedule = new TrainSchedule();

        JLabel greetingLabel = new JLabel("Вітаємо! ");
        greetingLabel.setHorizontalAlignment(JLabel.CENTER);
        add(greetingLabel, BorderLayout.NORTH);

        JLabel timeLabel = new JLabel();
        updateTimeLabel(timeLabel);
        add(timeLabel, BorderLayout.CENTER);

        Timer timer = new Timer(1000, e -> updateTimeLabel(timeLabel));
        timer.start();

        JComboBox<String> departureCityComboBox = new JComboBox<>(trainSchedule.getCityNames());
        departureCityComboBox.setSelectedIndex(-1);
        departureCityComboBox.insertItemAt("Виберіть місто відправлення", 0);
        JPanel departurePanel = createCityPanel(departureCityComboBox, "Місто відправлення", "icon_departure.png");

        JComboBox<String> destinationCityComboBox = new JComboBox<>(trainSchedule.getCityNames());
        destinationCityComboBox.setSelectedIndex(-1);
        destinationCityComboBox.insertItemAt("Виберіть місто кінцевого пункту", 0);
        JPanel destinationPanel = createCityPanel(destinationCityComboBox, "Місто призначення", "icon_destination.png");

        DefaultTableModel model = new DefaultTableModel();
        JTable trainTable = new JTable(model);
        model.addColumn("Назва потягу");
        model.addColumn("Місто відправлення");
        model.addColumn("Місто призначення");
        model.addColumn("Час відправлення");
        model.addColumn("Час прибуття");

        JButton showTrainsButton = new JButton("Показати потяги");
        showTrainsButton.addActionListener(e -> {
            model.setRowCount(0);
            String departureCity = (String) departureCityComboBox.getSelectedItem();
            String destinationCity = (String) destinationCityComboBox.getSelectedItem();
            if (!departureCity.equals("Виберіть місто відправлення") &&
                    !destinationCity.equals("Виберіть місто кінцевого пункту")) {
                Train[] trains = trainSchedule.getTrains(departureCity, destinationCity);
                for (Train train : trains) {
                    model.addRow(new Object[]{train.getName(), train.getDepartureCity(), train.getDestination(), train.getDepartureTime(), train.getArrivalTime()});
                }
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        inputPanel.add(departurePanel);
        inputPanel.add(destinationPanel);
        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(showTrainsButton);
        add(buttonPanel, BorderLayout.SOUTH);

        add(new JScrollPane(trainTable), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createCityPanel(JComboBox<String> cityComboBox, String labelName, String iconName) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel cityLabel = new JLabel(labelName);
        cityLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(cityLabel, BorderLayout.NORTH);
        panel.add(cityComboBox, BorderLayout.CENTER);

        ImageIcon icon = new ImageIcon(iconName);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(iconLabel, BorderLayout.SOUTH);

        return panel;
    }
    // Виведення поточної дати та часу.
    private void updateTimeLabel(JLabel timeLabel) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);
        timeLabel.setText("Поточний час: " + formattedDateTime + ", " + getCurrentDayOfWeek());
    }

    private String getCurrentDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String dayOfWeek = new SimpleDateFormat("EEEE").format(date);
        return "Сьогодні " + dayOfWeek;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RailwayApp::new);
    }

    private static class TrainSchedule {
        private Map<String, Train[]> citiesTrains;
        // Список потягів.
        public TrainSchedule() {
            citiesTrains = new HashMap<>();
            citiesTrains.put("Київ", new Train[]{
                    new Train("Київ - Львів", "Київ", "Львів", "09:00", "12:00"),
                    new Train("Київ - Харків", "Київ", "Харків", "10:30", "14:30"),
                    new Train("Київ - Одеса", "Київ", "Одеса", "14:00", "18:00")
            });
            citiesTrains.put("Львів", new Train[]{
                    new Train("Львів - Київ", "Львів", "Київ", "08:45", "12:45"),
                    new Train("Львів - Харків", "Львів", "Харків", "11:00", "15:00"),
                    new Train("Львів - Одеса", "Львів", "Одеса", "15:30", "19:30")
            });
            citiesTrains.put("Харків", new Train[]{
                    new Train("Харків - Київ", "Харків", "Київ", "08:00", "11:00"),
                    new Train("Харків - Львів", "Харків", "Львів", "12:15", "15:15"),
                    new Train("Харків - Одеса", "Харків", "Одеса", "16:45", "19:45")
            });
            citiesTrains.put("Одеса", new Train[]{
                    new Train("Одеса - Київ", "Одеса", "Київ", "07:30", "11:30"),
                    new Train("Одеса - Львів", "Одеса", "Львів", "10:45", "14:45"),
                    new Train("Одеса - Харків", "Одеса", "Харків", "13:20", "17:20")
            });
        }

        public String[] getCityNames() {
            return citiesTrains.keySet().toArray(new String[0]);
        }

        public Train[] getTrains(String departureCity, String destinationCity) {
            return citiesTrains.get(departureCity);
        }
    }
    // Основа.
    private static class Train {
        private final String name;
        private final String departureCity;
        private final String destination;
        private final String departureTime;
        private final String arrivalTime;

        public Train(String name, String departureCity, String destination, String departureTime, String arrivalTime) {
            this.name = name;
            this.departureCity = departureCity;
            this.destination = destination;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
        }

        public String getName() {
            return name;
        }

        public String getDepartureCity() {
            return departureCity;
        }

        public String getDestination() {
            return destination;
        }

        public String getDepartureTime() {
            return departureTime;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }
    }
}
