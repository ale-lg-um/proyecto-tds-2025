# 🔄 3. Diagrama de Interacción

Este documento presenta el diagrama de secuencia correspondiente a la **Historia de Usuario 01: Registrar Gasto**.

Esta historia ha sido seleccionada por ser la funcionalidad central del sistema, permitiendo visualizar la interacción completa entre las capas de la arquitectura **MVC** y la persistencia de datos mediante el patrón **Repositorio**.

---

## Diagrama de Secuencia: Registrar Gasto

```mermaid
sequenceDiagram
    actor Usuario
    participant View as Vista (Formulario)
    participant FormCtrl as FormularioController
    participant DetalleCtrl as DetalleController
    participant Service as CuentaService
    participant Strategy as Estrategias (Semanal/Mensual)
    participant Model as Cuenta (Modelo)
    participant Repo as Repositorio

    Note over Usuario, View: El usuario intenta guardar un nuevo gasto

    Usuario->>View: Rellena datos y clic "Guardar"
    View->>FormCtrl: guardar()
    
    create participant NuevoGasto as :Gasto
    FormCtrl->>NuevoGasto: new Gasto(...)
    FormCtrl-->>DetalleCtrl: Retorna objeto nuevoGasto

    Note over DetalleCtrl, Service: 1. VALIDACIÓN DE ALERTAS (Informativa)

    DetalleCtrl->>Service: verificarAlertas(cuenta, nuevoGasto)
    
    loop Para cada Alerta activa
        Service->>Strategy: verificarLimite(alerta, cuenta, nuevoGasto)
        activate Strategy
        Strategy-->>Service: true (Supera) / false (Ok)
        deactivate Strategy
    end

    alt ⚠️ Límite Superado
        Service->>Model: add(Notificacion)
        Note right of Model: Se registra la alerta en el historial
        Service-->>DetalleCtrl: Retorna mensaje de advertencia
    else ✅ Límite OK
        Service-->>DetalleCtrl: Retorna null (Sin advertencias)
    end

    Note over DetalleCtrl: 2. PERSISTENCIA DEL GASTO (Siempre se ejecuta)

    DetalleCtrl->>Model: add(nuevoGasto)
    activate Model
    Model-->>DetalleCtrl: Lista de gastos actualizada en RAM
    deactivate Model

    DetalleCtrl->>Service: agregarCuenta(..., cuenta)
    activate Service
    Service->>Repo: save(cuenta)
    activate Repo
    Note right of Repo: Se guarda TODO (Gasto + Notificación si hubo)
    Repo-->>Service: JSON actualizado
    deactivate Repo
    Service-->>DetalleCtrl: Confirmación
    deactivate Service

    Note over DetalleCtrl: 3. FEEDBACK AL USUARIO

    DetalleCtrl->>DetalleCtrl: actualizarTabla()
    
    opt Si hubo Advertencia de Alerta
        DetalleCtrl-->>Usuario: Popup: "Gasto guardado, pero límite superado"
    end
    
    DetalleCtrl-->>Usuario: Muestra el nuevo gasto en la tabla