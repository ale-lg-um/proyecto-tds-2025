# 💰 Gestor de Gastos - Proyecto Final

Este repositorio contiene el código fuente y la documentación del proyecto "Gestor de Gastos", una aplicación de escritorio desarrollada en Java para la gestión de finanzas personales y compartidas.

## 👥 Integrantes del Grupo

| Nombre Completo | Correo Electrónico | Subgrupo |
| :--- | :--- | :---: |
| Mario Franco Alcázar | m.francoalcazar@um.es | [1.2] |
| Alejandro López Galián  | alejandro.l.g2@um.es<br>galatasari1970@gmail.com | [1.2] |
| Jaime Martínez Ríos | j.martinezrios@um.es | [1.2] |

---

## 📝 Descripción del Proyecto

La aplicación permite a los usuarios gestionar su economía diaria de forma eficiente mediante una interfaz gráfica moderna construida con **JavaFX**. El sistema sigue una arquitectura **MVC (Modelo-Vista-Controlador)** y utiliza el patrón **Repositorio** para la persistencia de datos.

### Funcionalidades Principales:
* **Gestión de Cuentas:** Soporte para cuentas Personales, Compartidas y Especiales (Proporcionales).
* **Control de Gastos:** Creación, edición y eliminación de gastos con categorización y fechas.
* **Categorías Personalizadas:** Gestión de categorías con colores identificativos para una mejor visualización.
* **Cálculo de Deudas (Saldos):** Algoritmo automático para calcular quién debe a quién en cuentas compartidas.
* **Persistencia de Datos:** Almacenamiento local mediante archivos JSON (librería Jackson).

---

## 🚀 Cómo Ejecutar el Proyecto

### Requisitos Previos
* **Java JDK:** Versión 17 o superior (Recomendado Java 21).
* **Maven:** Para la gestión de dependencias.
* **IDE:** Eclipse, IntelliJ IDEA o NetBeans (con soporte para JavaFX).

### Opción 1: Ejecución desde Eclipse (Recomendada)
1.  Importar el proyecto como **"Existing Maven Project"**.
2.  Hacer clic derecho sobre el proyecto > **Maven** > **Update Project** (para descargar dependencias).
3.  Buscar la clase principal: `gestorgastos.app_gastos.App` (o `Main`).
4.  Hacer clic derecho > **Run As** > **Java Application**.

### Opción 2: Ejecución mediante Terminal (Maven)
Abre una terminal en la raíz del proyecto y ejecuta:

```bash
mvn clean javafx:run
```
---

## Enlace a la Documentacion
**Estes un enlace al indice de la documentacion ->**[Indice de la Documentacion](./docs/DOCUMENTACION.md)**
