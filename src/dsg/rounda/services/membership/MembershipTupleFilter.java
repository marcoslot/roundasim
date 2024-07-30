/**
 * 
 */
package dsg.rounda.services.membership;

/**
 * Interface to accept membership tuples
 */
public interface MembershipTupleFilter {
    boolean accept(MembershipTuple tuple);
}
