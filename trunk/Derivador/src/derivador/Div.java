/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package derivador;

import java.util.Hashtable;

/**
 *
 * @author sebastian
 */
public class Div implements Funcion{
    Funcion izq,der;
     
     public Div(Funcion izq,Funcion der){
         this.izq=izq;
         this.der=der;
     }

    public double evaluar(Hashtable<String, Integer> Table) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Funcion derivar(String var) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
