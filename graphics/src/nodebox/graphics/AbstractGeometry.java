package nodebox.graphics;

import java.util.Iterator;

/**
 *
 */
public abstract class AbstractGeometry extends AbstractTransformable implements IGeometry {

    //// Container operations ////

    /**
     * Extend the current geometry with the given list of points.
     *
     * @param points the points to add to the geometry.
     */
    public void extend(Iterator<Point> points) {
        while (points.hasNext()) {
            Point point = points.next();
            addPoint(point.clone());
        }
    }

    /**
     * Extend the current geometry with the given list of points.
     *
     * @param points the points to add to the geometry.
     */
    public void extend(Point[] points) {
        for (Point point : points) {
            addPoint(point.clone());
        }
    }

    //// Geometric operations ////

    /**
     * Make 100 new points along the contours of the existing path.
     * <p/>
     * Points are evenly distributed according to the length of each geometric object.
     *
     * @return a list of Points.
     */
    public Point[] makePoints() {
        return makePoints(DEFAULT_POINT_AMOUNT, false);
    }

    public Point[] makePoints(int amount) {
        return makePoints(amount, false);
    }

    /**
     * Calculate how far the points would be apart, given the specified amount and whether the geometry is closed.
     *
     * @param amount the amount of points
     * @param closed whether the geometry is closed
     * @return the delta value between each point
     */
    protected float pointDelta(int amount, boolean closed) {
        float delta = 1;
        if (closed) {
            if (amount > 0) {
                delta = 1f / amount;
            }
        } else {
            // The delta value is divided by amount - 1, because we also want the last point (t=1.0)
            // If I wouldn't use amount - 1, I fall one point short of the end.
            // E.g. if amount = 4, I want point at t 0.0, 0.33, 0.66 and 1.0,
            // if amount = 2, I want point at t 0.0 and t 1.0
            if (amount > 2) {
                delta = 1f / (amount - 1f);
            }
        }
        return delta;
    }

    public abstract IGeometry clone();

}
