# 🏢 Chat Corporativo - ChatDAM_MiguelAngel-Alberto

<div align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white" />
  <img src="https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white" />
</div>

---

## 📖 Descripción del Proyecto
Nuestra empresa cliente ha crecido rápidamente y ahora cuenta con empleados distribuidos en distintas oficinas y trabajando en remoto. La comunicación por correo electrónico ya no es suficiente para coordinar tareas en tiempo real. 

Este proyecto consiste en el diseño e implementación de una aplicación de chat corporativo de escritorio, robusta, segura y conectada a servicios en la nube, adaptada a las necesidades empresariales.

---

## 🚀 Escenario de Uso Real

### 🔐 Sistema de Autenticación
Cuando un empleado abre la aplicación, se muestra una ventana de inicio de sesión:
1. Introduce su **Nombre de usuario y Contraseña**.
2. La información se envía **cifrada** a una API REST desarrollada con Spring Boot.
3. El sistema valida las credenciales contra una base de datos alojada en la nube (ej. AWS).
4. **Login incorrecto:** Se muestra una ventana de error con un botón `Salir` para cerrar la aplicación.
5. **Login correcto:** El usuario accede a la ventana principal del chat.

### 💬 Comunicación en Tiempo Real
Una vez en la ventana principal del chat:
* **Visualización:** El empleado ve los últimos **10 mensajes** solicitados a la API REST. Cada mensaje incluye: Contenido, Nombre del autor, Fecha y Hora.
* **Envío:** El usuario dispone de componentes para escribir y un botón `Enviar`.
* **Sincronización:** Al enviar, el mensaje se guarda en la base de datos vía API y se envía automáticamente al resto de usuarios conectados (comunicación cliente-servidor en tiempo real).
* **Cierre:** Barra de menú con opción `Salir` para cerrar correctamente.

---

## 🛠️ Requisitos Técnicos y Arquitectura

> **Stack Tecnológico:** Tecnologías profesionales del ecosistema Java.

* **Gestión del Proyecto:** Estructura clara y profesional utilizando **Maven o Gradle**.
* **Interfaz Gráfica:** Desarrollada íntegramente con **JavaFX**.
* **Control de Versiones:** Uso de Git y GitHub mediante commits frecuentes y significativos.
* **Persistencia:** Base de datos en la nube gestionada a través de API REST.

---

## 👥 Sistema de Roles (Funcionalidad Opcional)

El sistema incluye control de permisos basado en dos roles de usuario:

### 🛡️ Rol EMPLEADO
* ✔️ Iniciar sesión.
* ✔️ Ver mensajes.
* ✔️ Enviar mensajes.
* ✔️ Salir de la aplicación.

### 👑 Rol ADMINISTRADOR
* ✔️ Todos los permisos del rol EMPLEADO.
* ➕ **Gestión de empleados:** Nueva opción en el menú que abre una ventana para dar de alta nuevos usuarios. Envía nombre de usuario y contraseña cifrados a la API REST para su almacenamiento.

---

## 📈 Criterios de Evaluación
El desarrollo de este proyecto contempla la evaluación de los siguientes puntos:
1. Correcto uso de Git.
2. Organización del código y Arquitectura del proyecto.
3. Seguridad (cifrado) y Persistencia de datos.
4. Comunicación cliente-servidor y Funcionamiento real.
5. Calidad de la Interfaz gráfica.

---
*Proyecto académico desarrollado para el módulo de Programación - Unidad 7.*
