package it.proximacentauri.ienergy.osgi.dao;

import it.proximacentauri.ienergy.osgi.domain.Survey;

import java.sql.SQLException;

public interface SurveyDao {

	void insert(Survey obj) throws SQLException;
}
