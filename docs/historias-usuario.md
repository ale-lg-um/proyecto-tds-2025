# 📋 Especificación de Historias de Usuario

Este documento detalla las Historias de Usuario (HU) definidas para el proyecto **Gestor de Gastos**, incluyendo sus descripciones y criterios de aceptación.

---

## 1. Gestión Básica de Gastos

### HU-01: Registrar gasto
**Como** usuario,  
**quiero** registrar un gasto con cantidad, fecha y categoría,  
**para** llevar el control de mis finanzas personales.

* **Criterios de Aceptación:**
    * Dado que estoy en la ventana de registro de gastos,
    * Cuando introduzco los datos y pulso “guardar”,
    * Entonces el gasto se añade a la lista y se actualiza el total.

### HU-02: Crear categoría
**Como** usuario,  
**quiero** crear nuevas categorías de gasto,  
**para** organizar mis gastos según mis necesidades.

* **Criterios de Aceptación:**
    * Dado que estoy en la sección de categorías,
    * Cuando introduzco el nombre de una nueva categoría y confirmo,
    * Entonces la categoría aparece disponible para asignarla a futuros gastos.

### HU-03: Editar gasto
**Como** usuario,  
**quiero** editar un gasto registrado,  
**para** corregir errores o actualizar su información.

* **Criterios de Aceptación:**
    * Dado que estoy visualizando un gasto,
    * Cuando pulso “editar” y modifico los datos,
    * Entonces el gasto se actualiza correctamente en la lista.

### HU-04: Borrar gasto
**Como** usuario,  
**quiero** borrar un gasto registrado,  
**para** eliminar entradas incorrectas o innecesarias.

* **Criterios de Aceptación:**
    * Dado que estoy visualizando un gasto,
    * Cuando pulso “borrar” y confirmo la acción,
    * Entonces el gasto desaparece de la lista y el total se actualiza.

### HU-05: Persistencia de datos
**Como** usuario,  
**quiero** que todos mis datos se guarden automáticamente,  
**para** no perder información al cerrar la aplicación.

* **Criterios de Aceptación:**
    * Dado que he registrado o editado un gasto,
    * Cuando cierro y vuelvo a abrir la aplicación,
    * Entonces los datos siguen disponibles y actualizados.

---

## 2. Interfaz y Accesibilidad

### HU-06: Interfaz gráfica
**Como** usuario,  
**quiero** gestionar mis gastos desde una interfaz visual,  
**para** interactuar de forma cómoda y clara.

* **Criterios de Aceptación:**
    * Dado que abro la aplicación,
    * Cuando accedo a la interfaz gráfica,
    * Entonces puedo registrar, editar y borrar gastos desde botones y formularios.

### HU-07: Línea de comandos
**Como** usuario avanzado,  
**quiero** poder registrar, editar y borrar gastos desde la línea de comandos,  
**para** tener una alternativa rápida y flexible.

* **Criterios de Aceptación:**
    * Dado que ejecuto la aplicación en modo consola,
    * Cuando introduzco comandos válidos,
    * Entonces el sistema realiza las acciones correspondientes y muestra confirmación.

---

## 3. Visualización y Análisis

### HU-08: Visualizar gastos en tabla/lista
**Como** usuario,  
**quiero** visualizar mis gastos en formato de tabla o lista,  
**para** revisar fácilmente los detalles de cada gasto.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de visualización,
    * Cuando selecciono la vista en tabla o lista,
    * Entonces se muestran los gastos con sus datos organizados por filas.

### HU-09: Visualizar gastos en gráficos
**Como** usuario,  
**quiero** ver mis gastos representados en gráficos de barras y circulares,  
**para** comprender mejor la distribución por categorías.

* **Criterios de Aceptación:**
    * Dado que tengo gastos registrados,
    * Cuando accedo a la vista gráfica,
    * Entonces se muestran los gráficos con los datos agrupados por categoría.

### HU-10: Visualizar gastos en calendario
**Como** usuario,  
**quiero** visualizar mis gastos en un calendario con vista diaria,  
**para** entender cuándo realizo más gastos.

* **Criterios de Aceptación:**
    * Dado que accedo a la vista de calendario,
    * Cuando selecciono una fecha,
    * Entonces se muestran los gastos registrados ese día en la vista Full Day.

---

## 4. Filtrado de Datos

### HU-11: Filtrar gastos por mes
**Como** usuario,  
**quiero** filtrar mis gastos por mes,  
**para** analizar cuánto gasto en cada periodo mensual.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de filtros,
    * Cuando selecciono un mes concreto,
    * Entonces se muestran únicamente los gastos registrados en ese mes.

### HU-12: Filtrar gastos por intervalo de fechas
**Como** usuario,  
**quiero** filtrar mis gastos por un rango de fechas personalizado,  
**para** revisar mis gastos en un periodo específico.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de filtros,
    * Cuando defino una fecha de inicio y una de fin,
    * Entonces se muestran los gastos comprendidos en ese intervalo.

### HU-13: Filtrar gastos por categoría
**Como** usuario,  
**quiero** filtrar mis gastos por categoría,  
**para** entender en qué tipo de cosas gasto más dinero.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de filtros,
    * Cuando selecciono una categoría,
    * Entonces se muestran únicamente los gastos asociados a esa categoría.

### HU-14: Filtrar gastos combinando criterios
**Como** usuario,  
**quiero** combinar filtros de fecha y categoría,  
**para** obtener una vista más específica de mis gastos.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de filtros,
    * Cuando selecciono una categoría y un intervalo de fechas,
    * Entonces se muestran los gastos que cumplen ambas condiciones.

---

## 5. Sistema de Alertas

### HU-15: Configurar alerta de gasto
**Como** usuario,  
**quiero** configurar alertas de gasto por semana, mes o categoría,  
**para** recibir avisos cuando supere mis límites definidos.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de alertas,
    * Cuando defino un límite de gasto y selecciono el periodo y categoría (opcional),
    * Entonces el sistema guarda la alerta y la activa para futuras comprobaciones.

### HU-16: Generar notificación al superar alerta
**Como** sistema,  
**quiero** generar una notificación cuando se supere el límite de una alerta,  
**para** informar al usuario de que ha excedido su presupuesto.

* **Criterios de Aceptación:**
    * Dado que el usuario ha registrado un gasto,
    * Cuando el total acumulado supera el límite definido en una alerta,
    * Entonces se genera una notificación visible para el usuario.

### HU-17: Consultar historial de alertas
**Como** usuario,  
**quiero** consultar el historial de notificaciones de alerta,  
**para** revisar cuándo y por qué se activaron.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de historial de alertas,
    * Cuando selecciono una fecha o categoría,
    * Entonces se muestran las notificaciones generadas en ese contexto.

---

## 6. Cuentas Compartidas

### HU-18: Crear cuenta de gasto compartida
**Como** usuario,  
**quiero** crear una cuenta de gasto compartida con varias personas,  
**para** registrar gastos grupales y repartirlos entre todos.

* **Criterios de Aceptación:**
    * Dado que accedo a la sección de cuentas compartidas,
    * Cuando introduzco los nombres de los participantes y confirmo,
    * Entonces se crea una cuenta con esos miembros y queda lista para registrar gastos.

### HU-19: Registrar gasto en cuenta compartida
**Como** usuario,  
**quiero** registrar un gasto dentro de una cuenta compartida indicando quién lo ha pagado,  
**para** calcular automáticamente cuánto debe cada persona.

* **Criterios de Aceptación:**
    * Dado que accedo a una cuenta compartida,
    * Cuando registro un gasto indicando el pagador y el importe,
    * Entonces el sistema actualiza los saldos de todos los miembros proporcionalmente.

### HU-20: Calcular saldos individuales
**Como** sistema,  
**quiero** calcular automáticamente el saldo de cada persona en una cuenta compartida,  
**para** reflejar quién debe dinero y quién ha pagado de más.

* **Criterios de Aceptación:**
    * Dado que se registra un nuevo gasto,
    * Cuando el sistema actualiza los saldos,
    * Entonces cada persona tiene un saldo que refleja su deuda o crédito en el grupo.

### HU-21: Consultar saldos en cuenta compartida
**Como** usuario,  
**quiero** consultar el saldo actual de cada persona en una cuenta compartida,  
**para** saber cuánto debe o le deben.

* **Criterios de Aceptación:**
    * Dado que accedo a una cuenta compartida,
    * Cuando selecciono la opción de ver saldos,
    * Entonces se muestran los saldos individuales de todos los miembros.

### HU-22: Definir porcentajes personalizados
**Como** usuario,  
**quiero** definir qué porcentaje del gasto asume cada persona al crear una cuenta compartida,  
**para** reflejar acuerdos personalizados de reparto.

* **Criterios de Aceptación:**
    * Dado que estoy creando una cuenta compartida,
    * Cuando asigno porcentajes individuales a cada miembro,
    * Entonces el sistema valida que la suma sea 100% y guarda la configuración.

### HU-23: Bloqueo de edición tras creación
**Como** usuario,  
**quiero** que la cuenta compartida no se pueda modificar una vez creada,  
**para** mantener la coherencia en el reparto de gastos.

* **Criterios de Aceptación:**
    * Dado que la cuenta ya ha sido creada,
    * Cuando intento cambiar los miembros o sus porcentajes,
    * Entonces el sistema bloquea la edición y muestra un mensaje explicativo.

### HU-24: Validación de suma de porcentajes
**Como** sistema,  
**quiero** validar que la suma de los porcentajes asignados sea exactamente 100%,  
**para** evitar errores en el cálculo de saldos.

* **Criterios de Aceptación:**
    * Dado que el usuario está asignando porcentajes,
    * Cuando la suma no es 100%,
    * Entonces el sistema impide continuar y muestra un aviso de corrección.

---

## 7. Importación de Datos

### HU-25: Importar gastos desde fichero
**Como** usuario,  
**quiero** importar gastos desde un fichero de texto plano,  
**para** añadir rápidamente datos desde otras plataformas como la bancaria.

* **Criterios de Aceptación:**
    * Dado que accedo a la opción de importación,
    * Cuando selecciono un fichero válido y lo confirmo,
    * Entonces el sistema añade los gastos contenidos en el fichero a mi lista de gastos.

### HU-26: Soporte para múltiples formatos
**Como** usuario,  
**quiero** que el sistema soporte distintos formatos de fichero,  
**para** poder importar datos desde diversas fuentes externas.

* **Criterios de Aceptación:**
    * Dado que intento importar un fichero,
    * Cuando el formato es diferente al ejemplo bancario,
    * Entonces el sistema lo interpreta correctamente si cumple con una estructura reconocida.

### HU-27: Procesamiento automático del fichero
**Como** sistema,  
**quiero** procesar automáticamente los datos del fichero importado,  
**para** convertirlos en registros válidos de gasto sin intervención manual.

* **Criterios de Aceptación:**
    * Dado que el usuario ha importado un fichero,
    * Cuando el sistema lo analiza,
    * Entonces cada línea válida se convierte en un gasto registrado con sus datos correspondientes.