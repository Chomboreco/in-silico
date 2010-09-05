package modelo;

public enum Par
{
	EURUSD, USDJPY, GBPUSD, USDCHF, EURCHF(EURUSD, USDCHF), AUDUSD, USDCAD,
	NZDUSD, EURJPY(EURUSD, USDJPY), GBPJPY, CHFJPY(USDCHF, USDJPY), 
	GBPCHF(GBPUSD, USDCHF), EURAUD(EURUSD, AUDUSD), AUDJPY(AUDUSD, USDJPY),
	TODOS;
	
	private static final Par[] crucesYen = new Par[] {USDJPY, EURJPY, GBPJPY, CHFJPY, AUDJPY};
	
	private final Par padreUno;
	private final Par padreDos;
	private double bidActual = 0;
	private double askActual = 0;
	private double ssiActual = 0;
	
	private Par()
	{
		padreUno = this;
		padreDos = this;
	}
	
	private Par(Par p1, Par p2)
	{
		padreUno = p1;
		padreDos = p2;
	}

	private boolean esCruceYen()
	{
		for(Par p : crucesYen)
			if(p.equals(this))
				return true;
		return false;
	}
	
	public Par darPadreUno()
	{
		return padreUno;
	}
	
	public Par darPadreDos()
	{
		return padreDos;
	}
	
	public boolean estaBien(double precio)
	{
		if(esCruceYen())
			return precio > 5;
		return precio < 5;
	}
	
	public boolean esDistinto(Par par) 
	{
		if(par == TODOS)
			return false;
		return !equals(par);
	}
	
	public int diferenciaPips(double otro, boolean compra)
	{
		double precioActual = darPrecioActual(compra);
		double precioParActual = compra ? precioActual - otro : otro - precioActual;
		return esCruceYen() ? (int) Math.round((precioParActual) * 100) : (int) Math.round((precioParActual) * 10000);
	}
	
	public synchronized void ponerPrecioActual(double bid, double ask) 
	{
		if(bidActual == 0)
		{
			if(estaBien(bid) && estaBien(ask))
			{
				bidActual = bid;
				askActual = ask;
			}
		}
		else
		{
			if(Math.abs(diferenciaPips(bid, true)) <= 500 || Math.abs(diferenciaPips(ask, false)) <= 500)
			{
				bidActual = bid;
				askActual = ask;
			}
		}
	}
	
	public synchronized double darPrecioActual(boolean compra)
	{
    	return compra ? bidActual : askActual;
	}
	
	public synchronized void ponerSSI(double ssi)
	{
		ssiActual = ssi;
	}
	
	public synchronized double darSSI()
	{
		return ssiActual;
	}
	
	public static Par stringToPar(String string) 
	{
		for(Par a : values())
			if(string.equals(a.toString()))
				return a;
		return TODOS;
	}
	
	public static Par convertirPar(String cuerpo)
	{
		Par par = null;
		for(Par a : values())
			if(!a.equals(TODOS) && cuerpo.contains(a.toString()))
				par = a;
		return par;
	}
}