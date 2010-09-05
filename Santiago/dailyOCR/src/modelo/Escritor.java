package modelo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

import control.AdministradorHilos;
import control.Error;
import control.dailyOCR;
import control.conexion.ConexionMySql;


public class Escritor
{	
	private final LinkedBlockingQueue < ArrayList <EntradaEscritor> > entradas;
	private ArrayList <EntradaEscritor> enConstruccion;
	private String pathMeta;
	private Proceso proceso;
	private Socket socket = null;
	private PrintWriter socketOut;
	private BufferedReader socketIn;
	public volatile boolean debug = true;
	
	private void reiniciarProceso()
	{
		try
		{
			proceso.cerrar();
			try 
			{
				Thread.sleep(100000);
			} 
			catch (InterruptedException e1) 
			{
				Error.agregar("Error de interrupcion en path: " + pathMeta);
			}
			iniciarSocket();
		}
		catch(Exception e)
		{
			Error.agregar("Error reiniciando proceso, reinicando equipo");
			reiniciarEquipo();
		}
	}
	
	private void reiniciarEquipo()
	{
		new File(pathMeta + "ordenes.txt").delete();
		try 
		{
			Runtime.getRuntime().exec("shutdown now -r");
			System.exit(0);
		} 
		catch (IOException e) 
		{
			Error.agregar("Error reiniciando equipo " + e.getMessage());
			System.exit(0);
		}
	}
	
	public Escritor(String path, ArrayList < ArrayList <EntradaEscritor> > iniciales)
	{
		pathMeta = path;
		entradas = new LinkedBlockingQueue < ArrayList <EntradaEscritor> > ();
		try
		{
			for(ArrayList <EntradaEscritor> entrada : iniciales)
				entradas.put(entrada);
		}
		catch(Exception e)
		{
			Error.agregar("Error inicializando escritor en path: " + pathMeta);
		}
		try
		{
			proceso = new Proceso(path);
		}
		catch(Exception e)
		{
			Error.agregar("Error iniciando proceso, reinicando equipo");
			reiniciarEquipo();
		}
		iniciarSocket();
		enConstruccion = new ArrayList <EntradaEscritor> ();
		Thread hiloEscritor = new Thread(new Runnable()
		{
			public void run() 
			{
				while(true)
				{
					try
					{
						synchronized(entradas)
						{
							while(entradas.size() == 0)
								entradas.wait(100000);
						}
						if(debug)
						{
							Error.agregar("Notificado " + pathMeta + " " + System.currentTimeMillis());
						}
						procesar(entradas.peek());
						entradas.take();
					}
					catch(Exception e)
					{
						Error.agregar("Error en el hilo de escritura en path: " + pathMeta);
					}
				}
			}
		});
		hiloEscritor.setName("Escritor " + path);
		AdministradorHilos.agregarHilo(hiloEscritor);
	}
	
	public void terminarCiclo()
	{
		if(enConstruccion.size() != 0)
		{
			String mensaje = "";
			try 
			{
				entradas.put(enConstruccion);
				for(EntradaEscritor e : enConstruccion)
					mensaje += e.getLinea() + ";";
			} 
			catch (InterruptedException e)
			{
				Error.agregar("Error de interrupcion en path: " + pathMeta);
			}
			enConstruccion = new ArrayList <EntradaEscritor> ();
			synchronized(entradas)
			{
				if(debug)
				{
					Error.agregar("Encolando y notificando " + pathMeta + " " + mensaje + " " + System.currentTimeMillis());
				}
				entradas.notifyAll();
			}
		}
	}
	
	public synchronized void iniciarSocket()
	{
		 String s = null;
		 try
		 {
			 Thread.sleep(100000);
			 Scanner sc = new Scanner(new File(pathMeta + "port.txt"));
		     socket = new Socket(s, sc.nextInt());
		     socketOut = new PrintWriter(socket.getOutputStream(), true);
		     socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		 }
		 catch(Exception e)
		 {
			 Error.agregar(e.getMessage() + " error iniciando socket, " + pathMeta);
			 reiniciarEquipo();
		 }
	}
	
	private void escribir(ArrayList <EntradaEscritor> trabajoActual) 
	{
		try
		{
			File archivoEscritura = new File(pathMeta + "log.txt");
			if(!archivoEscritura.exists())
				archivoEscritura.createNewFile();
			FileWriter fw = new FileWriter(archivoEscritura, true);
			String mensaje = "";
			String lineaEnvio = "";
			for(EntradaEscritor entrada : trabajoActual)
			{
				String linea = entrada.getLinea();
				lineaEnvio += linea + ";" + "-";
				fw.write(linea + ";\n");
				mensaje += entrada.getLinea() + ";";
			}
			lineaEnvio = lineaEnvio.substring(0, lineaEnvio.length() - 1);
			fw.close();
			socketOut.println(lineaEnvio);
			if(debug)
				Error.agregar("Escribiendo " + mensaje);
		}
		catch(Exception e)
		{
			Error.agregar(e.getMessage() + " error escribiendo en el socket " + pathMeta);
			reiniciarEquipo();
		}
	}

	private ArrayList <String> leer(int tiempoEspera)
	{
		try
		{
			ArrayList <String> leidos = new ArrayList <String> ();
			File archivoEscritura = new File(pathMeta + "log.txt");
			socket.setSoTimeout(tiempoEspera * 5);
			String magicos = socketIn.readLine();
			Error.agregar("Leido: " + magicos + ", " + System.currentTimeMillis());
			if(!archivoEscritura.exists())
				archivoEscritura.createNewFile();
			FileWriter fw = new FileWriter(archivoEscritura, true);
			fw.write(magicos + "\n");
			fw.close();
			String [] magicosPartidos = magicos.split("-");
			for(String s : magicosPartidos)
				leidos.add(s);
			return leidos;
		}
		catch(SocketTimeoutException e)
		{
			Error.agregar("Socket no respondio en " + tiempoEspera + " esperando");
		}
		catch(Exception e)
		{
			Error.agregar(e.getMessage() + " error escribiendo en el socket");
		}
		return null;
	}
	
	private ArrayList <String> cargarEntradas(ArrayList <EntradaEscritor> trabajoActual, int tiempoExtra)
	{
		escribir(trabajoActual);
		return leer(10000 + 20000 * trabajoActual.size() + tiempoExtra);
	}
	
	protected Senal darSenal(EntradaEscritor entrada) 
	{
		return dailyOCR.darEstrategia(entrada.getId()).tienePar(entrada.getPar());
	}
	
	protected boolean procesar(EntradaEscritor entrada, String lectura)
	{
		if(entrada.isCierre())
		{
			if(lectura.endsWith("_CIERRE"))
			{
				Scanner sc = new Scanner(lectura);
		        sc.useDelimiter("\\Q;\\E");
		        int gananciaReal = sc.nextInt();
		        String stringMagico = sc.next().replace("_CIERRE", "");
		        int magico = 0;
		        boolean error = false;
		        try
		        {
		        	magico = Integer.parseInt(stringMagico);
		        }
		        catch(Exception e)
		        {
		        	error = true;
		        }
		        if(error)
		        	Error.agregar("Error procesando magico de cierre en escritor: " + pathMeta + ", respuesta: " + lectura);
		        else
		        {
		        	Senal actual = entrada.getAfectada();
		        	if(actual == null)
		        		return true;
		        	if(magico == actual.darMagico(0))
		        	{
			        	for(int i = 0; i < entrada.getNumeroLotes() - 1; i++)
							ConexionMySql.agregarEntrada(entrada.getId(), actual);
			        	actual.ponerGananciaReal(gananciaReal);
			        	ConexionMySql.agregarEntrada(entrada.getId(), actual);
		        	}
		        	else
		        	{
		        		Error.agregar("Senal no existia: " + entrada.getLinea() + ", en escritor: " + pathMeta);
			        	for(int i = 0; i < entrada.getNumeroLotes(); i++)
							ConexionMySql.agregarEntrada(entrada.getId(), actual);
			        	return false;
		        	}
		        }
				return true;
			}
			else
			{
				Error.agregar("Error, resultado de un cierre no fue OK o senal no existia, respuesta fue: " + lectura);
				Senal actual = entrada.getAfectada();
				if(actual != null)
					for(int i = 0; i < entrada.getNumeroLotes(); i++)
						ConexionMySql.agregarEntrada(entrada.getId(), actual);
				return false;
			}
		}
		Scanner sc = new Scanner(lectura);
        sc.useDelimiter("\\Q;\\E");
        int magico = sc.nextInt();
        Par par = Par.convertirPar(sc.next());
        if(entrada.getPar().equals(par))
        {
        	Senal actual = darSenal(entrada);
        	if(actual != null)
        	{
        		actual.ponerMagico(0, magico);
        		if(debug)
        		{
        			Error.agregar("Procesado " + lectura + " -> " + entrada.getId().toString() + " " + par.toString() + " " + magico + " " + System.currentTimeMillis());
        		}
        	}
        	else
        		Error.agregar("Error leyendo magicos en path: " + pathMeta + ", en estrategia: " + entrada.getId().toString() + ", no se encuentra par: " + entrada.getPar());
        }
        else
        {
        	Error.agregar("Error leyendo magicos en path: " + pathMeta + ", no coinciden: " + entrada.getPar() + " y " + par);
        }
        sc.close();
        return true;
	}

	public synchronized void procesar(ArrayList <EntradaEscritor> trabajoActual)
	{
		ArrayList <String> entradas = null;
		for(int i = 0; i < 11; i++)
		{
			entradas = cargarEntradas(trabajoActual, i == 0 ? 0 : 90000);
			if(entradas != null)
				break;
			else
			{
				if(i == 10)
				{
					Error.agregar("Error en la lecutura del socket, reiniciando despues de diez intentos");
					reiniciarEquipo();
				}
				else
				{
					Error.agregar("Error en la lecutura del socket, reiniciando proceso");
					reiniciarProceso();
				}
			}
		}
		if(trabajoActual.size() != entradas.size())
		{
			Error.agregar("Error procesando magicos en path: " + pathMeta + ", tamanos distintos");
			return;
		}
		else
		{
			try
			{
				Iterator <String> it = entradas.iterator();
				String actual = it.next();
				for(EntradaEscritor entrada : trabajoActual)
				{
					if(procesar(entrada, actual))
					{
						if(it.hasNext())
							actual = it.next();
						else
							actual = null;
					}
				}
			}
			catch(Exception e)
			{
				Error.agregar("Error procesando magicos en path: " + pathMeta + ", " + e.getMessage());
			}
		}
	}
	
	public synchronized ArrayList <String> chequearSenales() 
	{
		debug = false;
		ArrayList <EntradaEscritor> trabajoActual = new ArrayList <EntradaEscritor> ();
		trabajoActual.add(new EntradaEscritor(null, null, "GBPCHF;LIST;CLOSE;0", false));
		ArrayList <String> entradas = null;
		for(int i = 0; i < 11; i++)
		{
			entradas = cargarEntradas(trabajoActual, i == 0 ? 0 : 90000);
			if(entradas != null)
				break;
			else
			{
				if(i == 10)
				{
					Error.agregar("Error en la lecutura del socket, reiniciando despues de diez intentos");
					reiniciarEquipo();
				}
				else
				{
					Error.agregar("Error en la lecutura del socket, reiniciando proceso");
					reiniciarProceso();
				}
			}
		}
		debug = true;
		return entradas;
	}
	
	public void cerrar(SenalEntrada entrada, Senal afectada)
	{
		if(entrada.getNumeroLotes() > 5)
		{
    		Error.agregar("Mas de cinco lotes abiertos en: " + entrada.getPar().toString() + ", en el path: " + pathMeta);
		}
		afectada.setNumeroLotes(afectada.getNumeroLotes() - entrada.getNumeroLotes());
		if(afectada.darMagico(0) != 0)
		{
			if(afectada.getNumeroLotes() == 0)
			{
				enConstruccion.add(new EntradaEscritor(entrada.getEstrategia(), entrada.getPar(), entrada.getPar() + ";" + (entrada.isCompra() ? "BUY" : "SELL") + ";" + "CLOSE;" + afectada.darMagico(0), true, entrada.getNumeroLotes(), afectada));
				Error.agregar("Cerrado: " + entrada.getPar() + ";" + (entrada.isCompra() ? "BUY" : "SELL") + ";" + "CLOSE;" + afectada.darMagico(0) + " " + System.currentTimeMillis());
			}
			else
			{
				for(int i = 0; i < entrada.getNumeroLotes(); i++)
					ConexionMySql.agregarEntrada(entrada.getEstrategia(), afectada);
				Error.agregar("Cambio sin consecuencias " + entrada.getPar().toString() + " " + pathMeta + " " + System.currentTimeMillis());
			}
		}
		else
		{
			for(int i = 0; i < entrada.getNumeroLotes(); i++)
				ConexionMySql.agregarEntrada(entrada.getEstrategia(), afectada);
			Error.agregar("Cambio sin consecuencias " + entrada.getPar().toString() + " " + pathMeta + " " + System.currentTimeMillis());
		}
		if(afectada.getNumeroLotes() <= 0)
			return;
		int[] magicoCopy = afectada.darMagicoCopy();
		afectada.setMagico(Arrays.copyOfRange(magicoCopy, 0, magicoCopy.length - entrada.getNumeroLotes()));
	}

	public void abrir(SenalEntrada entrada, Senal nueva)
	{
		Estrategia estrategia = dailyOCR.darEstrategiaSenal(nueva);
		if(entrada.getNumeroLotes() > 5)
		{
    		Error.agregar("Mas de cinco lotes abiertos en: " + entrada.getPar().toString() + ", en el path: " + pathMeta);
		}
		if(estrategia.darActivo(entrada.getPar()))
		{
			enConstruccion.add(new EntradaEscritor(estrategia.getId(), entrada.getPar(), entrada.getPar() + ";" + (entrada.isCompra() ? "BUY" : "SELL") + ";OPEN;0", false)); 
			Error.agregar("Abierto: " + entrada.getPar() + ";" + (entrada.isCompra() ? "BUY" : "SELL") + ";OPEN;0" + " " + System.currentTimeMillis());
		}
		else
		{
			Error.agregar("Cambio sin consecuencias " + entrada.getPar().toString() + " " + pathMeta + " " + System.currentTimeMillis());
		}
		nueva.setMagico(new int[entrada.getNumeroLotes()]);
	}
	
	public void agregarLinea(String linea)
	{
		enConstruccion.add(new EntradaEscritor(null, null, linea, true));
	}
	
	public ArrayList < ArrayList <EntradaEscritor> > darCopiaEntradas() 
	{
		ArrayList < ArrayList <EntradaEscritor> > entradasNuevas = new ArrayList < ArrayList <EntradaEscritor> > ();
		for(ArrayList <EntradaEscritor> actual : entradas)
		{
			entradasNuevas.add(actual);
		}
		return entradasNuevas;
	}
}
