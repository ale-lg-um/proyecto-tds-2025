# Diagrama de Clases - Gestor de Gastos

A continuación se presenta la estructura de clases del sistema y sus relaciones (herencia, composición y asociación).

```mermaid
classDiagram
    direction BT

    %% Relaciones de Herencia
    CuentaPersonal --|> Cuenta : Extends
    CuentaCompartida --|> Cuenta : Extends
    CuentaProporcional --|> CuentaCompartida : Extends

    %% Relaciones de Composición/Asociación
    Cuenta "1" *-- "many" Gasto : contiene
    Cuenta "1" *-- "many" Categoria : define
    Gasto "many" --> "1" Categoria : pertenece a

    class Cuenta {
        <<Abstract>>
        #String id
        #String nombre
        #List~Gasto~ gastos
        #List~Categoria~ categorias
        +getTipo()* String
        +agregarGasto(Gasto gasto)
        +eliminarGasto(Gasto gasto)
    }

    class CuentaPersonal {
        +getTipo() String
    }

    class CuentaCompartida {
        #List~String~ miembros
        +getTipo() String
        +calcularSaldos() Map
    }

    class CuentaProporcional {
        -Map~String, Double~ porcentajes
        +getTipo() String
        +calcularSaldos() Map
    }

    class Gasto {
        -String id
        -String concepto
        -double importe
        -LocalDate fecha
        -Categoria categoria
        -String pagador
    }

    class Categoria {
        -String nombre
        -String descripcion
        -String colorHex
    }

    class Usuario {
        -String nombre
    }