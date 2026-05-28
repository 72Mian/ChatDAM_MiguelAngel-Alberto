package es.damdi.miguelangel.chatdam_servidor.dto;

import lombok.Data;

@Data // Lombok genera los getters y setters automáticamente
public class LoginRequest {
    private String username;
    private String password;
}
