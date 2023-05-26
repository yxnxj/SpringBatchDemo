package com.example.batch;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;

public class SkipWritePolicy implements org.springframework.batch.core.step.skip.SkipPolicy {
    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        return true;
    }
}
