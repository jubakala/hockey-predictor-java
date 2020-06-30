package model;

import java.util.Vector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/*
 * Luokka huolehtii kertoimiin ja vedonlyöntiin liittyvistä toiminnoista.
 */
public class Odd extends Model
{
	/*
	 * Metodi simuloi vedonlyöntiä.
	 * 
	 * @param		odds			Vedonlyöntitoimiston kertoimet todennäköisyysarvioina.
	 * @param		probability		Todennäköisyys lopputulokselle, josta lyödään vetoa.
	 * @param		wallet			Pelikassa-olio.
	 * @param		bet				lopputulos, josta lyödään vetoa. 1 = kotivoitto, 2 = vierasvoitto, 3 = tasapeli.
	 * @param		homeGoals		Toteutuneet kotijoukkueen maalit ottelussa.
	 * @param		awayGoals		Toteutuneet vierasjoukkueen maalit ottelussa..
	 */
	public void bet(double odds, double probability, Wallet wallet, int bet, int homeGoals, int awayGoals)
	{
		// 1 = kotivoitto, 2 = vierasvoitto, 3 = tasapeli.
		int correctOutcome = 1;
		if (homeGoals == awayGoals) { correctOutcome = 3; }
		if (homeGoals < awayGoals)  { correctOutcome = 2; }
		
		// Kellyn kaava = Panostettava % pelikassasta = ((kerroin * todennäköisyys - 1) / (kerroin - 1)) / Kellyn jakaja.
		double stake = 0.0;
		stake   	 = (odds * probability - 1) / (odds - 1);
		// Kellyn jakaja on tallennettuna wallet-olioon.
		stake 		 = stake / wallet.getKellyDivider();
		
		// Vähennetään panos pelikassasta.
		wallet.substractStake(stake);
		
		// Veto meni oikein.
		if (bet == correctOutcome)
		{
			// Voitto
			double won = odds * stake;
			// Lisätään voitto pelikassaan.
			wallet.addWin(won);
		}
	}
	
	/*
	 * Metodi muuttaa kaikkien lopputulosten kertoimet todennäköisyysarvioiksi.
	 * 
	 * @param		odds		Kertoimet.
	 * 
	 * @return					Todennäköisyysarviot.
	 */
	public Vector<Double> allOddsToProbabilities(Vector<Double> odds)
	{
		Vector<Double> probabilities = new Vector<Double>();
		
		double homeWinProb 		= oddsToProbability(odds.get(0));
		probabilities.add(homeWinProb);
		double drawProb    		= oddsToProbability(odds.get(1));
		probabilities.add(drawProb);
		double awayWinProb 		= oddsToProbability(odds.get(2));
		probabilities.add(awayWinProb);
		
		return probabilities;
	}
	
	/*
	 * Metodi päättelee vedonlyöntikertoimista johdettujen todennäköisyyksien ja algoritmin määrittämien todennäköisyyksien perusteella ensin,
	 * minkä lopputuloksen algoritmi ennustaa olevan todennäköisin ja tämän jälkeen, onko kyseiselle lopputulokselle tarjolla ylikerroin, eli
	 * onko algoritmin määrittämä todennäköisyys suurempi kuin vedonvälittäjän arvioima.
	 * 
	 * @param		odds			Vedonvälittäjän arvioimat todennäköisyydet ottelun lopputuloksille. Indeksissä nolla on kotivoiton todennäköisyys,
	 * 								indeksissä yksi on tasapelin todennäköisyys ja indeksissä kaksi on vierasvoiton todennäköisyys.
	 * @param		probabilities	Algoritmin määrittämät todennäköisyydet ottelun lopputuloksille. Indeksissä nolla on kotivoiton todennäköisyys,
	 * 								indeksissä yksi on tasapelin todennäköisyys ja indeksissä kaksi on vierasvoiton todennäköisyys.
	 * @param		whatToPlay		Käyttäjän määrittämä muuttuja joka voi saada arvot 0-3. 0 = pelataan kotivoittoja, vierasvoittoja ja tasapelejä, jos
	 * 								niihin on ylikerroin. 1 = pelataan vain ylikertoimisia kotivoittoja, ei muita, vaikka niihin löytyisikin ylikerroin.
	 * 								2 = pelataan vain ylikertoimisia vierasvoittoja. 3 = pelataan vain ylikertoimisia tasapelejä. Tämä on vain teoreettinen
	 * 								vaihtoehto, koska graafisesta käyttöliittymästä tätä vaihtoehtoa ei voi valita. Syynä tähän on, että algoritmi ennustaa
	 * 								ottelun päättyvän tasapeliin äärimmäisen harvoin.
	 * @return						0 = ei pelattavaa, eli ei lyödä vetoa. 1 = lyödään vetoa kotivoiton puolesta. 2 = lyödään vetoa vierasvoiton puolesta.
	 * 								3 = lyödään vetoa tasapelin puolesta.
	 */
	public int toPlayOrNotToPlay(Vector<Double> odds, Vector<Double> probabilities, int whatToPlay)
	{
		int toPlayOrNotToPlay = 0;
		
		// Vedonlyöntitoimiston todennäköisyydet.
		double homeWinProbability = odds.get(0);
		double drawProbability    = odds.get(1);
		double awayWinProbability = odds.get(2);
		
		// Algoritmin muodostamat todennäköisyydet.
		double homeWinAlgoProbability 	= probabilities.get(0);
		double drawAlgoProbability 		= probabilities.get(1);
		double awayWinAlgoProbability 	= probabilities.get(2);
		
		// Jos algoritmi ennustaa ottelun päättyvän kotivoittoon. Kotivoitoksi lasketaan myös tilanteet, joissa kotivoiton todennäköisyys on sama kuin 
		// vierasvoiton tai tasapelin todennäköisyys. Kotivoitto katsotaan näissä tilanteissa siis aina todennäköisimmäksi.
		if (homeWinAlgoProbability >= awayWinAlgoProbability && homeWinAlgoProbability >= drawAlgoProbability)
		{
			// Onko kotivoitolle ylikerroin. Ylikertoimeksi ei ajatella tilannetta, jossa algoritmin määrittämä todennäköisyys on sama kuin vedonvälittäjän
			// antama todennäköisyys.
			if (homeWinAlgoProbability > homeWinProbability)
			{
				// Jos käyttäjä haluaa lyödä vetoa kaikista lopputuloksista, tai vain kotivoitoista.
				if (whatToPlay == 0 || whatToPlay == 1)
				{
					toPlayOrNotToPlay = 1;
				}
			}
		}
		else
		{
			// Jos algoritmi ennustaa ottelun päättyvän vierasvoittoon. Jos vierasvoiton ja tasapelin todennäköisyys on sama, ajatellaan vierasvoiton
			// olevan todennäköisempi lopputulos.
			if (awayWinAlgoProbability > homeWinAlgoProbability && awayWinAlgoProbability >= drawAlgoProbability)
			{
				// Onko vierasvoitolle ylikerroin. Ylikertoimeksi ei ajatella tilannetta, jossa algoritmin määrittämä todennäköisyys on sama kuin vedonvälittäjän
				// antama todennäköisyys.
				if (awayWinAlgoProbability > awayWinProbability)
				{
					// Jos käyttäjä haluaa lyödä vetoa kaikista lopputuloksista, tai vain vierasvoitoista.
					if (whatToPlay == 0 || whatToPlay == 2)
					{
						toPlayOrNotToPlay = 2;
					}
				}
			}
			else // Jos algoritmi ennustaa ottelun päättyvän tasapeliin.
			{
				// Onko tasapelille ylikerroin. Ylikertoimeksi ei ajatella tilannetta, jossa algoritmin määrittämä todennäköisyys on sama kuin vedonvälittäjän
				// antama todennäköisyys.
				if (drawAlgoProbability > drawProbability)
				{
					// Jos käyttäjä haluaa lyödä vetoa kaikista lopputuloksista, tai vain tasapeleistä.
					if (whatToPlay == 0 || whatToPlay == 3)
					{
						toPlayOrNotToPlay = 3;
					}
				}
			}
		}
		
		return toPlayOrNotToPlay;
	}
	
	/*
	 * Metodi muuttaa yksittäisen ottelun kertoimen todennäköisyysarvioksi.
	 * 
	 * @param		odds		Kerroin.
	 * 
	 * @return					Todennäköisyysarvio.
	 */
	public double oddsToProbability(double odds)
	{
		return (double)(1 / odds);
	}
	
	/*
	 * Metodi hakee suurimmat kertoimet kullekin lopputulokselle tiettyyn otteluun tietokannasta.
	 * 
	 * @param		gameID		Ottelun ID.
	 * 
	 * @return					Vektori, joka sisältää parhaat kertoimet, jos ottelulle läytyi kertoimet tietokannasta. Indeksissä 0 = kotivoiton kerroin,
	 * 							indeksissä 1 = tasapelin kerroin, indeksissä 2 = vierasvoiton kerroin.
	 */
	public Vector<Double> getBestOdds(String gameID)
	{
		Vector<Double> odds   = new Vector<Double>();
		Connection connection = getConnection();
		double bestHomeOdds   = 0.0;
		double bestDrawOdds   = 0.0;
		double bestAwayOdds   = 0.0;
		
		if (connection != null) 
		{ 
			try
			{
				// Paras kerroin kotivoitolle.
				Statement statement = connection.createStatement();
				ResultSet results   = statement.executeQuery("SELECT max(home_win) AS home_win FROM odds WHERE game_id = '" + gameID + "';");
				
				while (results.next()) 
				{
					// Kotivoiton paras kerroin tässä ottelussa.
					bestHomeOdds = results.getDouble("home_win");
					
					if (bestHomeOdds > 0)
					{
						odds.add(bestHomeOdds);
					}
					else
					{
						odds.add(0.0);
					}
			    }
				
				// Paras kerroin tasapelille.
				results = statement.executeQuery("SELECT max(draw) AS draw FROM odds WHERE game_id = '" + gameID + "';");
				
				while (results.next()) 
				{
					// Kotivoiton paras kerroin tässä ottelussa.
					bestDrawOdds = results.getDouble("draw");
					
					if (bestDrawOdds > 0)
					{
						odds.add(bestDrawOdds);
					}
					else
					{
						odds.add(0.0);
					}
			    }
				
				// Paras kerroin vierasvoitolle.
				results = statement.executeQuery("SELECT max(away_win) AS away_win FROM odds WHERE game_id = '" + gameID + "';");
				
				while (results.next()) 
				{
					// Kotivoiton paras kerroin tässä ottelussa.
					bestAwayOdds = results.getDouble("away_win");
					
					if (bestDrawOdds > 0)
					{
						odds.add(bestAwayOdds);
					}
					else
					{
						odds.add(0.0);
					}
				}
				
				connection.close();
			}
			catch(SQLException ex) {}
		}		
		
		return odds;
	}
}
