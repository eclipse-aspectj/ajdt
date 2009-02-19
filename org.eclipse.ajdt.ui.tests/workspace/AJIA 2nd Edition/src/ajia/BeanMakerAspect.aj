package ajia;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public aspect BeanMakerAspect {
    private PropertyChangeSupport BeanSupport.propertyChangeSupport;
    
    public void BeanSupport.addPropertyChangeListener(
            PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }            

    public void BeanSupport.removePropertyChangeListener(
            PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }            

    pointcut beanCreation(BeanSupport bean) 
        : execution(BeanSupport+.new()) && this(bean);
        
    pointcut beanPropertyChange(BeanSupport bean, Object newValue) 
        : execution(* BeanSupport+.set*(..))
          && args(newValue) && this(bean);
    
    after(BeanSupport bean) returning : beanCreation(bean) {
        bean.propertyChangeSupport = new PropertyChangeSupport(bean);
    }
}