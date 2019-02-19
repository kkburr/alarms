package com.handshake;

import com.handshake.resources.Alarm;
import j2html.tags.ContainerTag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static j2html.TagCreator.body;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.join;
import static j2html.TagCreator.li;
import static j2html.TagCreator.ul;

@RestController
public class AlarmsController {
    private ResultSet getAlarmsFromDB(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        return stmt.executeQuery( "SELECT * FROM alarms;" );
    }

    private Connection connectToDb() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5433/handshake", "postgres", "Turn!p12");
        System.out.println("Opened DB successfully");
        return conn;
    }

    private void putAlarmInDB(Connection conn, String alarmText) throws SQLException {
        PreparedStatement stmt = null;
        String sqlString = "INSERT INTO alarms (TEXT) VALUES (?)";
        try {
            stmt = conn.prepareStatement(sqlString);
            stmt.setString(1, alarmText);

            // execute insert SQL stetement
            stmt.executeUpdate();
            System.out.println("Record is inserted into alarms table!");

            Statement stmt2 = conn.createStatement();
            ResultSet set = stmt2.executeQuery("SELECT max(id) FROM alarms;");

            while ( set.next() ) {
                Integer id = set.getInt("max");
                try {
                    pushAlarmToPhone(id);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally {
            if (stmt != null) {
                stmt.close();
            }

            if (conn != null) {
                conn.close();
            }
        }
    }

    private String cleanText(String text) {
        return text.toUpperCase();
    }

    private void pushAlarmToPhone(Integer id) throws MalformedURLException, IOException {
        String urlParameters  = "alarm_id=" + id.toString();
        byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
        String request        = "http://handshake-bellbird.herokuapp.com/push";
        URL url = new URL(request);
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setRequestMethod( "POST" );
//        int    postDataLength = postData.length;
        conn.setDoOutput( true );
//        conn.setInstanceFollowRedirects( false );
//        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
//        conn.setRequestProperty( "charset", "utf-8");
//        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
//        conn.setUseCaches( false );
        try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
            wr.write( postData );
        }
    }

    private void upVoteAlarm(Connection conn, Integer id) {
        PreparedStatement stmt = null;
        String sqlString = "UPDATE alarms set votes = votes + 1 where id = ?";

        try {
            stmt = conn.prepareStatement(sqlString);
            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Record is inserted into alarms table!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());

        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    localhost:8080/7
    @PostMapping(path = "/upvote")
    public void voteForAlarm(@RequestParam Integer id){
        try {
            Connection conn = connectToDb();
            upVoteAlarm(conn, id);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    @PostMapping(path = "/new")
    public void setAlarms(@RequestParam String text){
        try {
            Connection conn = connectToDb();
            putAlarmInDB(conn, cleanText(text));
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    @GetMapping("/index")
    public String getAlarms() {
        ArrayList<Alarm> alarms = new ArrayList();
        try {
            Connection conn = connectToDb();
            ResultSet rs = getAlarmsFromDB(conn);
//            ideally would sort at this level instead of having to stream and sort on line 102
            while ( rs.next() ) {
                int id = rs.getInt("id");
                int votes = rs.getInt("votes");
                String text = rs.getString("text");
                alarms.add(new Alarm(id, votes, text));
            }
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return body(
                h1("Alarms"),
                ul(
                    alarms.stream().sorted((a1, a2) -> a2.getId().compareTo(a1.getId())).map(
                        a -> li(join(a.getId().toString(), " -- ", a.getText(), ", Upvotes: ", a.getVotes().toString()))).toArray(ContainerTag[]::new)
                )
        ).render();
    }
}

