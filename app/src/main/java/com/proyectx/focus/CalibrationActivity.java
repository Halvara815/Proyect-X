package com.proyectx.focus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** First-run local preference flow used to tailor the dashboard without changing barriers. */
public final class CalibrationActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(246, 248, 252);
    private static final int SURFACE = Color.WHITE;
    private static final int RAISED = Color.rgb(238, 244, 255);
    private static final int PRIMARY = Color.rgb(55, 120, 246);
    private static final int INK = Color.rgb(8, 16, 38);
    private static final int TEXT = Color.rgb(10, 19, 41);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int OUTLINE = Color.rgb(226, 232, 240);

    private int step;
    private String goal = "Reducir distracciones";
    private String window = "Noche";
    private String mode = "Equilibrado";
    private CalibrationStore store;

    public static Intent intent(Context context) {
        return new Intent(context, CalibrationActivity.class);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new CalibrationStore(this);
        CalibrationStore.Profile prior = store.read();
        if (prior.hasSelections()) {
            goal = prior.goal();
            window = prior.window();
            mode = prior.mode();
        }
        if (state != null) {
            step = state.getInt("step", 0);
            goal = state.getString("goal", goal);
            window = state.getString("window", window);
            mode = state.getString("mode", mode);
        }
        render();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("step", step);
        state.putString("goal", goal);
        state.putString("window", window);
        state.putString("mode", mode);
    }

    private void render() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        LinearLayout page = column();
        page.setPadding(dp(18), dp(20), dp(18), dp(28));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        LinearLayout top = row(Gravity.CENTER_VERTICAL);
        Button back = compactButton(step == 0 ? "×" : "‹", RAISED, TEXT);
        back.setTextSize(22);
        back.setContentDescription(step == 0 ? "Cerrar calibración" : "Paso anterior");
        back.setOnClickListener(view -> {
            if (step == 0) finish();
            else {
                step--;
                render();
            }
        });
        top.addView(back, size(dp(42), dp(42)));
        LinearLayout headings = column();
        headings.setPadding(dp(10), 0, 0, 0);
        headings.addView(text("Calibración local", 19, TEXT, Typeface.BOLD));
        headings.addView(text("PROYECT X · SIN CUENTA", 10, PRIMARY, Typeface.BOLD));
        top.addView(headings, weight());
        TextView status = tag(step == 0 ? "INICIO" : "PASO " + step + " DE 4", PRIMARY, RAISED);
        top.addView(status);
        page.addView(top);

        if (step > 0) page.addView(progress(), marginTop(16));
        if (step == 0) renderWelcome(page);
        else if (step == 1)
            renderChoice(
                    page,
                    "¿Qué quieres proteger primero?",
                    "Elige una intención. Esto no crea reglas ni enciende permisos.",
                    new String[] {"Reducir distracciones", "Proteger navegación", "Concentrarme en una tarea"},
                    goal,
                    value -> goal = value);
        else if (step == 2)
            renderChoice(
                    page,
                    "¿Cuándo te conviene más apoyo?",
                    "Usaremos esto sólo para mostrarte recomendaciones locales en la interfaz.",
                    new String[] {"Mañana", "Tarde", "Noche", "Horario variable"},
                    window,
                    value -> window = value);
        else if (step == 3)
            renderChoice(
                    page,
                    "¿Cómo quieres empezar?",
                    "Podrás cambiarlo más adelante. Tus barreras siguen siendo decisiones separadas.",
                    new String[] {"Equilibrado", "Concentrado", "Estricto"},
                    mode,
                    value -> mode = value);
        else renderSummary(page);
        setContentView(scroll);
    }

    private void renderWelcome(LinearLayout page) {
        LinearLayout hero = card(INK, INK);
        hero.addView(tag("CONFIGURACIÓN INICIAL", Color.rgb(207, 225, 255), Color.rgb(24, 39, 74)));
        hero.addView(
                text("Construye un espacio que te ayude a volver a lo importante.", 27, Color.WHITE, Typeface.BOLD));
        hero.addView(text(
                "Responder toma menos de un minuto. Tus respuestas se guardan sólo en este dispositivo y no son un diagnóstico médico.",
                15,
                Color.rgb(207, 225, 255),
                Typeface.NORMAL));
        page.addView(hero, marginTop(24));

        LinearLayout note = card(SURFACE, OUTLINE);
        note.addView(text("Qué cambia", 16, TEXT, Typeface.BOLD));
        note.addView(text(
                "El inicio mostrará un perfil y accesos más útiles. No activaremos Accesibilidad, DNS, VPN ni reglas sin tu confirmación.",
                14,
                MUTED,
                Typeface.NORMAL));
        page.addView(note, marginTop(14));

        Button start = button("COMENZAR CALIBRACIÓN", INK, Color.WHITE);
        start.setOnClickListener(view -> {
            step = 1;
            render();
        });
        page.addView(start, marginTop(20));
        Button skip = button("IR A INICIO POR AHORA", SURFACE, TEXT);
        skip.setBackground(outline(SURFACE, OUTLINE, 14));
        skip.setOnClickListener(view -> {
            store.skip();
            finish();
        });
        page.addView(skip, marginTop(8));
    }

    private void renderChoice(
            LinearLayout page,
            String title,
            String description,
            String[] options,
            String selected,
            ChoiceListener listener) {
        page.addView(text(title, 25, TEXT, Typeface.BOLD), marginTop(24));
        page.addView(text(description, 15, MUTED, Typeface.NORMAL), marginTop(6));
        for (String option : options) {
            boolean active = option.equals(selected);
            Button choice = button(option, active ? RAISED : SURFACE, active ? PRIMARY : TEXT);
            choice.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            choice.setBackground(outline(active ? RAISED : SURFACE, active ? PRIMARY : OUTLINE, 18));
            choice.setOnClickListener(view -> {
                listener.select(option);
                render();
            });
            page.addView(choice, marginTop(12));
        }
        Button next = button(step == 3 ? "VER MI PERFIL" : "CONTINUAR", INK, Color.WHITE);
        next.setOnClickListener(view -> {
            step++;
            render();
        });
        page.addView(next, marginTop(22));
        Button skip = compactButton("OMITIR POR AHORA", SURFACE, MUTED);
        skip.setOnClickListener(view -> {
            store.skip();
            finish();
        });
        page.addView(skip, marginTop(8));
    }

    private void renderSummary(LinearLayout page) {
        page.addView(text("Tu perfil está listo", 25, TEXT, Typeface.BOLD), marginTop(24));
        page.addView(
                text(
                        "Puedes cambiar estas preferencias cuando quieras desde el centro local.",
                        15,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(6));
        LinearLayout result = card(INK, INK);
        result.addView(tag("PERFIL LOCAL", Color.rgb(207, 225, 255), Color.rgb(24, 39, 74)));
        result.addView(text(goal, 21, Color.WHITE, Typeface.BOLD));
        result.addView(text(
                "Momento preferido: " + window + " · Inicio: " + mode, 14, Color.rgb(207, 225, 255), Typeface.NORMAL));
        page.addView(result, marginTop(18));
        LinearLayout notice = card(SURFACE, OUTLINE);
        notice.addView(text("El control sigue en tus manos", 16, TEXT, Typeface.BOLD));
        notice.addView(text(
                "Guardar este perfil no modifica reglas, aplicaciones, sitios, permisos ni tu conexión. Podrás revisar cada barrera por separado.",
                14,
                MUTED,
                Typeface.NORMAL));
        page.addView(notice, marginTop(14));
        Button saveButton = button("GUARDAR PERFIL LOCAL", PRIMARY, Color.WHITE);
        saveButton.setOnClickListener(view -> {
            store.save(new CalibrationStore.Profile(goal, window, mode));
            finish();
        });
        page.addView(saveButton, marginTop(22));
    }

    private LinearLayout progress() {
        LinearLayout row = row(Gravity.CENTER_VERTICAL);
        for (int index = 1; index <= 4; index++) {
            TextView segment = new TextView(this);
            segment.setBackground(round(index <= step ? PRIMARY : OUTLINE, 6));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(6), 1f);
            if (index < 4) params.rightMargin = dp(5);
            row.addView(segment, params);
        }
        return row;
    }

    private LinearLayout card(int color, int outline) {
        LinearLayout card = column();
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(outline(color, outline, 22));
        return card;
    }

    private Button button(String value, int background, int foreground) {
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setText(value);
        button.setTextSize(14);
        button.setTextColor(foreground);
        button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(50));
        button.setPadding(dp(14), dp(6), dp(14), dp(6));
        button.setBackground(round(background, 14));
        return button;
    }

    private Button compactButton(String value, int background, int foreground) {
        Button button = button(value, background, foreground);
        button.setMinHeight(dp(40));
        button.setTextSize(11);
        return button;
    }

    private TextView tag(String value, int foreground, int background) {
        TextView tag = text(value, 10, foreground, Typeface.BOLD);
        tag.setGravity(Gravity.CENTER);
        tag.setPadding(dp(10), dp(5), dp(10), dp(5));
        tag.setBackground(round(background, 30));
        return tag;
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

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout row(int gravity) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(gravity);
        return layout;
    }

    private LinearLayout.LayoutParams marginTop(int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(top);
        return params;
    }

    private LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, -2, 1f);
    }

    private LinearLayout.LayoutParams size(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
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

    private interface ChoiceListener {
        void select(String value);
    }
}
