package model;

/*
 * Luokka huolehtii pi-lukuihin liittyvistä toiminnosta.
 */
public class PiRating extends Model
{
		private int teamID 			= 0;
		private double homePiRating = 0.0;
		private double awayPiRating = 0.0;
		
		/*
		 * Metodi asettaa joukkueen ID:n.
		 */
		public void setTeamID(int id)
		{
			this.teamID = id;
		}
		
		/*
		 * Metodi asettaa koti-pi-luvuksi syötteenä saadun luvun.
		 * 
		 * @param		rating		Uusi pi-luku.
		 */
		public void setHomePiRating(double rating)
		{
			this.homePiRating = rating;
		}
		
		/*
		 * Metodi asettaa vieras-pi-luvuksi syötteenä saadun luvun.
		 * 
		 * @param		rating		Uusi pi-luku. 
		 */
		public void setAwayPiRating(double rating)
		{
			this.awayPiRating = rating;
		}
		
		/*
		 * Metodi palauttaa joukkueen ID:n.
		 * 
		 * @return			Joukkueen ID.
		 */
		public int getTeamID()
		{
			return this.teamID;
		}
		
		/*
		 * Metodi palauttaa koti-pi-luvun.
		 * 
		 * @return		Koti-pi-luku.
		 */
		public double getHomePiRating()
		{
			return this.homePiRating;
		}
		
		/*
		 * Metodi palauttaa vieras-pi-luvun.
		 * 
		 * @return		Vierasotteluiden pi-luku.
		 */
		public double getAwayPiRating()
		{
			return this.awayPiRating;
		}
		
		/*
		 * Metodi päivittää kotiotteluiden pi-lukua.
		 * 
		 * @param		update		Päivityksen määrä.
		 */
		public void updateHomeRating(double update)
		{
			double newHomeRating = this.homePiRating + update * 0.1;
			double newAwayRating = this.awayPiRating + (newHomeRating - this.homePiRating) * 0.3;
			
			this.homePiRating = newHomeRating;
			this.awayPiRating = newAwayRating;
		}

		/*
		 * Metodi päivittää vierasotteluiden pi-lukua.
		 * 
		 * @param		update		Päivityksen määrä.
		 */
		public void updateAwayRating(double update)
		{
			double newAwayRating = this.awayPiRating + update * 0.1;
			double newHomeRating = this.homePiRating + (newAwayRating - this.awayPiRating) * 0.3;
			
			this.homePiRating = newHomeRating;
			this.awayPiRating = newAwayRating;
		}
		
		/*
		 * Metodi laskee ennakoidun kotijoukkueen maaliero-osuuden.
		 * 
		 * @return			Ennakoitu kotijoukkueen osuus maalierosta.
		 */
		public double calculateExpectedHomeGoalDifference()
		{
			double difference = 0.0;
			difference = Math.pow(10, (this.homePiRating / 3)) - 1;
			
			return difference;
		}

		/*
		 * Metodi laskee ennakoidun vierasjoukkueen maaliero-osuuden.
		 * 
		 * @return			Ennakoitu vierasjoukkueen osuus maalierosta.
		 */
		public double calculateExpectedAwayGoalDifference()
		{
			double difference = 0.0;
			difference = -(Math.pow(10, (Math.abs(this.awayPiRating) / 3)) - 1);
			
			return difference;
		}
}
