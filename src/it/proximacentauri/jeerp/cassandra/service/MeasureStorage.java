/**
 * 
 */
package it.proximacentauri.jeerp.cassandra.service;

import it.polito.elite.domotics.model.notification.ParametricNotification;
import it.proximacentauri.jeerp.dao.cassandra.CassandraDaoImpl;
import it.proximacentauri.jeerp.domain.Survey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Dictionary;

import javax.measure.Measure;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;

/**
 * @author bonino
 * 
 */
@Component
public class MeasureStorage implements EventHandler, ManagedService {

	private LogService log = null;
	private CassandraDaoImpl dao = null;

	public void activate() {
		log.log(LogService.LOG_INFO, "[MeasureStorage]: Activate of cassandra");
	}

	public void deactivate() {
		log.log(LogService.LOG_INFO,
				"[MeasureStorage]: Deactivate of cassandra");
		dao = null;
	}

	public void bind(LogService log) {
		this.log = log;
	}

	public void unbind(LogService log) {
		if (this.log == log) {
			this.log = null;
		}
	}

	@Override
	public void handleEvent(Event event) {

		log.log(LogService.LOG_DEBUG,
				"[MeasureStorage]: Rcv measure " + event.getTopic());

		// handle Notification
		Object eventContent = event.getProperty(EventConstants.EVENT);

		if (dao != null && eventContent instanceof ParametricNotification) {
			// store the received notification
			ParametricNotification receivedNotification = (ParametricNotification) eventContent;

			// the device uri
			String deviceURI = receivedNotification.getDeviceUri();

			// the notification measure
			Measure<?, ?> value = null;

			// get all the notification methods
			Method[] notificationMethods = receivedNotification.getClass()
					.getDeclaredMethods();

			// extract the measure value...
			for (Method currentMethod : notificationMethods) {
				if (currentMethod.getReturnType().isAssignableFrom(
						Measure.class)) {
					try {
						// read the value
						value = (Measure<?, ?>) currentMethod.invoke(
								receivedNotification, new Object[] {});
						break;
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			// debug
			log.log(LogService.LOG_DEBUG,
					"[MeasureStorage]: notification from " + deviceURI
							+ " value " + value);

			Survey survey = new Survey();
			survey.setName(deviceURI);
			survey.setValue(value);
			survey.setTimestamp(new Date());
			dao.insert(survey);
		}

	}

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {

		String host = null;
		String keyspace = null;
		try {
			host = (String) properties.get(Constants.HOST);
			keyspace = (String) properties.get(Constants.KEYSPACE);
		} catch (Exception e) {
			log.log(LogService.LOG_ERROR, "Missing configuration param "
					+ Constants.HOST + " or " + Constants.KEYSPACE);
			return;
		}

		final Cluster cluster = HFactory.getOrCreateCluster("cluster", host);

		dao = new CassandraDaoImpl(cluster);
		dao.setEnableHoursTimeline(true);
		dao.setKeyspace(keyspace);
	}
}
