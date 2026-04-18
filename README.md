# FocusLab - Aplicación Pomodoro para Android

**Nombre del estudiante:** Chavez Martínez Marco Antonio
**Número de cuenta:** 320328594


---

## Descripción General del Proyecto

### ¿Qué es FocusBuddy?

FocusBuddy es una aplicación móvil para Android que ayuda a los estudiantes a mejorar su productividad usando la **técnica Pomodoro**. La idea es simple: trabajar en sesiones enfocadas de 25 minutos, descansar 5 minutos, y después de 4 ciclos, tomar un descanso largo de 15 minutos.

### Objetivos logrados 

La aplicación cumple con todos los requisitos del proyecto:

1. **Interfaz moderna y profesional**
   - Diseño limpio con Material Design
   - Botones, tarjetas e iconos atractivos
   - Pantalla principal clara y fácil de usar

2. **Sistema de cronómetro funcional**
   - Temporizador que cuenta hacia atrás (25:00 → 0:00)
   - Botones para Iniciar, Pausar, Reanudar, Reiniciar y Saltar
   - Feedback visual con colores que cambian según el tipo de sesión
   - Vibración del dispositivo al terminar cada sesión

3. **Guardado automático en la base de datos**
   - Cada sesión se guarda con: tipo, fecha, hora, duración y si fue completada
   - Los datos persisten incluso si cierras la app
   - Sistema de **SQLite** (base de datos local del dispositivo)

4. **Historial de sesiones**
   - Lista de todas las sesiones que has realizado
   - Filtros por período: "Hoy", "Esta semana", "Todo el historial"
   - Cada sesión muestra si fue completada o interrumpida
   - Contador dinámico de resultados

5. **Preferencias personalizables**
   - Página de configuración donde el usuario puede ajustar opciones
   - Uso de SharedPreferences para guardar las configuraciones
   - Separación clara entre ajustes de sesiones y ajustes de usuario

6. **Control de orientación**
   - La app solo funciona en modo vertical (Portrait)
   - Previene que la pantalla se rote accidentalmente
   - El cronómetro no se reinicia al rotar el dispositivo

---

##  Tareas Realizadas - Detalle Técnico (sin ser complicado)

### 1. **Pantalla Principal (MainActivity)**
- Creación del temporizador usando `CountDownTimer` (un componente de Android que cuenta hacia atrás)
- Gestión de 3 estados: Idle (parado), Running (contando) y Paused (pausado)
- Integración de vibración táctil cuando termina cada sesión
- Sistema de puntos visuales que aumentan con cada sesión completada
- Cambio automático entre Enfoque → Descanso → Descanso largo


### 2. **Base de Datos (DatabaseHelper)**
- Creación de tabla SQLite llamada "sessions" con columnas para: tipo, fecha, hora, duración, si fue completada
- Operaciones CRUD (Create, Read, Update, Delete):
  - **Create**: insertar nuevas sesiones
  - **Read**: leer todas las sesiones o filtrar por día/semana
  - **Delete**: no implementado (datos permanentes)



### 3. **Sesiones (Session + SessionManager)**
- Modelo `Session`: representa una sesión individual
- Controlador `SessionManager`: actúa como "puerta" entre la interfaz y la base de datos
- Las escrituras en BD se hacen en un **thread secundario** para no congelar la app



### 4. **Historial de Sesiones (SessionHistoryActivity)**
- Pantalla que muestra todas las sesiones guardadas
- Chips para filtrar: "Hoy", "Esta semana", "Todo"
- Lista con desplazamiento vertical (RecyclerView)
- Empty State: cuando no hay sesiones, muestra un mensaje amable



### 5. **Adaptador para la lista (SessionHistoryAdapter)**
- Convierte cada sesión en una tarjeta visual dentro de la lista
- Muestra: tipo, fecha, hora, duración y estado
- Colores: verde para "Completada", rojo para "Interrumpida"



### 6. **Preferencias (PreferencesActivity + PreferencesFragment)**
- Pantalla de configuración con opciones como tema y sonidos
- Uso de `SharedPreferences` (almacenamiento simple de configuraciones)
- Separación de preferencias de usuario vs. datos de sesiones



### 7. **Archivos de Recursos (XML)**
- `strings.xml`: todos los textos en español sin hardcoding
- `colors.xml`: colores reutilizables (primario, secundario, fondo, etc.)
- `dimens.xml`: tamaños estándar (márgenes, padding, texto)
- `activity_main.xml`: layout de la pantalla principal
- `activity_history_session.xml`: layout del historial
- Respeto total a los requisitos de no usar valores fijos



### 8. **AndroidManifest.xml**
- Declaración de permisos necesarios (vibración)
- Restricción de orientación a Portrait en todas las activities
- Exportación correcta de activities



##  Lo que MÁS TRABAJO COSTÓ

### 1. **Sincronización de datos entre threads**  (Lo más complicado)

**¿Cuál era el problema?**
- Las operaciones de base de datos son "lentas" (comparadas con operaciones en memoria)
- Si intentabas guardar datos en el hilo principal (UI), la pantalla se congelaba
- Pero si guardabas en un hilo secundario, debías asegurar que los datos no se perdieran

**¿Cómo se  resolvio**
```java
// En SessionManager, la escritura se hace en thread secundario
new Thread(() -> {
    dbHelper.insertSession(session);  // Esto no bloquea la UI
}).start();
```

**¿Por qué costó trabajo?**
-Entender la implementación y manejo de hilos
- Entender cuándo usar threads y cuándo no
- Asegurar que no haya "condiciones de carrera" (dos threads escribiendo al mismo tiempo)
- Manejar excepciones que podrían ocurrir en threads

---

### 2. **Gestión del estado del cronómetro** 

**¿Cuál era el problema?**
- El `CountDownTimer` es complicado de pausar y reanudar
- Si cerraba la app mientras contaba, se perdía el estado
- Había que manejar 3 estados diferentes (Idle, Running, Paused)

**¿Cómo se  resolvio**
```java
enum TimerState { IDLE, RUNNING, PAUSED }

// Cada acción cambia el estado
if (timerState == TimerState.RUNNING) pauseTimer();
else startTimer();
```


---

### 3. **Filtrado de sesiones por fecha** 

**¿Cuál era el problema?**
- Las fechas en Android se manejan de varias maneras diferentes
- Necesitabas filtrar por "Hoy", "Última semana", etc.
- Las fechas en SQLite deben estar en formato estándar (yyyy-MM-dd)

**¿Cómo se  resolvio?**
```java
// Calcular hace cuántos días empezó la semana
Calendar cal = Calendar.getInstance();
cal.add(Calendar.DAY_OF_YEAR, -6);  // 6 días atrás
String weekStart = FMT_SORT.format(cal.getTime());
```

**¿Por qué costó trabajo?**
- Entender cómo funciona `Calendar` en Android
- Las comparaciones de strings vs. objetos Date
- Los tests con fechas diferentes son complicados

---

### 4. **Persistencia de datos en SQLite** 

**¿Cuál era el problema?**
- SQLite es una base de datos real, no un simple archivo
- Los datos debían perseguir incluso si el sistema mataba la app

**¿Cómo lo resolvimos?**
```java
public class DatabaseHelper extends SQLiteOpenHelper {
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sessions (...)");
    }
}
```

**¿Por qué costó trabajo?**
- SQL es un lenguaje diferente (no es Java)
- Los tipos de datos son limitados (TEXT, INTEGER, etc.)
- La integracion del base con la aplicacion




---

##  Segundo vezQué haría diferente?

Si tuvieras que hacer una **versión 2.0** de FocusLab, aquí están las mejoras:

### 1. **Análisis y estadísticas** 
**Antes:** Solo ves una lista de sesiones  
**Ahora:** Gráficos que muestren:
- Cuántas horas de sesion tuviste en la semana
- Cuál es tu mejor hora del día para estudiar
- Tu tasa de sesiones completadas vs. interrumpidas
- Racha de días consecutivos estudiando

---

### 2. **Metas personalizables** 
**Antes:** Siempre 25 minutos de enfoque, 5 minutos de descanso  
**Ahora:** 
- Duración personalizable: "Quiero 20 minutos de enfoque"
- Metas semanales: "Mi meta es 10 sesiones completadas esta semana"
- Recordatorios: "Es hora de estudiar" (notificaciones)

---

### 3. **Sonidos y vibraciones personalizables** 
**Antes:** Solo vibración simple  
**Ahora:**
- Diferentes sonidos para diferentes eventos (inicio, fin, pausa)
- Volumen ajustable
- Opción de sonido "agradable" vs. "alarma fuerte"

---

### 4. **Categorías de sesiones** 
**Antes:** Todo es "Enfoque"  
**Ahora:** Puedes categorizar tus sesiones:
- "Matemáticas"
- "Programación"
- "Inglés"
- "Proyecto final"

Luego ver estadísticas por categoría.


---

### 5. **Sincronización en la nube** 
**Antes:** Los datos solo están en tu teléfono  
**Ahora:**
- Respalda tus sesiones en Firebase o Google Drive
- Si pierdes el teléfono, recuperas tus datos
- Sincroniza entre múltiples dispositivos


