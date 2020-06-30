package controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.util.HashMap;
import model.EloRating;
import java.util.Vector;

/*
 * Tämä luokka huolehtii Elo-lukuihin liittyvistä toimenpiteistä sovelluksessa. 
 */
public class EloRatingsController extends Controller
{
	private static EloRatingsController instance = null;
	
	protected EloRatingsController() {}
	
	/*
	 * Metodi, joka tarkistaa, onko luokasta jo olemassa oliota. Jos ei ole, se luodaan ja palautetaan kutsujalle.
	 * Jos olio on jo olemassa, palautetaan tämä olio kutsujalle. Toteutetaan siis Singleton-suunnittelumalli.
	 * 
	 * @return 		Palautetaan EloRatingsController-olio kutsujalle.
	 */
	public static EloRatingsController getInstance()
	{
		// Jos tämä on ensimmäinen kerta kun tätä kyseistä luokkaa kutsutaan, luodaan olio.
		// Sen sijaan, jos tästä luokasta on jo olemassa olio/instanssi, palautetaan se kutsujalle.
		if (instance == null)
		{
			instance = new EloRatingsController();
		}
		
		return instance;
	}
	
	/*
	 * Tämä metodi laskee todennäköisyydet eri lopputuloksille yksittäiseen otteluun.
	 * 
	 * @param		season				Kausi, jota simuloidaan.
	 * @param		homeEloRating		Kotijoukkueen ottelua edeltävä Elo-luku.
	 * @param		awayEloRating		Vierasjoukkueen ottelua edeltävä Elo-luku.
	 * @param		expectedHomePoints	Kotijoukkueen ennakoidut pisteet tähän otteluun Elo-lukujen perusteella laskettuna. Tämä on myös kotivoiton todennäköisyys
	 * 									ennen kuin tasapelin osuus on vähennetty siitä.
	 * @param		expectedAwayPoints	Vierasjoukkueen ennakoidut pisteet tähän otteluun Elo-lukujen perusteella laskettuna. Tämä on myös vierasvoiton todennäköisyys
	 * 									ennen kuin tasapelin osuus on vähennetty siitä.
	 * @returm							Todennäköisyydet ottelun eri lopputuloksille. 
	 * 									Indeksissä 0 = kotivoitontodennäköisyys, indeksissä 1 = tasapelintodennäköisyys ja
	 * 									indeksiss� 2 = vierasvoitontodennäköisyys.
	 */
	public Vector<Double> getProbabilities(String season, double homeEloRating, double awayEloRating, double expectedHomePoints, double expectedAwayPoints)
	{
		Vector<Double> probabilities = new Vector<Double>();
		// Lasketaan joukkueiden Elo-lukujen välinen erotus.
		double difference 			 = homeEloRating - awayEloRating;
		double drawProbability 		 = 0.0;

		// Jos erotus on negatiivinen, haetaan sen vastaluku.
		if (difference < 0) { difference = difference - difference - difference; }
		
		// Todennäköisyydet lasketaan vain kaudesta 2008-2009 alkaen, koska tietokannasta löytyy kertoimet vasta tuosta kaudesta alkaen.
		// Lisäksi tasapelien todennäköisyyksiä on laskettu kaudet 2002-2008, joita käytetään kaudella 200
		if (season.equals("2008-2009") || season.equals("2009-2010") || season.equals("2010-2011") || season.equals("2011-2012")) 
		{
			String drawCategory = "";
			
			// Määritellään tasapelikategoria. 
			if (difference > 249) { drawCategory = "> 250"; }
			if (difference < 249 && difference > 200) { drawCategory = "200-249"; }
			if (difference < 200 && difference > 150) { drawCategory = "150-199"; }
			if (difference < 150 && difference > 100) { drawCategory = "100-149"; }
			if (difference < 100 && difference > 50)  { drawCategory = "50-99"; }
			if (difference < 50)  { drawCategory = "0-49"; }
			
			Connection connection 			    = getConnection();
			Statement statement   			    = null;
			ResultSet results	  			    = null;
			
			if (connection != null) 
			{ 
				try
				{
					statement = connection.createStatement();
					// Haetaan tasapelikategorian tiedot tietokannasta.
					results   = statement.executeQuery("SELECT amount, draw_amount FROM drawcategories WHERE season = '" + season + "' AND category = '" + drawCategory + "';");
					
					while (results.next()) 
					{
						// Toteutuneiden tasapelien määrä kys. tasapelikategoriassa.
						int drawAmount  = results.getInt("draw_amount");
						// Otteluiden yhteismäärä kys. tasapelikategoriassa.
						int amount      = results.getInt("amount");
						// Tasapelin todennäköisyys. (tasapelimäärä / otteluiden yhteismäärä)
						drawProbability = (double)drawAmount / amount;
						// Jaetaan tasapelin todennäköisyys "kahteen osaan".
						double part = drawProbability / 2;
						// Vähennetään tasapelitodennäköisyydenpuolikas kotivoitontodennäköisyydestä.
						expectedHomePoints -= part;
						// Vähennetään tasapelitodennäköisyydenpuolikas vierasvoitontodennäköisyydestä.
						expectedAwayPoints -= part;
					}
					
					connection.close();
				}
				catch(SQLException ex) {}
			}
			
			// Lisätään todennäköisyydet vektoriin.
			probabilities.add(expectedHomePoints);
			probabilities.add(drawProbability);
			probabilities.add(expectedAwayPoints);
		}
		
		return probabilities;
	}
	
	/*
	 * Metodi laskee Elo-lukujen perusteella odotetut pistem��r�t ottelussa molemmille joukkueille. Pistem��rien summa on 1.
	 * 
	 * @param		homeEloRating		Kotijoukkueen t�m�n hetkinen Elo-luku.
	 * @param		awayEloRating		Vierasjoukkueen t�m�n hetkinen Elo-luku.
	 * @return							Joukkueiden odotetut pistem��r�t otteluun. Indeksiss� 0 = kotijoukkueen odotetut pisteet, 
	 * 									indeksiss� 1 = vierasjoukkueen odotetut pisteet.
	 */
	public Vector<Double> countExpectedPoints(double homeEloRating, double awayEloRating)
	{
		Vector<Double> expectedPoints = new Vector<Double>();
		// Lasketaan kotijoukkueen odotetut pisteet. 
		double homePoints = 10 * homeEloRating / 400 / ((10 * homeEloRating / 400) + (10 * awayEloRating / 400));
		// Lasketaan vierasjoukkueen odotetut pisteet.
		double awayPoints = 1 - homePoints;
		// Tallennetaan odotetut pistem��r�t vektoriin.
		expectedPoints.add(homePoints);
		expectedPoints.add(awayPoints);
		
		return expectedPoints;
	}
	
	/*
	 * T�m� metodi hakee kaikki joukkueet tietokannasta. Koska joukkueet ovat aina samat joka kaudella, ei kautta tarvitse erikseen valita.
	 * 
	 * @return				HashMap, jossa indeksin� on joukkueen ID tietokannassa, ja varsinaisena tietona joukkueen Elo-luku olio.  
	 */
	public HashMap<Integer, EloRating> initializeTeamRatings()
	{
		Connection connection 			    = getConnection();
		Statement statement   			    = null;
		ResultSet results	  			    = null;
		HashMap<Integer, EloRating> ratings = new HashMap<Integer, EloRating>();
		
		if (connection != null) 
		{ 
			try
			{
				statement = connection.createStatement();
				// Haetaan kaikkien joukkueiden ID:t tietokannasta. 
				results   = statement.executeQuery("SELECT home_team FROM games GROUP BY home_team;");
				
				while (results.next()) 
				{
					// Luodaan "tyhj�" Elo-luku -olio jokaiselle joukkueelle.
					EloRating rating = new EloRating();
					int teamID = results.getInt("home_team");
					// Tallennetaan joukkueen ID Elo-luku -olioon.
					rating.setTeamID(teamID);
					// Lis�t��n Elo-luku -olio HashMappiin, joukkueen ID:n indeksiin.
					ratings.put(teamID, rating);
				}
				
				connection.close();
			}
			catch(SQLException ex) {}
		}
		
		return ratings;
	}
}
