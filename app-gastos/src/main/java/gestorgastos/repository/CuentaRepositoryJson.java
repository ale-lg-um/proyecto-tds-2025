package gestorgastos.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gestorgastos.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CuentaRepositoryJson implements CuentaRepository {

    private final String RUTA_ARCHIVO = "cuentas.json";
    private ObjectMapper mapper;

    public CuentaRepositoryJson() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<Cuenta> findAll() {
        File file = new File(RUTA_ARCHIVO);
        if (!file.exists()) return new ArrayList<>();

        try {
            return mapper.readValue(file, new TypeReference<List<Cuenta>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void save(Cuenta cuenta) {
        List<Cuenta> cuentas = findAll();
        
        System.out.println("--- GUARDANDO CUENTA ---");
        System.out.println("Total cuentas antes: " + cuentas.size());
        System.out.println("Intentando guardar ID: " + cuenta.getId());

        // Eliminamos la versión vieja de forma segura
        boolean borrado = cuentas.removeIf(c -> {
            if (c.getId() == null) return false; // Protección anti-null
            return c.getId().equals(cuenta.getId());
        });
        
        System.out.println("¿Se encontró y borró la anterior?: " + borrado);

        // Añadimos la nueva
        cuentas.add(cuenta);
        System.out.println("Total cuentas después: " + cuentas.size());
        
        saveAll(cuentas);
    }

    @Override
    public void delete(Cuenta cuenta) {
        List<Cuenta> cuentas = findAll();
        cuentas.removeIf(c -> c.getId() != null && c.getId().equals(cuenta.getId()));
        saveAll(cuentas);
    }

    @Override
    public void saveAll(List<Cuenta> cuentas) {
        try {
            mapper.writeValue(new File(RUTA_ARCHIVO), cuentas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
