package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V3__Controlled_upload_resources extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    context.getConnection().createStatement().execute("""
        create table if not exists upload_resources (
          id varchar(36) not null primary key,
          owner_user_id bigint not null,
          original_name varchar(255) not null,
          stored_name varchar(100) not null,
          content_type varchar(80) not null,
          size_bytes bigint not null,
          created_at timestamp(6) not null,
          constraint uk_upload_resources_stored_name unique (stored_name),
          constraint fk_upload_resources_owner foreign key (owner_user_id) references users(id) on delete cascade
        )
        """);
    context.getConnection().createStatement().execute("create index idx_upload_resources_owner_created on upload_resources(owner_user_id, created_at)");
  }
}
