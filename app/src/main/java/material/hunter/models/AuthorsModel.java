package material.hunter.models;

public class AuthorsModel {

    private String nickname;
    private String nicknamedesc;
    private String url;
    private String description;

    public AuthorsModel(String nickname, String nicknamedesc, String url, String description) {
        this.nickname = nickname;
        this.nicknamedesc = nicknamedesc;
        this.url = url;
        this.description = description;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNicknameDesc() {
        return nicknamedesc;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }
}