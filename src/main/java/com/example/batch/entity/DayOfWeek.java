package com.example.batch.entity;

import com.example.batch.enums.Week;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Builder
public class DayOfWeek extends BaseTimeEntity {
    @EmbeddedId
    private DayOfWeekId dayOfWeekId;

    @MapsId("alarmId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="alarm_id")
    private Alarm alarm;

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    @Builder
    public static class DayOfWeekId implements Serializable {
        private Long alarmId;

        @Enumerated(EnumType.STRING)
        private Week day;
    }

}
