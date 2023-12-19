package model;

public class User {
    private int UID;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String theme;
    private int personal_best;
    private int currentScore = 0;

    public User(int UID, String username, String password, String email, String nickname, String theme,
            int personal_best) {
        this.UID = UID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.theme = theme;
        this.personal_best = personal_best;
    }

    public int getUID() {
        return UID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTheme() {
        return theme;
    }

    public int getPersonalBest() {
        return personal_best;
    }

    public void setPersonalBest(int personal_best) {
        if (this.personal_best < personal_best) {
            this.personal_best = personal_best;
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        if (this.currentScore < currentScore) {
            this.currentScore = currentScore;
        }
    }
}
