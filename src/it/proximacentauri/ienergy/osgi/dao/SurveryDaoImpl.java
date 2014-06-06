package it.proximacentauri.ienergy.osgi.dao;

import it.proximacentauri.ienergy.osgi.domain.Survey;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.measure.unit.Unit;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurveryDaoImpl implements SurveyDao {

	final private Logger log = LoggerFactory.getLogger(SurveryDaoImpl.class);

	private String driver = "org.postgresql.Driver";
	private String url = "jdbc:postgresql://10.10.10.196:5432/ienergy";
	private String username = "ienergy";
	private String password = "ienergy";
	private int maxActive = 2;
	private String removePattern = null;

	private static final String DRAIN_REGISTRY = "SELECT id, drain FROM da_drain_descriptor";
	private static final String DRAIN_INSERT = "INSERT INTO da_drain_descriptor(drain, unit) VALUES (?, ?)";
	private static final String INSERT_MEASURE_RT = "INSERT INTO da_measure_rt(drain_id, value, \"time\")VALUES (?, ?, ?)";
	private static final String UPDATE_MEASURE_RT = "UPDATE da_measure_rt SET \"value\" = ?, \"time\" = ? WHERE drain_id = ?";

	private DataSource dataSource = null;

	private Map<String, Long> drainIdMap = null;

	public SurveryDaoImpl(String driver, String url, String user, String password, int maxActive, String removePattern) throws Exception {
		// save the connection parameter
		this.driver = driver;
		this.url = url;
		this.username = user;
		this.password = password;
		this.maxActive = maxActive;
		this.removePattern = removePattern;

		// hash map for drain id
		drainIdMap = Collections.synchronizedMap(new HashMap<String, Long>());
		setUp();
	}

	private void setUp() throws Exception {
		log.info("start setup connection pool for {}", driver);
		//
		// Load JDBC Driver class.
		//
		Class.forName(driver).newInstance();

		//
		// Creates an instance of GenericObjectPool that holds our
		// pool of connections object.
		//
		GenericObjectPool<DataSource> connectionPool = new GenericObjectPool<DataSource>();
		connectionPool.setMaxActive(maxActive);

		//
		// Creates a connection factory object which will be use by
		// the pool to create the connection object. We passes the
		// JDBC url info, username and password.
		//
		ConnectionFactory cf = new DriverManagerConnectionFactory(url, username, password);

		//
		// Creates a PoolableConnectionFactory that will wraps the
		// connection object created by the ConnectionFactory to add
		// object pooling functionality.
		//
		new PoolableConnectionFactory(cf, connectionPool, null, null, false, true);
		dataSource = new PoolingDataSource(connectionPool);
	}

	@Override
	public void insert(Survey obj) throws SQLException {
		log.info("insert new measure {}", obj);
		// drain name convertion
		String drainName = obj.getName();

		if (removePattern != null) {
			drainName = drainName.replaceAll(removePattern, "").toUpperCase();
			log.info("drain name convertion from " + obj.getName() + " to " + drainName);
			obj.setName(drainName);
		}

		// try to load the drain registry id
		long id = getDrainId(obj);

		Connection conn = null;
		try {
			log.debug("insert drain to register {}", obj.getName());
			conn = dataSource.getConnection();

			// try to update the measure
			PreparedStatement pStatement = conn.prepareStatement(UPDATE_MEASURE_RT);
			pStatement.setBigDecimal(1, (BigDecimal) obj.getValue().getValue());
			pStatement.setTimestamp(2, new java.sql.Timestamp(obj.getTimestamp().getTime()));
			pStatement.setLong(3, id);

			int count = pStatement.executeUpdate();
			pStatement.close();

			if (count == 0) {
				// failed the update try with insert
				pStatement = conn.prepareStatement(INSERT_MEASURE_RT);
				pStatement.setLong(1, id);
				pStatement.setBigDecimal(2, (BigDecimal) obj.getValue().getValue());
				pStatement.setTimestamp(3, new java.sql.Timestamp(obj.getTimestamp().getTime()));

				pStatement.executeUpdate();
				pStatement.close();
			}

		} catch (SQLException e) {
			// log error and rethrow ex
			log.error("", e);
			throw e;
		} catch (Exception e) {
			// log error and rethrow ex
			log.error("", e);
			throw e;
		}

		finally {
			try {
				// close the connection
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				log.error("", e);
			}
		}
	}

	private long getDrainId(Survey obj) throws SQLException {
		log.debug("get drain id for current survey {}", obj);

		// extract drain name
		String drainName = obj.getName();

		// check id drain is in the maps
		if (drainIdMap.containsKey(drainName)) {
			// return the drain name
			return drainIdMap.get(drainName);
		}

		// drain id missing try to reload it from db
		loadDrainRegistry();

		// check id drain is in the maps
		if (drainIdMap.containsKey(drainName)) {
			// return the drain name
			return drainIdMap.get(drainName);
		}

		// id not found insert the drain in the database
		insertDrainRegistry(drainName, obj.getValue().getUnit());

		return drainIdMap.get(drainName);
	}

	/**
	 * Load drain registry from database
	 * 
	 * @throws SQLException
	 */
	synchronized private void loadDrainRegistry() throws SQLException {
		synchronized (this) {
			Connection conn = null;
			try {
				log.debug("load drain registry from database");
				conn = dataSource.getConnection();

				// load drain and id from database
				final PreparedStatement pStatement = conn.prepareStatement(DRAIN_REGISTRY);
				final ResultSet resultSet = pStatement.executeQuery();

				while (resultSet.next()) {
					// load data
					final String drainString = resultSet.getString("drain");
					final long drainId = resultSet.getLong("id");

					log.debug("found drain {} with id {}", drainString, drainId);
					drainIdMap.put(drainString, drainId);
				}
			} catch (SQLException e) {
				// log error and rethrow ex
				log.error("", e);
				throw e;
			} finally {
				try {
					// close the connection
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					log.error("", e);
				}
			}
		}
	}

	/**
	 * Insert a new drain the into registry
	 * 
	 * @param drainName
	 *            the drain name
	 * @throws SQLException
	 */
	synchronized private void insertDrainRegistry(String drainName, Unit<?> unit) throws SQLException {
		synchronized (this) {
			Connection conn = null;
			try {
				log.debug("insert drain to register {}", drainName);
				conn = dataSource.getConnection();

				// insert drain into db
				final PreparedStatement pStatement = conn.prepareStatement(DRAIN_INSERT, Statement.RETURN_GENERATED_KEYS);
				pStatement.setString(1, drainName);
				pStatement.setString(2, unit.toString());

				pStatement.executeUpdate();

				// get the generated id
				ResultSet resultSet = pStatement.getGeneratedKeys();
				if (resultSet.next()) {
					long id = resultSet.getLong(1);

					// save the new id
					drainIdMap.put(drainName, id);
				}
			} catch (SQLException e) {
				// log error and rethrow ex
				log.error("", e);
				throw e;
			} finally {
				try {
					// close the connection
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					log.error("", e);
				}
			}
		}
	}
}
