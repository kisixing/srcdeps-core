/**
 * Copyright 2015-2017 Maven Source Dependencies
 * Plugin contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.srcdeps.config.yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.srcdeps.core.BuildRequest.Verbosity;
import org.srcdeps.core.config.BuilderIo;
import org.srcdeps.core.config.Configuration;
import org.srcdeps.core.config.ConfigurationException;
import org.srcdeps.core.config.Maven;
import org.srcdeps.core.config.MavenAssertions;
import org.srcdeps.core.config.ScmRepository;
import org.srcdeps.core.config.ScmRepositoryGradle;
import org.srcdeps.core.config.ScmRepositoryMaven;
import org.srcdeps.core.config.scalar.CharStreamSource;
import org.srcdeps.core.config.scalar.Duration;
import org.srcdeps.core.config.tree.walk.DefaultsAndInheritanceVisitor;
import org.yaml.snakeyaml.constructor.ConstructorException;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class YamlConfigurationIoTest {

    @Test(expected = ConstructorException.class)
    public void readBadVersion() throws ConfigurationException, UnsupportedEncodingException, IOException {
        try (Reader in = new InputStreamReader(getClass().getResourceAsStream("/srcdeps-bad-version.yaml"), "utf-8")) {
            new YamlConfigurationIo().read(in);
        }
    }

    @Test
    public void readFull() throws ConfigurationException, UnsupportedEncodingException, IOException {
        try (Reader in = new InputStreamReader(getClass().getResourceAsStream("/srcdeps-full.yaml"), "utf-8")) {
            Configuration actual = new YamlConfigurationIo().read(in).build();
            Configuration expected = Configuration.builder() //
                    .configModelVersion("2.2") //
                    .forwardProperty("myProp1") //
                    .forwardProperty("myProp2") //
                    .builderIo(BuilderIo.builder().stdin("read:/path/to/input/file")
                            .stdout("write:/path/to/output/file").stderr("err2out"))
                    .skip(true) //
                    .sourcesDirectory(Paths.get("/home/me/.m2/srcdeps")) //
                    .verbosity(Verbosity.debug) //
                    .buildTimeout(new Duration(35, TimeUnit.MINUTES)) //
                    .maven( //
                            Maven.builder() //
                                    .versionsMavenPluginVersion("1.2") //
                                    .failWith( //
                                            MavenAssertions.failWithBuilder() //
                                                    .addDefaults(false) //
                                                    .goal("goal1") //
                                                    .goal("goal2") //
                                                    .profile("profile1") //
                                                    .profile("profile2") //
                                                    .property("property1") //
                                                    .property("property2") //
                            ) //
                                    .failWithout( //
                                            MavenAssertions.failWithoutBuilder() //
                                                    .goal("goalA") //
                                                    .goal("goalB") //
                                                    .profile("profileA") //
                                                    .profile("profileB") //
                                                    .property("propertyA") //
                                                    .property("propertyB") //
                            ) //
                    ) //
                    .repository( //
                            ScmRepository.builder() //
                                    .id("org.repo1") //
                                    .verbosity(Verbosity.trace) //
                                    .include("group1") //
                                    .include("group2:artifact2:*") //
                                    .exclude("group3") //
                                    .exclude("group4:artifact4") //
                                    .url("url1") //
                                    .url("url2") //
                                    .buildArgument("-arg1") //
                                    .buildArgument("-arg2") //
                                    .addDefaultBuildArguments(false) //
                                    .skipTests(false) //
                                    .buildTimeout(new Duration(64, TimeUnit.SECONDS)) //
                                    .maven( //
                                            ScmRepositoryMaven.builder() //
                                                    .versionsMavenPluginVersion("2.2") //
                            ) //
                                    .gradle( //
                                            ScmRepositoryGradle.builder() //
                                                    .modelTransformer(CharStreamSource.of("file:my/file")) //
                            ) //
                    ) //
                    .repository( //
                            ScmRepository.builder() //
                                    .id("org.repo2") //
                                    .include("group3:artifact3") //
                                    .include("group4:artifact4:1.2.3") //
                                    .url("url3") //
                                    .url("url4") //
                                    .buildArgument("arg3") //
                                    .addDefaultBuildArguments(false) //
                                    .skipTests(false)) //
                    .build();
            Assert.assertEquals(expected, actual);
        }
    }


    @Test
    public void readFull21Selectors() throws ConfigurationException, UnsupportedEncodingException, IOException {
        try (Reader in = new InputStreamReader(getClass().getResourceAsStream("/srcdeps-full-2.1-selectors.yaml"), "utf-8")) {
            Configuration actual = new YamlConfigurationIo().read(in).build();
            Configuration expected = Configuration.builder() //
                    .configModelVersion("2.1") //
                    .forwardProperty("myProp1") //
                    .forwardProperty("myProp2") //
                    .builderIo(BuilderIo.builder().stdin("read:/path/to/input/file")
                            .stdout("write:/path/to/output/file").stderr("err2out"))
                    .skip(true) //
                    .sourcesDirectory(Paths.get("/home/me/.m2/srcdeps")) //
                    .verbosity(Verbosity.debug) //
                    .buildTimeout(new Duration(35, TimeUnit.MINUTES)) //
                    .maven( //
                            Maven.builder() //
                                    .versionsMavenPluginVersion("1.2") //
                                    .failWith( //
                                            MavenAssertions.failWithBuilder() //
                                                    .addDefaults(false) //
                                                    .goal("goal1") //
                                                    .goal("goal2") //
                                                    .profile("profile1") //
                                                    .profile("profile2") //
                                                    .property("property1") //
                                                    .property("property2") //
                            ) //
                                    .failWithout( //
                                            MavenAssertions.failWithoutBuilder() //
                                                    .goal("goalA") //
                                                    .goal("goalB") //
                                                    .profile("profileA") //
                                                    .profile("profileB") //
                                                    .property("propertyA") //
                                                    .property("propertyB") //
                            ) //
                    ) //
                    .repository( //
                            ScmRepository.builder() //
                                    .id("org.repo1") //
                                    .verbosity(Verbosity.trace) //
                                    .include("group1") //
                                    .include("group2:artifact2:*") //
                                    .url("url1") //
                                    .url("url2") //
                                    .buildArgument("-arg1") //
                                    .buildArgument("-arg2") //
                                    .addDefaultBuildArguments(false) //
                                    .skipTests(false) //
                                    .buildTimeout(new Duration(64, TimeUnit.SECONDS)) //
                                    .maven( //
                                            ScmRepositoryMaven.builder() //
                                                    .versionsMavenPluginVersion("2.2") //
                            ) //
                    ) //
                    .repository( //
                            ScmRepository.builder() //
                                    .id("org.repo2") //
                                    .include("group3:artifact3") //
                                    .include("group4:artifact4:1.2.3") //
                                    .url("url3") //
                                    .url("url4") //
                                    .buildArgument("arg3") //
                                    .addDefaultBuildArguments(false) //
                                    .skipTests(false)) //
                    .build();
            Assert.assertEquals(expected, actual);
        }
    }
    @Test
    public void readMinimal() throws ConfigurationException, UnsupportedEncodingException, IOException {
        try (Reader in = new InputStreamReader(getClass().getResourceAsStream("/srcdeps-minimal.yaml"), "utf-8")) {
            Configuration actual = new YamlConfigurationIo() //
                    .read(in) //
                    .accept(new DefaultsAndInheritanceVisitor()) //
                    .build();
            Configuration expected = Configuration.builder() //
                    .sourcesDirectory(Paths.get("/home/me/.m2/srcdeps")) //
                    .repository( //
                            ScmRepository.builder() //
                                    .id("srcdepsTestArtifact") //
                                    .include("org.l2x6.maven.srcdeps.itest") //
                                    .url("git:https://github.com/srcdeps/srcdeps-test-artifact.git") //
                    ) //
                    .accept(new DefaultsAndInheritanceVisitor()) //
                    .build();
            Assert.assertEquals(expected, actual);
        }
    }

}
