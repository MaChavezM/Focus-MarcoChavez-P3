package mx.unam.fc.icat.pomodorotimer.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import mx.unam.fc.icat.pomodorotimer.data.DatabaseHelper;

/**
 * Gestiona el ciclo de vida de las tareas sugeridas dentro de la aplicación.
 * Implementa las operaciones básicas de persistencia en memoria (CRUD).
 * 
 * @author <a href="mailto:monmm@ciencias.unam.mx" > Mónica Miranda Mijangos
 *         </a> - @monmm
 * @version 1.0, feb 2026
 */
public class SessionManager {

    /** Helper que encapsula todas las operaciones de SQLite. */
    private final DatabaseHelper dbHelper;

    // Constructor

    /**
     * Crea una instancia de {@code SessionManager} inicializando el helper
     * de base de datos.
     *
     * @param context Contexto de la aplicación; se usa para localizar el
     *                archivo de base de datos en el almacenamiento privado.
     */
    public SessionManager(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // Escritura (CREATE)

    /**
     * Persiste una nueva sesión en la base de datos de forma asíncrona.
     *
     * <p>La inserción se realiza en un hilo secundario ({@link Thread})
     * para garantizar que la interfaz no se congele, incluso si el sistema
     * de archivos está ocupado.</p>
     *
     * @param session Sesión a guardar; se ignora si es {@code null}.
     */
    public void addSession(final Session session) {
        if (session == null) return;
        // Ejecutamos la escritura en un hilo de fondo para no bloquear la UI.
        new Thread(() -> {
            try {
                dbHelper.insertSession(session);
            } catch (Exception e) {
                // Error silencioso: el historial podría quedar incompleto,
                // pero la app sigue funcionando.
            }
        }).start();
    }

    // Lectura (READ)

    /**
     * Devuelve el historial completo de sesiones registradas, ordenadas
     * de la más reciente a la más antigua.
     *
     * @return Lista (posiblemente vacía) de objetos {@link Session}.
     */
    public List<Session> getHistory() {
        return dbHelper.getAllSessions();
    }

    /**
     * Devuelve únicamente las sesiones correspondientes al día indicado.
     *
     * @param todaySortDate Fecha de hoy en formato "yyyy-MM-dd".
     * @return Lista filtrada de {@link Session}.
     */
    public List<Session> getTodaySessions(String todaySortDate) {
        return dbHelper.getSessionsByDay(todaySortDate);
    }

    /**
     * Devuelve las sesiones a partir de una fecha de corte (inclusive),
     * útil para el filtro "Esta semana".
     *
     * @param fromSortDate Fecha inicial en formato "yyyy-MM-dd".
     * @return Lista filtrada de {@link Session}.
     */
    public List<Session> getWeekSessions(String fromSortDate) {
        return dbHelper.getSessionsFromDate(fromSortDate);
    }
}
