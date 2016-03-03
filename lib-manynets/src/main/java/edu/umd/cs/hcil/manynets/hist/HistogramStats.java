/*
 *  This file is part of ManyNets.
 *
 *  ManyNets is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  ManyNets is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with ManyNets.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  ManyNets was created at the Human Computer Interaction Lab, 
 *  University of Maryland at College Park. See the README file for details
 */

package edu.umd.cs.hcil.manynets.hist;

import java.util.Iterator;

/**
 * Utility class -- all methods are static
 * @author Awalin Shopan
 */
public class HistogramStats {

    public interface DistanceMetric<T> {
        public double distance(T a, T b);
    }

    public interface HistogramMetric extends DistanceMetric<DefaultHistogramModel> {
    }


    public static double getDelArea(DefaultHistogramModel a,  DefaultHistogramModel b){
        double delArea = 0;

        // use floating point to derive floats later, avoids lots of casting
        double n = a.count(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        double nRef = b.count(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        Iterator<Double> vi = a.getData().keySet().iterator();
        Iterator<Double> vir = b.getData().keySet().iterator();

        double cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
        double cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
        int count = 0;
        int countr = 0;
        double x0 = 0 , x1 =0 ;
        x0=Math.min(cv, cvr);

        for (; vi.hasNext() || vir.hasNext();) {
            if (cv < cvr) {
                count += ((DefaultHistogramModel.DataPoint)a.getData().get(cv)).count;
                x1 = cv ;
                cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
            } else if (cvr < cv) {
                x1 = cvr ;
                countr += ((DefaultHistogramModel.DataPoint)b.getData().get(cvr)).count;
                cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
            } else {
                count += ((DefaultHistogramModel.DataPoint)a.getData().get(cv)).count;
                cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
                countr += ((DefaultHistogramModel.DataPoint)b.getData().get(cvr)).count;
                cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
            }

            double probabilityRef = countr / nRef;  //F'(x)
            double probability = count / n;
            double d = probability - probabilityRef;//after the jump
            delArea += Math.abs((x1-x0)*d);
            x0=x1;
        }

//        System.err.println(" area distance="+delArea);

        if(Double.isNaN(delArea)){
            return 0;
        }
        return delArea;

    }
 
     public static class AreaDistanceMetric implements HistogramMetric {

        @Override
        public double distance( DefaultHistogramModel a,  DefaultHistogramModel b) {
          return getDelArea(a, b);
        }
    }

    public static class KSDistanceMetric implements HistogramMetric {

        @Override
        public  double distance(DefaultHistogramModel a, DefaultHistogramModel b) {
             double compKSstat = Double.NEGATIVE_INFINITY;

        // use floating point to derive floats later, avoids lots of casting
        float n = a.count(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);//total points in distribution
        float nRef = b.count(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        Iterator<Double> vi = a.getData().keySet().iterator();
        Iterator<Double> vir = b.getData().keySet().iterator();

        double cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
        double cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
        int count = 0;
        int countr = 0;
        for (; vi.hasNext() || vir.hasNext();) {
            if (cv < cvr) {
                count += ((DefaultHistogramModel.DataPoint)a.getData().get(cv)).count;
                cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
            } else if (cvr < cv) {
                countr += ((DefaultHistogramModel.DataPoint)b.getData().get(cvr)).count;
                cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
            } else {
                count += ((DefaultHistogramModel.DataPoint)a.getData().get(cv)).count;
                cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
                countr += ((DefaultHistogramModel.DataPoint)b.getData().get(cvr)).count;
                cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
            }
//       System.err.print("value "+ xValue + " count = " +count( min, xValue)+" ,total "+ n);

            double probabilityRef = countr / nRef;  //F'(x)
            double probability = count / n;
            double d = Math.abs(probability - probabilityRef);//after the jump
            compKSstat = Math.max(compKSstat, d);
//      System.err.println(" post " + post +" KS " + compKSstat);
        }

//        System.err.println("---->>compare KS Stat : " + compKSstat);

        if( Double.isNaN(compKSstat)){
           return 0;
                }
        return compKSstat;
      }
        

    }

     public static class EuclideanDistanceMetric implements HistogramMetric {

        @Override
        public double distance( DefaultHistogramModel a,  DefaultHistogramModel b ) {
             double distance = 0;

        Iterator<Double> vi  = a.getData().keySet().iterator();
        Iterator<Double> vir = b.getData().keySet().iterator();

//        System.err.print(" distance metric:: n " + n );

        double cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
        double cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
        double x=0;

        int count = 0;
        int countr = 0;

        // TODO : map x-values, cv to number , if it is already not a number
        // see if any point with this value exists in the H

         for (; vi.hasNext() || vir.hasNext();) {
            if (cv < cvr) {
                cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
                x=cv;
            } else if (cvr < cv) {
                cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
                x=cvr;
            } else {
                x=cv;
                cv = vi.hasNext() ? vi.next() : Double.POSITIVE_INFINITY;
                cvr = vir.hasNext() ? vir.next() : Double.POSITIVE_INFINITY;
            }
             if (a.getData().containsKey(x)) {
                     count = ((DefaultHistogramModel.DataPoint)a.getData().get(x)).count;
                 } else {
                     count = 0;
                 }
             if (b.getData().containsKey(x)) {
                     countr = ((DefaultHistogramModel.DataPoint)b.getData().get(x)).count;
                 } else {
                     countr = 0;
                 }
            distance += Math.pow(count-countr, 2 );
        }
        distance = Math.sqrt(distance);
        if(Double.isNaN(distance)){
            System.err.println(" distance Euclid= " + distance );
            return 0;
        }
       if( Double.isNaN(distance)){
            return 0;
        }
        return distance;
        }
    }

     public static class MDPAMetric implements HistogramMetric {

        int bins ;

        public MDPAMetric(){
            bins = 100;
        }
        public MDPAMetric(int mBin){
            bins = mBin;
        }

        @Override
        public double distance(DefaultHistogramModel a,  DefaultHistogramModel b) {
         int prefixSum = 0;
         double hdist = 0 ;
         double min = Math.min( a.getMin(), b.getMin()) ;
         double binSize = ( Math.max( a.getMax(), b.getMax()) - min  )/bins ;

         for(int i=0; i< bins; i++){
             prefixSum += a.count(min, min+binSize) - b.count(min, min+binSize);
             hdist += Math.abs(prefixSum);
         }
      if( Double.isNaN(hdist)){
            return 0;
        }
      return hdist;
        }
    }

     public static class AbsScalarDistanceMetric implements DistanceMetric<Double> {
        @Override
         public double distance(Double a, Double b) {
             return Math.abs(a - b);
         }
     }


     public static class KroneckerScalarDistanceMetric implements DistanceMetric<Double> {
        @Override
         public double distance(Double a, Double b) {
             return (a == b) ? 0 : 1;
         }
     }
}

