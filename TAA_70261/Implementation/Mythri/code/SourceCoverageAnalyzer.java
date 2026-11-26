import java.io.*;
import java.util.*;
public class SourceCoverageAnalyzer {
    private static final String FILE_NAME = "APTnotes.csv";
    private static final Scanner input = new Scanner(System.in);
    private static final List<Report> reports = new ArrayList<>();
    private static final Map<String, List<Report>> sourceIndex =
            new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static class Report {
        String filename, title, source, link, date, year;
        Report(String filename, String title, String source,
               String link, String date, String year) {
            this.filename = filename;
            this.title = title;
            this.source = source;
            this.link = link;
            this.date = date;
            this.year = year;
        }
        void display() {
            System.out.println("Title    : " + title);
            System.out.println("Source   : " + source);
            System.out.println("Date     : " + date);
            System.out.println("Year     : " + year);
            System.out.println("Filename : " + filename);
            System.out.println("Link     : " + link);
        }
    }
    static class SourceStat {
        String name;
        int count;
        SourceStat(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }
    public static void main(String[] args) {
        printHeading();
        if (!loadDataset()) {
            System.out.println("Unable to start without valid dataset records.");
            input.close();
            return;
        }
        buildSourceIndex();
        runMenu();
        input.close();
    }
    private static void printHeading() {
        System.out.println("==============================================");
        System.out.println("     INTELLIGENCE SOURCE COVERAGE ANALYZER");
        System.out.println("==============================================");
        System.out.println("Dataset: APTnotes campaign report catalogue");
    }
    private static boolean loadDataset() {
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(FILE_NAME))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("The CSV file is empty.");
                return false;
            }
            List<String> headers = parseCsvLine(headerLine);
            int fileCol = findColumn(headers, "Filename");
            int titleCol = findColumn(headers, "Title");
            int sourceCol = findColumn(headers, "Source");
            int linkCol = findColumn(headers, "Link");
            int dateCol = findColumn(headers, "Date");
            int yearCol = findColumn(headers, "Year");
            if (titleCol < 0 || sourceCol < 0) {
                System.out.println("Required Title or Source column is missing.");
                return false;
            }
            String line;
            int skipped = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> fields = parseCsvLine(line);
                String title = getField(fields, titleCol);
                String source = getField(fields, sourceCol);
                if (title.isEmpty() || source.isEmpty()) {
                    skipped++;
                    continue;
                }
                reports.add(new Report(
                        getField(fields, fileCol),
                        title,
                        source,
                        getField(fields, linkCol),
                        getField(fields, dateCol),
                        getField(fields, yearCol)
                ));
            }
            System.out.println("\nDataset loaded successfully.");
            System.out.println("Valid records: " + reports.size());
            System.out.println("Rows skipped : " + skipped);
            return !reports.isEmpty();
        } catch (IOException error) {
            System.out.println("Could not read " + FILE_NAME);
            System.out.println("Keep it in the same folder as this Java file.");
            System.out.println("Details: " + error.getMessage());
            return false;
        }
    }
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                fields.add(value.toString().trim());
                value.setLength(0);
            } else {
                value.append(ch);
            }
        }
        fields.add(value.toString().trim());
        return fields;
    }
    private static int findColumn(List<String> headers, String wanted) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).trim().equalsIgnoreCase(wanted)) {
                return i;
            }
        }
        return -1;
    }
    private static String getField(List<String> fields, int index) {
        if (index < 0 || index >= fields.size()) {
            return "";
        }
        return fields.get(index).trim();
    }
    private static void buildSourceIndex() {
        for (Report report : reports) {
            String key = normalize(report.source);
            sourceIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(report);
        }
    }
    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }
    private static void runMenu() {
        boolean running = true;
        while (running) {
            showMenu();
            int choice = readInteger("Choose an option: ");
            switch (choice) {
                case 1: showAllSourceCounts(); break;
                case 2: showTopSources(); break;
                case 3: showSourceDetails(); break;
                case 4: searchReportsBySource(); break;
                case 5: compareSources(); break;
                case 6: showSourceYearCoverage(); break;
                case 7: showSourcesForYear(); break;
                case 8: showCoverageSummary(); break;
                case 9: exportSummary(); break;
                case 10:
                    running = false;
                    System.out.println("Thank you for using the analyzer.");
                    break;
                default:
                    System.out.println("Invalid choice. Enter 1 to 10.");
            }
            if (running) {
                System.out.print("\nPress Enter to continue...");
                input.nextLine();
            }
        }
    }
    private static void showMenu() {
        System.out.println("\n============== SOURCE ANALYSIS MENU ==============");
        System.out.println("1. Display report count for every source");
        System.out.println("2. Display top five sources");
        System.out.println("3. View details of one source");
        System.out.println("4. Search reports by source keyword");
        System.out.println("5. Compare two sources");
        System.out.println("6. Show year coverage for a source");
        System.out.println("7. Find sources listed in a selected year");
        System.out.println("8. Display dataset coverage summary");
        System.out.println("9. Export source summary to a text file");
        System.out.println("10. Exit");
        System.out.println("===================================================");
    }
    private static List<SourceStat> getStatistics() {
        List<SourceStat> stats = new ArrayList<>();
        for (Map.Entry<String, List<Report>> entry : sourceIndex.entrySet()) {
            stats.add(new SourceStat(entry.getKey(), entry.getValue().size()));
        }
        return stats;
    }
    private static void showAllSourceCounts() {
        System.out.println("\n------ REPORT COUNT BY SOURCE ------");
        List<SourceStat> stats = getStatistics();
        stats.sort(Comparator.comparing(s -> s.name,
                String.CASE_INSENSITIVE_ORDER));
        for (SourceStat stat : stats) {
            System.out.printf("%-42s %5d reports%n",
                    shorten(stat.name, 42), stat.count);
        }
        System.out.println("Distinct source names: " + stats.size());
    }
    private static void showTopSources() {
        System.out.println("\n------ TOP FIVE SOURCES ------");
        List<SourceStat> stats = getStatistics();
        stats.sort((a, b) -> Integer.compare(b.count, a.count));
        int limit = Math.min(5, stats.size());
        for (int i = 0; i < limit; i++) {
            SourceStat stat = stats.get(i);
            System.out.println((i + 1) + ". " + stat.name
                    + " - " + stat.count + " reports");
        }
        System.out.println("Ranking is based on report frequency only.");
    }
    private static void showSourceDetails() {
        System.out.println("\n------ SOURCE DETAILS ------");
        String requested = readText("Enter exact source name: ");
        String key = findSourceKey(requested);
        if (key == null) {
            System.out.println("Source not found.");
            return;
        }
        List<Report> matching = sourceIndex.get(key);
        System.out.println("Source: " + key);
        System.out.println("Number of reports: " + matching.size());
        int number = 1;
        for (Report report : matching) {
            System.out.println("\nReport " + number++);
            report.display();
        }
    }
    private static void searchReportsBySource() {
        System.out.println("\n------ SEARCH BY SOURCE ------");
        String keyword = readText("Enter source keyword: ").toLowerCase();
        int found = 0;
        for (Report report : reports) {
            if (report.source.toLowerCase().contains(keyword)) {
                System.out.println("\nMatch " + (++found));
                report.display();
            }
        }
        System.out.println("Total matching records: " + found);
    }
    private static void compareSources() {
        System.out.println("\n------ COMPARE TWO SOURCES ------");
        String first = findSourceKey(readText("First source name: "));
        String second = findSourceKey(readText("Second source name: "));
        if (first == null || second == null) {
            System.out.println("One or both sources were not found.");
            return;
        }
        int firstCount = sourceIndex.get(first).size();
        int secondCount = sourceIndex.get(second).size();
        System.out.println(first + ": " + firstCount + " reports");
        System.out.println(second + ": " + secondCount + " reports");
        if (firstCount > secondCount) {
            System.out.println(first + " has more listed reports.");
        } else if (secondCount > firstCount) {
            System.out.println(second + " has more listed reports.");
        } else {
            System.out.println("Both sources have equal report counts.");
        }
        System.out.println("Frequency does not measure accuracy or reliability.");
    }
    private static void showSourceYearCoverage() {
        System.out.println("\n------ SOURCE YEAR COVERAGE ------");
        String key = findSourceKey(readText("Enter source name: "));
        if (key == null) {
            System.out.println("Source not found.");
            return;
        }
        Map<String, Integer> years = new TreeMap<>();
        for (Report report : sourceIndex.get(key)) {
            years.put(report.year, years.getOrDefault(report.year, 0) + 1);
        }
        System.out.println("Year-wise reports for " + key + ":");
        for (Map.Entry<String, Integer> entry : years.entrySet()) {
            System.out.println(entry.getKey() + " : "
                    + entry.getValue() + " reports");
        }
    }
    private static void showSourcesForYear() {
        System.out.println("\n------ SOURCES BY YEAR ------");
        String year = readText("Enter four-digit year: ");
        if (!year.matches("\\d{4}")) {
            System.out.println("Enter a valid four-digit year.");
            return;
        }
        Map<String, Integer> counts =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Report report : reports) {
            if (report.year.equals(year)) {
                counts.put(report.source,
                        counts.getOrDefault(report.source, 0) + 1);
            }
        }
        if (counts.isEmpty()) {
            System.out.println("No sources found for " + year + ".");
            return;
        }
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println(entry.getKey() + " - "
                    + entry.getValue() + " reports");
        }
        System.out.println("Distinct sources in " + year + ": " + counts.size());
    }
    private static void showCoverageSummary() {
        System.out.println("\n------ DATASET COVERAGE SUMMARY ------");
        int withLinks = 0;
        int withDates = 0;
        for (Report report : reports) {
            if (!report.link.isEmpty()) withLinks++;
            if (!report.date.isEmpty()) withDates++;
        }
        System.out.println("Total report records : " + reports.size());
        System.out.println("Distinct source names: " + sourceIndex.size());
        System.out.println("Records with links   : " + withLinks);
        System.out.println("Records with dates   : " + withDates);
        if (!reports.isEmpty()) {
            System.out.printf("Link coverage        : %.2f%%%n",
                    100.0 * withLinks / reports.size());
            System.out.printf("Date coverage        : %.2f%%%n",
                    100.0 * withDates / reports.size());
        }
        System.out.println("\nThis summary describes catalogue coverage.");
        System.out.println("It does not verify source credibility.");
    }
    private static void exportSummary() {
        System.out.println("\n------ EXPORT SOURCE SUMMARY ------");
        List<SourceStat> stats = getStatistics();
        stats.sort((a, b) -> Integer.compare(b.count, a.count));
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter("SourceCoverageSummary.txt"))) {
            writer.write("INTELLIGENCE SOURCE COVERAGE SUMMARY");
            writer.newLine();
            writer.write("Total reports: " + reports.size());
            writer.newLine();
            writer.write("Distinct sources: " + stats.size());
            writer.newLine();
            writer.newLine();
            for (SourceStat stat : stats) {
                writer.write(stat.name + " : " + stat.count + " reports");
                writer.newLine();
            }
            writer.newLine();
            writer.write("Counts show frequency, not source reliability.");
            writer.newLine();
            System.out.println("Saved as SourceCoverageSummary.txt");
        } catch (IOException error) {
            System.out.println("Could not write summary: " + error.getMessage());
        }
    }
    private static String findSourceKey(String requested) {
        for (String source : sourceIndex.keySet()) {
            if (source.equalsIgnoreCase(requested.trim())) {
                return source;
            }
        }
        return null;
    }
    private static String shorten(String value, int maximum) {
        if (value.length() <= maximum) return value;
        return value.substring(0, maximum - 3) + "...";
    }
    private static String readText(String prompt) {
        System.out.print(prompt);
        return input.nextLine().trim();
    }
    private static int readInteger(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException error) {
                System.out.println("Please enter a whole number.");
            }
        }
    }
}

