# 🛠️ Patrones de Diseño

Este documento describe los patrones de diseño aplicados en el **Gestor de Gastos**. Se han implementado soluciones estándar basadas en el catálogo de **Erich Gamma et al. (Patterns GoF)** para resolver problemas recurrentes de creación, estructura y comportamiento en el software.

---

## 1. Patrones de Creación

Tienen como objetivo abstraer el proceso de instanciación de los objetos, haciendo que el sistema sea independiente de cómo se crean sus componentes.

### 1.1. Singleton
**Propósito**: Garantizar que una clase tenga una única instancia y proporcionar un punto de acceso global a ella.
* **Aplicación**: Implementado en las capas de servicios para mantener la consistencia del estado global.
    * `SesionService`: Asegura que solo exista un usuario activo en toda la aplicación.
    * `CuentaService`: Gestiona la instancia única de la cuenta con la que el usuario interactúa.
* **Beneficio**: Evita conflictos en el acceso a datos y ahorra recursos de memoria.

### 1.2. Método Factoría (Factory Method)
**Propósito**: Definir una interfaz para crear un objeto, pero dejar que las subclases decidan qué clase instanciar.
* **Aplicación**: Localizado en el paquete de importación.
    * `FactoriaImportacion`: Centraliza la creación de los distintos adaptadores (`AdaptadorCSV`, `AdaptadorExcel`, etc.) según la extensión del archivo proporcionado.
* **Beneficio**: Desacopla la lógica de creación del código cliente que utiliza los importadores.

---

## 2. Patrones Estructurales

Se centran en cómo se combinan clases y objetos para formar estructuras mayores y más complejas.

### 2.1. Adaptador (Adapter / Wrapper)
**Propósito**: Convertir la interfaz de una clase en otra interfaz que el cliente espera.
* **Aplicación**: Se utiliza para integrar fuentes de datos externas heterogéneas.
    * `AdaptadorCSV`, `AdaptadorExcel`, `AdaptadorJSON`, `AdaptadorTXT`: Implementan la interfaz `Importador`, transformando los datos externos en objetos `GastoTemporal` compatibles con el sistema.
* **Beneficio**: Permite la colaboración de clases con interfaces incompatibles sin modificar su código original.

### 2.1. Fachada (Facade)
**Propósito**: Proporcionar una interfaz unificada y simplificada para un conjunto de interfaces en un subsistema.
* **Aplicación**: La capa de persistencia actúa como una fachada.
    * `CuentaRepository`: Oculta la complejidad de la librería Jackson y la gestión de archivos JSON al resto de la aplicación.
* **Beneficio**: Reduce el acoplamiento entre el subsistema de datos y el resto del sistema.

---

## 3. Patrones de Comportamiento

Gestionan la comunicación entre objetos y la asignación de responsabilidades.

### 3.1. Estrategia (Strategy)
**Propósito**: Definir una familia de algoritmos, encapsular cada uno de ellos y hacerlos intercambiables.
* **Aplicación**: Implementado para la gestión flexible de límites de gasto.
    * `InterfaceAlerta`: Define el contrato para la validación.
    * `EstrategiaSemanal` / `EstrategiaMensual`: Algoritmos concretos que calculan si se ha superado un límite en un periodo de tiempo específico.
* **Beneficio**: Permite cambiar el comportamiento de las alertas en tiempo de ejecución sin alterar la clase `Alerta`.

---

## 4. Resumen de Aplicabilidad

| Patrón | Tipo | Clase/Componente | Motivación Técnica |
| :--- | :--- | :--- | :--- |
| **Singleton** | Creación | `SesionService` | Control de instancia única. |
| **Factoría** | Creación | `FactoriaImportacion` | Encapsular lógica de creación. |
| **Adaptador** | Estructural | `AdaptadorCSV` | Reutilización de clases incompatibles. |
| **Fachada** | Estructural | `CuentaRepository` | Interfaz simplificada al subsistema JSON. |
| **Estrategia** | Comportamiento | `InterfaceAlerta` | Algoritmos intercambiables en ejecución. |

---

## 5. Criterios de Calidad de Diseño

Siguiendo los principios **GRASP** y **DDD** vistos en los temas teóricos, el uso de estos patrones garantiza:

* **Alta Cohesión**: Cada patrón resuelve un problema específico del dominio.
* **Bajo Acoplamiento**: El uso de interfaces y factorías reduce la dependencia entre componentes.
* **Favorecer el cambio**: El sistema es fácilmente extensible para soportar nuevos tipos de cuenta o formatos de importación sin modificar el núcleo del negocio.