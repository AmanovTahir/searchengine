package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.services.lemmatisator.LemmaFinderService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SnippetServiceImpl implements SnippetService {
    private final LemmaFinderService lemmaFinderService;

    public String getSnippet(String html, String searchQuery) {
        String text = cleanText(html);
        List<String> searchWords = getSearchWords(text, searchQuery);
        List<String> strings = Arrays.stream(text.split("[.]")).toList();
        return substring(searchWords, strings);
    }

    @NotNull
    private String substring(List<String> searchWords, List<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String searchWord : searchWords) {
            for (String string : strings) {
                if (string.toLowerCase().contains(searchWord)) {
                    String splitText = getSplitText(searchWord, string.toLowerCase())
                            .replaceAll(searchWord + ".*?(\\s)", "<b>" + searchWord + "</b> ");
                    stringBuilder.append(splitText);
                    break;
                }
            }
        }
        return stringBuilder.toString();
    }

    @NotNull
    private List<String> getSearchWords(String cleanText, String searchQuery) {
        Map<String, String> stringStringMap = lemmaFinderService.collectLemmasAndQueryWord(cleanText);
        Map<String, String> searchLemmas = lemmaFinderService.collectLemmasAndQueryWord(searchQuery);
        return searchLemmas
                .keySet().stream()
                .filter(stringStringMap::containsKey)
                .map(stringStringMap::get)
                .toList();
    }

    @NotNull
    private String cleanText(String html) {
        return Jsoup.parse(html).body().text();
    }

    private String getSplitText(String word, String cleanText) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = getMatcher(word, cleanText);
        if (matcher.find()) {
            builder.append(matcher.group(0).trim()).append(" ... ");
        }
        return builder.toString();
    }

    @NotNull
    private Matcher getMatcher(String word, String cleanText) {
        String regex = "(" + word + ".*?\\s?(\\s*.*?\\s){0,5})|(.*?\\s+){0,5}+(" + word + ".*?)\\s?(\\s*.*?\\s){0,5}";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        return pattern.matcher(cleanText);
    }
}
