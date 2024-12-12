import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class Data {
    int id; // ключ каждого аккаунта
    String fullName; // имя
    LocalDate purchaseDate; //начало и конец (даты)
    LocalDate expirationDate;
    boolean individualTraining; // наличие индивидуальных тренировок.

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Data(int id, String fullName, LocalDate purchaseDate, LocalDate expirationDate, boolean individualTraining) {
        this.id = id;
        this.fullName = fullName;
        this.purchaseDate = purchaseDate;
        this.expirationDate = expirationDate;
        this.individualTraining = individualTraining;
    }

    public String toFileString() {
        return id + "," + fullName + "," + purchaseDate.format(DATE_FORMATTER) + "," + expirationDate.format(DATE_FORMATTER) + "," + individualTraining;
    }

    public static Data fromFileString(String line) {
        String[] parts = line.split(","); // через запятую в файле .db
        return new Data(
                Integer.parseInt(parts[0]),
                parts[1],
                LocalDate.parse(parts[2], DATE_FORMATTER),
                LocalDate.parse(parts[3], DATE_FORMATTER),
                Boolean.parseBoolean(parts[4])
        );
    }
}
