package com.stephenwranger.compgeo.algorithms.convexhull;

import java.util.ArrayList;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;

public class ConvexHullJarvisMarch implements Algorithm<Tuple2d> {
   public ConvexHullJarvisMarch() {
      // nothing
   }

   @Override
   public void compute(final List<Tuple2d> input, final List<Tuple2d> output, final List<Pair<Tuple2d, Tuple2d>> outputEdges) {
      final int count = input.size();
      final List<Integer> result = new ArrayList<Integer>();

      int lowest = 0;

      for (int i = 1; i < input.size(); i++) {
         if (input.get(i).x <= input.get(lowest).x && input.get(i).y < input.get(lowest).y) {
            lowest = i;
         }
      }

      int p = lowest;
      int q;

      do {
         q = (p+1) % count;
         result.add(p);

         for(int i = 0; i < count; i++) {
            if (ConvexHullJarvisMarch.isCounterClockwise(input.get(p), input.get(i), input.get(q))) {
               q = i;
            }
         }

         p = q;
      } while(p != lowest);

      for (int i = 0; i < result.size(); i++) {
         output.add(input.get(result.get(i)));
         outputEdges.add(Pair.getInstance(input.get(result.get((i - 1 + result.size()) % result.size())), input.get(result.get(i))));
      }
   }

   public static boolean isCounterClockwise(final Tuple2d p0, final Tuple2d p1, final Tuple2d p2) {
      return ((p1.y - p0.y) * (p2.x - p1.x) - (p1.x - p0.x) * (p2.y - p1.y)) < 0;
   }
}
