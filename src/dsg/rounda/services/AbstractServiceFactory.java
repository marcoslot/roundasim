/**
 * 
 */
package dsg.rounda.services;


/**
 * Base class for service factories that automatically registers
 * the factory
 */
public abstract class AbstractServiceFactory implements ServiceFactory {

    /**
     * Register this instace as the service factory
     * for class cls.
     */
    public AbstractServiceFactory(Class<?> cls) {
        ServiceFactory.CHOOSER.add(cls, this);
    }


}
