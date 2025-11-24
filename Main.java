import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        benchmark();
        // if (true) return;   // normal menüye geçmesini istemiyorsan


        Scanner scanner = new Scanner(System.in);

        // Hash tabloları oluştur
        HashTable<Article> articleMap = new HashTable<>();
        HashTable<HashTable<Integer>> indexMap = new HashTable<>();


        // Reader oluştur
        Reader reader = new Reader(articleMap, indexMap);


        System.out.println("Loading CSV...");

        // ⭐ Süre ölçme başlıyor
        long start = System.currentTimeMillis();

        reader.loadCSV();

        long end = System.currentTimeMillis();
        // ⭐ Süre ölçme bitiyor

        System.out.println("CSV Loaded Successfully!");

        System.out.println("Load Time: " + (end - start) + " ms ("
                + ((end - start)/1000.0) + " seconds)\n");



        while (true) {
            System.out.println("===== MENU =====");
            System.out.println("1) Search Article by ID");
            System.out.println("2) Search Articles by Word");
            System.out.println("3) Exit");
            System.out.print("Choose: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid input!");
                continue;
            }

            if (choice == 1) {

                System.out.print("Enter Article ID: ");
                String id = scanner.nextLine().trim();

                Article a = articleMap.get(id);

                if (a == null) {
                    System.out.println("Article not found.\n");
                } else {
                    System.out.println();
                    a.printArticle();
                }

            } else if (choice == 2) {

                System.out.print("Enter word to search: ");
                String word = scanner.nextLine().trim().toLowerCase();

                reader.searchText(word);
                System.out.println();

            } else if (choice == 3) {

                System.out.println("Goodbye!");
                break;

            } else {
                System.out.println("Invalid choice.\n");
            }
        }

        scanner.close();
    }

    private static void printHeader() {
        System.out.println("Load Factor | Hash Function | Collision Handling | Collision Count | Indexing Time(s) | Avg Search Time(ns)");
        System.out.println("----------- | ------------- | ------------------ | --------------- | ---------------- | ------------------");
    }

    private static void printRow(double load, String hashFunc, String coll, long collisions, double indexTime, double avgSearch) {
        System.out.printf("%-11s | %-13s | %-18s | %-15d | %-16.2f | %-18.0f\n",
                load, hashFunc, coll, collisions, indexTime, avgSearch);
    }

    private static void benchmark() {

        double[] loadFactors = {0.5, 0.8};
        String[] hashFuncs = {"PAF","SSF"};
        String[] collisions = {"LP", "DH"};

        System.out.println("Load Factor | Hash Function | Collision Handling | Collision Count | Indexing Time(s) | Avg Searching Time(ns)");
        System.out.println("----------- | ------------- | ------------------ | --------------- | ---------------- | ----------------------");

        for (double lf : loadFactors) {
            for (String hf : hashFuncs) {
                for (String col : collisions) {

                    // ✅ LOAD FACTOR'Ü KULLAN!
                    int initialSize = (int)(10000 / lf);  // Örnek hesaplama

                    HashTable<Article> articleMap = new HashTable<>(lf, initialSize);
                    HashTable<HashTable<Integer>> indexMap = new HashTable<>(lf, initialSize);

                    Reader r = new Reader(articleMap, indexMap);

                    // Hash fonksiyonu seçimi
                    articleMap.setUsePAF(hf.equals("PAF"));
                    indexMap.setUsePAF(hf.equals("PAF"));

                    // Collision handling
                    boolean useDH = col.equals("DH");
                    articleMap.setDoubleHashing(useDH);
                    indexMap.setDoubleHashing(useDH);

                    // Çarpışma sayaçlarını sıfırla
                    articleMap.resetCollisionCount();
                    indexMap.resetCollisionCount();

                    // Indexing
                    long start = System.currentTimeMillis();
                    r.loadCSV();
                    long end = System.currentTimeMillis();

                    double indexTime = (end - start) / 1000.0;

                    // Search benchmark
                    double avgSearch = r.benchmarkSearchAverage("src//search.txt");

                    // Total collisions
                    long totalCollisions =
                            articleMap.getCollisionCount()
                                    + indexMap.getCollisionCount()
                                    + r.getTotalInnerCollisions();

                    System.out.printf("%-11s | %-13s | %-18s | %-15d | %-16.2f | %-22.0f\n",
                            lf, hf, col, totalCollisions, indexTime, avgSearch);
                }
            }
        }
    }



}
