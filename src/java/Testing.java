import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.Vector2d;


public class Testing {
   public static void main(final String[] args) {
      final Tuple2d amin = new Tuple2d(304, 183); // common
      final Tuple2d amax = new Tuple2d(401, 222);
      final Tuple2d bmin = new Tuple2d(304, 183); // common
      final Tuple2d bmax = new Tuple2d(322, 318);

      final Tuple2d cmin = new Tuple2d(401, 222);
      final Tuple2d cmax = new Tuple2d(511, 205); // common
      final Tuple2d dmin = new Tuple2d(322, 318);
      final Tuple2d dmax = new Tuple2d(511, 205); // common

      final Vector2d a = new Vector2d(amax.x, amax.y);
      a.subtract(amin);
      a.normalize();

      final Vector2d b = new Vector2d(bmax.x, bmax.y);
      b.subtract(bmin);
      b.normalize();

      final Vector2d c = new Vector2d(cmin.x, cmin.y);
      c.subtract(cmax);
      c.normalize();

      final Vector2d d = new Vector2d(dmin.x, dmin.y);
      d.subtract(dmax);
      d.normalize();

      final double ab = Math.toDegrees(a.angle(b));
      final double cd = Math.toDegrees(c.angle(d));

      System.out.println(ab + " + " + cd + " = " + (ab + cd));
   }
}
