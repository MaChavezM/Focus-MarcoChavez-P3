### 1. Lógica de Negocio y Control de Sesiones
La `MainActivity` gestiona el flujo de productividad basado en el método Pomodoro, implementando las siguientes reglas:
* **Ciclo de Sesiones Dinámico:** Configuración de intervalos de 25 min (Enfoque) y 5 min (Descanso). Tras completar 4 sesiones de enfoque, el sistema activa automáticamente un descanso largo de 15 min.
* **Gestión de Estados:**
    * **Reset:** Detiene el temporizador activo y restablece el contador al tiempo inicial del modo actual.
    * **Skip:** Permite al usuario saltar al siguiente estado del ciclo (Enfoque -> Descanso o viceversa) ignorando el tiempo restante.
* **Optimización de Pantalla:** Implementación de modo inmersivo y configuración para mantener la pantalla encendida durante el conteo, evitando interrupciones en la visibilidad del temporizador.

### 2. Interfaz y Experiencia de Usuario (UI/UX)
El diseño se basa en **Material Components** y las mejores prácticas de diseño de Android:
* **Jerarquía Visual:** Uso de `ConstraintLayout` para una disposición eficiente de `ImageViews`, `TextViews` y `MaterialButtons`.
* **Feedback de Estado:** El `Chip` del modo activo se resalta visualmente mediante un borde (*stroke*), facilitando la identificación del estado actual del ciclo mediante `ChipGroups`.
* **Recursos y Activos:**
    * Soporte multilingüe completo mediante archivos `values-es/strings.xml`.
    * Identidad visual gestionada a través de `Image Assets` para el logo de la aplicación.
* **Herramientas de Diseño:** Uso de atributos `tools` en el XML para previsualizar estados sin afectar el tiempo de ejecución.

### 3. Arquitectura de Datos y Persistencia (MVC)
La aplicación utiliza el patrón **Modelo-Vista-Controlador** para desacoplar la interfaz de la lógica de almacenamiento:

* **Modelo:**
    * **Entidad `Session`:** Clase POJO que encapsula los datos de cada intervalo (tipo, fecha, hora, duración y estado de completado).
    * **SessionManager**: Clase encargada de administrar una lista dinámica en memoria RAM (ArrayList<Session>). Actúa como el controlador de datos de la aplicación, permitiendo agregar nuevas sesiones y recuperar el historial para su visualización inmediata. Nota: En esta rama, los datos son volátiles y se reinician al finalizar el proceso de la aplicación, sirviendo como base estructural para la futura implementación de persistencia.
* **Controlador (`MainActivity`):** Vincula el código lógico con la interfaz, enviando los datos de cada sesión finalizada al `SessionManager` para su almacenamiento permanente.
* **Vista del Historial:**
    * **RecyclerView:** Implementación eficiente para mostrar el historial de sesiones.
    * **`history_session_entry.xml`:** Definición personalizada para cada entrada de la lista.
    * **Gestión de "Estado Vacío":** Interfaz dinámica que muestra una vista alternativa cuando el historial no contiene registros.

### 4. Resumen de Componentes Técnicos
* **Almacenamiento:** Mientras que en la rama `master` el historial de sesiones es gestionado de forma local y volátil en memoria RAM, la rama [`sqlite-persistence`](https://github.com/monmm/FocusLab/blob/feature/sqlite-persistence/README.md) escala el proyecto integrando una capa de persistencia definitiva mediante **SQLite nativo**, garantizando la integridad del historial de productividad del usuario a largo plazo.
* **Arquitectura:** MVC con separación clara de responsabilidades.
* **Configuración:** `SharedPreferences` para persistir preferencias de usuario (Tema e Idioma).

Esta estructura garantiza que **FocusLab** sea una aplicación profesional, escalable y capaz de mantener la integridad de los datos de productividad del usuario incluso tras el cierre de la aplicación.
