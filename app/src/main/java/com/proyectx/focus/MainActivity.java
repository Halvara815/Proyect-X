package com.proyectx.focus;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Main local-first dashboard. The accessibility integration remains explicitly optional. */
public final class MainActivity extends Activity {
    private static final int DNS_VPN_CONSENT_REQUEST = 4_201;
    private static final int DNS_NOTIFICATION_REQUEST = 4_202;
    private static final int RULE_EXPORT_REQUEST = 4_203;
    // Original Proyect X visual tokens. They intentionally do not derive from reference products.
    private static final int BACKGROUND = Color.rgb(246, 242, 234);
    private static final int SURFACE = Color.WHITE;
    private static final int SURFACE_RAISED = Color.rgb(232, 239, 233);
    private static final int PRIMARY = Color.rgb(22, 122, 104);
    private static final int ACCENT = Color.rgb(199, 243, 107);
    private static final int TEXT = Color.rgb(22, 51, 45);
    private static final int MUTED = Color.rgb(102, 117, 111);
    private static final int DANGER = Color.rgb(204, 91, 56);
    private static final int HERO = Color.rgb(228, 244, 225);
    private static final int OUTLINE = Color.rgb(211, 221, 213);

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = this::refreshSession;
    private RuleStore ruleStore;
    private BudgetStore budgetStore;
    private FocusSessionStore sessionStore;
    private HabitStreakStore habitStreakStore;
    private FaithModeStore faithModeStore;
    private RecoveryStore recoveryStore;
    private LinearLayout rulesContainer;
    private TextView ruleCountView;
    private TextView protectionStatusView;
    private TextView dnsStatusView;
    private ImageView dnsPowerIcon;
    private TextView sessionTitleView;
    private TextView sessionDetailView;
    private TextView domainRuleCountView;
    private TextView appRuleCountView;
    private TextView keywordRuleCountView;
    private TextView ruleBreakdownView;
    private Button sessionAction;
    private EditText valueInput;
    private Spinner typeSelector;
    private Spinner actionSelector;
    private Button appPickerButton;
    private Button scheduleButton;
    private Button budgetPickerButton;
    private LinearLayout budgetsContainer;
    private Button dnsAction;
    private TextView scheduleDetailView;
    private ScheduleWindow draftSchedule = ScheduleWindow.always();
    private String activeDestination = "inicio";
    private String pendingRulesExport;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        ruleStore = new RuleStore(this);
        budgetStore = new BudgetStore(this);
        sessionStore = new FocusSessionStore(this);
        habitStreakStore = new HabitStreakStore(this);
        faithModeStore = new FaithModeStore(this);
        recoveryStore = new RecoveryStore(this);
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ruleStore != null) render();
    }

    @Override
    protected void onPause() {
        refreshHandler.removeCallbacks(refreshRunnable);
        super.onPause();
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(32));
        scroll.addView(
                root,
                new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout header = row(Gravity.CENTER_VERTICAL);
        TextView mark = text("X", 18, Color.WHITE, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(PRIMARY, 22));
        header.addView(mark, size(dp(44), dp(44)));
        LinearLayout headerWords = column();
        headerWords.setPadding(dp(12), 0, 0, 0);
        headerWords.addView(text("Proyect X", 26, TEXT, Typeface.BOLD));
        headerWords.addView(text("PRIVACIDAD LOCAL · SIN CUENTA", 11, PRIMARY, Typeface.BOLD));
        header.addView(headerWords, weight());
        ImageView menu = new ImageView(this);
        menu.setImageResource(R.drawable.ic_menu_centro);
        menu.setColorFilter(TEXT);
        menu.setContentDescription("Abrir centro local");
        menu.setClickable(true);
        menu.setFocusable(true);
        menu.setPadding(dp(10), dp(10), dp(10), dp(10));
        menu.setBackground(round(SURFACE_RAISED, 18));
        menu.setOnClickListener(this::showControlMenu);
        header.addView(menu, size(dp(44), dp(44)));
        root.addView(header);

        LinearLayout destinations = column();
        LinearLayout homeDestination = column();
        LinearLayout protectionDestination = column();
        LinearLayout rulesDestination = column();
        LinearLayout recoveryDestination = column();
        destinations.addView(homeDestination);
        destinations.addView(protectionDestination);
        destinations.addView(rulesDestination);
        destinations.addView(recoveryDestination);
        root.addView(destinations);

        LinearLayout hero = card();
        hero.setBackground(round(HERO, 24));
        hero.setElevation(0);
        hero.addView(tag("MAPA DEL DÍA", PRIMARY, BACKGROUND));
        hero.addView(text("Tu día, con menos interrupciones.", 25, TEXT, Typeface.BOLD));
        hero.addView(text(
                "Elige una sesión, ajusta tus reglas y conoce siempre el alcance de la protección que activas.",
                15,
                MUTED,
                Typeface.NORMAL));
        LinearLayout heroRoutes = row(Gravity.CENTER_VERTICAL);
        Button barriersRoute = compactButton("VER BARRERAS", SURFACE, TEXT);
        barriersRoute.setOnClickListener(view -> switchDestination("proteccion"));
        heroRoutes.addView(barriersRoute, weightWithMargin(1, 0, 0, dp(8), 0));
        Button rulesRoute = compactButton("REVISAR REGLAS", PRIMARY, Color.WHITE);
        rulesRoute.setOnClickListener(view -> switchDestination("reglas"));
        heroRoutes.addView(rulesRoute, weight());
        hero.addView(heroRoutes, marginTop(12));
        homeDestination.addView(hero, marginTop(16));

        if (faithModeStore.isEnabled()) {
            LinearLayout refugeCard = card();
            refugeCard.addView(tag("MODO FE OPCIONAL", PRIMARY, SURFACE_RAISED));
            refugeCard.addView(text("Refugio para este momento", 19, TEXT, Typeface.BOLD), marginTop(8));
            refugeCard.addView(
                    text(
                            "Filipenses 4:6 · Por nada estéis afanosos; sino sean notorias vuestras peticiones delante de Dios en toda oración y ruego, con hacimiento de gracias.",
                            14,
                            MUTED,
                            Typeface.NORMAL),
                    marginTop(5));
            Button openReader = button("ABRIR LECTURA", PRIMARY, BACKGROUND);
            openReader.setOnClickListener(view -> openBibleReader());
            refugeCard.addView(openReader, marginTop(12));
            homeDestination.addView(refugeCard, marginTop(12));
        }

        if (!isAccessibilityEnabled()) {
            LinearLayout accessNotice = card();
            accessNotice.setPadding(dp(16), dp(14), dp(16), dp(14));
            LinearLayout noticeHeader = row(Gravity.CENTER_VERTICAL);
            noticeHeader.addView(tag("ACCIÓN REQUERIDA", DANGER, BACKGROUND));
            noticeHeader.addView(text("Bloqueo de apps pendiente", 16, TEXT, Typeface.BOLD), weight());
            accessNotice.addView(noticeHeader);
            accessNotice.addView(text(
                    "Para aplicar reglas a aplicaciones, revisa y activa Accesibilidad desde los ajustes de Android. Esta app no puede encenderla por ti.",
                    13,
                    MUTED,
                    Typeface.NORMAL));
            Button openAccess = compactButton("REVISAR ACCESIBILIDAD", SURFACE_RAISED, TEXT);
            openAccess.setOnClickListener(view -> openAccessibilitySettings());
            accessNotice.addView(openAccess, marginTop(8));
            homeDestination.addView(accessNotice, marginTop(12));
        }

        LinearLayout metrics = row(Gravity.CENTER_VERTICAL);
        metrics.addView(metricCard("REGLAS ACTIVAS", "0", "en este dispositivo"), weightWithMargin(1, 0, 0, dp(8), 0));
        metrics.addView(metricCard("COBERTURA", "Revisar", "permisos y modos"), weightWithMargin(1, 0, 0, 0, 0));
        homeDestination.addView(metrics, marginTop(12));
        ruleCountView = (TextView) ((LinearLayout) metrics.getChildAt(0)).getChildAt(1);
        protectionStatusView = (TextView) ((LinearLayout) metrics.getChildAt(1)).getChildAt(1);

        LinearLayout ruleMap = card();
        ruleMap.setPadding(dp(16), dp(14), dp(16), dp(14));
        LinearLayout mapHeader = row(Gravity.CENTER_VERTICAL);
        mapHeader.addView(text("Tu mapa de reglas", 16, TEXT, Typeface.BOLD), weight());
        mapHeader.addView(tag("LOCAL", PRIMARY, BACKGROUND));
        ruleMap.addView(mapHeader);
        ruleMap.addView(text(
                "Cada contador proviene de reglas guardadas en este dispositivo; no representa actividad ni historial.",
                12,
                MUTED,
                Typeface.NORMAL));
        LinearLayout ruleKinds = row(Gravity.CENTER_VERTICAL);
        domainRuleCountView = compactMetric("DOMINIOS");
        appRuleCountView = compactMetric("APPS");
        keywordRuleCountView = compactMetric("PALABRAS");
        ruleKinds.addView(domainRuleCountView, weightWithMargin(1, 0, 0, dp(6), 0));
        ruleKinds.addView(appRuleCountView, weightWithMargin(1, 0, 0, dp(6), 0));
        ruleKinds.addView(keywordRuleCountView, weight());
        ruleMap.addView(ruleKinds, marginTop(10));
        homeDestination.addView(ruleMap, marginTop(12));

        LinearLayout session = card();
        LinearLayout sessionHeader = row(Gravity.CENTER_VERTICAL);
        LinearLayout sessionWords = column();
        sessionTitleView = text("Sesión de enfoque", 19, TEXT, Typeface.BOLD);
        sessionDetailView =
                text("Sin sesión activa. Elige un bloque de tiempo para tu intención.", 14, MUTED, Typeface.NORMAL);
        sessionWords.addView(sessionTitleView);
        sessionWords.addView(sessionDetailView);
        sessionHeader.addView(sessionWords, weight());
        sessionHeader.addView(tag("LOCAL", ACCENT, BACKGROUND));
        session.addView(sessionHeader);
        session.addView(text("Elige una duración", 12, MUTED, Typeface.BOLD), marginTop(10));
        LinearLayout durations = row(Gravity.CENTER_VERTICAL);
        durations.addView(sessionDurationButton("25 min", 25), weightWithMargin(1, 0, 0, dp(6), 0));
        durations.addView(sessionDurationButton("50 min", 50), weightWithMargin(1, 0, 0, dp(6), 0));
        durations.addView(sessionDurationButton("90 min", 90), weight());
        session.addView(durations, marginTop(4));
        sessionAction = button("INICIAR SESIÓN", PRIMARY, BACKGROUND);
        sessionAction.setOnClickListener(view -> showSessionChoices());
        session.addView(sessionAction, marginTop(14));
        session.addView(text(
                "El temporizador es privado. Las reglas de aplicaciones mantienen su propio estado y nunca inspeccionan texto ni URLs.",
                12,
                MUTED,
                Typeface.NORMAL));
        homeDestination.addView(session, marginTop(16));

        protectionDestination.addView(text("Protección del dispositivo", 21, TEXT, Typeface.BOLD), marginTop(20));
        protectionDestination.addView(text(
                "Activa el servicio solo si deseas bloquear paquetes Android. Android siempre pide esta aprobación manualmente.",
                14,
                MUTED,
                Typeface.NORMAL));
        Button accessibility = button("REVISAR ACCESIBILIDAD", SURFACE_RAISED, TEXT);
        accessibility.setOnClickListener(view -> openAccessibilitySettings());
        protectionDestination.addView(accessibility, marginTop(10));

        LinearLayout budgets = card();
        budgets.addView(text("Límites diarios", 19, TEXT, Typeface.BOLD));
        budgets.addView(text(
                "Configura un presupuesto local por aplicación. Solo se consulta el total de hoy mientras Acceso de uso y Accesibilidad estén activados; no se guarda ese total.",
                14,
                MUTED,
                Typeface.NORMAL));
        Button usageAccess = button("REVISAR ACCESO DE USO", SURFACE_RAISED, TEXT);
        usageAccess.setOnClickListener(view -> showUsageAccessDisclosure());
        budgets.addView(usageAccess, marginTop(10));
        budgetPickerButton = button("CONFIGURAR PRESUPUESTO", SURFACE_RAISED, TEXT);
        budgetPickerButton.setOnClickListener(view -> showBudgetAppPicker());
        budgets.addView(budgetPickerButton, marginTop(8));
        budgetsContainer = column();
        budgets.addView(budgetsContainer, marginTop(6));
        protectionDestination.addView(budgets, marginTop(12));

        LinearLayout coverage = card();
        coverage.addView(text("Cobertura web", 19, TEXT, Typeface.BOLD));
        coverage.addView(text(
                "El filtro DNS es opcional. Solo evalúa DNS UDP contra reglas locales; no inspecciona URLs ni contenido del navegador.",
                14,
                MUTED,
                Typeface.NORMAL));
        coverage.addView(
                text(
                        "Política incorporada: " + BundledDomainPolicy.VERSION
                                + " (fixture CC0 para pruebas; no es una lista web de producción).",
                        13,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(6));
        LinearLayout dnsStatusRow = row(Gravity.CENTER_VERTICAL);
        dnsPowerIcon = new ImageView(this);
        dnsPowerIcon.setImageResource(R.drawable.ic_power_status);
        dnsPowerIcon.setColorFilter(MUTED);
        dnsPowerIcon.setContentDescription("Filtro DNS detenido");
        dnsStatusRow.addView(dnsPowerIcon, size(dp(24), dp(24)));
        dnsStatusView = text("Filtro DNS detenido.", 13, MUTED, Typeface.NORMAL);
        dnsStatusView.setPadding(dp(8), 0, 0, 0);
        dnsStatusRow.addView(dnsStatusView, weight());
        coverage.addView(dnsStatusRow, marginTop(8));
        if (isThirdPartyVpnActive()) {
            coverage.addView(
                    text(
                            "VPN externa detectada: Android solo permite un túnel VPN activo. Proyect X no la reemplaza ni modifica su configuración.",
                            13,
                            DANGER,
                            Typeface.BOLD),
                    marginTop(8));
        }
        Button coverageDetail = button("VER COBERTURA Y LÍMITES", SURFACE_RAISED, TEXT);
        coverageDetail.setOnClickListener(view -> showCoverageDisclosure());
        coverage.addView(coverageDetail, marginTop(10));
        dnsAction = button("ACTIVAR FILTRO DNS", PRIMARY, BACKGROUND);
        dnsAction.setOnClickListener(view -> onDnsAction());
        coverage.addView(dnsAction, marginTop(8));
        Button vpnSettings = button("ABRIR AJUSTES DE VPN", SURFACE_RAISED, TEXT);
        vpnSettings.setOnClickListener(view -> showAlwaysOnDisclosure());
        coverage.addView(vpnSettings, marginTop(8));
        protectionDestination.addView(coverage, marginTop(12));

        protectionDestination.addView(text("Privacidad primero", 21, TEXT, Typeface.BOLD), marginTop(20));
        LinearLayout privacyCard = card();
        privacyCard.addView(text("Control visible, datos locales", 19, TEXT, Typeface.BOLD));
        privacyCard.addView(text(
                "Proyect X no crea una cuenta ni guarda un historial de tus consultas. Cada permiso conserva su alcance y puede revocarse desde Android.",
                14,
                MUTED,
                Typeface.NORMAL));
        Button privacyCoverage = button("REVISAR COBERTURA", SURFACE_RAISED, TEXT);
        privacyCoverage.setOnClickListener(view -> showCoverageDisclosure());
        privacyCard.addView(privacyCoverage, marginTop(12));
        Button privacyVpn = button("REVISAR AJUSTES DE VPN", SURFACE_RAISED, TEXT);
        privacyVpn.setOnClickListener(view -> showAlwaysOnDisclosure());
        privacyCard.addView(privacyVpn, marginTop(8));
        protectionDestination.addView(privacyCard, marginTop(12));

        recoveryDestination.addView(text("Reinicio con calma", 21, TEXT, Typeface.BOLD), marginTop(20));
        LinearLayout recoveryCard = card();
        recoveryCard.addView(text("Vuelve a elegir tu siguiente paso", 19, TEXT, Typeface.BOLD));
        recoveryCard.addView(text(
                "Un reinicio cierra la sesión actual y registra una nota local. Tus reglas, permisos y protección permanecen como están.",
                14,
                MUTED,
                Typeface.NORMAL));
        TextView recoverySummary = text(recoveryStore.summary(), 13, MUTED, Typeface.NORMAL);
        recoveryCard.addView(recoverySummary, marginTop(10));
        Button pauseRecovery = button("NECESITO UNA PAUSA", DANGER, Color.WHITE);
        pauseRecovery.setOnClickListener(view -> showRecoveryPrompt("Pausa consciente"));
        recoveryCard.addView(pauseRecovery, marginTop(14));
        Button planRecovery = button("CAMBIAR MI PLAN", SURFACE_RAISED, TEXT);
        planRecovery.setOnClickListener(view -> showRecoveryPrompt("Cambio de plan"));
        recoveryCard.addView(planRecovery, marginTop(8));
        recoveryDestination.addView(recoveryCard, marginTop(12));

        HabitStreak streak = habitStreakStore.read();
        LinearLayout rhythmCard = card();
        rhythmCard.addView(text("Ritmo de enfoque", 19, TEXT, Typeface.BOLD));
        rhythmCard.addView(text(
                streak.currentDays() == 0
                        ? "Inicia una sesión cuando te sirva: el primer día crea tu racha local."
                        : streak.currentDays()
                                + (streak.currentDays() == 1 ? " día seguido" : " días seguidos")
                                + " con una sesión de enfoque iniciada.",
                14,
                MUTED,
                Typeface.NORMAL));
        rhythmCard.addView(
                text(
                        "Mejor racha local: " + streak.bestDays() + (streak.bestDays() == 1 ? " día." : " días."),
                        13,
                        PRIMARY,
                        Typeface.BOLD),
                marginTop(8));
        rhythmCard.addView(
                text(
                        "Consejo: elige una sesión breve y protege sólo tu siguiente bloque de atención.",
                        13,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(8));
        recoveryDestination.addView(rhythmCard, marginTop(12));

        LinearLayout composer = card();
        composer.addView(text("Nueva regla", 19, TEXT, Typeface.BOLD));
        composer.addView(text(
                "Permitir tiene prioridad sobre Bloquear para conservar excepciones específicas.",
                13,
                MUTED,
                Typeface.NORMAL));
        composer.addView(label("TIPO"));
        typeSelector = spinner(new String[] {"Dominio", "Palabra clave", "Paquete Android"});
        composer.addView(typeSelector);
        composer.addView(label("ACCIÓN"));
        actionSelector = spinner(new String[] {"Bloquear", "Permitir"});
        composer.addView(actionSelector);
        composer.addView(label("VALOR"));
        valueInput = new EditText(this);
        valueInput.setHint("ejemplo.com");
        valueInput.setHintTextColor(MUTED);
        valueInput.setTextColor(TEXT);
        valueInput.setTextSize(16);
        valueInput.setSingleLine(true);
        valueInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        valueInput.setPadding(dp(14), dp(10), dp(14), dp(10));
        valueInput.setBackground(outline(SURFACE_RAISED, ACCENT, 12));
        typeSelector.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateRuleInputMode();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        composer.addView(valueInput, marginTop(2));
        appPickerButton = button(getString(R.string.installed_app_picker), SURFACE_RAISED, TEXT);
        appPickerButton.setOnClickListener(view -> showInstalledAppPicker());
        composer.addView(appPickerButton, marginTop(8));
        updateRuleInputMode();
        composer.addView(label("HORARIO"));
        scheduleButton = button("ELEGIR HORARIO", SURFACE_RAISED, TEXT);
        scheduleButton.setOnClickListener(view -> showScheduleChoices());
        composer.addView(scheduleButton);
        scheduleDetailView = text("Siempre · la regla se aplica a toda hora.", 13, MUTED, Typeface.NORMAL);
        composer.addView(scheduleDetailView, marginTop(4));
        updateScheduleSummary();
        Button add = button("GUARDAR REGLA", PRIMARY, BACKGROUND);
        add.setOnClickListener(view -> addRule());
        composer.addView(add, marginTop(14));
        rulesDestination.addView(composer, marginTop(20));

        rulesDestination.addView(text("Tus reglas", 20, TEXT, Typeface.BOLD), marginTop(22));
        ruleBreakdownView = text("", 13, MUTED, Typeface.NORMAL);
        rulesDestination.addView(ruleBreakdownView, marginTop(2));
        rulesContainer = column();
        rulesDestination.addView(rulesContainer);
        showDestination(homeDestination, protectionDestination, rulesDestination, recoveryDestination);

        LinearLayout shell = column();
        shell.setBackgroundColor(BACKGROUND);
        shell.addView(scroll, fillWeight());
        shell.addView(destinationNavigation());
        setContentView(shell);
        refreshDashboard();
    }

    private void refreshDashboard() {
        List<Rule> rules = ruleStore.list();
        ruleCountView.setText(String.valueOf(rules.size()));
        int domains = 0;
        int apps = 0;
        int keywords = 0;
        for (Rule rule : rules) {
            if (!rule.enabled()) continue;
            if (rule.type() == Rule.Type.DOMAIN) domains++;
            else if (rule.type() == Rule.Type.APP) apps++;
            else keywords++;
        }
        if (domainRuleCountView != null) domainRuleCountView.setText("DOMINIOS\n" + domains);
        if (appRuleCountView != null) appRuleCountView.setText("APPS\n" + apps);
        if (keywordRuleCountView != null) keywordRuleCountView.setText("PALABRAS\n" + keywords);
        if (ruleBreakdownView != null) {
            ruleBreakdownView.setText(domains + " dominios · " + apps + " apps · " + keywords + " palabras activas");
        }
        boolean enabled = isAccessibilityEnabled();
        protectionStatusView.setText(enabled ? "Activo" : "Pendiente");
        protectionStatusView.setTextColor(enabled ? PRIMARY : ACCENT);
        renderRules(rules);
        renderBudgets(budgetStore.list());
        refreshDnsStatus();
        refreshSession();
    }

    private void refreshDnsStatus() {
        if (dnsStatusView == null || dnsAction == null) return;
        if (!isThirdPartyVpnActive() && DnsVpnService.recoverIfNeeded(this)) {
            refreshHandler.postDelayed(this::refreshDnsStatus, 750L);
        }
        DnsVpnService.Status status = DnsVpnService.readStatus(this);
        boolean active = DnsVpnService.STATE_ACTIVE.equals(status.state())
                || DnsVpnService.STATE_STARTING.equals(status.state());
        boolean attention =
                DnsVpnService.STATE_ERROR.equals(status.state()) || DnsVpnService.STATE_REVOKED.equals(status.state());
        dnsStatusView.setText(status.detail());
        dnsStatusView.setTextColor(active ? PRIMARY : attention ? DANGER : MUTED);
        if (dnsPowerIcon != null) {
            int statusColor = active ? PRIMARY : attention ? DANGER : MUTED;
            dnsPowerIcon.setColorFilter(statusColor);
            dnsPowerIcon.setContentDescription(
                    active ? "Filtro DNS activo" : attention ? "Filtro DNS necesita atención" : "Filtro DNS detenido");
        }
        dnsAction.setText(active ? "DETENER FILTRO DNS" : attention ? "REINTENTAR FILTRO DNS" : "ACTIVAR FILTRO DNS");
        dnsAction.setBackground(round(active ? SURFACE_RAISED : PRIMARY, 14));
        dnsAction.setTextColor(active ? DANGER : Color.WHITE);
    }

    private void refreshSession() {
        FocusSession current = sessionStore.current(System.currentTimeMillis());
        if (current == null) {
            sessionTitleView.setText(R.string.session_idle_title);
            sessionDetailView.setText(R.string.session_idle_detail);
            sessionAction.setText(R.string.session_start);
            sessionAction.setOnClickListener(view -> showSessionChoices());
            return;
        }
        long remaining = current.remainingMillis(System.currentTimeMillis());
        long minutes = Math.max(1L, (remaining + 59_999L) / 60_000L);
        sessionTitleView.setText(R.string.session_active_title);
        sessionDetailView.setText(getString(R.string.session_remaining, minutes));
        sessionAction.setText(R.string.session_finish);
        sessionAction.setOnClickListener(view -> {
            sessionStore.clear();
            refreshSession();
        });
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, 30_000L);
    }

    private void showSessionChoices() {
        String[] choices = {"25 minutos", "50 minutos", "90 minutos"};
        int[] durations = {25, 50, 90};
        new AlertDialog.Builder(this)
                .setTitle("¿Cuánto tiempo quieres proteger?")
                .setItems(choices, (dialog, selected) -> startFocusSession(durations[selected]))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void startFocusSession(int durationMinutes) {
        try {
            long nowMillis = System.currentTimeMillis();
            sessionStore.start(durationMinutes, nowMillis);
            try {
                habitStreakStore.recordFocusStart(nowMillis);
            } catch (IllegalStateException ignored) {
                // The voluntary session remains valid even if optional motivation state cannot persist.
            }
            refreshSession();
            Toast.makeText(this, "Sesión local iniciada.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showRecoveryPrompt(String reason) {
        new AlertDialog.Builder(this)
                .setTitle("¿Reiniciar el plan?")
                .setMessage(
                        "Se cerrará la sesión de enfoque actual y se guardará este reinicio solo en el dispositivo. Tus reglas, permisos y filtro DNS no cambiarán.")
                .setNegativeButton("Volver", null)
                .setPositiveButton("REINICIAR", (dialog, which) -> completeRecovery(reason))
                .show();
    }

    private void completeRecovery(String reason) {
        try {
            recoveryStore.record(reason);
            sessionStore.clear();
            activeDestination = "inicio";
            render();
            Toast.makeText(this, "Plan reiniciado sin desactivar tus barreras.", Toast.LENGTH_LONG)
                    .show();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addRule() {
        try {
            Rule.Type type = switch (typeSelector.getSelectedItemPosition()) {
                case 0 -> Rule.Type.DOMAIN;
                case 1 -> Rule.Type.KEYWORD;
                default -> Rule.Type.APP;
            };
            Rule.Action action = actionSelector.getSelectedItemPosition() == 0 ? Rule.Action.BLOCK : Rule.Action.ALLOW;
            Rule rule = Rule.create(type, action, valueInput.getText().toString(), draftSchedule);
            if (!ruleStore.save(rule)) throw new IllegalStateException("No se pudo guardar la regla local.");
            valueInput.setText("");
            draftSchedule = ScheduleWindow.always();
            updateScheduleSummary();
            refreshDashboard();
            Toast.makeText(this, "Regla guardada en este dispositivo.", Toast.LENGTH_SHORT)
                    .show();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void renderRules(List<Rule> rules) {
        rulesContainer.removeAllViews();
        if (rules.isEmpty()) {
            LinearLayout empty = card();
            empty.addView(text("Aún no hay reglas", 17, TEXT, Typeface.BOLD));
            empty.addView(text(
                    "Crea la primera para empezar. Una excepción Permitir vence a un bloqueo más general.",
                    14,
                    MUTED,
                    Typeface.NORMAL));
            rulesContainer.addView(empty, marginTop(8));
            return;
        }
        for (Rule rule : rules) {
            LinearLayout item = card();
            LinearLayout top = row(Gravity.CENTER_VERTICAL);
            int actionColor = rule.action() == Rule.Action.BLOCK ? DANGER : PRIMARY;
            top.addView(tag(rule.action() == Rule.Action.BLOCK ? "BLOQUEAR" : "PERMITIR", actionColor, SURFACE_RAISED));
            TextView category = text(
                    rule.type() == Rule.Type.APP
                            ? "PAQUETE ANDROID"
                            : rule.type() == Rule.Type.DOMAIN ? "DOMINIO" : "PALABRA CLAVE",
                    11,
                    MUTED,
                    Typeface.BOLD);
            category.setGravity(Gravity.END);
            top.addView(category, weight());
            Switch enabled = new Switch(this);
            enabled.setText(rule.enabled() ? "ACTIVA" : "PAUSADA");
            enabled.setTextSize(11);
            enabled.setTextColor(rule.enabled() ? PRIMARY : MUTED);
            enabled.setChecked(rule.enabled());
            enabled.setContentDescription("Regla " + rule.value() + (rule.enabled() ? " activa" : " pausada"));
            enabled.setOnCheckedChangeListener((button, checked) -> {
                if (checked == rule.enabled()) return;
                if (!ruleStore.setEnabled(rule.id(), checked)) {
                    Toast.makeText(this, "No se pudo actualizar la regla local.", Toast.LENGTH_LONG)
                            .show();
                }
                refreshDashboard();
            });
            top.addView(enabled);
            item.addView(top);
            if (!rule.enabled()) item.setAlpha(0.66f);
            item.addView(text(rule.value(), 18, TEXT, Typeface.BOLD));
            item.addView(text(rule.schedule().toDisplayText(), 13, MUTED, Typeface.NORMAL));
            Button remove = button("QUITAR", SURFACE_RAISED, DANGER);
            remove.setOnClickListener(view -> {
                ruleStore.remove(rule.id());
                refreshDashboard();
            });
            item.addView(remove, marginTop(8));
            rulesContainer.addView(item, marginTop(8));
        }
    }

    private void updateRuleInputMode() {
        if (typeSelector == null || valueInput == null) return;
        boolean packageRule = typeSelector.getSelectedItemPosition() == 2;
        valueInput.setHint(
                packageRule
                        ? "com.ejemplo.app"
                        : typeSelector.getSelectedItemPosition() == 0 ? "ejemplo.com" : "una frase");
        if (appPickerButton != null) appPickerButton.setVisibility(packageRule ? View.VISIBLE : View.GONE);
    }

    private void renderBudgets(List<DailyBudget> budgets) {
        if (budgetsContainer == null) return;
        budgetsContainer.removeAllViews();
        if (budgets.isEmpty()) {
            budgetsContainer.addView(text(
                    "Aún no hay presupuestos. Configurarlos no activa permisos ni bloqueos por sí solo.",
                    13,
                    MUTED,
                    Typeface.NORMAL));
            return;
        }
        for (DailyBudget budget : budgets) {
            LinearLayout item = column();
            item.setPadding(0, dp(8), 0, 0);
            item.addView(text(budget.packageName(), 14, TEXT, Typeface.BOLD));
            item.addView(text(
                    budget.limitMinutes() + " minutos por día · se aplica solo con ambas autorizaciones activas",
                    12,
                    MUTED,
                    Typeface.NORMAL));
            Button remove = button("QUITAR PRESUPUESTO", SURFACE_RAISED, DANGER);
            remove.setOnClickListener(view -> {
                budgetStore.remove(budget.packageName());
                refreshDashboard();
            });
            item.addView(remove, marginTop(5));
            budgetsContainer.addView(item);
        }
    }

    private void showBudgetAppPicker() {
        budgetPickerButton.setEnabled(false);
        new Thread(() -> {
                    final List<InstalledApp> apps;
                    try {
                        apps = InstalledAppRepository.listLaunchable(getApplicationContext());
                    } catch (RuntimeException exception) {
                        runOnUiThread(() -> {
                            budgetPickerButton.setEnabled(true);
                            Toast.makeText(
                                            this,
                                            "No se pudieron consultar las aplicaciones visibles.",
                                            Toast.LENGTH_LONG)
                                    .show();
                        });
                        return;
                    }
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        budgetPickerButton.setEnabled(true);
                        if (apps.isEmpty()) {
                            Toast.makeText(
                                            this,
                                            "Android no expuso aplicaciones con icono de inicio.",
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        CharSequence[] choices = new CharSequence[apps.size()];
                        for (int index = 0; index < apps.size(); index++) {
                            InstalledApp app = apps.get(index);
                            choices[index] = app.label() + "\n" + app.packageName();
                        }
                        new AlertDialog.Builder(this)
                                .setTitle("Elegir aplicación para presupuesto")
                                .setItems(choices, (dialog, which) -> showBudgetLimitDialog(apps.get(which)))
                                .setNegativeButton("Cancelar", null)
                                .show();
                    });
                })
                .start();
    }

    private void showBudgetLimitDialog(InstalledApp app) {
        EditText minutes = new EditText(this);
        minutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        minutes.setHint("Minutos por día (1–1440)");
        minutes.setHintTextColor(MUTED);
        minutes.setTextColor(TEXT);
        minutes.setSingleLine(true);
        minutes.setPadding(dp(18), dp(8), dp(18), dp(8));
        minutes.setBackground(outline(SURFACE_RAISED, ACCENT, 12));
        DailyBudget existing = budgetStore.find(app.packageName());
        if (existing != null) minutes.setText(String.valueOf(existing.limitMinutes()));
        new AlertDialog.Builder(this)
                .setTitle("Presupuesto para " + app.label())
                .setMessage(
                        "El total de uso se consulta solo hoy, en este dispositivo, cuando habilites Acceso de uso y Accesibilidad. No se conserva historial.")
                .setView(minutes)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("GUARDAR", (dialog, which) -> {
                    try {
                        int limit =
                                Integer.parseInt(minutes.getText().toString().trim());
                        if (!budgetStore.save(new DailyBudget(app.packageName(), limit)))
                            throw new IllegalStateException("No se pudo guardar el presupuesto local.");
                        refreshDashboard();
                        Toast.makeText(this, "Presupuesto guardado en este dispositivo.", Toast.LENGTH_SHORT)
                                .show();
                    } catch (IllegalArgumentException exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .show();
    }

    private void showInstalledAppPicker() {
        appPickerButton.setEnabled(false);
        appPickerButton.setText(R.string.installed_app_picker_loading);
        new Thread(() -> {
                    final List<InstalledApp> apps;
                    try {
                        apps = InstalledAppRepository.listLaunchable(getApplicationContext());
                    } catch (RuntimeException exception) {
                        runOnUiThread(() -> restoreAppPickerButton("No se pudo consultar las aplicaciones visibles."));
                        return;
                    }
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        restoreAppPickerButton(null);
                        if (apps.isEmpty()) {
                            Toast.makeText(
                                            this,
                                            "Android no expuso aplicaciones con icono de inicio.",
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }
                        showInstalledAppResults(apps);
                    });
                })
                .start();
    }

    private void restoreAppPickerButton(String error) {
        if (appPickerButton != null) {
            appPickerButton.setEnabled(true);
            appPickerButton.setText(R.string.installed_app_picker);
        }
        if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void showInstalledAppResults(List<InstalledApp> apps) {
        LinearLayout content = column();
        content.setPadding(dp(8), 0, dp(8), 0);
        TextView privacy = text(
                "La lista se consulta solo para elegir una app. No se guarda ni se envía.", 13, MUTED, Typeface.NORMAL);
        content.addView(privacy);
        EditText search = new EditText(this);
        search.setHint("Buscar por nombre o paquete");
        search.setHintTextColor(MUTED);
        search.setTextColor(TEXT);
        search.setSingleLine(true);
        search.setPadding(dp(12), dp(8), dp(12), dp(8));
        search.setBackground(outline(SURFACE_RAISED, ACCENT, 12));
        content.addView(search, marginTop(8));

        ArrayList<InstalledApp> visibleApps = new ArrayList<>(apps);
        ListView list = new ListView(this);
        ArrayAdapter<InstalledApp> adapter = new ArrayAdapter<>(this, 0, visibleApps) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                InstalledApp app = getItem(position);
                LinearLayout row = row(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(8), dp(8), dp(8), dp(8));
                ImageView icon = new ImageView(MainActivity.this);
                try {
                    Drawable drawable = getPackageManager().getApplicationIcon(app.packageName());
                    icon.setImageDrawable(drawable);
                } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {
                    icon.setImageDrawable(null);
                }
                row.addView(icon, size(dp(38), dp(38)));
                LinearLayout words = column();
                words.setPadding(dp(12), 0, 0, 0);
                words.addView(text(app.label(), 16, TEXT, Typeface.BOLD));
                words.addView(text(app.packageName(), 12, MUTED, Typeface.NORMAL));
                row.addView(words, weight());
                return row;
            }
        };
        list.setAdapter(adapter);
        content.addView(list, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(330)));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Elegir aplicación")
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .create();
        list.setOnItemClickListener((parent, view, position, id) -> {
            InstalledApp selected = visibleApps.get(position);
            valueInput.setText(selected.packageName());
            dialog.dismiss();
            Toast.makeText(this, selected.label() + " seleccionada.", Toast.LENGTH_SHORT)
                    .show();
        });
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                visibleApps.clear();
                for (InstalledApp app : apps) if (app.matches(text.toString())) visibleApps.add(app);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable text) {}
        });
        dialog.show();
    }

    private void showScheduleChoices() {
        String[] choices = {"Siempre", "Horario personalizado"};
        new AlertDialog.Builder(this)
                .setTitle("¿Cuándo se aplica esta regla?")
                .setSingleChoiceItems(choices, draftSchedule.isAlways() ? 0 : 1, (dialog, selected) -> {
                    dialog.dismiss();
                    if (selected == 0) {
                        draftSchedule = ScheduleWindow.always();
                        updateScheduleSummary();
                    } else {
                        showDayPicker();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDayPicker() {
        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        boolean[] checked = new boolean[7];
        int selectedMask = draftSchedule.isAlways() ? ScheduleWindow.ALL_DAYS : draftSchedule.getDayMask();
        for (int day = 0; day < checked.length; day++) checked[day] = (selectedMask & (1 << day)) != 0;

        new AlertDialog.Builder(this)
                .setTitle("Elige los días")
                .setMultiChoiceItems(days, checked, (dialog, which, selected) -> checked[which] = selected)
                .setPositiveButton("CONTINUAR", (dialog, ignored) -> {
                    int dayMask = 0;
                    for (int day = 0; day < checked.length; day++) if (checked[day]) dayMask |= 1 << day;
                    if (dayMask == 0) {
                        Toast.makeText(this, "Selecciona al menos un día.", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    showStartTimePicker(dayMask);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showStartTimePicker(int dayMask) {
        int start = draftSchedule.isAlways() ? 9 * 60 : draftSchedule.getStartMinute();
        new TimePickerDialog(
                        this,
                        (view, hour, minute) -> showEndTimePicker(dayMask, hour * 60 + minute),
                        start / 60,
                        start % 60,
                        true)
                .show();
    }

    private void showEndTimePicker(int dayMask, int startMinute) {
        int end = draftSchedule.isAlways() ? 17 * 60 : draftSchedule.getEndMinute();
        new TimePickerDialog(
                        this,
                        (view, hour, minute) -> {
                            try {
                                draftSchedule = ScheduleWindow.of(dayMask, startMinute, hour * 60 + minute);
                                updateScheduleSummary();
                            } catch (IllegalArgumentException exception) {
                                Toast.makeText(this, "El inicio y el final deben ser diferentes.", Toast.LENGTH_LONG)
                                        .show();
                            }
                        },
                        end / 60,
                        end % 60,
                        true)
                .show();
    }

    private void updateScheduleSummary() {
        if (scheduleButton == null || scheduleDetailView == null) return;
        scheduleButton.setText(draftSchedule.isAlways() ? "SIEMPRE" : "EDITAR HORARIO");
        scheduleDetailView.setText(
                draftSchedule.isAlways()
                        ? "Siempre · la regla se aplica a toda hora."
                        : draftSchedule.toDisplayText() + " · Los rangos pueden cruzar medianoche.");
    }

    private boolean isAccessibilityEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        List<AccessibilityServiceInfo> services =
                manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo service : services) {
            if (service.getResolveInfo() == null || service.getResolveInfo().serviceInfo == null) continue;
            if (getPackageName().equals(service.getResolveInfo().serviceInfo.packageName)
                    && AppBlockAccessibilityService.class.getName().equals(service.getResolveInfo().serviceInfo.name))
                return true;
        }
        return false;
    }

    /** Detects an active VPN transport only to explain the conflict; it does not inspect its traffic or settings. */
    private boolean isThirdPartyVpnActive() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;
        Network active = manager.getActiveNetwork();
        NetworkCapabilities capabilities = active == null ? null : manager.getNetworkCapabilities(active);
        return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    }

    private void openAccessibilitySettings() {
        try {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } catch (RuntimeException exception) {
            Toast.makeText(this, "No se pudieron abrir los ajustes de accesibilidad.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void showUsageAccessDisclosure() {
        new AlertDialog.Builder(this)
                .setTitle("Acceso de uso: decisión tuya")
                .setMessage(
                        "Para aplicar un límite diario, Android puede entregar a Proyect X los totales de tiempo de uso por aplicación. "
                                + "Se usarían solo en este dispositivo para comparar cada total con el presupuesto que elijas. "
                                + "No se enviarán, no se usarán para analítica y puedes revocar el acceso en Ajustes cuando quieras. "
                                + "Esta pantalla no inicia ninguna lectura: solo un presupuesto que configures, junto con Accesibilidad activa, puede consultar el total del día.")
                .setNegativeButton("Ahora no", null)
                .setPositiveButton("ABRIR AJUSTES", (dialog, which) -> openUsageAccessSettings())
                .show();
    }

    private void openUsageAccessSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } catch (RuntimeException exception) {
            Toast.makeText(this, "No se pudieron abrir los ajustes de acceso de uso.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void showCoverageDisclosure() {
        new AlertDialog.Builder(this)
                .setTitle("Cobertura web: estado honesto")
                .setMessage(
                        "El filtro DNS opcional evalúa solo consultas DNS UDP y aplica tus reglas de dominio locales. Los dominios bloqueados reciben una respuesta local; las demás consultas se reenvían a 1.1.1.1:53 mediante un socket protegido para que no entre en el propio túnel. Proyect X no guarda consultas, pero ese resolvedor puede recibirlas según su propia política.\n\n"
                                + "No clasifica URLs ni contenido TLS cifrado y no cubre IP directas, DoH/DoT, QUIC, DNS TCP, resolutores propios ni una VPN de terceros. Android permite un solo túnel VPN activo.\n\n"
                                + "Nunca se presentará esta capa como infalible. Puedes detenerla desde esta app o desde Ajustes de Android.")
                .setPositiveButton("ENTENDIDO", null)
                .show();
    }

    private void onDnsAction() {
        DnsVpnService.Status status = DnsVpnService.readStatus(this);
        if (DnsVpnService.STATE_ACTIVE.equals(status.state()) || DnsVpnService.STATE_STARTING.equals(status.state())) {
            DnsVpnService.killSwitch(this);
            refreshDnsStatus();
            return;
        }
        if (isThirdPartyVpnActive()) {
            Toast.makeText(
                            this,
                            "Desactiva primero la VPN externa: Android permite un solo túnel VPN.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Activar filtro DNS local")
                .setMessage(
                        "Android mostrará su propio consentimiento de VPN. La VPN no es un servidor remoto: solo redirige DNS UDP a un resolvedor local. Las consultas permitidas saldrán a 1.1.1.1:53; Proyect X no las registra. Puedes detenerlo desde la notificación o esta pantalla.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("CONTINUAR", (dialog, which) -> requestDnsNotificationThenConsent())
                .show();
    }

    private void requestDnsNotificationThenConsent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, DNS_NOTIFICATION_REQUEST);
            return;
        }
        requestDnsVpnConsent();
    }

    private void requestDnsVpnConsent() {
        Intent consent = VpnService.prepare(this);
        if (consent == null) {
            startDnsService(DnsVpnService.ACTION_START);
        } else {
            startActivityForResult(consent, DNS_VPN_CONSENT_REQUEST);
        }
    }

    private void startDnsService(String action) {
        Intent intent = new Intent(this, DnsVpnService.class).setAction(action);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
            // Startup is asynchronous. Refresh now and once more after Android establishes or stops
            // the tunnel, so the action never advertises the stale opposite state.
            refreshDnsStatus();
            refreshHandler.postDelayed(this::refreshDnsStatus, 750L);
        } catch (RuntimeException exception) {
            Toast.makeText(this, "No se pudo cambiar el estado del filtro DNS.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    @Deprecated
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RULE_EXPORT_REQUEST) {
            writeRulesExport(resultCode, data);
            return;
        }
        if (requestCode != DNS_VPN_CONSENT_REQUEST) return;
        if (resultCode == RESULT_OK) {
            startDnsService(DnsVpnService.ACTION_START);
        } else {
            Toast.makeText(this, "No se activó el filtro DNS porque Android no concedió la VPN.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != DNS_NOTIFICATION_REQUEST) return;
        boolean granted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            requestDnsVpnConsent();
        } else {
            Toast.makeText(this, "Se necesita una notificación visible para activar el filtro DNS.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void showAlwaysOnDisclosure() {
        new AlertDialog.Builder(this)
                .setTitle("VPN always-on: decisión de Android")
                .setMessage(
                        "Proyect X no activa always-on ni ‘bloquear conexiones sin VPN’ por ti. Esas opciones viven en Ajustes de Android y podrían dejarte sin conexión si la VPN falla. Esta versión no declara soporte always-on hasta completar pruebas de reinicio, red y recuperación.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("ABRIR AJUSTES", (dialog, which) -> openVpnSettings())
                .show();
    }

    private void openVpnSettings() {
        try {
            String action = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    ? Settings.ACTION_VPN_SETTINGS
                    : Settings.ACTION_WIRELESS_SETTINGS;
            startActivity(new Intent(action));
        } catch (RuntimeException exception) {
            Toast.makeText(this, "No se pudieron abrir los ajustes de VPN.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    /** Opens a local-only action menu. No item sends data without a user-selected share target. */
    private void showControlMenu(View anchor) {
        LinearLayout content = column();
        content.setPadding(dp(16), dp(16), dp(16), dp(16));
        content.setBackground(round(SURFACE, 20));
        content.addView(text("Centro local", 20, TEXT, Typeface.BOLD));
        content.addView(text("Herramientas del dispositivo", 12, MUTED, Typeface.NORMAL));

        PopupWindow popup = new PopupWindow(content, dp(330), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(10));

        Button export = menuAction("EXPORTAR REGLAS", "Guarda una copia JSON donde tú elijas");
        export.setOnClickListener(view -> {
            popup.dismiss();
            createRulesDocument();
        });
        content.addView(export, marginTop(12));

        Button share = menuAction("COMPARTIR REGLAS", "Abre el selector de Android; nada se envía automáticamente");
        share.setOnClickListener(view -> {
            popup.dismiss();
            confirmRuleShare();
        });
        content.addView(share, marginTop(6));

        Button privacy = menuAction("PRIVACIDAD Y LÍMITES", "Qué queda local y qué no cubre cada barrera");
        privacy.setOnClickListener(view -> {
            popup.dismiss();
            showPrivacySummary();
        });
        content.addView(privacy, marginTop(6));

        Button faithMode = menuAction("MODO FE / LECTURA BÍBLICA", "Activa o pausa el lector local RV1909");
        faithMode.setOnClickListener(view -> {
            popup.dismiss();
            showFaithModeSettings();
        });
        content.addView(faithMode, marginTop(6));

        Button help = menuAction("AYUDA", "Permisos, reglas y cobertura explicados");
        help.setOnClickListener(view -> {
            popup.dismiss();
            showHelpSummary();
        });
        content.addView(help, marginTop(6));

        Button language = menuAction("IDIOMA", "La app sigue el idioma del dispositivo cuando haya traducciones");
        language.setOnClickListener(view -> {
            popup.dismiss();
            showLanguageSettings();
        });
        content.addView(language, marginTop(6));

        Button feedback = menuAction("COMPARTIR OPINIÓN", "Tú eliges si quieres abrir el selector de Android");
        feedback.setOnClickListener(view -> {
            popup.dismiss();
            showFeedbackComposer();
        });
        content.addView(feedback, marginTop(6));
        popup.showAsDropDown(anchor, -dp(286), dp(8));
    }

    private void showFaithModeSettings() {
        LinearLayout content = column();
        content.setPadding(dp(8), dp(4), dp(8), dp(4));
        LinearLayout top = row(Gravity.CENTER_VERTICAL);
        LinearLayout words = column();
        words.addView(text("Modo Fe / Lectura Bíblica (Opcional)", 18, TEXT, Typeface.BOLD));
        words.addView(text(
                "Habilita Refugio y los versículos RV1909 locales. No cambia las barreras ni envía lecturas.",
                13,
                MUTED,
                Typeface.NORMAL));
        top.addView(words, weight());
        Switch enabled = new Switch(this);
        enabled.setChecked(faithModeStore.isEnabled());
        enabled.setContentDescription("Modo Fe / Lectura Bíblica (Opcional)");
        top.addView(enabled);
        content.addView(top);
        content.addView(
                text(
                        "Fuente: Reina-Valera 1909, incluida en este dispositivo como texto de dominio público.",
                        12,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(12));
        new AlertDialog.Builder(this)
                .setTitle("Ajustes locales")
                .setView(content)
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton("GUARDAR", (dialog, which) -> {
                    if (!faithModeStore.setEnabled(enabled.isChecked())) {
                        Toast.makeText(this, "No se pudo guardar el ajuste local.", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    render();
                    Toast.makeText(
                                    this,
                                    enabled.isChecked()
                                            ? "Lectura bíblica opcional activada."
                                            : "Lectura bíblica opcional pausada.",
                                    Toast.LENGTH_SHORT)
                            .show();
                })
                .show();
    }

    private void openBibleReader() {
        if (!faithModeStore.isEnabled()) {
            showFaithModeSettings();
            return;
        }
        startActivity(BibleReaderActivity.intent(this));
    }

    private Button menuAction(String title, String detail) {
        Button action = button(title + "\n" + detail, SURFACE_RAISED, TEXT);
        action.setAllCaps(false);
        action.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        action.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        action.setTextSize(13);
        action.setContentDescription(title + ". " + detail);
        return action;
    }

    private void createRulesDocument() {
        try {
            pendingRulesExport = rulesExportJson();
            Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("application/json")
                    .putExtra(Intent.EXTRA_TITLE, "proyect-x-reglas.json");
            startActivityForResult(create, RULE_EXPORT_REQUEST);
        } catch (IllegalStateException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void writeRulesExport(int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null || data.getData() == null || pendingRulesExport == null) {
            pendingRulesExport = null;
            return;
        }
        Uri destination = data.getData();
        try (OutputStream stream = getContentResolver().openOutputStream(destination, "w")) {
            if (stream == null) throw new IOException("Android no abrió el archivo elegido.");
            stream.write(pendingRulesExport.getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, "Reglas exportadas en el archivo que elegiste.", Toast.LENGTH_LONG)
                    .show();
        } catch (IOException | SecurityException exception) {
            Toast.makeText(this, "No se pudieron exportar las reglas locales.", Toast.LENGTH_LONG)
                    .show();
        } finally {
            pendingRulesExport = null;
        }
    }

    private void confirmRuleShare() {
        new AlertDialog.Builder(this)
                .setTitle("Compartir reglas locales")
                .setMessage(
                        "El selector de Android recibirá una copia de tus reglas, incluidas sus excepciones y horarios. Proyect X no conserva a quién eliges ni envía nada por su cuenta.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("ABRIR SELECTOR", (dialog, which) -> shareRules())
                .show();
    }

    private void shareRules() {
        try {
            Intent share = new Intent(Intent.ACTION_SEND)
                    .setType("application/json")
                    .putExtra(Intent.EXTRA_SUBJECT, "Reglas locales de Proyect X")
                    .putExtra(Intent.EXTRA_TEXT, rulesExportJson());
            startActivity(Intent.createChooser(share, "Compartir reglas"));
        } catch (IllegalStateException | android.content.ActivityNotFoundException exception) {
            Toast.makeText(this, "No hay una aplicación disponible para compartir.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private String rulesExportJson() {
        try {
            JSONArray rules = new JSONArray();
            for (Rule rule : ruleStore.list()) {
                JSONObject item = new JSONObject();
                item.put("id", rule.id());
                item.put("type", rule.type().name());
                item.put("action", rule.action().name());
                item.put("value", rule.value());
                item.put("enabled", rule.enabled());
                JSONObject schedule = new JSONObject();
                schedule.put("always", rule.schedule().isAlways());
                schedule.put("days", rule.schedule().getDayMask());
                schedule.put("startMinute", rule.schedule().getStartMinute());
                schedule.put("endMinute", rule.schedule().getEndMinute());
                item.put("schedule", schedule);
                rules.put(item);
            }
            JSONObject document = new JSONObject();
            document.put("schema", "proyect-x-rules-v1");
            document.put("rules", rules);
            return document.toString(2);
        } catch (JSONException exception) {
            throw new IllegalStateException("No se pudo preparar la exportación local.", exception);
        }
    }

    private void showPrivacySummary() {
        new AlertDialog.Builder(this)
                .setTitle("Privacidad y límites")
                .setMessage(
                        "Reglas, sesiones y reinicios se guardan localmente. No hay cuenta, analítica ni historial de navegación.\n\n"
                                + "El servicio de apps ve solamente el paquete que llega al frente; no lee texto, URL ni contenido. El filtro DNS opcional evalúa solo DNS UDP y no cubre DoH, DoT, QUIC, IP directas, DNS TCP, resolutores propios ni otra VPN.")
                .setNegativeButton("CERRAR", null)
                .setPositiveButton("VER COBERTURA", (dialog, which) -> showCoverageDisclosure())
                .show();
    }

    private void showHelpSummary() {
        new AlertDialog.Builder(this)
                .setTitle("Ayuda de Proyect X")
                .setMessage("• Las reglas Permitir tienen prioridad sobre Bloquear.\n"
                        + "• Un switch pausado deja de aplicar esa regla, pero no la elimina.\n"
                        + "• El bloqueo de apps requiere Accesibilidad; Android lo habilita manualmente.\n"
                        + "• El DNS es opcional y su cobertura se explica antes de activarlo.\n"
                        + "• Reinicio cierra solo la sesión voluntaria; no desactiva barreras.")
                .setPositiveButton("ENTENDIDO", null)
                .show();
    }

    private void showLanguageSettings() {
        new AlertDialog.Builder(this)
                .setTitle("Idioma de la app")
                .setMessage(
                        "Esta versión está escrita en español. Proyect X seguirá la preferencia del sistema cuando incorporemos traducciones revisadas; no cambiaremos textos a medias ni enviaremos tu idioma fuera del dispositivo.")
                .setNegativeButton("CERRAR", null)
                .setPositiveButton("AJUSTES DEL SISTEMA", (dialog, which) -> openLanguageSettings())
                .show();
    }

    private void openLanguageSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, "Tu versión de Android no tiene preferencias por aplicación.", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        try {
            startActivity(new Intent(Settings.ACTION_APP_LOCALE_SETTINGS));
        } catch (RuntimeException exception) {
            Toast.makeText(this, "No se pudieron abrir los ajustes de idioma.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void showFeedbackComposer() {
        EditText feedback = new EditText(this);
        feedback.setHint("¿Qué podríamos mejorar?");
        feedback.setHintTextColor(MUTED);
        feedback.setTextColor(TEXT);
        feedback.setMinLines(3);
        feedback.setPadding(dp(14), dp(10), dp(14), dp(10));
        feedback.setBackground(outline(SURFACE_RAISED, ACCENT, 12));
        new AlertDialog.Builder(this)
                .setTitle("Comparte tu opinión")
                .setMessage(
                        "Tu texto no se envía a Proyect X. Si continúas, Android abre un selector y tú decides si compartirlo.")
                .setView(feedback)
                .setNegativeButton("CANCELAR", null)
                .setPositiveButton(
                        "ABRIR SELECTOR",
                        (dialog, which) -> shareFeedback(feedback.getText().toString()))
                .show();
    }

    private void shareFeedback(String feedback) {
        String message = feedback == null ? "" : feedback.trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Escribe una opinión antes de compartirla.", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        try {
            Intent share = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(share, "Compartir opinión"));
        } catch (android.content.ActivityNotFoundException exception) {
            Toast.makeText(this, "No hay una aplicación disponible para compartir.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private LinearLayout metricCard(String caption, String value, String detail) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.addView(text(caption, 10, MUTED, Typeface.BOLD));
        card.addView(text(value, 18, TEXT, Typeface.BOLD));
        card.addView(text(detail, 12, MUTED, Typeface.NORMAL));
        return card;
    }

    private TextView compactMetric(String label) {
        TextView view = text(label + "\n0", 12, TEXT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        view.setLineSpacing(0f, 0.95f);
        view.setPadding(dp(8), dp(10), dp(8), dp(10));
        view.setBackground(round(SURFACE_RAISED, 14));
        return view;
    }

    private Spinner spinner(String[] values) {
        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values));
        spinner.setBackground(outline(SURFACE, OUTLINE, 14));
        spinner.setPadding(dp(12), dp(4), dp(12), dp(4));
        return spinner;
    }

    private LinearLayout card() {
        LinearLayout card = column();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(outline(SURFACE, OUTLINE, 22));
        card.setElevation(dp(1));
        return card;
    }

    private Button button(String value, int background, int foreground) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(value);
        button.setTextSize(14);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(foreground);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(48));
        button.setPadding(dp(12), dp(6), dp(12), dp(6));
        button.setBackground(round(background, 14));
        return button;
    }

    private Button compactButton(String value, int background, int foreground) {
        Button button = button(value, background, foreground);
        button.setTextSize(11);
        button.setMinHeight(dp(42));
        button.setPadding(dp(6), dp(2), dp(6), dp(2));
        button.setBackground(round(background, 12));
        return button;
    }

    private TextView tag(String value, int foreground, int background) {
        TextView tag = text(value, 10, foreground, Typeface.BOLD);
        tag.setGravity(Gravity.CENTER);
        tag.setPadding(dp(10), dp(6), dp(10), dp(6));
        tag.setBackground(round(background, 40));
        return tag;
    }

    private TextView label(String value) {
        TextView label = text(value, 11, PRIMARY, Typeface.BOLD);
        label.setPadding(0, dp(16), 0, dp(5));
        return label;
    }

    private TextView text(String value, int size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", style));
        view.setIncludeFontPadding(false);
        view.setPadding(0, dp(4), 0, dp(4));
        return view;
    }

    private LinearLayout row(int gravity) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(gravity);
        return row;
    }

    private LinearLayout column() {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private void showDestination(
            LinearLayout home, LinearLayout protection, LinearLayout rules, LinearLayout recovery) {
        home.setVisibility("inicio".equals(activeDestination) ? View.VISIBLE : View.GONE);
        protection.setVisibility("proteccion".equals(activeDestination) ? View.VISIBLE : View.GONE);
        rules.setVisibility("reglas".equals(activeDestination) ? View.VISIBLE : View.GONE);
        recovery.setVisibility("reinicio".equals(activeDestination) ? View.VISIBLE : View.GONE);
    }

    private LinearLayout destinationNavigation() {
        LinearLayout navigation = row(Gravity.CENTER_VERTICAL);
        navigation.setPadding(dp(12), dp(8), dp(12), dp(12));
        navigation.setBackground(outline(SURFACE, OUTLINE, 0));
        navigation.addView(destinationButton("inicio", "Inicio"), navigationWeight());
        navigation.addView(destinationButton("proteccion", "Barreras"), navigationWeight());
        navigation.addView(destinationButton("reglas", "Reglas"), navigationWeight());
        navigation.addView(destinationButton("reinicio", "Reinicio"), navigationWeight());
        return navigation;
    }

    private LinearLayout destinationButton(String destination, String title) {
        boolean selected = destination.equals(activeDestination);
        int foreground = selected ? TEXT : MUTED;
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER);
        tile.setContentDescription(title);
        tile.setClickable(true);
        tile.setFocusable(true);
        tile.setMinimumHeight(dp(56));
        tile.setPadding(dp(2), dp(4), dp(2), dp(4));
        tile.setBackground(round(selected ? ACCENT : SURFACE, 16));
        ImageView icon = new ImageView(this);
        icon.setImageResource(navigationIcon(destination));
        icon.setColorFilter(foreground);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(icon, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(24)));
        TextView label = text(title, 11, foreground, Typeface.BOLD);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, dp(2), 0, 0);
        label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(label, size(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tile.setOnClickListener(view -> {
            if (destination.equals(activeDestination)) return;
            switchDestination(destination);
        });
        return tile;
    }

    private int navigationIcon(String destination) {
        return switch (destination) {
            case "proteccion" -> R.drawable.ic_nav_barreras;
            case "reglas" -> R.drawable.ic_nav_reglas;
            case "reinicio" -> R.drawable.ic_nav_reinicio;
            default -> R.drawable.ic_nav_inicio;
        };
    }

    private void switchDestination(String destination) {
        if (destination.equals(activeDestination)) return;
        activeDestination = destination;
        render();
    }

    private Button sessionDurationButton(String title, int minutes) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(title);
        button.setTextSize(12);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(TEXT);
        button.setMinHeight(dp(42));
        button.setPadding(dp(4), dp(2), dp(4), dp(2));
        button.setBackground(round(SURFACE_RAISED, 12));
        button.setOnClickListener(view -> startFocusSession(minutes));
        return button;
    }

    private LinearLayout.LayoutParams size(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private LinearLayout.LayoutParams fillWeight() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
    }

    private LinearLayout.LayoutParams navigationWeight() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private LinearLayout.LayoutParams weightWithMargin(float weight, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        params.setMargins(left, top, right, bottom);
        return params;
    }

    private LinearLayout.LayoutParams marginTop(int top) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(top);
        return params;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private GradientDrawable outline(int color, int stroke, int radius) {
        GradientDrawable drawable = round(color, radius);
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
