package edu.depaul.ntessema.csc575;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebCrawler {

    /*
     * The number of available BBC programs is to great to deal with
     * in this course project. So, I had to limit the programs to only
     * a subset of the entire program list.
     *
     * The base URLs were themselves obtained by crawling the a to z program list.
     */
    private static String[][] selectedPrograms = new String [][] {
        { "The-Why-Factor", "https://www.bbc.co.uk/programmes/p00xtky9/episodes/player" }
    };

    public static void main(String[] args) throws IOException {

        List<String> listOfPrograms = getSelectedProgramsList();
        List<String> listOfEpisodes = getEpisodes(listOfPrograms);
    }

    private static List<String> getEpisodes(List<String> listOfPrograms) throws IOException {
        /*
         * List that holds all episodes of all programs.
         */
        List<String> listOfEpisodes = new ArrayList<>();

        final String cssClass = "div.programme__body h2.programme__titles a";

        int totalNumberOfEpisodesFound = 0;
        /*
         * Iterate over the list of programs found.
         * Each program page could have multiple pages.
         */
        for(String programRootUrl : listOfPrograms) {

            System.out.println(String.format("%31s", "TRYING TO CONNECT TO: ") + programRootUrl);
            /*
             * Connect to the page at p.
             */
            Connection connection = Jsoup.connect(programRootUrl);
            /*
             * Disable exception throwing. Many things could go
             * wrong while crawling, but we don't want that to
             * stop execution of the crawler.
             */
            connection.ignoreHttpErrors(true);
            System.out.println(String.format("%31s", "CONNECTED TO: ") + programRootUrl);
            if(connection != null) {
                /*
                 * Proceed only if http status is OK
                 */
                int statusCode = connection.execute().statusCode();
                if (statusCode == 200) {
                    /*
                     * Get the DOM of the page at p
                     */
                    Document d = Jsoup.connect(programRootUrl).get();
                    if (d != null) {
                        /*
                         * Find how many pages there are for this program
                         */
                        int totalNumberOfPages = 0;
                        /*
                         * Extract the pagination elements (<li>)
                         */
                        List<Element> paginationElements = d.select("div.programmes-page ol.pagination li.pagination__page");
                        /*
                         * If there are other pages than the current one
                         * there will be pagination and, therefore, pagination elements.
                         */
                        if(paginationElements.size() > 0) {
                            /*
                             * Get the last pagination element
                             */
                            Element lastElement = paginationElements.get(paginationElements.size() - 1).getAllElements().last();
                            /*
                             * The total number of pages is the number in the last
                             * pagination element.
                             */
                            totalNumberOfPages = Integer.parseInt(lastElement.getElementsByTag("a").html());
                            System.out.print(String.format("%31s", "PAGES: ") + totalNumberOfPages);
                        }
                        /*
                         * Ready to visit every page...
                         */
                        int currentPage = 1;
                        /*
                         * The first page is at the programRootUrl
                         * (https://www.bbc.co.uk/programmes/{programId}/episodes/player)
                         */
                        do {
                            System.out.println(String.format("%31s", "NOW IN PAGE: ") + currentPage);
                            /*
                             * all epiosodes in currentPage only
                             */
                            List<Element> episodes = d.select(cssClass);
                            if (episodes != null) {
                                /*
                                 * Iterate over the episodes in the currentPage
                                 */
                                episodes.forEach(e -> {
                                    /*
                                     * Add the link to the discovered episodes to the
                                     * list that contains all episodes from all programs.
                                     */
                                    listOfEpisodes.add(e.attr("href"));
                                });
                                System.out.println(String.format("%31s", "NUMBER OF EPISODES FOUND:") + episodes.size());
                                totalNumberOfEpisodesFound += episodes.size();
                            }
                            /*
                             * The next page is programRootUrl?page={pageCount}
                             * where programRootUrl = https://www.bbc.co.uk/programmes/{programId}/episodes/player
                             */
                            d = Jsoup.connect(programRootUrl + "?page=" + currentPage++).get();
                        } while(currentPage <= totalNumberOfPages);
                    }
                }
            }
        }
        listOfEpisodes.forEach(System.out::println);
        System.out.println(String.format("%31s", "ACCUMULATED NUMBER OF EPISODES:") + totalNumberOfEpisodesFound);
        System.out.println(String.format("%31s", "SIZE OF EPISODES' LIST ") + listOfEpisodes.size());
        return listOfEpisodes;
    }

    /*
     * The entire collection of BBC programs is too large to
     * deal with in a mini-course project. So, I will not attempt
     * to crawl the entire collection. However, if the program is
     * to be scaled up to crawl the entire collection, this
     * method (which, for now, crawls only the first pages of the
     * a to z list) can be used.
     */
    private static List<String> findBBCPrograms() throws IOException {
        final String urlPrefix = "https://www.bbc.co.uk/programmes/a-z/by/";
        final String urlSuffix = "/player";
        final String cssClass = "div.programme__body h2.programme__titles a";
        /*
         * List that holds all BBC Radio programs.
         */
        List<String> listOfPrograms = new ArrayList<>();
        String url;

        /*
         * Only pages a-z are selected for crawling.
         */
        for(int i = 0x61; i <= 0x7a; i++) {
            char ch = (char) (i);
            url = urlPrefix + ch + urlSuffix;
            Document document = Jsoup.connect(url).get();
            List<Element> programmes =  document.select(cssClass);
            programmes.forEach((element) -> {
                String programLink = element.attr("href") + "/episodes/guide";
                /*
                 * The cssClass is the same for episode links
                 * add N episodes for each program
                 */
                listOfPrograms.add(programLink);
            });
        }
        listOfPrograms.forEach(System.out::println);
        return listOfPrograms;
    }

    /*
     *
     */
    private static List<String> getSelectedProgramsList() {
        List<String> listOfPrograms = new ArrayList();
        for (String[] program : selectedPrograms) {
            listOfPrograms.add(program[1]);
        }
        return listOfPrograms;
    }
}
