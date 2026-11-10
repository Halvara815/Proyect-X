package com.proyectx.focus;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reads the bundled RV1909 asset only. Its schema is metadata -> books -> chapters -> [verse, text].
 * The parser does not fetch, send, or log reading selections.
 */
public final class BibleRepository {
    public static final String ASSET_NAME = "rv1909.json";
    private final List<Book> books;

    private BibleRepository(List<Book> books) {
        this.books = Collections.unmodifiableList(new ArrayList<>(books));
    }

    public static BibleRepository load(Context context) throws IOException, JSONException {
        String json = readAsset(context, ASSET_NAME);
        JSONObject root = new JSONObject(json);
        JSONArray sourceBooks = root.getJSONArray("libros");
        List<Book> books = new ArrayList<>();
        for (int bookIndex = 0; bookIndex < sourceBooks.length(); bookIndex++) {
            JSONObject sourceBook = sourceBooks.getJSONObject(bookIndex);
            JSONArray sourceChapters = sourceBook.getJSONArray("capitulos");
            List<Chapter> chapters = new ArrayList<>();
            for (int chapterIndex = 0; chapterIndex < sourceChapters.length(); chapterIndex++) {
                JSONArray sourceChapter = sourceChapters.getJSONArray(chapterIndex);
                List<Verse> verses = new ArrayList<>();
                for (int verseIndex = 0; verseIndex < sourceChapter.length(); verseIndex++) {
                    JSONArray sourceVerse = sourceChapter.getJSONArray(verseIndex);
                    verses.add(new Verse(sourceVerse.getInt(0), sourceVerse.getString(1)));
                }
                chapters.add(new Chapter(chapterIndex + 1, verses));
            }
            books.add(new Book(sourceBook.getInt("id"), sourceBook.getString("nombre"), chapters));
        }
        if (books.isEmpty()) throw new JSONException("La Biblia local no contiene libros.");
        return new BibleRepository(books);
    }

    public List<Book> books() {
        return books;
    }

    public Book findBook(String name) {
        for (Book book : books) {
            if (book.name().equalsIgnoreCase(name)) return book;
        }
        return null;
    }

    private static String readAsset(Context context, String assetName) throws IOException {
        try (InputStream input = context.getAssets().open(assetName);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8_192];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    public static final class Book {
        private final int id;
        private final String name;
        private final List<Chapter> chapters;

        private Book(int id, String name, List<Chapter> chapters) {
            this.id = id;
            this.name = name;
            this.chapters = Collections.unmodifiableList(new ArrayList<>(chapters));
        }

        public int id() {
            return id;
        }

        public String name() {
            return name;
        }

        public List<Chapter> chapters() {
            return chapters;
        }

        public Chapter chapter(int number) {
            return number >= 1 && number <= chapters.size() ? chapters.get(number - 1) : null;
        }
    }

    public static final class Chapter {
        private final int number;
        private final List<Verse> verses;

        private Chapter(int number, List<Verse> verses) {
            this.number = number;
            this.verses = Collections.unmodifiableList(new ArrayList<>(verses));
        }

        public int number() {
            return number;
        }

        public List<Verse> verses() {
            return verses;
        }
    }

    public static final class Verse {
        private final int number;
        private final String text;

        private Verse(int number, String text) {
            this.number = number;
            this.text = text;
        }

        public int number() {
            return number;
        }

        public String text() {
            return text;
        }
    }
}
