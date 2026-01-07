# 💎 Nuestro Modelo de Dominio

En este documento describimos cómo hemos organizado la "lógica" de nuestro **Gestor de Gastos**. Nos hemos basado en los principios de **Desarrollo Dirigido por el Dominio (DDD)** para que las entidades y las reglas de negocio tengan sentido y sean fáciles de mantener.

---

## 1. Entidades y Agregados: El orden del sistema

Siguiendo lo que aprendimos en el diseño de objetos, hemos organizado el dominio en piezas con identidad propia (**Entidades**) y grupos que funcionan como uno solo (**Agregados**).

### 1.1. El Agregado "Cuenta" (Nuestra Raíz)
La clase abstracta `Cuenta` es la que manda aquí. Es lo que en los apuntes llaman el **Objeto Raíz** (Root Aggregate). Es la encargada de que todo lo que hay dentro de una cuenta esté en orden:
* **Gasto**: Cada ticket o compra que metéis es una entidad con su propio ID único para que no se confundan.
* **Categoría**: Las etiquetas para vuestros gastos. Como pusimos en el manual, cada una tiene su color para que los "quesitos" se vean claros.
* **Alerta**: Las reglas que nos avisan si nos estamos pasando de la raya con el presupuesto.
* **Notificación**: El historial que os sale cuando saltan los límites de gasto.

---

## 2. Definición de los Objetos: ¿Qué es cada cosa?

Clasificamos los objetos según su función en el dominio:

| Elemento | ¿Qué tipo es? | Su función |
| :--- | :--- | :--- |
| **Cuenta** | Entidad / Raíz | Es el eje central. Gestiona los gastos y las alertas de cada usuario. |
| **Gasto** | Entidad | Tiene identidad propia (un ID). Guarda cuánto, cuándo y quién pagó. |
| **GastoTemporal** | DTO (Value Object) | Es un objeto de paso. Lo usamos solo para la "Importación Inteligente" antes de crear el gasto real. |
| **Categoría** | Entidad | Define el nombre y el color hex de los gastos para los gráficos. |
| **Usuario** | Entidad | Aunque el login sea "meramente estético" ahora mismo, representa al dueño de la sesión. |

---

## 3. Las Reglas del Negocio: El "cerebro" del proyecto

Nuestro modelo no solo guarda datos, sino que sabe cómo comportarse gracias al polimorfismo que tanto nos recalcaron en clase.

### 3.1. Tipos de Cuentas y Reparto
Nos hemos roto un poco la cabeza para que el sistema soporte distintas formas de pagar:
* **CuentaPersonal**: Para llevar tus cuentas tú solo, sin líos con nadie más.
* **CuentaCompartida**: La típica para viajes donde todo se divide a partes iguales entre los miembros.
* **CuentaProporcional**: Esta es la avanzada. Cada uno tiene un porcentaje asignado y el sistema valida que la suma sea el 100% para que el reparto sea justo.

### 3.2. Validación con Estrategias
Para las alertas, usamos el patrón **Strategy**. Así el sistema sabe si tiene que mirar el gasto de la semana o del mes sin llenar el código de condiciones raras.

---

## 4. Guardado y Ciclo de Vida

Necesitábamos que el manejo de los objetos fuera siempre igual para no rompernos el código unos a otros:

* **Factorías (`FactoriaImportacion`)**: Las usamos para crear los objetos de importación complejos sin que el resto del programa sepa cómo se lee un CSV o un Excel.
* **Repositorios (`CuentaRepository`)**: Es como nuestro catálogo global. Aquí es donde vamos a buscar o guardar las cuentas en el archivo JSON.
* **Servicios**: Clases como `ServicioAlertas` o `CuentaService` que hacen el trabajo sucio que no encaja solo en una Cuenta o un Gasto.

**En definitiva:** Hemos intentado que cada objeto tenga su sitio y su ID. Así, aunque dos gastos parezcan iguales (mismo importe y fecha), el sistema sabe que son cosas distintas porque cada uno tiene su identidad única.