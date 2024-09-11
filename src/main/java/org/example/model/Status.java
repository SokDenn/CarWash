package org.example.model;

public enum Status {
    WAITING_RESERVATION(1, "Ожидание бронирования"),
    BOOKED(2, "Забронировано"),
    AT_CAR_WASH(3, "На автомойке"),
    CANCELLED(4, "Отменено"),
    COMPLETED(5, "Завершено");


    private final int number;
    private final String name;

    private Status(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public static Status numberOfStatus(int numberStatus) {
        for (Status status : Status.values()) {
            if (status.number == numberStatus) return status;
        }
        return null;
    }

    public static Status nameOfStatus(String name) {
        for (Status status : Status.values()) {
            if (status.name.equalsIgnoreCase(name)) return status;
        }
        return null;
    }

    public static Status nameEntityOfStatus(String entityName) {
        for (Status status : Status.values()) {
            if (status.toString().equalsIgnoreCase(entityName)) return status;
        }
        return null;
    }
}
