package es.damdi.miguelangel.chatdam_servidor.repository;

import es.damdi.miguelangel.chatdam_servidor.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    // Esta consulta mágica cumple con el requisito obligatorio:
    // "Los últimos 10 mensajes ordenados por su fecha y hora"
    List<Mensaje> findTop10ByOrderByFechaDescHoraDesc();
}
