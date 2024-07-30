/**
 * 
 */
package dsg.rounda.services.vertigo;

import java.util.Map;

import dsg.rounda.services.membership.MembershipTuple;

/**
 * Wrapper of a method that gets called when the result
 * of a Vertigo session is available
 */
public interface ResultHandler {

    /**
     * Called when the result of a query is available
     * 
     * @param success whether the query was successful
     * @param acks the set of identifiers of confirmed receivers
     * @param membership the membership tuple used to determine success
     */
    void result(boolean success, Map<Integer,MicroResponse> responses, MembershipTuple membership);
}
