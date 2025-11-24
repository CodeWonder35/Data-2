import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Reader {

    private HashTable<Article> articleMap;      // Article ID → Article
    HashTable<HashTable<Integer>> indexMap;
    private HashTable<Boolean> stopWords = new HashTable<>();

    private String csvPath = "src//CNN_Articels.csv";
    private String stopWordsPath = "src//stop_words_en.txt";
    private String delimitersPath = "src//delimiters.txt";

    private String DELIMITERS = "";

    public Reader(HashTable<Article> articleMap, HashTable<HashTable<Integer>> indexMap) {
        this.articleMap = articleMap;
        this.indexMap = indexMap;
        //this.indexMap = new HashTable<>();

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

                // burası general motor sadece searchtxt deki kelimeleri aratmamız lazım
                // ama bizden istenen general performans matrisinde searchtxt kelimelerinin süreleri yazacak
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
    private void addWordToIndex(String word, String articleID) {

        HashTable<Integer> inner = indexMap.get(word);

        if (inner == null) {
            inner = new HashTable<>(251);
            indexMap.put(word, inner);
        }

        Integer count = inner.get(articleID);

        if (count == null) {
            inner.put(articleID, 1);
        } else {
            inner.put(articleID, count + 1);
        }
    }


    public void searchText(String word) {

        word = word.toLowerCase().trim();

        if (stopWords.get(word) != null) {
            System.out.println("Bu kelime stopword olduğu için aranamıyor.");
            return;
        }

        HashTable<Integer> inner = indexMap.get(word);

        if (inner == null) {
            System.out.println("Bu kelime hiçbir makalede bulunmuyor.");
            return;
        }

        String[] ids = inner.getAllKeys();
        int[] scores = new int[ids.length];

        for (int i = 0; i < ids.length; i++) {
            scores[i] = inner.get(ids[i]);
        }

        // Sort (descending)
        for (int i = 0; i < scores.length - 1; i++) {
            for (int j = i + 1; j < scores.length; j++) {
                if (scores[j] > scores[i]) {
                    int t = scores[i]; scores[i] = scores[j]; scores[j] = t;
                    String ts = ids[i]; ids[i] = ids[j]; ids[j] = ts;
                }
            }
        }

        System.out.println("\n--- TOP 5 RESULTS FOR WORD: " + word + " ---\n");

        int limit = Math.min(5, ids.length);

        for (int i = 0; i < limit; i++) {

            Article a = articleMap.get(ids[i]);

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
    private void searchTextSilent(String word) {
        if (stopWords.get(word) != null) return;

        HashTable<Integer> inner = indexMap.get(word);
        if (inner == null) return;

        String[] ids = inner.getAllKeys();
        for (String id : ids) {
            inner.get(id); // sadece erişim testi
        }
    }


    public double benchmarkSearchAverage(String searchFilePath) {
        long totalTime = 0;
        int count = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(searchFilePath)))) {
            String word;
            while ((word = br.readLine()) != null) {

                word = word.trim().toLowerCase();
                if (word.isEmpty()) continue;

                long start = System.nanoTime();
                searchTextSilent(word);     // normal searchText ama ekrana yazdırmıyor
                long end = System.nanoTime();

                totalTime += (end - start);
                count++;
            }
        }
        catch (Exception e) {
            System.out.println("Benchmark search error: " + e.getMessage());
        }

        if (count == 0) return 0;
        return totalTime / (double) count;   // ns
    }

    public long getTotalInnerCollisions() {
        long sum = 0;

        String[] keys = indexMap.getAllKeys();
        if (keys == null) return 0;

        for (String w : keys) {
            HashTable<Integer> inner = indexMap.get(w);
            if (inner != null) {
                sum += inner.getCollisionCount();
            }
        }
        return sum;
    }



}

