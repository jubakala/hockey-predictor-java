package model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/*
 * Metodi huolehtii joukkueisiin liittyvistä toimenpiteistä.
 */
public class Team extends Model
{
	private String name = "";
	private int id      = 0;
	
	public Team() {}
	
	/*
	 * Konstruktori, joka alustaa joukkueen nimellä ja ID:llä.
	 * 
	 * @param		name		Joukkueen nimi. 
	 * @param		id			Joukkueen ID.
	 */
	public Team(String name, int id)
	{
		setName(name);
		setId(id);
	}
	
	/*
	 * Metodi asettaa joukkueelle nimen.
	 * 
	 * @param		name		Joukkueen nimi.
	 */ 
	public void setName(String name)
	{
		this.name = name;
	}
	
	/*
	 * Metodi palauttaa joukkueelle nimen.
	 * 
	 * @return		Joukkueen nimi.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/*
	 * Metodi asettaa joukkueelle ID:n
	 * 
	 * @param		id		Joukkueen ID.
	 */
	public void setId(int id)
	{
		this.id = id;
	}
	
	/*
	 * Metodi palauttaa joukkueen ID:n
	 * 
	 * @return			Joukkueen ID.
	 */
	public int getId()
	{
		return this.id;
	}
	
	/*
	 * Tämä metodi hakee ensin tietokannasta kaikki tietyllä kaudella, ennen untilDate päivämäärää ennen
	 * pelattujen otteluiden määrät tehdyt maalit, joko koti- tai vierasotteluissa, ja laskee maalikeskiarvon.
	 * Jos gamesAmount -muuttuja on -1, lasketaan mukaan kaikki kauden ottelut untilDate päivämäärään asti.
	 * Muutoin lasketaan vain gamesAmount -muuttujan arvon verran viimeisiä otteluita mukaan (esim. 3).
	 * Jos joukkue ei ole pelannut ennen untilDate päivämäärää vähintään kolmea ottelua tapauksesta riippuen
	 * joko kotona tai vieraissa, palautetaan keskiarvo 0.0, joka tarkoittaa null arvoa.
	 * 
	 * Vain Poisson-malli käyttää tätä metodia.
	 * 
	 * @param		teamID		Joukkueen ID.
	 * @param		season		Kausi, jota simuloidaan.
	 * @param		homeAway	1 = koti- ja vierasotteluiden arvot erikseen. 0 == vain yksi keskiarvo koti- ja vierasotteluihin.
	 * @param		untilDate	Päivämäärä, johon asti otteluita haetaan.
	 * @param		gamesAmount	Otteluiden määrä, jotka otetaan vain huomioon keskiarvoja laskettaessa. -1 = kaikki jo pelatut ottelut huomioidaan.
	 */
	public double getMean(int teamID, String season, int homeAway, String untilDate, int gamesAmount)
	{
		Connection connection = getConnection();
		String homeOrAway 	  = "";
		String homeTeamOrAway = "";
		int gamesPlayed       = 0;
		int goalsAmount		  = 0;
		Statement statement   = null;
		ResultSet results	  = null;
		
		// Kuinka monta ottelua joukkueen on vähintään pitänyt pelata.
		int minumumGameAmount = 3;
		
		if (gamesAmount > -1) { minumumGameAmount = gamesAmount; }
		
		// Lasketaan ensin ennen untilDate -päivämäärää pelattujen otteluiden määrä. Jos se on alle 3, ei
		// keskiarvoa lasketa.
		try
		{
			// Jos lasketaan koti- ja vierasotteluille erilliset keskiarvot.
			if (homeAway == 1)
			{
				homeOrAway     = "home_goals";
				homeTeamOrAway = "home_team";
			}
			else
			{
				homeOrAway     = "away_goals";
				homeTeamOrAway = "away_team";
			}
			
			statement  = connection.createStatement();
			String sql = "";
			
			// Lasketaan ennen untilDate-päivämäärää ennen pelattujen otteluiden määrä.
			sql = "SELECT COUNT(*) AS amount FROM games WHERE season = '" + season + "' AND " + homeTeamOrAway + " = '" + teamID + "' AND date < '" + untilDate + "';";

			results    = statement.executeQuery(sql);
			
			while (results.next()) 
			{
				gamesPlayed = results.getInt("amount");
			}

			connection.close();
		}
		catch(SQLException ex) {}
		
		// Haetaan koti/vierasmaalit otteluissa, jotka ovat pelattu ennen untilDate -päivämäärää.		
		if (gamesPlayed > minumumGameAmount)
		{
			try
			{
				connection = getConnection();
				statement  = connection.createStatement();
				results	   = null;
				String sql = "";
				
				// Lasketaan kaikkein tähän asti pelattujen otteluiden maalit mukaan keskiarvoon.
				if (gamesAmount == -1)
				{
					sql     = "SELECT SUM(" + homeOrAway + ") AS amount FROM games WHERE season = '" + season + "' AND " + homeTeamOrAway + " = '" + teamID + "' AND date < '" + untilDate + "';";
					results = statement.executeQuery(sql);
					
					while (results.next()) 
					{
						goalsAmount = results.getInt("amount");
					}
				}
				else
				{
					sql         = "SELECT " + homeOrAway + " FROM games WHERE season = '" + season + "' AND " + homeTeamOrAway + " = '" + teamID + "' AND date < '" + untilDate + "' ORDER BY date DESC LIMIT 0," + gamesAmount + ";";
					results     = statement.executeQuery(sql);
					goalsAmount = 0;
					
					while (results.next()) 
					{
						goalsAmount = goalsAmount + results.getInt(homeOrAway);
					}
				}

				connection.close();
			}
			catch(SQLException ex) {}
		}
		
		double mean = 0.0;
		
		// Lasketaan maalikeskiarvo.
		// Jos kaikki jo pelatut ottelut huomioidaan keskiarvoon.
		if (gamesAmount == -1)
		{
			mean = (double)goalsAmount / gamesPlayed;
		}
		else
		{
			mean = (double)goalsAmount / gamesAmount;
		}
		
		return mean;
	}
}
