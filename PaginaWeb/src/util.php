<?php

/* 
 *  Archivo con funciones de utilidad general para los módulos de la aplicación
 */
 
	include("conn.php");

	// Retorna el primer valor retornado por la consulta SQL, o def si no hay resultado
	function executeScalar($sql,$def="") {
		$rs = mysql_query($sql) or die("sql syntax error");
		if ($row = mysql_fetch_array($rs)) {
			mysql_free_result($rs);
			return $row[0];
		}
		return $def;
	}
	
	//retorna la primera fila retornada por la consulta SQL
	function executeRow($sql,$def="") {
		$rs = mysql_query($sql) or die("sql syntax error");
		if ($row = mysql_fetch_array($rs)) {
			mysql_free_result($rs);
			return $row;
		}
		return $def;
	}
	
	//"Imprime" el resultado de una consulta con valores separados por coma
	function executeCSV($sql) {
		$rs = mysql_query($sql) or die("sql syntax error");
		while ($row = mysql_fetch_array($rs)) {
			$last = count($row)-1;
			for ( $i=0; $i<$last; $i++) {
				echo $row[$i], ",";
			}
			echo $row[$last],"\n";
		}
		mysql_free_result($rs);
		return $def;
	}
	
?>
