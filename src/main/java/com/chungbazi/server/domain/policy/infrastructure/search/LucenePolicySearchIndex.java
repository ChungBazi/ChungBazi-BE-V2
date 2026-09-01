package com.chungbazi.server.domain.policy.infrastructure.search;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Component;

@Component
public class LucenePolicySearchIndex {

    private static final String POLICY_ID = "policyId";
    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";
    private static final String SUPPORT_CONTENT = "supportContent";

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;
    private final SearcherManager searcherManager;

    public LucenePolicySearchIndex() throws IOException {
        directory = new ByteBuffersDirectory();
        analyzer = new KoreanAnalyzer();
        writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));
        searcherManager = new SearcherManager(writer, null);
    }

    public synchronized void rebuild(List<PolicySearchDocument> policies) throws IOException {
        writer.deleteAll();

        for (PolicySearchDocument policy : policies) {
            writer.addDocument(toDocument(policy));
        }
        writer.commit();
        searcherManager.maybeRefreshBlocking();
    }

    public int documentCount() throws IOException {
        var searcher = searcherManager.acquire();
        try {
            return searcher.getIndexReader().numDocs();
        } finally {
            searcherManager.release(searcher);
        }
    }

    private Document toDocument(PolicySearchDocument policy) {
        Document document = new Document();
        document.add(new LongPoint(POLICY_ID, policy.policyId()));
        document.add(new StoredField(POLICY_ID, policy.policyId()));
        document.add(new TextField(TITLE, valueOrEmpty(policy.title()), Field.Store.NO));
        document.add(new TextField(SUMMARY, valueOrEmpty(policy.summary()), Field.Store.NO));
        document.add(new TextField(SUPPORT_CONTENT, valueOrEmpty(policy.supportContent()), Field.Store.NO));
        return document;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    @PreDestroy
    public void close() throws IOException {
        searcherManager.close();
        writer.close();
        analyzer.close();
        directory.close();
    }
}
