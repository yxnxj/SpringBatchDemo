package com.example.batch;

import com.example.batch.entity.Alarm;
import com.example.batch.entity.DayOfWeek;
import com.example.batch.enums.Week;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import javax.swing.tree.TreePath;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class DayOfWeekRowMapper implements RowMapper<DayOfWeek> {
    private final AlarmRepository alarmRepository;


    @Override
    public DayOfWeek mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long alarmId = rs.getLong("alarm_id");
        Alarm alarm = alarmRepository.findById(alarmId).get();
        DayOfWeek.DayOfWeekId dayOfWeekId = DayOfWeek.DayOfWeekId.builder()
                .alarmId(alarmId)
                .day(Week.valueOf(rs.getString("day")))
                .build();

        DayOfWeek dayOfWeek = DayOfWeek.builder()
                .dayOfWeekId(dayOfWeekId)
                .alarm(alarm)
                .build();

        return dayOfWeek;
    }
}
