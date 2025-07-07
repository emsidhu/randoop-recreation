package com.woops.filters;

import com.woops.Sequence;

import java.util.HashSet;
import java.util.Set;

public class EqualityFilter implements Filter {
    private final Set<String> seen = new HashSet<>();

    @Override
    public boolean isValid(Sequence sequence) {
        Object result = sequence.getLastResult();
        if (result == null) return false;
        String repr = result.toString();
        if (seen.contains(repr)) return false;
        seen.add(repr);
        return true;
    }

    @Override
    public String getName() {
        return "EqualityFilter";
    }
}
