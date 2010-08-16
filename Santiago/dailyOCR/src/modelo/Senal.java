package modelo;

import control.IdEstrategia;
import control.Par;
import control.conexion.ConexionServidor;

public class Senal
{
	private IdEstrategia estrategia;
	private boolean compra;
	private Par par;
	private int numeroLotes;
	private int lotesCerradosManualmente = 0;
	private double precioEntrada;
	private int[] magico = {0};
    private boolean manual = false;
    private double limite;
	private double VIX;
	private double SSI1;
	private double SSI2;
    
	public Senal(IdEstrategia estrategia, boolean compra, Par par, int numeroLotes, double precioEntrada)
	{
		this.estrategia = estrategia;
		this.compra = compra;
		this.par = par;
		this.numeroLotes = numeroLotes;
		this.precioEntrada = precioEntrada;
		this.VIX = ConexionServidor.leerVIX();
		this.SSI1 = ConexionServidor.darSSI(Par.padres.get(par.ordinal())[0]);
		this.SSI2 = ConexionServidor.darSSI(Par.padres.get(par.ordinal())[1]); 
	}
    
	public IdEstrategia getEstrategia() {
		return estrategia;
	}

	public void setEstrategia(IdEstrategia estrategia) {
		this.estrategia = estrategia;
	}

	public boolean isCompra() {
		return compra;
	}

	public void setCompra(boolean compra) {
		this.compra = compra;
	}

	public Par getPar() {
		return par;
	}

	public void setPar(Par par) {
		this.par = par;
	}

	public int getNumeroLotes() {
		return numeroLotes;
	}

	public void setNumeroLotes(int numeroLotes) {
		this.numeroLotes = numeroLotes;
	}

	public int getLotesCerradosManualmente() {
		return lotesCerradosManualmente;
	}

	public void setLotesCerradosManualmente(int lotesCerradosManualmente) {
		this.lotesCerradosManualmente = lotesCerradosManualmente;
	}

	public double getPrecioEntrada() {
		return precioEntrada;
	}

	public void setPrecioEntrada(double precioEntrada) {
		this.precioEntrada = precioEntrada;
	}

	public int[] getMagico() {
		return magico;
	}

	public void setMagico(int[] magico) {
		this.magico = magico;
	}

	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}

	public double getLimite() {
		return limite;
	}

	public void setLimite(double limite) {
		this.limite = limite;
	}
	
	public double getVIX() {
		return VIX;
	}

	public void setVIX(double vIX) {
		VIX = vIX;
	}

	public double getSSI1() {
		return SSI1;
	}

	public void setSSI1(double sSI1) {
		SSI1 = sSI1;
	}

	public double getSSI2() {
		return SSI2;
	}

	public void setSSI2(double sSI2) {
		SSI2 = sSI2;
	}

	public String toString()
	{
		return estrategia + " " + (compra ? "Compra" : "Venta") + " " + numeroLotes + " Lotes de " + par + " a: " + precioEntrada; 
	}
}
