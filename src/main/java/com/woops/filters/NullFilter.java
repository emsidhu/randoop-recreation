package com.woops.filters;

import com.woops.Sequence;

public class NullFilter implements Filter {
    @Override
    public boolean isValid(Sequence sequence) {
        return sequence.getLastResult() != null;
    }

    @Override
    public String getName() {
        return "NullFilter";
    }
}
