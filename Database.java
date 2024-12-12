import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Database {
    private static String main_path = "sportclub.db"; // файл самой БД
    private static final String backup_file = "sportclub_backup.db"; // файл для сохранения

    // 1 МЕТОД: создание базы данных. Учитывает, существует ли уже БД;
    // создается файл (!)
    public static void createDatabase(DatabaseGUI gui) throws IOException {
        File file = new File(main_path);
        if (file.createNewFile()) {
            gui.showMessage("DB created successfully"); // соо в интерфейсе, если все создалось
        } else {
            gui.showMessage("Error: DB already exists"); //соо в интерфейсе, если ошибка
        }
    }

    // 2 МЕТОД: геттер всех значений в существующей БД.
    // Работа напрямую с файлом, если он существует.
    public static void getValue(DatabaseGUI gui) throws IOException {
        File file = new File(main_path);
        if (file.exists()) {
            Scanner scanner = new Scanner(file);
            StringBuilder values = new StringBuilder();
            while (scanner.hasNextLine()) {
                values.append(scanner.nextLine()).append("\n"); // на каждой новой стрчоке = новыый запрос
            }
            scanner.close();
            gui.updateTable(values.toString()); // для актуализации значений
        } else {
            gui.showMessage("Database does not exist."); //соо в интерфейсе, если ошибка
        }
    }

    //МЕТОД 3: реализует автоматическую генерацию ID.
    //Каждая новая запись получает id на 1 больше, чем другое.
    // Это обеспечивает уникальность ключей + избежание опечаток человека + id отсортированы
    public static int getNextId(DatabaseGUI gui) throws IOException {
        File file = new File(main_path);
        int maxId = 0;
        if (file.exists()) {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                Data value = Data.fromFileString(scanner.nextLine());
                if (value.id > maxId) {
                    maxId = value.id;
                }
            }
            scanner.close();
        }
        return maxId + 1;
    }

    // МЕТОД 4: добавляет запись в БД.
    // **работает напрямую с файлом: открывает файл и writer добавляет запись.
    public static void addValue(DatabaseGUI gui, Data value) throws IOException {
        File file = new File(main_path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter writer = new FileWriter(file, true);
        writer.write(value.toFileString() + "\n");
        writer.close();
        gui.showMessage("Data added without errors"); // сообщение об успехе ввода
    }

    // МЕТОД 5: реализация бинпоиска. Позволит в дальнейшем улучшить некоторые методы.
    // Не используется доп. структур данных: реализуется бинпоиск в файле
    // благодаря определенной генерации ключей, не требуется сортировка.
    private static Data binarySearch(int id) throws IOException {
        File file = new File(main_path);
        RandomAccessFile raf = new RandomAccessFile(file, "r"); //  RandomAccessFile позволяет перемещаться к любой позиции в файле с помощью метода seek
        long low = 0;
        long high = raf.length();

        while (low < high) {
            long mid = (low + high) / 2;
            raf.seek(mid);
            if (mid != 0) {
                raf.readLine();
            }
            String line = raf.readLine();
            if (line == null) {
                high = mid;
                continue;
            }
            Data value = Data.fromFileString(line);
            if (value.id == id) {
                raf.close();
                return value;
            } else if (value.id < id) {
                low = raf.getFilePointer();
            } else {
                high = mid;
            }
        }
        raf.close();
        return null;
    }

    // МЕТОД 6: редактирование записей.
    // Если запись найдена, происходит редактирование записи непосредственно в основном файле.
    public static void editValue(DatabaseGUI gui, int id, Data newValue) throws IOException {
        Data valueChange = binarySearch(id); // Используем бинарный поиск для нахождения записи по ключу
        if (valueChange == null) {
            gui.showMessage("Data cannot be found");
            return;
        }

        File file = new File(main_path);
        RandomAccessFile raf = new RandomAccessFile(file, "rw"); // открываем файл для чтения и записи
        String line;
        StringBuilder updatedContent = new StringBuilder();

        while ((line = raf.readLine()) != null) {
            Data initialValue = Data.fromFileString(line);
            if (initialValue.id == id) {
                updatedContent.append(newValue.toFileString()).append("\n");
            } else {
                updatedContent.append(initialValue.toFileString()).append("\n");
            }
        }
        raf.setLength(0); // очищаем файл
        raf.seek(0); // перемещаемся в начало файла
        raf.write(updatedContent.toString().getBytes()); // записываем обновленное содержимое
        raf.close();

        gui.showMessage("Data edited without errors");
    }

    // МЕТОД 7: ищем по ключевому значению
    // используется бинпоиск
    public static Data searchById(DatabaseGUI gui, int id) throws IOException {
        Data value = binarySearch(id);
        if (value != null) {
            return value;
        } else {
            gui.showMessage("Record not found.");
        }
        return value;
    }

    // МЕТОД 8: поиск по НЕключевому значению (в моей бд -- по имени)
    // Поиск как по полному ФИО, таки и отдельно обрабаывает только имя, только фамилию и т.д.
    // создает строку для вывода (в таблицу в дальнейшем)
    public static void searchByFullName(DatabaseGUI gui, String fullName, StringBuilder values) throws IOException {
        try (Scanner scanner = new Scanner(new File(main_path))) {
            while (scanner.hasNextLine()) {
                Data record = Data.fromFileString(scanner.nextLine());
                if (record.fullName.toLowerCase().contains(fullName.toLowerCase())) {
                    values.append(record.toFileString()).append("\n");
                }
            }
        }
    }

    // МЕТОД 9: удаление по ключевому значению.
    public static void deleteValueID(DatabaseGUI gui, int id) throws IOException {
        File file = new File(main_path);
        RandomAccessFile raf = new RandomAccessFile(file, "rw"); // открываем файл для чтения и записи
        String line;
        StringBuilder updatedContent = new StringBuilder();

        while ((line = raf.readLine()) != null) {
            Data value = Data.fromFileString(line);
            if (value.id != id) {
                updatedContent.append(value.toFileString()).append("\n");
            }
        }
        raf.setLength(0); // очищаем файл
        raf.seek(0); // перемещаемся в начало файла
        raf.write(updatedContent.toString().getBytes()); // записываем обновленное содержимое
        raf.close();

        gui.showMessage("Data deleted without errors");
    }

    // МЕТОД 10: удаление по НЕключевому значению.
    public static void deleteValuebyName(DatabaseGUI gui, String fullName) throws IOException {
        File file = new File(main_path);
        RandomAccessFile raf = new RandomAccessFile(file, "rw"); // открываем файл для чтения и записи
        String line;
        StringBuilder updatedContent = new StringBuilder();
        StringBuilder valueDeleted = new StringBuilder();

        searchByFullName(gui, fullName, valueDeleted); // метод поиска для нахождения всех записей, соответствующих полному имени

        while ((line = raf.readLine()) != null) {
            Data value = Data.fromFileString(line);
            if (!valueDeleted.toString().contains(value.toFileString())) {
                updatedContent.append(value.toFileString()).append("\n");
            }
        }
        raf.setLength(0); // очищаем файл
        raf.seek(0); // перемещаемся в начало файла
        raf.write(updatedContent.toString().getBytes()); // записываем обновленное содержимое
        raf.close();

        if (valueDeleted.length() > 0) {
            gui.showMessage("data deleted");
        } else {
            gui.showMessage("data not found.");
        }
    }

    // МЕТОД 11: создание бэкапа (копируем в другой файл)
    public static void createBackup(DatabaseGUI gui) throws IOException {
        File initialFile = new File(main_path);
        File updatesFile = new File(backup_file);
        if (updatesFile.exists()) {
            updatesFile.delete();
        }
        Files.copy(initialFile.toPath(), updatesFile.toPath());
        gui.showMessage("Backup created ");
    }

    // МЕТОД 12: восстановление данных (копируем из другого файла)
    public static void restoreFromBackup(DatabaseGUI gui) throws IOException {
        File initialFile = new File(backup_file);
        File updatedFile = new File(main_path);
        if (updatedFile.exists()) {
            updatedFile.delete();
        }
        Files.copy(initialFile.toPath(), updatedFile.toPath());
        gui.showMessage("DB updated from backup ");
    }

    // МЕТОД 13: удаление файла.
    public static void deleteDatabase(DatabaseGUI gui) throws IOException {
        File file = new File(main_path);
        if (file.delete()) {
            gui.showMessage("DB deleted successfully.");
        } else {
            gui.showMessage("DB doesn't exist");
        }
    }
}
