package org.w3id.scholarlydata.clodg.hsqldb;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class HyperSQLClodg {

	private Connection conn;
	
	public HyperSQLClodg(Connection conn) {
		this.conn = conn;
	}
	
	public HyperSQLClodg() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			
			this.conn = DriverManager.getConnection("jdbc:hsqldb:file:/Users/andrea/Documents/workspaceMars/clodg2/clodg", "SA", "");
			
			System.out.println("Database connected.");
			
			String expression = "CREATE FUNCTION SHA1(in VARCHAR(100000))"
					+ "RETURNS VARCHAR(100000) "
					+ "LANGUAGE JAVA DETERMINISTIC NO SQL "
					+ "EXTERNAL NAME 'CLASSPATH:org.apache.commons.codec.digest.DigestUtils.shaHex'";
			
			Statement stmt = this.conn.createStatement();
			stmt.execute(expression);
			stmt.execute("SET DATABASE SQL SYNTAX MYS TRUE");

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void update(String expression) throws SQLException {

        Statement st = null;

        st = conn.createStatement();    // statements

        int i = st.executeUpdate(expression);    // run the query

        if (i == -1) {
            System.out.println("db error : " + expression);
        }

        st.close();
    }
	
	public synchronized void query(String expression) throws SQLException {

        Statement st = null;
        ResultSet rs = null;

        st = conn.createStatement();         // statement objects can be reused with

        // repeated calls to execute but we
        // choose to make a new one each time
        rs = st.executeQuery(expression);    // run the query

        // do something with the result set.
        dump(rs);
        st.close();    // NOTE!! if you close a statement the associated ResultSet is
        
                // closed too
        // so you should copy the contents to some other object.
        // the result set is invalidated also  if you recycle an Statement
        // and try to execute some other query before the result set has been
        // completely examined.
    }
	
	public static void dump(ResultSet rs) throws SQLException {

        // the order of the rows in a cursor
        // are implementation dependent unless you use the SQL ORDER statement
        ResultSetMetaData meta   = rs.getMetaData();
        int               colmax = meta.getColumnCount();
        int               i;
        Object            o = null;

        // the result set is a cursor into the data.  You can only
        // point to one row at a time
        // assume we are pointing to BEFORE the first row
        // rs.next() points to next row and returns true
        // or false if there is no next row, which breaks the loop
        for (; rs.next(); ) {
            for (i = 0; i < colmax; ++i) {
                o = rs.getObject(i + 1);    // Is SQL the first column is indexed

                // with 1 not 0
                System.out.print(o.toString() + " ");
            }

            System.out.println(" ");
        }
    } 
	
	
	public void shutdown() throws SQLException {

        Statement st = conn.createStatement();

        // db writes out to files and performs clean shuts down
        // otherwise there will be an unclean shutdown
        // when program ends
        st.execute("SHUTDOWN");
        conn.close();    // if there are no other open connection
    }
	
	public void loadCSV(File file){
		CSVLoader loader = new CSVLoader(conn);
		try {
			loader.loadCSV(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		HyperSQLClodg hyperSQLClodg = new HyperSQLClodg();
		try {
			hyperSQLClodg.query("SELECT \"ORGANISING\".\"email\", (\"ORGANISING\".\"email\" <> '') AS expr18ed4c53, (\"ORGANISING\".\"email\" <> \"\") AS expr18ed4bb3, \"ORGANISING\".\"Web site\", (ORGANISING.`Web site` <> '') AS expr857fa612, \"ORGANISING\".\"last name\", \"ORGANISING\".\"first name\", \"ORGANISING\".\"organization\", (SHA1(CONCAT(\"mailto:\", \"ORGANISING\".\"email\"))) AS expre3201e94, \"ORGANISING\".\"role\" FROM \"ORGANISING\" WHERE ((\"ORGANISING\".\"Web site\" IS NOT NULL AND \"ORGANISING\".\"first name\" IS NOT NULL AND \"ORGANISING\".\"last name\" IS NOT NULL AND (ORGANISING.`Web site` <> '')) OR (\"ORGANISING\".\"email\" IS NOT NULL AND \"ORGANISING\".\"first name\" IS NOT NULL AND \"ORGANISING\".\"last name\" IS NOT NULL AND (\"ORGANISING\".\"email\" <> '')) OR (\"ORGANISING\".\"first name\" IS NOT NULL AND \"ORGANISING\".\"last name\" IS NOT NULL AND \"ORGANISING\".\"organization\" IS NOT NULL) OR (\"ORGANISING\".\"first name\" IS NOT NULL AND \"ORGANISING\".\"last name\" IS NOT NULL AND \"ORGANISING\".\"role\" IS NOT NULL) OR (\"ORGANISING\".\"first name\" IS NOT NULL AND \"ORGANISING\".\"last name\" IS NOT NULL AND (\"ORGANISING\".\"email\" <> \"\") AND (SHA1(CONCAT(\"mailto:\", \"ORGANISING\".\"email\"))) IS NOT NULL) OR (\"ORGANISING\".\"first name\" IS NOT NULL AND \"ORGANISING\".\"last name\" IS NOT NULL))");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//String expression = "CREATE TABLE sample_table ( id INTEGER IDENTITY, str_col VARCHAR(256), num_col INTEGER)";
		try {
			/*
			//hyperSQLClodg.update(expression);
			
			hyperSQLClodg.update(
	                "INSERT INTO sample_table(str_col,num_col) VALUES('Ford', 100)");
			hyperSQLClodg.update(
	                "INSERT INTO sample_table(str_col,num_col) VALUES('Toyota', 200)");
			hyperSQLClodg.update(
	                "INSERT INTO sample_table(str_col,num_col) VALUES('Honda', 300)");
			hyperSQLClodg.update(
	                "INSERT INTO sample_table(str_col,num_col) VALUES('GM', 400)");
			
			
			InputStream is = HyperSQLClodg.class.getClassLoader().getResourceAsStream("hsqldb/easychair_schema.sql");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String content = "";
			String line = null;
			
			while((line = reader.readLine()) != null){
				content += line + '\n';
			}
			
			reader.close();
			is.close();
					
			
			hyperSQLClodg.query("SELECT * FROM sample_table WHERE num_col < 250");
			
			hyperSQLClodg.query("SELECT * FROM sample_table WHERE num_col < 250");
			
			
			*
			hyperSQLClodg.loadCSV(new File("demo/submission.csv"));
			 */
			// at end of program
			hyperSQLClodg.shutdown();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
}
