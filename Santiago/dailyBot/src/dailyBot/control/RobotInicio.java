package dailyBot.control;
import java.io.File;
import java.io.IOException;

public class RobotInicio 
{
	public static void main(String [] args) throws InterruptedException, IOException
	{
		ProcessBuilder pb = new ProcessBuilder("sudo", "java", "-server", "-Xms256m", "-Xmx1024m", "-jar", "dailyBot.jar");
		pb.directory(new File("/home/santiago/Desktop/dailyBot/"));
		pb.start();
	}
}
