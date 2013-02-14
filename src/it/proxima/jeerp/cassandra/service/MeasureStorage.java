/**
 * 
 */
package it.proxima.jeerp.cassandra.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;

import javax.measure.Measure;

import it.polito.elite.domotics.model.notification.Notification;
import it.polito.elite.domotics.model.notification.ParametricNotification;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * @author bonino
 * 
 */
public class MeasureStorage implements EventHandler, ManagedService
{
	
	/**
	 * 
	 */
	public MeasureStorage()
	{
		System.err.println("[MeasureStorage]: Created...");
	}
	
	@Override
	public void handleEvent(Event event)
	{
		System.err.println("[MeasureStorage]: " + event.getTopic());
		
		// handle Notification
		Object eventContent = event.getProperty(EventConstants.EVENT);
		
		if (eventContent instanceof ParametricNotification)
		{
			// store the received notification
			ParametricNotification receivedNotification = (ParametricNotification) eventContent;
			
			// the device uri
			String deviceURI = receivedNotification.getDeviceUri();
			
			// the notification measure
			Measure<?, ?> value = null;
			
			// get all the notification methods
			Method[] notificationMethods = receivedNotification.getClass().getDeclaredMethods();
			
			// extract the measure value...
			for (Method currentMethod : notificationMethods)
			{
				if (currentMethod.getReturnType().isAssignableFrom(Measure.class))
				{
					try
					{
						// read the value
						value = (Measure<?, ?>) currentMethod.invoke(receivedNotification, new Object[]{});
						break;
					}
					catch (IllegalAccessException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IllegalArgumentException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (InvocationTargetException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			//debug
			System.err.println("[MeasureStorage]: notification from " + deviceURI+" value "+value);
			
		}
		
	}

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException
	{
		// TODO Auto-generated method stub
		
	}
}
