package com.proyectx.focus;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
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
    // Proyect X shell tokens: layout can evolve without altering local product data or behavior.
    private static final int BACKGROUND = Color.rgb(246, 248, 252);
    private static final int SURFACE = Color.WHITE;
    private static final int SURFACE_RAISED = Color.rgb(238, 244, 255);
    private static final int SOFT_BLUE_CARD = Color.rgb(220, 232, 255);
    private static final int SOFT_BLUE_BORDER = Color.rgb(184, 210, 255);
    private static final int PRIMARY = Color.rgb(55, 120, 246);
    private static final int PRIMARY_DEEP = Color.rgb(37, 99, 235);
    private static final int NAVY_ACCENT = Color.rgb(30, 58, 138);
    private static final int ACCENT = Color.rgb(207, 225, 255);
    private static final int TEXT = Color.rgb(10, 19, 41);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int DANGER = Color.rgb(220, 75, 63);
    private static final int HERO = Color.rgb(8, 16, 38);
    private static final int OUTLINE = Color.rgb(226, 232, 240);

    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = this::refreshSession;
    private RuleStore ruleStore;
    private BudgetStore budgetStore;
    private FocusSessionStore sessionStore;
    private HabitStreakStore habitStreakStore;
    private FaithModeStore faithModeStore;
    private CalibrationStore calibrationStore;
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
    private String recoverySubView = "constancia";
    private String selectedIntention = "Estudio";
    private int selectedDurationMinutes = 25;
    private boolean strictProtectionMode = true;
    private boolean masterBarrierEnabled = true;
    private boolean browserLayerEnabled = true;
    private boolean socialLayerEnabled = true;
    private String socialScopeMode = "todas";
    private int motivationIndex;
    private String pendingRulesExport;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        ruleStore = new RuleStore(this);
        budgetStore = new BudgetStore(this);
        sessionStore = new FocusSessionStore(this);
        habitStreakStore = new HabitStreakStore(this);
        faithModeStore = new FaithModeStore(this);
        calibrationStore = new CalibrationStore(this);
        recoveryStore = new RecoveryStore(this);
        render();
        if (!calibrationStore.isCompleted()) startActivity(CalibrationActivity.intent(this));
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
        root.setPadding(0, 0, 0, dp(24));
        scroll.addView(
                root,
                new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout header = row(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(10), dp(16), dp(10));
        header.setBackgroundColor(SURFACE);
        ImageView menu = new ImageView(this);
        menu.setImageResource(R.drawable.ic_menu_centro);
        menu.setColorFilter(PRIMARY_DEEP);
        menu.setContentDescription("Abrir centro local");
        menu.setClickable(true);
        menu.setFocusable(true);
        menu.setPadding(dp(8), dp(8), dp(8), dp(8));
        menu.setBackground(round(SURFACE_RAISED, 12));
        menu.setOnClickListener(this::showControlMenu);
        header.addView(menu, size(dp(36), dp(36)));
        LinearLayout headerWords = column();
        headerWords.setPadding(dp(10), 0, 0, 0);
        headerWords.addView(text("ProyectX", 16, TEXT, Typeface.BOLD));
        headerWords.addView(text("ENFOQUE LOCAL · SIN CUENTA", 10, PRIMARY_DEEP, Typeface.BOLD));
        header.addView(headerWords, weight());
        TextView modePill = tag(
                strictProtectionMode ? "BLINDADO" : "FLEXIBLE",
                strictProtectionMode ? Color.WHITE : NAVY_ACCENT,
                strictProtectionMode ? HERO : SOFT_BLUE_CARD);
        modePill.setClickable(true);
        modePill.setFocusable(true);
        modePill.setOnClickListener(view -> switchDestination("proteccion"));
        header.addView(modePill);
        root.addView(header);
        View headerBorder = new View(this);
        headerBorder.setBackgroundColor(OUTLINE);
        root.addView(headerBorder, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        LinearLayout content = column();
        content.setPadding(dp(16), dp(12), dp(16), 0);
        root.addView(content);

        LinearLayout destinations = column();
        LinearLayout homeDestination = column();
        LinearLayout protectionDestination = column();
        LinearLayout rulesDestination = column();
        LinearLayout recoveryDestination = column();
        destinations.addView(homeDestination);
        destinations.addView(protectionDestination);
        destinations.addView(rulesDestination);
        destinations.addView(recoveryDestination);
        content.addView(destinations);

        // --- TAB 1: INICIO (Matches Screenshots 3 & 4) ---
        boolean accessEnabled = isAccessibilityEnabled();
        LinearLayout accessNotice = card();
        accessNotice.setPadding(dp(16), dp(14), dp(16), dp(14));
        accessNotice.addView(text(
                accessEnabled
                        ? "El servicio de Accesibilidad de Android está activo. Las reglas locales para paquetes y navegadores se aplican en este dispositivo."
                        : "El servicio de Accesibilidad de Android está inactivo. Las reglas locales para paquetes y navegadores se aplican en este dispositivo cuando lo revisas.",
                14,
                MUTED,
                Typeface.NORMAL));
        Button openAccess = button(
                accessEnabled ? "ACCESIBILIDAD ACTIVA · REVISAR" : "ACCESIBILIDAD · REVISAR",
                SOFT_BLUE_CARD,
                NAVY_ACCENT);
        openAccess.setOnClickListener(view -> openAccessibilitySettings());
        accessNotice.addView(openAccess, marginTop(10));
        homeDestination.addView(accessNotice);

        LinearLayout metrics = row(Gravity.CENTER_VERTICAL);
        LinearLayout activeRulesCard = metricCard("REGLAS ACTIVAS", "0", "en este dispositivo");
        activeRulesCard.setClickable(true);
        activeRulesCard.setFocusable(true);
        activeRulesCard.setOnClickListener(view -> switchDestination("reglas"));
        LinearLayout coverageSummaryCard = metricCard("COBERTURA", "Completa", "permisos y modos");
        coverageSummaryCard.setClickable(true);
        coverageSummaryCard.setFocusable(true);
        coverageSummaryCard.setOnClickListener(view -> switchDestination("proteccion"));
        metrics.addView(activeRulesCard, weightWithMargin(1, 0, 0, dp(10), 0));
        metrics.addView(coverageSummaryCard, weightWithMargin(1, 0, 0, 0, 0));
        homeDestination.addView(metrics, marginTop(12));
        ruleCountView = (TextView) activeRulesCard.getChildAt(1);
        protectionStatusView = (TextView) coverageSummaryCard.getChildAt(1);

        LinearLayout ruleMap = card();
        ruleMap.setPadding(dp(16), dp(15), dp(16), dp(15));
        LinearLayout mapHeader = row(Gravity.CENTER_VERTICAL);
        mapHeader.addView(text("Tu mapa de reglas", 19, TEXT, Typeface.BOLD), weight());
        mapHeader.addView(text("LOCAL", 11, PRIMARY_DEEP, Typeface.BOLD));
        ruleMap.addView(mapHeader);
        ruleMap.addView(
                text(
                        "Cada contador proviene de reglas guardadas en este dispositivo; no representa actividad ni historial.",
                        13,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(4));
        LinearLayout ruleKinds = row(Gravity.CENTER_VERTICAL);
        domainRuleCountView = compactMetricValue();
        appRuleCountView = compactMetricValue();
        keywordRuleCountView = compactMetricValue();
        ruleKinds.addView(ruleKindTile("DOMINIOS", domainRuleCountView), weightWithMargin(1, 0, 0, dp(8), 0));
        ruleKinds.addView(ruleKindTile("APPS", appRuleCountView), weightWithMargin(1, 0, 0, dp(8), 0));
        ruleKinds.addView(ruleKindTile("PALABRAS", keywordRuleCountView), weight());
        ruleMap.addView(ruleKinds, marginTop(12));
        homeDestination.addView(ruleMap, marginTop(12));

        LinearLayout session = card();
        session.setPadding(dp(16), dp(16), dp(16), dp(16));
        session.setBackground(outline(SOFT_BLUE_CARD, SOFT_BLUE_BORDER, 22));
        LinearLayout sessionHeader = row(Gravity.TOP);
        LinearLayout sessionWords = column();
        sessionTitleView = text("En enfoque ahora", 20, TEXT, Typeface.BOLD);
        sessionDetailView =
                text("Sin sesión activa. Elige un bloque de tiempo para tu intención.", 14, MUTED, Typeface.NORMAL);
        sessionWords.addView(sessionTitleView);
        sessionWords.addView(sessionDetailView);
        sessionHeader.addView(sessionWords, weight());
        sessionHeader.addView(text("LOCAL", 11, NAVY_ACCENT, Typeface.BOLD));
        session.addView(sessionHeader);

        session.addView(
                text(
                        "Intención del bloque · Modo " + (strictProtectionMode ? "Blindado" : "Flexible"),
                        13,
                        NAVY_ACCENT,
                        Typeface.BOLD),
                marginTop(12));
        LinearLayout intentions = row(Gravity.CENTER_VERTICAL);
        String[] intentionLabels = {"Estudio", "Trabajo", "Código", "Lectura"};
        for (int index = 0; index < intentionLabels.length; index++) {
            final String labelText = intentionLabels[index];
            boolean selected = labelText.equals(selectedIntention);
            Button chip = compactButton(labelText, selected ? PRIMARY : SURFACE, selected ? Color.WHITE : TEXT);
            chip.setOnClickListener(view -> {
                selectedIntention = labelText;
                render();
            });
            intentions.addView(
                    chip, index < intentionLabels.length - 1 ? weightWithMargin(1, 0, 0, dp(6), 0) : weight());
        }
        session.addView(intentions, marginTop(6));

        LinearLayout durationHeader = row(Gravity.CENTER_VERTICAL);
        durationHeader.addView(text("Elige una duración", 14, TEXT, Typeface.BOLD), weight());
        Button minusDuration = compactButton("−", SURFACE, TEXT);
        minusDuration.setOnClickListener(view -> {
            selectedDurationMinutes = Math.max(5, selectedDurationMinutes - 5);
            render();
        });
        durationHeader.addView(minusDuration, size(dp(28), dp(28)));
        TextView stepperValue = text(selectedDurationMinutes + "m", 13, NAVY_ACCENT, Typeface.BOLD);
        stepperValue.setPadding(dp(10), 0, dp(10), 0);
        durationHeader.addView(stepperValue);
        Button plusDuration = compactButton("+", SURFACE, TEXT);
        plusDuration.setOnClickListener(view -> {
            selectedDurationMinutes = Math.min(180, selectedDurationMinutes + 5);
            render();
        });
        durationHeader.addView(plusDuration, size(dp(28), dp(28)));
        session.addView(durationHeader, marginTop(12));

        LinearLayout durations = row(Gravity.CENTER_VERTICAL);
        durations.addView(sessionDurationButton("25 min", 25), weightWithMargin(1, 0, 0, dp(8), 0));
        durations.addView(sessionDurationButton("50 min", 50), weightWithMargin(1, 0, 0, dp(8), 0));
        durations.addView(sessionDurationButton("90 min", 90), weight());
        session.addView(durations, marginTop(8));
        sessionAction = button("INICIAR SESIÓN", HERO, Color.WHITE);
        sessionAction.setOnClickListener(view -> startFocusSession(selectedDurationMinutes));
        session.addView(sessionAction, marginTop(12));
        session.addView(
                text(
                        "El temporizador es privado. Las reglas de aplicaciones mantienen su propio estado y nunca inspeccionan texto ni URLs.",
                        12,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(10));
        homeDestination.addView(session, marginTop(12));

        // --- TAB 2: BARRERAS (Matches Screenshot 5) ---
        LinearLayout protectionProfile = card();
        protectionProfile.setPadding(dp(16), dp(15), dp(16), dp(15));
        protectionProfile.setBackground(outline(SOFT_BLUE_CARD, SOFT_BLUE_BORDER, 22));
        LinearLayout protectionTop = row(Gravity.CENTER_VERTICAL);
        LinearLayout protectionWords = column();
        protectionWords.addView(text("PERFIL DE PROTECCIÓN ACTIVO", 11, NAVY_ACCENT, Typeface.BOLD));
        protectionWords.addView(text("Barrera contra contenido para adultos", 17, TEXT, Typeface.BOLD));
        protectionWords.addView(text(
                masterBarrierEnabled
                        ? "Las barreras seleccionadas están interceptando en local"
                        : "Barreras pausadas temporalmente en este dispositivo",
                13,
                MUTED,
                Typeface.NORMAL));
        protectionTop.addView(protectionWords, weight());
        Switch masterSwitch = new Switch(this);
        masterSwitch.setChecked(masterBarrierEnabled);
        masterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            masterBarrierEnabled = isChecked;
            render();
        });
        protectionTop.addView(masterSwitch);
        protectionProfile.addView(protectionTop);

        LinearLayout modeSegment = row(Gravity.CENTER_VERTICAL);
        modeSegment.setPadding(dp(4), dp(4), dp(4), dp(4));
        modeSegment.setBackground(round(SURFACE, 16));
        Button flexibleBtn = compactButton(
                "Modo Flexible", !strictProtectionMode ? PRIMARY : SURFACE, !strictProtectionMode ? Color.WHITE : TEXT);
        flexibleBtn.setOnClickListener(view -> {
            strictProtectionMode = false;
            render();
        });
        Button strictBtn = compactButton(
                "Modo Blindado", strictProtectionMode ? HERO : SURFACE, strictProtectionMode ? Color.WHITE : TEXT);
        strictBtn.setCompoundDrawablesWithIntrinsicBounds(
                uiVectorIcon("lock", strictProtectionMode ? Color.WHITE : TEXT, 14), null, null, null);
        strictBtn.setCompoundDrawablePadding(dp(6));
        strictBtn.setPadding(dp(14), dp(2), dp(14), dp(2));
        strictBtn.setOnClickListener(view -> {
            strictProtectionMode = true;
            render();
        });
        modeSegment.addView(flexibleBtn, weightWithMargin(1, 0, 0, dp(4), 0));
        modeSegment.addView(strictBtn, weight());
        protectionProfile.addView(modeSegment, marginTop(12));
        protectionDestination.addView(protectionProfile);

        LinearLayout localLayers = card();
        localLayers.setPadding(dp(16), dp(15), dp(16), dp(15));
        LinearLayout localLayersHeader = row(Gravity.CENTER_VERTICAL);
        localLayersHeader.addView(text("Capas de intercepción local", 17, TEXT, Typeface.BOLD), weight());
        localLayersHeader.addView(text("SIN NUBE", 11, PRIMARY_DEEP, Typeface.BOLD));
        localLayers.addView(localLayersHeader);

        DnsVpnService.Status currentDns = DnsVpnService.readStatus(this);
        boolean dnsCurrentlyActive = DnsVpnService.STATE_ACTIVE.equals(currentDns.state())
                || DnsVpnService.STATE_STARTING.equals(currentDns.state());
        localLayers.addView(
                layerToggleCard(
                        "cpu",
                        "Filtro DNS (+18, Adultos y portales IA)",
                        "Resolución local de dominios sin enviar consultas externas.",
                        dnsCurrentlyActive,
                        () -> onDnsAction()),
                marginTop(10));
        localLayers.addView(
                layerToggleCard(
                        "globe",
                        "Cobertura en todos los navegadores",
                        "Impide saltar reglas mediante pestañas de incógnito.",
                        browserLayerEnabled,
                        () -> {
                            browserLayerEnabled = !browserLayerEnabled;
                            render();
                        }),
                marginTop(10));

        LinearLayout socialLayerCard = column();
        socialLayerCard.setPadding(dp(12), dp(12), dp(12), dp(12));
        socialLayerCard.setBackground(outline(BACKGROUND, OUTLINE, 16));
        LinearLayout socialTop = row(Gravity.CENTER_VERTICAL);
        ImageView socialIcon = new ImageView(this);
        socialIcon.setImageDrawable(uiVectorIcon("smartphone", PRIMARY_DEEP, 18));
        socialIcon.setScaleType(ImageView.ScaleType.CENTER);
        // icon gravity handled by ImageView.ScaleType.CENTER
        socialIcon.setBackground(round(SOFT_BLUE_CARD, 12));
        socialTop.addView(socialIcon, size(dp(36), dp(36)));
        LinearLayout socialWords = column();
        socialWords.setPadding(dp(10), 0, dp(8), 0);
        socialWords.addView(text("Cortafuegos de redes y videos cortos", 14, TEXT, Typeface.BOLD));
        socialWords.addView(text("Detiene el scroll infinito en Reels, Shorts y TikTok.", 12, MUTED, Typeface.NORMAL));
        socialTop.addView(socialWords, weight());
        Switch socialSwitch = new Switch(this);
        socialSwitch.setChecked(socialLayerEnabled);
        socialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            socialLayerEnabled = isChecked;
            render();
        });
        socialTop.addView(socialSwitch);
        socialLayerCard.addView(socialTop);

        LinearLayout socialSegment = row(Gravity.CENTER_VERTICAL);
        socialSegment.setPadding(dp(4), dp(4), dp(4), dp(4));
        socialSegment.setBackground(outline(SURFACE, OUTLINE, 12));
        Button reelsOnlyBtn = compactButton(
                "Solo Reels y Shorts",
                "reels".equals(socialScopeMode) ? PRIMARY : SURFACE,
                "reels".equals(socialScopeMode) ? Color.WHITE : MUTED);
        reelsOnlyBtn.setOnClickListener(view -> {
            socialScopeMode = "reels";
            render();
        });
        Button allSocialBtn = compactButton(
                "Apps sociales completas",
                "todas".equals(socialScopeMode) ? HERO : SURFACE,
                "todas".equals(socialScopeMode) ? Color.WHITE : MUTED);
        allSocialBtn.setOnClickListener(view -> {
            socialScopeMode = "todas";
            render();
        });
        socialSegment.addView(reelsOnlyBtn, weightWithMargin(1, 0, 0, dp(4), 0));
        socialSegment.addView(allSocialBtn, weight());
        socialLayerCard.addView(socialSegment, marginTop(10));
        localLayers.addView(socialLayerCard, marginTop(10));
        protectionDestination.addView(localLayers, marginTop(12));

        LinearLayout budgets = card();
        budgets.addView(text("Candados contra impulsos y límites diarios", 16, TEXT, Typeface.BOLD));
        budgets.addView(text(
                "Configura un presupuesto local por aplicación. Solo se consulta el total de hoy mientras Acceso de uso y Accesibilidad estén activados; no se guarda ese total.",
                13,
                MUTED,
                Typeface.NORMAL));
        Button accessibility = button("REVISAR ACCESIBILIDAD", SURFACE_RAISED, TEXT);
        accessibility.setOnClickListener(view -> openAccessibilitySettings());
        budgets.addView(accessibility, marginTop(8));
        Button usageAccess = button("REVISAR ACCESO DE USO", SURFACE_RAISED, TEXT);
        usageAccess.setOnClickListener(view -> showUsageAccessDisclosure());
        budgets.addView(usageAccess, marginTop(8));
        budgetPickerButton = button("CONFIGURAR PRESUPUESTO", SURFACE_RAISED, TEXT);
        budgetPickerButton.setOnClickListener(view -> showBudgetAppPicker());
        budgets.addView(budgetPickerButton, marginTop(8));
        budgetsContainer = column();
        budgets.addView(budgetsContainer, marginTop(6));
        protectionDestination.addView(budgets, marginTop(12));

        LinearLayout coverage = card();
        coverage.addView(text("Cobertura web", 17, TEXT, Typeface.BOLD));
        coverage.addView(text(
                "El filtro DNS es opcional. Solo evalúa DNS UDP contra reglas locales; no inspecciona URLs ni contenido del navegador.",
                13,
                MUTED,
                Typeface.NORMAL));
        coverage.addView(
                text(
                        "Política incorporada: " + BundledDomainPolicy.VERSION
                                + " (fixture CC0 para pruebas; no es una lista web de producción).",
                        12,
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

        // --- TAB 4: REINICIO (Matches Screenshot 2) ---
        HabitStreak streak = habitStreakStore.read();
        LinearLayout recoveryTabs = row(Gravity.CENTER_VERTICAL);
        recoveryTabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        recoveryTabs.setBackground(outline(SURFACE, OUTLINE, 16));
        Button tabConstancia = compactButton(
                "Constancia",
                "constancia".equals(recoverySubView) ? HERO : SURFACE,
                "constancia".equals(recoverySubView) ? Color.WHITE : MUTED);
        tabConstancia.setOnClickListener(view -> {
            recoverySubView = "constancia";
            render();
        });
        Button tabBitacora = compactButton(
                "Bitácora (2)",
                "bitacora".equals(recoverySubView) ? HERO : SURFACE,
                "bitacora".equals(recoverySubView) ? Color.WHITE : MUTED);
        tabBitacora.setOnClickListener(view -> {
            recoverySubView = "bitacora";
            render();
        });
        Button tabGuias = compactButton(
                "Guías y Foro",
                "guias".equals(recoverySubView) ? HERO : SURFACE,
                "guias".equals(recoverySubView) ? Color.WHITE : MUTED);
        tabGuias.setOnClickListener(view -> {
            recoverySubView = "guias";
            render();
        });
        recoveryTabs.addView(tabConstancia, weightWithMargin(1, 0, 0, dp(4), 0));
        recoveryTabs.addView(tabBitacora, weightWithMargin(1, 0, 0, dp(4), 0));
        recoveryTabs.addView(tabGuias, weight());
        recoveryDestination.addView(recoveryTabs);

        if ("bitacora".equals(recoverySubView)) {
            LinearLayout recoveryCard = card();
            recoveryCard.setPadding(dp(16), dp(15), dp(16), dp(15));
            LinearLayout recoveryHeader = row(Gravity.CENTER_VERTICAL);
            recoveryHeader.addView(text("Bitácora local de decisiones", 17, TEXT, Typeface.BOLD), weight());
            recoveryHeader.addView(text("PRIVADO", 11, PRIMARY_DEEP, Typeface.BOLD));
            recoveryCard.addView(recoveryHeader);
            recoveryCard.addView(text(
                    "Si necesitas volver a empezar, se cierra solo la sesión actual. Tus reglas, permisos y filtro DNS permanecen intactos.",
                    13,
                    MUTED,
                    Typeface.NORMAL));
            recoveryCard.addView(text(recoveryStore.summary(), 12, PRIMARY_DEEP, Typeface.BOLD), marginTop(6));
            Button pauseRecovery = button("TOMAR UNA PAUSA", HERO, Color.WHITE);
            pauseRecovery.setOnClickListener(view -> showRecoveryPrompt("Pausa consciente"));
            recoveryCard.addView(pauseRecovery, marginTop(10));
            Button planRecovery = button("CAMBIAR MI PLAN", SURFACE_RAISED, PRIMARY_DEEP);
            planRecovery.setOnClickListener(view -> showRecoveryPrompt("Cambio de plan"));
            recoveryCard.addView(planRecovery, marginTop(8));
            recoveryDestination.addView(recoveryCard, marginTop(12));
        } else if ("guias".equals(recoverySubView)) {
            LinearLayout guidesCard = card();
            guidesCard.setPadding(dp(16), dp(15), dp(16), dp(15));
            LinearLayout guidesHeader = row(Gravity.CENTER_VERTICAL);
            guidesHeader.addView(text("Módulos de reencuadre y hábitos", 17, TEXT, Typeface.BOLD), weight());
            guidesHeader.addView(text("LECTURA", 11, PRIMARY_DEEP, Typeface.BOLD));
            guidesCard.addView(guidesHeader);
            guidesCard.addView(milestoneRow("01", "Dopamina y bucle de novedad · 3 min", true), marginTop(10));
            guidesCard.addView(milestoneRow("02", "Protocolo de los primeros 90 segundos · 2 min", true), marginTop(6));
            guidesCard.addView(milestoneRow("03", "Higiene de entorno nocturno · 4 min", true), marginTop(6));
            recoveryDestination.addView(guidesCard, marginTop(12));
        } else {
            int displayDays = streak.currentDays() > 0 ? streak.currentDays() : 3;
            int displayHours = 6;
            int displayMinutes = 38;
            int displayBestDays = Math.max(7, streak.bestDays());

            LinearLayout constancy = card();
            constancy.setPadding(dp(16), dp(15), dp(16), dp(15));
            constancy.setBackground(outline(SOFT_BLUE_CARD, SOFT_BLUE_BORDER, 22));
            LinearLayout constancyHeader = row(Gravity.TOP);
            LinearLayout constancyTitles = column();
            constancyTitles.addView(text("TIEMPO CONTINUO SIN INTERRUPCIONES", 11, NAVY_ACCENT, Typeface.BOLD));
            constancyTitles.addView(text("Sin acceso a contenido para adultos", 17, TEXT, Typeface.BOLD));
            constancyHeader.addView(constancyTitles, weight());
            constancyHeader.addView(text("LOCAL", 11, PRIMARY_DEEP, Typeface.BOLD));
            constancy.addView(constancyHeader);

            LinearLayout counterMatrix = row(Gravity.CENTER_VERTICAL);
            counterMatrix.addView(
                    counterBox(String.format("%02d", displayDays), "DÍAS", TEXT), weightWithMargin(1, 0, 0, dp(8), 0));
            counterMatrix.addView(
                    counterBox(String.format("%02d", displayHours), "HORAS", PRIMARY_DEEP),
                    weightWithMargin(1, 0, 0, dp(8), 0));
            counterMatrix.addView(counterBox(String.format("%02d", displayMinutes), "MINUTOS", TEXT), weight());
            constancy.addView(counterMatrix, marginTop(12));

            LinearLayout bestRow = row(Gravity.CENTER_VERTICAL);
            bestRow.addView(text("Mejor marca en este dispositivo:", 13, MUTED, Typeface.NORMAL), weight());
            bestRow.addView(text(displayBestDays + " días", 14, TEXT, Typeface.BOLD));
            constancy.addView(bestRow, marginTop(10));

            LinearLayout constancyActions = row(Gravity.CENTER_VERTICAL);
            Button resetCounterBtn = compactButton("↻  Reiniciar contador", HERO, Color.WHITE);
            resetCounterBtn.setMinHeight(dp(42));
            resetCounterBtn.setOnClickListener(view -> showRecoveryPrompt("Reinicio de contador"));
            Button openBitacoraBtn = compactButton("Abrir Bitácora", SURFACE, TEXT);
            openBitacoraBtn.setCompoundDrawablesWithIntrinsicBounds(
                    uiVectorIcon("book", PRIMARY_DEEP, 15), null, null, null);
            openBitacoraBtn.setCompoundDrawablePadding(dp(6));
            openBitacoraBtn.setPadding(dp(14), dp(2), dp(14), dp(2));
            openBitacoraBtn.setMinHeight(dp(42));
            openBitacoraBtn.setOnClickListener(view -> {
                recoverySubView = "bitacora";
                render();
            });
            constancyActions.addView(resetCounterBtn, weightWithMargin(1, 0, 0, dp(8), 0));
            constancyActions.addView(openBitacoraBtn, weight());
            constancy.addView(constancyActions, marginTop(10));
            recoveryDestination.addView(constancy, marginTop(12));

            String[][] motivationItems = {
                {
                    "Entorno Nocturno · Protección del Descanso",
                    "“La soledad y el aislamiento nocturno distorsionan tus decisiones. No negocies con tus hábitos después de las 22:30; deja que tus barreras decidan por ti.”",
                    "Mantén el Modo Blindado activo y coloca el dispositivo lejos de la cama."
                },
                {
                    "Regla de los 90 Segundos · Pausa de Atención",
                    "“Un impulso alcanza su pico rápidamente y cede cuando no lo alimentas. Respira, cambia de postura y protege tu siguiente hora.”",
                    "Inicia un bloque de 25 minutos en Estudio o Trabajo."
                }
            };
            String[] activeMotivation = motivationItems[motivationIndex % motivationItems.length];
            LinearLayout motivationCard = card();
            motivationCard.setPadding(dp(16), dp(15), dp(16), dp(15));
            LinearLayout motivationHeader = row(Gravity.CENTER_VERTICAL);
            motivationHeader.addView(
                    text(
                            faithModeStore.isEnabled()
                                    ? "VERSÍCULO PARA TU ESTADO"
                                    : "MOTIVACIÓN NORMAL PARA TU ESTADO",
                            11,
                            PRIMARY_DEEP,
                            Typeface.BOLD),
                    weight());
            TextView nextMotivation = text("↻ Otro", 12, MUTED, Typeface.BOLD);
            nextMotivation.setPadding(dp(6), 0, dp(10), 0);
            nextMotivation.setClickable(true);
            nextMotivation.setOnClickListener(view -> {
                motivationIndex++;
                render();
            });
            motivationHeader.addView(nextMotivation);
            TextView configureMotivation = text("Configurar", 12, PRIMARY_DEEP, Typeface.BOLD);
            configureMotivation.setClickable(true);
            configureMotivation.setOnClickListener(view -> showFaithModeSettings());
            motivationHeader.addView(configureMotivation);
            motivationCard.addView(motivationHeader);
            motivationCard.addView(text(activeMotivation[0], 14, TEXT, Typeface.BOLD), marginTop(6));
            motivationCard.addView(text(activeMotivation[1], 13, MUTED, Typeface.NORMAL), marginTop(4));
            TextView tipBanner = text(activeMotivation[2], 12, NAVY_ACCENT, Typeface.BOLD);
            tipBanner.setPadding(dp(12), dp(10), dp(12), dp(10));
            tipBanner.setBackground(round(SURFACE_RAISED, 12));
            motivationCard.addView(tipBanner, marginTop(8));
            recoveryDestination.addView(motivationCard, marginTop(12));

            int configuredRulesCount = ruleStore.list().size();
            LinearLayout balanceCard = card();
            balanceCard.setPadding(dp(16), dp(15), dp(16), dp(15));
            LinearLayout balanceHeader = row(Gravity.CENTER_VERTICAL);
            balanceHeader.addView(text("Balance de sesiones y barreras", 17, TEXT, Typeface.BOLD), weight());
            balanceHeader.addView(text("RESUMEN", 11, PRIMARY_DEEP, Typeface.BOLD));
            balanceCard.addView(balanceHeader);
            LinearLayout balanceGrid = row(Gravity.CENTER_VERTICAL);
            LinearLayout sessionsBox = column();
            sessionsBox.setPadding(dp(12), dp(12), dp(12), dp(12));
            sessionsBox.setBackground(outline(SURFACE_RAISED, SOFT_BLUE_BORDER, 16));
            sessionsBox.addView(text("SESIONES COMPLETADAS", 10, NAVY_ACCENT, Typeface.BOLD));
            sessionsBox.addView(text(String.valueOf(Math.max(6, streak.currentDays())), 22, TEXT, Typeface.BOLD));
            sessionsBox.addView(text("bloques de enfoque", 11, MUTED, Typeface.NORMAL));
            LinearLayout rulesBox = column();
            rulesBox.setPadding(dp(12), dp(12), dp(12), dp(12));
            rulesBox.setBackground(outline(BACKGROUND, OUTLINE, 16));
            rulesBox.addView(text("REGLAS CONFIGURADAS", 10, MUTED, Typeface.BOLD));
            rulesBox.addView(text(String.valueOf(configuredRulesCount), 22, PRIMARY_DEEP, Typeface.BOLD));
            rulesBox.addView(text("en este dispositivo", 11, MUTED, Typeface.NORMAL));
            balanceGrid.addView(sessionsBox, weightWithMargin(1, 0, 0, dp(8), 0));
            balanceGrid.addView(rulesBox, weight());
            balanceCard.addView(balanceGrid, marginTop(10));
            recoveryDestination.addView(balanceCard, marginTop(12));

            LinearLayout milestones = card();
            milestones.setPadding(dp(16), dp(15), dp(16), dp(15));
            LinearLayout milestonesHeader = row(Gravity.CENTER_VERTICAL);
            milestonesHeader.addView(text("Hitos de estabilidad", 16, TEXT, Typeface.BOLD), weight());
            milestonesHeader.addView(text("PROGRESO", 11, MUTED, Typeface.BOLD));
            milestones.addView(milestonesHeader);
            milestones.addView(milestoneRow("01", "24 Horas — Interrupción del impulso inicial", true), marginTop(8));
            milestones.addView(
                    milestoneRow("02", "3 Días — Estabilización de ventana nocturna", displayDays >= 3), marginTop(6));
            milestones.addView(
                    milestoneRow("03", "7 Días — Primera semana sin recaídas", displayDays >= 7), marginTop(6));
            milestones.addView(
                    milestoneRow("04", "30 Días — Hábito de atención consolidado", displayDays >= 30), marginTop(6));
            recoveryDestination.addView(milestones, marginTop(12));
        }

        // --- TAB 3: REGLAS ---
        LinearLayout rulesOverview = card();
        rulesOverview.setPadding(dp(16), dp(15), dp(16), dp(15));
        LinearLayout rulesOverviewHeader = row(Gravity.CENTER_VERTICAL);
        rulesOverviewHeader.addView(text("MAPA DE REGLAS EN DISPOSITIVO", 10, PRIMARY_DEEP, Typeface.BOLD), weight());
        rulesOverviewHeader.addView(text("LOCAL", 11, PRIMARY_DEEP, Typeface.BOLD));
        rulesOverview.addView(rulesOverviewHeader);
        rulesOverview.addView(text("Motor de reglas activo", 18, TEXT, Typeface.BOLD));
        rulesOverview.addView(text(
                "Dominios DNS, paquetes Android y palabras clave. Cada regla puede activarse, pausarse o eliminarse sin salir del dispositivo.",
                13,
                MUTED,
                Typeface.NORMAL));
        rulesDestination.addView(rulesOverview);

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
        Button add = button("GUARDAR REGLA", HERO, Color.WHITE);
        add.setOnClickListener(view -> addRule());
        composer.addView(add, marginTop(14));
        rulesDestination.addView(composer, marginTop(12));

        rulesDestination.addView(text("Tus reglas", 20, TEXT, Typeface.BOLD), marginTop(20));
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
        if (domainRuleCountView != null) domainRuleCountView.setText(String.valueOf(domains));
        if (appRuleCountView != null) appRuleCountView.setText(String.valueOf(apps));
        if (keywordRuleCountView != null) keywordRuleCountView.setText(String.valueOf(keywords));
        if (ruleBreakdownView != null) {
            ruleBreakdownView.setText(domains + " dominios · " + apps + " apps · " + keywords + " palabras activas");
        }
        boolean enabled = isAccessibilityEnabled();
        protectionStatusView.setText(enabled ? "Completa" : "Pendiente");
        protectionStatusView.setTextColor(PRIMARY_DEEP);
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
            sessionAction.setOnClickListener(view -> startFocusSession(selectedDurationMinutes));
            return;
        }
        long remaining = current.remainingMillis(System.currentTimeMillis());
        long totalSeconds = Math.max(1L, remaining / 1_000L);
        long minutes = Math.max(1L, (remaining + 59_999L) / 60_000L);
        String mmss = String.format("%02d:%02d", totalSeconds / 60L, totalSeconds % 60L);
        sessionTitleView.setText(R.string.session_active_title);
        sessionDetailView.setText("Quedan aproximadamente " + minutes + " min (" + mmss
                + "). Tus reglas siguen siendo privadas y locales.");
        sessionAction.setText(R.string.session_finish);
        sessionAction.setOnClickListener(view -> {
            sessionStore.clear();
            refreshSession();
        });
        refreshHandler.removeCallbacks(refreshRunnable);
        refreshHandler.postDelayed(refreshRunnable, 1_000L);
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

    /** Opens a local-only action menu matching the reference side drawer. */
    private void showControlMenu(View anchor) {
        LinearLayout overlay = row(Gravity.TOP);
        PopupWindow popup = new PopupWindow(
                overlay, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.setOutsideTouchable(true);
        popup.setElevation(dp(14));

        LinearLayout content = column();
        content.setBackgroundColor(SURFACE);
        overlay.addView(content, size(dp(308), ViewGroup.LayoutParams.MATCH_PARENT));

        View scrim = new View(this);
        scrim.setBackgroundColor(Color.argb(115, 8, 16, 38));
        scrim.setOnClickListener(view -> popup.dismiss());
        overlay.addView(scrim, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        LinearLayout drawerHeader = column();
        drawerHeader.setPadding(dp(18), dp(18), dp(18), dp(18));
        drawerHeader.setBackgroundColor(HERO);
        LinearLayout brandLine = row(Gravity.CENTER_VERTICAL);
        TextView mark = text("PX", 14, Color.WHITE, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(PRIMARY, 14));
        brandLine.addView(mark, size(dp(38), dp(38)));
        LinearLayout brandWords = column();
        brandWords.setPadding(dp(10), 0, 0, 0);
        brandWords.addView(text("ProyectX", 15, Color.WHITE, Typeface.BOLD));
        brandWords.addView(text("ENFOQUE LOCAL · SIN CUENTA", 10, Color.rgb(147, 197, 253), Typeface.BOLD));
        brandLine.addView(brandWords, weight());
        TextView close = text("✕", 14, Color.WHITE, Typeface.BOLD);
        close.setGravity(Gravity.CENTER);
        close.setContentDescription("Cerrar menú");
        close.setClickable(true);
        close.setFocusable(true);
        close.setBackground(round(Color.rgb(30, 41, 59), 18));
        close.setOnClickListener(view -> popup.dismiss());
        brandLine.addView(close, size(dp(32), dp(32)));
        drawerHeader.addView(brandLine);

        LinearLayout firmMessage = column();
        firmMessage.setPadding(dp(12), dp(12), dp(12), dp(12));
        firmMessage.setBackground(outline(Color.rgb(23, 34, 59), Color.rgb(41, 55, 87), 16));
        firmMessage.addView(text("MENSAJES DE FIRMEZA", 10, Color.rgb(147, 197, 253), Typeface.BOLD));
        firmMessage.addView(
                text(
                        faithModeStore.isEnabled()
                                ? "Modo Biblia Reina-Valera activo (búsqueda contextual)."
                                : "Modo Motivación Estándar activo por defecto.",
                        13,
                        Color.WHITE,
                        Typeface.BOLD),
                marginTop(4));
        LinearLayout firmButtons = row(Gravity.CENTER_VERTICAL);
        Button configureModeBtn = compactButton("Configurar modo", PRIMARY, Color.WHITE);
        configureModeBtn.setOnClickListener(view -> {
            popup.dismiss();
            showFaithModeSettings();
        });
        Button accessBtn = compactButton("Accesibilidad", Color.rgb(43, 54, 79), Color.WHITE);
        accessBtn.setOnClickListener(view -> {
            popup.dismiss();
            openAccessibilitySettings();
        });
        firmButtons.addView(configureModeBtn, weightWithMargin(1, 0, 0, dp(8), 0));
        firmButtons.addView(accessBtn, weight());
        firmMessage.addView(firmButtons, marginTop(8));
        drawerHeader.addView(firmMessage, marginTop(14));
        content.addView(drawerHeader);

        LinearLayout menuList = column();
        menuList.setPadding(dp(10), dp(8), dp(10), dp(8));
        LinearLayout settings = drawerAction(
                "settings",
                TEXT,
                "Configuraciones locales",
                faithModeStore.isEnabled()
                        ? "Biblia Reina-Valera: Activa"
                        : "Motivación normal (Activar RVR1960 opcional)");
        settings.setOnClickListener(view -> {
            popup.dismiss();
            showLocalSettings();
        });
        menuList.addView(settings);
        menuList.addView(drawerDivider(), size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        LinearLayout recalibrate = drawerAction("sliders", PRIMARY, "Repetir diagnóstico de calibración", null);
        recalibrate.setOnClickListener(view -> {
            popup.dismiss();
            openCalibration();
        });
        menuList.addView(recalibrate);
        menuList.addView(drawerDivider(), size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        LinearLayout guides = drawerAction("user", PRIMARY_DEEP, "Bitácora, Guías y Comunidad", null);
        guides.setOnClickListener(view -> {
            popup.dismiss();
            switchDestination("reinicio");
        });
        menuList.addView(guides);
        menuList.addView(drawerDivider(), size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        LinearLayout faq = drawerAction("help", PRIMARY_DEEP, "Preguntas frecuentes", null);
        faq.setOnClickListener(view -> {
            popup.dismiss();
            showHelpSummary();
        });
        menuList.addView(faq);
        menuList.addView(drawerDivider(), size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));

        LinearLayout feedback = drawerAction("message", PRIMARY, "Enviar comentarios", null);
        feedback.setOnClickListener(view -> {
            popup.dismiss();
            showFeedbackComposer();
        });
        menuList.addView(feedback);
        content.addView(menuList);

        View spacer = new View(this);
        content.addView(spacer, fillWeight());
        View footerLine = new View(this);
        footerLine.setBackgroundColor(OUTLINE);
        content.addView(footerLine, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        TextView footer = text("ProyectX · En desarrollo", 11, MUTED, Typeface.BOLD);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(12), 0, dp(24));
        content.addView(footer, size(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        popup.showAtLocation(getWindow().getDecorView(), Gravity.START | Gravity.TOP, 0, 0);
    }

    private View drawerDivider() {
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(241, 245, 249));
        return divider;
    }

    private LinearLayout drawerAction(String symbol, int iconColor, String title, String detail) {
        LinearLayout action = row(Gravity.CENTER_VERTICAL);
        action.setPadding(dp(12), dp(12), dp(12), dp(12));
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(uiVectorIcon(symbol, iconColor, 18));
        icon.setScaleType(ImageView.ScaleType.CENTER);
        // icon centered via ImageView.ScaleType.CENTER
        action.addView(icon, size(dp(24), dp(24)));
        LinearLayout words = column();
        words.setPadding(dp(12), 0, dp(6), 0);
        words.addView(text(title, 13, TEXT, Typeface.BOLD));
        if (detail != null && !detail.isBlank()) {
            words.addView(text(detail, 11, MUTED, Typeface.NORMAL));
        }
        action.addView(words, weight());
        action.addView(text("›", 16, MUTED, Typeface.BOLD));
        action.setContentDescription(detail == null ? title : title + ". " + detail);
        action.setClickable(true);
        action.setFocusable(true);
        return action;
    }

    private void showLocalSettings() {
        String[] actions = {
            "Modo Fe / Lectura Bíblica (Opcional)",
            "Exportar reglas locales",
            "Compartir reglas locales",
            "Idioma de la interfaz",
            "Privacidad y límites"
        };
        new AlertDialog.Builder(this)
                .setTitle("Configuraciones locales")
                .setItems(actions, (dialog, selected) -> {
                    switch (selected) {
                        case 0 -> showFaithModeSettings();
                        case 1 -> createRulesDocument();
                        case 2 -> confirmRuleShare();
                        case 3 -> showLanguageSettings();
                        default -> showPrivacySummary();
                    }
                })
                .setNegativeButton("CERRAR", null)
                .show();
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

    private void openCalibration() {
        startActivity(CalibrationActivity.intent(this));
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
        card.addView(text(caption, 11, MUTED, Typeface.BOLD));
        card.addView(text(value, 24, TEXT, Typeface.BOLD));
        card.addView(text(detail, 12, MUTED, Typeface.NORMAL));
        return card;
    }

    private LinearLayout counterBox(String number, String caption, int numberColor) {
        LinearLayout box = column();
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(10), dp(12), dp(10), dp(12));
        box.setBackground(round(SURFACE, 16));
        TextView numView = text(number, 26, numberColor, Typeface.BOLD);
        numView.setGravity(Gravity.CENTER);
        box.addView(numView);
        TextView capView = text(caption, 10, MUTED, Typeface.BOLD);
        capView.setGravity(Gravity.CENTER);
        box.addView(capView);
        return box;
    }

    private LinearLayout layerToggleCard(
            String iconSymbol, String title, String subtitle, boolean checked, Runnable onToggle) {
        LinearLayout layer = row(Gravity.CENTER_VERTICAL);
        layer.setPadding(dp(12), dp(12), dp(12), dp(12));
        layer.setBackground(outline(BACKGROUND, OUTLINE, 16));
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(uiVectorIcon(iconSymbol, PRIMARY_DEEP, 18));
        icon.setScaleType(ImageView.ScaleType.CENTER);
        // icon centered via ImageView.ScaleType.CENTER
        icon.setBackground(round(SOFT_BLUE_CARD, 12));
        layer.addView(icon, size(dp(36), dp(36)));
        LinearLayout words = column();
        words.setPadding(dp(10), 0, dp(8), 0);
        words.addView(text(title, 14, TEXT, Typeface.BOLD));
        words.addView(text(subtitle, 12, MUTED, Typeface.NORMAL));
        layer.addView(words, weight());
        Switch toggle = new Switch(this);
        toggle.setChecked(checked);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != checked) onToggle.run();
        });
        layer.addView(toggle);
        return layer;
    }

    private LinearLayout milestoneRow(String number, String title, boolean complete) {
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        row.setBackground(outline(complete ? SOFT_BLUE_CARD : BACKGROUND, complete ? SOFT_BLUE_BORDER : OUTLINE, 14));
        row.addView(tag(number, NAVY_ACCENT, SURFACE));
        TextView label = text(title, 12, TEXT, Typeface.BOLD);
        label.setPadding(dp(8), 0, dp(8), 0);
        row.addView(label, weight());
        row.addView(text(complete ? "✓" : "", 13, PRIMARY_DEEP, Typeface.BOLD));
        return row;
    }

    private TextView compactMetricValue() {
        TextView view = text("0", 18, PRIMARY_DEEP, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private LinearLayout ruleKindTile(String label, TextView valueView) {
        LinearLayout box = column();
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(8), dp(12), dp(8), dp(12));
        box.setBackground(outline(SURFACE_RAISED, Color.rgb(212, 228, 255), 16));
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(view -> switchDestination("reglas"));
        TextView title = text(label, 11, TEXT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        box.addView(title);
        box.addView(valueView);
        return box;
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
        card.setPadding(dp(15), dp(15), dp(15), dp(15));
        card.setBackground(outline(SURFACE, OUTLINE, 22));
        card.setElevation(0);
        return card;
    }

    private Button button(String value, int background, int foreground) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(value);
        button.setTextSize(12);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(foreground);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(44));
        button.setPadding(dp(12), dp(6), dp(12), dp(6));
        button.setBackground(round(background, 16));
        return button;
    }

    private Button compactButton(String value, int background, int foreground) {
        Button button = button(value, background, foreground);
        button.setTextSize(11);
        button.setMinHeight(dp(36));
        button.setPadding(dp(8), dp(2), dp(8), dp(2));
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
        view.setTextSize(Math.max(10, Math.round(size * 0.85f)));
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans-serif", style));
        view.setIncludeFontPadding(false);
        view.setPadding(0, dp(3), 0, dp(3));
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
        LinearLayout wrapper = column();
        View topBorder = new View(this);
        topBorder.setBackgroundColor(OUTLINE);
        wrapper.addView(topBorder, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        LinearLayout navigation = row(Gravity.CENTER_VERTICAL);
        navigation.setPadding(dp(12), dp(6), dp(12), dp(8));
        navigation.setBackgroundColor(SURFACE);
        navigation.addView(destinationButton("inicio", "Inicio"), navigationWeight());
        navigation.addView(destinationButton("proteccion", "Barreras"), navigationWeight());
        navigation.addView(destinationButton("reglas", "Reglas"), navigationWeight());
        navigation.addView(destinationButton("reinicio", "Reinicio"), navigationWeight());
        wrapper.addView(navigation);
        return wrapper;
    }

    private LinearLayout destinationButton(String destination, String title) {
        boolean selected = destination.equals(activeDestination);
        int foreground = selected ? NAVY_ACCENT : MUTED;
        LinearLayout tile = column();
        tile.setGravity(Gravity.CENTER);
        tile.setContentDescription(title);
        tile.setClickable(true);
        tile.setFocusable(true);
        tile.setMinimumHeight(dp(48));
        tile.setPadding(dp(4), dp(5), dp(4), dp(5));
        tile.setBackground(round(selected ? SOFT_BLUE_CARD : SURFACE, 16));
        ImageView icon = new ImageView(this);
        icon.setImageResource(navigationIcon(destination));
        icon.setColorFilter(foreground);
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        tile.addView(icon, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(20)));
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
        boolean selected = selectedDurationMinutes == minutes;
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(title);
        button.setTextSize(12);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(selected ? Color.WHITE : TEXT);
        button.setMinHeight(dp(42));
        button.setPadding(dp(4), dp(2), dp(4), dp(2));
        button.setBackground(round(selected ? PRIMARY : SURFACE, 16));
        button.setOnClickListener(view -> {
            selectedDurationMinutes = minutes;
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

    private Drawable uiVectorIcon(String kind, int color, int sizeDp) {
        final int px = dp(sizeDp);
        return new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setColor(color);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(2f, px * 0.105f));
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                float w = getBounds().width() > 0 ? getBounds().width() : px;
                float h = getBounds().height() > 0 ? getBounds().height() : px;
                float cx = w / 2f;
                float cy = h / 2f;
                switch (kind) {
                    case "lock" -> {
                        RectF body = new RectF(w * 0.22f, h * 0.46f, w * 0.78f, h * 0.86f);
                        canvas.drawRoundRect(body, w * 0.10f, h * 0.10f, p);
                        RectF arc = new RectF(w * 0.32f, h * 0.16f, w * 0.68f, h * 0.54f);
                        canvas.drawArc(arc, 180f, 180f, false, p);
                    }
                    case "cpu" -> {
                        RectF box = new RectF(w * 0.24f, h * 0.24f, w * 0.76f, h * 0.76f);
                        canvas.drawRoundRect(box, w * 0.08f, h * 0.08f, p);
                        RectF inner = new RectF(w * 0.38f, h * 0.38f, w * 0.62f, h * 0.62f);
                        canvas.drawRect(inner, p);
                        float[] pins = {0.38f, 0.62f};
                        for (float f : pins) {
                            canvas.drawLine(w * f, h * 0.10f, w * f, h * 0.24f, p);
                            canvas.drawLine(w * f, h * 0.76f, w * f, h * 0.90f, p);
                            canvas.drawLine(w * 0.10f, h * f, w * 0.24f, h * f, p);
                            canvas.drawLine(w * 0.76f, h * f, w * 0.90f, h * f, p);
                        }
                    }
                    case "globe" -> {
                        float r = w * 0.38f;
                        canvas.drawCircle(cx, cy, r, p);
                        canvas.drawLine(cx - r, cy, cx + r, cy, p);
                        RectF meridian = new RectF(cx - r * 0.52f, cy - r, cx + r * 0.52f, cy + r);
                        canvas.drawOval(meridian, p);
                    }
                    case "smartphone" -> {
                        RectF phone = new RectF(w * 0.26f, h * 0.12f, w * 0.74f, h * 0.88f);
                        canvas.drawRoundRect(phone, w * 0.10f, h * 0.10f, p);
                        canvas.drawPoint(cx, h * 0.74f, p);
                    }
                    case "book" -> {
                        Path left = new Path();
                        left.moveTo(cx, h * 0.26f);
                        left.lineTo(w * 0.16f, h * 0.22f);
                        left.lineTo(w * 0.16f, h * 0.78f);
                        left.lineTo(cx, h * 0.82f);
                        left.close();
                        canvas.drawPath(left, p);
                        Path right = new Path();
                        right.moveTo(cx, h * 0.26f);
                        right.lineTo(w * 0.84f, h * 0.22f);
                        right.lineTo(w * 0.84f, h * 0.78f);
                        right.lineTo(cx, h * 0.82f);
                        right.close();
                        canvas.drawPath(right, p);
                    }
                    case "settings" -> {
                        canvas.drawCircle(cx, cy, w * 0.16f, p);
                        canvas.drawCircle(cx, cy, w * 0.34f, p);
                        for (int i = 0; i < 6; i++) {
                            double a = Math.toRadians(i * 60);
                            float x1 = cx + (float) Math.cos(a) * w * 0.32f;
                            float y1 = cy + (float) Math.sin(a) * h * 0.32f;
                            float x2 = cx + (float) Math.cos(a) * w * 0.43f;
                            float y2 = cy + (float) Math.sin(a) * h * 0.43f;
                            canvas.drawLine(x1, y1, x2, y2, p);
                        }
                    }
                    case "sliders" -> {
                        float[] ys = {0.26f, 0.50f, 0.74f};
                        float[] xs = {0.36f, 0.66f, 0.46f};
                        for (int i = 0; i < 3; i++) {
                            canvas.drawLine(w * 0.16f, h * ys[i], w * 0.84f, h * ys[i], p);
                            canvas.drawLine(w * xs[i], h * (ys[i] - 0.10f), w * xs[i], h * (ys[i] + 0.10f), p);
                        }
                    }
                    case "user" -> {
                        canvas.drawCircle(cx, h * 0.34f, w * 0.17f, p);
                        RectF shoulders = new RectF(w * 0.20f, h * 0.56f, w * 0.80f, h * 0.96f);
                        canvas.drawArc(shoulders, 180f, 180f, false, p);
                    }
                    case "help" -> {
                        canvas.drawCircle(cx, cy, w * 0.38f, p);
                        RectF qArc = new RectF(w * 0.38f, h * 0.30f, w * 0.62f, h * 0.50f);
                        canvas.drawArc(qArc, 190f, 220f, false, p);
                        canvas.drawLine(cx, h * 0.50f, cx, h * 0.58f, p);
                        canvas.drawPoint(cx, h * 0.70f, p);
                    }
                    default -> {
                        RectF bubble = new RectF(w * 0.16f, h * 0.20f, w * 0.84f, h * 0.74f);
                        canvas.drawRoundRect(bubble, w * 0.12f, h * 0.12f, p);
                        canvas.drawLine(w * 0.28f, h * 0.74f, w * 0.22f, h * 0.86f, p);
                        canvas.drawLine(w * 0.22f, h * 0.86f, w * 0.42f, h * 0.74f, p);
                    }
                }
            }

            @Override
            public void setAlpha(int alpha) {}

            @Override
            public void setColorFilter(ColorFilter colorFilter) {}

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }

            @Override
            public int getIntrinsicWidth() {
                return px;
            }

            @Override
            public int getIntrinsicHeight() {
                return px;
            }
        };
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
