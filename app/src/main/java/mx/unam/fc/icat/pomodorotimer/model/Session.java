package mx.unam.fc.icat.pomodorotimer.model;


/**
 * Modelo de datos que representa una sesión de trabajo registrada por FocusLab.
 *
 * <p>Campos principales:</p>
 * <ul>
 *   <li>{@code type}      – tipo de sesión: "Enfoque", "Descanso" o "Descanso largo"</li>
 *   <li>{@code date}      – fecha en formato legible, p.ej. "lun, 18 mar 2026"</li>
 *   <li>{@code startTime} – hora de inicio en formato "HH:mm"</li>
 *   <li>{@code duration}  – duración en minutos (25, 5 ó 15)</li>
 *   <li>{@code completed} – {@code true} si finalizó naturalmente; {@code false} si fue interrumpida</li>
 *   <li>{@code sortDate}  – fecha en formato "yyyy-MM-dd" para ordenamiento y filtrado en SQLite</li>
 * </ul>
 *
 * @author <a href="mailto:marcoantonio.chavez594@ciencias.unam.mx" > Marco Antonio Chavez Martinez </a> - @MaChavezM
 * @version 1.0, abril 2026
 */
public class Session {
    private String type; // Tipo de la sesion: Enfoque o Descanso
    private String date; // Formato: EEE, dd MMM yyyy
    private String startTime; // Formato: hh:mm
    private int duration; // Duracion de la sesion: 25, 5 o 15 min
    private boolean completed; // La sesion fue Completada o Interrumpida
    private String sortDate; // Formato: yyyy-MM-dd usada para filtrar registros en SQLite

    /**
     * Constructor principal que incluye el campo de ordenamiento.
     *
     * @param type      Tipo de sesión.
     * @param date      Fecha en formato legible.
     * @param startTime Hora de inicio.
     * @param duration  Duración en minutos.
     * @param completed Estado de finalización.
     * @param sortDate  Fecha en formato "yyyy-MM-dd" para filtros DB.
     */
    public Session(String type, String date, String startTime, int duration, boolean completed,
                   String sortDate) {
        this.type = type;
        this.date = date;
        this.startTime = startTime;
        this.duration = duration;
        this.completed = completed;
        this.sortDate  = sortDate;
    }

    /**
     * Constructor de compatibilidad sin {@code sortDate}.
     * El campo {@code sortDate} quedará como cadena vacía.
     *
     * @param type      Tipo de sesión.
     * @param date      Fecha en formato legible.
     * @param startTime Hora de inicio.
     * @param duration  Duración en minutos.
     * @param completed Estado de finalización.
     */
    public Session(String type, String date, String startTime,
                   int duration, boolean completed) {
        this(type, date, startTime, duration, completed, "");
    }

    // Getters y Setters
    /** @return Tipo de sesión. */
    public String getType() { return type; }

    /** @param type Nuevo tipo de sesión. */
    public void setType(String type) { this.type = type; }

    /** @return Fecha en formato legible. */
    public String getDate() { return date; }

    /** @param date Nueva fecha en formato legible. */
    public void setDate(String date) { this.date = date; }

    /** @return Hora de inicio en formato "HH:mm". */
    public String getStartTime() { return startTime; }

    /** @param startTime Nueva hora de inicio. */
    public void setStartTime(String startTime) { this.startTime = startTime; }

    /** @return Duración de la sesión en minutos. */
    public int getDuration() { return duration; }

    /** @param duration Nueva duración en minutos. */
    public void setDuration(int duration) { this.duration = duration; }

    /** @return {@code true} si la sesión fue completada exitosamente. */
    public boolean isCompleted() { return completed; }

    /** @param completed Nuevo estado de finalización. */
    public void setCompleted(boolean completed) { this.completed = completed; }

    /** @return Fecha en formato "yyyy-MM-dd" para operaciones de base de datos. */
    public String getSortDate() { return sortDate; }

    /** @param sortDate Nueva fecha en formato "yyyy-MM-dd". */
    public void setSortDate(String sortDate) { this.sortDate = sortDate; }
}