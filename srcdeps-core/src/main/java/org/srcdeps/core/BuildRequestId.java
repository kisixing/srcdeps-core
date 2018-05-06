/**
 * Copyright 2015-2018 Maven Source Dependencies
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
package org.srcdeps.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.srcdeps.core.BuildRequest.Verbosity;

/**
 * A view of {@link BuildRequest} that keeps only the fields whose changes require a rebuild.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 * @since 3.2.1
 */
public class BuildRequestId {

    /**
     * @param request
     *            the base for the resulting {@link BuildRequestId}
     * @return a new {@link BuildRequestId} out of the given {@link BuildRequest}
     */
    public static BuildRequestId of(BuildRequest request) {
        return new BuildRequestId(request.isAddDefaultBuildArguments(), request.isAddDefaultBuildEnvironment(),
                request.getBuildArguments(), request.getBuildEnvironment(), request.getForwardProperties(),
                request.getGavSet(), request.getScmUrls(), request.isSkipTests(), request.getSrcVersion(),
                request.getVersion(), request.getTimeoutMs(), request.getVerbosity());
    }

    private final boolean addDefaultBuildArguments;

    private final boolean addDefaultBuildEnvironment;

    private final List<String> buildArguments;

    private final Map<String, String> buildEnvironment;

    private final Set<String> forwardProperties;
    private final GavSet gavSet;
    private final String hash;
    private final List<String> scmUrls;

    private final boolean skipTests;
    private final SrcVersion srcVersion;
    private final long timeoutMs;
    private final Verbosity verbosity;

    private final String version;

    public BuildRequestId(boolean addDefaultBuildArguments, boolean addDefaultBuildEnvironment,
            List<String> buildArguments, Map<String, String> buildEnvironment, Set<String> forwardProperties,
            GavSet gavSet, List<String> scmUrls, boolean skipTests, SrcVersion srcVersion, String version,
            long timeoutMs, Verbosity verbosity) {
        super();
        this.addDefaultBuildArguments = addDefaultBuildArguments;
        this.addDefaultBuildEnvironment = addDefaultBuildEnvironment;
        this.buildArguments = buildArguments;
        this.buildEnvironment = buildEnvironment;
        this.forwardProperties = forwardProperties;
        this.gavSet = gavSet;
        this.scmUrls = scmUrls;
        this.skipTests = skipTests;
        this.srcVersion = srcVersion;
        this.timeoutMs = timeoutMs;
        this.verbosity = verbosity;
        this.version = version;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos)) {
            out.writeBoolean(addDefaultBuildArguments);
            out.writeBoolean(addDefaultBuildEnvironment);
            for (String e : buildArguments) {
                out.writeUTF(e);
            }
            for (Map.Entry<String, String> e : buildEnvironment.entrySet()) {
                out.writeUTF(e.getKey());
                out.writeUTF(e.getValue());
            }
            for (String e : forwardProperties) {
                out.writeUTF(e);
            }
            out.flush();
            try (OutputStreamWriter w = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
                gavSet.appendIncludes(w);
                gavSet.appendExcludes(w);
            }
            for (String e : scmUrls) {
                out.writeUTF(e);
            }
            out.writeBoolean(skipTests);
            out.writeUTF(srcVersion.toString());
            out.writeUTF(version);
            out.writeLong(timeoutMs);
            out.writeUTF(verbosity.name());

            out.flush();

            final byte[] bytes = baos.toByteArray();
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(bytes);
            final byte[] sha1Bytes = digest.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : sha1Bytes) {
                sb.append(String.format("%02x", b));
            }
            this.hash = sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BuildRequestId other = (BuildRequestId) obj;
        if (addDefaultBuildArguments != other.addDefaultBuildArguments)
            return false;
        if (addDefaultBuildEnvironment != other.addDefaultBuildEnvironment)
            return false;
        if (buildArguments == null) {
            if (other.buildArguments != null)
                return false;
        } else if (!buildArguments.equals(other.buildArguments))
            return false;
        if (buildEnvironment == null) {
            if (other.buildEnvironment != null)
                return false;
        } else if (!buildEnvironment.equals(other.buildEnvironment))
            return false;
        if (forwardProperties == null) {
            if (other.forwardProperties != null)
                return false;
        } else if (!forwardProperties.equals(other.forwardProperties))
            return false;
        if (gavSet == null) {
            if (other.gavSet != null)
                return false;
        } else if (!gavSet.equals(other.gavSet))
            return false;
        if (scmUrls == null) {
            if (other.scmUrls != null)
                return false;
        } else if (!scmUrls.equals(other.scmUrls))
            return false;
        if (skipTests != other.skipTests)
            return false;
        if (srcVersion == null) {
            if (other.srcVersion != null)
                return false;
        } else if (!srcVersion.equals(other.srcVersion))
            return false;
        if (timeoutMs != other.timeoutMs)
            return false;
        if (verbosity != other.verbosity)
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    /**
     * @return see {@link BuildRequest#getBuildArguments()}
     */
    public List<String> getBuildArguments() {
        return buildArguments;
    }

    /**
     * @return see {@link BuildRequest#getBuildEnvironment()}
     */
    public Map<String, String> getBuildEnvironment() {
        return buildEnvironment;
    }

    /**
     * @return see {@link BuildRequest#getBuildArguments()}
     */
    public Set<String> getForwardProperties() {
        return forwardProperties;
    }

    /**
     * @return see {@link BuildRequest#getBuildArguments()}
     */
    public GavSet getGavSet() {
        return gavSet;
    }

    /**
     * @return a hash string representing this {@link BuildRequestId} that is stable across JVM runs and
     *         implementations.
     */
    public String getHash() {
        return hash;
    }

    /**
     * @return see {@link BuildRequest#getScmUrls()}
     */
    public List<String> getScmUrls() {
        return scmUrls;
    }

    /**
     * @return see {@link BuildRequest#getSrcVersion()}
     */
    public SrcVersion getSrcVersion() {
        return srcVersion;
    }

    /**
     * @return see {@link BuildRequest#getTimeoutMs()}
     */
    public long getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * @return see {@link BuildRequest#getVerbosity()}
     */
    public Verbosity getVerbosity() {
        return verbosity;
    }

    /**
     * @return see {@link BuildRequest#getVersion()}
     */
    public String getVersion() {
        return version;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    /**
     * @return see {@link BuildRequest#isAddDefaultBuildArguments()}
     */
    public boolean isAddDefaultBuildArguments() {
        return addDefaultBuildArguments;
    }

    /**
     * @return see {@link BuildRequest#isAddDefaultBuildEnvironment()}
     */
    public boolean isAddDefaultBuildEnvironment() {
        return addDefaultBuildEnvironment;
    }

    /**
     * @return see {@link BuildRequest#isSkipTests()}
     */
    public boolean isSkipTests() {
        return skipTests;
    }

    @Override
    public String toString() {
        return "BuildRequestId [addDefaultBuildArguments=" + addDefaultBuildArguments + ", addDefaultBuildEnvironment="
                + addDefaultBuildEnvironment + ", buildArguments=" + buildArguments + ", buildEnvironment="
                + buildEnvironment + ", forwardProperties=" + forwardProperties + ", gavSet=" + gavSet + ", hash="
                + hash + ", scmUrls=" + scmUrls + ", skipTests=" + skipTests + ", srcVersion=" + srcVersion
                + ", timeoutMs=" + timeoutMs + ", verbosity=" + verbosity + ", version=" + version + "]";
    }

}