package org.kchima.solrsamples;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class finds documents related to others.
 */
public class MoreLikeThisApp {

    private static Logger log = LoggerFactory.getLogger(MoreLikeThisApp.class);

    private static final String[] FIELDS_TO_RETURN = new String[] {"dc_title", "brc_abstract"};

    private final List<SolrServer> solrServers;

    public MoreLikeThisApp(String... baseUrls) {
        solrServers = new ArrayList<>();
        for (String baseUrl : baseUrls) {
            solrServers.add(new HttpSolrServer(baseUrl));
        }
    }

    public List<SolrDocument> getRelatedDocuments(String field, String value) throws SolrServerException {
        List<SolrDocument> relatedDocs = new ArrayList<>();

        SolrDocument doc = findDoc(field, value);

        if (doc != null) {
            relatedDocs = findRelatedDocs(doc);
        }

        return relatedDocs;
    }

    private SolrDocument findDoc(String field, String value) throws SolrServerException {
        SolrDocument doc = null;

        for (SolrServer solrServer : solrServers) {
            SolrQuery q = new SolrQuery(field + ":" + value);
            q.set(CommonParams.ROWS, "1");
            q.set(CommonParams.FL, FIELDS_TO_RETURN);
            QueryResponse rsp = solrServer.query(q);
            SolrDocumentList results = rsp.getResults();

            if (results == null || results.isEmpty()) {
                log.debug("No doc found with {}:{}", field, value);
                continue;
            }

            log.debug("Found doc");

            doc = results.get(0);
        }

        return doc;
    }

    private List<SolrDocument> findRelatedDocs(SolrDocument doc) throws SolrServerException {
        List<SolrDocument> docs = new ArrayList<>();

        String docContent = getContents(doc, FIELDS_TO_RETURN);

        for (SolrServer solrServer : solrServers) {
            SolrQuery q = new SolrQuery();
            q.set(CommonParams.QT, "/mlt");
            q.set(MoreLikeThisParams.BOOST, true);
            q.set(MoreLikeThisParams.INTERESTING_TERMS, "details");
            q.set(CommonParams.ROWS, "5");
            q.set(MoreLikeThisParams.SIMILARITY_FIELDS, "dc_title", "brc_abstract", "s2_content_xml_stripped");
            q.set(MoreLikeThisParams.QF, "dc_title^0.5 brc_abstract^1.5 s2_content_xml_stripped^2.5");
            q.set(CommonParams.STREAM_BODY, docContent);

            QueryResponse rsp = solrServer.query(q);

            logInterestingTerms(rsp.getResponse());

            SolrDocumentList results = rsp.getResults();

            if (results == null || results.isEmpty()) {
                continue;
            }

            Iterator<SolrDocument> iter = results.iterator();

            while (iter.hasNext()) {
                docs.add(iter.next());
            }

            break;
        }

        return docs;
    }

    private String getContents(SolrDocument doc, String... fields) {
        StringBuilder b = new StringBuilder();
        for (String field : fields) {
            String value = (String)doc.getFieldValue(field);
            if (StringUtils.isNotBlank(value)) {
                b.append(doc.getFieldValue(field)).append(" ");
            }
        }
        return b.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        if (solrServers != null && !solrServers.isEmpty()) {
            for (SolrServer solrServer : solrServers) {
                solrServer.shutdown();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        MoreLikeThisApp app = new MoreLikeThisApp("http://localhost:8181/collection1", "http://localhost:8282/collection1");
        List<SolrDocument> relatedDocs = app.getRelatedDocuments("s2_id", "black-holes/all-black-holes/rotating-black-holes;withtext1/withtext1/1234567890123.pdf");

        logRelatedDocs(relatedDocs);
    }

    private static void logRelatedDocs(List<SolrDocument> relatedDocs) {
        log.debug("Found {} related docs", relatedDocs.size());

        for (SolrDocument doc : relatedDocs) {
            StringBuilder b = new StringBuilder();
            for (String field : FIELDS_TO_RETURN) {
                b.append(field).append("=").append((String) doc.getFieldValue(field)).append(",");
            }
            log.debug("doc[{}]", b.toString());
        }
    }


    private void logInterestingTerms(NamedList<Object> response) {
        if (response != null && response.get("interestingTerms") != null) {
            Object interestingTerms = response.get("interestingTerms");
            log.debug("interesting terms: {}", interestingTerms);
        }
    }
}
