package eu.leads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.dumpreader.DumpReader;
import org.sweble.wikitext.dumpreader.export_0_10.PageType;
import org.sweble.wikitext.dumpreader.export_0_10.RevisionType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class XmlDumpParser {

    static Logger logger = LoggerFactory.getLogger(XmlDumpParser.class.getClass());

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            printUsage();
            System.exit(0);
        }
        String xmlFileName = args[0];
        final File file = new File(xmlFileName);
        final InputStream is = new FileInputStream(file);
        String directoryPath = args[1];
        final File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                logger.error("Could not successfully create directory " + directoryPath);
                System.exit(1);
            }
        }

        final Map<String, List<String>> history = new HashMap<>();
        logger.info("Start parsing dump file " + xmlFileName);
        DumpReader reader = new DumpReader(is, Charset.forName("UTF-8"), "", logger, false) {
            @Override
            protected void processPage(Object mediaWiki, Object p) {
                PageType page = (PageType) p;
                List<Object> revisions = page.getRevisionOrUpload();
                logger.info("nbRevisions: " + revisions.size());
                File pageDirectory = new File(directory.getAbsolutePath() + "/" + page.getId());
                if (!pageDirectory.exists()) {
                    if (!pageDirectory.mkdirs()) {
                        logger.error("Could not successfully create page directory " + pageDirectory);
                        return;
                    }
                }
                for (Object r : revisions) {
                    RevisionType revision = (RevisionType) r;
                    Path revisionOnDisk = Paths.get(pageDirectory.getAbsolutePath(), revision.getTimestamp().toString());
                    String timestamp = revision.getTimestamp().toString();
                    if (!history.containsKey(timestamp)) {
                        history.put(timestamp, new ArrayList<String>());
                    }
                    String relativePath = Paths.get(page.getId().toString(), revision.getTimestamp().toString()).toString();
                    history.get(timestamp).add(relativePath);
                    if (!Files.exists(revisionOnDisk)) {
                        try {
                            Files.write(revisionOnDisk, revision.getText().getValue().getBytes());
                        } catch (IOException e) {
                            logger.error("Could not write file " + revisionOnDisk + "(page id: " + page.getId() + ", revision id: " + revision.getId() + " )");
                        }
                    }
                }
            }
        };
        reader.unmarshal();
        List<String> timestamps = new ArrayList(history.keySet());
        Collections.sort(timestamps);
        Path timeline = Paths.get(directory.getAbsolutePath(), "timeline.txt");
        try (FileWriter writer = new FileWriter(timeline.toString())) {
            for (String timestamp : timestamps) {
                List<String> revisions = history.get(timestamp);
                for (String revision : revisions) {
                    writer.append(revision + System.lineSeparator());
                }
            }
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar myprogram.jar <path/to/dump.xml> <path/to/dump_directory>");
        System.out.println("");
        System.out.println("Arguments: ");
        System.out.println("\tdump.xml         Path to the xml file to parse");
        System.out.println("\tdump_directory   Path to the directory where the changes should be written");
    }
}
