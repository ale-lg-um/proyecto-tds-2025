# 🔄 3. Diagrama de Interacción

Este documento presenta el diagrama de secuencia correspondiente a la **Historia de Usuario 01: Registrar Gasto**.

Esta historia ha sido seleccionada por ser la funcionalidad central del sistema, permitiendo visualizar la interacción completa entre las capas de la arquitectura **MVC**, el uso del patrón **Singleton** en el servicio y la persistencia de datos mediante el patrón **Repositorio**.

---

## Diagrama de Secuencia: Registrar Gasto

```mermaid
sequenceDiagram
    actor Usuario
    participant View as Vista (FormularioGasto)
    participant FormCtrl as FormularioGastoController
    participant DetalleCtrl as DetalleCuentaController
    participant Model as Cuenta (Modelo)
    participant Service as CuentaService (Singleton)
    participant Repo as CuentaRepositoryJson
    participant Jackson as ObjectMapper (Librería)

    Note over Usuario, View: El usuario abre la ventana de "Nuevo Gasto"

    Usuario->>View: Introduce Concepto, Importe, Fecha y Categoría
    Usuario->>View: Clic en botón "Guardar"
    View->>FormCtrl: guardar()

    Note over FormCtrl: 1. Validación de datos y creación del objeto

    create participant NuevoGasto as :Gasto
    FormCtrl->>NuevoGasto: new Gasto(...)
    
    FormCtrl-->>View: cerrarVentana()
    
    Note over DetalleCtrl: 2. El controlador principal recibe el control
    
    DetalleCtrl->>FormCtrl: getGastoResultado()
    FormCtrl-->>DetalleCtrl: retorna nuevoGasto
    
    Note over DetalleCtrl: 3. Actualización en Memoria (Modelo)

    DetalleCtrl->>Model: agregarGasto(nuevoGasto)
    activate Model
    Model-->>DetalleCtrl: (Lista actualizada en RAM)
    deactivate Model

    Note over DetalleCtrl: 4. Persistencia (Guardado en Disco)

    DetalleCtrl->>Service: agregarCuenta(null, cuentaActual)
    activate Service
    Service->>Repo: save(cuentaActual)
    activate Repo
    
    Note right of Repo: Carga estado actual, actualiza y serializa
    Repo->>Repo: findAll()
    Repo->>Repo: removeIf(id coincide)
    Repo->>Repo: add(cuentaActual)
    
    Repo->>Jackson: writeValue("cuentas.json", lista)
    activate Jackson
    Jackson-->>Repo: (Archivo JSON escrito)
    deactivate Jackson
    
    Repo-->>Service: void
    deactivate Repo
    Service-->>DetalleCtrl: void
    deactivate Service

    Note over DetalleCtrl: 5. Refresco de la Interfaz

    DetalleCtrl->>DetalleCtrl: actualizarTabla()
    DetalleCtrl-->>Usuario: Muestra el nuevo gasto en la tabla