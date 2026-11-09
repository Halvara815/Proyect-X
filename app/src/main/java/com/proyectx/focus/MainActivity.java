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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/** Main local-first dashboard. The accessibility integration remains explicitly optional. */
public final class MainActivity extends Activity {
    private static final int DNS_VPN_CONSENT_REQUEST = 4_201;
    private static final int DNS_NOTIFICATION_REQUEST = 4_202;
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
    private LinearLayout rulesContainer;
    private TextView ruleCountView;
    private TextView protectionStatusView;
    private TextView dnsStatusView;
    private TextView sessionTitleView;
    private TextView sessionDetailView;
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

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        ruleStore = new RuleStore(this);
        budgetStore = new BudgetStore(this);
        sessionStore = new FocusSessionStore(this);
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
        root.addView(header);

        LinearLayout destinations = column();
        LinearLayout homeDestination = column();
        LinearLayout protectionDestination = column();
        LinearLayout rulesDestination = column();
        LinearLayout privacyDestination = column();
        destinations.addView(homeDestination);
        destinations.addView(protectionDestination);
        destinations.addView(rulesDestination);
        destinations.addView(privacyDestination);
        root.addView(destinations);

        LinearLayout hero = card();
        hero.setBackground(round(HERO, 24));
        hero.setElevation(0);
        hero.addView(tag("HOY", PRIMARY, BACKGROUND));
        hero.addView(text("Tu día, con menos interrupciones.", 25, TEXT, Typeface.BOLD));
        hero.addView(text(
                "Elige una sesión, ajusta tus reglas y conoce siempre el alcance de la protección que activas.",
                15,
                MUTED,
                Typeface.NORMAL));
        homeDestination.addView(hero, marginTop(16));

        LinearLayout metrics = row(Gravity.CENTER_VERTICAL);
        metrics.addView(metricCard("REGLAS ACTIVAS", "0", "en este dispositivo"), weightWithMargin(1, 0, 0, dp(8), 0));
        metrics.addView(metricCard("COBERTURA", "Revisar", "permisos y modos"), weightWithMargin(1, 0, 0, 0, 0));
        homeDestination.addView(metrics, marginTop(12));
        ruleCountView = (TextView) ((LinearLayout) metrics.getChildAt(0)).getChildAt(1);
        protectionStatusView = (TextView) ((LinearLayout) metrics.getChildAt(1)).getChildAt(1);

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
        dnsStatusView = text("Filtro DNS detenido.", 13, MUTED, Typeface.NORMAL);
        coverage.addView(dnsStatusView, marginTop(8));
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

        privacyDestination.addView(text("Privacidad primero", 21, TEXT, Typeface.BOLD), marginTop(20));
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
        privacyDestination.addView(privacyCard, marginTop(12));

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
        rulesContainer = column();
        rulesDestination.addView(rulesContainer);
        showDestination(homeDestination, protectionDestination, rulesDestination, privacyDestination);

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
                .setItems(choices, (dialog, selected) -> {
                    try {
                        sessionStore.start(durations[selected], System.currentTimeMillis());
                        refreshSession();
                        Toast.makeText(this, "Sesión local iniciada.", Toast.LENGTH_SHORT)
                                .show();
                    } catch (IllegalArgumentException | IllegalStateException exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
            item.addView(top);
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

    private LinearLayout metricCard(String caption, String value, String detail) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.addView(text(caption, 10, MUTED, Typeface.BOLD));
        card.addView(text(value, 18, TEXT, Typeface.BOLD));
        card.addView(text(detail, 12, MUTED, Typeface.NORMAL));
        return card;
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

    private void showDestination(LinearLayout home, LinearLayout protection, LinearLayout rules, LinearLayout privacy) {
        home.setVisibility("inicio".equals(activeDestination) ? View.VISIBLE : View.GONE);
        protection.setVisibility("proteccion".equals(activeDestination) ? View.VISIBLE : View.GONE);
        rules.setVisibility("reglas".equals(activeDestination) ? View.VISIBLE : View.GONE);
        privacy.setVisibility("privacidad".equals(activeDestination) ? View.VISIBLE : View.GONE);
    }

    private LinearLayout destinationNavigation() {
        LinearLayout navigation = row(Gravity.CENTER_VERTICAL);
        navigation.setPadding(dp(12), dp(8), dp(12), dp(12));
        navigation.setBackground(outline(SURFACE, OUTLINE, 0));
        navigation.addView(destinationButton("inicio", "Inicio"), navigationWeight());
        navigation.addView(destinationButton("proteccion", "Protección"), navigationWeight());
        navigation.addView(destinationButton("reglas", "Reglas"), navigationWeight());
        navigation.addView(destinationButton("privacidad", "Privacidad"), navigationWeight());
        return navigation;
    }

    private Button destinationButton(String destination, String title) {
        boolean selected = destination.equals(activeDestination);
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(title);
        button.setTextSize(11);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(selected ? TEXT : MUTED);
        button.setMinHeight(dp(52));
        button.setPadding(dp(2), dp(4), dp(2), dp(4));
        button.setBackground(round(selected ? ACCENT : SURFACE, 16));
        button.setOnClickListener(view -> {
            if (destination.equals(activeDestination)) return;
            activeDestination = destination;
            render();
        });
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
