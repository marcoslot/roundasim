/**
 * 
 */
package dsg.rounda.swans;

import java.util.Iterator;
import java.util.LinkedList;

import jist.swans.mac.MacAddress;
import jist.swans.mac.MacInterface;
import jist.swans.net.NetAddress;
import jist.swans.net.NetInterface;
import jist.swans.net.NetMessage;
import dsg.rounda.model.Message;
import dsg.rounda.model.NetworkDeliveryInterface;
import dsg.rounda.model.NetworkTransmissionInterface;

/**
 * @author slotm
 *
 */
public class SwansNetInterface implements NetInterface, NetworkTransmissionInterface {

    private static final int MAX_QUEUE_SIZE = 10;
    
    final NetworkDeliveryInterface network;
    final LinkedList<Message> outgoing;
    
    MacInterface mac;
    boolean busy;

    /**
     * 
     */
    public SwansNetInterface(NetworkDeliveryInterface network) {
        this.network = network;
        this.outgoing = new LinkedList<Message>();
        this.busy = false;
    }
    
    /**
     * @param mac the mac to set
     */
    public void setMacInterface(MacInterface mac) {
        this.mac = mac;
    }

    /**
     * @see jist.swans.net.NetInterface#getAddress()
     */
    @Override
    public NetAddress getAddress() {
        return new NetAddress(network.getId());
    }

    @Override
    public void send(Message msg) {
        if(mac == null) {
            return;
        }
        
        if(msg.isBeacon()) {
            Iterator<Message> it = outgoing.iterator();
            
            while(it.hasNext()) {
                Message oldMsg = it.next();
                
                if(oldMsg.isBeacon()) {
                    it.remove();
                }
            }
        }
        
        if(outgoing.size() >= MAX_QUEUE_SIZE) {
            // Remove oldest element
            outgoing.removeFirst();
        }
        
        outgoing.add(0, msg);
        pump(0);
    }

    /**
     * @see jist.swans.net.NetInterface#pump(int)
     */
    @Override
    public void pump(int networkId) {
        if(mac == null) {
            return;
        }
        
        if(!outgoing.isEmpty() && !busy) {
            Message msg = outgoing.remove();
            send(mac, msg);
            busy = true;
        } else {
            busy = false;
        }
    }

    void send(MacInterface mac, Message msg) {
        MacAddress macDest;
        
        if(msg.getDestination() != Message.ANY_DESTINATION) {
            macDest = new MacAddress(msg.getDestination());
        } else {
            macDest = MacAddress.ANY;
        }
        
        mac.send(new SwansMessage(msg), macDest);
    }


    /**
     * Receive a message from the link layer.
     *
     * @param msg incoming network packet
     * @param lastHop link-level source of incoming packet
     * @param macId incoming interface
     * @param promiscuous whether network interface is in promisc. mode
     */
    @Override
    public void receive(jist.swans.misc.Message msg, MacAddress lastHop, byte macId, boolean promiscuous) {
        SwansMessage swm = (SwansMessage) msg;
        network.deliver(swm.getMessage());
    }

    /**
     * Send a message along given interface (usually from ROUTING).
     *
     * @param msg packet (usually from routing layer)
     * @param interfaceId interface along which to send packet
     * @param nextHop packet next hop address
     */
    @Override
    public void send(NetMessage.Ip msg, int interfaceId, MacAddress nextHop) {
        throw new Error("Not implemented");
    }

    /**
     * Route, if necessary, and send a message (from TRANSPORT).
     *
     * @param msg packet payload (usually from transport or routing layers)
     * @param dst packet destination address
     * @param protocol packet protocol identifier
     * @param priority packet priority
     * @param ttl packet time-to-live value
     */
    @Override
    public void send(jist.swans.misc.Message msg, NetAddress dst, short protocol, byte priority, byte ttl) {
        throw new Error("Not implemented");
    }

}
