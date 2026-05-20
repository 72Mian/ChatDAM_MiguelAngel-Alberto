package es.damdi.miguelangel.chatdam_servidor.model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data // Genera automáticamente getters, setters, toString y constructores gracias a Lombok
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Recuerda que se guardará cifrada

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
}