package dto;

public class BotCredentials {
    private String name;
    private String token;

    public BotCredentials() {}

    public BotCredentials(String name, String token) {
        this.name = name;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
