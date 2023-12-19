package model;

public class GuestUser extends User {
    public GuestUser() {
        super(-1, "guest", "guest", "guest2@example.com", "Guest Player", "Default", 0);
    }
}
