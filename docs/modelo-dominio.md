# 💎 Modelo de Dominio (Actualizado)

Este documento describe la estructura lógica y conceptual del sistema **Gestor de Gastos**, fundamentada en los principios de **Desarrollo Dirigido por el Dominio (DDD)**. Se centra en las entidades, sus relaciones y las reglas de negocio que gobiernan la gestión de finanzas.

---

## 1. Identificación de Entidades y Agregados

Siguiendo los principios de diseño orientado a objetos, el dominio se organiza en objetos con identidad propia (**Entidades**) y agrupaciones lógicas que funcionan como una unidad de consistencia (**Agregados**).

### 1.1. El Agregado "Cuenta"
La clase abstracta `Cuenta` actúa como el **Objeto Raíz** (Root Aggregate) del sistema. Es la entidad principal que encapsula y garantiza la integridad de los elementos que contiene:
* **Gasto**: Entidad que representa una salida de dinero vinculada a la cuenta.
* **Categoría**: Clasificación técnica y visual de los gastos gestionada internamente por cada cuenta.
* **Alerta**: Definición de límites presupuestarios asociados a estrategias temporales específicas.
* **Notificación**: Registro histórico de eventos y límites superados generados por el dominio.

---

## 2. Definición de Objetos del Dominio

De acuerdo con la teoría de diseño de software, los objetos del dominio se clasifican según su función y persistencia:

| Elemento | Tipo (DDD / Patrón) | Función en el Dominio |
| :--- | :--- | :--- |
| **Cuenta** | Entidad / Raíz | Posee identidad única y coordina el ciclo de vida de gastos, categorías y alertas. |
| **Gasto** | Entidad | Objeto con identidad propia que almacena importe, fecha y el miembro pagador. |
| **GastoTemporal** | DTO / Value Object | Objeto de transferencia de datos utilizado exclusivamente en el proceso de importación externa. |
| **Categoría** | Entidad | Define el contexto (nombre y color) para la clasificación de los gastos. |
| **Usuario** | Entidad | Representa al actor del sistema que posee la titularidad de las sesiones. |

---

## 3. Lógica y Reglas del Negocio

El modelo de dominio implementa las reglas esenciales que rigen el comportamiento de la aplicación, aprovechando el polimorfismo para extender su funcionalidad.

### 3.1. Especialización de Cuentas
El dominio soporta diferentes lógicas de reparto de gastos mediante una jerarquía polimórfica:
* **CuentaPersonal**: Realiza un seguimiento simple para un único usuario.
* **CuentaCompartida**: Divide el total de gastos equitativamente entre los miembros registrados.
* **CuentaProporcional**: Especialización donde la responsabilidad de cada miembro se pondera según un porcentaje específico, validando que la suma total sea el 100%.

### 3.2. Validación de Alertas (Patrón Strategy)
El dominio separa la definición de la alerta de su lógica de comprobación temporal:
* **Como** sistema de control,  
**quiero** verificar límites sin conocer la implementación interna de los tiempos,  
**para** permitir que las reglas semanales o mensuales sean intercambiables mediante la `InterfaceAlerta`.

---

## 4. Gestión del Ciclo de Vida de los Objetos

El ciclo de vida de los objetos del dominio se gestiona mediante componentes especializados que respetan la lógica de negocio:

* **Factorías (`FactoriaImportacion`)**: Clases encargadas de la construcción de objetos complejos. Se utiliza para instanciar el adaptador de importación correcto según la extensión del archivo (CSV, JSON, Excel, TXT).
* **Repositorios (`CuentaRepository`)**: Encapsulan la lógica necesaria para recuperar y almacenar las entidades del dominio, actuando como una colección global accesible para los servicios.
* **Servicios de Dominio**: Clases como `ServicioAlertas` y `CuentaService` que contienen lógica de negocio que no pertenece naturalmente a una única entidad.

* **Criterios de Identidad:**
    * Dado que un objeto (Gasto o Cuenta) se crea en el sistema,
    * Cuando se le asigna un identificador único (ID),
    * Entonces el sistema puede distinguirlo unívocamente de cualquier otro objeto similar aunque sus atributos coincidan.