package com.example.batch.processor;

import com.example.batch.AlarmRepository;
import com.example.batch.entity.Alarm;
import com.example.batch.entity.DayOfWeek;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@RequiredArgsConstructor
public class AlarmItemProcessor implements ItemProcessor<DayOfWeek, Alarm> {

    private final AlarmRepository alarmRepository;

    @Override
    public Alarm process(DayOfWeek item) throws Exception {
        LocalDate localDate = LocalDate.now();
        String day = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT_STANDALONE, new Locale("eng")).toUpperCase(Locale.ROOT);

        String storedDay = item.getDayOfWeekId().getDay().toString();
        if(day.equals(storedDay)) {

            System.out.println("Day: " + day.toString());
            return alarmRepository.findById(item.getAlarm().getId()).get();
        }
        return null;
    }
}
