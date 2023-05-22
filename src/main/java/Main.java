import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


public class Main {
    public static void main(String[] args) {
        String rootPage = "http://skillbox.ru/";
        FileWriter file;
        HtmlMap.setRootPath(rootPage);
        ForkJoinPool pool = new ForkJoinPool();
        HtmlMap page = new HtmlMap(rootPage, 0);
        pool.execute(page);
        do {
            System.out.print("******************************************\n");
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            System.out.printf("Main: Active Threads: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Task Count: %d\n", pool.getQueuedTaskCount());
            System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
            System.out.print("******************************************\n");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!page.isDone());
        pool.shutdown();

        List<HtmlLink> result;
        result = page.join();
        System.out.printf("found %d object \n", result.size());
        System.out.println("writing to file");
        try {
            file = new FileWriter("out/map.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.forEach(element -> {
            try {
                file.write(element.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("all done");
    }
}
