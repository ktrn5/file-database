import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;

// класс для реализации интерфейса
public class DatabaseGUI extends JFrame {
    private Database database;
    private JTable table;
    private DefaultTableModel tableModel;

    public DatabaseGUI() {
        database = new Database();
        setTitle("Sportclub DB");
        setSize(400, 400); // размер
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(10, 1));

        String[] buttonNames = { //все кнопки
                "Create DB", "Delete DB", "Open DB",
                "Add account", "Delete account by ID", "Delete account by Name",
                "Edit account by ID", "Search by ID", "Search by Name",
                "Create Backup", "Restore from Backup"
        };

        Runnable[] actions = { // действия этих кнопок
                this::createDatabase, this::deleteDatabase, this::openDatabase,
                this::addValue, this::deleteValue, this::deleteValueName,
                this::editValue, this::searchById, this::searchByName,
                this::createBackup, this::restoreBackup
        };
        // создаем, используй массив выше
        for (int i = 0; i < buttonNames.length; i++) {
            JButton button = new JButton(buttonNames[i]);
            int finalI = i;
            button.addActionListener(e -> actions[finalI].run());
            add(button);
        }
    }
    // далее идет реализация каждой кнопки. Используются методы класса ДБ + обработка исключений
    private void createDatabase() {
        try {
            Database.createDatabase(this);
        } catch (IOException e) {
            showMessage("Error during the operation: " + e.getMessage());
        }
    }

    private void deleteDatabase() { // удаление ДБ
        try {
            Database.deleteDatabase(this);
        } catch (IOException e) {
            showMessage("Error during the operation: " + e.getMessage());
        }
    }

    private void openDatabase() { // открытие БД
        try {
            Database.getValue(this);
        } catch (IOException e) {
            showMessage("Error during the operation: " + e.getMessage());
        }
    }

    //создание аккаунта.
    // в этом методе реализуется отдельный интерфейс, который используется для ЗАПОЛНЕНИЯ данных в аккаунтне.
    private void addValue() { //
        JDialog dialog = new JDialog(this, "Add", true);
        dialog.setLayout(new GridLayout(5, 2));
        // табличка, названия столбцов.
        String[] names = {
                "Full Name:", "Purchase Date (dd.mm.yyyy):",
                "Expiration Date (dd.mm.yyyy):", "Individual training:"
        };

        JTextField[] fields = new JTextField[3];
        String[] individualTrainingOptions = {"yes", "not"}; // для типа булеан: у нас выбор тру или false
        JComboBox<String> individualTrainingComboBox = new JComboBox<>(individualTrainingOptions);

        for (int i = 0; i < names.length; i++) {
            JLabel label = new JLabel(names[i]);
            if (i < 3) {
                fields[i] = new JTextField();
                dialog.add(label);
                dialog.add(fields[i]);
            } else {
                dialog.add(label);
                dialog.add(individualTrainingComboBox);
            }
        }

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String fullName = fields[0].getText();
            String purchaseDateStr = fields[1].getText();
            String expirationDateStr = fields[2].getText();
            String individualTrainingStr = (String) individualTrainingComboBox.getSelectedItem();
            //все поля заполнены. Используем необходимые форматы и типы
            if (fullName != null && purchaseDateStr != null && expirationDateStr != null && individualTrainingStr != null) {
                LocalDate purchaseDate = LocalDate.parse(purchaseDateStr, Data.DATE_FORMATTER);
                LocalDate expirationDate = LocalDate.parse(expirationDateStr, Data.DATE_FORMATTER);
                boolean individualTraining = Boolean.parseBoolean(individualTrainingStr);
                Data record = null;
                try {
                    record = new Data(Database.getNextId(this), fullName, purchaseDate, expirationDate, individualTraining);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    Database.addValue(this, record);
                    dialog.dispose();
                } catch (IOException ex) {
                    showMessage("Error during the operation " + ex.getMessage());
                }
            }
        });

        dialog.add(saveButton);
        dialog.setSize(400, 300);
        dialog.setVisible(true);
    }

    private void deleteValue() { //удаление по ID
        String idStr = JOptionPane.showInputDialog(this, "Enter ID to delete:");

        if (idStr != null) {
            int id = Integer.parseInt(idStr);
            try {
                Database.deleteValueID(this, id);
            } catch (IOException e) {
                showMessage("Error during the operation " + e.getMessage());
            }
        }
    }

    private void deleteValueName() { // аналогично удаление по имени
        String fullName = JOptionPane.showInputDialog(this, "Enter name to delete:");

        if (fullName != null) {
            try {
                Database.deleteValuebyName(this, fullName);
            } catch (IOException e) {
                showMessage("Error during the operation " + e.getMessage());
            }
        }
    }

    private void editValue() { //для редактирования аналогичная созданию аккаунта функция.
        //реализуем окно, которое будет открываться с УЖЕ введенными данными.
        String idStr = JOptionPane.showInputDialog(this, "Enter ID to edit:");

        if (idStr != null) {
            int id = Integer.parseInt(idStr);
            try {
                Data currentRecord = Database.searchById(this, id);
                if (currentRecord != null) {
                    JDialog dialog = new JDialog(this, "Edit ", true);
                    dialog.setLayout(new GridLayout(5, 2));

                    String[] labels = {
                            "Full Name:", "Purchase Date (dd.mm.yyyy):",
                            "Expiration Date (dd.mm.yyyy):", "Individual training:"
                    };

                    JTextField[] fields = new JTextField[3];
                    String[] individualTrainingOptions = {"yes", "no"};
                    JComboBox<String> individualTrainingComboBox = new JComboBox<>(individualTrainingOptions);
                    individualTrainingComboBox.setSelectedItem(String.valueOf(currentRecord.individualTraining));

                    for (int i = 0; i < labels.length; i++) {
                        JLabel label = new JLabel(labels[i]);
                        if (i < 3) {
                            if (i == 0) {
                                fields[i] = new JTextField(currentRecord.fullName);
                            } else if (i == 1) {
                                fields[i] = new JTextField(currentRecord.purchaseDate.format(Data.DATE_FORMATTER));
                            } else {
                                fields[i] = new JTextField(currentRecord.expirationDate.format(Data.DATE_FORMATTER));
                            }
                            dialog.add(label);
                            dialog.add(fields[i]);
                        } else {
                            dialog.add(label);
                            dialog.add(individualTrainingComboBox);
                        }
                    }

                    JButton saveButton = new JButton("Save");
                    saveButton.addActionListener(e -> {
                        String fullName = fields[0].getText();
                        String purchaseDateStr = fields[1].getText();
                        String expirationDateStr = fields[2].getText();
                        String individualTrainingStr = (String) individualTrainingComboBox.getSelectedItem();

                        if (fullName != null && purchaseDateStr != null && expirationDateStr != null && individualTrainingStr != null) {
                            LocalDate purchaseDate = LocalDate.parse(purchaseDateStr, Data.DATE_FORMATTER);
                            LocalDate expirationDate = LocalDate.parse(expirationDateStr, Data.DATE_FORMATTER);
                            boolean individualTraining = Boolean.parseBoolean(individualTrainingStr);
                            Data newRecord = new Data(id, fullName, purchaseDate, expirationDate, individualTraining);
                            try {
                                Database.editValue(this, id, newRecord);
                                dialog.dispose();
                            } catch (IOException ex) {
                                showMessage("Error during the operation: " + ex.getMessage());
                            }
                        }
                    });

                    dialog.add(saveButton);
                    dialog.setSize(400, 300);
                    dialog.setVisible(true);
                } else {
                    showMessage("Account with ID " + id + " not found.");
                }
            } catch (IOException e) {
                showMessage("Error during the operation " + e.getMessage());
            }
        }
    }
    // поиск по ключу
    private void searchById() {
        String idStr = JOptionPane.showInputDialog(this, "Enter ID to search:");

        if (idStr != null) {
            int id = Integer.parseInt(idStr);
            try {
                Data record = Database.searchById(this, id);
                if (record != null) {
                    updateTable(record.toFileString());
                } else {
                    updateTable(""); // пустая табличка если не найдено ничего
                }
            } catch (IOException e) {
                showMessage("Error during the operation" + e.getMessage());
            }
        }
    }
    // поиск по имени
    private void searchByName() {
        String fullName = JOptionPane.showInputDialog(this, "Enter name to search:");

        if (fullName != null) {
            StringBuilder records = new StringBuilder();
            try {
                Database.searchByFullName(this, fullName, records);
                if (records.length() > 0) {
                    updateTable(records.toString());
                } else {
                    updateTable(""); // пустая табличка если не найдено ничего
                }
            } catch (IOException e) {
                showMessage("Error during the operation " + e.getMessage());
            }
        }
    }

    private void createBackup() {
        try {
            Database.createBackup(this);
        } catch (IOException e) {
            showMessage("Error during the operation " + e.getMessage());
        }
    }

    private void restoreBackup() {
        try {
            Database.restoreFromBackup(this);
        } catch (IOException e) {
            showMessage("Error during the operation " + e.getMessage());
        }
    }
    // публичные методы
    // отображение сообщения (окна с сообщением)
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    // обновление таблички для актуализации данным
    public void updateTable(String records) {
        String[] columnNames = {"ID", "Full Name", "Purchase Date", "Expiration Date", "Individual Training"};
        String[][] data = new String[0][];

        if (records != null && !records.isEmpty()) {
            String[] rows = records.split("\n");
            data = new String[rows.length][];
            for (int i = 0; i < rows.length; i++) {
                data[i] = rows[i].split(",");
            }
        }

        tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame tableFrame = new JFrame("Database values");
        tableFrame.setSize(800, 600);
        tableFrame.add(scrollPane);
        tableFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DatabaseGUI gui = new DatabaseGUI();
            gui.setVisible(true);
        });
    }
}
