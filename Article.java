public class Article {
    private  String id;
    private  String author;
    private  String publicationDate;
    private  String category;
    private  String section;
    private  String url;
    private  String headline;
    private  String description;
    private  String keywords;
    private  String secondHeadline;
    private  String articleText;

    public Article(String id, String author, String publicationDate, String category, String section,
                   String url, String headline, String description, String keywords, String secondHeadline,
                   String articleText){
        this.id = id;
        this.author = author;
        this.publicationDate = publicationDate;
        this.category = category;
        this.section = section;
        this.url = url;
        this.headline = headline;
        this.description = description;
        this.keywords = keywords;
        this.secondHeadline = secondHeadline;
        this.articleText = articleText;

    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public String getCategory() {
        return category;
    }

    public String getSection() {
        return section;
    }

    public String getUrl() {
        return url;
    }

    public String getHeadline() {
        return headline;
    }

    public String getDescription() {
        return description;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getSecondHeadline() {
        return secondHeadline;
    }

    public String getArticleText() {
        return articleText;
    }

    public void printArticle() {
        System.out.println("ID: " + id);
        System.out.println("Author: " + author);
        System.out.println("Date: " + publicationDate);
        System.out.println("Category: " + category);
        System.out.println("Section: " + section);
        System.out.println("URL: " + url);
        System.out.println("Headline: " + headline);
        System.out.println("Second Headline: " + secondHeadline);
        System.out.println("Description: " + description);
        System.out.println("Keywords: " + keywords);
        System.out.println("Content: " + articleText);
    }

}