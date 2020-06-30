package model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;
import java.sql.Statement;
import java.sql.ResultSet;

/*
 * Luokka huolehtii otteluihin liittyvistä toiminnoista.
 */
public class Game extends Model
{
	/*
	 * Metodi laskee keskiarvotodennäköisyydet pi-, elo- ja poisson-keskiarvoista.
	 * 
	 * @param		piProbabilities			pi-hockey -mallilla lasketut todennäköisyydet ottelun lopputuloksille.
	 * @param		eloProbabilities		Elo-mallilla lasketut todennäköisyydet ottelun lopputuloksille.
	 * @param		poissonProbabilities	Poisson-mallilla lasketut todennäköisyydet ottelun lopputuloksille.
	 * 
	 * @return								Keskiarvotodennäköisyydet eri lopputuloksille.
	 */
	public Vector<Double> formAggregateProbabilities(Vector<Double> piProbabilities, Vector<Double> eloProbabilities, Vector<Double> poissonProbabilities)
	{
		Vector<Double> probabilities = new Vector<Double>();
		
		// pi-hockey -mallin laskemat todennäköisyydet.
		Double piHomeProbability = piProbabilities.get(0);
		Double piDrawProbability = piProbabilities.get(1);
		Double piAwayProbability = piProbabilities.get(2);
		
		// Elo-mallin laskemat todennäköisyydet.
		Double eloHomeProbability = eloProbabilities.get(0);
		Double eloDrawProbability = eloProbabilities.get(1);
		Double eloAwayProbability = eloProbabilities.get(2);
		
		// Poisson-mallin laskemat todennäköisyydet.
		Double poissonHomeProbability = poissonProbabilities.get(0);
		Double poissonDrawProbability = poissonProbabilities.get(1);
		Double poissonAwayProbability = poissonProbabilities.get(2);
		
		Double homeProbability = 0.0;
		Double drawProbability = 0.0;
		Double awayProbability = 0.0;
		
		// Aivan kauden ensimmäisiin otteluihin ei ole tarjolla Poisson-todennäköisyyksiä.
		if (poissonHomeProbability > 0 && poissonDrawProbability > 0 && poissonAwayProbability > 0)
		{
			homeProbability = (double)(piHomeProbability + eloHomeProbability + poissonHomeProbability) / 3;
			drawProbability = (double)(piDrawProbability + eloDrawProbability + poissonDrawProbability) / 3;
			awayProbability = (double)(piAwayProbability + eloAwayProbability + poissonAwayProbability) / 3;
		}
		else
		{
			homeProbability = (double)(piHomeProbability + eloHomeProbability) / 2;
			drawProbability = (double)(piDrawProbability + eloDrawProbability) / 2;
			awayProbability = (double)(piAwayProbability + eloAwayProbability) / 2;
		}
		
		probabilities.add(homeProbability);
		probabilities.add(drawProbability);
		probabilities.add(awayProbability);
		
		return probabilities;
	}
	
	/*
	 * Metodi tarkastaa, onnistuiko algoritmi ennustamaan ottelun lopputuloksen oikein. Metodi ei palauta mitään, vaan tulokset tallennetaan Counter-olioon,
	 * joka on staattinen.
	 * 
	 * @param		season			Kausi, jota simuloidaan.
	 * @param		homeTeam		Kotijoukkueen ID.
	 * @param		awayTeam		Vierasjoukkueen ID.
	 * @param		homeGoals		Kotijoukkueen tekemät maalit ottelussa.
	 * @param		awayGoals		Vierasjoukkueen tekemät maalit ottlussa.
	 * @param		probabilities	Algoritmin määrittämät todennäköisyydet eri lopputuloksille.
	 * @param		counter			Counter-luokan olio, joka pitää kirjaa simuloinnin aikana tapahtuvista asioista.
	 */
	public void check(String season, String date, int homeTeam, int awayTeam, int homeGoals, int awayGoals, Vector<Double> probabilities, Counter counter)
	{
		int result = 0;
		if (homeGoals > awayGoals)  { result  = 1; }
		if (homeGoals == awayGoals) { result  = 3; }
		if (homeGoals < awayGoals)  { result  = 2; }
		
		double homeProb = probabilities.get(0);
		double drawProb = probabilities.get(1);
		double awayProb = probabilities.get(2);
		
		// Jos algoritmi ennusti ottelun päättyvän kotivoitoon.
		if (homeProb > drawProb && homeProb > awayProb)
		{
			// Jos ottelu päättyi kotivoittoon.
			if (result == 1) 
			{
				counter.updateCorrectHomeWinPredictions();
				counter.updateCorrect();
			}
			else
			{
				counter.updateInCorrectHomeWinPredictions();
				counter.updateInCorrect();
			}
		}
		
		// Jos sekä koti että vierasvoiton todennäköisyydet ovat samat, valitaan kotivoitto.
		if (homeProb == awayProb || homeProb == drawProb)
		{
			// Ottelu päättyi kotivoittoon.
			if (result == 1) 
			{
				counter.updateCorrectHomeWinPredictions();
				counter.updateCorrect();
			}
			else
			{
				counter.updateInCorrectHomeWinPredictions();
				counter.updateInCorrect();
			}
		}
		
		// Jos algoritmi ennusti ottelun päättyvän tasapeliin.
		if (drawProb > homeProb && drawProb > awayProb)
		{
			// Ottelu päättyi tasapeliin.
			if (result == 3)
			{
				counter.updateCorrectDrawPredictions();
				counter.updateCorrect();
			}
			else
			{
				counter.updateInCorrectDrawPredictions();
				counter.updateInCorrect();
			}
		}
		
		// Jos algoritmi ennusti ottelun päättyvän vierasvoittoon.
		if (awayProb > drawProb && awayProb > homeProb)
		{
			// Jos ottelu päättyi vierasvoittoon.
			if (result == 2)
			{
				counter.updateCorrectAwayWinPredictions();
				counter.updateCorrect();
			}
			else
			{
				counter.updateInCorrectAwayWinPredictions();
				counter.updateInCorrect();
			}
		}
		
		counter.updateGameCount();
	}
	
	/*
	 * Metodi palauttaa kaikkien tietokannasta löytyvien kausien vuosiluvut. (Esim. 2008-2009).
	 * 
	 * @return				Kaudet.
	 */
	public Vector<String> getSeasons()
	{
		Vector<String> seasons = new Vector<String>();
		Connection connection  = getConnection();
		
		if (connection != null) 
		{ 			
			try
			{
				Statement statement = connection.createStatement();
				ResultSet results   = statement.executeQuery("SELECT season FROM games GROUP BY season;");
				
				while (results.next()) 
				{
			        seasons.add(results.getString("season"));
				}
				
				connection.close();
			}
			catch(SQLException ex) {}
		} 
		
		return seasons;
	}
}
