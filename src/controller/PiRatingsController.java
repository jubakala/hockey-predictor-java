package controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

import model.PiRating;

/*
 * Tämä luokka huolehtii pi-hockey -mallin tarvitsemista toiminnoista.
 */
public class PiRatingsController extends Controller 
{
	private static PiRatingsController instance = null;
	
	protected PiRatingsController() {}
	
	/*
	 * Metodi, joka tarkistaa, onko luokasta jo olemassa oliota. Jos ei ole, se luodaan ja palautetaan kutsujalle.
	 * Jos olio on jo olemassa, palautetaan tämä olio kutsujalle. Toteutetaan siis Singleton-suunnittelumalli.
	 * 
	 * @return 		Palautetaan PiRatingsController-olio kutsujalle.
	 */
	public static PiRatingsController getInstance()
	{
		// Jos tämä on ensimmäinen kerta kun tätä kyseistä luokkaa kutsutaan, luodaan olio.
		// Sen sijaan, jos tästä luokasta on jo olemassa olio/instanssi, palautetaan se kutsujalle.
		if (instance == null)
		{
			instance = new PiRatingsController();
		}
		
		return instance;
	}
	
	/*
	 * Tämä metodi laskee todennäköisyydet ottelun eri lopputuloksille pi-hockey -mallia hyödyntäen.
	 * 
	 * @param		expectedGoalDifference		Odotettu maaliero ottelussa.
	 * @param		season						Kausi, jota simuloidaan.
	 */
	public Vector<Double> formProbabilities(double expectedGoalDifference, String season)
	{
		// Itseisarvo odotetusta maalierosta.
		expectedGoalDifference       = Math.abs(expectedGoalDifference);
		Connection connection 		 = getConnection();
		Statement statement   		 = null;
		ResultSet results	  		 = null;
		Vector<Double> probabilities = new Vector<Double>();
		double lowerLimit			 = 0.0;
		double upperLimit			 = 0.0;
		String sql					 = "";
		
		// Määritellään odotetun maalieron perustella ala- ja ylärajat tasapelikategorian määrittämiseksi.
		if (expectedGoalDifference >= 0   && expectedGoalDifference <= 0.15)   	{ lowerLimit = 0;    upperLimit = 0.15; }
		if (expectedGoalDifference > 0.15 && expectedGoalDifference <= 0.3)    	{ lowerLimit = 0.15; upperLimit = 0.3; }
		if (expectedGoalDifference > 0.30 && expectedGoalDifference <= 0.45)   	{ lowerLimit = 0.3;  upperLimit = 0.45; }
		if (expectedGoalDifference > 0.45 && expectedGoalDifference <= 0.6)    	{ lowerLimit = 0.45; upperLimit = 0.6; }
		if (expectedGoalDifference > 0.60 && expectedGoalDifference <= 0.75)   	{ lowerLimit = 0.6;  upperLimit = 0.75; }
		if (expectedGoalDifference > 0.75 && expectedGoalDifference <= 0.9)    	{ lowerLimit = 0.75; upperLimit = 0.9; }
		if (expectedGoalDifference > 0.90 && expectedGoalDifference <= 1.05)   	{ lowerLimit = 0.9;  upperLimit = 1.05; }
		if (expectedGoalDifference > 1.05 && expectedGoalDifference <= 1.2)    	{ lowerLimit = 1.05; upperLimit = 1.2; }
		if (expectedGoalDifference > 1.20 && expectedGoalDifference <= 1.35)   	{ lowerLimit = 1.2;  upperLimit = 1.35; }
		if (expectedGoalDifference > 1.35 && expectedGoalDifference <= 1.5)    	{ lowerLimit = 1.35; upperLimit = 1.5; }
		if (expectedGoalDifference > 1.5  && expectedGoalDifference <= 1.65)    { lowerLimit = 1.5;  upperLimit = 1.65; }
		if (expectedGoalDifference > 1.65 && expectedGoalDifference <= 1.8)    	{ lowerLimit = 1.65; upperLimit = 1.8; }
		if (expectedGoalDifference > 1.8  && expectedGoalDifference <= 1.95)    { lowerLimit = 1.8;  upperLimit = 1.95; }
		if (expectedGoalDifference > 1.95 && expectedGoalDifference <= 2.1)    	{ lowerLimit = 1.95; upperLimit = 2.1; }
		if (expectedGoalDifference > 2.1  && expectedGoalDifference <= 2.25)    { lowerLimit = 2.1;  upperLimit = 2.25; }
		if (expectedGoalDifference > 2.25 && expectedGoalDifference <= 2.4)    	{ lowerLimit = 2.25; upperLimit = 2.4; }
		if (expectedGoalDifference > 2.4  && expectedGoalDifference <= 2.55)    { lowerLimit = 2.4;  upperLimit = 2.55; }
		if (expectedGoalDifference > 2.55)    									{ lowerLimit = 2.55; upperLimit = 9; }
		
		// Haetaan koti- ja vierasvoiton sekä tasapelin todennäköisyydet ylä- ja alarajan sekä kauden perusteella.
		sql = "SELECT home_win, draw, away_win FROM pi_probabilities WHERE lower_limit = '" + lowerLimit + "' AND upper_limit = '" + upperLimit + "' AND season = '" + season + "';";
		
		double homeWin = 0.0;
		double draw    = 0.0;
		double awayWin = 0.0;
		
		if (connection != null) 
		{ 
			try
			{
				statement = connection.createStatement();
				results   = statement.executeQuery(sql);
				
				while (results.next()) 
				{
					homeWin = results.getDouble("home_win");
					draw    = results.getDouble("draw");
					awayWin = results.getDouble("away_win");
				}
				
				connection.close();
			}
			catch(SQLException ex) {}
		}
		
		// Lisätään todennäköisyydet vektoriin.
		probabilities.add(homeWin);
		probabilities.add(draw);
		probabilities.add(awayWin);
		
		return probabilities;
	}
	
	/*
	 * Tämä metodi laskee maalierovirheen kotijoukkueelle. 
	 * 
	 * @param		actualGoalDifference		Ottelun toteutunut maaliero.
	 * @param		expectedGoalDifference		Ottelun ennakoitu maaliero.
	 * @param		error						Toteutunut maalierovirhe.
	 * 
	 * @return									Maalierovirhe.
	 */
	public double calculateHomeError(double actualGoalDifference, double expectedGoalDifference, double error)
	{
		double homeError = 0.0;
		
		// Jos ennakoitu maaliero oli pieniempi tai yhtä suuri kuin toteutunut maaliero => kotijoukkue suoriutui odotettua paremmin.
		if (expectedGoalDifference <= actualGoalDifference)
		{
			homeError = error;
		}
		else // Kotijoukkue suoriutui odotettua huonommin.
		{
			homeError = error - error - error;
		}
		
		return homeError;
	}
	
	/*
	 * Tämä metodi laskee maalierovirheen vierasjoukkueelle. 
	 * 
	 * @param		actualGoalDifference		Ottelun toteutunut maaliero.
	 * @param		expectedGoalDifference		Ottelun ennakoitu maaliero.
	 * @param		error						Toteutunut maalierovirhe.
	 * 
	 * @return									Maalierovirhe.
	 */	
	public double calculateAwayError(double actualGoalDifference, double expectedGoalDifference, double error)
	{
		double awayError = 0.0;
		
		// Jos ennakoitu maaliero oli suurempi tai yhtä suuri kuin toteutunut maaliero => vierasjoukkuejoukkue suoriutui odotettua paremmin.
		if (expectedGoalDifference >= actualGoalDifference)
		{
			awayError = error;
		}
		else // Vierasjoukkue suoriutui odotettua huonommin.
		{
			awayError = error - error - error;
		}
		
		return awayError;
	}
	
	/*
	 * Metodi laskee painotetun maalierovirheen.
	 * 
	 * @param		error		Toteutunut maalierovirhe.
	 * 
	 * @return					Painotettu maalierovirhe.
	 */
	public double calculateWeightedGoalError(double error)
	{
		double weightedError = 0.0;
		weightedError = 3 * (Math.log10(1 + error));
		
		return weightedError;
	}
	
	/*
	 * Metodi laskee maalierovirheen. Maalierovirhe on aina positiivinen luku (itseisarvo).
	 * 
	 * @param		actualDifference		Toteutunut maaliero ottelussa.
	 * @param		expectedDifference		Ennakoitu maaliero ottelussa.
	 * 
	 * @return								Maalierovirhe.
	 */
	public double calculateGoalDifferenceError(double actualDifference, double expectedDifference)
	{
		double difference = Math.abs(actualDifference - expectedDifference);
		
		return difference;
	}
	
	/*
	 * Metodi laskee odotetun maalieron ottelussa.
	 * 
	 * @param		expectedHomeTeamDifference		Kotijoukkueen ennakoitu maaliero.
	 * @param		expectedAwayTeamDifference		Vierasjoukkueen ennakoitu maaliero.
	 * 
	 * @return										Odotettu maaliero.
	 */
	public double calculateExpectedGoalDifference(double expectedHomeTeamDifference, double expectedAwayTeamDifference)
	{
		double difference = 0.0;
		
		difference = expectedHomeTeamDifference - expectedAwayTeamDifference;
		
		return difference;
	}
	
	/*
	 * Metodi alustaa joukkueet ja niiden pi-luvut kautta varten.
	 * 
	 * @return			HashMap -olio, jonka indeksinä on joukkueen ID ja varsinaisena tietona joukkueen alustettu PiRating -olio.
	 */
	public HashMap<Integer, PiRating> initializeTeamRatings()
	{
		Connection connection 			    = getConnection();
		Statement statement   			    = null;
		ResultSet results	  			    = null;
		HashMap<Integer, PiRating> ratings = new HashMap<Integer, PiRating>();
		
		if (connection != null) 
		{ 
			try
			{
				statement = connection.createStatement();
				// Haetaan joukkueiden ID:t tietokannasta.
				results   = statement.executeQuery("SELECT home_team FROM games GROUP BY home_team;");
				
				while (results.next()) 
				{
					PiRating rating = new PiRating();
					int teamID = results.getInt("home_team");
					rating.setTeamID(teamID);
					ratings.put(teamID, rating);
				}
				
				connection.close();
			}
			catch(SQLException ex) {}
		}
		
		return ratings;
	}
}
