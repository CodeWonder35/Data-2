import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Hash tabloları oluştur
        HashTable<Article> articleMap = new HashTable<>();
        HashTable<WordInfo[]> indexMap = new HashTable<>();

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
}
