package gestorgastos.repository;

import gestorgastos.model.Cuenta;
import java.util.List;

public interface CuentaRepository {
    
    // Recuperar todas las cuentas guardadas
    List<Cuenta> findAll();
    
    // Guardar una cuenta (nueva o existente)
    void save(Cuenta cuenta);
    
    // Borrar una cuenta
    void delete(Cuenta cuenta);
    
    // Método auxiliar para guardar toda la lista de golpe (muy útil para JSON)
    void saveAll(List<Cuenta> cuentas);
}