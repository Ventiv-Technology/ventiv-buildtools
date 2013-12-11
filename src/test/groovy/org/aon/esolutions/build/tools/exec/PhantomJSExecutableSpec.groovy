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
package org.aon.esolutions.build.tools.exec

import spock.lang.Specification

/**
 *
 *
 * @author John Crygier
 */
class PhantomJSExecutableSpec extends Specification {

    private File tempInstallLocation;

    def setup() {
        tempInstallLocation = File.createTempFile("phantomJs", "installable")
        tempInstallLocation.delete();
        tempInstallLocation.mkdir();
    }

    def cleanup() {
        tempInstallLocation.deleteDir();
    }

    def "ensure installing twice doesn't fail"() {
        when:
        PhantomJSExecutable executable = new PhantomJSExecutable();
        File tempInstallLocation = File.createTempFile("phantomJs", "installable")
        tempInstallLocation.delete();
        tempInstallLocation.mkdir();
        def installResponse = executable.install(tempInstallLocation)

        then:
        tempInstallLocation.listFiles().size() > 0
        installResponse == executable

        when:
        def newInstallResponse = executable.install(tempInstallLocation)

        then:
        tempInstallLocation.listFiles().size() > 0
        newInstallResponse == executable
    }
}
