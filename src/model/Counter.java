package model;

import java.text.DecimalFormat;

/*
 * Luokka pitää kirjaa erinäisistä asioista simuloinnin aikana.
 */
public class Counter extends Model 
{
	private static Counter instance 			= null;
	
	private int correct							= 0;
	private int inCorrect 						= 0;
	private int correctHomeWinPredictions 		= 0;
	private int inCorrectHomeWinPredictions 	= 0;
	private int correctAwayWinPredictions 		= 0;
	private int inCorrectAwayWinPredictions 	= 0;
	private int correctDrawPredictions			= 0;
	private int inCorrectDrawPredictions		= 0;
	private int gameCount					  	= 0;
	
	protected Counter() {}
	
	/*
	 * Metodi, joka tarkistaa, onko luokasta jo olemassa oliota. Jos ei ole, se luodaan ja palautetaan kutsujalle.
	 * Jos olio on jo olemassa, palautetaan tämä olio kutsujalle. Toteutetaan siis Singleton-suunnittelumalli.
	 * 
	 * @return 		Palautetaan Counter-olio kutsujalle.
	 */
	public static Counter getInstance()
	{
		// Jos tämä on ensimmäinen kerta kun tätä kyseistä luokkaa kutsutaan, luodaan olio.
		// Sen sijaan, jos tästä luokasta on jo olemassa olio/instanssi, palautetaan se kutsujalle.
		if (instance == null)
		{
			instance = new Counter();
		}
		
		return instance;
	}
	
	/*
	 * Metodi alustaa kaikki luokan arvot nolliksi.
	 */
	public void initialize()
	{
		this.correct 						= 0;
		this.inCorrect 						= 0;
		this.correctHomeWinPredictions 		= 0;
		this.inCorrectHomeWinPredictions 	= 0;
		this.correctAwayWinPredictions 		= 0;
		this.inCorrectAwayWinPredictions 	= 0;
		this.correctDrawPredictions 		= 0;
		this.inCorrectDrawPredictions 		= 0;
		this.gameCount 						= 0;
	}
	
	/*
	 * Metodi muodostaa tekstin, joka näytetään käyttöliittymässä simuloinnin jälkeen.
	 * 
	 * @param		season		Kausi, jota simuloitiin.
	 * @param		wallet		Pelikassa-olio.
	 * 
	 * @return					Merkkijono, joka tulostetaan käyttöliittymässä.
	 */
	public String print(String season, Wallet wallet)
	{
		String results = "";
		results = results + "Kausi: " + season + "\n";
		results = results + "Oikein: " + getCorrect();
		results = results + "\tVäärin: " + getInCorrect();
		results = results + "\tYhteensä: " + getGameCount();
		results = results + "\nKotivoitoiksi ennustetut:\n";
		results = results + "Oikein: " + this.getCorrectHomeWinPredictions();
		results = results + "\tVäärin: " + this.getInCorrectHomeWinPredictions();
		results = results + "\tYhteensä: " + (this.getCorrectHomeWinPredictions() + this.getInCorrectHomeWinPredictions());
		results = results + "\nVierasvoitoiksi ennustetut:\n";
		results = results + "Oikein: " + this.getCorrectAwayWinPredictions();
		results = results + "\tVäärin: " + this.getInCorrectAwayWinPredictions();
		results = results + "\tYhteensä: " + (this.getCorrectAwayWinPredictions() + this.getInCorrectAwayWinPredictions() + "\n");
		
		// Jos lyödään vetoa ja pelikassan summa on suurempi kuin 0.
		if (wallet.getBetsCounter() > 0 && wallet.getBetsSum() > 0)
		{
			DecimalFormat df = new DecimalFormat("####0.00");
			String winSum  = df.format(wallet.getWinsSum());
			String betSum  = df.format(wallet.getBetsSum());
			String balance = df.format(wallet.getBalance());
			
			results = results + "Pelatut:\n";
			results = results + "Vedot: " + wallet.getBetsCounter() + "\t€ " + betSum;
			results = results + "\nVoitot: " + wallet.getWinsCounter() + "\t€ " + winSum;
			results = results + "\nPelikassan saldo: " + balance + "\n";
		}
		
		results = results + "\n";
		
		return results;
	}
	
	/*
	 * Metodi palauttaa simuloinnin aikana oikein vetojan määrän.
	 * 
	 * @return		Oikeiden vetojan määrä.
	 */
	public int getCorrect()
	{
		return this.correct;
	}
	
	
	/*
	 * Metodi palauttaa simuloinnin aikana väärinmenneiden vetojen määrän.
	 * 
	 * @return			Väärinmenneiden vetojen määrä.
	 */
	public int getInCorrect()
	{
		return this.inCorrect;
	}
	
	/*
	 * Metodi palauttaa simuloinnin aikana oikeinmenneiden, kotivoitoiksi ennustettujen otteluiden määrän.
	 * 
	 * @return		Oikeinmenneet kotivoitoiksi ennustetut.
	 */
	public int getCorrectHomeWinPredictions()
	{
		return this.correctHomeWinPredictions;
	}
	
	/*
	 * Metodi palauttaa simuloinnin aikana väärinmenneiden kotivoitoiksi ennustettujen otteluiden määrän.
	 * 
	 * @return		Väärinmenneet kotivoitoiksi ennustetut.
	 */
	public int getInCorrectHomeWinPredictions()
	{
		return this.inCorrectHomeWinPredictions;
	}
	
	/*
	 * Metodi palauttaa simuloinnin aikana oikeinmenneiden vierasvoitoiksi ennustettujen otteluiden määrän.
	 * 
	 * @return			Oikeinmenneet vierasvoitoiksi ennustetut.
	 */
	public int getCorrectAwayWinPredictions()
	{
		return this.correctAwayWinPredictions;
	}
	
	/*
	 * Metodi palauttaa simuloinnin aikana väärinmenneiden vierasvoitoiksi ennustettujen otteluiden määrän.
	 * 
	 * @return			Väärinmenneet vierasvoitoiksi ennustetut.
	 */
	public int getInCorrectAwayWinPredictions()
	{
		return this.inCorrectAwayWinPredictions;
	}
	
	/*
	 * Metodi palauttaa simuloinnin aikana oikeinmenneiden tasapeleiksi ennustettujen otteluiden määrän.
	 * 
	 * @return			Oikeinmenneet tasapeleiksi ennustetut.
	 */	
	public int getCorrectDrawPredictions()
	{
		return this.correctDrawPredictions;
	}
	
	/*
	 * Metodi palauttaa simuloinnin aikana väärinmenneiden tasapeleiksi ennustettujen otteluiden määrän.
	 * 
	 * @return			Väärinmenneet tasapeleiksi ennustetut.
	 */
	public int getInCorrectDrawPredictions()
	{
		return this.inCorrectDrawPredictions;
	}
	
	/*
	 * Metodi palauttaa simuloitujen otteluiden määrän kys. simuloinnissa.
	 * 
	 * @return			Simuloitujen otteluiden määrä.
	 */
	public int getGameCount()
	{
		return this.gameCount;
	}
	
	/*
	 * Metodi päivittää oikeinmenneiden vetojen määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateCorrect()
	{
		this.correct++;
	}
	
	/*
	 * Metodi päivittää väärinmenneiden vetojen määrää lisäämällä tämän hetkiseen määrään yhden.
	 */	
	public void updateInCorrect()
	{
		this.inCorrect++;
	}
	
	/*
	 * Metodi päivittää oikeinmenneiden kotivoitoiksi ennustettujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateCorrectHomeWinPredictions()
	{
		this.correctHomeWinPredictions++;
	}
	
	/*
	 * Metodi päivittää väärinmenneiden kotivoitoiksi ennustettujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateInCorrectHomeWinPredictions()
	{
		this.inCorrectHomeWinPredictions++;
	}
	
	/*
	 * Metodi päivittää oikeinmenneiden vierasvoitoiksi ennustettujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */	
	public void updateCorrectAwayWinPredictions()
	{
		this.correctAwayWinPredictions++;
	}
	
	/*
	 * Metodi päivittää väärinmenneiden vierasvoitoiksi ennustettujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateInCorrectAwayWinPredictions()
	{
		this.inCorrectAwayWinPredictions++;
	}
	
	/*
	 * Metodi päivittää oikeinmenneiden tasapeleiksi ennustettujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */		
	public void updateCorrectDrawPredictions()
	{
		this.correctAwayWinPredictions++;
	}
	
	/*
	 * Metodi päivittää väärinmenneiden tasapeleiksi ennustettujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */	
	public void updateInCorrectDrawPredictions()
	{
		this.inCorrectDrawPredictions++;
	}
	
	/*
	 * Metodi päivittää simuloitujen otteluiden määrää lisäämällä tämän hetkiseen määrään yhden.
	 */
	public void updateGameCount()
	{
		this.gameCount++;
	}
}
