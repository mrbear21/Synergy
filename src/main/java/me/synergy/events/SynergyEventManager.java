package me.synergy.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import me.synergy.anotations.SynergyHandler;
import me.synergy.brains.Synergy;

// Event manager
public class SynergyEventManager {

    private static Set<Object> listeners = new HashSet<>();

    // Method to register listeners
    public void registerEvents(Object listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            Synergy.getLogger().info(listener.getClass().getSimpleName() + " event has been registered!");
        }
    }

    // Method to fire events
    public void fireEvent(SynergyEvent event) {
        for (Object listener : listeners) {
            Method[] methods = listener.getClass().getDeclaredMethods();
            for (Method method : methods) {
                // Check if method has SynergyHandler annotation
                if (method.isAnnotationPresent(SynergyHandler.class)) {
                    // Check if the method has a single parameter and the parameter is of the event's type
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == 1 && params[0].isAssignableFrom(event.getClass())) {
                        try {
                            // Invoke the method
                            method.invoke(listener, event);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    // Static method for other plugins to access the event manager
    public static SynergyEventManager getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private static class SingletonHolder {
        private static final SynergyEventManager INSTANCE = new SynergyEventManager();
    }
}
