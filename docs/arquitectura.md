# 🏗️ Arquitectura del Sistema

Este documento describe la estructura técnica y los patrones de diseño aplicados en el proyecto **Gestor de Gastos**, garantizando la separación de responsabilidades y la integridad de los datos financieros.

---

## 1. Modelo Arquitectónico

El sistema se basa en una **Arquitectura Multicapa** que implementa el patrón **MVC (Modelo-Vista-Controlador)** para desacoplar la lógica de negocio de la interfaz de usuario.

### MVC: Componentes principales
* **Modelo**: Gestiona los datos y las reglas del dominio, incluyendo la jerarquía de cuentas, gastos y el sistema de alertas.
* **Vista**: Compuesta por la interfaz gráfica desarrollada en JavaFX y la terminal interactiva (CLI) para usuarios avanzados.
* **Controlador**: Actúa como intermediario, recibiendo las acciones del usuario y coordinando las respuestas mediante la invocación de servicios.

---

## 2. Desglose de Capas Técnicas

La aplicación organiza su código en paquetes específicos según su responsabilidad funcional:

### Capa de Presentación (`app_gastos` / `cli`)
**Como** sistema de interacción,  
**debe** proporcionar medios visuales y textuales para que el usuario gestione su información.
* **JavaFX**: Utiliza archivos FXML para definir la estructura de las ventanas.
* **GestorCLI**: Implementa un hilo independiente (`Runnable`) para permitir el control de la cuenta mediante comandos de consola.

### Capa de Control (`controller`)
**Como** núcleo de coordinación,  
**debe** validar la entrada del usuario y actualizar la vista tras procesar los datos.
* Gestiona el ciclo de vida de las ventanas y la navegación entre los diferentes menús de la aplicación.

### Capa de Negocio (`services`)
**Como** cerebro del sistema,  
**debe** aplicar las reglas de cálculo de saldos y la lógica de bloqueo por alertas.
* **ServicioAlertas**: Comprueba si un nuevo gasto excede los límites configurados antes de permitir su registro definitivo.

### Capa de Persistencia (`repository`)
**Como** almacén de datos,  
**debe** garantizar que la información se guarde físicamente en el dispositivo.
* **CuentaRepositoryJson**: Utiliza la librería Jackson para serializar objetos Java en un archivo local llamado `cuentas.json`.

---

## 3. Patrones de Diseño Aplicados

### 3.1. Patrón Strategy (`strategies`)
Utilizado para implementar la verificación flexible de límites de gasto sin modificar la estructura de las alertas.
* **EstrategiaMensual**: Calcula el acumulado del mes corriente para validar el límite de una alerta.
* **EstrategiaSemanal**: Determina si el gasto se encuentra dentro del intervalo de la semana actual.

### 3.2. Patrón Singleton (`services`)
Asegura que solo exista una instancia de los servicios críticos durante la ejecución de la aplicación para mantener la consistencia.
* **SesionService**: Mantiene la identidad del usuario que ha iniciado sesión de forma global.
* **CuentaService**: Centraliza el acceso al repositorio de cuentas para evitar conflictos en la escritura de datos.

---

## 4. Jerarquía y Polimorfismo de Cuentas

El sistema emplea **herencia y polimorfismo** para gestionar diferentes tipos de reparto de gastos desde una base común denominada `Cuenta`.

* **CuentaPersonal**: Diseñada para el seguimiento individual de finanzas sin gestión de miembros externos.
* **CuentaCompartida**: Implementa el cálculo de saldos equitativos dividiendo los gastos entre una lista de miembros.
* **CuentaProporcional (Especial)**: Extiende la cuenta compartida para aplicar porcentajes de responsabilidad personalizados a cada participante.

---

## 5. Flujo de Datos y Persistencia

El sistema garantiza la **integridad de la información** mediante un flujo de guardado automático tras cada operación relevante.

* **Criterios de Persistencia:**
    * Dado que se realiza cualquier modificación (crear cuenta, añadir gasto o borrar categoría),
    * Cuando el servicio correspondiente confirma el cambio lógico,
    * Entonces se invoca al repositorio para sobrescribir el archivo JSON con el estado más reciente de los objetos.