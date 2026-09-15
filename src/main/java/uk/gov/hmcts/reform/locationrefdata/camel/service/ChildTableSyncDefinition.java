package uk.gov.hmcts.reform.locationrefdata.camel.service;

import java.util.List;
import java.util.stream.Stream;

public class ChildTableSyncDefinition {

    private final String tableName;
    private final List<String> keyColumns;
    private final List<String> dataColumns;

    public ChildTableSyncDefinition(String tableName, List<String> keyColumns, List<String> dataColumns) {
        this.tableName = tableName;
        this.keyColumns = List.copyOf(keyColumns);
        this.dataColumns = List.copyOf(dataColumns);
    }

    public String tableName() {
        return tableName;
    }

    public List<String> keyColumns() {
        return keyColumns;
    }

    public List<String> dataColumns() {
        return dataColumns;
    }

    public List<String> columns() {
        return Stream.concat(keyColumns.stream(), dataColumns.stream()).toList();
    }
}
