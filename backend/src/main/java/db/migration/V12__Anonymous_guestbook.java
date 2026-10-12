package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V12__Anonymous_guestbook extends BaseJavaMigration {
  @Override public void migrate(Context context) throws Exception {
    try (var statement = context.getConnection().createStatement()) {
      statement.execute("alter table guestbook_messages add column author_name varchar(32) null after user_id");
      statement.execute("update guestbook_messages g join users u on g.user_id=u.id set g.author_name=coalesce(nullif(u.nickname,''),'访客') where g.author_name is null");
      statement.execute("alter table guestbook_messages modify column author_name varchar(32) not null");
      statement.execute("alter table guestbook_messages modify column user_id bigint null");
    }
  }
}
