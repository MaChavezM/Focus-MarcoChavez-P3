package mx.unam.fc.icat.pomodorotimer.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mx.unam.fc.icat.pomodorotimer.R;
import mx.unam.fc.icat.pomodorotimer.model.Session;
import mx.unam.fc.icat.pomodorotimer.model.SessionManager;

/**
 * Actividad que visualiza el historial cronológico de las sesiones de enfoque y
 * descanso.
 * Se utiliza como práctica para el manejo de RecyclerView, adaptadores y
 * filtrado de datos.
 * 
 * @author <a href="mailto:monmm@ciencias.unam.mx" > Mónica Miranda Mijangos
 *         </a> - @monmm
 * @version 1.2, mar 2026 (esqueleto para alumnos)
 */
public class SessionHistoryActivity extends AppCompatActivity {

    // Componentes de la Interfaz de Usuario.
    private Toolbar toolbar;
    private TextView tvResultCount;
    private ConstraintLayout layoutEmpty;
    private RecyclerView recyclerView;

    // TODO: Declarar los componentes de filtrado (ChipGroup y Chips individuales).
    private ChipGroup chipGroupFilter;
    private Chip chipFilterToday;
    private Chip chipFilterWeek;
    private Chip chipFilterAll;

    // Lógica y Datos.
    private SessionHistoryAdapter adapter;
    private SessionManager sessionManager;

    // Formatos de fecha para los filtros
    private static final SimpleDateFormat FMT_SORT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    // ── Filtro activo ──────────────────────────────────────────────────────
    /**
     * Constantes que identifican el filtro de período seleccionado.
     */
    private static final int FILTER_TODAY = 0;
    private static final int FILTER_WEEK = 1;
    private static final int FILTER_ALL = 2;

    /**
     * Filtro activo al abrir la pantalla.
     */
    private int activeFilter = FILTER_TODAY;

    /**
     * Inicializa la actividad: vincula vistas, configura la Toolbar,
     * el RecyclerView y la lógica de filtrado.
     *
     * @param savedInstanceState Estado previo (no utilizado en esta actividad).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_session);

        // Instanciamos el SessionManager con el contexto de la app.
        sessionManager = new SessionManager(this);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterLogic();
        // updateHistoryDisplay();
        // Aplicamos el filtro inicial ("Hoy") y actualizamos la UI.
        applyFilter(activeFilter);
    }

    /**
     * Vincula las variables con los componentes del XML.
     */
    private void bindViews() {
        toolbar = findViewById(R.id.history_toolbar);
        tvResultCount = findViewById(R.id.tvResultCount);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        recyclerView = findViewById(R.id.recyclerViewHistory);

        // Chips de filtrado (declarados en activity_history_session.xml).
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipFilterToday = findViewById(R.id.chipFilterToday);
        chipFilterWeek = findViewById(R.id.chipFilterWeek);
        chipFilterAll = findViewById(R.id.chipFilterAll);
    }

    /**
     * Registra los escuchas del {@link ChipGroup} de filtros.
     * Al cambiar la selección, se recuperan los datos correspondientes
     * desde SQLite y se refresca el RecyclerView.
     */
    private void setupFilterLogic() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return; // singleSelection garantiza al menos 1.

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipFilterToday) {
                activeFilter = FILTER_TODAY;
            } else if (checkedId == R.id.chipFilterWeek) {
                activeFilter = FILTER_WEEK;
            } else if (checkedId == R.id.chipFilterAll) {
                activeFilter = FILTER_ALL;
            }
            applyFilter(activeFilter);
        });
    }

    /**
     * Recupera de SQLite las sesiones que corresponden al filtro indicado
     * y actualiza el RecyclerView, el contador de resultados y el estado vacío.
     *
     * @param filter Una de las constantes: {@link #FILTER_TODAY},
     *               {@link #FILTER_WEEK} o {@link #FILTER_ALL}.
     */
    private void applyFilter(int filter) {
        List<Session> sessions;
        Date now = new Date();

        try {
            switch (filter) {
                case FILTER_TODAY:
                    // Sesiones del día de hoy.
                    String today = FMT_SORT.format(now);
                    sessions = sessionManager.getTodaySessions(today);
                    break;

                case FILTER_WEEK:
                    // Sesiones de los últimos 7 días (incluyendo hoy).
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_YEAR, -6); // hoy − 6 días = semana completa
                    String weekStart = FMT_SORT.format(cal.getTime());
                    sessions = sessionManager.getWeekSessions(weekStart);
                    break;

                default:
                    // Historial completo.
                    sessions = sessionManager.getHistory();
                    break;
            }
        } catch (Exception e) {
            // Ante cualquier error de base de datos mostramos lista vacía.
            sessions = new java.util.ArrayList<>();
        }

        updateHistoryDisplay(sessions);
    }

    /**
     * Configura la Toolbar como ActionBar de la actividad.
     * Habilita el botón de retroceso (Up Navigation) y asigna el título
     * desde los recursos de cadena para soporte multi-idioma.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_history);
        }
    }

    /**
     * Inicializa el RecyclerView con su LayoutManager y Adaptador.
     * Vincula la lista de sesiones obtenida del SessionManager con la
     * interfaz visual mediante el SessionHistoryAdapter.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Iniciamos con lista vacía; applyFilter() la poblará de inmediato.
        adapter = new SessionHistoryAdapter(
                new java.util.ArrayList<>(), getResources());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Gestiona la visibilidad de la UI y actualiza el contador.
     */
    private void updateHistoryDisplay(List<Session> sessions) {
        // Actualizamos el dataset del adaptador con notificación de cambio.
        adapter.updateDataset(sessions);

        boolean isEmpty = (sessions == null || sessions.isEmpty());
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        // Actualizamos el contador de resultados.
        int count = (sessions != null) ? sessions.size() : 0;
        tvResultCount.setText(getString(R.string.session_count, count));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}