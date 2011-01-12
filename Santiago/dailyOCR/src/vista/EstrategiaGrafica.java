package vista;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import modelo.Par;
import modelo.Estrategia.IdEstrategia;
import analisis.AnalisisLogica;
import analisis.Rangos;
import control.Error;

public class EstrategiaGrafica extends JFrame
{
	private static final long serialVersionUID = 7878714258759106938L;

	IdEstrategia idEstrategia;
	boolean enLinea;
	
	public EstrategiaGrafica(IdEstrategia id, boolean eL) 
	{	
		super();
		idEstrategia = id;
		enLinea = eL;
		initialize();
	}
	
	private void initialize()
	{
		new RangosGrafico(new Rangos(), AnalisisLogica.darRegistrosEstrategia(idEstrategia, Par.TODOS), null, null);
		GridLayout gridLayout = new GridLayout(0, 2);
		this.setLayout(gridLayout);
		this.setSize(259, 490);
		for(Par par : Par.values())
			if(par != Par.TODOS)
				this.add(darBotonPar(par));
		pack();
		setVisible(true);
	}

	private JButton darBotonPar(final Par par) 
	{
		JButton botonNuevo = new JButton();
		botonNuevo.setText(par.toString());
		botonNuevo.addActionListener(new java.awt.event.ActionListener() 
		{
			public void actionPerformed(java.awt.event.ActionEvent e)
			{
				try
				{
					new RangosGrafico(!enLinea ? new Rangos() : ParteGrafica.conexion.darRangosEstrategia(idEstrategia.ordinal(), par.ordinal()), AnalisisLogica.darRegistrosEstrategia(idEstrategia, par), idEstrategia, par);
				}
				catch(Exception e1)
				{
					Error.agregarRMI("Se produjo una excepcion de conexion remota " + e1.getMessage());
				}
			}
		});
		return botonNuevo;
	}
	
	public static void main(String[] args)
	{
		new EstrategiaGrafica(IdEstrategia.values()[((IdEstrategia) JOptionPane.showInputDialog(null, "Escoja la estrategia", "Analisis grafico", JOptionPane.QUESTION_MESSAGE, null, IdEstrategia.values(), IdEstrategia.BREAKOUT1)).ordinal()], false);
	}
}
