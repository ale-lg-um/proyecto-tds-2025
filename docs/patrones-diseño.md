# 🛠️ Patrones de Diseño (GoF) aplicados al Gestor

Para que el código no fuera un lío y siguiendo lo que nos han explicado en clase sobre la "Banda de los Cuatro" (Erich Gamma y el resto del GoF), hemos metido varios patrones que nos ayudan a que el Gestor de Gastos sea fácil de ampliar y no se rompa todo al tocar una clase.

---

## 1. Patrones de Creación: ¿Cómo nacen nuestros objetos?

### 1.1. Singleton (Instancia única)
Lo hemos usado para que cosas críticas no se dupliquen por ahí.
* **`SesionService`**: Como decimos en el manual, el login es "meramente estético", pero por dentro necesitamos que solo haya un usuario activo a la vez.
* **`CuentaService`**: Así nos aseguramos de que todos los controladores toquen la misma cuenta y no se pierdan los gastos por el camino.
* **Ventaja:** Evitamos variables globales sucias y controlamos el acceso desde cualquier parte de la App.

### 1.2. Factory Method (Factoría de Importación)
En la parte de **Importación Inteligente**, no sabíamos qué tipo de archivo iba a subir el usuario.
* **`FactoriaImportacion`**: Dependiendo de si es un `.csv`, `.json` o un Excel, esta clase decide qué objeto crear.
* **Uso real:** Cuando eliges un fichero en la pantalla de importación, la factoría nos da el importador correcto sin que el controlador sepa cómo funciona cada formato.

---

## 2. Patrones Estructurales: Conectando las piezas

### 2.1. Adaptador (Adapter / Wrapper)
Este es clave para que los datos bancarios externos se entiendan con nuestro sistema.
* **Los Adaptadores (`AdaptadorCSV`, `AdaptadorExcel`, etc.)**: Transforman las líneas raras de un fichero en objetos `GastoTemporal` que nuestra App sí sabe leer.
* **Propósito:** Reutilizar lógica de lectura de archivos aunque tengan interfaces que no encajan con nuestro modelo de `Gasto`.

### 2.2. Fachada (Facade)
Para que el resto del programa no tenga que pegarse con la librería Jackson o con cómo se escriben los archivos JSON.
* **`CuentaRepositoryJson`**: Funciona como una fachada que simplifica todo el lío de la persistencia. Si mañana cambiamos el JSON por una base de datos real, solo tendríamos que tocar aquí.

---

## 3. Patrones de Comportamiento: El cerebro del Gestor

### 3.1. Estrategia (Strategy)
Lo usamos para el **Sistema de Alertas**. Queríamos que el usuario pudiera elegir si el límite de gasto es semanal o mensual sin llenar el código de `if` o `switch`.
* **`InterfaceAlerta`**: Define el contrato para la validación.
* **`EstrategiaSemanal` y `EstrategiaMensual`**: Son los algoritmos que cambian según el tiempo.
* **En la App:** Cuando el sistema saca un log de "Descartado por ALERTA", es porque la estrategia correspondiente ha hecho el cálculo.

---

## 📊 Resumen de organización técnica

| Patrón | ¿Dónde buscarlo? | ¿Para qué nos sirve en el proyecto? |
| :--- | :--- | :--- |
| **Singleton** | `services` | Para que la sesión y la cuenta activa sean únicas. |
| **Factoría** | `importacion` | Para crear importadores sin conocer la extensión del archivo. |
| **Adaptador** | `importacion` | Para que los datos de fuera se conviertan en gastos. |
| **Fachada** | `repository` | Para ocultar el lío del guardado en JSON. |
| **Estrategia** | `strategies` | Para cambiar entre alertas semanales y mensuales. |

Con esto conseguimos una **alta cohesión** y un **bajo acoplamiento**, que es lo que pide el diseño dirigido por el dominio (DDD).