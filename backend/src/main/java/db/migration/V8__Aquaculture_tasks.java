package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V8__Aquaculture_tasks extends BaseJavaMigration {
  @Override public void migrate(Context context) throws Exception {
    context.getConnection().createStatement().execute("""
      create table if not exists aquaculture_tasks (
        id bigint not null auto_increment primary key,
        user_id bigint not null,
        title varchar(100) not null,
        category varchar(24) not null,
        pond_name varchar(64) null,
        due_at timestamp(6) not null,
        notes varchar(500) null,
        completed_at timestamp(6) null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        constraint fk_aquaculture_task_user foreign key (user_id) references users(id) on delete cascade,
        index idx_aquaculture_task_user_status_due (user_id, completed_at, due_at)
      )
      """);
  }
}
