package mx.unam.fc.icat.pomodorotimer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import mx.unam.fc.icat.pomodorotimer.model.Session;

/**
 * Proveedor de acceso a la base de datos SQLite local de FocusLab.
 * Extiende {@link SQLiteOpenHelper} para gestionar la creación, versión y
 * actualización del esquema de la base de datos de forma automática.
 *
 * <p>Esquema de la tabla {@value #TABLE_SESSIONS}:</p>
 * <ul>
 *   <li>{@value #COL_ID}         – clave primaria autoincremental</li>
 *   <li>{@value #COL_TYPE}       – tipo de sesión ("Enfoque", "Descanso", "Descanso largo")</li>
 *   <li>{@value #COL_DATE}       – fecha legible, p.ej. "lun, 18 mar 2026"</li>
 *   <li>{@value #COL_START_TIME} – hora de inicio, p.ej. "15:30"</li>
 *   <li>{@value #COL_DURATION}   – duración en minutos (entero)</li>
 *   <li>{@value #COL_COMPLETED}  – 1 si se completó, 0 si se interrumpió</li>
 *   <li>{@value #COL_DATE_SORT}  – fecha en formato "yyyy-MM-dd" para ordenamiento y filtrado</li>
 * </ul>
 *
 * @author <a href="mailto:marcoantonio.chavez594@ciencias.unam.mx" > Marco Antonio Chavez Martinez </a> - @MaChavezM
 * @version 1.0, abril 2026
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // ── Metadatos de la base de datos ────────────────────────────
    private static final String DB_NAME    = "focuslab.db";
    private static final int    DB_VERSION = 1;

    // Nombre de la tabla
    public static final String TABLE_SESSIONS = "sessions";

    // Nombres de columnas
    public static final String COL_ID         = "_id";
    public static final String COL_TYPE       = "type";
    public static final String COL_DATE       = "date";
    public static final String COL_START_TIME = "start_time";
    public static final String COL_DURATION   = "duration";
    public static final String COL_COMPLETED  = "completed";
    public static final String COL_DATE_SORT  = "date_sort"; // formato "yyyy-MM-dd"

    // DDL
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_SESSIONS + " ("
                    + COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_TYPE       + " TEXT    NOT NULL, "
                    + COL_DATE       + " TEXT    NOT NULL, "
                    + COL_START_TIME + " TEXT    NOT NULL, "
                    + COL_DURATION   + " INTEGER NOT NULL, "
                    + COL_COMPLETED  + " INTEGER NOT NULL DEFAULT 0, "
                    + COL_DATE_SORT  + " TEXT    NOT NULL"
                    + ")";

    // ── Constructor ────────

    /**
     * Crea o abre la base de datos {@value #DB_NAME} en el almacenamiento
     * privado de la aplicación.
     *
     * @param context Contexto de la aplicación necesario para localizar el
     *                directorio de bases de datos.
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ── Ciclo de vida del esquema

    /**
     * Se invoca la primera vez que la base de datos es creada.
     * Ejecuta la sentencia DDL para construir la tabla de sesiones.
     *
     * @param db Instancia de la base de datos recién creada.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    /**
     * Se invoca cuando la versión de la base de datos cambia.
     * La estrategia actual es descartar y recrear todas las tablas
     * (válido durante el desarrollo; en producción se usarían migraciones).
     *
     * @param db         Base de datos a actualizar.
     * @param oldVersion Número de versión anterior.
     * @param newVersion Número de versión nuevo.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        onCreate(db);
    }

    // Operaciones CRUD

    /**
     * Inserta una nueva sesión en la base de datos.
     * @param session Objeto {@link Session} con los datos a persistir.
     *                No debe ser {@code null}.
     * @return El ID de la fila insertada, o {@code -1} si ocurrió un error.
     */
    public long insertSession(Session session) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COL_TYPE,       session.getType());
            values.put(COL_DATE,       session.getDate());
            values.put(COL_START_TIME, session.getStartTime());
            values.put(COL_DURATION,   session.getDuration());
            values.put(COL_COMPLETED,  session.isCompleted() ? 1 : 0);
            values.put(COL_DATE_SORT,  session.getSortDate());
            return db.insert(TABLE_SESSIONS, null, values);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Recupera el historial completo de sesiones, ordenado de la más
     * reciente a la más antigua.
     *
     * @return Lista de objetos {@link Session}; vacía si no hay registros
     *         o si ocurre un error.
     */
    public List<Session> getAllSessions() {
        List<Session> sessions = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.query(
                     TABLE_SESSIONS,
                     null,
                     null, null, null, null,
                     COL_ID + " DESC")) {
            while (cursor.moveToNext()) {
                sessions.add(cursorToSession(cursor));
            }
        } catch (Exception e) {
            // Devolvemos la lista vacía ante cualquier error.
        }
        return sessions;
    }

    /**
     * Recupera únicamente las sesiones registradas en el día indicado.
     *
     * @param dateSort Fecha en formato "yyyy-MM-dd" correspondiente al
     *                 día que se quiere consultar.
     * @return Lista filtrada de {@link Session}; vacía si no hay coincidencias.
     */
    public List<Session> getSessionsByDay(String dateSort) {
        List<Session> sessions = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.query(
                     TABLE_SESSIONS,
                     null,
                     COL_DATE_SORT + " = ?",
                     new String[]{dateSort},
                     null, null,
                     COL_ID + " DESC")) {
            while (cursor.moveToNext()) {
                sessions.add(cursorToSession(cursor));
            }
        } catch (Exception e) {
            // Devolvemos la lista vacía ante cualquier error.
        }
        return sessions;
    }

    /**
     * Recupera las sesiones registradas a partir de una fecha de corte
     * (inclusive), útil para el filtro "Esta semana".
     *
     * @param fromDateSort Fecha en formato "yyyy-MM-dd" a partir de la cual
     *                     se incluyen los registros.
     * @return Lista filtrada de {@link Session}; vacía si no hay coincidencias.
     */
    public List<Session> getSessionsFromDate(String fromDateSort) {
        List<Session> sessions = new ArrayList<>();
        try (SQLiteDatabase db = getReadableDatabase();
             Cursor cursor = db.query(
                     TABLE_SESSIONS,
                     null,
                     COL_DATE_SORT + " >= ?",
                     new String[]{fromDateSort},
                     null, null,
                     COL_ID + " DESC")) {
            while (cursor.moveToNext()) {
                sessions.add(cursorToSession(cursor));
            }
        } catch (Exception e) {
            // Devolvemos la lista vacía ante cualquier error.
        }
        return sessions;
    }


    /**
     * Construye un objeto {@link Session} a partir de la fila actualmente
     * apuntada por el cursor.
     *
     * @param cursor Cursor posicionado en una fila válida de la tabla.
     * @return Objeto {@link Session} poblado con los datos de la fila.
     */
    private Session cursorToSession(Cursor cursor) {
        String  type      = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
        String  date      = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
        String  startTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME));
        int     duration  = cursor.getInt   (cursor.getColumnIndexOrThrow(COL_DURATION));
        boolean completed = cursor.getInt   (cursor.getColumnIndexOrThrow(COL_COMPLETED)) == 1;
        String  sortDate  = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_SORT));
        return new Session(type, date, startTime, duration, completed, sortDate);
    }
}