/**
 * Copyright (c) 2013 Aon eSolutions
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
package org.aon.esolutions.build.tools.util

import java.util.zip.ZipInputStream

class FileUtil {

	public static boolean downloadFile(File localFile, String address) {
		def file = new FileOutputStream(localFile)
		def out = new BufferedOutputStream(file)
		out << new URL(address).openStream()
		out.close()
	}

	public static boolean unzipFile(File zipFile, File destFile = null) {
		def result = new ZipInputStream(new FileInputStream(zipFile))
		if (destFile == null)
			destFile = zipFile.getParentFile();

		if(!destFile.exists()){
			destFile.mkdir();
		}
		result.withStream{
			def entry
			while(entry = result.nextEntry){
				if (!entry.isDirectory()){
					File currentFile = new File(destFile, entry.name)
					currentFile.parentFile?.mkdirs();

					def output = new FileOutputStream(currentFile)

					output.withStream{
						int len = 0;
						byte[] buffer = new byte[4096]
						while ((len = result.read(buffer)) > 0){
							output.write(buffer, 0, len);
						}
					}
				}
				else {
					new File(destFile, entry.name).mkdir()
				}
			}
		}
	}
}
