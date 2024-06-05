package labelsApp.api.cloudTranslation;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslateWord {
    /**
     * Function that will detect the language of the labels and translate them to a specific language
     *
     * @param word the string to be translated
     * @param targetLanguage the language to translate the label, e.g. "pt" for Portuguese, see
     * <a href="https://cloud.google.com/translate/docs/languages">supported languages</a>
     */
    public String detectLangAndTranslateTo(String word, String targetLanguage) {
        String translatedWord = "";
        try {
            Translate translate = TranslateOptions.getDefaultInstance().getService();

            String detectedLanguage = translate.detect(word).getLanguage();

            Translation translation = translate.translate(word, TranslateOption.sourceLanguage(detectedLanguage), TranslateOption.targetLanguage(targetLanguage));

            translatedWord = translation.getTranslatedText();
        } catch (Exception e) {
            System.out.println("Error translating labels: " + e.getMessage());
        }
        return translatedWord;
    }
}
