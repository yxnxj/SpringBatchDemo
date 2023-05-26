package com.example.batch;

import com.example.batch.entity.Alarm;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class NoOpItemWriter implements ItemWriter<Alarm> {

    @Override
    public void write(Chunk chunk) throws Exception {

    }
}
