package uk.gov.hmcts.reform.locationrefdata.camel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChildTableDataSyncService {

    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[a-z][a-z0-9_]*");

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void sync(ChildTableSyncDefinition definition, List<Map<String, Object>> desiredRows) {
        validateDefinition(definition);

        List<Map<String, Object>> uniqueDesiredRows = removeDuplicateKeys(definition, desiredRows);
        deleteRowsMissingFromFile(definition, uniqueDesiredRows);
        upsertRowsFromFile(definition, uniqueDesiredRows);
    }

    private void deleteRowsMissingFromFile(ChildTableSyncDefinition definition,
                                           List<Map<String, Object>> desiredRows) {
        List<Map<String, Object>> existingRows = jdbcTemplate.queryForList(selectKeysSql(definition));
        var desiredKeys = desiredRows.stream()
            .map(row -> key(definition, row))
            .collect(Collectors.toSet());

        List<Object[]> rowsToDelete = existingRows.stream()
            .filter(row -> !desiredKeys.contains(key(definition, row)))
            .map(row -> keyValues(definition, row))
            .toList();

        if (!rowsToDelete.isEmpty()) {
            jdbcTemplate.batchUpdate(deleteSql(definition), rowsToDelete);
        }
    }

    private void upsertRowsFromFile(ChildTableSyncDefinition definition,
                                    List<Map<String, Object>> desiredRows) {
        if (desiredRows.isEmpty()) {
            return;
        }

        List<String> columns = definition.columns();
        List<Object[]> rowsToUpsert = desiredRows.stream()
            .map(row -> columns.stream().map(row::get).toArray())
            .toList();

        jdbcTemplate.batchUpdate(upsertSql(definition), rowsToUpsert);
    }

    private List<Map<String, Object>> removeDuplicateKeys(ChildTableSyncDefinition definition,
                                                          List<Map<String, Object>> desiredRows) {
        Map<List<Object>, Map<String, Object>> uniqueRows = new LinkedHashMap<>();
        desiredRows.forEach(row -> uniqueRows.put(key(definition, row), row));
        return List.copyOf(uniqueRows.values());
    }

    private List<Object> key(ChildTableSyncDefinition definition, Map<String, Object> row) {
        return definition.keyColumns().stream()
            .map(row::get)
            .toList();
    }

    private Object[] keyValues(ChildTableSyncDefinition definition, Map<String, Object> row) {
        return definition.keyColumns().stream()
            .map(row::get)
            .toArray();
    }

    private String selectKeysSql(ChildTableSyncDefinition definition) {
        return "SELECT " + String.join(", ", definition.keyColumns())
            + " FROM " + definition.tableName();
    }

    private String deleteSql(ChildTableSyncDefinition definition) {
        return "DELETE FROM " + definition.tableName()
            + " WHERE " + whereClause(definition.keyColumns());
    }

    private String upsertSql(ChildTableSyncDefinition definition) {
        List<String> columns = definition.columns();
        String updateClause = definition.dataColumns().isEmpty()
            ? " DO NOTHING"
            : " DO UPDATE SET " + definition.dataColumns().stream()
                .map(column -> column + " = EXCLUDED." + column)
                .collect(Collectors.joining(", "));

        return "INSERT INTO " + definition.tableName()
            + " (" + String.join(", ", columns) + ")"
            + " VALUES (" + columns.stream().map(column -> "?").collect(Collectors.joining(", ")) + ")"
            + " ON CONFLICT (" + String.join(", ", definition.keyColumns()) + ")"
            + updateClause;
    }

    private String whereClause(List<String> keyColumns) {
        return keyColumns.stream()
            .map(column -> column + " = ?")
            .collect(Collectors.joining(" AND "));
    }

    private void validateDefinition(ChildTableSyncDefinition definition) {
        validateIdentifier(definition.tableName());
        definition.columns().forEach(this::validateIdentifier);
    }

    private void validateIdentifier(String identifier) {
        if (!SQL_IDENTIFIER.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
    }
}
