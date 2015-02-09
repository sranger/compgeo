package com.stephenwranger.compgeo.algorithms;

import java.util.List;

import com.stephenwranger.graphics.collections.Pair;

public interface Algorithm<T> {
   public void compute(final List<T> input, final List<T> output, final List<Pair<T, T>> outputEdges);
}
