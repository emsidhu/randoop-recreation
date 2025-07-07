package com.woops.filters;

import com.woops.Sequence;

public interface Filter {
    boolean isValid(Sequence sequence);
    String getName();
}
