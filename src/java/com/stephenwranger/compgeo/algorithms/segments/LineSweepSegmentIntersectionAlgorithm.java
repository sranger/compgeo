package com.stephenwranger.compgeo.algorithms.segments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.segments.SegmentEvent.EventType;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.renderables.LineSegment;

public class LineSweepSegmentIntersectionAlgorithm implements Algorithm<LineSegment, Tuple2d> {

   private final List<SegmentEvent>       eventQueue               = new LinkedList<SegmentEvent>();
   private final List<LineSegment>        status                   = new LinkedList<LineSegment>();
   private double                         currentX                 = -Double.MAX_VALUE;

   private final Comparator<SegmentEvent> SEGMENT_EVENT_COMPARATOR = new Comparator<SegmentEvent>() {
      @Override
      public int compare(final SegmentEvent o1, final SegmentEvent o2) {
         final Tuple2d v1 = o1.getVertex();
         final Tuple2d v2 = o2.getVertex();
         return v1.x < v2.x ? -1 : v1.x == v2.x ? 0 : 1;
      }
   };

   public LineSweepSegmentIntersectionAlgorithm() {

   }

   @Override
   public boolean compute(final List<LineSegment> input, final List<Tuple2d> output, final long timeout) {
      final long startTime = System.nanoTime();
      this.eventQueue.clear();
      this.status.clear();
      this.currentX = -Double.MAX_VALUE;

      for (final LineSegment s : input) {
         if ((System.nanoTime() - startTime) / 1000000l > timeout) {
            return false;
         }
         this.insertEvent(new SegmentEvent(EventType.START_VERTEX, s.min, s));
         this.insertEvent(new SegmentEvent(EventType.END_VERTEX, s.max, s));
      }

      SegmentEvent currentEvent, tempEvent;
      LineSegment above, currentSegment, below;

      while (!this.eventQueue.isEmpty()) {
         if ((System.nanoTime() - startTime) / 1000000l > timeout) {
            return false;
         }

         currentEvent = this.getNextEvent();
         this.currentX = currentEvent.vertex.x;

         switch (currentEvent.type) {
            case START_VERTEX:
               currentSegment = currentEvent.getSegment(0);
               this.insertStatusLine(currentSegment);

               above = this.getStatusAbove(currentSegment);
               below = this.getStatusBelow(currentSegment);
               tempEvent = this.getEvent(above, below);

               if (tempEvent != null) {
                  this.deleteEvent(tempEvent);
               }

               this.checkForIntersection(above, currentSegment);
               this.checkForIntersection(currentSegment, below);
               break;
            case END_VERTEX:
               currentSegment = currentEvent.getSegment(0);
               above = this.getStatusAbove(currentSegment);
               below = this.getStatusBelow(currentSegment);

               this.deleteStatusLine(currentSegment);
               this.checkForIntersection(above, below);
               break;
            case INTERSECTION_VERTEX:
               if (output != null) {
                  output.add(currentEvent.vertex);
               }
               above = currentEvent.getSegment(0);
               below = currentEvent.getSegment(1);

               if (above.min.y < below.min.y) {
                  above = currentEvent.getSegment(1);
                  below = currentEvent.getSegment(0);
               }

               this.deleteEvent(this.getEvent(above, this.getStatusAbove(above)));
               this.deleteEvent(this.getEvent(below, this.getStatusAbove(below)));

               this.swapStatus(above, below);

               this.checkForIntersection(below, this.getStatusAbove(below));
               this.checkForIntersection(above, this.getStatusBelow(above));
               break;
         }
      }

      return true;
   }

   private void checkForIntersection(final LineSegment s1, final LineSegment s2) {
      if (s1 != null && s2 != null) {
         final Tuple2d intersection = s1.intersect(s2);

         if (intersection != null) {
            this.insertEvent(new SegmentEvent(EventType.INTERSECTION_VERTEX, intersection, s1, s2));
         }
      }
   }

   private void insertStatusLine(final LineSegment segment) {
      int index = this.status.size();
      double y;

      for (int i = 0; i < this.status.size(); i++) {
         y = this.status.get(i).getIntersection(segment.min.x);

         if (segment.min.y > y) {
            index = i;
            break;
         }
      }

      this.status.add(index, segment);
   }

   private void deleteStatusLine(final LineSegment segment) {
      this.status.remove(segment);
   }

   private void swapStatus(final LineSegment s1, final LineSegment s2) {
      final int index1 = this.status.indexOf(s1);
      final int index2 = this.status.indexOf(s2);

      if (index1 >= 0 && index2 >= 0) {
         this.status.set(index1, s2);
         this.status.set(index2, s1);
      }
   }

   private LineSegment getStatusAbove(final LineSegment segment) {
      if (segment == null) {
         return null;
      }

      final int index = this.status.indexOf(segment);

      if (index > 0) {
         return this.status.get(index - 1);
      } else {
         return null;
      }
   }

   private LineSegment getStatusBelow(final LineSegment segment) {
      if (segment == null) {
         return null;
      }

      final int index = this.status.indexOf(segment);

      if (index >= 0 && index < this.status.size() - 1) {
         return this.status.get(index + 1);
      } else {
         return null;
      }
   }

   private void insertEvent(final SegmentEvent event) {
      if (event != null && event.vertex.x > this.currentX) {
         this.eventQueue.add(event);
         Collections.sort(this.eventQueue, this.SEGMENT_EVENT_COMPARATOR);
      }
   }

   private SegmentEvent getEvent(final LineSegment... segments) {
      for (final SegmentEvent event : this.eventQueue) {
         if (event.containsAll(segments)) {
            return event;
         }
      }

      return null;
   }

   private SegmentEvent getNextEvent() {
      return this.eventQueue.remove(0);
   }

   private void deleteEvent(final SegmentEvent event) {
      if (event != null) {
         this.eventQueue.remove(event);
      }
   }

   public static void main(final String[] args) {
      final LineSweepSegmentIntersectionAlgorithm alg = new LineSweepSegmentIntersectionAlgorithm();
      final List<LineSegment> input = new ArrayList<LineSegment>();
      final List<Tuple2d> output = new ArrayList<Tuple2d>();

      input.add(new LineSegment(new Tuple2d(0, 0), new Tuple2d(1, 1)));
      input.add(new LineSegment(new Tuple2d(1, 0), new Tuple2d(0, 1)));
      input.add(new LineSegment(new Tuple2d(0, 0.5), new Tuple2d(1, 0.5)));
      input.add(new LineSegment(new Tuple2d(0, 0.4), new Tuple2d(1, 0.6)));
      input.add(new LineSegment(new Tuple2d(0, 0.3), new Tuple2d(1, 0.7)));
      input.add(new LineSegment(new Tuple2d(0, 0.2), new Tuple2d(1, 0.8)));
      input.add(new LineSegment(new Tuple2d(0, 0.1), new Tuple2d(1, 0.9)));

      alg.compute(input, output, Long.MAX_VALUE);
      System.out.println("output: " + output.size());
   }
}
