package org.aon.esolutions.build.tools.exec;

import java.util.regex.Pattern

public class Executable {
	
	private File executableFile = null;
	private StringBuffer standardOut = new StringBuffer();
	private StringBuffer standardError = new StringBuffer();

	public Executable(File executableFile) {
		this.executableFile = executableFile;
	}
	
	public Executable(Executable executable) {
		executableFile = executable.getExecutableFile();
	}
	
	/**
	 * Executes the file that was passed in on the constructor.  Optionally takes a callback
	 * for the Standard Output and Standard Error streams.  Will pass each line as the parameter
	 * into the callback.
	 * 
	 * @param stdInCallback Callback for Standard In, called once for each line
	 * @param sdtOutCallback Callback for Standard error, called once for each line
	 */
	public int run(String[] arguments, Closure stdInCallback = null, Closure sdtOutCallback = null) {
		def arugmentsWithFile = [executableFile.getAbsolutePath()]
		arugmentsWithFile.addAll(arguments)
		def proc = arugmentsWithFile.execute()
		
		proc.consumeProcessOutput(standardOut, standardError);
		proc.waitFor()
		
		if (stdInCallback)
			standardOut.eachLine(stdInCallback)
			
		if (sdtOutCallback)
			standardError.eachLine(sdtOutCallback)
			
		if (proc.exitValue()!=0){
			throw new RuntimeException("Error Executing $executableFile.  Return code ${proc.exitValue()}")
		}
		
		return proc.exitValue();
	}
	
	public String runGrepOnStandardOut(String[] arguments, String patternToMatch, int groupToReturn) {
		String foundMatch = null;
		
		run(arguments, {
			def matcher = (it =~ patternToMatch)
			if (matcher.matches())
				foundMatch = matcher[0][groupToReturn]
		})
		
		return foundMatch;
	}
	
	public String runGrepOnStandardError(String[] arguments, String patternToMatch, int groupToReturn) {
		String foundMatch = null;
		
		run(arguments, null, {
			def matcher = (it =~ patternToMatch)
			if (matcher.matches())
				foundMatch = matcher[0][groupToReturn]
		})	
		
		return foundMatch;	
	}
	
	public String getStandardOut() {
		return standardOut.toString()
	}
	
	public String getStandardError() {
		return standardError.toString();
	}
	
	
	public File getExecutableFile() {
		return executableFile;
	}
	
}
