package gestorgastos.services;


import gestorgastos.model.Usuario;

public class SesionService {
    private Usuario usuarioActivo;

    public void iniciarSesion(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            this.usuarioActivo = new Usuario(nombre);
            System.out.println("✅ Sesión iniciada para: " + nombre);
        } else {
            System.out.println("⚠️ Nombre de usuario vacío");
        }
    }

    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }
}
