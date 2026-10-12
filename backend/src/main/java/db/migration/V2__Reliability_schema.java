package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V2__Reliability_schema extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    if (tableExists(connection, "diagnose_records")) {
      addColumn(connection, "diagnose_records", "confidence", "varchar(16) null");
      addColumn(connection, "diagnose_records", "evidence_json", "text null");
      addColumn(connection, "diagnose_records", "sources_json", "text null");
      addColumn(connection, "diagnose_records", "warnings_json", "text null");
      addColumn(connection, "diagnose_records", "next_steps_json", "text null");
      addIndex(connection, "diagnose_records", "idx_diagnose_created_at", "created_at");
    }
    if (tableExists(connection, "daily_visits")) {
      addIndex(connection, "daily_visits", "idx_daily_visits_last_visited", "last_visited_at");
    }
  }

  private boolean tableExists(Connection connection, String table) throws SQLException {
    try (ResultSet result = connection.getMetaData().getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
      return result.next();
    }
  }

  private void addColumn(Connection connection, String table, String column, String definition) throws SQLException {
    try (ResultSet result = connection.getMetaData().getColumns(connection.getCatalog(), null, table, column)) {
      if (result.next()) return;
    }
    connection.createStatement().execute("alter table " + table + " add column " + column + " " + definition);
  }

  private void addIndex(Connection connection, String table, String index, String column) throws SQLException {
    try (ResultSet result = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, table, false, false)) {
      while (result.next()) if (index.equalsIgnoreCase(result.getString("INDEX_NAME"))) return;
    }
    connection.createStatement().execute("create index " + index + " on " + table + " (" + column + ")");
  }
}
