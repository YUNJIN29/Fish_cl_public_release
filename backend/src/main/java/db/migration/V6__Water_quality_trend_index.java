package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class V6__Water_quality_trend_index extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();
    if (!hasIndex(connection, "water_quality_records", "idx_water_quality_user_pond_observed")) {
      try (Statement statement = connection.createStatement()) {
        statement.execute("CREATE INDEX idx_water_quality_user_pond_observed ON water_quality_records(user_id, pond_name, observed_at)");
      }
    }
  }

  private boolean hasIndex(Connection connection, String table, String index) throws Exception {
    try (ResultSet result = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, table, false, false)) {
      while (result.next()) if (index.equalsIgnoreCase(result.getString("INDEX_NAME"))) return true;
    }
    return false;
  }
}
