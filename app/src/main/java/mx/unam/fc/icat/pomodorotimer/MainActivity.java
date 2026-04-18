package mx.unam.fc.icat.pomodorotimer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mx.unam.fc.icat.pomodorotimer.view.SessionHistoryActivity;
import mx.unam.fc.icat.pomodorotimer.view.PreferencesActivity;

import mx.unam.fc.icat.pomodorotimer.model.Session;
import mx.unam.fc.icat.pomodorotimer.model.SessionManager;


/**
 * Actividad principal que gestiona el ciclo de vida del temporizador Pomodoro.
 * Esta clase coordina la interfaz de usuario, los estados de la sesión y la
 * lógica de temporización utilizando CountDownTimer.
 * 
 * @author <a href="mailto:monmm@ciencias.unam.mx" > Mónica Miranda Mijangos
 *         </a> - @monmm
 * @version 1.2, mar 2026 (esqueleto para alumnos)
 */
public class MainActivity extends AppCompatActivity {

    /** Estados posibles del temporizador. */
    enum TimerState {
        IDLE, RUNNING, PAUSED
    }

    /** Modos de sesión según la técnica Pomodoro. */
    enum SessionMode {
        FOCUS, BREAK, REST
    }

    // Constantes de configuración.
    private static final long FOCUS_DURATION_MS = 25 * 60 * 1000L;
    private static final long BREAK_DURATION_MS = 5 * 60 * 1000L;
    private static final long REST_DURATION_MS = 15 * 60 * 1000L;
    private static final int SESSIONS_BEFORE_REST = 4;

    // Componentes de la interfaz de usuario.
    private Toolbar toolbar;
    private ChipGroup chipGroupMode;
    private Chip chipFocus, chipBreak, chipRest;
    private TextView tvTimerDisplay;
    private MaterialButton btnStartStop;
    private LinearLayout sessionDotsContainer;
    // TODO: Declarar los componentes faltantes para completar la IU:
    // 1. TextView para el estado de la sesión.
    // 2. TextView para el contador de sesiones completadas.
    // 3. ImageButtons para reiniciar (reset) y saltar (skip) la sesión.
    // 4. TextView para la(s) frase(s) motivadora(s).
    private TextView      tvSessionLabel;      //1.- Etiqueta de estado de la sesión
    private TextView      tvSessionsCount;     // 2.-Contador "X / 4 sesiones"
    private TextView      tvQuote;             // 4.-Frase motivadora
    private ImageButton   btnReset;            //3.-Reiniciar sesión actual
    private ImageButton   btnSkip;             //3.- Saltar la sesión


    // Elementos para el funcionamiento del temporizador.
    private CountDownTimer countDownTimer;
    private TimerState timerState = TimerState.IDLE;
    private SessionMode currentMode = SessionMode.FOCUS;
    private long timeLeftMillis = FOCUS_DURATION_MS;
    private int focusSessionsCompleted = 0;

    //  Datos de sesión activa
    /** Hora en que comenzó la sesión actual (para registrar en el historial). */
    private String sessionStartTime = "";
    /** Fecha ISO de hoy ("yyyy-MM-dd") para el campo sortDate en DB. */
    private String todaySortDate    = "";
    // Capa de datos
    private SessionManager sessionManager;

    // Formatos de fecha y hora
    private static final SimpleDateFormat FMT_DATE_DISPLAY =
            new SimpleDateFormat("EEE, dd MMM yyyy", new Locale("es", "MX"));
    private static final SimpleDateFormat FMT_DATE_SORT    =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat FMT_TIME         =
            new SimpleDateFormat("HH:mm", Locale.US);


    /**
     * Punto de entrada de la actividad.
     * Infla el layout, inicializa la capa de datos y prepara todos los
     * componentes de la UI para el primer uso.
     *
     * @param savedInstanceState Estado guardado de la instancia anterior
     *                           (no se usa porque la orientación es fija).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializamos la capa de datos.
        sessionManager = new SessionManager(this);

        // Pre-calculamos la fecha de hoy para no repetir la operación.
        Date now = new Date();
        todaySortDate = FMT_DATE_SORT.format(now);

        // Vinculamos las vistas con sus IDs.
        bindViews();
        // Habilitamos la barra de herramientas.
        setSupportActionBar(toolbar);
        // Registramos los escuchas de los botones.
        setupClickListeners();
        // Pintamos la UI en su estado inicial.
        updateTimerDisplay(timeLeftMillis);
        // Mostramos la frase motivadora inicial.
        tvQuote.setText(R.string.quote);
    }

    /**
     * Inicializa el menú de opciones superior (Overflow menu).
     * 
     * @param menu Objeto menú donde se inflarán las opciones.
     * @return true para que el menú sea visible.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Maneja la selección de ítems en el menú de la Toolbar.
     * 
     * @param item Ítem del menú seleccionado.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_history) {
            startActivity(new Intent(this, SessionHistoryActivity.class));
        }

        if (id == R.id.action_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Gestiona el comportamiento de pantalla completa inmersiva.
     * Se activa cada vez que la aplicación vuelve al primer plano.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    /**
     *  Libera los recursos del temporizador al destruir la actividad,
     *  implementa la cancelación del temporizador para prevenir fugas de
     * memoria.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancelamos el CountDownTimer para evitar que sus llamadas
        // intenten actualizar una vista que ya no existe.
        cancelTimer();

    }

    /**
     * Vincula las variables de instancia con los componentes declarados
     * en {@code activity_main.xml} mediante sus identificadores de recurso.
     */
    private void bindViews() {
        toolbar              = findViewById(R.id.tbMenu);
        chipGroupMode        = findViewById(R.id.chipGroupMode);
        chipFocus            = findViewById(R.id.chipFocus);
        chipBreak            = findViewById(R.id.chipBreak);
        chipRest             = findViewById(R.id.chipRest);
        tvTimerDisplay       = findViewById(R.id.tvTimerDisplay);
        tvSessionLabel       = findViewById(R.id.tvSessionLabel);
        tvSessionsCount      = findViewById(R.id.tvSessionsCount);
        tvQuote              = findViewById(R.id.tvQuote);
        btnStartStop         = findViewById(R.id.btnStartStop);
        btnReset             = findViewById(R.id.btnReset);
        btnSkip              = findViewById(R.id.btnSkip);
        sessionDotsContainer = findViewById(R.id.sessionDotsContainer);
    }

    /**
     * Registra los escuchas de clic para todos los botones interactivos
     * de la pantalla principal.
     */
    private void setupClickListeners() {
        // Botón principal: alterna entre Iniciar, Pausar y Reanudar.
        btnStartStop.setOnClickListener(v -> {
            if (timerState == TimerState.RUNNING)
                pauseTimer();
            else
                startTimer();
        });

        // Botón de reinicio: vuelve el temporizador al inicio de la sesión actual.
        btnReset.setOnClickListener(v -> resetTimer());

        // Botón de salto: avanza al siguiente estado de la secuencia Pomodoro.
        btnSkip.setOnClickListener(v -> skipToNextSession());
    }

    /**
     * Inicia o continúa el temporizador.
     *
     * Mantiene la pantalla encendida mientras el tiempo corre y actualiza
     * el botón a "Pausar". Crea un contador que descuenta segundo a segundo
     * y actualiza la pantalla hasta que la sesión termina.
     */
    private void startTimer() {
        // Mantiene la pantalla encendida.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timerState = TimerState.RUNNING;
        btnStartStop.setText(R.string.btn_pause);

        // Registramos la hora de inicio sólo cuando la sesión arranca de cero,
        // no cuando se reanuda desde pausa.
        if (sessionStartTime.isEmpty()) {
            sessionStartTime = FMT_TIME.format(new Date());
        }

        // PRUEBA
         addDot();

        // Creamos e inicializamos un contador.
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
            }

            @Override
            public void onFinish() {

                onSessionFinished();
            }
        }.start();
    }

    /**
     * Pausa el temporizador conservando el tiempo restante.
     * El estado pasa a {@link TimerState#PAUSED} y el botón muestra "Reanudar".
     */
    private void pauseTimer() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Detenemos nuestro contador.
        if (countDownTimer != null)
            countDownTimer.cancel();
        // Actualizamos el estado de nuestro temporizador.
        timerState = TimerState.PAUSED;
        // Actualizamos el texto del boton que controla el temporizador.
        btnStartStop.setText(R.string.btn_resume);
    }

    /**
     * TODO: Documentar.
     * TODO: Reiniciar el contenedor de puntos o agregar un nuevo indicador visual.
     * TODO: Actualizar el TextView de sesiones completadas (ej: "2 / 4").
     */
    private void onSessionFinished() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Actualizamos el estado de nuestro temporizador.
        timerState = TimerState.IDLE;

        // Lógica de transición de la técnica Pomodoro.
        if (currentMode == SessionMode.FOCUS) {
            focusSessionsCompleted++;
            if (focusSessionsCompleted >= SESSIONS_BEFORE_REST) {
                focusSessionsCompleted = 0;
                currentMode = SessionMode.REST;
            } else {
                currentMode = SessionMode.BREAK;
            }
        } else {
            currentMode = SessionMode.FOCUS;
        }

        // Mostramos un mensaje sencillo al finalizar cada sesion.
        Toast.makeText(this, "¡Sesión terminada!", Toast.LENGTH_SHORT).show();

        // Solicitamos al servicio del sistema que genere una vibracion simple
        // para notificar al usuario que la sesion a terminado.
        // PERMISOS NECESARIOS EN EL MANIFIESTO:
        // <uses-permission android:name="android.permission.VIBRATE" />
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // Actualizamos el temporizador y el texto del boton que lo controla.
        resetModeTime();
        btnStartStop.setText("Comenzar");
    }

    /**
     * Agrega un punto visual al contenedor {@link #sessionDotsContainer}
     * cada vez que se completa una sesión de enfoque.
     * Los puntos se limpian al completar el ciclo de 4 sesiones.
     */
    private void addDot() {
        // Limpiamos el contenedor cuando comenzamos un nuevo ciclo.
        if (sessionDotsContainer.getChildCount() >= SESSIONS_BEFORE_REST) {
            sessionDotsContainer.removeAllViews();
        }

        // Creamos la vista del punto.
        View dot = new View(this);
        float density = getResources().getDisplayMetrics().density;

        // Definimos su tamano (10dp convertido a pixeles).
        int dotSize = (int) (10 * getResources().getDisplayMetrics().density);
        // Creamos un contenedor para el punto.
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
        // Agregamos un margen de separacion a la derecha (8dp).
        params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
        // Aplicamos el layout a la vista.
        dot.setLayoutParams(params);
        // Asignamos la figura de nuestro punto (drawable).
        dot.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_session_completed));
        // Agregamos el punto creado al contenedor.
        sessionDotsContainer.addView(dot);
    }

    /**
     * TODO: Documentar.
     */
    private void resetModeTime() {
        // Reasignamos la duracion de la sesion segun el estado actual.
        switch (currentMode) {
            case FOCUS:
                timeLeftMillis = FOCUS_DURATION_MS;
                break;
            case BREAK:
                timeLeftMillis = BREAK_DURATION_MS;
                break;
            case REST:
                timeLeftMillis = REST_DURATION_MS;
                break;
        }
        // Actualizamos la IU.
        updateTimerDisplay(timeLeftMillis);
    }

    /**
     * Cancela el temporizador activo y libera su referencia.
     * Se llama en {@link #onDestroy()}, {@link #resetTimer()} y
     * {@link #skipToNextSession()} para evitar efectos secundarios.
     */
    private void cancelTimer() {
        // Si el temporizador esta activo:
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (countDownTimer != null) {
            // Detemos el tiempo.
            countDownTimer.cancel();
            // Anulamos el temporizador.
            countDownTimer = null;
        }
    }

    /**
     * Reinicia el temporizador al inicio de la sesión actual sin cambiar
     * el modo (FOCUS/BREAK/REST).
     *
     * <p>Si había una sesión en curso (RUNNING o PAUSED), se guarda como
     * "Interrumpida" en el historial antes de reiniciar.</p>
     */
    private void resetTimer() {
        // Si había una sesión activa, la registramos como interrumpida.
        if (timerState != TimerState.IDLE && currentMode == SessionMode.FOCUS
              && !sessionStartTime.isEmpty()) {
            saveSession(true);
        }
        cancelTimer();
        timerState   = TimerState.IDLE;
        sessionStartTime = "";
        resetModeTime();
        btnStartStop.setText(R.string.btn_start);
    }

    /**
     * Salta al siguiente estado de la secuencia Pomodoro sin esperar a que
     * el temporizador llegue a cero.
     *
     * <p>Si había una sesión de enfoque en curso, se guarda como "Interrumpida".
     * Los descansos saltados no generan registro en el historial.</p>
     */
    private void skipToNextSession() {
        // Registramos la sesión de enfoque como interrumpida si estaba activa.
        if (timerState != TimerState.IDLE && currentMode == SessionMode.FOCUS &&
                !sessionStartTime.isEmpty()) {
            saveSession(true);
        }
        cancelTimer();
        // Simulamos el mismo avance de estado que ocurriría al finalizar.
        advanceMode();
        timerState = TimerState.IDLE;
        //sessionStartTime = "";
        resetModeTime();
        btnStartStop.setText(R.string.btn_start);
    }


    /**
     * Construye un objeto {@link Session} con los datos de la sesión
     * actual y lo entrega al {@link SessionManager} para su persistencia.
     *
     * @param completed {@code true} si la sesión se completó normalmente;
     *                  {@code false} si fue interrumpida por el usuario.
     */
    private void saveSession(boolean completed) {
        // Determinamos el tipo en texto legible.
        String type;
        int    durationMin;
        switch (currentMode) {
            case BREAK:
                type        = "Descanso";
                durationMin = (int) (BREAK_DURATION_MS / 60_000);
                break;
            case REST:
                type        = "Descanso largo";
                durationMin = (int) (REST_DURATION_MS  / 60_000);
                break;
            default:
                type        = "Enfoque";
                durationMin = (int) (FOCUS_DURATION_MS / 60_000);
                break;
        }

        Date   now         = new Date();
        String dateDisplay = FMT_DATE_DISPLAY.format(now);
        String startTime   = sessionStartTime.isEmpty()
                ? FMT_TIME.format(now)
                : sessionStartTime;

        Session session = new Session(
                type, dateDisplay, startTime, durationMin, completed, todaySortDate);

        // SessionManager ejecuta la inserción en un hilo secundario.
        sessionManager.addSession(session);
    }

    /**
     * Avanza el modo de la sesión siguiendo la secuencia Pomodoro:
     * FOCUS → BREAK (o REST cada 4 sesiones) → FOCUS.
     */
    private void advanceMode() {
        if (currentMode == SessionMode.FOCUS) {
            focusSessionsCompleted++;
            if (focusSessionsCompleted >= SESSIONS_BEFORE_REST) {
                focusSessionsCompleted = 0;
                currentMode = SessionMode.REST;
            } else {
                currentMode = SessionMode.BREAK;
            }
        } else {
            currentMode = SessionMode.FOCUS;
        }
    }

    /**
     * TODO: Documentar.
     * 
     * @param millis ...
     */
    private void updateTimerDisplay(long millis) {
        // Resaltamos el chip correspondiente al estado actual del temporizador.
        selectChipForMode(currentMode);
        int minutes = (int) (millis / 1000) / 60;
        int seconds = (int) (millis / 1000) % 60;
        // Actualizamos el texto del temporizador.
        tvTimerDisplay.setText(String.format("%02d:%02d", minutes, seconds));

        // Actualizamos la etiqueta de estado de la sesión.
        updateSessionLabel();
        // Actualizamos el contador "X / 4".
        updateSessionsCountLabel();
    }

    /**
     * Actualiza la etiqueta de texto que describe el modo actual
     * (p.ej. "Sesión de Enfoque", "Descanso corto", "Descanso largo").
     */
    private void updateSessionLabel() {
        String label;
        switch (currentMode) {
            case BREAK: label = getString(R.string.timer_label_focus, "Descanso corto");  break;
            case REST:  label = getString(R.string.timer_label_focus, "Descanso largo");  break;
            default:    label = getString(R.string.timer_label_focus, "Enfoque");         break;
        }
        tvSessionLabel.setText(label);
    }

    /**
     * Actualiza el contador de sesiones de enfoque completadas en la
     * etiqueta inferior del temporizador (p.ej. "2 sesiones completadas").
     */
    private void updateSessionsCountLabel() {
        String text = getString(R.string.sessionsCount, focusSessionsCompleted);
        tvSessionsCount.setText(text);
    }


    /**
     * Marca el chip que corresponde al modo correspondiente
     *
     * @param mode Modo actual del temporizador.
     */
    private void selectChipForMode(SessionMode mode) {
        // El identificador del chip a seleccionar.
        int chipId;
        switch (mode) {
            case BREAK:
                // Asignamos el elemento en el layout (el chip).
                chipId = R.id.chipBreak;
                // Resaltamos el chip seleccionado.
                highlightChip(chipBreak);
                break;
            case REST:
                chipId = R.id.chipRest;
                highlightChip(chipRest);
                break;
            default:
                chipId = R.id.chipFocus;
                highlightChip(chipFocus);
                break;
        }
        chipGroupMode.check(chipId);
        // La agrupacion sabe que chip hemos seleccionado.
    }

    /**
     * TODO: Documentar.
     * 
     * @param activeChip ...
     */
    private void highlightChip(Chip activeChip) {
        // Obtenemos la densidad de pantalla necesaria para construir el borde de
        // nuestros chips.
        float density = getResources().getDisplayMetrics().density;
        // Enlistamos los chips disponibles para manipularlos facilmente.
        Chip[] allChips = { chipFocus, chipBreak, chipRest };

        // Quitamos el borde de todos los chips.
        for (Chip chip : allChips) {
            chip.setChipStrokeWidth(0);
        }

        // Resaltamos el chip activo modificando el grosor del borde.
        activeChip.setChipStrokeWidth(2 * density);
        // Recuperamos el color para resaltar el borde del chip de los recursos de
        // nuestra app.
        int colorAccent = ContextCompat.getColor(this, R.color.color_border_accent);
        // Asignamos el color del borde para resaltar al chip activo.
        activeChip.setChipStrokeColor(ColorStateList.valueOf(colorAccent));
    }


}

