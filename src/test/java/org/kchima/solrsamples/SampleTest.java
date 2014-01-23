package org.kchima.solrsamples;

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.util.AbstractSolrTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SampleTest extends AbstractSolrTestCase {

    private static final String SOLR_HOME = "solr";
    private static final String SOLR_SCHEMA = "solr/collection1/conf/schema.xml";
    private static final String SOLR_CONFIG = "solr/collection1/conf/solrconfig.xml";

    private static SolrServer solrServer;

    @Override
    public String getSolrHome() {
        return SOLR_HOME;
    }

    public static String getSchemaFile() {
        return SOLR_SCHEMA;
    }

    public static String getSolrConfigFile() {
        return SOLR_CONFIG;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        SolrTestCaseJ4.initCore(SOLR_CONFIG, SOLR_SCHEMA, SOLR_HOME);
        solrServer = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
    }

    @AfterClass
    public static void afterClass() {
        if (solrServer != null) {
            solrServer.shutdown();
        }
    }

    @Test
    public void testOne() {
        assertNotNull(solrServer);
    }
}
