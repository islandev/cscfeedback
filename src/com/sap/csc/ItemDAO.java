package com.sap.csc;

import java.sql.SQLException;

public interface ItemDAO {
	public FeedbackItem[] findAll() throws SQLException;
	
	public boolean insert(FeedbackItem fbi) throws SQLException;
}
