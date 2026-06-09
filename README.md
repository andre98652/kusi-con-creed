# Kusi-CRED 🌱

**Kusi-CRED** (Control de Crecimiento y Desarrollo) es una aplicación móvil Android diseñada para ayudar a los padres y cuidadores a monitorear la salud, el crecimiento y el desarrollo psicomotor de sus bebés durante sus primeros años de vida. 

El sistema digitaliza por completo la clásica "Tarjeta de Control" de los hospitales, brindando herramientas automáticas y calculadoras basadas en estándares médicos oficiales.

---

## 🚀 Características Principales

### 1. Curvas de Crecimiento (Estándares OMS)
- Registro histórico de peso y talla del bebé.
- **Motor de Inferencia:** Calcula automáticamente el percentil de crecimiento basándose en las Tablas Oficiales de la Organización Mundial de la Salud (OMS).
- Gráficas interactivas para visualizar la curva de crecimiento del niño a lo largo del tiempo.

### 2. Calendario de Vacunación (Esquema MINSA)
- Catálogo precargado con el Esquema Nacional de Vacunación del Ministerio de Salud.
- **Cálculo Automático:** Genera las fechas exactas en las que el bebé debe recibir sus dosis dependiendo de su fecha de nacimiento.
- Control interactivo de dosis "Pendientes" y "Aplicadas".

### 3. Hitos del Desarrollo Psicomotor (TPED)
- Evaluación interactiva del desarrollo estructurada por rango de edad (1 a 24 meses).
- Áreas evaluadas: Motora, Lenguaje, Social y Cognitiva.
- Guías de estimulación temprana sugeridas automáticamente si el bebé presenta un retraso en algún hito específico.

### 4. Suplementación y Control de Anemia
- Registro de tamizajes de hemoglobina.
- Control y seguimiento de entregas de gotas o jarabes de Hierro (Sulfato Ferroso).

### 5. Exportación Premium (Reporte Clínico PDF)
- Generación local de un reporte clínico profesional en formato A4 (PDF) que consolida toda la información de crecimiento, vacunas e hitos, ideal para compartir con el pediatra.

---

## 🛠️ Tecnologías y Arquitectura

La aplicación está construida siguiendo los estándares modernos de desarrollo Android, utilizando una arquitectura limpia (**Clean Architecture**) y el patrón de diseño **MVVM** (Model-View-ViewModel).

- **Lenguaje:** Kotlin
- **Interfaz de Usuario:** Jetpack Compose + Material Design 3
- **Inyección de Dependencias:** Dagger Hilt
- **Base de Datos:** Room Database (Offline-First) + DataStore (Preferencias y Sesión)
- **Gráficos:** Vico (Compose Charts)
- **Generación de PDF:** iTextG

---

## 📱 Funcionamiento Offline-First

Kusi-CRED ha sido diseñada pensando en la accesibilidad. Toda la base de datos de estándares de la OMS y el esquema de vacunación residen dentro del dispositivo del usuario. 
La aplicación **funciona al 100% sin conexión a internet**, asegurando que los padres puedan revisar o actualizar los datos médicos de su bebé sin importar en dónde se encuentren. El sistema de inicio de sesión actual guarda los datos localmente de forma segura.

---

## ⚙️ Cómo ejecutar el proyecto

1. Clona este repositorio en tu computadora.
2. Abre el proyecto utilizando **Android Studio** (Koala o superior).
3. Espera a que Gradle sincronice las dependencias.
4. Conecta un dispositivo Android físico o inicia un Emulador.
5. Presiona el botón de **Run (Play)** en la parte superior.

---
*Desarrollado con ♥ para el bienestar y la salud infantil.*
