//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <Util.java Tue 2004/04/06 11:47:00 barr pompom.cs.cornell.edu>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.misc;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import jist.swans.Constants;

/**
 * Miscellaneous utility methods.
 *
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
 * @version $Id: Util.java,v 1.18 2004-04-06 16:07:49 barr Exp $
 * @since SWANS1.0
 */

public final class Util
{

  /** An empty enumeration. */
  public static final Enumeration EMPTY_ENUMERATION = new Enumeration()
  {
    public boolean hasMoreElements()
    {
      return false;
    }
    public Object nextElement()
    {
      throw new NoSuchElementException();
    }
  };

  /**
   * Return number squared.
   *
   * @param x number to square
   * @return number squared
   */
  public static double square(double x)
  {
    return x*x;
  }

  /**
   * Return number squared.
   *
   * @param x number to square
   * @return number squared
   */
  public static int square(int x)
  {
    return x*x;
  }

  /**
   * Convert number to decibels.
   *
   * @param x number to convert
   * @return number in decibels
   */
  public static double toDB(double x)
  {
    return 10.0 * Math.log(x) / Constants.log10;
  }

  /**
   * Convert number from decibels.
   *
   * @param x number to convert
   * @return number on linear scale
   */
  public static double fromDB(double x)
  {
    return Math.pow(10.0, x / 10.0);
  }

  /**
   * Return current simulation time as string in seconds.
   *
   * @return string with current simulation time in seconds.
   */
  public static String timeSeconds()
  {
    //return (jist.runtime.JistAPI.getTime()/(float)Constants.SECOND)+" sec";
	  throw new Error("not implemented");
  }

  /**
   * Native logarithm function wrapper. Will use the regular Java Math.log
   * if the native function is not available.
   *
   * @param n number to log
   * @return log of given number
   */
  public static float log(float n)
  {
    return  (float)Math.log(n);
  }

  /**
   * Validate condition.
   *
   * @param cond condition to validate
   */
  public static void assertion(boolean cond)
  {
    if(!cond) throw new Error("assertion");
  }

  /**
   * Return whether a given Objects exists within an Object array.
   *
   * @param set an array of objects to test for membership
   * @param item object to test membership
   * @return whether given item exists in the given set
   */
  public static boolean contains(Object[] set, Object item)
  {
    int i=0;
    while (i<set.length)
    {
      if (item.equals(set[i]))
      {
        return true;
      }
      i++;
    }
    return false;
  }

  /**
   * Concatenate array of Strings separated by given delimeter.
   *
   * @param objs array of objects to stringify and concatenate
   * @param delim delimeter to insert between each pair of strings
   * @return delimited concatenation of strings
   */
  public static String stringJoin(Object[] objs, String delim)
  {
    StringBuffer sb = new StringBuffer();
    int i=0;
    while(i<objs.length-1)
    {
      sb.append(objs[i++]);
      sb.append(delim);
    }
    if(i<objs.length)
    {
      sb.append(objs[i]);
    }
    return sb.toString();
  }

  /**
   * Return array with all but first component.
   *
   * @param values array to copy all but first component
   * @return array with all but first component
   */
  public static Object rest(Object values)
  {
	    throw new Error("Not implemented");
  }

  /**
   * Return array with new component appended.
   *
   * @param values array to copy and append to
   * @param value component to append
   * @return array with new value appended
   */
  public static Object append(Object values, Object value)
  {
    throw new Error("Not implemented");
  }

  /**
   * Return random long between 0 (inclusive) and bound (exclusive).
   *
   * @param bound upper bound of range
   * @return random long between 0 (inclusive) and bound (exclusive)
   */
  public static long randomTime(long bound)
  {
    return (long)(Constants.random.nextDouble()*bound);
  }

  /**
   * Return status of a single bit within a byte of flags.
   *
   * @param flags byte of flags
   * @param mask mask of bit to read
   * @return status of masked bit
   */
  public static boolean getFlag(byte flags, byte mask)
  {
    return (flags & mask)!=0;
  }

  /**
   * Set status of a single bit within a byte of flags.
   *
   * @param flags byte of flags
   * @param mask mask of bit to be set
   * @param value new value for bit
   * @return new flags value with status of single bit set to value
   */
  public static byte setFlag(byte flags, byte mask, boolean value)
  {
    return (byte)(value ? flags | mask : flags & ~mask);
  }

  /**
   * Set a flag within a byte of flags.
   *
   * @param flags byte of flags
   * @param mask mask of bit to be set
   * @return new flags value with status of single bit set on
   */
  public static byte setFlag(byte flags, byte mask)
  {
    return setFlag(flags, mask, true);
  }

  /**
   * Clear a flag within a byte of flags.
   *
   * @param flags byte of flags
   * @param mask mask of bit to be set
   * @return new flags value with status of single bit cleared
   */
  public static byte clearFlag(byte flags, byte mask)
  {
    return setFlag(flags, mask, false);
  }

} // class: Util

