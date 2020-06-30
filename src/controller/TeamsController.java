package controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.util.Vector;
import model.Team;

/*
 * Luokka huolehtii joukkueisiin liittyvistä toimenpiteistä.
 */
public class TeamsController extends Controller
{
	private static TeamsController instance = null;
	private Statement statement    			= null;
	private ResultSet results      			= null;
	
	protected TeamsController() {}
	
	/*
	 * Metodi, joka tarkistaa, onko luokasta jo olemassa oliota. Jos ei ole, se luodaan ja palautetaan kutsujalle.
	 * Jos olio on jo olemassa, palautetaan tämä olio kutsujalle. Toteutetaan siis Singleton-suunnittelumalli.
	 * 
	 * @return 		Palautetaan TeamController-olio kutsujalle.
	 */
	public static TeamsController getInstance()
	{
		// Jos tämä on ensimmäinen kerta kun tätä kyseistä luokkaa kutsutaan, luodaan olio.
		// Sen sijaan, jos tästä luokasta on jo olemassa olio/instanssi, palautetaan se kutsujalle.
		if (instance == null)
		{
			instance = new TeamsController();
		}
		
		return instance;
	}

	/*
	 * Tämä metodi hakee kaikkien kyseisellä kaudella pelanneiden joukkueiden id:t ja nimet tietokannasta ja tallentaa ne vektoriin.
	 * 
	 * @param		season		Kausi, jota halutaan simuloida.
	 * 
	 * @return					Vektori, johon on tallennettuna joukkueiden tiedot.
	 */
	public Vector<Team> getTeams(String season)
	{
		Vector<Team> teams    = new Vector<Team>();
		Connection connection = getConnection();
		ResultSet teamResults = null;
		
		if (connection != null) 
		{ 
			try
			{
				statement = connection.createStatement();
				results   = statement.executeQuery("SELECT home_team FROM games GROUP BY home_team;");
				
				while (results.next()) 
				{
					int teamID  = results.getInt("home_team");
					statement   = connection.createStatement();
					teamResults = statement.executeQuery("SELECT name FROM teams WHERE id = '" + teamID + "' LIMIT 0,1;");
					
					teamResults.first();
					String name = teamResults.getString("name");
					Team team   = new Team(name, teamID);
					teams.add(team);
				}
				
				connection.close();
			}
			catch(SQLException ex) {}
		} 
		
		return teams;
	}
}
