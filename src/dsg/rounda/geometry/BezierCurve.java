/**
 * 
 */
package dsg.rounda.geometry;

import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/*  Subroutine to generate a Bezier curve.
 Copyright (c) 2000 David F. Rogers. All rights reserved. */

public class BezierCurve {

    private static final GeometryFactory GEOM = new GeometryFactory();
    private static final Factorial FAC = new Factorial(32);

    float controlPoints[];

    public BezierCurve(Coordinate[] coordinates) {
        this.controlPoints = new float[coordinates.length*3+1];
        
        for(int i = 0, len = coordinates.length; i < len; i++) {
            controlPoints[i*3+1] = (float) coordinates[i].x;
            controlPoints[i*3+2] = (float) coordinates[i].y;
            controlPoints[i*3+3] = (float) (Double.isNaN(coordinates[i].z) ? 0.0 : coordinates[i].z);
        }
    }

    public BezierCurve(Collection<Coordinate> coordinates) {
        this.controlPoints = new float[coordinates.size()*3+1];
        
        int i = 0;
        for(Coordinate coordinate : coordinates) {
            controlPoints[i*3+1] = (float) coordinate.x;
            controlPoints[i*3+2] = (float) coordinate.y;
            controlPoints[i*3+3] = (float) (Double.isNaN(coordinate.z) ? 0.0 : coordinate.z);
            i += 1;
        }
    }

    public BezierCurve(float[] flatControlPoints) {
        this.controlPoints = flatControlPoints;
    }


    /* function to calculate the factorial function for Bernstein basis */

    float Ni(int n, int i) {
        double ni;
        ni = FAC.computeFactorial(n) / (FAC.computeFactorial(i) * FAC.computeFactorial(n - i));
        return (float) ni;
    }

    /* function to calculate the Bernstein basis */

    float computeBernsteinBasis(int n, int i, float t) {
        float basis;
        float ti; /* this is t^i */
        float tni; /* this is (1 - t)^i */

        /* handle the special cases to avoid domain problem with pow */

        if (t == 0. && i == 0)
            ti = 1.0f;
        else
            ti = (float) Math.pow(t, i);
        if (n == i && t == 1.)
            tni = 1.0f;
        else
            tni = (float) Math.pow((1 - t), (n - i));
        basis = Ni(n, i) * ti * tni; /* calculate Bernstein basis function */
        return basis;
    }
    
    public LineString getCurvePoints(int numPoints) {
        Coordinate[] coordinates = new Coordinate[numPoints];
        float[] flatPoints = getFlatCurvePoints(numPoints);
        
        for(int i = 0; i < numPoints; i++) {
            coordinates[i] = new Coordinate(
                    flatPoints[3*i+1],
                    flatPoints[3*i+2],
                    flatPoints[3*i+3]
            );
        }
        
        return GEOM.createLineString(coordinates);
    }

    public float[] getFlatCurvePoints(int cpts) {
        int i;
        int j;
        int i1;
        int icount;
        int jcount;
        int n;
        int numControlPoints;

        float step;
        float t;
        float p[];
        
        p = new float[3*cpts+1];

        /* calculate the points on the Bezier curve */

        numControlPoints = getNumControlPoints();
        icount = 0;
        t = 0;
        step = 1.0f / ((float) (cpts - 1));

        for (i1 = 1; i1 <= cpts; i1++) { /* main loop */

            if ((1.0 - t) < 5e-6)
                t = 1.0f;

            for (j = 1; j <= 3; j++) { /* generate a point on the curve */
                jcount = j;
                p[icount + j] = 0.f;
                for (i = 1; i <= numControlPoints; i++) { /* Do x,y,z components */
                    p[icount + j] = p[icount + j] + computeBernsteinBasis(numControlPoints - 1, i - 1, t)
                            * controlPoints[jcount];
                    jcount = jcount + 3;
                }
            }

            icount = icount + 3;
            t = t + step;
        }
        
        return p;
    }

    public int getNumControlPoints() {
        return (controlPoints.length-1)/3;
    }
    
    public Coordinate getControlPoint(int i) {
        return new Coordinate(
                controlPoints[i*3+1],
                controlPoints[i*3+2],
                controlPoints[i*3+3]
        );
    }

    public Coordinate[] getControlPoints() {
        Coordinate[] coordinates = new Coordinate[getNumControlPoints()];
        
        for(int i = 0; i < coordinates.length; i++) {
            coordinates[i] = getControlPoint(i);
        }
        
        return coordinates;
    }

}
