package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V11__Guestbook_messages extends BaseJavaMigration {
  @Override public void migrate(Context context) throws Exception {
    try (var statement = context.getConnection().createStatement()) {
      statement.execute("""
          create table guestbook_messages (
            id bigint not null auto_increment,
            user_id bigint not null,
            content varchar(500) not null,
            status varchar(16) not null default 'PENDING',
            admin_reply varchar(500) null,
            created_at datetime(6) not null,
            reviewed_at datetime(6) null,
            primary key (id),
            constraint fk_guestbook_user foreign key (user_id) references users(id) on delete cascade,
            index idx_guestbook_status_created (status, created_at desc),
            index idx_guestbook_user_created (user_id, created_at desc)
          ) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci
          """);
    }
  }
}
