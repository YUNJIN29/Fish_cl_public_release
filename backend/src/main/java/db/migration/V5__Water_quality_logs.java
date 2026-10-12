package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V5__Water_quality_logs extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    context.getConnection().createStatement().execute("""
        create table if not exists water_quality_records (
          id bigint not null auto_increment primary key,
          user_id bigint not null,
          pond_name varchar(64) not null,
          temperature decimal(6,2) null,
          ph decimal(5,2) null,
          dissolved_oxygen decimal(7,3) null,
          ammonia decimal(8,3) null,
          nitrite decimal(8,3) null,
          notes varchar(500) null,
          observed_at timestamp(6) not null,
          created_at timestamp(6) not null,
          constraint fk_water_quality_user foreign key (user_id) references users(id) on delete cascade
        )
        """);
    context.getConnection().createStatement().execute("create index idx_water_quality_user_time on water_quality_records(user_id, observed_at)");
  }
}
