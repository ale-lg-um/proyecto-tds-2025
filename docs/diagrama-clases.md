# 📊 Diagrama de Clases - Gestor de Gastos

Este documento describe la estructura de clases del sistema, detallando las relaciones de herencia, realización de interfaces y los patrones de diseño aplicados para garantizar la extensibilidad del proyecto.

```mermaid
classDiagram
    direction BT

    %% Relaciones de Herencia y Realización
    CuentaPersonal --|> Cuenta : Extends
    CuentaCompartida --|> Cuenta : Extends
    CuentaProporcional --|> CuentaCompartida : Extends
    
    AdaptadorCSV ..|> Importador : Realizes
    AdaptadorExcel ..|> Importador : Realizes
    AdaptadorJSON ..|> Importador : Realizes
    AdaptadorTXT ..|> Importador : Realizes
    
    EstrategiaSemanal ..|> InterfaceAlerta : Realizes
    EstrategiaMensual ..|> InterfaceAlerta : Realizes

    %% Relaciones de Composición y Asociación
    Cuenta "1" *-- "many" Gasto : contiene
    Cuenta "1" *-- "many" Categoria : define
    Cuenta "1" *-- "many" Alerta : posee
    Alerta "1" --> "1" InterfaceAlerta : usa (Strategy)
    Gasto "many" --> "1" Categoria : pertenece a
    FactoriaImportacion ..> Importador : crea

    %% Definición de Clases del Dominio
    class Cuenta {
        <<Abstract>>
        #String id
        #String nombre
        #List~Gasto~ gastos
        #List~Categoria~ categorias
        #List~Alerta~ alertas
        +getTipo()* String
    }

    class CuentaProporcional {
        -Map~String, Double~ porcentajes
        +calcularSaldos() Map
    }

    class Gasto {
        -String id
        -double importe
        -LocalDate fecha
        -String pagador
    }

    class Alerta {
        -double limite
        -InterfaceAlerta estrategia
        +esLimiteSuperado(double acumulado) boolean
    }

    %% Sistema de Importación (Novedad)
    class Importador {
        <<Interface>>
        +importar(File archivo) List~GastoTemporal~
    }

    class FactoriaImportacion {
        +getImportador(String extension) Importador
    }

    class GastoTemporal {
        -String concepto
        -double importe
        -String fecha
    }

    %% Capa de Servicios (Singletons)
    class SesionService {
        <<Singleton>>
        -Usuario usuarioActivo
        +getInstancia() SesionService
    }

    class CuentaService {
        <<Singleton>>
        -Cuenta cuentaActiva
        +getInstancia() CuentaService
    }