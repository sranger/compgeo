package com.stephenwranger.compgeo.algorithms.convexhull;

import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.graphics.math.Tuple2d;

public class ConvexHullGrahamsScan implements Algorithm<Tuple2d, Tuple2d> {
   public ConvexHullGrahamsScan() {
      // nothing
   }

   @Override
   public boolean compute(final List<Tuple2d> input, final List<Tuple2d> output, final long timeout) {
      return false;
   }
}
