public class WordInfo {

    private String articleID;
    private int count;

    public WordInfo(String articleID) {
        this.articleID = articleID;
        this.count = 1; // ilk kez bulunduğunda count=1
    }

    public String getArticleID() {
        return articleID;
    }

    public int getCount() {
        return count;
    }

    public void increment() {
        this.count++;
    }
}
