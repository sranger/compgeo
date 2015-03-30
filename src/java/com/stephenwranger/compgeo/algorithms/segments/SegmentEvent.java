package com.stephenwranger.compgeo.algorithms.segments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;

public class SegmentEvent {
   public enum EventType {
      START_VERTEX, END_VERTEX, INTERSECTION_VERTEX;
   }

   public final EventType          type;
   public final Tuple2d            vertex;
   private final List<LineSegment> segments = new ArrayList<LineSegment>();

   public SegmentEvent(final EventType type, final Tuple2d vertex, final LineSegment... segments) {
      this.type = type;
      this.vertex = vertex;

      if (segments != null) {
         this.segments.addAll(Arrays.asList(segments));
      }
   }

   public Tuple2d getVertex() {
      return new Tuple2d(this.vertex.x, this.vertex.y);
   }

   public LineSegment getSegment(final int index) {
      return this.segments.get(index);
   }

   public boolean containsAll(final LineSegment... segments) {
      if (segments == null) {
         return this.segments.isEmpty();
      } else if (segments.length == this.segments.size()) {
         for (int i = 0; i < segments.length; i++) {
            if (!this.segments.contains(segments[i])) {
               return false;
            }
         }

         return true;
      }

      return false;
   }

   @Override
   public String toString() {
      return this.type + ", " + this.vertex + ", " + this.segments.size();
   }
}
