package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V7__Water_quality_targets extends BaseJavaMigration {
  @Override
  public void migrate(Context context) throws Exception {
    context.getConnection().createStatement().execute("""
        create table if not exists water_quality_targets (
          id bigint not null auto_increment primary key,
          user_id bigint not null,
          pond_name varchar(64) not null,
          temperature_min decimal(6,2) null,
          temperature_max decimal(6,2) null,
          ph_min decimal(5,2) null,
          ph_max decimal(5,2) null,
          dissolved_oxygen_min decimal(7,3) null,
          ammonia_max decimal(8,3) null,
          nitrite_max decimal(8,3) null,
          created_at timestamp(6) not null,
          updated_at timestamp(6) not null,
          constraint uk_water_quality_target_user_pond unique (user_id, pond_name),
          constraint fk_water_quality_target_user foreign key (user_id) references users(id) on delete cascade
        )
        """);
  }
}
