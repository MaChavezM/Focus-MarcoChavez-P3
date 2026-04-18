package mx.unam.fc.icat.pomodorotimer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el ciclo de vida de las tareas sugeridas dentro de la aplicación.
 * Implementa las operaciones básicas de persistencia en memoria (CRUD).
 * 
 * @author <a href="mailto:monmm@ciencias.unam.mx" > Mónica Miranda Mijangos
 *         </a> - @monmm
 * @version 1.0, feb 2026
 */
public class SessionManager {
    private List<Session> sessionHistory;

    public SessionManager() {
        this.sessionHistory = new ArrayList<>();
    }

    /**
     *
     * @param session
     */
    public void addSession(Session session) {
        if (session != null) {
            sessionHistory.add(0, session); // Insertamos al inicio para ver lo más reciente
        }
    }

    /**
     *
     * @return
     */
    public List<Session> getHistory() {
        return new ArrayList<>(sessionHistory);
    }

    // TODO: completar operaciones CRUD.
    // + metodo para obtener las sesiones del dia de hoy.
    // + metodo para las sesiones de esta semana.
}
