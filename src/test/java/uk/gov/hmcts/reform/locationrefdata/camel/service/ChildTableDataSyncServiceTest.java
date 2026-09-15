package uk.gov.hmcts.reform.locationrefdata.camel.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChildTableDataSyncServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ChildTableDataSyncService childTableDataSyncService;

    @Test
    void syncDeletesRowsMissingFromFileAndUpsertsDesiredRows() {
        ChildTableSyncDefinition definition = new ChildTableSyncDefinition(
            "test_child",
            List.of("id", "type"),
            List.of("description")
        );

        when(jdbcTemplate.queryForList("SELECT id, type FROM test_child")).thenReturn(List.of(
            row("id", "1", "type", "A"),
            row("id", "2", "type", "A")
        ));

        childTableDataSyncService.sync(definition, List.of(
            row("id", "1", "type", "A", "description", "Updated"),
            row("id", "3", "type", "A", "description", "Created")
        ));

        ArgumentCaptor<List<Object[]>> deleteRowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(
            eq("DELETE FROM test_child WHERE id = ? AND type = ?"),
            deleteRowsCaptor.capture()
        );
        assertThat(deleteRowsCaptor.getValue())
            .singleElement()
            .satisfies(values -> assertThat(values).containsExactly("2", "A"));

        ArgumentCaptor<List<Object[]>> upsertRowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(
            eq("INSERT INTO test_child (id, type, description) VALUES (?, ?, ?) "
                   + "ON CONFLICT (id, type) DO UPDATE SET description = EXCLUDED.description"),
            upsertRowsCaptor.capture()
        );
        assertThat(upsertRowsCaptor.getValue())
            .hasSize(2)
            .anySatisfy(values -> assertThat(values).containsExactly("1", "A", "Updated"))
            .anySatisfy(values -> assertThat(values).containsExactly("3", "A", "Created"));
    }

    @Test
    void syncUsesDoNothingForKeyOnlyTables() {
        ChildTableSyncDefinition definition = new ChildTableSyncDefinition(
            "test_mapping",
            List.of("id", "type"),
            List.of()
        );

        when(jdbcTemplate.queryForList("SELECT id, type FROM test_mapping")).thenReturn(List.of());

        childTableDataSyncService.sync(definition, List.of(row("id", "1", "type", "A")));

        verify(jdbcTemplate).batchUpdate(
            eq("INSERT INTO test_mapping (id, type) VALUES (?, ?) ON CONFLICT (id, type) DO NOTHING"),
            anyList()
        );
    }

    @Test
    void syncSkipsUpsertWhenFileHasNoRows() {
        ChildTableSyncDefinition definition = new ChildTableSyncDefinition(
            "test_child",
            List.of("id"),
            List.of("description")
        );

        when(jdbcTemplate.queryForList("SELECT id FROM test_child")).thenReturn(List.of(row("id", "1")));

        childTableDataSyncService.sync(definition, List.of());

        verify(jdbcTemplate).batchUpdate(eq("DELETE FROM test_child WHERE id = ?"), anyList());
        verify(jdbcTemplate, never()).batchUpdate(
            eq("INSERT INTO test_child (id, description) VALUES (?, ?) "
                   + "ON CONFLICT (id) DO UPDATE SET description = EXCLUDED.description"),
            anyList()
        );
    }

    @Test
    void syncRejectsUnexpectedSqlIdentifiers() {
        ChildTableSyncDefinition definition = new ChildTableSyncDefinition(
            "test_child;drop",
            List.of("id"),
            List.of("description")
        );

        assertThatThrownBy(() -> childTableDataSyncService.sync(definition, List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid SQL identifier: test_child;drop");

        verify(jdbcTemplate, never()).queryForList(anyString());
    }

    private Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            row.put((String) values[index], values[index + 1]);
        }
        return row;
    }
}
