# 🏗️ Arquitectura de Nuestro Sistema

En este documento explicamos cómo hemos montado las "tripas" del **Gestor de Gastos**. Hemos diseñado una estructura que no solo funciona, sino que sigue los principios de **Separación Modelo-Vista** y **Diseño Dirigido por el Dominio (DDD)** que hemos visto en clase.

---

## 1. El Esquema de Trabajo (MVC)

Para que el proyecto no fuera un caos de código, decidimos usar una **Arquitectura Multicapa** basada en el patrón **MVC (Modelo-Vista-Controlador)**. 

La idea es sencilla: separar los datos de lo que el usuario ve.
* **Modelo**: Es el corazón del programa. Aquí están las reglas de las cuentas y los gastos.
* **Vista**: Lo que usáis para interactuar, ya sea la interfaz de JavaFX con sus "quesitos" o la terminal para los que prefieren tirar comandos.
* **Controlador**: Hace de "jefe de tráfico" (Controlador GRASP), moviendo la info entre la pantalla y los servicios de negocio.

---

## 2. Organización por Capas

Hemos repartido el código en paquetes para que cada cosa tenga su sitio (lo que en TDS llaman alta cohesión):

| Capa | ¿Qué hace? | Ubicación en el Código |
| :--- | :--- | :--- |
| **Presentación** | Es la cara visible: JavaFX y el hilo de la terminal (CLI). | `app_gastos` / `cli` |
| **Control** | Gestiona los clics y lo que escribís en los formularios. | `controller` |
| **Importación** | La magia de la "Importación Inteligente" que lee CSV o Excel. | `importacion` |
| **Negocio** | Donde reside la lógica real: sesiones, alertas y cálculos. | `services` |
| **Persistencia** | Se encarga de que nada se pierda al cerrar, guardándolo todo en el JSON. | `repository` |
| **Modelo** | El núcleo: define qué es una Cuenta o un Gasto. | `model` |

---

## 3. Patrones de Diseño (Los "clásicos" de GoF)

Necesitábamos soluciones que ya funcionaran. Hemos aplicado varios patrones de la famosa "Banda de los Cuatro" (Erich Gamma y compañía):

* **Singleton**: Lo usamos en `SesionService` y `CuentaService` para que solo haya una sesión y una cuenta activa a la vez.
* **Método Factoría**: En la capa de importación, la `FactoriaImportacion` elige el adaptador adecuado según el archivo que nos paséis.
* **Adaptador (Adapter)**: Fundamental para que formatos externos raros se conviertan en gastos que nuestra App entienda.
* **Fachada (Facade)**: El repositorio nos ofrece una cara amable para no tener que pelearnos con el sistema de archivos cada vez que guardamos algo.
* **Estrategia (Strategy)**: Para que el sistema de alertas sepa si tiene que calcular por semanas o por meses de forma intercambiable.

---

## 4. Jerarquías y el "Poder" del Polimorfismo

Una de las partes que más nos costó diseñar fue el reparto de gastos. Gracias al **polimorfismo**, el sistema trata todas las cuentas igual, pero cada una se comporta de forma distinta bajo el capó:
* Tenemos la **CuentaPersonal**, la **Compartida** (a partes iguales) y la **Proporcional** (donde cada uno paga su porcentaje).
* Lo mismo pasa con los **Importadores**: todos siguen la misma interfaz aunque lean formatos distintos.

---

## 5. El ciclo de vida y el guardado

Siguiendo las pautas de **DDD**, nos hemos asegurado de que los datos sean consistentes:
* **Agregados**: La clase `Cuenta` es la "raíz". Ella manda sobre sus gastos, categorías y alertas.
* **Persistencia JSON**: Usamos este formato porque es ligero y flexible para guardar vuestra info financiera.
* **DTOs**: Usamos el `GastoTemporal` para mover los datos desde la importación hasta el dominio sin ensuciar las entidades reales.

**En resumen:** Si tocáis algo en la App o importáis un fichero, el sistema lo valida en la capa de negocio y el repositorio lo sincroniza al momento con el archivo `cuentas.json` para que no perdáis ni un céntimo.