//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <Mobility.java Sun 2005/03/13 11:02:59 barr rimbase.rimonbarr.com>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.field;

import jist.swans.misc.Location;

/** 
 * Interface of all mobility models.
 *
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
 * @version $Id: Mobility.java,v 1.22 2005-03-13 16:11:54 barr Exp $
 * @since SWANS1.0
 */
public interface Mobility
{

  /**
   * Initiate mobility; initialize mobility data structures.
   *
   * @param f field entity
   * @param id node identifier
   * @param loc node location
   * @return mobility information object
   */
  MobilityInfo init(FieldInterface f, Integer id, Location loc);

  /**
   * Schedule next movement. This method will again be called after every
   * movement on the field.
   *
   * @param f field entity
   * @param id radio identifier
   * @param loc destination of move
   * @param info mobility information object
   */
  void next(FieldInterface f, Integer id, Location loc, MobilityInfo info);


  //////////////////////////////////////////////////
  // mobility information
  //

  /**
   * Interface of algorithm-specific mobility information objects.
   *
   * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
   * @since SWANS1.0
   */

  public static interface MobilityInfo
  {
    /** The null MobilityInfo object. */
    MobilityInfo NULL = new MobilityInfo()
    {
    };
  }


  //////////////////////////////////////////////////
  // static mobility model
  //


  /**
   * Static (noop) mobility model.
   *
   * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
   * @since SWANS1.0
   */

  public static class Static implements Mobility
  {
    //////////////////////////////////////////////////
    // Mobility interface
    //

    /** {@inheritDoc} */
    public MobilityInfo init(FieldInterface f, Integer id, Location loc)
    {
      return null;
    }

    /** {@inheritDoc} */
    public void next(FieldInterface f, Integer id, Location loc, MobilityInfo info)
    {
    }

  } // class Static



} // interface Mobility

// todo: other mobility models
