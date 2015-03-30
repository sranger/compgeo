package com.stephenwranger.compgeo.algorithms.delaunay;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.Triangle2D;

public class InteractiveDelaunay {
   public static void main(final String[] args) {
      final JFrame frame = new JFrame("Interactive Delaunay Triangulation");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      final List<Point> clicks = new ArrayList<Point>();
      final DelaunayTriangulation dt = new DelaunayTriangulation();

      final JPanel panel = new JPanel() {
         @Override
         public void paint(final Graphics g) {
            for (final Point p : clicks) {
               g.fillOval(p.x - 5, p.y - 5, 10, 10);
            }

            Tuple2d[] corners;
            for(final Triangle2D t : dt.getTriangles()) {
               corners = t.getCorners();

               g.drawPolygon(new int[] { (int) corners[0].x, (int) corners[1].x, (int) corners[2].x }, new int[] { (int) corners[0].y, (int) corners[1].y,
                     (int) corners[2].y }, 3);
            }
         }
      };
      panel.setPreferredSize(new Dimension(800, 500));
      panel.addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(final MouseEvent e) {
            clicks.add(e.getPoint());
            dt.addVertex(new Tuple2d(e.getX(), e.getY()));
            panel.repaint();
         }
      });

      final Container content = frame.getContentPane();
      content.add(panel);

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            frame.setLocation(200, 200);
            frame.pack();
            frame.setVisible(true);
         }
      });
   }
}
