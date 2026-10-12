package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V4__Diagnosis_follow_ups extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    context.getConnection().createStatement().execute("""
        create table if not exists diagnosis_follow_ups (
          id bigint not null auto_increment primary key,
          diagnose_record_id bigint not null,
          user_id bigint not null,
          condition_status varchar(16) not null,
          appetite_status varchar(16) not null,
          mortality_count int not null default 0,
          water_quality varchar(255) null,
          notes varchar(1000) null,
          observed_at timestamp(6) not null,
          created_at timestamp(6) not null,
          constraint fk_follow_up_diagnosis foreign key (diagnose_record_id) references diagnose_records(id) on delete cascade,
          constraint fk_follow_up_user foreign key (user_id) references users(id) on delete cascade
        )
        """);
    context.getConnection().createStatement().execute(
        "create index idx_follow_up_diagnosis_time on diagnosis_follow_ups(diagnose_record_id, observed_at)");
    context.getConnection().createStatement().execute(
        "create index idx_follow_up_user_created on diagnosis_follow_ups(user_id, created_at)");
  }
}
