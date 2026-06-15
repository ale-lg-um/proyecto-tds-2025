package gestorgastos.services;

import gestorgastos.model.Cuenta;
import gestorgastos.model.Usuario;

public class SesionService {
    private static SesionService instancia;   // SINGLETON
    private Usuario usuarioActivo;
    private Cuenta cuentaActiva;

    // Constructor privado para que no se pueda instanciar desde fuera
    private SesionService() {}

    // Método para obtener la instancia única
    public static SesionService getInstancia() {
        if (instancia == null) {
            instancia = new SesionService();
        }
        return instancia;
    }

    // Iniciar sesión
    public void iniciarSesion(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            this.usuarioActivo = new Usuario(nombre);
            System.out.println("✅ Sesión iniciada para: " + nombre);
        } else {
            System.out.println("⚠️ Nombre de usuario vacío");
        }
    }

    // Obtener usuario activo
    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }
    
    public void setCuentaActiva(Cuenta cuenta) {
    	this.cuentaActiva = cuenta;
    }
    
    public Cuenta getCuentaActiva() {
    	return this.cuentaActiva;
    }
}
