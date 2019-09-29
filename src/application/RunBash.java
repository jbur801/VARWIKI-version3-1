
package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;


/**
 * 
 * @author davidxiao
 *	This class is used for bash command line interaction.
 */
public class RunBash extends Task<List<String>>{

	private String _command;
	private List<String> _stdOut = new ArrayList<String>();
	private ProcessBuilder _pb;
	private String _stdError;
	private int exitStatus;

	public RunBash(String command){
		_command = command;
		_pb = new ProcessBuilder("bash", "-c", _command);
		
		System.out.println(_command);
		//sets the working directory of the processbuilder to be in the current folder. There is a problem with open jdk where default wd is home. oracle does not have this problem
		try {
			String pwd = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
			_pb.directory(new File(pwd.substring(0,pwd.lastIndexOf("/"))));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}


	/**
	 * all bash commands are run on a thread different to the application thread to prevent GUI freezing.
	 */
	@Override
	protected List<String> call() throws Exception {
		try {

			Process process = _pb.start();

			exitStatus=process.waitFor();
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			_stdError=stdError.readLine();
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = stdOut.readLine()) != null) {
				_stdOut.add(line);
			}

		}catch(Exception e) {
			e.printStackTrace();
		}
		return _stdOut;
	}


	public String returnError() {
		return _stdError;
	}
	
	public int getExitStatus() {
		return exitStatus;
	}
}

