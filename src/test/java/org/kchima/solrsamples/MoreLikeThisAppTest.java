package org.kchima.solrsamples;

import org.apache.solr.common.SolrDocument;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MoreLikeThisAppTest {

    private static Logger log = LoggerFactory.getLogger(MoreLikeThisAppTest.class);

    private static final String[] DEFAULT_FIELDS_TO_RETURN = new String[] {"dc_title", "brc_abstract"};

    @Test
    public void main(String[] args) throws Exception {
        MoreLikeThisApp app = new MoreLikeThisApp("http://localhost:8181/collection1", "http://localhost:8282/collection1");
        app.setFieldsToReturn(DEFAULT_FIELDS_TO_RETURN);
        List<SolrDocument> relatedDocs = app.getRelatedDocuments("s2_id", "black-holes/all-black-holes/rotating-black-holes;withtext1/withtext1/1234567890123.pdf");
        logRelatedDocs(relatedDocs);
    }

    private void logRelatedDocs(List<SolrDocument> relatedDocs) {
        log.debug("Found {} related docs", relatedDocs.size());

        for (SolrDocument doc : relatedDocs) {
            StringBuilder b = new StringBuilder();
            for (String field : DEFAULT_FIELDS_TO_RETURN) {
                b.append(field).append("=").append((String) doc.getFieldValue(field)).append(",");
            }
            log.debug("doc[{}]", b.toString());
        }
    }
}
