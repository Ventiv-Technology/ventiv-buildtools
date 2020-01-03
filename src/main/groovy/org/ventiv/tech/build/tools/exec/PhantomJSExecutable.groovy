/*
 * Copyright (c) 2020 Ventiv Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ventiv.tech.build.tools.exec

import org.ventiv.tech.build.tools.util.FileUtil;
import org.ventiv.tech.build.tools.util.OSUtil;

public class PhantomJSExecutable extends Executable {

	public PhantomJSExecutable() {
		super(ExecutableFinder.findExecutable("phantomjs"))
	}
	
	public boolean isInstalled() {
		return this.executableFile != null;
	}
	
	public PhantomJSExecutable install(File dirToInstall) {
		if (isInstalled())
			return this;
			
		// Ensure we're not trying to install into a file
		assert dirToInstall.isFile() == false;
		
		// Make the directories
		dirToInstall.mkdirs();
		
		if (OSUtil.isWindows())
			installWindows(dirToInstall);
			
		return this;		
	}
	
	protected boolean installWindows(File dirToInstall) {
		File zipFile = new File(dirToInstall, "phantomjs-1.9.0-windows.zip");
		
		FileUtil.downloadFile(zipFile, "https://phantomjs.googlecode.com/files/phantomjs-1.9.0-windows.zip");
		FileUtil.unzipFile(zipFile)
		
		setExecutableFile(new File(zipFile.getParentFile(), "phantomjs-1.9.0-windows/phantomjs.exe"));
		return getExecutableFile().exists();
	}
	
	protected boolean installLinux(File dirToInstall) {
		File zipFile = new File(dirToInstall, "phantomjs-1.9.0-linux-x86_64.tar.bz2");
		
		FileUtil.downloadFile(zipFile, "https://phantomjs.googlecode.com/files/phantomjs-1.9.0-linux-x86_64.tar.bz2");
		FileUtil.unzipFile(zipFile)
		
		setExecutableFile(new File(zipFile.getParentFile(), "phantomjs"));
		return getExecutableFile().exists();
	}
	
}
