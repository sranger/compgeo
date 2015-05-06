package com.stephenwranger.compgeo.algorithms.delaunay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Triangle2D;
import com.stephenwranger.graphics.renderables.Circle;
import com.stephenwranger.graphics.utils.Iterative;
import com.stephenwranger.graphics.utils.IterativeListener;

public class DelaunayTriangulation implements Iterative {
   private final List<Tuple2d> vertices = new ArrayList<Tuple2d>();
   private final Map<LineSegment, Set<Triangle2D>> edgesToTrianglesMap = new HashMap<LineSegment, Set<Triangle2D>>();
   private final Set<Triangle2D> triangles = new CopyOnWriteArraySet<Triangle2D>();
   private final Set<IterativeListener> listeners = new HashSet<IterativeListener>();

   private final Triangle2D boundingTriangle;
   
   private boolean isBusy = false;
   private boolean isWaiting = false;
   private int continueCtr = 0;

   public DelaunayTriangulation(final Triangle2D boundingTriangle) {
      this.boundingTriangle = boundingTriangle;

      final Tuple2d[] corners = this.boundingTriangle.getCorners();
      this.addVertex(corners[0], false);
      this.addVertex(corners[1], false);
      this.addVertex(corners[2], false);
   }

   public void addVertex(final Tuple2d vertex) {
      this.isBusy = true;
      new Thread() {
         @Override
         public void run() {
            addVertex(vertex, true);
         }
      }.start();
   }
   
   public void addVertex(final Tuple2d vertex, final boolean sendIterativeListenerNotifications) {
      synchronized (DelaunayTriangulation.this) {
         DelaunayTriangulation.this.vertices.add(vertex);

         if(sendIterativeListenerNotifications)
            DelaunayTriangulation.this.notifyListeners("Adding New Vertex: " + vertex, vertex);

         if (DelaunayTriangulation.this.vertices.size() == 3) {
            DelaunayTriangulation.this.triangles.add(new Triangle2D(DelaunayTriangulation.this.vertices.get(0), DelaunayTriangulation.this.vertices.get(1), DelaunayTriangulation.this.vertices.get(2)));
         } else if (DelaunayTriangulation.this.vertices.size() > 3) {
            final List<Triangle2D> needsReplacing = new ArrayList<Triangle2D>();
            Circle c = null;

            if(sendIterativeListenerNotifications)
               DelaunayTriangulation.this.notifyListeners("Checking New Point against existing Triangle's Circumscribed Circles.");
            for (final Triangle2D t : DelaunayTriangulation.this.triangles) {
               c = t.getCircumscribedCircle();
               if(sendIterativeListenerNotifications)
                  DelaunayTriangulation.this.notifyListeners("\tChecking circumcircle.", t, c);

               if (c.contains(vertex)) {
                  needsReplacing.add(t);
               }
            }

            if(sendIterativeListenerNotifications)
               DelaunayTriangulation.this.notifyListeners("Found " + needsReplacing.size()
                  + " triangles that need updating; removing them from the triangulation.", needsReplacing.toArray());

            final List<LineSegment> edges = new ArrayList<LineSegment>();
            final List<LineSegment> output = new ArrayList<LineSegment>();
            final Set<Triangle2D> newTriangles = new HashSet<Triangle2D>();

            for (final Triangle2D tri : needsReplacing) {
               edges.addAll(Arrays.asList(tri.getLineSegments()));
               DelaunayTriangulation.this.removeTriangle(tri);
            }

            if(sendIterativeListenerNotifications)
               DelaunayTriangulation.this.notifyListeners("Removing any non-unique edges from removed triangles.");

            if (!edges.isEmpty()) {
               DelaunayTriangulation.getUniqueEdges(edges, output);
               if(sendIterativeListenerNotifications) {
                  edges.removeAll(output);
                  DelaunayTriangulation.this.notifyListeners("Number of unique edges found: " + output.size() + ". Creating new Triangles from unique edge list and inserted vertex.", edges.toArray());
               }

               Triangle2D triangle;

               for (final LineSegment segment : output) {
                  triangle = new Triangle2D(segment.min, segment.max, vertex);
                  newTriangles.add(triangle);
                  DelaunayTriangulation.this.addTriangle(triangle);
                  if(sendIterativeListenerNotifications)
                     DelaunayTriangulation.this.notifyListeners("Triangle added: " + triangle, triangle);
               }
            } else {
               if(sendIterativeListenerNotifications)
                  DelaunayTriangulation.this.notifyListeners("No unique edges; something probably went wrong.");
            }

            if (!newTriangles.isEmpty()) {
               if(sendIterativeListenerNotifications)
                  DelaunayTriangulation.this.notifyListeners("Total Triangles added: " + newTriangles.size()
                     + "; verifying Delaunay properties.", newTriangles.toArray());
               DelaunayTriangulation.this.verifyDelaunay(newTriangles);
            } else {
               if(sendIterativeListenerNotifications)
                  DelaunayTriangulation.this.notifyListeners("No new Triangles; something probably went wrong.");
            }
         }

         if(sendIterativeListenerNotifications)
            DelaunayTriangulation.this.notifyListeners("Addition of new Vertex complete.", vertex);
         DelaunayTriangulation.this.isBusy = false;
      }
   }

   public Iterable<Triangle2D> getTriangles() {
      return Collections.unmodifiableCollection(this.triangles);
   }

   public void clear() {
      this.triangles.clear();
      this.vertices.clear();
      this.edgesToTrianglesMap.clear();

      final Tuple2d[] corners = this.boundingTriangle.getCorners();
      this.addVertex(corners[0], false);
      this.addVertex(corners[1], false);
      this.addVertex(corners[2], false);
   }

   private void removeTriangle(final Triangle2D triangle) {
      this.triangles.remove(triangle);

      Set<Triangle2D> set;

      for (final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);

         if (set != null) {
            set.remove(triangle);
         }
      }
   }

   private void addTriangle(final Triangle2D triangle) {
      this.triangles.add(triangle);

      Set<Triangle2D> set;

      for (final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);

         if (set == null) {
            set = new HashSet<Triangle2D>();
            this.edgesToTrianglesMap.put(segment, set);
         }

         set.add(triangle);
      }
   }

   private void verifyDelaunay(final Set<Triangle2D> toCheck) {
      final Set<Triangle2D> toCheckNext = new HashSet<Triangle2D>();

      Set<Triangle2D> neighbors;
      Pair<Triangle2D, Triangle2D> newTriangles;
      boolean hasFlipped = false;

      for (final Triangle2D triangle : toCheck) {
         neighbors = this.getNeighbors(triangle);
         this.notifyListeners("Checking neighbors of " + triangle
               + " for invalid triangulation.\nThis occurs if the sum of the two internal angles opposite the shared"
               + " edge exceed 180 degrees. With our recursive iterative algorithm, it should rarely occur.", neighbors.toArray(), triangle);

         for (final Triangle2D neighbor : neighbors) {
            if (hasFlipped) {
               this.notifyListeners("Adding existing triangles to next verify loop: " + neighbor, neighbor);
               toCheckNext.add(neighbor);
            } else if ((newTriangles = this.fixDelaunay(triangle, neighbor)) != null) {
               this.notifyListeners("Removing invalid delaunay triangulation and flipping shared edge.", triangle, neighbor);
               this.removeTriangle(triangle);
               this.removeTriangle(neighbor);
               this.notifyListeners("Removed triangles: " + triangle + ", " + neighbor, triangle, neighbor);

               this.addTriangle(newTriangles.left);
               this.addTriangle(newTriangles.right);
               this.notifyListeners("Flipped additions: " + newTriangles.left + ", " + newTriangles.right, newTriangles.left, newTriangles.right);

               toCheckNext.add(newTriangles.left);
               toCheckNext.add(newTriangles.right);

               hasFlipped = true;
            }
         }
      }

      if (!toCheckNext.isEmpty()) {
         this.notifyListeners("Verifying delaunay with remainder of new triangles list along with all flipped triangles.", toCheckNext.toArray());
         this.verifyDelaunay(toCheckNext);
      } else {
         this.notifyListeners("Verification complete.");
      }
   }

   /**
    * Checks if the two given triangles form a valid delaunay triangulation and if not, flips their common edge.
    *
    * @param t1
    * @param t2
    * @return the new triangles or null if no changes were made
    */
   private Pair<Triangle2D, Triangle2D> fixDelaunay(final Triangle2D t1, final Triangle2D t2) {
      final LineSegment edge = t1.getCommonEdge(t2);
      final Pair<LineSegment, LineSegment> corner1 = t1.getOppositeEdges(edge);
      final Pair<LineSegment, LineSegment> corner2 = t2.getOppositeEdges(edge);

      final double angle = Math.toDegrees(Math.abs(corner1.left.getAngle(corner1.right))
            + Math.abs(corner2.left.getAngle(corner2.right)));

      if (angle > 180.0) {
         final Tuple2d commonVertex1 = corner1.left.getCommonVertex(corner1.right);
         final Tuple2d commonVertex2 = corner2.left.getCommonVertex(corner2.right);
         final Triangle2D newT1 = new Triangle2D(edge.min, commonVertex1, commonVertex2);
         final Triangle2D newT2 = new Triangle2D(edge.max, commonVertex1, commonVertex2);

         return Pair.getInstance(newT1, newT2);
      }

      return null;
   }

   private Set<Triangle2D> getNeighbors(final Triangle2D triangle) {
      final Set<Triangle2D> neighbors = new HashSet<Triangle2D>();

      for (final LineSegment segment : triangle.getLineSegments()) {
         neighbors.addAll(this.edgesToTrianglesMap.get(segment));
      }

      // TODO: do this better
      neighbors.remove(triangle);

      return neighbors;
   }

   private void waitForContinue() {
      while(this.isWaiting) {
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   @Override
   public void continueIteration() {
      this.continueCtr--;
      
      if(this.continueCtr == 0) {
         this.isWaiting = false;
      }
   }

   @Override
   public void addIterativeListener(IterativeListener listener) {
      this.listeners.add(listener);
   }

   @Override
   public void removeIterativeListener(IterativeListener listener) {
      this.listeners.remove(listener);
   }

   private void notifyListeners(final String message, final Object... payload) {
      if(!this.listeners.isEmpty()) {
         this.isWaiting = true;
         this.continueCtr = this.listeners.size();
         
         for (final IterativeListener listener : this.listeners) {
            listener.step(this, message, Arrays.asList(payload));
         }
   
         this.waitForContinue();
      }
   }

   private static void getUniqueEdges(final List<LineSegment> input, final List<LineSegment> output) {
      output.clear();

      for (final LineSegment segment : input) {
         if (!output.remove(segment)) {
            output.add(segment);
         }
      }
   }

   @Override
   public boolean isBusy() {
      return this.isBusy;
   }
}
