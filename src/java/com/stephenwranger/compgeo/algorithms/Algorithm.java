package com.stephenwranger.compgeo.algorithms;

import java.util.List;

public interface Algorithm<T> {
   public boolean compute(final List<T> input, final List<T> output, final long timeout);
}
