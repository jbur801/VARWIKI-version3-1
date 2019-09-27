
package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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

	public RunBash(String command){
		_command = command;
		_pb = new ProcessBuilder("bash", "-c", _command);
		
		//sets the working directory of the processbuilder to be in the current folder. There is a problem with open jdk where default wd is home. oracle does not have this problem
		_pb.directory(new File(ClassLoader.getSystemClassLoader().getResource(".").getPath()));
	}


	/**
	 * all bash commands are run on a thread different to the applicaiton thread to prevent GUI freezing.
	 */
	@Override
	protected List<String> call() throws Exception {
		try {

			Process process = _pb.start();

			while(process.isAlive()) {	
			}
			

			BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;

			while ((line = stdOut.readLine()) != null) {
				_stdOut.add(line);
			}
			process.destroy();

			//System.out.println(_stdOut);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return _stdOut;
	}
}

