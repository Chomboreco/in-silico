package dailyBot.analisis;

import dailyBot.control.Error;
import dailyBot.modelo.Proveedor.IdProveedor;
import dailyBot.vista.VentanaPrincipal;

public class RangosProveedor extends Rangos
{
	private static final long serialVersionUID = 2028970098750246429L;
	
	IdProveedor id;
	
	public RangosProveedor(IdProveedor i)
	{
		id = i;
	}
	
	@Override
	public boolean cumple(RegistroHistorial registro, boolean ignorarInfo, String enviarMensaje) 
	{
		try 
		{
			if(id == null || VentanaPrincipal.conexion.darActivoProveedor(id.ordinal(), registro.id.ordinal(), registro.par.ordinal()))
				return VentanaPrincipal.conexion.darRangosProveedor(id.ordinal(), registro.id.ordinal(), registro.par.ordinal()).cumple(registro, true, "");
			else
				return false;
		} 
        catch (Exception e)
        {        	
        	Error.agregar(e.getMessage() + " Error haciendo la conexion RMI");
        	System.exit(0);
        	return false;
        }
	}
}
