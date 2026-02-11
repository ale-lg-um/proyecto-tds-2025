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
    // Corrección noveno PR
    private List<Cuenta> cuentas;

    public CuentaRepositoryJson() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.cuentas = cargarDelArchivo();
    }
    
    private List<Cuenta> cargarDelArchivo() {
    	File file = new File(RUTA_ARCHIVO);
    	if(!file.exists()) return new ArrayList<>();
    	
    	try {
    		return mapper.readValue(file,  new TypeReference<List<Cuenta>>() {});
    	} catch (IOException e) {
    		e.printStackTrace();
    		return new ArrayList<>();
    	}
    }

    @Override
    public List<Cuenta> findAll() {
        /*File file = new File(RUTA_ARCHIVO);
        if (!file.exists()) return new ArrayList<>();

        try {
            return mapper.readValue(file, new TypeReference<List<Cuenta>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }*/
    	return this.cuentas;
    }

    @Override
    public void save(Cuenta cuenta) {
        //List<Cuenta> cuentas = findAll();
        
        System.out.println("--- GUARDANDO CUENTA ---");
        System.out.println("Total cuentas antes: " + cuentas.size());
        System.out.println("Intentando guardar ID: " + cuenta.getId());

        
        // Corrección octavo PR
        //cuentas.removeIf(c -> c.getId().equals(cuenta.getId())); // Si la cuenta es nula, se lanzará una excepción, no se tiene que ocultar esa excepción.
        
        //System.out.println("¿Se encontró y borró la anterior?: " + borrado);

        // Añadimos la nueva
        /*cuentas.add(cuenta);
        saveAll(cuentas);
        System.out.println("Total cuentas después: " + cuentas.size());*/
        
        this.cuentas.removeIf(c -> c.getId().equals(cuenta.getId()));
        this.cuentas.add(cuenta);
        System.out.println("Total cuentas después: " + cuentas.size());
        volcar();
    }

    @Override
    public void delete(Cuenta cuenta) {
        List<Cuenta> cuentas = findAll();
        //cuentas.removeIf(c -> c.getId() != null && c.getId().equals(cuenta.getId()));
        // Corrección octavo PR
        cuentas.removeIf(c -> c.getId().equals(cuenta.getId()));
        volcar();
    }

    @Override
    public void saveAll(List<Cuenta> cuentasNuevas) {
        
    	for(Cuenta cuenta : cuentasNuevas) {
    		this.cuentas.removeIf(c -> c.getId().equals(cuenta.getId()));
    		this.cuentas.add(cuenta);
    	}
    	
    	volcar();
    }
    
    private void volcar() {
    	try {
            mapper.writeValue(new File(RUTA_ARCHIVO), cuentas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
