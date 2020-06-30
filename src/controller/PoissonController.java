package controller;

import java.util.Vector;
import java.lang.Math;

/*
 * Tämä luokka huolehtii Poisson-mallin tarvitsemista ja käyttämistä laskutoimituksista.
 */
public class PoissonController 
{
	/*
	 * Metodi laskee annettun luvun kertoman.
	 * 
	 * @param			number		Luku, jonka kertoma halutaan laskea.
	 * @return						Luvun kertoma.
	 */
	public int factorial(int number)
	{
		int factorial = number;
		
		if (number > 1)
		{
			for (int ii = number - 1; ii > 0; ii--)
			{
				factorial = factorial * ii;
			}
		}
		
		// On sovittu, että luvun 0 kertoma on 1. Samoin luvun 1 kertoma on 1.
		if (number == 0 || number == 1)
		{
			factorial = 1; 
		}
		
		return factorial;
	}
	
	/*
	 * Metodi laskee Poissonin jakauman annetulle keskiarvolle
	 * 
	 * @param		number		Luku, jonka todennäköisyyttä arvioidaan.
	 * @param		avg			Tapahtuman keskimääräinen tapahtumatiheys tietyssä ajassa.
	 * 
	 * @return					Poissonin jakauman antama todennäköisyys tapahtumalle annetun keskiarvon perusteella.
	 */
	public double poisson(int number, double avg)
	{
		double probability = 0.0;
		double negativeAvg = (double)(avg - avg - avg);
		
		// Korotetaan Eulerin luku variance -muuttujan vastaluvun potenssiin.
		double a    = (double)Math.exp((double)negativeAvg); 
		probability = (double)(a * (Math.pow((double)avg, number))) / factorial(number);
		
		return probability;
	}
	
	/*
	 * Metodi muodostaa todennäköisyydet ottelun eri lopputuloksille Poissonin jakaumaa hyödyntäen.
	 * 
	 *  @param		homeMean	Kotijoukkueen tehtyjen maalien keskiarvo.
	 *  @param		awayMean	Vierasjoukkueen tehtyjen maalien keskiarvo.
	 *  
	 *  @return					Todennäköisyydet kotivoitolle, tasapelille ja vierasvoitolle.
	 */
	public Vector<Double> formProbabilities(double homeMean, double awayMean)
	{
		double homeWin = 0.0;
		double draw    = 0.0;
		double awayWin = 0.0;
		
		Vector<Double> probabilities = new Vector<Double>();
		
		// Käydään läpi kaikki tulokset, joissa sekä koti- että vierasjoukkueella on 0-10 maalia.
		for (int ii = 0; ii < 11; ii++)
		{
			// Kotijoukkueen todennäköisyys tehdä ottelussa ii -maalia.
			double homeProbability = poisson(ii, homeMean);
			
			for (int jj = 0; jj < 11; jj++)
			{
				// Vierasjoukkueen todennäköisyys tehdä ottelussa jj -maalia.
				double awayProbability = poisson(jj, awayMean);
				// Tuloksen ii - jj todennäköisyys.
				double probability = ((double)homeProbability * awayProbability);
				
				// Jos todennäköisyys on pienempi kuin 0.0001 muutetaan se nollaksi.
				if (probability < 0.0001) { probability = 0; }
				
				// Lasketaan eri lopputulosten todennäköisyydet yhteen.
				// Kotivoitto
				if (ii > jj) { homeWin = homeWin + probability; }
				// Tasapeli
				if (ii == jj) { draw = draw + probability; }
				// Vierasvoitto
				if (ii < jj) { awayWin = awayWin + probability; }
			}
		}
		
		// Tallennetaan todennäköisyydet vektoriin.
		probabilities.add(homeWin);
		probabilities.add(draw);
		probabilities.add(awayWin);
		
		return probabilities;
	}
}
