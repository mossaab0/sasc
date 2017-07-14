/**
 * Tools Lang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.umiacs.clip.sis.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.function.Function.identity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import java.util.stream.Stream;

/**
 *
 * @author Mossaab Bagdouri
 */
public class LangUtils {

    private static final Pattern P = Pattern.compile("([\\p{L}\\p{Nd}]+)|([^\\p{L}\\p{Nd}]+)");

    public static String ngram(String input, int n, String sep, boolean includeBoudnaries) {
        if (includeBoudnaries) {
            for (int i = 0; i < n - 1; i++) {
                input = "START " + input + " END";
            }
        }
        String[] words = input.split(" ");
        if (words.length < n) {
            return "";
        }
        StringBuilder ngram = new StringBuilder();
        for (int i = 0; i < words.length - n + 1; i++) {
            for (int j = i; j < i + n - 1; j++) {
                ngram.append(words[j]).append(sep);
            }
            ngram.append(words[i + n - 1]);
            if (i < words.length - n) {
                ngram.append(" ");
            }
        }
        return ngram.toString().trim();
    }

    public static String ngrams(String input, int nStartIncluded, int nEndIncluded, String sep, boolean includeBoudnaries) {
        StringBuilder sb = new StringBuilder();
        for (int n = nStartIncluded; n < nEndIncluded; n++) {
            sb.append(ngram(input, n, sep, includeBoudnaries)).append(" ");
        }
        sb.append(ngram(input, nEndIncluded, sep, includeBoudnaries));
        return sb.toString().trim();
    }

    public static List<String> charNgram(String input, int n, boolean includeBoudnaries) {
        if (includeBoudnaries) {
            for (int i = 0; i < n - 1; i++) {
                input = "S" + input + "E";
            }
        }
        String modified = input;
        return range(0, modified.length() - n + 1).boxed().
                map(i -> modified.substring(i, i + n)).collect(toList());
    }

    public static List<String> charNgrams(String input, int nStartIncluded, int nEndIncluded, boolean includeBoudnaries) {
        return range(nStartIncluded, nEndIncluded + 1).boxed().
                flatMap(n -> charNgram(input, n, includeBoudnaries).stream()).
                collect(toList());
    }

    public static Map<String, Integer> toFreqMap(String input) {
        input = input.trim();
        if (input.isEmpty()) {
            return new HashMap<>();
        }
        return Stream.of(input.split("\\s+")).
                collect(groupingBy(identity(), reducing(0, e -> 1, Integer::sum)));
    }

    public static String tokenizeKeepPunctuation(String input) {
        Matcher m = P.matcher(input.replaceAll("\\s+", " ").trim());
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String token = m.group().trim();
            if (!token.isEmpty()) {
                sb.append(token).append(" ");
            }
        }
        return sb.toString().trim();
    }

    //From http://stackoverflow.com/questions/1670862
    private static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<>());
            return sets;
        }
        List<T> list = new ArrayList<>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<>(list.subList(1, list.size()));
        powerSet(rest).stream().forEach(set -> {
            Set<T> newSet = new HashSet<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        });
        return sets;
    }

    public static String powerSet(String input, String sep) {
        return String.join(" ", powerSet(Stream.of(input.trim().split("\\s+")).
                filter(word -> !word.isEmpty()).collect(toSet())).
                parallelStream().
                filter(set -> !set.isEmpty()).
                map(set -> String.join(sep, set.stream().collect(toList()))).
                collect(toList()));
    }

    public static void main(String[] args) {
        String input = "How are you?\n?today?? :-)";
        //System.out.println(ngrams(input, 1, 3, "_", false));
        //System.out.println(toFreqMap(""));
        //System.out.println(tokenizeKeepPunctuation(input));
        System.out.println(charNgrams("abc", 2, 3, true));
    }
}
