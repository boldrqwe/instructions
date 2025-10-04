package com.example.instructions.common;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Утилита для генерации URL-совместимых слагов.
 */
public final class SlugGenerator {

    private static final Pattern NON_LATIN = Pattern.compile("[^a-z0-9-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Map<Character, String> CYRILLIC_MAP = buildCyrillicMap();

    private SlugGenerator() {
    }

    /**
     * Преобразует произвольную строку в слаг.
     *
     * @param input исходная строка
     * @return транслитерованный слаг в нижнем регистре
     */
    public static String fromText(String input) {
        if (input == null || input.isBlank()) {
            throw new BadRequestException("Нельзя сгенерировать слаг из пустой строки");
        }
        String lower = transliterate(input).toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(
                WHITESPACE.matcher(normalized).replaceAll("-")
        ).replaceAll("");
        slug = slug.replaceAll("-+", "-");
        return slug.startsWith("-") ? slug.substring(1) : slug;
    }

    private static String transliterate(String input) {
        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            String replacement = CYRILLIC_MAP.get(c);
            if (replacement != null) {
                builder.append(replacement);
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static Map<Character, String> buildCyrillicMap() {
        Map<Character, String> map = new HashMap<>();
        map.put('а', "a");
        map.put('б', "b");
        map.put('в', "v");
        map.put('г', "g");
        map.put('д', "d");
        map.put('е', "e");
        map.put('ё', "e");
        map.put('ж', "zh");
        map.put('з', "z");
        map.put('и', "i");
        map.put('й', "y");
        map.put('к', "k");
        map.put('л', "l");
        map.put('м', "m");
        map.put('н', "n");
        map.put('о', "o");
        map.put('п', "p");
        map.put('р', "r");
        map.put('с', "s");
        map.put('т', "t");
        map.put('у', "u");
        map.put('ф', "f");
        map.put('х', "h");
        map.put('ц', "ts");
        map.put('ч', "ch");
        map.put('ш', "sh");
        map.put('щ', "sch");
        map.put('ы', "y");
        map.put('э', "e");
        map.put('ю', "yu");
        map.put('я', "ya");
        map.put('ъ', "");
        map.put('ь', "");
        map.put('А', "A");
        map.put('Б', "B");
        map.put('В', "V");
        map.put('Г', "G");
        map.put('Д', "D");
        map.put('Е', "E");
        map.put('Ё', "E");
        map.put('Ж', "Zh");
        map.put('З', "Z");
        map.put('И', "I");
        map.put('Й', "Y");
        map.put('К', "K");
        map.put('Л', "L");
        map.put('М', "M");
        map.put('Н', "N");
        map.put('О', "O");
        map.put('П', "P");
        map.put('Р', "R");
        map.put('С', "S");
        map.put('Т', "T");
        map.put('У', "U");
        map.put('Ф', "F");
        map.put('Х', "H");
        map.put('Ц', "Ts");
        map.put('Ч', "Ch");
        map.put('Ш', "Sh");
        map.put('Щ', "Sch");
        map.put('Ы', "Y");
        map.put('Э', "E");
        map.put('Ю', "Yu");
        map.put('Я', "Ya");
        map.put('Ъ', "");
        map.put('Ь', "");
        return map;
    }
}
