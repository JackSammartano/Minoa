package jack.Minoa.DTO;

import jack.Minoa.User;

public class UserRegistrationDto {

    private String username;
    private String password;
    private User.AccessLevel accessLevel;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User.AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(User.AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}

