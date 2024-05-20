import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;

public class RailwayApp extends JFrame {
    private TrainSchedule trainSchedule;
    private TicketSystem ticketSystem;

    public RailwayApp() {
        setTitle("Railway System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        trainSchedule = new TrainSchedule();
        ticketSystem = new TicketSystem();

        Passenger passenger = new Passenger();
        passenger.inputPassengerData();

        String passengerName = passenger.getName();
        String studentID = passenger.getStudentID();

        JLabel greetingLabel = new JLabel("Вітаємо! ");
        greetingLabel.setHorizontalAlignment(JLabel.CENTER);
        add(greetingLabel, BorderLayout.NORTH);

        JLabel timeLabel = new JLabel();
        updateTimeLabel(timeLabel);
        add(timeLabel, BorderLayout.CENTER);

        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> updateTimeLabel(timeLabel));
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
        model.addColumn("Час у дорозі");
        model.addColumn("Тип потягу");
        model.addColumn("Дії");

        JButton showTrainsButton = new JButton("Показати потяги");
        showTrainsButton.addActionListener(e -> {
            model.setRowCount(0);
            String departureCity = (String) departureCityComboBox.getSelectedItem();
            String destinationCity = (String) destinationCityComboBox.getSelectedItem();
            if (departureCity != null && destinationCity != null &&
                    !departureCity.equals("Виберіть місто відправлення") &&
                    !destinationCity.equals("Виберіть місто кінцевого пункту")) {
                Train[] trains = trainSchedule.getTrains(departureCity, destinationCity);
                for (Train train : trains) {
                    long travelTime = train.getTravelTime();
                    long hours = travelTime / 60;
                    long minutes = travelTime % 60;
                    model.addRow(new Object[]{
                            train.getName(), train.getDepartureCity(), train.getDestination(),
                            train.getDepartureTime(), train.getArrivalTime(),
                            String.format("%02d:%02d", hours, minutes), train.getTrainType(),
                            "Купити"
                    });
                }
            }
        });

        JPanel inputPanel = new JPanel(new GridLayout(2, 1));
        inputPanel.add(departurePanel);
        inputPanel.add(destinationPanel);
        add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(showTrainsButton);
        add(buttonPanel, BorderLayout.SOUTH);

        JComboBox<String> stationComboBox = new JComboBox<>(trainSchedule.getCityNames());
        stationComboBox.setSelectedIndex(-1);
        stationComboBox.insertItemAt("Виберіть станцію", 0);
        stationComboBox.addActionListener(e -> {
            String selectedStation = (String) stationComboBox.getSelectedItem();
            if (selectedStation != null && !selectedStation.equals("Виберіть станцію")) {
                model.setRowCount(0);
                for (Map.Entry<String, List<Train>> entry : trainSchedule.getCitiesTrains().entrySet()) {
                    for (Train train : entry.getValue()) {
                        if (train.getDepartureCity().equals(selectedStation) || train.getDestination().equals(selectedStation)) {
                            long travelTime = train.getTravelTime();
                            long hours = travelTime / 60;
                            long minutes = travelTime % 60;
                            model.addRow(new Object[]{
                                    train.getName(), train.getDepartureCity(), train.getDestination(),
                                    train.getDepartureTime(), train.getArrivalTime(),
                                    String.format("%02d:%02d", hours, minutes), train.getTrainType(),
                                    "Купити"
                            });
                        }
                    }
                }
            }
        });

        JPanel stationPanel = new JPanel(new GridLayout(1, 2));
        JLabel stationLabel = new JLabel("Вибір по станції: ");

        stationLabel.setHorizontalAlignment(JLabel.CENTER);
        stationPanel.add(stationLabel);
        stationPanel.add(stationComboBox);
        buttonPanel.add(stationPanel);

        add(new JScrollPane(trainTable), BorderLayout.CENTER);

        trainTable.getColumn("Дії").setCellRenderer(new ButtonRenderer());
        trainTable.getColumn("Дії").setCellEditor(new ButtonEditor(new JCheckBox(), trainTable));

        setVisible(true);
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        private JButton button;

        public ButtonRenderer() {
            setOpaque(true);
            button = new JButton();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            button.setText((value == null) ? "" : value.toString());
            return button;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;

        public ButtonEditor(JCheckBox checkBox, JTable table) {
            super(checkBox);
            this.table = table;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }

            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    String trainName = (String) table.getValueAt(selectedRow, 0);
                    String departureCity = (String) table.getValueAt(selectedRow, 1);
                    String destination = (String) table.getValueAt(selectedRow, 2);

                    showTicketPurchaseDialog(trainName, departureCity, destination);
                } else {
                    JOptionPane.showMessageDialog(button, "Не вибрано жодного рядка!");
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    private void showTicketPurchaseDialog(String trainName, String departureCity, String destination) {
        JDialog dialog = new JDialog(this, "Придбати квиток", true);
        dialog.setLayout(new GridLayout(6, 2));

        dialog.add(new JLabel("Потяг:"));
        dialog.add(new JLabel(trainName));

        dialog.add(new JLabel("Від:"));
        dialog.add(new JLabel(departureCity));

        dialog.add(new JLabel("До:"));
        dialog.add(new JLabel(destination));

        JComboBox<String> carriageTypeComboBox = new JComboBox<>(new String[]{"Плацкарт - 120 грн", "Купе - 180 грн", "Люкс Купе - 250 грн"});
        dialog.add(new JLabel("Тип вагону:"));
        dialog.add(carriageTypeComboBox);

        JComboBox<Integer> carriageNumberComboBox = new JComboBox<>();
        JComboBox<Integer> seatNumberComboBox = new JComboBox<>();
        carriageTypeComboBox.addActionListener(e -> {
            carriageNumberComboBox.removeAllItems();
            seatNumberComboBox.removeAllItems();

            String selectedCarriageType = (String) carriageTypeComboBox.getSelectedItem();
            List<Integer> carriageNumbers = trainSchedule.getCarriageNumbers(trainName, selectedCarriageType);
            for (Integer number : carriageNumbers) {
                carriageNumberComboBox.addItem(number);
            }

            carriageNumberComboBox.setSelectedIndex(-1);
        });

        carriageNumberComboBox.addActionListener(e -> {
            seatNumberComboBox.removeAllItems();
            Integer selectedCarriageNumber = (Integer) carriageNumberComboBox.getSelectedItem();
            if (selectedCarriageNumber != null) {
                List<Integer> availableSeats = trainSchedule.getAvailableSeats(trainName, selectedCarriageNumber);
                for (Integer seat : availableSeats) {
                    seatNumberComboBox.addItem(seat);
                }
            }
        });

        dialog.add(new JLabel("Номер вагону:"));
        dialog.add(carriageNumberComboBox);

        dialog.add(new JLabel("Номер місця:"));
        dialog.add(seatNumberComboBox);

        JButton purchaseButton = new JButton("Купити");
        purchaseButton.addActionListener(e -> {
            String selectedCarriageType = (String) carriageTypeComboBox.getSelectedItem();
            Integer selectedCarriageNumber = (Integer) carriageNumberComboBox.getSelectedItem();
            Integer selectedSeatNumber = (Integer) seatNumberComboBox.getSelectedItem();
            if (selectedCarriageType != null && selectedCarriageNumber != null && selectedSeatNumber != null) {
                String ticketNumber = UUID.randomUUID().toString().substring(0, 8);
                double price = getPrice(selectedCarriageType);
                String message = String.format("Квиток придбано!\nПотяг: %s\nВід: %s\nДо: %s\nТип вагону: %s\nНомер вагону: %d\nНомер місця: %d\nЦіна: %.2f грн\nНомер квитка: %s",
                        trainName, departureCity, destination, selectedCarriageType, selectedCarriageNumber, selectedSeatNumber, price, ticketNumber);
                JOptionPane.showMessageDialog(dialog, message, "Квиток придбано", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Будь ласка, оберіть тип вагону, номер вагону та номер місця.", "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JLabel());
        dialog.add(purchaseButton);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private double getPrice(String carriageType) {
        switch (carriageType) {
            case "Плацкарт - 120 грн":
                return 120.0;
            case "Купе - 180 грн":
                return 180.0;
            case "Люкс Купе - 250 грн":
                return 250.0;
            default:
                return 0.0;
        }
    }

    private void updateTimeLabel(JLabel timeLabel) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = LocalDateTime.now().format(formatter);
        timeLabel.setText(formattedTime);
    }

    private JPanel createCityPanel(JComboBox<String> comboBox, String labelText, String iconName) {
        JPanel panel = new JPanel(new GridLayout(1, 2));
        JLabel label = new JLabel(labelText);
        label.setHorizontalAlignment(JLabel.CENTER);
        panel.add(label);
        panel.add(comboBox);
        return panel;
    }


    public static void main(String[] args) {
        new RailwayApp();
    }
}

class TrainSchedule {
    private Map<String, List<Train>> citiesTrains;

    public TrainSchedule() {
        citiesTrains = new HashMap<>();
        initializeCitiesTrains();
    }

    public void initializeCitiesTrains() {
        citiesTrains.put("Київ", new ArrayList<>());
        citiesTrains.put("Львів", new ArrayList<>());
        citiesTrains.put("Одеса", new ArrayList<>());
        citiesTrains.put("Харків", new ArrayList<>());
        citiesTrains.put("Луцьк", new ArrayList<>());
        citiesTrains.put("Ковель", new ArrayList<>());
        citiesTrains.put("Полтава", new ArrayList<>());
        citiesTrains.put("Івано-Франківськ", new ArrayList<>());
        citiesTrains.put("Рівне", new ArrayList<>());
        citiesTrains.put("Чернігів", new ArrayList<>());
        citiesTrains.put("Ужгород", new ArrayList<>());
        citiesTrains.put("Краматорськ", new ArrayList<>());
        citiesTrains.put("Хелм", new ArrayList<>());
        citiesTrains.put("Дніпро", new ArrayList<>());

        Train train1 = new Train("Інтерсіті 725", "Київ", "Львів", LocalTime.of(6, 45), LocalTime.of(12, 10), "Інтерсіті", 325);
        Train train2 = new Train("Нічний Експрес 10", "Київ", "Одеса", LocalTime.of(22, 35), LocalTime.of(6, 15), "Нічний Експрес", 460);
        Train train3 = new Train("Швидкий 112", "Львів", "Харків", LocalTime.of(7, 10), LocalTime.of(17, 25), "Швидкий", 615);
        Train train4 = new Train("064Л Оберіг", "Львів", "Харків", LocalTime.of(15, 30), LocalTime.of(5, 56), "Стандарт", 840);
        Train train5 = new Train("368Л", "Луцьк", "Львів", LocalTime.of(17, 45), LocalTime.of(22, 12), "Стандарт", 267);
        Train train6 = new Train("756К Пасажирський", "Луцьк", "Київ", LocalTime.of(16, 27), LocalTime.of(22, 32), "Стандарт", 365);
        Train train7 = new Train("Нічний експрес 008Ш", "Одеса", "Харків", LocalTime.of(19, 56), LocalTime.of(13, 22), "Стандарт", 1047);
        Train train8 = new Train("148Ш Пасажирський", "Одеса", "Київ", LocalTime.of(17, 23), LocalTime.of(9, 3), "Швидкий", 940);
        Train train9 = new Train("078Л Пасажирський", "Ковель", "Одеса", LocalTime.of(17, 35), LocalTime.of(9, 8), "Стандарт", 933);
        Train train10 = new Train("Нічний Експрес 098Л", "Ковель", "Київ", LocalTime.of(15, 58), LocalTime.of(22, 54), "Стандарт", 426);
        Train train11 = new Train("149О Пасажирський", "Полтава", "Чернівці", LocalTime.of(16, 52), LocalTime.of(22, 59), "Стандарт", 367);
        Train train12 = new Train("141Л Пасажирський", "Івано-Франківськ", "Чернігів", LocalTime.of(14, 44), LocalTime.of(17, 7), "Стандарт", 143);
        Train train13 = new Train("Нічний Експрес 001Л", "Івано-Франківськ", "Харків", LocalTime.of(19, 53), LocalTime.of(22, 10), "Стандарт", 137);
        Train train14 = new Train("804Ш Пасажирський", "Рівне", "Львів", LocalTime.of(6, 40), LocalTime.of(9, 29), "Стандарт", 169);
        Train train15 = new Train("806Ш Пасажирський", "Рівне", "Львів", LocalTime.of(13, 20), LocalTime.of(16, 15), "Стандарт", 176);
        Train train16 = new Train("368Л Пасажирський", "Луцьк", "Ужгород", LocalTime.of(15, 2), LocalTime.of(18, 12), "Стандарт", 190);
        Train train17 = new Train("368Л Пасажирський", "Луцьк", "Ужгород", LocalTime.of(19, 2), LocalTime.of(22, 12), "Стандарт", 190);
        Train train18 = new Train("Нічний Експрес 029К", "Київ", "Ужгород", LocalTime.of(2, 26), LocalTime.of(7, 33), "Нічний Експрес", 305);
        Train train19 = new Train("038Ш Пасажирський", "Одеса", "Ужгород", LocalTime.of(3, 20), LocalTime.of(9, 1), "Стандарт", 340);
        Train train20 = new Train("368Л Пасажирський", "Луцьк", "Ужгород", LocalTime.of(19, 2), LocalTime.of(22, 12), "Стандарт", 190);
        Train train21 = new Train("104Л Пасажирський", "Львів", "Краматорськ", LocalTime.of(18, 42), LocalTime.of(15, 17), "Стандарт", 1235);
        Train train22 = new Train("Інтерсіті 712К ", "Київ", "Краматорськ", LocalTime.of(6, 45), LocalTime.of(13, 41), "Інтерсіті", 416);
        Train train23 = new Train("094О Пасажирський", "Хелм", "Харків", LocalTime.of(1, 16), LocalTime.of(13, 53), "Стандарт", 806);
        Train train24 = new Train("120Д Пасажирський", "Хелм", "Дніпро", LocalTime.of(10, 27), LocalTime.of(7, 28), "Стандарт", 1560);
        Train train25 = new Train("733Д Пасажирський", "Дніпро", "Київ", LocalTime.of(6, 18), LocalTime.of(13, 41), "Стандарт", 446);
        Train train26 = new Train("Нічний Експрес 079П", "Дніпро", "Львів", LocalTime.of(21, 58), LocalTime.of(5, 42), "Нічний Експрес", 464);

        addTrain(train1);
        addTrain(train2);
        addTrain(train3);
        addTrain(train4);
        addTrain(train5);
        addTrain(train6);
        addTrain(train7);
        addTrain(train8);
        addTrain(train9);
        addTrain(train10);
        addTrain(train11);
        addTrain(train12);
        addTrain(train13);
        addTrain(train14);
        addTrain(train15);
        addTrain(train16);
        addTrain(train17);
        addTrain(train18);
        addTrain(train19);
        addTrain(train20);
        addTrain(train21);
        addTrain(train22);
        addTrain(train23);
        addTrain(train24);
        addTrain(train25);
        addTrain(train26);
    }

    public String[] getCityNames() {
        return citiesTrains.keySet().toArray(new String[0]);
    }

    public void addTrain(Train train) {
        List<Train> trains = citiesTrains.getOrDefault(train.getDepartureCity(), new ArrayList<>());
        trains.add(train);
        citiesTrains.put(train.getDepartureCity(), trains);
    }

    public Train[] getTrains(String departureCity, String destination) {
        List<Train> result = new ArrayList<>();
        for (List<Train> trains : citiesTrains.values()) {
            for (Train train : trains) {
                if (train.getDepartureCity().equals(departureCity) && train.getDestination().equals(destination)) {
                    result.add(train);
                }
            }
        }
        return result.toArray(new Train[0]);
    }

    public Map<String, List<Train>> getCitiesTrains() {
        return citiesTrains;
    }

    public List<Integer> getCarriageNumbers(String trainName, String carriageType) {
        return IntStream.rangeClosed(1, 5).boxed().toList();
    }

    public List<Integer> getAvailableSeats(String trainName, int carriageNumber) {
        return IntStream.rangeClosed(1, 10).boxed().toList();
    }
}

class Train {
    private String name;
    private String departureCity;
    private String destination;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String trainType;
    private long travelTime;

    public Train(String name, String departureCity, String destination, LocalTime departureTime, LocalTime arrivalTime, String trainType, long travelTime) {
        this.name = name;
        this.departureCity = departureCity;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trainType = trainType;
        this.travelTime = travelTime;
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

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public String getTrainType() {
        return trainType;
    }

    public long getTravelTime() {
        return travelTime;
    }
}

class TicketSystem {
    private List<Ticket> tickets;

    public TicketSystem() {
        tickets = new ArrayList<>();
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
}

class Ticket {
    private String ticketNumber;
    private String carriageType;
    private int seatNumber;

    public Ticket(String ticketNumber, String carriageType, int seatNumber) {
        this.ticketNumber = ticketNumber;
        this.carriageType = carriageType;
        this.seatNumber = seatNumber;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getCarriageType() {
        return carriageType;
    }

    public void setCarriageType(String carriageType) {
        this.carriageType = carriageType;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
}


class Passenger {
    private String name;
    private String studentID;

    public Passenger(String name, String studentID) {
        this.name = name;
        this.studentID = studentID;
    }

    public Passenger() {
        this.name = "";
        this.studentID = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public void inputPassengerData() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введіть ім'я пасажира:");
        this.name = scanner.nextLine();
        System.out.println("Введіть номер студентського квитка:");
        this.studentID = scanner.nextLine();
    }
}
class TrainTicketSystem {
    private List<Ticket> tickets;
    private List<Passenger> passengers;

    public TrainTicketSystem() {
        tickets = new ArrayList<>();
        passengers = new ArrayList<>();
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    public void removeTicket(Ticket ticket) {
        tickets.remove(ticket);
    }

    public void addPassenger(Passenger passenger) {
        passengers.add(passenger);
    }

    public void removePassenger(Passenger passenger) {
        passengers.remove(passenger);
    }
}