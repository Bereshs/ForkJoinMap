import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class HtmlMap extends RecursiveTask<List<HtmlLink>> {
    static String rootPath;
    int currentDeep;
    String linkPath;
    static public List<HtmlLink> htmlMapList = new ArrayList<>();


    public HtmlMap(String linkPath, int currentDeep) {
        this.currentDeep = currentDeep;
        this.linkPath = linkPath;
    }

    public static void setRootPath(String rootPath) {
        HtmlMap.rootPath = rootPath;
        htmlMapList.add(new HtmlLink(rootPath, 0));
    }

    public static String getRootPath() {
        return HtmlMap.rootPath;
    }

    public int getCurrentDeep() {
        return currentDeep;
    }

    @Override
    protected List<HtmlLink> compute() {
        List<HtmlLink> localHtmlMapList = new ArrayList<>();
        List<HtmlMap> taskList = new ArrayList<>();
        HtmlLink.setRootPath(getRootPath());
        HtmlLink htmlLink = new HtmlLink(linkPath, getCurrentDeep());
        htmlLink.setAbsolutePath();
        if (!htmlLink.isValid()) {
            return localHtmlMapList;
        }
        localHtmlMapList.add(htmlLink);
        List<HtmlLink> currentSublinksList = htmlLink.getSubLinks();
        synchronized (HtmlLink.getAllViewedLinks()) {
            HtmlLink.getAllViewedLinks().add(htmlLink.getLinkPath());
        }
        for (HtmlLink link : currentSublinksList) {
            link.setAbsolutePath();
            if (!link.isDocumentLink() && link.isValid() && !HtmlLink.alreadyContains(link)) {
                synchronized (HtmlLink.getAllViewedLinks()) {
                    HtmlLink.getAllViewedLinks().add(link.getLinkPath());
                }
                HtmlMap task = new HtmlMap(link.getLinkPath(), link.getDeep() + 1);
                task.fork();
                taskList.add(task);
//                link.getSubLinks();
            }
        }
        addResultFromTasks(taskList, localHtmlMapList);
        return localHtmlMapList;
    }

    private void addResultFromTasks(List<HtmlMap> taskList, List<HtmlLink> localHtmlMapList) {
        for (HtmlMap task : taskList) {
            localHtmlMapList.addAll(task.join());
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        htmlMapList.forEach(link -> stringBuilder.append(link).append(System.lineSeparator()));
        return stringBuilder.toString();
    }
}
