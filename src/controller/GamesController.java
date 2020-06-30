package controller;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import model.*;
import java.util.*;
import java.sql.Connection;

/*
 * Tämä luokka huolehtii kaikkien simulointien käynnistämisestä ja itse simuloinnistakin, eri Controller-luokkien olioita hyödyntäen.
 */
public class GamesController extends Controller
{
	private static GamesController instance = null;
	private Statement statement    		    = null;
	private ResultSet results      			= null;
	// Elo-lukujen tallentamista edelliseltä kaudelta varten.
	private HashMap<Integer, EloRating> previousYearsRatings = null;
	// true = edellisen vuoden elo-luvut on laskettu, false = edellisen vuoden elo-lukuja ei ole vielä laskettu, tai ei lasketa ollenkaan.
	boolean recorded					= false;
	
	// Singleton suunnittelumallin mahdollistamiseksi.
	protected GamesController() {}
	
	/*
	 * Metodi, joka tarkistaa, onko luokasta jo olemassa oliota. Jos ei ole, se luodaan ja palautetaan kutsujalle.
	 * Jos olio on jo olemassa, palautetaan tämä olio kutsujalle. Toteutetaan siis Singleton-suunnittelumalli.
	 * 
	 * @return 		Palautetaan GamesController-olio kutsujalle.
	 */
	public static GamesController getInstance()
	{
		// Jos tämä on ensimmäinen kerta kun tätä kyseistä luokkaa kutsutaan, luodaan olio.
		// Sen sijaan, jos tästä luokasta on jo olemassa olio/instanssi, palautetaan se kutsujalle.
		if (instance == null)
		{
			instance = new GamesController();
		}
		
		return instance;
	}
	
	/*
	 * Suoritetaan valitun kauden simulointi aggregaatti-mallia hyödyntäen.
	 * 
	 * @param		season			Kausi, jota halutaan simuloida.
	 * @param		whatToPlay		0 = lyödään vetoa kaikista lopputuloksista. 1 = lyödään vetoa vain algoritmin kotivoitoiksi ennustamista otteluista.
	 * 								2 = lyödään vetoa vain algoritmin vierasvoitoiksi ennustamista otteluista. 3 = lyödään vetoa vain algoritmin tasapeleiksi.
	 * @param		wallet			Pelikassa-olio. 
	 * 
	 * @return						Metodi palauttaa Counter-olion, joka sisältää kaikki tiedot simuloinneista sekä myös Wallet-olion.
	 */
	public Counter aggregate(String season, int whatToPlay, Wallet wallet)
	{
		Counter counter							= Counter.getInstance();
		Odd odd 				     			= new Odd();
		PiRatingsController piController   		= PiRatingsController.getInstance();
		// Alustetaan joukkueet pi-hockey -mallia varten.
		HashMap<Integer, PiRating> piRatings	= piController.initializeTeamRatings();
		EloRatingsController eloC 		    	= EloRatingsController.getInstance();
		// Alustetaan joukkueet Elo-mallia varten.
		HashMap<Integer, EloRating> eloRatings 	= eloC.initializeTeamRatings();
		Vector<Double> expectedPoints			= null;
		Vector<Double>probabilities	 			= null;
		Game game								= new Game();
		int toPlayOrNotToPlay 			   		= 0;
			
		Team team = new Team();
		counter.initialize();
		
		try
		{
			Connection connection = getConnection(); 
			statement = connection.createStatement();
			// Haetaan kauden kaikki ottelut tietoineen tietokannasta.
			results   = statement.executeQuery("SELECT game_id, date, home_team, away_team, home_goals, away_goals FROM games WHERE season = '" + season + "' ORDER BY date ASC;");
			
			while (results.next())
			{
				// Kotijoukkueen ID.
				int homeTeamID = results.getInt("home_team");
				// Vierasjoukkueen ID.
				int awayTeamID = results.getInt("away_team");
				// Kotijoukkueen maalit.
				int homeGoals = results.getInt("home_goals");
				// Vierasjoukkueen maalit.
				int awayGoals = results.getInt("away_goals");
				// Ottelun päivämäärä.
				String date = results.getString("date");
				
				int actualGoalDifference = 0;
				
				// ***************** Pi-rating *********************
				// Kotijoukkueen tämän hetkinen koti-pi-luku.
				PiRating homeRating = piRatings.get(homeTeamID);
				// Vierasjoukkueen tämän hetkinen koti-pi-luku.
				PiRating awayRating = piRatings.get(awayTeamID);

				// Määritellään toteutunut maaliero ottelussa.
				if (homeGoals > awayGoals) 
				{ 
					actualGoalDifference = homeGoals - awayGoals; 
				}
				else
				{
					if (homeGoals < awayGoals)
					{
						actualGoalDifference = awayGoals - homeGoals;
					}
					else
					{
						actualGoalDifference = 0;
					}
				}
				
				double expectedGoalDifference = 0.0;
				// Lasketaan pi-lukujen perusteella odotettu maaliero joukkueiden välillä.
				expectedGoalDifference = piController.calculateExpectedGoalDifference(homeRating.calculateExpectedHomeGoalDifference(), awayRating.calculateExpectedAwayGoalDifference());
				// Lasketaan todellisen maalieron ja oletetun maalieron virhe.
				double error = piController.calculateGoalDifferenceError(actualGoalDifference, expectedGoalDifference);
				// Lasketaan painotettu maalierovirhe.
				double weightedError = piController.calculateWeightedGoalError(error);
				// Lasketaan kotijoukkueen maalierovirhe.
				double homeError = piController.calculateHomeError(actualGoalDifference, expectedGoalDifference, weightedError);
				// Lasketaan vierasjoukkueen maalierovirhe. 
				double awayError = piController.calculateAwayError(actualGoalDifference, expectedGoalDifference, weightedError);
				
				// Päivitetään kotijoukkueen koti- ja vieras-pi-luvut.
				homeRating.updateHomeRating(homeError);
				// Päivitetään vierasjoukkueen koti- ja vieras-pi-luvut.
				awayRating.updateAwayRating(awayError);
				
				Vector<Double> piProbabilities = null;
				// Muodostetaan todennäköisyydet ottelun eri lopputulosvaihtoehdoille.
				piProbabilities = piController.formProbabilities(expectedGoalDifference, season);
				// ***************** Pi-rating loppuu *********************
				
				// ***************** Poisson *******************
				// Lasketaan kotijoukkueen kotiotteluissa tehtyjen maalien keskiarvo kaudella tähän mennessä.
				double homeMean = team.getMean(homeTeamID, season, 1, date, -1);
				// Lasketaan vierasjoukkueen vierasotteluissa tehtyjen maalien keskiarvo kaudella tähän mennessä.
				double awayMean = team.getMean(awayTeamID, season, 2, date, -1);
				
				PoissonController m = new PoissonController();
				// Muodostetaan todennäköisyydet eri tulosvaihtoehdoille (0-0, 0-1, 0-2... 10-10)
				Vector<Double>poissonProbabilities = m.formProbabilities(homeMean, awayMean);
				// ***************** Poisson loppuu *********************
				
				// ***************** Elo-rating *******************
				EloRating homeEloRating = null;
				EloRating awayEloRating = null;
				
				// Kotijoukkueen elo-luku.
				homeEloRating = eloRatings.get(homeTeamID);
				// Vierasjoukkueen elo-luku.
				awayEloRating = eloRatings.get(awayTeamID);
				
				expectedPoints = eloC.countExpectedPoints(homeEloRating.getEloRating(), awayEloRating.getEloRating());
				double actualHomePoints = 0.0;
				double actualAwayPoints = 0.0;
				
				// Määritetään toteutuneet pisteet ottelussa.
				// Jos ottelu päättyi kotivoittoon.
				if (homeGoals > awayGoals)
				{
					actualHomePoints = 1.0;
					actualAwayPoints = 0.0;
				}
				// Jos ottelu päättyi tasapeliin.
				if (homeGoals == awayGoals)
				{
					actualHomePoints = 0.5;
					actualAwayPoints = 0.5;
				}
				// Jos ottelu päättyi vierasvoittoon.
				if (homeGoals < awayGoals)
				{
					actualHomePoints = 0.0;
					actualAwayPoints = 1.0;
				}
				
				// Päivitetään kotijoukkueen elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
				homeEloRating.updateEloRating(expectedPoints.get(0), actualHomePoints);
				// Päivitetään vierasjoukkueen elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
				awayEloRating.updateEloRating(expectedPoints.get(1), actualAwayPoints);
				Vector<Double> eloProbabilities = new Vector<Double>();
				eloProbabilities = eloC.getProbabilities(season, homeEloRating.getEloRating(), awayEloRating.getEloRating(), expectedPoints.get(0), expectedPoints.get(1));
				// ***************** Elo-rating loppuu *****************
				
				// Lasketaan todennäköisyydet lopputuloksille.
				probabilities = game.formAggregateProbabilities(piProbabilities, eloProbabilities, poissonProbabilities);
				
				// Ottelun ID
				String gameID 				 = results.getString("game_id");
				// Haetaan parhaat kertoimet kullekin ottelun lopputulokselle tietokannasta.
				Vector<Double> odds 	     = odd.getBestOdds(gameID);
				Vector<Double> oddProbs      = new Vector<Double>();
				
				// Muuta kertoimet todennäköisyyksiksi.
				if (odds != null)
				{
					// Kertoimet todennäköisyyksinä.
					oddProbs = odd.allOddsToProbabilities(odds);
					
					// Vertaa kertoimien antamia todennäköisyyksiä algoritmin antamiin todennäköisyyksiin.
					// Metodin palauttamien arvojen merkitykset: 
					// 0 = ei pelattavaa, 1 = pelaa kotivoittoa, 2 = pelaa vierasvoitto, 3 = pelaa tasapeliä
					toPlayOrNotToPlay = odd.toPlayOrNotToPlay(oddProbs, probabilities, whatToPlay);
				}
				
				// Tarkistetaan, osuiko algoritmin antama ennustus oikeaan.
				game.check(season, results.getString("date"), homeTeamID, awayTeamID, homeGoals, awayGoals, probabilities, counter);
				
				// Jos joku algoritmin antama todennäköisyys on suurempi kuin kertoimen, lyödään vetoa. 
				if (toPlayOrNotToPlay > 0)
				{
					// Otetaan oikean merkin (1 X 2) kerroin ja oikea todennäköisyys.
					double correctOdds = 0.0;
					double correctProb = 0.0;
					int bet = toPlayOrNotToPlay;
					
					// Jos lyödään vetoa kotivoiton puolesta.
					if (toPlayOrNotToPlay == 1) { correctOdds = odds.get(0); correctProb = probabilities.get(0); }
					// Jos lyödään vetoa vierasvoiton puolesta.
					if (toPlayOrNotToPlay == 3) { correctOdds = odds.get(1); correctProb = probabilities.get(1); }
					// Jos lyödään vetoa t puolesta.
					if (toPlayOrNotToPlay == 2) { correctOdds = odds.get(2); correctProb = probabilities.get(2); }
					
					// Lyödään vetoa.
					odd.bet(correctOdds, correctProb, wallet, bet, homeGoals, awayGoals);
				}
			}

			connection.close();
		}
		catch(SQLException ex) {}
		
		return counter;
	}
	
	/*
	 * Suoritetaan valitun kauden simulointi pi-hockey -mallia hyödyntäen.
	 * 
	 * @param		season			Kausi, jota halutaan simuloida.
	 * @param		whatToPlay		0 = lyödään vetoa kaikista lopputuloksista. 1 = lyödään vetoa vain algoritmin kotivoitoiksi ennustamista otteluista.
	 * 								2 = lyödään vetoa vain algoritmin vierasvoitoiksi ennustamista otteluista. 3 = lyödään vetoa vain algoritmin tasapeleiksi.
	 * @param		wallet			Pelikassa-olio. 
	 * 
	 * @return						Metodi palauttaa Counter-olion, joka sisältää kaikki tiedot simuloinneista sekä myös Wallet-olion.
	 */
	public Counter formPiRatings(String season, int whatToPlay, Wallet wallet)
	{
		Counter counter						= Counter.getInstance();
		PiRatingsController piController   	= PiRatingsController.getInstance();
		// Alustetaan joukkuelistaus ja joukkueiden pi-luvut.
		HashMap<Integer, PiRating> ratings 	= piController.initializeTeamRatings();
		int toPlayOrNotToPlay 			   	= 0;
		
		// Alustetaan Counter-olio.
		counter.initialize();
		
		try
		{
			Connection connection = getConnection();
			statement = connection.createStatement();
			// Haetaan kauden jokaisen ottelun tiedot tietokannasta.
			results   = statement.executeQuery("SELECT game_id, date, home_team, away_team, home_goals, away_goals FROM games WHERE season = '" + season + "' ORDER BY date ASC;");
			
			while (results.next())
			{
				// Kotijoukkueen ID.
				int homeTeamID = results.getInt("home_team");
				// Vierasjoukkueen ID.
				int awayTeamID = results.getInt("away_team");
				// Kotijoukkueen maalit.
				int homeGoals = results.getInt("home_goals");
				// Vierasjoukkueen maalit.
				int awayGoals = results.getInt("away_goals");
				// Ottelun päivämäärä.
				
				int actualGoalDifference = 0;
				
				// Joukkuiden tämän hetkiset pi-luvut.
				PiRating homeRating = ratings.get(homeTeamID);
				PiRating awayRating = ratings.get(awayTeamID);
				
				// Määritelläänt toteutunut maaliero ottelussa.
				if (homeGoals > awayGoals) 
				{ 
					actualGoalDifference = homeGoals - awayGoals; 
				}
				else
				{
					if (homeGoals < awayGoals)
					{
						actualGoalDifference = awayGoals - homeGoals;
					}
					else
					{
						actualGoalDifference = 0;
					}
				}
				
				double expectedGoalDifference = 0.0;
				// Lasketaan pi-lukujen perusteella odotettu maaliero joukkueiden välillä.
				expectedGoalDifference = piController.calculateExpectedGoalDifference(homeRating.calculateExpectedHomeGoalDifference(), awayRating.calculateExpectedAwayGoalDifference());
				// Lasketaan todellisen maalieron ja oletetun maalieron virhe.
				double error = piController.calculateGoalDifferenceError(actualGoalDifference, expectedGoalDifference);
				// Lasketaan painotettu maalierovirhe.
				double weightedError = piController.calculateWeightedGoalError(error);
				// Lasketaan kotijoukkueen maalierovirhe.
				double homeError = piController.calculateHomeError(actualGoalDifference, expectedGoalDifference, weightedError);
				// Lasketaan vierasjoukkueen maalierovirhe. 
				double awayError = piController.calculateAwayError(actualGoalDifference, expectedGoalDifference, weightedError);
				
				// Päivitetään kotijoukkueen koti- ja vieras-pi-luvut.
				homeRating.updateHomeRating(homeError);
				// Päivitetään vierasjoukkueen koti- ja vieras-pi-luvut.
				awayRating.updateAwayRating(awayError);
				
				// Ottelun ID
				String gameID 				 = results.getString("game_id");
				Odd odd 				     = new Odd();
				// Haetaan tietokannasta paras kerroin kullekin lopputulokselle.
				Vector<Double> odds 	     = odd.getBestOdds(gameID);
				Vector<Double> oddProbs      = new Vector<Double>();
				Vector<Double> probabilities = null;
				
				// Muodostetaan todennäköisyydet ottelun eri lopputulosvaihtoehdoille.
				probabilities = piController.formProbabilities(expectedGoalDifference, season);
				
				// Muuta kertoimet todennäköisyyksiksi.
				if (odds != null)
				{
					oddProbs = odd.allOddsToProbabilities(odds);
					
					// Vertaa kertoimien antamia todennäköisyyksiä algoritmin antamiin todennäköisyyksiin.
					// Metodin palauttamien arvojen merkitykset: 
					// 0 = ei pelattavaa, 1 = pelaa kotivoittoa, 2 = pelaa vierasvoitto, 3 = pelaa tasapeliä
					toPlayOrNotToPlay = odd.toPlayOrNotToPlay(oddProbs, probabilities, whatToPlay);
				}
				
				// Tarkistetaan, osuiko algoritmin antama ennustus oikeaan.
				Game game 		  = new Game();
				game.check(season, results.getString("date"), homeTeamID, awayTeamID, homeGoals, awayGoals, probabilities, counter);
				
				// Jos joku algoritmin antama todennäköisyys on suurempi kuin kertoimen, lyödään vetoa. 
				if (toPlayOrNotToPlay > 0)
				{
					// Otetaan oikean merkin (1 X 2) kerroin ja oikea todennäköisyys.
					double correctOdds = 0.0;
					double correctProb = 0.0;
					int bet = toPlayOrNotToPlay;
					
					// Jos lyödään vetoa kotivoiton puolesta.
					if (toPlayOrNotToPlay == 1) { correctOdds = odds.get(0); correctProb = probabilities.get(0); }
					// Jos lyödään vetoa vierasvoiton puolesta.
					if (toPlayOrNotToPlay == 3) { correctOdds = odds.get(1); correctProb = probabilities.get(1); }
					// Jos lyödään vetoa t puolesta.
					if (toPlayOrNotToPlay == 2) { correctOdds = odds.get(2); correctProb = probabilities.get(2); }
					
					// Lyödään vetoa.
					odd.bet(correctOdds, correctProb, wallet, bet, homeGoals, awayGoals);
				}
			}

			connection.close();
		}
		catch(SQLException ex) {}
		
		return counter;
	}
	
	/*
	 * Suoritetaan valitun kauden simulointi Elo-mallia hyödyntäen.
	 * 
	 * @param		season					Kausi, jota simuloidaan.
	 * @param		whatToPlay				0 = lyödään vetoa kaikista lopputuloksista. 1 = lyödään vetoa vain algoritmin kotivoitoiksi ennustamista otteluista.
	 * 										2 = lyödään vetoa vain algoritmin vierasvoitoiksi ennustamista otteluista. 3 = lyödään vetoa vain algoritmin tasapeleiksi 
	 * 										ennustamista otteluista. (Tätä vaihtoehtoa ei voi valita käyttöliittymästä, olemassa vain kehitysvaiheen testausta varten).
	 * @param		wallet					Pelikassa-olio.
	 * @param		homeAway				true = lasketaan joka joukkueelle Elo-luvut erikseen sekä koti- että vierasotteluille. false = lasketaan joka joukkueelle
	 * 										vain yksi Elo-luku, jota käytetään sekä koti- että vierasotteluissa.
	 * @param		goalBased				true = käytetään maaliperustaista Elo-mallia simuloimiseen. false = käytetään perus-Elo-mallia simuloimiseen.
	 * @param		useWeightedEloRatings	true = käytetään painotettuja Elo-lukuja simuloimiseen. false = käytetään painottamattomia Elo-lukuja simuloimiseen.
	 * @return								Metodi palauttaa Counter-olion, joka sisältää kaikki tiedot simuloinneista sekä myös Wallet-olion.
	 */
	public Counter formEloRatings(String season, int whatToPlay, Wallet wallet, boolean homeAway, boolean goalBased, boolean useWeightedEloRatings)
	{
		Counter counter						= Counter.getInstance();
		Odd odd 				     	    = new Odd();
		EloRatingsController eloC 		    = EloRatingsController.getInstance();
		HashMap<Integer, EloRating> ratings = null;
		
		int toPlayOrNotToPlay 				= 0;
		Vector<Double> expectedPoints		= null;
		String previousSeason				= "";
		
		// Alustetaan Counter-olio.
		counter.initialize();
		
		try
		{
			Connection connection = getConnection();
			statement = connection.createStatement();
			
			// Alustetaan joukkueiden elo-luvut.
			// Ei käytetä painotettuja Elo-lukuja.
			if (useWeightedEloRatings == false)
			{
				ratings  = eloC.initializeTeamRatings();
				recorded = true;
			}
			else
			{
				// Jos edellisen kauden, eli painotetut Elo-luvut on jo laskettu, tai ei käytetä ollenkaan painotettuja Elo-lukuja.
				if (this.recorded)
				{
					// Tallennetaan painotetut Elo-luvut kauden alun Elo-luvuiksi.
					ratings = this.previousYearsRatings;
				}
				else
				{
					// Jos painotettuja Elo-lukuja ei ole vielä muodostettu, alustetaan joukkuelistaus ja Elo-luvut.
					previousYearsRatings = eloC.initializeTeamRatings();
				}
			}
			
			// Jos painotettuja Elo-lukuja ei ole vielä laskettu.
			if (recorded == false)
			{
				// Määritellään käyttäjän valitseman kauden edellinen kausi. 
				// Vain kausille 2008-2012 lasketaan elo-luvut, koska vain näille kausille löytyy tietokannasta kertoimet.
				if (season.equals("2008-2009")) { previousSeason = "2007-2008"; }
				if (season.equals("2009-2010")) { previousSeason = "2008-2009"; }
				if (season.equals("2010-2011")) { previousSeason = "2009-2010"; }
				if (season.equals("2011-2012")) { previousSeason = "2010-2011"; }
				
				// Haetaan edellisen kauden kaikki ottelut tietokannasta.
				results = statement.executeQuery("SELECT game_id, date, home_team, away_team, home_goals, away_goals FROM games WHERE season = '" + previousSeason + "' ORDER BY date ASC;");
			}
			else // Jos painotetut Elo-luvut on jo muodostettu tai ei käytetä ollenkaan painotettuja Elo-lukuja.
			{
				// Haetaan kauden kaikki pelit tietokannasta.
				results = statement.executeQuery("SELECT game_id, date, home_team, away_team, home_goals, away_goals FROM games WHERE season = '" + season + "' ORDER BY date ASC;");
			}
			
			// Käydään kaikki pelit läpi.
			while (results.next())
			{
				// Kotijoukkueen ID.
				int homeTeamID = results.getInt("home_team");
				// Vierasjoukkueen ID.
				int awayTeamID = results.getInt("away_team");
				// Kotijoukkueen maalit.
				int homeGoals = results.getInt("home_goals");
				// Vierasjoukkueen maalit.
				int awayGoals = results.getInt("away_goals");
				// Kotijoukkueen todelliset pisteet ottelusta.
				double actualHomePoints = 0.0;
				// Vierasjoukkueen todelliset pisteet ottelusta.
				double actualAwayPoints = 0.0;
				
				EloRating homeRating = null;
				EloRating awayRating = null;
				
				// Jos edellisen kauden Elo-luvut on jo laskettu tai ei käytetä ollenkaan painotettuja Elo-lukuja.
				if (this.recorded)
				{
					// Kotijoukkueen Elo-luku.
					homeRating = ratings.get(homeTeamID);
					// Vierasjoukkueen Elo-luku.
					awayRating = ratings.get(awayTeamID);
				}
				else
				{
					// Kotijoukkueen Elo-luku.
					homeRating = this.previousYearsRatings.get(homeTeamID);
					// Vierasjoukkueen Elo-luku.
					awayRating = this.previousYearsRatings.get(awayTeamID);
				}
				
				// Ottelun ID
				String gameID = results.getString("game_id");
												
				// Lasketaan tämän hetkisten Elo-lukujen perusteella odotettu pistemäärä koti- ja vierasjoukkueelle.
				if (homeAway) // Jos lasketaan erilliset Elo-luvut koti- ja vierasotteluille.
				{
					expectedPoints = eloC.countExpectedPoints(homeRating.getHomeEloRating(), awayRating.getAwayEloRating());
				}
				else
				{
					expectedPoints = eloC.countExpectedPoints(homeRating.getEloRating(), awayRating.getEloRating());
				}
				
				// Jos ottelu päättyi kotivoittoon.
				if (homeGoals > awayGoals)
				{
					actualHomePoints = 1.0;
					actualAwayPoints = 0.0;
				}
				// Jos ottelu päättyi tasapeliin.
				if (homeGoals == awayGoals)
				{
					actualHomePoints = 0.5;
					actualAwayPoints = 0.5;
				}
				// Jos ottelu päättyi vierasvoittoon.
				if (homeGoals < awayGoals)
				{
					actualHomePoints = 0.0;
					actualAwayPoints = 1.0;
				}
				
				// Jos lasketaan erilliset Elo-luvut koti- ja vierasotteluille.
				if (homeAway)
				{
					// Jos käytetään maaliperustaista Elo-mallia.
					if (goalBased)
					{
						// Päivitetään kotijoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						homeRating.updateGoalBasedHomeEloRating(homeGoals - awayGoals);
						// Päivitetään vierasjoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						awayRating.updateGoalBasedAwayEloRating(awayGoals - homeGoals);
					}
					else
					{
						// Päivitetään kotijoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						homeRating.updateHomeEloRating(expectedPoints.get(0), actualHomePoints);
						// Päivitetään vierasjoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						awayRating.updateAwayEloRating(expectedPoints.get(1), actualAwayPoints);
					}
				}
				else
				{
					// Jos käytetään maaliperustaista Elo-mallia.
					if (goalBased)
					{
						// Päivitetään kotijoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						homeRating.updateGoalBasedEloRating(homeGoals - awayGoals);
						// Päivitetään vierasjoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						awayRating.updateGoalBasedEloRating(awayGoals - homeGoals);
					}
					else
					{
						// Päivitetään kotijoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						homeRating.updateEloRating(expectedPoints.get(0), actualHomePoints);
						// Päivitetään vierasjoukkueen Elo-luku toteutuneiden ja odotettujen pisteiden perusteella.
						awayRating.updateEloRating(expectedPoints.get(1), actualAwayPoints);
					}
				}
				
				// Jos edellisen kauden Elo-luvut on jo laskettu tai ei käytetä ollenkaan painotettuja Elo-lukuja.
				if (this.recorded)
				{	
					Vector<Double> odds 	     = odd.getBestOdds(gameID);
					Vector<Double> oddProbs      = new Vector<Double>();
					Vector<Double> probabilities = null;
						
					// Muodostetaan todennäköisyydet ottelun eri lopputulosvaihtoehdoille.
					if (homeAway) // Jos lasketaan erilliset Elo-luvut koti- ja vierasotteluille.
					{
						probabilities = eloC.getProbabilities(season, homeRating.getHomeEloRating(), awayRating.getAwayEloRating(), expectedPoints.get(0), expectedPoints.get(1));
					}
					else
					{
						probabilities = eloC.getProbabilities(season, homeRating.getEloRating(), awayRating.getEloRating(), expectedPoints.get(0), expectedPoints.get(1));
					}
					
					// Muuta kertoimet todennäköisyyksiksi.
					if (odds != null)
					{
						// Kertoimet todennäköisyyksinä.
						oddProbs = odd.allOddsToProbabilities(odds);
						
						// Vertaa kertoimien antamia todennäköisyyksiä algoritmin antamiin todennäköisyyksiin.
						// Metodin palauttamien arvojen merkitykset: 
						// 0 = ei pelattavaa, 1 = pelaa kotivoittoa, 2 = pelaa vierasvoitto, 3 = pelaa tasapeliä
						toPlayOrNotToPlay = odd.toPlayOrNotToPlay(oddProbs, probabilities, whatToPlay);
					}
					
					// Tarkistetaan, osuiko algoritmin antama ennustus oikeaan.
					Game game 		  = new Game();
					game.check(season, results.getString("date"), homeTeamID, awayTeamID, homeGoals, awayGoals, probabilities, counter);
					
					// Jos joku algoritmin antama todennäköisyys on suurempi kuin kertoimen, lyödään vetoa. 
					if (toPlayOrNotToPlay > 0)
					{
						// Otetaan oikean merkin (1 X 2) kerroin ja oikea todennäköisyys.
						double correctOdds = 0.0;
						double correctProb = 0.0;
						int bet = toPlayOrNotToPlay;
						
						// Jos lyödään vetoa kotivoiton puolesta.
						if (toPlayOrNotToPlay == 1) { correctOdds = odds.get(0); correctProb = probabilities.get(0); }
						// Jos lyödään vetoa vierasvoiton puolesta.
						if (toPlayOrNotToPlay == 3) { correctOdds = odds.get(1); correctProb = probabilities.get(1); }
						// Jos lyödään vetoa t puolesta.
						if (toPlayOrNotToPlay == 2) { correctOdds = odds.get(2); correctProb = probabilities.get(2); }
						
						// Lyödään vetoa.
						odd.bet(correctOdds, correctProb, wallet, bet, homeGoals, awayGoals);
					}
				}
			}

			connection.close();
		}
		catch(SQLException ex) {}
		
		if (recorded)
		{
		}
		else // Kun painotetut Elo-luvut on laskettu.
		{
			this.recorded = true;
			// Kutsutaan rekursiivisesti tätä metodia uudestaan. Painotetut Elo-luvut ovat tallennettuna.
			this.formEloRatings(season, whatToPlay, wallet, homeAway, goalBased, useWeightedEloRatings);
		}
		
		return counter;
	}
	
	/*
	 * Suoritetaan valitun kauden simulointi Poisson-mallia hyödyntäen.
	 * Poisson-mallia käyttäen. 
	 * 
	 * @param		season				Kausi, jota simuloidaan.
	 * @param		lastGamesAmount		Kuinka monta joukkueiden viimeisintä peliä huomioidaan maalikeskiarvoja laskiessa. 0 = huomioidaan kaikki ottelut.
	 * 									Muina vaihtoehtoina ovat 1-6 ottelua.
	 * @param		whatToPlay			0 = lyödään vetoa kaikista lopputuloksista. 1 = lyödään vetoa vain algoritmin kotivoitoiksi ennustamista otteluista.
	 * 									2 = lyödään vetoa vain algoritmin vierasvoitoiksi ennustamista otteluista. 3 = lyödään vetoa vain algoritmin tasapeleiksi 
	 * 									ennustamista otteluista. (Tätä vaihtoehtoa ei voi valita käyttöliittymästä, olemassa vain kehitysvaiheen testausta varten).
	 * @param		wallet				Pelikassa-olio.
	 * @return							Metodi palauttaa Counter-olion, joka sisältää kaikki tiedot simuloinneista sekä myös Wallet-olion.
	 */
	public Counter poisson(String season, int lastGamesAmount, int whatToPlay, Wallet wallet)
	{
		Counter counter = Counter.getInstance();
		Game game = new Game();
		Team team = new Team();
		
		// Alustetan pelikassa.
		wallet.setCountersZero();
		counter.initialize();
		
		try
		{
			Connection connection = getConnection();
			statement = connection.createStatement();
			
			// Haetaan kauden kaikki pelit tietokannasta.
			results   = statement.executeQuery("SELECT game_id, home_team, away_team, home_goals, away_goals, date FROM games WHERE season = '" + season + "' ORDER BY date ASC;");
			
			while (results.next())
			{
				// Kotijoukkueen ID.
				int homeTeamID = results.getInt("home_team");
				// Vierasjoukkueen ID.
				int awayTeamID = results.getInt("away_team");
				// Päivämäärä, jolloin ottelu pelattiin.
				String date    = results.getString("date");
				
				// Lasketaan kotijoukkueen kotiotteluissa tehtyjen maalien keskiarvo kaudella tähän mennessä.
				double homeMean = team.getMean(homeTeamID, season, 1, date, lastGamesAmount);
				// Lasketaan vierasjoukkueen vierasotteluissa tehtyjen maalien keskiarvo kaudella tähän mennessä.
				double awayMean = team.getMean(awayTeamID, season, 2, date, lastGamesAmount);
				
				// homeMean/awayMean on 0.0 silloin, kun koti/vieras-joukkue ei ole pelannut vähintään kolmea koti/vieras-ottelua ennen kyseistä ottelua. 
				if (homeMean > 0.0 && awayMean > 0.0)
				{
					String gameID = results.getString("game_id");
					int homeGoals = results.getInt("home_goals");
					int awayGoals = results.getInt("away_goals");
					int toPlayOrNotToPlay = 0;
					
					PoissonController m = new PoissonController();
					// Muodostetaan todennäköisyydet eri tulosvaihtoehdoille (0-0, 0-1, 0-2... 10-10)
					Vector<Double>probabilities = m.formProbabilities(homeMean, awayMean);
					
					// Hae ottelun kertoimet tietokannasta, jos kausi on > 2007-2008
					Odd odd = new Odd();
					Vector<Double> odds = odd.getBestOdds(gameID);
					Vector<Double> oddProbs = new Vector<Double>();
					// Muutetaan kertoimet todennäköisyyksiksi.
					if (odds != null)
					{
						oddProbs = odd.allOddsToProbabilities(odds);
						
						// Vertaa kertoimien antamia todennäköisyyksiä algoritmin antamiin todennäköisyyksiin.
						// Metodin palauttamien arvojen merkitykset: 
						// 0 = ei pelattavaa, 1 = pelaa kotivoittoa, 2 = pelaa vierasvoitto, 3 = pelaa tasapeliä
						toPlayOrNotToPlay = odd.toPlayOrNotToPlay(oddProbs, probabilities, whatToPlay);
					}
					
					// Tarkistetaan, osuiko algoritmin antama ennustus oikeaan.
					game.check(season, date, homeTeamID, awayTeamID, homeGoals, awayGoals, probabilities, counter);
					
					// Jos joku algoritmin antama todennäköisyys on suurempi kuin kertoimen, lyödään vetoa. 
					if (toPlayOrNotToPlay > 0)
					{
						// Otetaan oikean merkin (1 X 2) kerroin ja oikea todennäköisyys.
						double correctOdds = 0.0;
						double correctProb = 0.0;
						int bet = toPlayOrNotToPlay;
						
						// Jos lyödään vetoa kotivoiton puolesta.
						if (toPlayOrNotToPlay == 1) { correctOdds = odds.get(0); correctProb = probabilities.get(0); }
						// Jos lyödään vetoa vierasvoiton puolesta.
						if (toPlayOrNotToPlay == 3) { correctOdds = odds.get(1); correctProb = probabilities.get(1); }
						// Jos lyödään vetoa t puolesta.
						if (toPlayOrNotToPlay == 2) { correctOdds = odds.get(2); correctProb = probabilities.get(2); }
						
						// Lyödään vetoa.
						odd.bet(correctOdds, correctProb, wallet, bet, homeGoals, awayGoals);
					}
				}
			}

			connection.close();
		}
		catch(SQLException ex) {}
		
		return counter;
	}
}
