import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Reader {

    private HashTable<Article> articleMap;      // Article ID → Article
    private HashTable<WordInfo[]> indexMap;     // word → WordInfo list
    private HashTable<Boolean> stopWords = new HashTable<>();

    private String csvPath = "src//CNN_Articels.csv";
    private String stopWordsPath = "src//stop_words_en.txt";
    private String delimitersPath = "src//delimiters.txt";

    private String DELIMITERS = "";

    public Reader(HashTable<Article> articleMap, HashTable<WordInfo[]> indexMap) {
        this.articleMap = articleMap;
        this.indexMap = new HashTable<>();

        loadStopWords();
        loadDelimiters();
    }

    // STOP WORDS LOAD
    private void loadStopWords() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(stopWordsPath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String w = line.trim().toLowerCase();
                if (!w.isEmpty()) {
                    stopWords.put(w, true);
                }
            }

        } catch (Exception e) {
            System.out.println("Stop words yüklenirken hata: " + e.getMessage());
        }
    }

    /*private void loadDelimiters() {
        // delimiters.txt dosyasının sadece regex içeriği
        DELIMITERS = "]\\[-+= \\r\\n1234567890’'\"(){}<>:,‒–—…!.«»-‐?‘’“”;/.␠·&@*\\\\•^¤¢$€£¥₩₪†‡°¡¿¬#№%‰‱¶′§~¨_|¦⁂☞∴‽※";
    }*/
    // DELIMITERS LOAD
    private void loadDelimiters() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(delimitersPath), StandardCharsets.UTF_8))) {

            String line;
            boolean insideString = false;
            StringBuilder currentString = new StringBuilder();

            while ((line = br.readLine()) != null) {
                // Her satırı karakter karakter işle
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);

                    if (c == '"') {
                        if (!insideString) {
                            // String başlıyor
                            insideString = true;
                            currentString = new StringBuilder();
                        } else {
                            // String bitiyor → içeriği al
                            insideString = false;
                            sb.append(currentString);
                        }
                    } else if (insideString) {
                        // Escape karakterleri işle
                        if (c == '\\' && i + 1 < line.length()) {
                            char next = line.charAt(i + 1);
                            if (next == 'n') {
                                currentString.append('\n');
                                i++; // bir karakter daha atla
                            } else if (next == 'r') {
                                currentString.append('\r');
                                i++;
                            } else if (next == '\\') {
                                currentString.append('\\');
                                i++;
                            } else if (next == '"') {
                                currentString.append('"');
                                i++;
                            } else {
                                currentString.append('\\').append(next);
                                i++;
                            }
                        } else {
                            currentString.append(c);
                        }
                    }
                    // String dışındakileri görmezden gel
                }
            }

            DELIMITERS = sb.toString();

        } catch (Exception e) {
            System.out.println("Delimiters yüklenirken hata: " + e.getMessage());
            // Fallback: sabit bir regex kullan
            DELIMITERS = " \\t\\r\\n!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~1234567890";
        }
    }

    // -------------------------------------
    // 📌 CSV YÜKLEME (StopWords kontrolünün YAPILACAĞI YER)
    // -------------------------------------
    public void loadCSV() {

        // 1) Toplam article sayısını bul
        int totalArticles = 0;
        try (BufferedReader countReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8))) {

            countReader.readLine(); // header
            while (countReader.readLine() != null) {
                totalArticles++;
            }
        } catch (Exception e) {
            System.out.println("CSV count error: " + e.getMessage());
        }

        int processedArticles = 0;

        long startTime = System.currentTimeMillis();

        // 2) CSV yükleme
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvPath), StandardCharsets.UTF_8))) {

            String line = br.readLine(); // header

            while ((line = br.readLine()) != null) {

                processedArticles++;

                // Yüzde hesapla
                int percent = (processedArticles * 100) / totalArticles;

                // Süre hesapla
                long now = System.currentTimeMillis();
                double seconds = (now - startTime) / 1000.0;

                // Aynı satıra YAZ (satır silme efekti)
                System.out.print("\rLoading CSV... " + percent + "%   (" + String.format("%.2f", seconds) + " s)");

                // --- ARTICLE İŞLEME ---
                String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 11) continue;

                Article article = new Article(
                        parts[0], parts[1], parts[2], parts[3], parts[4],
                        parts[5], parts[6], parts[7], parts[8], parts[9], parts[10]
                );

                articleMap.put(article.getId(), article);

                String combined =
                        article.getHeadline() + " " +
                                article.getDescription() + " " +
                                article.getArticleText();

                String[] words = combined.split("[\\P{L}]+");

                for (String w : words) {
                    String word = w.trim().toLowerCase();
                    if (word.isEmpty()) continue;
                    if (stopWords.get(word) != null) continue;

                    addWordToIndex(word, article.getId());
                }
            }

        } catch (Exception e) {
            System.out.println("CSV okunurken hata: " + e.getMessage());
        }

        System.out.println(); // Sonunda satır kır
    }



    // -------------------------------------
    // Kelimeyi indexMap’e ekleme (WordInfo yapısı)
    // -------------------------------------
    private void addWordToIndex(String word, String articleID) {

        // DEBUG → kelime gerçekten bölünüyor mu?
        //System.out.println("Adding word: " + word + " (Article: " + articleID + ")");

        // 1) Bu kelime zaten indekslenmiş mi?
        WordInfo[] list = indexMap.get(word);

        // 2) Eğer hiç yoksa → yeni liste oluştur
        if (list == null) {
            WordInfo[] newList = new WordInfo[1];
            newList[0] = new WordInfo(articleID);
            indexMap.put(word, newList);
            return;
        }

        // 3) Liste varsa → aynı articleID var mı?
        for (int i = 0; i < list.length; i++) {
            if (list[i].getArticleID().equals(articleID)) {
                list[i].increment();    // aynı article içinde tekrar geçti → count++
                indexMap.put(word, list);
                return;
            }
        }

        // 4) articleID listede yoksa → listeyi genişlet
        WordInfo[] newList = new WordInfo[list.length + 1];

        // eski elemanları taşı
        for (int i = 0; i < list.length; i++) {
            newList[i] = list[i];
        }

        // sona yeni articleID’yi ekle
        newList[list.length] = new WordInfo(articleID);

        // güncel listeyi tabloya koy
        indexMap.put(word, newList);
    }


    public void searchText(String word) {

        word = word.toLowerCase().trim();

        // 1) stopword kontrolü
        if (stopWords.get(word) != null) {
            System.out.println("Bu kelime stopword olduğu için aranamıyor.");
            return;
        }

        // 2) indexMap'te kelime var mı?
        WordInfo[] list = indexMap.get(word);

        if (list == null) {
            System.out.println("Bu kelime hiçbir makalede bulunmuyor.");
            return;
        }

        // 3) relevance hesaplaması için Article + skor tablosu hazırlayalım
        // MakaleID → skor
        HashTable<Integer> relevanceTable = new HashTable<>();

        for (WordInfo wi : list) {

            Integer currentScore = relevanceTable.get(wi.getArticleID());

            if (currentScore == null) {
                // ilk kez ekleniyor
                relevanceTable.put(wi.getArticleID(), wi.getCount());
            } else {
                // toplam üzerine ekliyoruz
                relevanceTable.put(wi.getArticleID(), currentScore + wi.getCount());
            }
        }

        // 4) relevance değerlerini bir listeye koy – sonra en büyükleri bulacağız
        // Kaç makalede geçtiğini bilmiyoruz → dynamic array
        String[] articleIDs = new String[list.length];
        int[] scores = new int[list.length];

        for (int i = 0; i < list.length; i++) {
            articleIDs[i] = list[i].getArticleID();
            scores[i] = relevanceTable.get(list[i].getArticleID());
        }

        // 5) En yüksek 5 skoru bul
        for (int i = 0; i < scores.length - 1; i++) {
            for (int j = i + 1; j < scores.length; j++) {
                if (scores[j] > scores[i]) {

                    // skor swap
                    int tempScore = scores[i];
                    scores[i] = scores[j];
                    scores[j] = tempScore;

                    // id swap
                    String tempID = articleIDs[i];
                    articleIDs[i] = articleIDs[j];
                    articleIDs[j] = tempID;
                }
            }
        }

        // 6) İlk 5 makaleyi yazdır
        System.out.println("\n--- TOP 5 RESULTS FOR WORD: " + word + " ---\n");

        int limit = Math.min(5, articleIDs.length);

        for (int i = 0; i < limit; i++) {

            Article a = articleMap.get(articleIDs[i]);

            if (a != null) {
                System.out.println((i + 1) + ". Result (Score = " + scores[i] + ")");
                System.out.println("----------------------------------------");
                System.out.println("ID: " + a.getId());
                System.out.println("Headline: " + a.getHeadline());
                System.out.println("Description: " + a.getDescription());
                System.out.println("Author: " + a.getAuthor());
                System.out.println("URL: " + a.getUrl());
                System.out.println();
            }
        }
    }


}

