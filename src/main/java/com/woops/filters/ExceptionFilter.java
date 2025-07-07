package com.woops.filters;

import com.woops.Sequence;

public class ExceptionFilter implements Filter {
    @Override
    public boolean isValid(Sequence sequence) {
        return !sequence.throwsException();  
    }

    @Override
    public String getName() {
        return "ExceptionFilter";
    }
}
