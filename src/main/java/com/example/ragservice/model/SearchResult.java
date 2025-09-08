package com.example.ragservice.model;

public class SearchResult {
    
    private Document document;
    private double score;

    public SearchResult() {}

    public SearchResult(Document document, double score) {
        this.document = document;
        this.score = score;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "document=" + document +
                ", score=" + score +
                '}';
    }
}
