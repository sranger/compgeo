package com.stephenwranger.compgeo.algorithms.delaunay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Triangle2D;
import com.stephenwranger.graphics.renderables.Circle;

public class DelaunayTriangulation {
   private final List<Tuple2d>    vertices  = new ArrayList<Tuple2d>();
   private final Map<LineSegment, Set<Triangle2D>> edgesToTrianglesMap = new HashMap<LineSegment, Set<Triangle2D>>();
   private final Set<Triangle2D> triangles = new HashSet<Triangle2D>();

   public DelaunayTriangulation() {

   }

   public void addVertex(final Tuple2d vertex) {
      this.vertices.add(vertex);

      if (this.vertices.size() == 3) {
         this.triangles.add(new Triangle2D(this.vertices.get(0), this.vertices.get(1), this.vertices.get(2)));
      } else if (this.vertices.size() > 3) {
         final List<Triangle2D> needsReplacing = new ArrayList<Triangle2D>();
         Circle c = null;

         for (final Triangle2D t : this.triangles) {
            c = t.getCircumscribedCircle();

            if (c.contains(vertex)) {
               needsReplacing.add(t);
            }
         }

         final List<LineSegment> edges = new ArrayList<LineSegment>();
         final List<LineSegment> output = new ArrayList<LineSegment>();
         final Set<Triangle2D> newTriangles = new HashSet<Triangle2D>();

         for (final Triangle2D tri : needsReplacing) {
            edges.addAll(Arrays.asList(tri.getLineSegments()));
            this.removeTriangle(tri);
         }

         if (!edges.isEmpty()) {
            getUniqueEdges(edges, output);
            
            Triangle2D triangle;

            for (final LineSegment segment : output) {
               triangle = new Triangle2D(segment.min, segment.max, vertex);
               newTriangles.add(triangle);
               addTriangle(triangle);
            }
         }
         
         if(!newTriangles.isEmpty()) {
            verifyDelaunay(newTriangles);
         }
      }
   }

   public Iterable<Triangle2D> getTriangles() {
      return Collections.unmodifiableCollection(this.triangles);
   }

   public void clear() {
      this.triangles.clear();
      this.vertices.clear();
   }
   
   private void removeTriangle(final Triangle2D triangle) {
      this.triangles.remove(triangle);
      
      Set<Triangle2D> set;
      
      for(final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);
         
         if(set != null) {
            set.remove(triangle);
         }
      }
   }
   
   private void addTriangle(final Triangle2D triangle) {
      this.triangles.add(triangle);
      
      Set<Triangle2D> set;
      
      for(final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);
         
         if(set == null) {
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
      
      for(final Triangle2D triangle : toCheck) {
         neighbors = this.getNeighbors(triangle);
         
         for(final Triangle2D neighbor : neighbors) {
            if(hasFlipped) {
               toCheckNext.add(neighbor);
            } else if((newTriangles = fixDelaunay(triangle, neighbor)) != null) {
               System.out.println("flipping");
               this.removeTriangle(triangle);
               this.removeTriangle(neighbor);

               this.addTriangle(newTriangles.left);
               this.addTriangle(newTriangles.right);

               toCheckNext.add(newTriangles.left);
               toCheckNext.add(newTriangles.right);
               
               hasFlipped = true;
            }
         }
      }
      
      if(!toCheckNext.isEmpty()) {
         this.verifyDelaunay(toCheckNext);
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

      final double angle = Math.toDegrees(Math.abs(corner1.left.getAngle(corner1.right)) + Math.abs(corner2.left.getAngle(corner2.right)));
      System.out.println("angle: " + angle);
      
      if(angle > 180.0) {
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
   
   private static void getUniqueEdges(final List<LineSegment> input, final List<LineSegment> output) {
      output.clear();
      
      for(final LineSegment segment : input) {
         if(!output.remove(segment)) {
            output.add(segment);
         }
      }
   }
}
