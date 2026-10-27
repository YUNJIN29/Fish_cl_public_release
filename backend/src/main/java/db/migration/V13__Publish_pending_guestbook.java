package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V13__Publish_pending_guestbook extends BaseJavaMigration {
  @Override public void migrate(Context context) throws Exception {
    try (var statement = context.getConnection().createStatement()) {
      statement.execute("update guestbook_messages set status='APPROVED', reviewed_at=coalesce(reviewed_at, now(6)) where status='PENDING'");
    }
  }
}
