package com.chungbazi.server.domain.policy.infrastructure.search;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ko.KoreanAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springframework.stereotype.Component;

@Component
public class LucenePolicySearchIndex {

    private static final String POLICY_ID = "policyId";
    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";
    private static final String SUPPORT_CONTENT = "supportContent";
    private static final String[] SEARCH_FIELDS = {TITLE, SUMMARY, SUPPORT_CONTENT};

    private static final Map<String, Float> FIELD_BOOSTS = Map.of(
            TITLE, 3.0f,
            SUMMARY, 2.0f,
            SUPPORT_CONTENT, 1.0f
    );

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;
    private final SearcherManager searcherManager;

    public LucenePolicySearchIndex() throws IOException {
        directory = new ByteBuffersDirectory();

        // 한글 문장을 형태소 단위로 분석
        analyzer = new KoreanAnalyzer();
        writer = new IndexWriter(directory, new IndexWriterConfig(analyzer).setSimilarity(new BM25Similarity()));

        searcherManager = new SearcherManager(writer, null);
    }

    // 기존 문서를 제거하고, 현재 검색 가능한 활성 정책으로 인덱스 교체
    public synchronized void rebuild(List<PolicySearchDocument> policies) throws IOException {
        writer.deleteAll();

        for (PolicySearchDocument policy : policies) {
            writer.addDocument(toDocument(policy));
        }
        writer.commit();
        searcherManager.maybeRefreshBlocking();
    }

    // 제목, 요약, 지원 내용에서 키워드 검색 후, BM25 관련도가 높은 순서로 반환
    public List<PolicySearchResult> search(String keyword, int size) throws IOException, ParseException {
        if (keyword == null || keyword.isBlank() || size <= 0) {
            return List.of();
        }

        MultiFieldQueryParser parser = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer, FIELD_BOOSTS);
        Query query = parser.parse(QueryParser.escape(keyword.trim()));

        IndexSearcher searcher = searcherManager.acquire();

        try {
            TopDocs topDocs = searcher.search(query, size);
            StoredFields storedFields = searcher.storedFields();

            return Arrays.stream(topDocs.scoreDocs)
                    .map(scoreDoc -> toSearchResult(storedFields, scoreDoc))
                    .toList();
        } finally {
            searcherManager.release(searcher);
        }
    }

    public int documentCount() throws IOException {
        IndexSearcher searcher = searcherManager.acquire();
        try {
            return searcher.getIndexReader().numDocs();
        } finally {
            searcherManager.release(searcher);
        }
    }

    private Document toDocument(PolicySearchDocument policy) {
        Document document = new Document();

        // ID는 형태소 분석하지 않고 그대로 색인, 검색 결과에서 꺼낼 수 있도록 저장
        document.add(new StringField(POLICY_ID, policy.policyId().toString(), Field.Store.YES));
        document.add(new TextField(TITLE, valueOrEmpty(policy.title()), Field.Store.NO));
        document.add(new TextField(SUMMARY, valueOrEmpty(policy.summary()), Field.Store.NO));
        document.add(new TextField(SUPPORT_CONTENT, valueOrEmpty(policy.supportContent()), Field.Store.NO));
        return document;
    }

    private PolicySearchResult toSearchResult(StoredFields storedFields, ScoreDoc scoreDoc) {
        try {
            Document document = storedFields.document(scoreDoc.doc);
            return new PolicySearchResult(
                    Long.parseLong(document.get(POLICY_ID)),
                    scoreDoc.score
            );
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
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
