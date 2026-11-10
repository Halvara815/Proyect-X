package com.proyectx.focus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;

/** Local-only RV1909 reader enabled by the person's explicit faith-mode setting. */
public final class BibleReaderActivity extends Activity {
    private static final String STATE_TIMER_STARTED_AT = "timer_started_at";
    private static final int BACKGROUND = Color.rgb(12, 18, 32);
    private static final int SURFACE = Color.rgb(21, 30, 48);
    private static final int PRIMARY = Color.rgb(104, 228, 168);
    private static final int TEXT = Color.rgb(245, 247, 251);
    private static final int MUTED = Color.rgb(174, 187, 207);
    private static final int OUTLINE = Color.rgb(47, 62, 86);
    private static final int TAB_CALM = 0;
    private static final int TAB_COMPLETE = 1;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Runnable cooldownTick = this::updateCooldown;
    private FaithModeStore faithModeStore;
    private BibleRepository repository;
    private LinearLayout root;
    private LinearLayout tabContent;
    private Button calmTab;
    private Button completeTab;
    private TextView timerView;
    private TextView victoryView;
    private ProgressBar cooldownProgress;
    private long timerStartedAt;
    private int activeTab = TAB_CALM;
    private String selectedBookName = "Salmos";
    private int selectedChapter = 23;

    public static Intent intent(Context context) {
        return new Intent(context, BibleReaderActivity.class);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        faithModeStore = new FaithModeStore(this);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);
        timerStartedAt = state == null ? SystemClock.elapsedRealtime() : state.getLong(STATE_TIMER_STARTED_AT);
        if (timerStartedAt <= 0L) timerStartedAt = SystemClock.elapsedRealtime();
        if (!faithModeStore.isEnabled()) {
            renderDisabled();
            return;
        }
        renderLoading();
        loader.execute(() -> {
            try {
                BibleRepository loaded = BibleRepository.load(this);
                runOnUiThread(() -> {
                    repository = loaded;
                    renderReader();
                });
            } catch (IOException | JSONException exception) {
                runOnUiThread(() -> renderLoadFailure(exception));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (faithModeStore != null && faithModeStore.isEnabled()) updateCooldown();
    }

    @Override
    protected void onPause() {
        timerHandler.removeCallbacks(cooldownTick);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        loader.shutdownNow();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_TIMER_STARTED_AT, timerStartedAt);
    }

    private void renderLoading() {
        root = screen();
        root.addView(header("Refugio", "Lectura contra la ansiedad"));
        root.addView(text("Cargando Biblia RV1909 local…", 16, MUTED, Typeface.NORMAL), marginTop(30));
        showRoot();
    }

    private void renderDisabled() {
        root = screen();
        root.addView(header("Refugio", "Lectura contra la ansiedad"));
        LinearLayout card = card();
        card.addView(text("Lectura bíblica opcional", 22, TEXT, Typeface.BOLD));
        card.addView(
                text(
                        "Activa “Modo Fe / Lectura Bíblica (Opcional)” desde el centro local para abrir este módulo.",
                        15,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(8));
        Button back = button("VOLVER", Color.rgb(35, 49, 76), TEXT);
        back.setOnClickListener(view -> finish());
        card.addView(back, marginTop(18));
        root.addView(card, marginTop(28));
        showRoot();
    }

    private void renderLoadFailure(Exception exception) {
        root = screen();
        root.addView(header("Refugio", "Lectura contra la ansiedad"));
        LinearLayout card = card();
        card.addView(text("No se pudo abrir la Biblia local", 22, TEXT, Typeface.BOLD));
        card.addView(
                text(
                        "El archivo RV1909 incluido en la app no pudo validarse. Ningún contenido se descargó ni se envió.",
                        15,
                        MUTED,
                        Typeface.NORMAL),
                marginTop(8));
        Button back = button("VOLVER", Color.rgb(35, 49, 76), TEXT);
        back.setOnClickListener(view -> finish());
        card.addView(back, marginTop(18));
        root.addView(card, marginTop(28));
        showRoot();
    }

    private void renderReader() {
        root = screen();
        root.addView(header("Refugio", "Lectura contra la ansiedad"));
        root.addView(cooldownCard(), marginTop(18));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        calmTab = button("CALMAR MI MENTE", SURFACE, TEXT);
        calmTab.setOnClickListener(view -> selectTab(TAB_CALM));
        tabs.addView(calmTab, weight(1, dp(4), 0));
        completeTab = button("BIBLIA COMPLETA", SURFACE, TEXT);
        completeTab.setOnClickListener(view -> selectTab(TAB_COMPLETE));
        tabs.addView(completeTab, weight(1, 0, dp(4)));
        root.addView(tabs, marginTop(18));

        ScrollView readingScroll = new ScrollView(this);
        readingScroll.setFillViewport(true);
        tabContent = new LinearLayout(this);
        tabContent.setOrientation(LinearLayout.VERTICAL);
        tabContent.setPadding(0, dp(12), 0, dp(24));
        readingScroll.addView(
                tabContent,
                new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(readingScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        selectTab(activeTab);
        updateCooldown();
    }

    private LinearLayout cooldownCard() {
        LinearLayout card = card();
        TextView eyebrow = text("ENFRIADOR DE IMPULSOS", 11, PRIMARY, Typeface.BOLD);
        card.addView(eyebrow);
        LinearLayout timerRow = new LinearLayout(this);
        timerRow.setGravity(Gravity.CENTER_VERTICAL);
        timerView = text("2:00", 30, TEXT, Typeface.BOLD);
        timerRow.addView(timerView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        timerRow.addView(text("LECTURA LOCAL", 11, PRIMARY, Typeface.BOLD));
        card.addView(timerRow, marginTop(4));
        card.addView(text(
                "Quédate leyendo este capítulo durante 2 minutos hasta que pase el impulso",
                15,
                MUTED,
                Typeface.NORMAL));
        cooldownProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        cooldownProgress.setMax(BibleReaderLogic.IMPULSE_COOLDOWN_SECONDS);
        cooldownProgress.setProgress(BibleReaderLogic.IMPULSE_COOLDOWN_SECONDS);
        cooldownProgress.setProgressTintList(android.content.res.ColorStateList.valueOf(PRIMARY));
        cooldownProgress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(OUTLINE));
        card.addView(cooldownProgress, marginTop(14));
        victoryView = text("¡Lo lograste! Has vencido una tentación hoy", 16, PRIMARY, Typeface.BOLD);
        victoryView.setVisibility(View.GONE);
        card.addView(victoryView, marginTop(12));
        return card;
    }

    private void updateCooldown() {
        if (timerView == null) return;
        long elapsed = Math.max(0L, SystemClock.elapsedRealtime() - timerStartedAt);
        int remaining = BibleReaderLogic.remainingCooldownSeconds(elapsed);
        timerView.setText(String.format(java.util.Locale.ROOT, "%d:%02d", remaining / 60, remaining % 60));
        if (cooldownProgress != null) cooldownProgress.setProgress(remaining);
        if (remaining == 0) {
            if (victoryView != null && victoryView.getVisibility() != View.VISIBLE) {
                victoryView.setVisibility(View.VISIBLE);
                AlphaAnimation victory = new AlphaAnimation(0f, 1f);
                victory.setDuration(500L);
                victoryView.startAnimation(victory);
            }
            return;
        }
        timerHandler.removeCallbacks(cooldownTick);
        timerHandler.postDelayed(cooldownTick, 1_000L);
    }

    private void selectTab(int tab) {
        activeTab = tab;
        if (calmTab == null || completeTab == null || tabContent == null) return;
        boolean calming = tab == TAB_CALM;
        calmTab.setBackground(round(calming ? PRIMARY : SURFACE, 12));
        calmTab.setTextColor(calming ? BACKGROUND : TEXT);
        completeTab.setBackground(round(calming ? SURFACE : PRIMARY, 12));
        completeTab.setTextColor(calming ? TEXT : BACKGROUND);
        tabContent.removeAllViews();
        if (calming) renderCalmMind();
        else renderCompleteBible();
    }

    private void renderCalmMind() {
        tabContent.addView(text("Calmar mi mente ahora", 22, TEXT, Typeface.BOLD));
        tabContent.addView(text("Abre un capítulo sin buscar ni configurar nada más.", 14, MUTED, Typeface.NORMAL));
        addQuickPassage("Paz y protección", "Salmo 23", "Salmos", 23);
        addQuickPassage("Paz y protección", "Salmo 91", "Salmos", 91);
        addQuickPassage("Paz y protección", "Salmo 121", "Salmos", 121);
        addQuickPassage("Tras una caída", "Salmo 51", "Salmos", 51);
        addQuickPassage("Ansiedad y afán", "Mateo 6:25–34", "Mateo", 6);
        addQuickPassage("Ansiedad y afán", "Filipenses 4", "Filipenses", 4);
        addQuickPassage("Fortaleza mental y espiritual", "Romanos 8", "Romanos", 8);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        addQuickPassage("Sabiduría diaria", "Proverbios del día · capítulo " + day, "Proverbios", day);
    }

    private void addQuickPassage(String category, String title, String book, int chapter) {
        Button passage = button(category + "\n" + title + "  ›", SURFACE, TEXT);
        passage.setAllCaps(false);
        passage.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        passage.setTextSize(16);
        passage.setContentDescription(category + ". Abrir " + title);
        passage.setOnClickListener(view -> openPassage(book, chapter));
        tabContent.addView(passage, marginTop(10));
    }

    private void openPassage(String book, int chapter) {
        selectedBookName = book;
        selectedChapter = chapter;
        selectTab(TAB_COMPLETE);
    }

    private void renderCompleteBible() {
        tabContent.addView(text("Biblia completa", 22, TEXT, Typeface.BOLD));
        tabContent.addView(text("Reina-Valera 1909 · archivo local · dominio público", 13, MUTED, Typeface.NORMAL));

        BibleRepository.Book book = repository.findBook(selectedBookName);
        if (book == null) book = repository.books().get(0);
        selectedBookName = book.name();
        if (selectedChapter < 1 || selectedChapter > book.chapters().size()) selectedChapter = 1;

        tabContent.addView(text("LIBRO", 11, PRIMARY, Typeface.BOLD), marginTop(14));
        Spinner bookSpinner = spinner(bookNames(), selectedBookName);
        bookSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String nextBook = (String) parent.getItemAtPosition(position);
                if (nextBook.equals(selectedBookName)) return;
                selectedBookName = nextBook;
                selectedChapter = 1;
                renderCompleteBible();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        tabContent.addView(bookSpinner);
        tabContent.addView(text("CAPÍTULO", 11, PRIMARY, Typeface.BOLD), marginTop(12));
        Spinner chapterSpinner = spinner(chapterNames(book), String.valueOf(selectedChapter));
        chapterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int nextChapter = position + 1;
                if (nextChapter == selectedChapter) return;
                selectedChapter = nextChapter;
                renderCompleteBible();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        tabContent.addView(chapterSpinner);
        renderChapter(book.chapter(selectedChapter));
    }

    private void renderChapter(BibleRepository.Chapter chapter) {
        if (chapter == null) {
            tabContent.addView(text("El capítulo no está disponible en el archivo local.", 15, MUTED, Typeface.NORMAL));
            return;
        }
        LinearLayout chapterCard = card();
        chapterCard.addView(text(selectedBookName + " " + chapter.number(), 20, TEXT, Typeface.BOLD));
        for (BibleRepository.Verse verse : chapter.verses()) {
            TextView verseView = text(verse.number() + "  " + verse.text(), 20, TEXT, Typeface.NORMAL);
            verseView.setLineSpacing(dp(5), 1f);
            chapterCard.addView(verseView, marginTop(14));
        }
        tabContent.addView(chapterCard, marginTop(16));
    }

    private List<String> bookNames() {
        List<String> names = new ArrayList<>();
        for (BibleRepository.Book book : repository.books()) names.add(book.name());
        return names;
    }

    private List<String> chapterNames(BibleRepository.Book book) {
        List<String> names = new ArrayList<>();
        for (BibleRepository.Chapter chapter : book.chapters()) names.add(String.valueOf(chapter.number()));
        return names;
    }

    private Spinner spinner(List<String> choices, String selected) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                return optionView(position, convertView, parent, false);
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                return optionView(position, convertView, parent, true);
            }

            private View optionView(int position, View convertView, android.view.ViewGroup parent, boolean dropdown) {
                TextView option = convertView instanceof TextView
                        ? (TextView) convertView
                        : new TextView(BibleReaderActivity.this);
                option.setText(getItem(position));
                option.setTextSize(16);
                option.setTextColor(TEXT);
                option.setPadding(dp(14), dp(12), dp(14), dp(12));
                option.setBackgroundColor(dropdown ? SURFACE : BACKGROUND);
                return option;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setBackground(round(SURFACE, 12));
        spinner.setSelection(Math.max(0, choices.indexOf(selected)));
        return spinner;
    }

    private LinearLayout screen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(20));
        root.setBackgroundColor(BACKGROUND);
        return root;
    }

    private void showRoot() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        scroll.addView(
                root,
                new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
    }

    private LinearLayout header(String title, String subtitle) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        Button back = button("‹", SURFACE, TEXT);
        back.setTextSize(28);
        back.setContentDescription("Volver");
        back.setOnClickListener(view -> finish());
        header.addView(back, size(dp(52), dp(52)));
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(14), 0, 0, 0);
        labels.addView(text(title, 26, TEXT, Typeface.BOLD));
        labels.addView(text(subtitle, 13, MUTED, Typeface.NORMAL));
        header.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return header;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(SURFACE, 20));
        return card;
    }

    private Button button(String label, int background, int foreground) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        button.setTextColor(foreground);
        button.setMinHeight(dp(48));
        button.setPadding(dp(10), dp(8), dp(10), dp(8));
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
        view.setPadding(0, dp(3), 0, dp(3));
        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout.LayoutParams marginTop(int top) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(top);
        return params;
    }

    private LinearLayout.LayoutParams weight(int value, int left, int right) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, value);
        params.leftMargin = left;
        params.rightMargin = right;
        return params;
    }

    private LinearLayout.LayoutParams size(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
