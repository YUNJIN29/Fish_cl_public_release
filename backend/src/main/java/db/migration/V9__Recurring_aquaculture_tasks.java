package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V9__Recurring_aquaculture_tasks extends BaseJavaMigration {
  @Override public void migrate(Context context) throws Exception {
    context.getConnection().createStatement().execute("""
      alter table aquaculture_tasks
        add column repeat_interval_days int null after notes,
        add column next_task_created boolean not null default false after repeat_interval_days
      """);
  }
}
