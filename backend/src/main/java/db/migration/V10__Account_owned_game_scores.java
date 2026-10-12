package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V10__Account_owned_game_scores extends BaseJavaMigration {
  @Override public void migrate(Context context) throws Exception {
    try(var statement=context.getConnection().createStatement()) {
      statement.execute("alter table game_scores add column user_id bigint null after ip_address");
      statement.execute("alter table game_scores drop index uk_game_ip");
      statement.execute("alter table game_scores add constraint fk_game_score_user foreign key (user_id) references users(id) on delete cascade");
      statement.execute("create unique index uk_game_user on game_scores(game_key,user_id)");
      statement.execute("create index idx_game_score_rank on game_scores(game_key,score desc,updated_at)");
    }
  }
}
