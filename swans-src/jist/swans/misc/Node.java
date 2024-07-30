//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <Node.java Sun 2005/03/13 11:08:23 barr rimbase.rimonbarr.com>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.misc;

import jist.swans.mac.MacInterface;
import jist.swans.net.NetInterface;
import jist.swans.radio.RadioInterface;

// todo: finish this off, making the correct hookups
public class Node
{
  protected RadioInterface radio, radioEntity;
  protected NetInterface net, netEntity;
  protected MacInterface mac, macEntity;

  public Node()
  {
  }

  public void addApplication()
  {
    // todo: application
  }

  public void addNetwork(NetInterface net)
  {
    this.net = net;
    netEntity = net;//(NetInterface)JistAPI.proxy(net, NetInterface.class);
  }

  public void addMac(MacInterface mac)
  {
    this.mac = mac;
    macEntity = mac;//(MacInterface)JistAPI.proxy(mac, MacInterface.class);
  }

  public void addRadio(RadioInterface radio)
  {
    this.radio = radio;
    radioEntity = radio;//(RadioInterface)JistAPI.proxy(radio, RadioInterface.class);
  }

  public void addMobility()
  {
    // todo: mobility
  }

}
