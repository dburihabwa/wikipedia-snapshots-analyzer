package eu.leads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.dumpreader.DumpReader;
import org.sweble.wikitext.dumpreader.export_0_10.PageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class XmlDumpParser {

    static Logger logger = LoggerFactory.getLogger(XmlDumpParser.class.getClass());

    public static void main(String[] args) throws Exception {

        String xmlFileName = args[0];
        final File file = new File(xmlFileName);
        final InputStream is = new FileInputStream(file);
        logger.info("Start parsing dump file " + xmlFileName);
        DumpReader reader = new DumpReader(is, Charset.forName("UTF-8"), "", logger, false) {
            @Override
            protected void processPage(Object mediaWiki, Object page) {
                PageType p = (PageType) page;
                List<Object> revisions = p.getRevisionOrUpload();
                logger.info("nbRevisions: " + revisions.size());
                for (Object v : revisions) {
                    System.out.println(v);
                }
            }
        };
        reader.unmarshal();
    }
}
