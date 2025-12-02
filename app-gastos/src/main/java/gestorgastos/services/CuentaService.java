package gestorgastos.services;

import gestorgastos.model.Categoria;
import gestorgastos.model.Cuenta;
import gestorgastos.model.Gasto;
import gestorgastos.model.Usuario;
import gestorgastos.repository.CuentaRepository;
import gestorgastos.repository.CuentaRepositoryJson;

import java.util.ArrayList;
import java.util.List;

public class CuentaService {

    private static CuentaService instancia;
    
 // Lista maestra de categorías del sistema
    private List<Categoria> categorias;
    
    // Aquí guardamos la referencia al repositorio (el que maneja el archivo)
    private CuentaRepository repositorio;

 // En el constructor CuentaService(), inicializa la lista:
    private CuentaService() {
        this.repositorio = new CuentaRepositoryJson();
        this.categorias = new ArrayList<>();
        // Categoría por defecto OBLIGATORIA
        categorias.add(new Categoria("General", "Gastos varios", "#D3D3D3")); // Gris claro
    }

    public static CuentaService getInstancia() {
        if (instancia == null) instancia = new CuentaService();
        return instancia;
    }

    /**
     * Recupera todas las cuentas del archivo JSON.
     * Al ser una app monousuario, ignoramos el parámetro 'usuario' 
     * y devolvemos todo lo que hay guardado.
     */
    public List<Cuenta> getCuentasDe(Usuario usuario) {
        return repositorio.findAll();
    }
    
    public List<Categoria> getCategorias() {
        return categorias;
    }

    /**
     * Guarda una nueva cuenta en el archivo JSON.
     */
    public void agregarCuenta(Usuario usuario, Cuenta cuenta) {
        repositorio.save(cuenta);
    }
    
    /**
     * Elimina una cuenta del archivo JSON (lo usarás más adelante).
     */
    public void eliminarCuenta(Cuenta cuenta) {
        repositorio.delete(cuenta);
    }
    
    public void agregarCategoria(Categoria cat) {
        if (!categorias.contains(cat)) {
            categorias.add(cat);
            // Aquí idealmente guardaríamos también las categorías en persistencia
            // (Podemos ver persistencia de esto más adelante, por ahora en memoria funciona para la sesión)
        }
    }
    
    public void borrarCategoria(Categoria catABorrar) {
        if ("General".equals(catABorrar.getNombre())) return; // No borrar la default

        // Lógica de reasignación: Buscar la categoría General
        Categoria catGeneral = categorias.stream()
                .filter(c -> "General".equals(c.getNombre()))
                .findFirst().orElse(null);

        if (catGeneral != null) {
            // Recorremos TODAS las cuentas y reasignamos los gastos
            List<Cuenta> todasLasCuentas = repositorio.findAll();
            for (Cuenta c : todasLasCuentas) {
                boolean cambio = false;
                for (Gasto g : c.getGastos()) {
                    if (g.getCategoria().equals(catABorrar)) {
                        g.setCategoria(catGeneral);
                        cambio = true;
                    }
                }
                // Si hubo cambios en la cuenta, la guardamos
                if (cambio) repositorio.save(c); 
            }
        }
        
        categorias.remove(catABorrar);
    }
}