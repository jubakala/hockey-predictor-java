package controller;

import model.*;
import view.*;

/*
 * Luokka huolehtii eri simulointimetodien kdynnistdmisestd.
 */
public class MainController extends Controller
{
	private static MainController instance 	= null;
	private GamesController gC 				= GamesController.getInstance();
	// Counter luokka huolehtii simuloinnin eri tapahtumien tilastoinnista.
	private Counter counter 				= Counter.getInstance();
	
	// Singleton suunnittelumallin mahdollistamiseksi.
	protected MainController() {}
	
	/*
	 * Metodi, joka tarkistaa, onko luokasta jo olemassa oliota. Jos ei ole, se luodaan ja palautetaan kutsujalle.
	 * Jos olio on jo olemassa, palautetaan tdmd olio kutsujalle. Toteutetaan siis Singleton-suunnittelumalli.
	 * 
	 * @return 		Palautetaan MainController-olio kutsujalle.
	 */
	public static MainController getInstance()
	{
		// Jos tdmd on ensimmdinen kerta kun tdtd kyseistd luokkaa kutsutaan, luodaan olio.
		// Sen sijaan, jos tdstd luokasta on jo olemassa olio/instanssi, palautetaan se kutsujalle.
		if (instance == null)
		{
			instance = new MainController();
		}
		
		return instance;
	}
	
	/*
	 * Tämä metodi alustaa ja näyttää sovelluksen ainoan käyttöliittymäikkunan sovelluksen käynnistyessä. 
	 */
	public void showStartWindow()
	{
		MainWindow mW = new MainWindow();
		mW.launch();
	}
	
	/*
	 * Tätä metodia kutsuaan, kun käyttäjä on käyttöliittymäikkunasta valinnut, että hän haluaa simuloida vedonlyöntiä
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
		counter.initialize();
		// Käynnistetään simulointi. // gC = GamesController-luokan singleton-olio.
		counter = gC.poisson(season, lastGamesAmount, whatToPlay, wallet);
		
		return counter;
	}
	
	/*
	 * Tätä metodia kutsuaan, kun käyttäjä on käyttöliittymäikkunasta valinnut, että hän haluaa simuloida vedonlyöntiä
	 * Elo-mallia käyttäen. 
	 * 
	 * @param		season				Kausi, jota simuloidaan.
	 * @param		whatToPlay			0 = lyödään vetoa kaikista lopputuloksista. 1 = lyödään vetoa vain algoritmin kotivoitoiksi ennustamista otteluista.
	 * 									2 = lyödään vetoa vain algoritmin vierasvoitoiksi ennustamista otteluista. 3 = lyödään vetoa vain algoritmin tasapeleiksi 
	 * 									ennustamista otteluista. (Tätä vaihtoehtoa ei voi valita käyttöliittymästä, olemassa vain kehitysvaiheen testausta varten).
	 * @param		wallet				Pelikassa-olio.
	 * @param		homeAway			true = lasketaan joka joukkueelle Elo-luvut erikseen sekä koti- että vierasotteluille. false = lasketaan joka joukkueelle
	 * 									vain yksi Elo-luku, jota käytetään sekä koti- että vierasotteluissa.
	 * @param		goalBased			true = käytetään maaliperustaista Elo-mallia simuloimiseen. false = käytetään perus-Elo-mallia simuloimiseen.
	 * @param		useWeighted			true = käytetään painotettuja Elo-lukuja simuloimiseen. false = käytetään painottamattomia Elo-lukuja simuloimiseen.
	 * @return							Metodi palauttaa Counter-olion, joka sisältää kaikki tiedot simuloinneista sekä myös Wallet-olion.
	 */
	public Counter elo(String season, int whatToPlay, Wallet wallet, boolean homeAway, boolean goalBased, boolean useWeighted)
	{
		counter.initialize();
		// Käynnistetään simulointi. // gC = GamesController-luokan singleton-olio.
		counter = gC.formEloRatings(season, whatToPlay, wallet, homeAway, goalBased, useWeighted);
		
		return counter;
	}
	
	/*
	 * Tätä metodia kutsuaan, kun käyttäjä on käyttöliittymäikkunasta valinnut, että hän haluaa simuloida vedonlyöntiä
	 * pi-hockey -mallia käyttäen. 
	 * 
	 * @param		season				Kausi, jota simuloidaan.
	 * @param		whatToPlay			0 = lyödään vetoa kaikista lopputuloksista. 1 = lyödään vetoa vain algoritmin kotivoitoiksi ennustamista otteluista.
	 * 									2 = lyödään vetoa vain algoritmin vierasvoitoiksi ennustamista otteluista. 3 = lyödään vetoa vain algoritmin tasapeleiksi 
	 * 									ennustamista otteluista. (Tätä vaihtoehtoa ei voi valita käyttöliittymästä, olemassa vain kehitysvaiheen testausta varten).
	 * @param		wallet				Pelikassa-olio.
	 * @return							Metodi palauttaa Counter-olion, joka sisältää kaikki tiedot simuloinneista sekä myös Wallet-olion.
	 */
	public Counter piHockey(String season, int whatToPlay, Wallet wallet)
	{
		counter.initialize();
		// Käynnistetään simulointi. // gC = GamesController-luokan singleton-olio.
		counter = gC.formPiRatings(season, whatToPlay, wallet);
		
		return counter;
	}
	
	/*
	 * Tätä metodia kutsuaan, kun käyttäjä on käyttöliittymäikkunasta valinnut, että hän haluaa simuloida vedonlyöntiä
	 * aggregaatti-mallia käyttäen. 
	 * 
	 * @param		season				Kausi, jota simuloidaan.
	 * @param		whatToPlay			0 = lyödään vetoa kaikista lopputuloksista. 1 = lyödään vetoa vain algoritmin kotivoitoiksi ennustamista otteluista.
	 * 									2 = lyödään vetoa vain algoritmin vierasvoitoiksi ennustamista otteluista. 3 = lyödään vetoa vain algoritmin tasapeleiksi 
	 * 									ennustamista otteluista. (Tätä vaihtoehtoa ei voi valita käyttöliittymästä, olemassa vain kehitysvaiheen testausta varten).
	 * @param		wallet				Pelikassa-olio.
	 * @return							Metodi palauttaa Counter-olion, joka sisältää kaikki tiedot simuloinneista sekä myös Wallet-olion.
	 */
	public Counter aggregateModel(String season, int whatToPlay, Wallet wallet)
	{
		counter.initialize();
		// Käynnistetään simulointi. // gC = GamesController-luokan singleton-olio.
		counter = gC.aggregate(season, whatToPlay, wallet);
		
		return counter;
	}
}
