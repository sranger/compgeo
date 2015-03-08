package com.stephenwranger.compgeo.algorithms;

import java.util.List;

public interface Algorithm<T, U> {
   public boolean compute(final List<T> input, final List<U> output, final long timeout);
}
