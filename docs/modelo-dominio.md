# 💎 Modelo de Dominio

Este apartado describe la estructura lógica y conceptual del sistema, fundamentada en los principios de **Desarrollo Dirigido por el Dominio (DDD)**. Se centra en las entidades, sus relaciones y las reglas de negocio que gobiernan la gestión de gastos.

---

## 1. Identificación de Entidades y Agregados

Siguiendo los principios de diseño orientado a objetos, el dominio se organiza en objetos con identidad propia (**Entidades**) y agrupaciones lógicas que funcionan como una unidad de consistencia (**Agregados**).

### 1.1. El Agregado "Cuenta"
La clase abstracta `Cuenta` actúa como el **Objeto Raíz** (Root Aggregate) del sistema. Es la entidad principal que encapsula y garantiza la integridad de los elementos que contiene:
* **Gasto**: Entidad con identidad única (UUID) que representa una salida de dinero dentro de la cuenta.
* **Categoría**: Clasificación técnica y visual de los gastos gestionada internamente por cada cuenta.
* **Alerta**: Definición de límites presupuestarios asociados a estrategias temporales específicas.
* **Notificación**: Registro histórico de eventos y límites superados generados por el dominio.

---

## 2. Definición de Objetos del Dominio

De acuerdo con la teoría de diseño de software, los objetos del dominio se clasifican según su función y persistencia:

| Elemento | Tipo (DDD) | Función en el Dominio |
| :--- | :--- | :--- |
| **Cuenta** | Entidad / Raíz | Posee identidad única y coordina el ciclo de vida de gastos, categorías y alertas. |
| **Gasto** | Entidad | Objeto con identidad propia que almacena importe, fecha, hora y el miembro pagador. |
| **Categoría** | Entidad | Define el contexto del gasto (nombre y color hex) para su clasificación visual. |
| **Usuario** | Entidad | Representa al actor del sistema que posee la titularidad de las sesiones activas. |

---

## 3. Lógica y Reglas del Negocio

El modelo de dominio implementa las reglas esenciales que rigen el comportamiento de la aplicación:

### 3.1. Especialización y Polimorfismo
El dominio soporta diferentes lógicas de reparto de gastos mediante una jerarquía polimórfica:
* **Reparto Equitativo**: En `CuentaCompartida`, el sistema divide el total de gastos entre el número de miembros.
* **Reparto Proporcional**: En `CuentaProporcional`, la responsabilidad de cada miembro se pondera según un porcentaje específico, validando que la suma total sea exactamente el 100%.

### 3.2. Gestión de Alertas y Estrategias
El dominio separa la definición de la alerta de su lógica de validación mediante patrones de comportamiento:
* **Como** sistema de control,  
**quiero** verificar límites sin conocer la implementación temporal interna,  
**para** permitir que las reglas semanales o mensuales sean intercambiables mediante el patrón **Strategy**.

---

## 4. Persistencia y Ciclo de Vida

El ciclo de vida de los objetos del dominio se gestiona mediante componentes de infraestructura que respetan la lógica de negocio:

* **Factorías**: Utilizadas en los controladores para la creación de objetos complejos, como las diferentes especializaciones de `Cuenta`.
* **Repositorios**: El componente `CuentaRepository` encapsula la lógica necesaria para recuperar y almacenar las entidades del dominio, abstrayendo el almacenamiento físico en archivos JSON.

* **Criterios de Identidad:**
    * Dado que un objeto (Gasto o Cuenta) se crea en el sistema,
    * Cuando se le asigna un identificador único (UUID o nombre de cuenta),
    * Entonces el sistema puede distinguirlo unívocamente de cualquier otro objeto similar aunque sus atributos coincidan.