import controller.MainController;
/*
 * Tämän luokan ainoana tehtävänä on käynnistää itse sovellus.
 */
public class Hockey 
{
	/*
	 * Main-metodi.
	 */
	public static void main(String args[])
	{
		Hockey h = new Hockey();
		h.start();
	}
	
	/*
	 * Tämä metodi käynnistää sovelluksen pääkontrolleri-luokan, ja pyytää sitä näyttämään käyttöliittymäikkunan.
	 */
	public void start()
	{
		MainController mainC = MainController.getInstance();
		mainC.showStartWindow();
	}
}
