package com.stephenwranger.compgeo.algorithms.convexhull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.AlgorithmUtils;
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
      double minX = Double.MAX_VALUE;
      double maxX = -Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double maxY = -Double.MAX_VALUE;

      int lowest = 0;

      for (int i = 1; i < input.size(); i++) {
         if (input.get(i).x < input.get(lowest).x) {// && input.get(i).y < input.get(lowest).y) {
            lowest = i;
         }
      }

      int p = lowest;
      int q;

      do {
         q = (p + 1) % count;

         for (int i = 0; i < count; i++) {
            if (ConvexHullJarvisMarch.isCounterClockwise(input.get(p), input.get(i), input.get(q))) {
               q = i;
            }
         }

         result.add(q);
         p = q;
      } while (p != lowest);

      Tuple2d point;

      for (int i = 0; i < result.size(); i++) {
         point = input.get(result.get(i));
         minX = Math.min(minX, point.x);
         maxX = Math.max(maxX, point.x);
         minY = Math.min(minY, point.y);
         maxY = Math.max(maxY, point.y);
         output.add(input.get(result.get(i)));
         outputEdges.add(Pair.getInstance(input.get(result.get((i - 1 + result.size()) % result.size())), input.get(result.get(i))));
      }

      final double centerX = (maxX - minX) / 2.0 + minX;
      final double centerY = (maxY - minY) / 2.0 + minY;
      Collections.sort(output, AlgorithmUtils.getComparator(new Tuple2d(centerX, centerY)));
   }

   public static boolean isCounterClockwise(final Tuple2d p, final Tuple2d q, final Tuple2d r) {
      // return ((p1.y - p0.y) * (p2.x - p1.x) - (p1.x - p0.x) * (p2.y - p1.y)) < 0;
      double val = (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y);

      return (val < 0); // true if counterclock wise
   }
}
