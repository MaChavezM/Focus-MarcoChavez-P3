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

import mx.unam.fc.icat.focusmony.view.PreferencesActivity;
import mx.unam.fc.icat.focusmony.view.SessionHistoryActivity;

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

    // Elementos para el funcionamiento del temporizador.
    private CountDownTimer countDownTimer;
    private TimerState timerState = TimerState.IDLE;
    private SessionMode currentMode = SessionMode.FOCUS;
    private long timeLeftMillis = FOCUS_DURATION_MS;
    private int focusSessionsCompleted = 0;

    /**
     * TODO: Documentar.
     * 
     * @param savedInstanceState ...
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Inflamos nuestra vista.
        setContentView(R.layout.activity_main);

        // Inicializamos los elementos de la IU.
        bindViews();
        // Habilitamos nuestra barra de herramientas.
        setSupportActionBar(toolbar);
        // Asignamos los escuchas.
        setupClickListeners();
        // Actualizamos la IU.
        updateTimerDisplay(timeLeftMillis);
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
     * TODO: Documentar.
     * TODO: implementar la cancelación del temporizador para prevenir fugas de
     * memoria.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * TODO: Documentar.
     * TODO: Inicializar todos los componentes declarados anteriormente.
     */
    private void bindViews() {
        toolbar = findViewById(R.id.tbMenu);
        chipGroupMode = findViewById(R.id.chipGroupMode);
        chipFocus = findViewById(R.id.chipFocus);
        chipBreak = findViewById(R.id.chipBreak);
        chipRest = findViewById(R.id.chipRest);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        btnStartStop = findViewById(R.id.btnStartStop);
        sessionDotsContainer = findViewById(R.id.sessionDotsContainer);
    }

    /**
     * TODO: Documentar.
     */
    private void setupClickListeners() {
        btnStartStop.setOnClickListener(v -> {
            // Lógica de alternancia según el estado actual del motor.
            if (timerState == TimerState.RUNNING)
                pauseTimer();
            else
                startTimer();
        });

        // TODO: Asignar listeners a los botones de reset y skip.
    }

    /**
     * TODO: Documentar.
     */
    private void startTimer() {
        // Mantiene la pantalla encendida.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        timerState = TimerState.RUNNING;
        btnStartStop.setText(R.string.btn_pause);

        // PRUEBA
        // addDot();

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
     * TODO: Documentar.
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
     * TODO: Documentar.
     */
    private void addDot() {
        // Creamos la vista del punto.
        View dot = new View(this);
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
     * TODO: Documentar.
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
     * TODO: Documentar.
     */
    private void resetTimer() {
        // TODO: Implementar el reinicio manual de la sesión actual.
    }

    /**
     * TODO: Documentar.
     */
    private void skipToNextSession() {
        // TODO: Implementar la lógica para saltar al siguiente estado.
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

        // TODO: Actualizar el texto del estado de la sesión.
    }

    /**
     * TODO: Documentar.
     * 
     * @param mode ...
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