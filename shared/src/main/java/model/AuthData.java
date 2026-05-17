package model;

import java.util.UUID;

public record AuthData(String authToken, String username) {
    public static AuthData generateToken(String user){
        return new AuthData(UUID.randomUUID().toString(), user);
    }

}
