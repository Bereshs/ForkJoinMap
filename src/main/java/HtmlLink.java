import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlLink {
    private final int deep;
    private String linkPath;
    private static String rootPath;
    private static volatile List<String> allViewedLinks = new ArrayList<>();
    private List<HtmlLink> sublinks;

    public HtmlLink(String linkPath, int deep) {
        this.deep = deep;
        this.linkPath = linkPath;
        sublinks = new ArrayList<>();
    }

    static boolean alreadyContains(HtmlLink link) {
        return allViewedLinks.contains(link.getLinkPath());
    }

    static List<String> getAllViewedLinks() {
        return allViewedLinks;
    }

    static void setRootPath(String rootPath) {
        HtmlLink.rootPath = rootPath;
    }

    @Override
    public boolean equals(Object objLink) {
        if (this == objLink) {
            return true;
        }
        if (!(objLink instanceof HtmlLink)) {
            return false;
        }
        HtmlLink oLink = (HtmlLink) objLink;
        return linkPath.equals(oLink.getLinkPath());
    }

    public String getLinkPath() {
        return linkPath;
    }

    public int getDeep() {
        return deep;
    }

    public boolean isHaveRootPath() {
        return Pattern.compile("^" + getRootPath()).matcher(linkPath).find();
    }


    public boolean isValid() {
        return linkPath.startsWith("http") && !linkPath.matches("(.+;$)|(.+:$)|(.+#$)");
    }

    public void setAbsolutePath() {
        if (isHaveRootPath() || linkPath.startsWith("tel") || linkPath.startsWith("tg") || linkPath.startsWith("mailto")) {
            return;
        }
        if (isValid()) {
            return;
        }
        linkPath = getRootPath() + linkPath;
        linkPath = linkPath.replaceAll("//", "/").replace(":/", "://");
    }

    public boolean isDocumentLink() {
        return linkPath.matches(".+[pdf]$");
    }


    public String getRootPath() {
        return HtmlLink.rootPath;
    }

    public List<HtmlLink> getSubLinks() {
        int subLinksDepth = deep + 1;
        Elements elements = new Elements();
        try {
            Document doc = Jsoup.connect(getLinkPath()).get();
            elements = doc.select("a[href]");
        } catch (HttpStatusException h) {
            System.out.println(h.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (org.jsoup.nodes.Element element : elements) {
            String hrefElement = element.attr("href");
            HtmlLink link = new HtmlLink(hrefElement, subLinksDepth);
            link.setAbsolutePath();
            if (!link.isHaveRootPath() || !link.isValid()) {
                continue;
            }
            sublinks.add(link);
        }
        return sublinks;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\t".repeat(getDeep())).append(getLinkPath()).append(System.lineSeparator());
        for (HtmlLink link : sublinks) {
            stringBuilder.append(link);
        }
        //     stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

}
