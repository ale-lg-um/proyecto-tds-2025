# 🏗️ Arquitectura del Sistema

Este documento detalla la estructura técnica y los patrones de diseño aplicados en el proyecto **Gestor de Gastos**. La arquitectura se fundamenta en los principios de **Separación Modelo-Vista** y **Desarrollo Dirigido por el Dominio (DDD)**.

---

## 1. Modelo Arquitectónico

El sistema utiliza una **Arquitectura Multicapa** que implementa el patrón **MVC (Modelo-Vista-Controlador)**. Esta estructura permite un acoplamiento débil entre los componentes, asegurando que la lógica de negocio permanezca independiente de la interfaz de usuario.

### MVC: Componentes principales
* **Modelo**: Representa los datos y las reglas del dominio, incluyendo la jerarquía de cuentas y gastos.
* **Vista**: Gestiona la interacción con el usuario mediante JavaFX (gráfica) y el hilo `GestorCLI` (consola).
* **Controlador**: Actúa como el **Controlador GRASP**, coordinando el flujo entre la vista y los servicios de negocio.

---

## 2. Desglose de Capas Técnicas

La aplicación organiza su código en paquetes específicos según su responsabilidad funcional y principios de alta cohesión:

| Capa | Paquete | Función y Responsabilidad |
| :--- | :--- | :--- |
| **Presentación** | `app_gastos` / `cli` | Punto de entrada del sistema. Gestiona la visualización y captura de eventos. |
| **Control** | `controller` | Traduce las acciones del usuario en la interfaz en operaciones del sistema. |
| **Importación** | `importacion` | Procesa datos externos (CSV, JSON, Excel) transformándolos en objetos del dominio. |
| **Negocio** | `services` | Contiene los **Servicios del Dominio** que implementan la lógica de sesión y alertas. |
| **Persistencia** | `repository` | Implementa el patrón **Repositorio** para el almacenamiento de entidades en JSON. |
| **Modelo** | `model` | Define las entidades y agregados que forman el núcleo del negocio. |

---

## 3. Patrones de Diseño Implementados

Se han aplicado patrones **GoF (Gang of Four)** para resolver problemas recurrentes de creación, estructura y comportamiento:

### 3.1. Patrones de Creación
* **Singleton (`services`)**: Clases como `SesionService` y `CuentaService` aseguran una única instancia global para mantener la consistencia del estado.
* **Método Factoría (`importacion`)**: La clase `FactoriaImportacion` centraliza la creación de adaptadores según el formato de archivo.

### 3.2. Patrones Estructurales
* **Adaptador (Adapter) (`importacion`)**: Permite la colaboración de clases con interfaces incompatibles, convirtiendo diversos formatos externos al modelo del sistema.
* **Fachada (Facade) (`repository`)**: El repositorio proporciona una interfaz simplificada para el subsistema de persistencia en disco.

### 3.3. Patrones de Comportamiento
* **Estrategia (Strategy) (`strategies`)**: Define una familia de algoritmos para la validación de alertas (semanal/mensual), haciéndolos intercambiables en tiempo de ejecución.

---

## 4. Jerarquía y Polimorfismo

El sistema utiliza el **Polimorfismo** para gestionar comportamientos variables de forma transparente para el cliente:

* **Especialización de Cuentas**: A través de la herencia de `Cuenta`, se implementan lógicas de reparto Personal, Compartida y Proporcional.
* **Adaptadores de Importación**: La interfaz `Importador` define el contrato común para todos los formatos soportados (CSV, JSON, etc.).

---

## 5. Ciclo de Vida y Persistencia

Siguiendo las directrices de **DDD**, se controla el ciclo de vida de los objetos para garantizar la integridad de los datos:

* **Agregados**: La clase `Cuenta` actúa como raíz del agregado, gestionando sus propios gastos, categorías y alertas.
* **Persistencia JSON**: Se utiliza un modelo de datos semiestructurado para garantizar la flexibilidad y ligereza en el intercambio de información.
* **DTO (Data Transfer Objects)**: Se emplea `GastoTemporal` para transportar datos desde la capa de importación hacia el dominio de forma segura.

* **Criterios de Consistencia:**
    * Dado que el usuario modifica una entidad o realiza una importación,
    * Cuando la operación es validada por la capa de negocio,
    * Entonces el repositorio sincroniza automáticamente los cambios en el almacenamiento físico.