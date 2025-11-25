package com.example.templatemerge.postprocess.model;

import java.util.List;

/**
 * Represents a data table for rendering in FreeMarker templates
 */
public class TableModel {
    private String id;
    private String title;
    private List<TableHeader> headers;
    private List<TableRow> rows;
    private Map<String, Object> totals; // Optional: aggregation totals
    private String cssClass; // Optional: custom CSS class
    
    // Constructors
    public TableModel() {}
    
    public TableModel(String title, List<TableHeader> headers, List<TableRow> rows) {
        this.title = title;
        this.headers = headers;
        this.rows = rows;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<TableHeader> getHeaders() {
        return headers;
    }
    
    public void setHeaders(List<TableHeader> headers) {
        this.headers = headers;
    }
    
    public List<TableRow> getRows() {
        return rows;
    }
    
    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }
    
    public Map<String, Object> getTotals() {
        return totals;
    }
    
    public void setTotals(Map<String, Object> totals) {
        this.totals = totals;
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}