package es.damdi.miguelangel.chatdam_servidor.repository;

import es.damdi.miguelangel.chatdam_servidor.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findTop10ByOrderByFechaDescHoraDesc();
}
