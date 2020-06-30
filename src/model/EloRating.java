package model;

/*
 * Metodi huolehtii Elo-mallin toiminnoista.
 */
public class EloRating extends Model 
{
	// Kerroin, jota käytetään elo-lukua päivittäessä.
	private int k 			 	  	= 20;
	private int teamID 		 	  	= 0;
	// Oletus Elo-luku.
	private double eloRatingOverall = 400.0;
	// Oletus koti-Elo-luku.
	private double eloRatingHome  	= 200.0;
	// Oletus vieras-Elo-luku.
	private double eloRatingAway  	= 200.0;
	private int homeGameCounter	    = 0;
	private int awayGameCounter		= 0;
	private int gameCounter			= 0;
	
	/*
	 * Metodi palauttaa simuloitujen kotiotteluiden määrän tietyllä hetkellä.
	 * 
	 * @return			Kotiotteluiden määrä.
	 */
	public int getCurrentHomeGameCount()
	{
		return this.homeGameCounter;
	}
	
	/*
	 * Metodi palauttaa simuloitujen vierasotteluiden määrän tietyllä hetkellä.
	 * 
	 * @return			Vierasotteluiden määrä.
	 */	
	public int getCurrentAwayGameCount()
	{
		return this.awayGameCounter;
	}
	
	/*
	 * Metodi palauttaa simuloitujen otteluiden määrän tietyllä hetkellä.
	 * 
	 * @return			Simuloitujen otteluiden määrä.
	 */
	public int getCurrentGameCount()
	{
		return this.gameCounter;
	}
	
	/*
	 * Metodi päivittää simuloitujen kotiotteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateCurrentHomeGameCount()
	{
		this.homeGameCounter++;
	}

	/*
	 * Metodi päivittää simuloitujen vierasotteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateCurrentAwayGameCount()
	{
		this.awayGameCounter++;
	}

	/*
	 * Metodi päivittää simuloitujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateCurrentGameCount()
	{
		this.gameCounter++;
	}
	
	/*
	 * Metodi päivittää maaliperustaisen Elo-mallin mukaisesti kotiotteluiden Elo-lukua.
	 */
	public void updateGoalBasedHomeEloRating(int goalDifference)
	{
		this.eloRatingHome = this.eloRatingHome + 10 * (1 + goalDifference);
		this.updateCurrentHomeGameCount();
		this.updateCurrentGameCount();
	}
	
	/*
	 * Metodi päivittää perus-Elo-mallin mukaisesti kotiotteluiden Elo-lukua.
	 */
	public void updateHomeEloRating(double expectedPoints, double actualPoints)
	{
		this.eloRatingHome = this.eloRatingHome + k * (actualPoints - expectedPoints);
		this.updateCurrentHomeGameCount();
		this.updateCurrentGameCount();
	}
	
	/*
	 * Metodi päivittää maaliperustaisen Elo-mallin mukaisesti vierasotteluiden Elo-lukua.
	 */
	public void updateGoalBasedAwayEloRating(int goalDifference)
	{
		this.eloRatingAway = this.eloRatingAway + 10 * (1 + goalDifference);
		this.updateCurrentHomeGameCount();
		this.updateCurrentGameCount();
	}
	
	/*
	 * Metodi päivittää perus-Elo-mallin mukaisesti vierasotteluiden Elo-lukua.
	 */
	public void updateAwayEloRating(double expectedPoints, double actualPoints)
	{
		this.eloRatingAway = this.eloRatingAway + k * (actualPoints - expectedPoints);
		this.updateCurrentAwayGameCount();
		this.updateCurrentGameCount();
	}
	
	/*
	 * Metodi palauttaa kotiotteluiden Elo-luvun.
	 * 
	 * @return		Kotiotteluiden Elo-luku.
	 */
	public double getHomeEloRating()
	{
		return this.eloRatingHome;
	}

	/*
	 * Metodi palauttaa vierasotteluiden Elo-luvun.
	 * 
	 * @return		Vierasotteluiden Elo-luku.
	 */
	public double getAwayEloRating()
	{
		return this.eloRatingAway;
	}
	
	/*
	 * Metodi päivittää maaliperustaisen Elo-mallin mukaisesti Elo-lukua.
	 */
	public void updateGoalBasedEloRating(int goalDifference)
	{
		this.eloRatingOverall = this.eloRatingOverall + 10 * (1 + goalDifference);
		this.updateCurrentGameCount();
	}

	/*
	 * Metodi päivittää perus-Elo-mallin mukaisesti Elo-lukua.
	 */
	public void updateEloRating(double expectedPoints, double actualPoints)
	{
		this.eloRatingOverall = this.eloRatingOverall + k * (actualPoints - expectedPoints);
		this.updateCurrentGameCount();
	}
	
	/*
	 * Metodi muuttaa uudeksi Elo-luvuksi syötteenä saamansa luvun.
	 */
	public void setEloRating(double rating)
	{
		this.eloRatingOverall = rating;
	}
	
	/*
	 * Metodi palauttaa tämänhetkisen Elo-luvun.
	 * 
	 * @return			Tämänhetkinen Elo-luku.
	 */
	public double getEloRating()
	{
		return this.eloRatingOverall;
	}
	
	/*
	 * Metodi asettaa joukkueen ID:n oliolle.
	 */
	public void setTeamID(int id)
	{
		this.teamID = id;
	}
	
	/*
	 * Metodi palauttaa joukkueen ID:n
	 * 
	 * @return		Joukkueen ID.
	 */
	public int getTeamID()
	{
		return this.teamID;
	}
}
