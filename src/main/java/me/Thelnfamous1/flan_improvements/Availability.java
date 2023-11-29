package me.Thelnfamous1.flan_improvements;

public enum Availability {
    AVAILABLE(true),
    UNAVAILABLE(false),
    UNKNOWN(false);

    private final boolean value;

    Availability(boolean value){
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    public static Availability from(boolean available) {
        return available ? AVAILABLE : UNAVAILABLE;
    }

}
