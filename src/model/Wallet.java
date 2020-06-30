package model;

/*
 * Staattinen, singleton-tyyppinen luokka, joka kuvaa pelikassaa sovelluksessa.
 */
public class Wallet extends Model
{
	private double balance = 0.0;
	// Kuvaa riskiä, jonka vedonlyöjä on valmis ottamaan per lyöty veto. Mitä suurempi Kelly Divider on,
	// sitä pienemmän riskin vedonlyöjä on valmis ottamaan.
	private int kellyDivider = 5;
	private static Wallet instance = null;
	private int betsCounter   = 0;
	private int winsCounter   = 0;
	private double betsSum    = 0.0;
	private double winsSum    = 0.0;
	
	// Singleton suunnittelumallin mahdollistamiseksi.
	protected Wallet() {}
	
	
	/*
	 * Metodi, joka tarkistaa, onko luokasta jo olemassa oliota. Jos ei ole, se luodaan ja palautetaan kutsujalle.
	 * Jos olio on jo olemassa, palautetaan tämä olio kutsujalle. Toteutetaan siis Singleton-suunnittelumalli.
	 * 
	 * @return 		Palautetaan Wallet-olio kutsujalle.
	 */
	public static Wallet getInstance()
	{
		// Jos tämä on ensimmäinen kerta kun tätä kyseistä luokkaa kutsutaan, luodaan olio.
		// Sen sijaan, jos tästä luokasta on jo olemassa olio/instanssi, palautetaan se kutsujalle.
		if (instance == null)
		{
			instance = new Wallet();
		}
		
		return instance;
	}
	
	/*
	 * Metodi nollaa kaikki laskurit.
	 */
	public void setCountersZero()
	{
		this.betsCounter = 0;
		this.betsSum = 0.0;
		this.winsCounter = 0;
		this.winsSum = 0.0;
	}
	
	/*
	 * Metodi päivittää lyötyjen vetojen yhteissummaa.
	 * 
	 * @param		stake		Panos, jolla lyötiin vetoa.
	 */
	public void updateBetsSum(double stake)
	{
		this.betsSum = this.betsSum + stake;
	}
	
	/*
	 * Metodi palauttaa lyötyjen vetojen yhteissumman.
	 * 
	 * @param			Lyötyjen vetojen yhteissumma.
	 */
	public double getBetsSum()
	{
		return this.betsSum;
	}
	
	/*
	 * Metodi päivittää voitokkaiden vetojen yhteissummaa.
	 * 
	 * @param		won		Voitettu rahamäärä.
	 */
	public void updateWinsSum(double won)
	{
		this.winsSum = this.winsSum + won;
	}
	
	/*
	 * Metodi palauttaa voittojen yhteissumman.
	 * 
	 * @return			Voittojen yhteissumma.
	 */
	public double getWinsSum()
	{
		return this.winsSum;
	}
	
	/*
	 * Metodi päivittää voitokkaiden vetojan määrää lisäämällä nykyiseen määrään 1.
	 */
	public void updateWinsCounter()
	{
		this.winsCounter++;
	}
	
	/*
	 * Metodi palauttaa voitokkaiden vetojen yhteismäärän.
	 * 
	 * @return			Voitokkaiden vetojen yhteismäärä.
	 */
	public int getWinsCounter()
	{
		return this.winsCounter;
	}
	
	/*
	 * Metodi päivittää lyötyjen vetojan määrää lisäämällä nykyiseen määrään 1. 
	 */
	public void updateBetsCounter()
	{
		this.betsCounter++;
	}
	
	/*
	 * Metodi palauttaa lyötyjen vetojen yhteismäärän.
	 * 
	 * @return			Lyötyjen vetojen yhteismäärä.
	 */
	public int getBetsCounter()
	{
		return this.betsCounter;
	}
	
	/*
	 * Metodi vähentään pelikassasta panoksen, jolla vetoa lyötiin.
	 * 
	 *  @param		stake		Vedonlyöntipanos.
	 */
	public void substractStake(double stake)
	{
		this.balance = balance - stake;
		this.updateBetsCounter();
		this.updateBetsSum(stake);
	}
	
	/*
	 * Metodi lisää pelikassaan yksittäisen vedon voittosumman.
	 * 
	 * @param		won		Voittosumma.
	 */
	public void addWin(double won)
	{
		this.balance = balance + won;
		this.updateWinsCounter();
		this.updateWinsSum(won);
	}
	
	/*
	 * Metodi asettaa Kellyn jakajan arvon.
	 * 
	 * @param		div		Kellyn jakaja.
	 */
	public void setKellyDivider(int div)
	{
		this.kellyDivider = div;
	}
	
	/*
	 * Metodi palauttaa Kellyn jakajan.
	 * 
	 * @return			Kellyn jakaja.
	 */
	public int getKellyDivider()
	{
		return this.kellyDivider;
	}
	
	/*
	 * Metodi asettaa pelikassan saldon halutuksi.
	 * 
	 * @param		balance		Haluttu pelikassan saldo.
	 */
	public void setBalance(double balance)
	{
		this.balance = balance;
	}
	
	/*
	 * Metodi palauttaa pelikassan tämänhetkisen saldon.
	 * 
	 * @return			Pelikassan saldo.
	 */
	public double getBalance()
	{
		return this.balance;
	}
}
