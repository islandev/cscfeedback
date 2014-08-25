/**
 * 
 */
package com.sap.csc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * @author I307658
 * 
 */
public class FeedbackItemDAOimpl implements ItemDAO {

	private DataSource dataSource;

	/**
	 * Create new data access object with data source.
	 */
	public FeedbackItemDAOimpl(DataSource newDataSource) throws SQLException {
		setDataSource(newDataSource);
	}

	/**
	 * Get data source which is used for the database operations.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set data source to be used for the database operations.
	 */
	public void setDataSource(DataSource newDataSource) throws SQLException {
		this.dataSource = newDataSource;

	}

	@Override
	public FeedbackItem[] findAll() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean insert(FeedbackItem fbi) throws SQLException {
		// TODO Auto-generated method stub

		Connection connection = dataSource.getConnection();

		try {
			PreparedStatement pstmt = connection
					.prepareStatement("INSERT INTO FEEDBACK (TYPE, SUBJECT, CONTENT,FULLNAME,USER_MAILADDRESS,TELEPHONE,SEVERITY,ISREPLIED,CREATE_DATETIME) "
							+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?,?)");
			pstmt.setInt(1, fbi.getType());
			pstmt.setString(2, fbi.getSubject());
			pstmt.setString(3, fbi.getContent());
			pstmt.setString(4, fbi.getFullName());
			pstmt.setString(5, fbi.getCustomerEmail());
			pstmt.setString(6, fbi.getMobileNum());
			pstmt.setInt(7, fbi.getSeverity());
			pstmt.setInt(8, fbi.isReplied());
			pstmt.setDate(9, fbi.getDatetime());
			pstmt.executeUpdate();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return true;
	}

}
