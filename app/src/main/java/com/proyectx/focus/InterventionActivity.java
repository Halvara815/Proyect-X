package com.proyectx.focus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Local, dismissible explanation shown after an enabled app-block rule matches. */
public final class InterventionActivity extends Activity {
    public static final String EXTRA_PACKAGE_NAME = "com.proyectx.focus.extra.PACKAGE_NAME";
    public static final String EXTRA_REASON = "com.proyectx.focus.extra.REASON";
    public static final String REASON_RULE = "rule";
    public static final String REASON_DAILY_BUDGET = "daily_budget";

    private static final int BACKGROUND = Color.rgb(12, 18, 32);
    private static final int SURFACE = Color.rgb(21, 30, 48);
    private static final int PRIMARY = Color.rgb(104, 228, 168);
    private static final int TEXT = Color.rgb(245, 247, 251);
    private static final int MUTED = Color.rgb(174, 187, 207);

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);

        String packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        boolean dailyBudget = REASON_DAILY_BUDGET.equals(getIntent().getStringExtra(EXTRA_REASON));
        String appName = appName(packageName);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));
        root.setBackgroundColor(BACKGROUND);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(22), dp(22), dp(22), dp(22));
        card.setBackground(round(SURFACE, 22));
        TextView badge = text("PAUSA INTENCIONAL", 11, PRIMARY, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(6), dp(10), dp(6));
        badge.setBackground(round(BACKGROUND, 40));
        card.addView(
                badge,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(text("Vuelve a elegir tu atención.", 27, TEXT, Typeface.BOLD), marginTop(18));
        card.addView(
                text(
                        dailyBudget
                                ? appName + " alcanzó el presupuesto diario que configuraste."
                                : appName + " está bloqueada por una regla local activa.",
                        16,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(8));
        card.addView(
                text(
                        dailyBudget
                                ? "Solo se consultó el total de uso de hoy en este dispositivo. Puedes quitar el presupuesto o revocar Acceso de uso cuando quieras."
                                : "No se leyó contenido, URL ni texto de la aplicación. Puedes revisar o quitar esta regla cuando quieras.",
                        13,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(18));

        if (new FaithModeStore(this).isEnabled()) {
            LinearLayout refuge = new LinearLayout(this);
            refuge.setOrientation(LinearLayout.VERTICAL);
            refuge.setPadding(dp(14), dp(14), dp(14), dp(14));
            refuge.setBackground(round(Color.rgb(35, 49, 76), 16));
            refuge.addView(text("REFUGIO · LECTURA OPCIONAL", 10, PRIMARY, Typeface.BOLD));
            refuge.addView(
                    text(
                            "Filipenses 4:6 · Por nada estéis afanosos; sino sean notorias vuestras peticiones delante de Dios en toda oración y ruego, con hacimiento de gracias.",
                            15,
                            TEXT,
                            Typeface.NORMAL),
                    marginTop(6));
            Button openReader = button("ABRIR LECTURA", PRIMARY, BACKGROUND);
            openReader.setOnClickListener(view -> startActivity(BibleReaderActivity.intent(this)));
            refuge.addView(openReader, marginTop(12));
            card.addView(refuge, marginTop(16));
        }

        Button home = button("IR AL INICIO", PRIMARY, BACKGROUND);
        home.setOnClickListener(view -> openHome());
        card.addView(home, marginTop(20));
        Button rules = button("REVISAR MIS REGLAS", Color.rgb(35, 49, 76), TEXT);
        rules.setOnClickListener(view -> openRules());
        card.addView(rules, marginTop(10));
        root.addView(
                card,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }

    @Override
    @SuppressLint("GestureBackNavigation") // Manifest opts out so this focus screen retains a predictable back action.
    public void onBackPressed() {
        openHome();
    }

    private String appName(String packageName) {
        if (packageName == null || packageName.isBlank()) return "Esta aplicación";
        try {
            CharSequence label =
                    getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(packageName, 0));
            return label == null ? packageName : label.toString();
        } catch (PackageManager.NameNotFoundException ignored) {
            return packageName;
        }
    }

    private void openHome() {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
        finish();
    }

    private void openRules() {
        Intent main = new Intent(this, MainActivity.class);
        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(main);
        finish();
    }

    private Button button(String value, int background, int foreground) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(value);
        button.setTextSize(13);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(foreground);
        button.setMinHeight(dp(48));
        button.setBackground(round(background, 12));
        return button;
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
